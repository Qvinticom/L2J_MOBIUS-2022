/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.model.actor.stat;

import java.util.concurrent.atomic.AtomicInteger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.data.xml.PetDataTable;
import org.l2jmobius.gameserver.enums.PlayerCondOverride;
import org.l2jmobius.gameserver.model.PetLevelData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.ClassMaster;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.transform.TransformTemplate;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.stats.Formulas;
import org.l2jmobius.gameserver.model.stats.MoveType;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExBrExtraUserInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExVitalityPointInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.util.Util;

public class PlayerStat extends PlayableStat
{
	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch
	private float _vitalityPoints = 1;
	private byte _vitalityLevel = 0;
	private long _startingXp;
	/** Player's maximum cubic count. */
	private int _maxCubicCount = 1;
	/** Player's maximum talisman count. */
	private final AtomicInteger _talismanSlots = new AtomicInteger();
	private boolean _cloakSlot = false;
	private boolean _pausedNevitHourglass = false;
	
	public static final int[] VITALITY_LEVELS =
	{
		240,
		2000,
		13000,
		17000,
		20000
	};
	
	public static final int MAX_VITALITY_POINTS = VITALITY_LEVELS[4];
	public static final int MIN_VITALITY_POINTS = 1;
	
	public PlayerStat(Player player)
	{
		super(player);
	}
	
	@Override
	public boolean addExp(long value)
	{
		final Player player = getActiveChar();
		
		// Allowed to gain exp?
		if (!getActiveChar().getAccessLevel().canGainExp())
		{
			return false;
		}
		
		if (!super.addExp(value))
		{
			return false;
		}
		
		// Set new karma
		if (!player.isCursedWeaponEquipped() && (player.getKarma() > 0) && !player.isInsideZone(ZoneId.PVP))
		{
			final int karmaLost = Formulas.calculateKarmaLost(player, value);
			if (karmaLost > 0)
			{
				player.setKarma(player.getKarma() - karmaLost);
				final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1);
				msg.addInt(player.getKarma());
				player.sendPacket(msg);
			}
		}
		
