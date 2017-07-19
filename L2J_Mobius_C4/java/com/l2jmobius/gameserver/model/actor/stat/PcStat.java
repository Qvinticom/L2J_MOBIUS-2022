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
package com.l2jmobius.gameserver.model.actor.stat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2PetDataTable;
import com.l2jmobius.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.base.Experience;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SocialAction;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;
import com.l2jmobius.gameserver.skills.Stats;

public class PcStat extends PlayableStat
{
	private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
	
	// =========================================================
	// Data Field
	
	private int _OldMaxCp; // stats watch
	private int _OldMaxHp; // stats watch
	private int _OldMaxMp; // stats watch
	
	// =========================================================
	// Constructor
	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	// =========================================================
	// Method - Public
	@Override
	public boolean addExp(long value)
	{
		// Prevent only gaining experience
		if (!getActiveChar().getGainXpSp() && (value > 0))
		{
			return false;
		}
		
		if (!super.addExp(value))
		{
			return false;
		}
		
		// Set new karma
		if ((getActiveChar().getKarma() > 0) && (getActiveChar().isGM() || !getActiveChar().isInsideZone(L2Character.ZONE_PVP)))
		{
			final int karmaLost = getActiveChar().calculateKarmaLost(value);
			if (karmaLost > 0)
			{
				getActiveChar().setKarma(getActiveChar().getKarma() - karmaLost);
			}
		}
		
		final StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.EXP, (int) getExp());
		getActiveChar().sendPacket(su);
		
