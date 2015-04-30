/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.actor.stat;

import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.ExperienceData;
import com.l2jserver.gameserver.data.xml.impl.PetDataTable;
import com.l2jserver.gameserver.enums.PartySmallWindowUpdateType;
import com.l2jserver.gameserver.enums.UserInfoType;
import com.l2jserver.gameserver.model.L2PetLevelData;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.actor.transform.TransformTemplate;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jserver.gameserver.model.stats.Formulas;
import com.l2jserver.gameserver.model.stats.MoveType;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jserver.gameserver.network.serverpackets.ExAcquireAPSkillList;
import com.l2jserver.gameserver.network.serverpackets.ExVitalityPointInfo;
import com.l2jserver.gameserver.network.serverpackets.ExVoteSystemInfo;
import com.l2jserver.gameserver.network.serverpackets.PartySmallWindowUpdate;
import com.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jserver.gameserver.network.serverpackets.SocialAction;
import com.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.network.serverpackets.UserInfo;
import com.l2jserver.gameserver.network.serverpackets.friend.L2FriendStatus;
import com.l2jserver.gameserver.util.Util;

public class PcStat extends PlayableStat
{
	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch
	private long _startingXp;
	/** Player's maximum cubic count. */
	private int _maxCubicCount = 1;
	/** Player's maximum talisman count. */
	private final AtomicInteger _talismanSlots = new AtomicInteger();
	private boolean _cloakSlot = false;
	
	public static final int MAX_VITALITY_POINTS = 140000;
	public static final int MIN_VITALITY_POINTS = 1;
	public static String VITALITY_VARIABLE = "vitality_points";
	
	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();
		
		// Allowed to gain exp?
		if (!getActiveChar().getAccessLevel().canGainExp())
		{
			return false;
		}
		if (Config.SERVER_CLASSIC_SUPPORT && (getActiveChar().getLevel() >= Config.MAX_CLASSIC_PLAYER_LEVEL))
		{
			return false;
		}
		
		if (!super.addExp(value))
		{
			return false;
		}
		