		// EXP status update currently not used in retail
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		return true;
	}
	
	public boolean addExpAndSp(double addToExpValue, double addToSpValue, boolean useBonuses)
	{
		final Player player = getActiveChar();
		
		// Allowed to gain exp/sp?
		if (!player.getAccessLevel().canGainExp())
		{
			return false;
		}
		
		double addToExp = addToExpValue;
		double addToSp = addToSpValue;
		
		// Premium rates
		if (player.hasPremiumStatus())
		{
			addToExp *= Config.PREMIUM_RATE_XP;
			addToSp *= Config.PREMIUM_RATE_SP;
		}
		
		final double baseExp = addToExp;
		final double baseSp = addToSp;
		double bonusExp = 1.;
		double bonusSp = 1.;
		
		// Start Nevit's Hourglass
		if (Config.NEVIT_ENABLED && (addToExp > 0) && !player.isInsideZone(ZoneId.PEACE))
		{
			player.startNevitHourglassTask();
			
			if (player.getEffectList().getFirstEffect(EffectType.NEVITS_HOURGLASS) == null)
			{
				setPausedNevitHourglassStatus(false);
			}
		}
		
		if (useBonuses)
		{
			bonusExp = getExpBonusMultiplier();
			bonusSp = getSpBonusMultiplier();
		}
		
		if (Config.NEVIT_ENABLED && (addToExp > 0) && !player.isInsideZone(ZoneId.PEACE))
		{
			player.getNevitSystem().startAdventTask();
		}
		
		addToExp *= bonusExp;
		addToSp *= bonusSp;
		double ratioTakenByPlayer = 0;
		
		// if this player has a pet and it is in his range he takes from the owner's Exp, give the pet Exp now
		if (player.hasPet() && Util.checkIfInShortRange(Config.ALT_PARTY_RANGE, player, player.getSummon(), false))
		{
			final Pet pet = (Pet) player.getSummon();
			ratioTakenByPlayer = pet.getPetLevelData().getOwnerExpTaken() / 100f;
			
			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if (ratioTakenByPlayer > 1)
			{
				ratioTakenByPlayer = 1;
			}
			
			if (!pet.isDead())
			{
				pet.addExpAndSp(addToExp * (1 - ratioTakenByPlayer), addToSp * (1 - ratioTakenByPlayer));
			}
			
			// now adjust the max ratio to avoid the owner earning negative exp/sp
			addToExp *= ratioTakenByPlayer;
			addToSp *= ratioTakenByPlayer;
		}
		
		final long finalExp = Math.round(addToExp);
		final long finalSp = Math.round(addToSp);
		final boolean expAdded = addExp(finalExp);
		final boolean spAdded = addSp(finalSp);
		SystemMessage sm = null;
		if (!expAdded && spAdded)
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_SP);
			sm.addLong(finalSp);
		}
		else if (expAdded && !spAdded)
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_EXPERIENCE);
			sm.addLong(finalExp);
		}
		else
		{
			if ((addToExp - baseExp) > 0)
			{
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4);
				sm.addLong(finalExp);
				sm.addLong(Math.round(addToExp - baseExp));
				sm.addLong(finalSp);
				sm.addLong(Math.round(addToSp - baseSp));
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP);
				sm.addLong((long) addToExp);
				sm.addLong((long) addToSp);
			}
		}
		player.sendPacket(sm);
		return true;
	}
	
	@Override
	public boolean removeExpAndSp(long addToExp, long addToSp)
	{
		return removeExpAndSp(addToExp, addToSp, true);
	}
	
	public boolean removeExpAndSp(long addToExp, long addToSp, boolean sendMessage)
	{
		final int level = getLevel();
		if (!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		if (sendMessage)
		{
			// Send a Server->Client System Message to the Player
			SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_EXPERIENCE_HAS_DECREASED_BY_S1);
			sm.addLong(addToExp);
			getActiveChar().sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
			sm.addLong(addToSp);
			getActiveChar().sendPacket(sm);
			if (getLevel() < level)
			{
				getActiveChar().broadcastStatusUpdate();
			}
		}
		return true;
	}
	
	@Override
	public boolean addLevel(byte value)
	{
		if ((getLevel() + value) > (ExperienceData.getInstance().getMaxLevel() - 1))
		{
			return false;
		}
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLevelChanged(getActiveChar(), getLevel(), getLevel() + value), getActiveChar());
		
		final boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
		{
			if (!Config.DISABLE_TUTORIAL)
			{
				final QuestState qs = getActiveChar().getQuestState("Q00255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE40", null, getActiveChar());
				}
			}
			
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
			getActiveChar().sendPacket(SystemMessageId.YOUR_LEVEL_HAS_INCREASED);
			
			ClassMaster.showQuestionMark(getActiveChar());
		}
		
		// Give AutoGet skills and all normal skills if Auto-Learn is activated.
		getActiveChar().rewardSkills();
		
		if (getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}
		if (getActiveChar().isInParty())
		{
			getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level
		}
		
		if (getActiveChar().isTransformed() || getActiveChar().isInStance())
		{
			getActiveChar().getTransformation().onLevelUp(getActiveChar());
		}
		
		// Synchronize level with pet if possible.
		if (getActiveChar().hasPet())
		{
			final Pet pet = (Pet) getActiveChar().getSummon();
			if (pet.getPetData().isSynchLevel() && (pet.getLevel() != getLevel()))
			{
				pet.getStat().setLevel(getLevel());
				pet.getStat().getExpForLevel(getActiveChar().getLevel());
				pet.setCurrentHp(pet.getMaxHp());
				pet.setCurrentMp(pet.getMaxMp());
				pet.broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
				pet.updateAndBroadcastStatus(1);
			}
		}
		
		final StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);
		
		// Update the overloaded status of the Player
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the Player
		getActiveChar().refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the Player
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		getActiveChar().sendPacket(new ExBrExtraUserInfo(getActiveChar()));
		// Nevit System
		if (Config.NEVIT_ENABLED)
		{
			getActiveChar().sendPacket(new ExVoteSystemInfo(getActiveChar()));
			getActiveChar().getNevitSystem().addPoints(2000);
		}
		
		return levelIncreased;
	}
	
	@Override
	public boolean addSp(long value)
	{
		if (!super.addSp(value))
		{
			return false;
		}
		
		final StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.SP, (int) getSp());
		getActiveChar().sendPacket(su);
		
		return true;
	}
	
	@Override
	public long getExpForLevel(int level)
	{
		return ExperienceData.getInstance().getExpForLevel(level);
	}
	
	@Override
	public Player getActiveChar()
	{
		return (Player) super.getActiveChar();
	}
	
	@Override
	public long getExp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
		}
		return super.getExp();
	}
	
	public long getBaseExp()
	{
		return super.getExp();
	}
	
	@Override
	public void setExp(long value)
	{
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		}
		else
		{
			super.setExp(value);
		}
	}
	
	public void setStartingExp(long value)
	{
		if (Config.BOTREPORT_ENABLE)
		{
			_startingXp = value;
		}
	}
	
	public long getStartingExp()
	{
		return _startingXp;
	}
	
	/**
	 * Gets the maximum cubic count.
	 * @return the maximum cubic count
	 */
	public int getMaxCubicCount()
	{
		return _maxCubicCount;
	}
	
	/**
	 * Sets the maximum cubic count.
	 * @param cubicCount the maximum cubic count
	 */
	public void setMaxCubicCount(int cubicCount)
	{
		_maxCubicCount = cubicCount;
	}
	
	/**
	 * Gets the maximum talisman count.
	 * @return the maximum talisman count
	 */
	public int getTalismanSlots()
	{
		return _talismanSlots.get();
	}
	
	public void addTalismanSlots(int count)
	{
		_talismanSlots.addAndGet(count);
	}
	
	public boolean canEquipCloak()
	{
		return _cloakSlot;
	}
	
	public void setCloakSlotStatus(boolean cloakSlot)
	{
		_cloakSlot = cloakSlot;
	}
	
	public boolean hasPausedNevitHourglass()
	{
		return _pausedNevitHourglass;
	}
	
	public void setPausedNevitHourglassStatus(boolean value)
	{
		_pausedNevitHourglass = value;
		getActiveChar().sendPacket(new ExVoteSystemInfo(getActiveChar()));
	}
	
	@Override
	public byte getLevel()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		}
		return super.getLevel();
	}
	
	public byte getBaseLevel()
	{
		return super.getLevel();
	}
	
	@Override
	public void setLevel(byte value)
	{
		byte level = value;
		if (level > (ExperienceData.getInstance().getMaxLevel() - 1))
		{
			level = (byte) (ExperienceData.getInstance().getMaxLevel() - 1);
		}
		
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		}
		else
		{
			super.setLevel(level);
		}
	}
	
	@Override
	public int getMaxCp()
	{
		// Get the Max CP (base+modifier) of the Player
		final int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stat.MAX_CP, getActiveChar().getTemplate().getBaseCpMax(getActiveChar().getLevel()));
		if (val != _oldMaxCp)
		{
			_oldMaxCp = val;
			
			// Launch a regen task if the new Max CP is higher than the old one
			if (getActiveChar().getStatus().getCurrentCp() != val)
			{
				getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp()); // trigger start of regeneration
			}
		}
		return val;
	}
	
	@Override
	public int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the Player
		final int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stat.MAX_HP, getActiveChar().getTemplate().getBaseHpMax(getActiveChar().getLevel()));
		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;
			
			// Launch a regen task if the new Max HP is higher than the old one
			if (getActiveChar().getStatus().getCurrentHp() != val)
			{
				getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}
		return val;
	}
	
	@Override
	public int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the Player
		final int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stat.MAX_MP, getActiveChar().getTemplate().getBaseMpMax(getActiveChar().getLevel()));
		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;
			
			// Launch a regen task if the new Max MP is higher than the old one
			if (getActiveChar().getStatus().getCurrentMp() != val)
			{
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}
		return val;
	}
	
	@Override
	public long getSp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		}
		return super.getSp();
	}
	
	public long getBaseSp()
	{
		return super.getSp();
	}
	
	@Override
	public void setSp(long value)
	{
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		}
		else
		{
			super.setSp(value);
		}
	}
	
	/**
	 * @param type movement type
	 * @return the base move speed of given movement type.
	 */
	@Override
	public double getBaseMoveSpeed(MoveType type)
	{
		final Player player = getActiveChar();
		if (player.isTransformed())
		{
			final TransformTemplate template = player.getTransformation().getTemplate(player);
			if (template != null)
			{
				return template.getBaseMoveSpeed(type);
			}
		}
		else if (player.isMounted())
		{
			final PetLevelData data = PetDataTable.getInstance().getPetLevelData(player.getMountNpcId(), player.getMountLevel());
			if (data != null)
			{
				return data.getSpeedOnRide(type);
			}
		}
		return super.getBaseMoveSpeed(type);
	}
	
	@Override
	public double getRunSpeed()
	{
		double val = super.getRunSpeed() + Config.RUN_SPD_BOOST;
		
		// Apply max run speed cap.
		if ((val > Config.MAX_RUN_SPEED) && !getActiveChar().canOverrideCond(PlayerCondOverride.MAX_STATS_VALUE))
		{
			return Config.MAX_RUN_SPEED;
		}
		
		// Check for mount penalties
		if (getActiveChar().isMounted())
		{
			// if level diff with mount >= 10, it decreases move speed by 50%
			if ((getActiveChar().getMountLevel() - getActiveChar().getLevel()) >= 10)
			{
				val /= 2;
			}
			// if mount is hungry, it decreases move speed by 50%
			if (getActiveChar().isHungry())
			{
				val /= 2;
			}
		}
		
		return val;
	}
	
	@Override
	public double getWalkSpeed()
	{
		double val = super.getWalkSpeed() + Config.RUN_SPD_BOOST;
		
		// Apply max run speed cap.
		if ((val > Config.MAX_RUN_SPEED) && !getActiveChar().canOverrideCond(PlayerCondOverride.MAX_STATS_VALUE))
		{
			return Config.MAX_RUN_SPEED;
		}
		
		if (getActiveChar().isMounted())
		{
			// if level diff with mount >= 10, it decreases move speed by 50%
			if ((getActiveChar().getMountLevel() - getActiveChar().getLevel()) >= 10)
			{
				val /= 2;
			}
			// if mount is hungry, it decreases move speed by 50%
			if (getActiveChar().isHungry())
			{
				val /= 2;
			}
		}
		
		return val;
	}
	
	@Override
	public double getPAtkSpd()
	{
		final double val = super.getPAtkSpd();
		if ((val > Config.MAX_PATK_SPEED) && !getActiveChar().canOverrideCond(PlayerCondOverride.MAX_STATS_VALUE))
		{
			return Config.MAX_PATK_SPEED;
		}
		return val;
	}
	
	private void updateVitalityLevel(boolean quiet)
	{
		final byte level;
		if (_vitalityPoints <= VITALITY_LEVELS[0])
		{
			level = 0;
		}
		else if (_vitalityPoints <= VITALITY_LEVELS[1])
		{
			level = 1;
		}
		else if (_vitalityPoints <= VITALITY_LEVELS[2])
		{
			level = 2;
		}
		else if (_vitalityPoints <= VITALITY_LEVELS[3])
		{
			level = 3;
		}
		else
		{
			level = 4;
		}
		
		if (!quiet && (level != _vitalityLevel))
		{
			if (level < _vitalityLevel)
			{
				getActiveChar().sendPacket(SystemMessageId.YOUR_VITALITY_HAS_DECREASED);
			}
			else
			{
				getActiveChar().sendPacket(SystemMessageId.YOUR_VITALITY_HAS_INCREASED);
			}
			if (level == 0)
			{
				getActiveChar().sendPacket(SystemMessageId.YOUR_VITALITY_IS_FULLY_EXHAUSTED);
			}
			else if (level == 4)
			{
				getActiveChar().sendPacket(SystemMessageId.YOUR_VITALITY_IS_AT_MAXIMUM);
			}
		}
		
		_vitalityLevel = level;
	}
	
	/*
	 * Return current vitality points in integer format
	 */
	public int getVitalityPoints()
	{
		return (int) _vitalityPoints;
	}
	
	/*
	 * Set current vitality points to this value if quiet = true - does not send system messages
	 */
	public void setVitalityPoints(int value, boolean quiet)
	{
		final int points = Math.min(Math.max(value, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
		if (points == _vitalityPoints)
		{
			return;
		}
		
		_vitalityPoints = points;
		updateVitalityLevel(quiet);
		getActiveChar().sendPacket(new ExVitalityPointInfo(getVitalityPoints()));
	}
	
	public synchronized void updateVitalityPoints(float value, boolean useRates, boolean quiet)
	{
		if ((value == 0) || !Config.ENABLE_VITALITY)
		{
			return;
		}
		
		float points = value;
		if (useRates)
		{
			if (getActiveChar().isLucky())
			{
				return;
			}
			
			if (points < 0) // vitality consumed
			{
				int stat = (int) calcStat(Stat.VITALITY_CONSUME_RATE, 1, getActiveChar(), null);
				if (getActiveChar().getNevitSystem().isAdventBlessingActive())
				{
					stat = -10; // increase Vitality During Blessing
				}
				
				if (stat == 0)
				{
					return;
				}
				if (stat < 0)
				{
					points = -points;
				}
			}
			
			if (points > 0)
			{
				// vitality increased
				points *= Config.RATE_VITALITY_GAIN;
			}
			else
			{
				// vitality decreased
				points *= Config.RATE_VITALITY_LOST;
			}
		}
		
		if (points > 0)
		{
			points = Math.min(_vitalityPoints + points, MAX_VITALITY_POINTS);
		}
		else
		{
			points = Math.max(_vitalityPoints + points, MIN_VITALITY_POINTS);
		}
		
		if (Math.abs(points - _vitalityPoints) <= 1e-6)
		{
			return;
		}
		
		_vitalityPoints = points;
		updateVitalityLevel(quiet);
	}
	
	public double getVitalityMultiplier()
	{
		double vitality = 1.0;
		if (Config.ENABLE_VITALITY)
		{
			switch (getVitalityLevel())
			{
				case 1:
				{
					vitality = Config.RATE_VITALITY_LEVEL_1;
					break;
				}
				case 2:
				{
					vitality = Config.RATE_VITALITY_LEVEL_2;
					break;
				}
				case 3:
				{
					vitality = Config.RATE_VITALITY_LEVEL_3;
					break;
				}
				case 4:
				{
					vitality = Config.RATE_VITALITY_LEVEL_4;
					break;
				}
			}
		}
		return vitality;
	}
	
	/**
	 * @return the _vitalityLevel
	 */
	public byte getVitalityLevel()
	{
		if (getActiveChar().getNevitSystem().isAdventBlessingActive())
		{
			return 4;
		}
		return _vitalityLevel;
	}
	
	public double getExpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double nevits = 1.0;
		double hunting = 1.0;
		double bonusExp = 1.0;
		
		// Bonus from Vitality System
		vitality = getVitalityMultiplier();
		
		// Bonus from Nevit's Blessing
		nevits = getActiveChar().getNevitHourglassMultiplier();
		
		// Bonus from Nevit's Hunting
		// TODO: Nevit's hunting bonus
		
		// Bonus exp from skills
		bonusExp = 1 + (calcStat(Stat.BONUS_EXP, 0, null, null) / 100);
		if (vitality > 1.0)
		{
			bonus += (vitality - 1);
		}
		if (nevits > 1.0)
		{
			bonus += (nevits - 1);
		}
		if (hunting > 1.0)
		{
			bonus += (hunting - 1);
		}
		if (bonusExp > 1)
		{
			bonus += (bonusExp - 1);
		}
		
		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		if (Config.MAX_BONUS_EXP > 0)
		{
			bonus = Math.min(bonus, Config.MAX_BONUS_EXP);
		}
		
		return bonus;
	}
	
	public double getSpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double nevits = 1.0;
		double hunting = 1.0;
		double bonusSp = 1.0;
		
		// Bonus from Vitality System
		vitality = getVitalityMultiplier();
		
		// Bonus from Nevit's Blessing
		nevits = getActiveChar().getNevitHourglassMultiplier();
		
		// Bonus from Nevit's Hunting
		// TODO: Nevit's hunting bonus
		
		// Bonus sp from skills
		bonusSp = 1 + (calcStat(Stat.BONUS_SP, 0, null, null) / 100);
		if (vitality > 1.0)
		{
			bonus += (vitality - 1);
		}
		if (nevits > 1.0)
		{
			bonus += (nevits - 1);
		}
		if (hunting > 1.0)
		{
			bonus += (hunting - 1);
		}
		if (bonusSp > 1)
		{
			bonus += (bonusSp - 1);
		}
		
		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		if (Config.MAX_BONUS_SP > 0)
		{
			bonus = Math.min(bonus, Config.MAX_BONUS_SP);
		}
		
		return bonus;
	}
}
