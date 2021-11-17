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

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.model.SubClass;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.ClassMaster;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class PlayerStat extends PlayableStat
{
	private static final Logger LOGGER = Logger.getLogger(PlayerStat.class.getName());
	
	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch
	
	public PlayerStat(Player player)
	{
		super(player);
	}
	
	@Override
	public boolean addExp(long value)
	{
		final Player player = getActiveChar();
		
		// Disable exp gain.
		if (!player.getAccessLevel().canGainExp() || (Config.ENABLE_EXP_GAIN_COMMANDS && !getActiveChar().isExpGainEnabled()))
		{
			return false;
		}
		
		if (!super.addExp(value))
		{
			return false;
		}
		
		// Set new karma.
		if ((player.getKarma() > 0) && (player.isGM() || !player.isInsideZone(ZoneId.PVP)))
		{
			final int karmaLost = player.calculateKarmaLost(value);
			if (karmaLost > 0)
			{
				player.setKarma(player.getKarma() - karmaLost);
			}
		}
		
		player.sendPacket(new UserInfo(player));
		return true;
	}
	
	/**
	 * Add Experience and SP rewards to the Player, remove its Karma (if necessary) and Launch increase level task.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove Karma when the player kills Monster</li>
	 * <li>Send a Server->Client packet StatusUpdate to the Player</li>
	 * <li>Send a Server->Client System Message to the Player</li>
	 * <li>If the Player increases it's level, send a Server->Client packet SocialAction (broadcast)</li>
	 * <li>If the Player increases it's level, manage the increase level task (Max MP, Max MP, Recommendation, Expertise and beginner skills...)</li>
	 * <li>If the Player increases it's level, send a Server->Client packet UserInfo to the Player</li><br>
	 * @param addToExpValue The Experience value to add
	 * @param addToSpValue The SP value to add
	 */
	@Override
	public boolean addExpAndSp(long addToExpValue, int addToSpValue)
	{
		float ratioTakenByPet = 0;
		
		// Disable exp gain.
		final Player player = getActiveChar();
		if (!player.getAccessLevel().canGainExp() || (Config.ENABLE_EXP_GAIN_COMMANDS && !getActiveChar().isExpGainEnabled()))
		{
			return false;
		}
		
		long addToExp = addToExpValue;
		int addToSp = addToSpValue;
		
		// if this player has a pet that takes from the owner's Exp, give the pet Exp now
		if (player.getPet() instanceof Pet)
		{
			final Pet pet = (Pet) player.getPet();
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
		
		// Send a Server->Client System Message to the Player
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP);
		sm.addNumber((int) addToExp);
		sm.addNumber(addToSp);
		getActiveChar().sendPacket(sm);
		
		return true;
	}
	
	@Override
	public boolean removeExpAndSp(long addToExp, int addToSp)
	{
		if (!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		// Send a Server->Client System Message to the Player
		SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_EXPERIENCE_HAS_DECREASED_BY_S1);
		sm.addNumber((int) addToExp);
		getActiveChar().sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
		sm.addNumber(addToSp);
		getActiveChar().sendPacket(sm);
		
		return true;
	}
	
	@Override
	public boolean addLevel(byte value)
	{
		if ((getLevel() + value) > (ExperienceData.getInstance().getMaxLevel() - 1))
		{
			return false;
		}
		
		final boolean levelIncreased = super.addLevel(value);
		if (Config.ALLOW_CLASS_MASTERS && Config.ALLOW_REMOTE_CLASS_MASTERS)
		{
			final ClassMaster masterInstance = ClassMaster.getInstance();
			if (masterInstance != null)
			{
				final int curLevel = getActiveChar().getClassId().level();
				if ((getLevel() >= 20) && (curLevel == 0) && Config.ALLOW_CLASS_MASTERS_FIRST_CLASS)
				{
					ClassMaster.getInstance().onAction(getActiveChar());
				}
				else if ((getLevel() >= 40) && (curLevel == 1) && Config.ALLOW_CLASS_MASTERS_SECOND_CLASS)
				{
					ClassMaster.getInstance().onAction(getActiveChar());
				}
				else if ((getLevel() >= 76) && (curLevel == 2) && Config.ALLOW_CLASS_MASTERS_THIRD_CLASS)
				{
					ClassMaster.getInstance().onAction(getActiveChar());
				}
			}
			else
			{
				LOGGER.info("Attention: Remote ClassMaster is Enabled, but not inserted into DataBase. Remember to install 31288 Custom_Npc...");
			}
		}
		
		if (levelIncreased)
		{
			if ((getActiveChar().getLevel() >= Config.MAX_LEVEL_NEWBIE_STATUS) && getActiveChar().isNewbie())
			{
				getActiveChar().setNewbie(false);
			}
			
			final QuestState qs = getActiveChar().getQuestState("Tutorial");
			if ((qs != null) && (qs.getQuest() != null))
			{
				qs.getQuest().notifyEvent("CE40", null, getActiveChar());
			}
			
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));
			getActiveChar().sendPacket(SystemMessageId.YOUR_LEVEL_HAS_INCREASED);
		}
		
		getActiveChar().rewardSkills(); // Give Expertise skill of this level
		if (getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
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
		
		// Update the overloaded status of the Player
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the Player
		getActiveChar().refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the Player
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		// getActiveChar().setLocked(false);
		return levelIncreased;
	}
	
	@Override
	public boolean addSp(int value)
	{
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
		final Player player = getActiveChar();
		if ((player != null) && player.isSubClassActive())
		{
			final SubClass playerSubclass = player.getSubClasses().get(player.getClassIndex());
			if (playerSubclass != null)
			{
				return playerSubclass.getExp();
			}
		}
		return super.getExp();
	}
	
	@Override
	public void setExp(long value)
	{
		final Player player = getActiveChar();
		if (player.isSubClassActive())
		{
			final SubClass playerSubclass = player.getSubClasses().get(player.getClassIndex());
			if (playerSubclass != null)
			{
				playerSubclass.setExp(value);
			}
		}
		else
		{
			super.setExp(value);
		}
	}
	
	@Override
	public int getLevel()
	{
		try
		{
			final Player player = getActiveChar();
			if (player.isSubClassActive())
			{
				final SubClass playerSubclass = player.getSubClasses().get(player.getClassIndex());
				if (playerSubclass != null)
				{
					return playerSubclass.getLevel();
				}
			}
			return super.getLevel();
		}
		catch (NullPointerException e)
		{
			return -1;
		}
	}
	
	@Override
	public void setLevel(int value)
	{
		int level = value;
		if (level > (ExperienceData.getInstance().getMaxLevel() - 1))
		{
			level = ExperienceData.getInstance().getMaxLevel() - 1;
		}
		
		final Player player = getActiveChar();
		if (player.isSubClassActive())
		{
			final SubClass playerSubclass = player.getSubClasses().get(player.getClassIndex());
			if (playerSubclass != null)
			{
				playerSubclass.setLevel(level);
			}
		}
		else
		{
			super.setLevel(level);
		}
	}
	
	@Override
	public int getMaxCp()
	{
		final int val = super.getMaxCp();
		if (val != _oldMaxCp)
		{
			_oldMaxCp = val;
			
			final Player player = getActiveChar();
			if (player.getStatus().getCurrentCp() != val)
			{
				player.getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp());
			}
		}
		return val;
	}
	
	@Override
	public int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the Player
		final int val = super.getMaxHp();
		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;
			
			final Player player = getActiveChar();
			
			// Launch a regen task if the new Max HP is higher than the old one
			if (player.getStatus().getCurrentHp() != val)
			{
				player.getStatus().setCurrentHp(player.getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}
		return val;
	}
	
	@Override
	public int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the Player
		final int val = super.getMaxMp();
		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;
			
			final Player player = getActiveChar();
			
			// Launch a regen task if the new Max MP is higher than the old one
			if (player.getStatus().getCurrentMp() != val)
			{
				player.getStatus().setCurrentMp(player.getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}
		return val;
	}
	
	@Override
	public int getSp()
	{
		final Player player = getActiveChar();
		if (player.isSubClassActive())
		{
			final SubClass playerSubclass = player.getSubClasses().get(player.getClassIndex());
			if (playerSubclass != null)
			{
				return playerSubclass.getSp();
			}
		}
		return super.getSp();
	}
	
	@Override
	public void setSp(int value)
	{
		final Player player = getActiveChar();
		if (player.isSubClassActive())
		{
			final SubClass playerSubclass = player.getSubClasses().get(player.getClassIndex());
			if (playerSubclass != null)
			{
				playerSubclass.setSp(value);
			}
		}
		else
		{
			super.setSp(value);
		}
	}
}