		return true;
	}
	
	/**
	 * Add Experience and SP rewards to the L2PcInstance, remove its Karma (if necessary) and Launch increase level task.<BR>
	 * <BR>
	 * <B><U> Actions </U> :</B><BR>
	 * <BR>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance</li>
	 * <li>Send a Server->Client System Message to the L2PcInstance</li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet SocialAction (broadcast)</li>
	 * <li>If the L2PcInstance increases it's level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...)</li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet UserInfo to the L2PcInstance</li><BR>
	 * <BR>
	 * @param addToExp The Experience value to add
	 * @param addToSp The SP value to add
	 */
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		float ratioTakenByPet = 0;
		
		// if this player has a pet that gains from the owner's Exp, give the pet Exp now
		if ((getActiveChar().getPet() != null) && (getActiveChar().getPet() instanceof L2PetInstance))
		{
			final L2PetInstance pet = (L2PetInstance) getActiveChar().getPet();
			ratioTakenByPet = pet.getPetData().getOwnerExpTaken();
			
			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if ((ratioTakenByPet > 0) && !pet.isDead())
			{
				pet.addExpAndSp((long) (addToExp * ratioTakenByPet), (int) (addToSp * ratioTakenByPet));
			}
			
			// now adjust the max ratio to avoid the owner earning negative exp/sp
			if (ratioTakenByPet > 1)
			{
				ratioTakenByPet = 1;
			}
			
			addToExp = (long) (addToExp * (1 - ratioTakenByPet));
			addToSp = (int) (addToSp * (1 - ratioTakenByPet));
		}
		
		if (!super.addExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		// Send a Server->Client System Message to the L2PcInstance
		final SystemMessage sm = new SystemMessage(SystemMessage.YOU_EARNED_S1_EXP_AND_S2_SP);
		sm.addNumber((int) addToExp);
		sm.addNumber(addToSp);
		getActiveChar().sendPacket(sm);
		
		return true;
	}
	
	@Override
	public boolean removeExpAndSp(long addToExp, int addToSp)
	{
		return removeExpAndSp(addToExp, addToSp, true);
	}
	
	public boolean removeExpAndSp(long addToExp, int addToSp, boolean sendMessage)
	{
		if (!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		if (sendMessage)
		{
			// Send a Server->Client System Message to the L2PcInstance
			SystemMessage sm = new SystemMessage(SystemMessage.EXP_DECREASED_BY_S1);
			sm.addNumber((int) addToExp);
			getActiveChar().sendPacket(sm);
			sm = new SystemMessage(SystemMessage.SP_DECREASED_S1);
			sm.addNumber(addToSp);
			getActiveChar().sendPacket(sm);
		}
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if ((getLevel() + value) > (Experience.MAX_LEVEL - 1))
		{
			return false;
		}
		
		final boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
		{
			L2ClassMasterInstance.showQuestionMark(getActiveChar());
			
			applyNewbieStatus();
			
			final QuestState qs = getActiveChar().getQuestState("255_Tutorial");
			if (qs != null)
			{
				qs.getQuest().notifyEvent("CE40", null, getActiveChar());
			}
			
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));
			getActiveChar().sendPacket(new SystemMessage(SystemMessage.YOU_INCREASED_YOUR_LEVEL));
		}
		
		getActiveChar().rewardSkills(); // Give Expertise skill of this level
		
		if (getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().addClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()), getActiveChar());
		}
		
		if (getActiveChar().isInParty())
		{
			getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level
		}
		
		final StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
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
		
		return levelIncreased;
	}
	
	@Override
	public boolean addSp(int value)
	{
		if (!getActiveChar().getGainXpSp() && (value > 0))
		{
			return false;
		}
		
		if (!super.addSp(value))
		{
			return false;
		}
		
		final StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActiveChar().sendPacket(su);
		
		return true;
	}
	
	@Override
	public final long getExpForLevel(int level)
	{
		return Experience.LEVEL[level];
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
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
	
	@Override
	public final byte getLevel()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		}
		
		return super.getLevel();
	}
	
	@Override
	public final void setLevel(byte value)
	{
		if (value > (Experience.MAX_LEVEL - 1))
		{
			value = Experience.MAX_LEVEL - 1;
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
		final int val = super.getMaxCp();
		if (val != _OldMaxCp)
		{
			_OldMaxCp = val;
			
			// Launch a regen task if the new Max HP is higher than the old one
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
		final int val = super.getMaxHp();
		if (val != _OldMaxHp)
		{
			_OldMaxHp = val;
			
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
		final int val = super.getMaxMp();
		
		if (val != _OldMaxMp)
		{
			_OldMaxMp = val;
			
			// Launch a regen task if the new Max MP is higher than the old one
			if (getActiveChar().getStatus().getCurrentMp() != val)
			{
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}
		
		return val;
	}
	
	@Override
	public final int getSp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		}
		
		return super.getSp();
	}
	
	@Override
	public final void setSp(int value)
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
	
	@Override
	public int getRunSpeed()
	{
		if (getActiveChar() == null)
		{
			return 1;
		}
		
		int val = super.getRunSpeed();
		
		final L2PcInstance player = getActiveChar();
		if (player.isMounted())
		{
			final int baseRunSpd = L2PetDataTable.getInstance().getPetData(player.getMountNpcId(), player.getMountLevel()).getPetSpeed();
			val = (int) Math.round(calcStat(Stats.RUN_SPEED, baseRunSpd, null, null));
		}
		
		return val;
	}
	
	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar() == null)
		{
			return 1;
		}
		
		if (getActiveChar().isMounted())
		{
			return (getRunSpeed() * 1f) / L2PetDataTable.getInstance().getPetData(getActiveChar().getMountNpcId(), getActiveChar().getMountLevel()).getPetSpeed();
		}
		
		return super.getMovementSpeedMultiplier();
	}
	
	@Override
	public int getWalkSpeed()
	{
		if (getActiveChar() == null)
		{
			return 1;
		}
		
		return (getRunSpeed() * 70) / 100;
	}
	
	/**
	 * If there are no characters on the server, the bonuses will be applied to the first character that becomes level 6 and end if this character reaches level 25 or above. If the first character that becomes level 6 is deleted, the rest of the characters may not receive the new character bonus If
	 * the first character to become level 6 loses a level, and the player makes another character level 6, the bonus will be applied to only the first character to achieve level 6. If the character loses a level after reaching level 25, the character may not receive the bonus.
	 */
	private void applyNewbieStatus()
	{
		if ((getActiveChar().getNewbieState() == L2PcInstance.NONE) && (getActiveChar().getLevel() >= Experience.MIN_NEWBIE_LEVEL) && (getActiveChar().getLevel() < Experience.MAX_NEWBIE_LEVEL))
		{
			if (!Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("SELECT value FROM account_data WHERE (account_name=?) AND (var='newbie_char')"))
				{
					statement.setString(1, getActiveChar().getAccountName());
					try (ResultSet rset = statement.executeQuery())
					{
						if (!rset.next())
						{
							try (PreparedStatement statement1 = con.prepareStatement("INSERT INTO account_data (account_name, var, value) VALUES (?, 'newbie_char', ?)"))
							{
								statement1.setString(1, getActiveChar().getAccountName());
								statement1.setInt(2, getActiveChar().getObjectId());
								statement1.executeUpdate();
							}
							
							getActiveChar().setNewbieState(L2PcInstance.NEW);
							
							if (Config.DEBUG)
							{
								_log.info("New newbie character: " + getActiveChar().getCharId());
							}
						}
						else
						{
							getActiveChar().setNewbieState(L2PcInstance.OLD);
						}
					}
				}
				catch (final SQLException e)
				{
					_log.warning("Could not check character for newbie: " + e);
				}
			}
			else
			{
				getActiveChar().setNewbieState(L2PcInstance.NEW);
			}
		}
		else if (getActiveChar().getLevel() >= Experience.MAX_NEWBIE_LEVEL)
		{
			if (getActiveChar().getNewbieState() != L2PcInstance.NONE)
			{
				getActiveChar().setNewbieState(L2PcInstance.NONE);
			}
		}
	}
	
}