		// Set new karma
		if (!activeChar.isCursedWeaponEquipped() && (activeChar.getKarma() > 0) && (activeChar.isGM() || !activeChar.isInsideZone(ZoneId.PVP)))
		{
			int karmaLost = Formulas.calculateKarmaLost(activeChar, value);
			if (karmaLost > 0)
			{
				activeChar.setKarma(activeChar.getKarma() - karmaLost);
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_REPUTATION_HAS_BEEN_CHANGED_TO_S1);
				msg.addInt(activeChar.getKarma());
				activeChar.sendPacket(msg);
			}
		}
		
		// EXP status update currently not used in retail
		activeChar.sendPacket(new UserInfo(activeChar));
		return true;
	}
	
	public boolean addExpAndSp(long addToExp, long addToSp, boolean useBonuses)
	{
		L2PcInstance activeChar = getActiveChar();
		
		// Allowed to gain exp/sp?
		if (!activeChar.getAccessLevel().canGainExp())
		{
			return false;
		}
		
		long baseExp = addToExp;
		long baseSp = addToSp;
		
		double bonusExp = 1.;
		double bonusSp = 1.;
		
		if (useBonuses)
		{
			bonusExp = getExpBonusMultiplier();
			bonusSp = getSpBonusMultiplier();
		}
		
		addToExp *= bonusExp;
		addToSp *= bonusSp;
		
		if (activeChar.hasPremiumStatus())
		{
			addToExp *= Config.PREMIUM_RATE_XP;
			addToSp *= Config.PREMIUM_RATE_SP;
		}
		
		float ratioTakenByPlayer = 0;
		
		// if this player has a pet and it is in his range he takes from the owner's Exp, give the pet Exp now
		final L2Summon sPet = activeChar.getPet();
		if ((sPet != null) && Util.checkIfInShortRadius(Config.ALT_PARTY_RANGE, activeChar, sPet, false))
		{
			L2PetInstance pet = (L2PetInstance) sPet;
			ratioTakenByPlayer = pet.getPetLevelData().getOwnerExpTaken() / 100f;
			
			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if (ratioTakenByPlayer > 1)
			{
				ratioTakenByPlayer = 1;
			}
			
			if (!pet.isDead())
			{
				pet.addExpAndSp((long) (addToExp * (1 - ratioTakenByPlayer)), (int) (addToSp * (1 - ratioTakenByPlayer)));
			}
			
			// now adjust the max ratio to avoid the owner earning negative exp/sp
			addToExp = (long) (addToExp * ratioTakenByPlayer);
			addToSp = (int) (addToSp * ratioTakenByPlayer);
		}
		
		if (!addExp(addToExp))
		{
			addToExp = 0;
		}
		
		if (!addSp(addToSp))
		{
			addToSp = 0;
		}
		
		if ((addToExp == 0) && (addToSp == 0))
		{
			return false;
		}
		
		SystemMessage sm = null;
		if ((addToExp == 0) && (addToSp != 0))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_SP);
			sm.addLong(addToSp);
		}
		else if ((addToSp == 0) && (addToExp != 0))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_XP);
			sm.addLong(addToExp);
		}
		else
		{
			if ((addToExp - baseExp) > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_XP_BONUS_S2_AND_S3_SP_BONUS_S4);
				sm.addLong(addToExp);
				sm.addLong(addToExp - baseExp);
				sm.addLong(addToSp);
				sm.addLong(addToSp - baseSp);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_XP_AND_S2_SP);
				sm.addLong(addToExp);
				sm.addLong(addToSp);
			}
		}
		activeChar.sendPacket(sm);
		return true;
	}
	
	@Override
	public boolean removeExpAndSp(long addToExp, long addToSp)
	{
		return removeExpAndSp(addToExp, addToSp, true);
	}
	
	public boolean removeExpAndSp(long addToExp, long addToSp, boolean sendMessage)
	{
		int level = getLevel();
		if (!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		if (sendMessage)
		{
			// Send a Server->Client System Message to the L2PcInstance
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_XP_HAS_DECREASED_BY_S1);
			sm.addLong(addToExp);
			getActiveChar().sendPacket(sm);
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
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
	public final boolean addLevel(byte value)
	{
		if ((getLevel() + value) > (ExperienceData.getInstance().getMaxLevel() - 1))
		{
			return false;
		}
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLevelChanged(getActiveChar(), getLevel(), getLevel() + value), getActiveChar());
		
		boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
		{
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
			getActiveChar().sendPacket(SystemMessageId.YOUR_LEVEL_HAS_INCREASED);
			getActiveChar().notifyFriends(L2FriendStatus.MODE_LEVEL);
			
			L2ClassMasterInstance.showQuestionMark(getActiveChar());
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
		final L2Summon sPet = getActiveChar().getPet();
		if (sPet != null)
		{
			final L2PetInstance pet = (L2PetInstance) sPet;
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
		
		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);
		
		// Update the overloaded status of the L2PcInstance
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the L2PcInstance
		getActiveChar().refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the L2PcInstance
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		// Send acquirable skill list
		getActiveChar().sendPacket(new AcquireSkillList(getActiveChar()));
		getActiveChar().sendPacket(new ExVoteSystemInfo(getActiveChar()));
		if (getActiveChar().isInParty())
		{
			final PartySmallWindowUpdate partyWindow = new PartySmallWindowUpdate(getActiveChar(), false);
			partyWindow.addUpdateType(PartySmallWindowUpdateType.LEVEL);
			getActiveChar().getParty().broadcastToPartyMembers(getActiveChar(), partyWindow);
		}
		if ((getLevel() == ExperienceData.getInstance().getMaxLevel()) && getActiveChar().isNoble())
		{
			getActiveChar().sendPacket(new ExAcquireAPSkillList(getActiveChar()));
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
		UserInfo ui = new UserInfo(getActiveChar(), false);
		ui.addComponentType(UserInfoType.CURRENT_HPMPCP_EXP_SP);
		getActiveChar().sendPacket(ui);
		
		return true;
	}
	
	@Override
	public final long getExpForLevel(int level)
	{
		return ExperienceData.getInstance().getExpForLevel(level);
	}
	
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
	
	@Override
	public final long getExp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
		}
		
		return super.getExp();
	}
	
	public final long getBaseExp()
	{
		return super.getExp();
	}
	
	@Override
	public final void setExp(long value)
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
	
	@Override
	public final byte getLevel()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		}
		
		return super.getLevel();
	}
	
	public final byte getBaseLevel()
	{
		return super.getLevel();
	}
	
	@Override
	public final void setLevel(byte value)
	{
		if (value > (Config.SERVER_CLASSIC_SUPPORT ? Config.MAX_CLASSIC_PLAYER_LEVEL : ExperienceData.getInstance().getMaxLevel() - 1))
		{
			value = Config.SERVER_CLASSIC_SUPPORT ? Config.MAX_CLASSIC_PLAYER_LEVEL : (byte) (ExperienceData.getInstance().getMaxLevel() - 1);
		}
		
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		}
		else
		{
			super.setLevel(value);
		}
	}
	
	@Override
	public final int getMaxCp()
	{
		// Get the Max CP (base+modifier) of the L2PcInstance
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_CP, getActiveChar().getTemplate().getBaseCpMax(getActiveChar().getLevel()));
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
	public final int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the L2PcInstance
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_HP, getActiveChar().getTemplate().getBaseHpMax(getActiveChar().getLevel()));
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
	public final int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the L2PcInstance
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_MP, getActiveChar().getTemplate().getBaseMpMax(getActiveChar().getLevel()));
		
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
	public final long getSp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		}
		
		return super.getSp();
	}
	
	public final long getBaseSp()
	{
		return super.getSp();
	}
	
	@Override
	public final void setSp(long value)
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
		final L2PcInstance player = getActiveChar();
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
			final L2PetLevelData data = PetDataTable.getInstance().getPetLevelData(player.getMountNpcId(), player.getMountLevel());
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
		if ((val > Config.MAX_RUN_SPEED) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
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
		if ((val > Config.MAX_RUN_SPEED) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
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
	public int getPAtkSpd()
	{
		int val = super.getPAtkSpd();
		
		if ((val > Config.MAX_PATK_SPEED) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			return Config.MAX_PATK_SPEED;
		}
		
		return val;
	}
	
	/*
	 * Set current vitality points to this value if quiet = true - does not send system messages
	 */
	public void setVitalityPoints(int points, boolean quiet)
	{
		points = Math.min(Math.max(points, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
		if (points == getActiveChar().getVitalityPoints())
		{
			return;
		}
		
		if (points < getActiveChar().getVitalityPoints())
		{
			getActiveChar().sendPacket(SystemMessageId.YOUR_VITALITY_HAS_DECREASED);
		}
		else
		{
			getActiveChar().sendPacket(SystemMessageId.YOUR_VITALITY_HAS_INCREASED);
		}
		
		getActiveChar().setVitalityPoints(points);
		
		if (points == 0)
		{
			getActiveChar().sendPacket(SystemMessageId.YOUR_VITALITY_IS_FULLY_EXHAUSTED);
		}
		else if (points == MAX_VITALITY_POINTS)
		{
			getActiveChar().sendPacket(SystemMessageId.YOUR_VITALITY_IS_AT_MAXIMUM);
		}
		
		final L2PcInstance player = getActiveChar();
		player.sendPacket(new ExVitalityPointInfo(getActiveChar().getVitalityPoints()));
		if (player.isInParty())
		{
			final PartySmallWindowUpdate partyWindow = new PartySmallWindowUpdate(player, false);
			partyWindow.addUpdateType(PartySmallWindowUpdateType.VITALITY_POINTS);
			player.getParty().broadcastToPartyMembers(player, partyWindow);
		}
	}
	
	public synchronized void updateVitalityPoints(int points, boolean useRates, boolean quiet)
	{
		if ((points == 0) || !Config.ENABLE_VITALITY)
		{
			return;
		}
		
		if (useRates)
		{
			if (getActiveChar().isLucky())
			{
				return;
			}
			
			if (points < 0) // vitality consumed
			{
				int stat = (int) calcStat(Stats.VITALITY_CONSUME_RATE, 1, getActiveChar(), null);
				
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
			points = Math.min(getActiveChar().getVitalityPoints() + points, MAX_VITALITY_POINTS);
		}
		else
		{
			points = Math.max(getActiveChar().getVitalityPoints() + points, MIN_VITALITY_POINTS);
		}
		
		if (Math.abs(points - getActiveChar().getVitalityPoints()) <= 1e-6)
		{
			return;
		}
		
		getActiveChar().setVitalityPoints(points);
	}
	
	public double getVitalityMultiplier()
	{
		return Config.ENABLE_VITALITY ? Config.RATE_VITALITY_EXP_MULTIPLIER : 1;
	}
	
	public double getExpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double bonusExp = 1.0;
		
		// Bonus from Vitality System
		vitality = getVitalityMultiplier();
		
		// Bonus exp from skills
		bonusExp = 1 + (calcStat(Stats.BONUS_EXP, 0, null, null) / 100);
		
		if (vitality > 1.0)
		{
			bonus += (vitality - 1);
		}
		
		if (bonusExp > 1)
		{
			bonus += (bonusExp - 1);
		}
		
		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_EXP);
		
		return bonus;
	}
	
	public double getSpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double bonusSp = 1.0;
		
		// Bonus from Vitality System
		vitality = getVitalityMultiplier();
		
		// Bonus sp from skills
		bonusSp = 1 + (calcStat(Stats.BONUS_SP, 0, null, null) / 100);
		
		if (vitality > 1.0)
		{
			bonus += (vitality - 1);
		}
		
		if (bonusSp > 1)
		{
			bonus += (bonusSp - 1);
		}
		
		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_SP);
		
		return bonus;
	}
	
	/**
	 * Gets the maximum brooch jewel count.
	 * @return the maximum brooch jewel count
	 */
	public int getBroochJewelSlots()
	{
		return (int) calcStat(Stats.BROOCH_JEWELS, 0);
	}
}
