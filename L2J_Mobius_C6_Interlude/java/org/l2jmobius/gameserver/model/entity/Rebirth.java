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
package org.l2jmobius.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.datatables.SkillTable;
import org.l2jmobius.gameserver.datatables.xml.ExperienceData;
import org.l2jmobius.gameserver.datatables.xml.ItemTable;
import org.l2jmobius.gameserver.idfactory.BitSetIDFactory;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

/**
 * <strong>This 'Custom Engine' was developed for L2J Forum Member 'sauron3256' on November 1st, 2008.</strong><br>
 * <br>
 * <strong>Quick Summary:</strong><br>
 * This engine will grant the player special bonus skills at the cost of reseting him to level 1.<br>
 * The -USER- can set up to X Rebirths, the skills received and their respective levels, and the item and price of each rebirth.<br>
 * PLAYER's information is stored in an SQL Db under the table name: REBIRTH_MANAGER.<br>
 * @author <strong>Beetle and Shyla</strong>
 */
public class Rebirth
{
	private static Logger LOGGER = Logger.getLogger(BitSetIDFactory.class.getName());
	
	private final HashMap<Integer, Integer> _playersRebirthInfo = new HashMap<>();
	
	private Rebirth()
	{
		// Do Nothing ^_-
	}
	
	/**
	 * This is what it called from the Bypass Handler. (I think that's all thats needed here).
	 * @param player the player
	 * @param command the command
	 */
	public void handleCommand(PlayerInstance player, String command)
	{
		if (command.startsWith("custom_rebirth_requestrebirth"))
		{
			displayRebirthWindow(player);
		}
		else if (command.startsWith("custom_rebirth_confirmrequest"))
		{
			requestRebirth(player);
		}
	}
	
	/**
	 * Display's an HTML window with the Rebirth Options.
	 * @param player the player
	 */
	public void displayRebirthWindow(PlayerInstance player)
	{
		try
		{
			final int currBirth = getRebirthLevel(player); // Returns the player's current birth level
			
			// Don't send html if player is already at max rebirth count.
			if (currBirth >= Config.REBIRTH_MAX)
			{
				player.sendMessage("You are currently at your maximum rebirth count!");
				return;
			}
			
			// Returns true if BASE CLASS is a mage.
			final boolean isMage = player.getBaseTemplate().classId.isMage();
			// Returns the skill based on next Birth and if isMage.
			Skill skill = getRebirthSkill((currBirth + 1), isMage);
			
			String icon = "" + skill.getId();// Returns the skill's id.
			
			// Incase the skill is only 3 digits.
			if (icon.length() < 4)
			{
				icon = "0" + icon;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks to see if the player is eligible for a Rebirth, if so it grants it and stores information.
	 * @param player the player
	 */
	public void requestRebirth(PlayerInstance player)
	{
		// Check to see if Rebirth is enabled to avoid hacks
		if (!Config.REBIRTH_ENABLE)
		{
			LOGGER.warning("[WARNING] Player " + player.getName() + " is trying to use rebirth system when it's disabled.");
			return;
		}
		
		// Check the player's level.
		if (player.getLevel() < Config.REBIRTH_MIN_LEVEL)
		{
			player.sendMessage("You do not meet the level requirement for a Rebirth!");
			return;
		}
		
		else if (player.isSubClassActive())
		{
			player.sendMessage("Please switch to your Main Class before attempting a Rebirth.");
			return;
		}
		
		final int currBirth = getRebirthLevel(player);
		int itemNeeded = 0;
		int itemAmount = 0;
		
		if (currBirth >= Config.REBIRTH_MAX)
		{
			player.sendMessage("You are currently at your maximum rebirth count!");
			return;
		}
		
		// Get the requirements
		int loopBirth = 0;
		for (String readItems : Config.REBIRTH_ITEM_PRICE)
		{
			final String[] currItem = readItems.split(",");
			if (loopBirth == currBirth)
			{
				itemNeeded = Integer.parseInt(currItem[0]);
				itemAmount = Integer.parseInt(currItem[1]);
				break;
			}
			loopBirth++;
		}
		
		// Their is an item required
		if (itemNeeded != 0)
		{
			// Checks to see if player has required items, and takes them if so.
			if (!playerIsEligible(player, itemNeeded, itemAmount))
			{
				return;
			}
		}
		
		// Check and see if its the player's first Rebirth calling.
		final boolean firstBirth = currBirth == 0;
		// Player meets requirements and starts Rebirth Process.
		grantRebirth(player, (currBirth + 1), firstBirth);
	}
	
	/**
	 * Physically rewards player and resets status to nothing.
	 * @param player the player
	 * @param newBirthCount the new birth count
	 * @param firstBirth the first birth
	 */
	public void grantRebirth(PlayerInstance player, int newBirthCount, boolean firstBirth)
	{
		try
		{
			final double actual_hp = player.getCurrentHp();
			final double actual_cp = player.getCurrentCp();
			
			int max_level = ExperienceData.getInstance().getMaxLevel();
			
			if (player.isSubClassActive())
			{
				max_level = Config.MAX_SUBCLASS_LEVEL;
			}
			
			// Protections
			Integer returnToLevel = Config.REBIRTH_RETURN_TO_LEVEL;
			if (returnToLevel < 1)
			{
				returnToLevel = 1;
			}
			if (returnToLevel > max_level)
			{
				returnToLevel = max_level;
			}
			
			// Resets character to first class.
			player.setClassId(player.getBaseClass());
			
			player.broadcastUserInfo();
			
			final byte lvl = Byte.parseByte(returnToLevel + "");
			
			final long pXp = player.getStat().getExp();
			final long tXp = ExperienceData.getInstance().getExpForLevel(lvl);
			
			if (pXp > tXp)
			{
				player.getStat().removeExpAndSp(pXp - tXp, 0);
			}
			else if (pXp < tXp)
			{
				player.getStat().addExpAndSp(tXp - pXp, 0);
			}
			
			// Remove the player's current skills.
			for (Skill skill : player.getAllSkills())
			{
				player.removeSkill(skill);
			}
			// Give players their eligible skills.
			player.giveAvailableSkills();
			
			// restore Hp-Mp-Cp
			player.setCurrentCp(actual_cp);
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentHp(actual_hp);
			player.broadcastStatusUpdate();
			
			// Updates the player's information in the Character Database.
			player.store();
			
			if (firstBirth)
			{
				storePlayerBirth(player);
			}
			else
			{
				updatePlayerBirth(player, newBirthCount);
			}
			
			// Give the player his new Skills.
			grantRebirthSkills(player);
			
			// Displays a congratulation message to the player.
			displayCongrats(player);
			
			// Update skill list
			player.sendSkillList();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Special effects when the player levels.
	 * @param player the player
	 */
	public void displayCongrats(PlayerInstance player)
	{
		// Victory Social Action.
		player.setTarget(player);
		player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
		player.sendMessage("Congratulations " + player.getName() + ". You have been REBORN!");
	}
	
	/**
	 * Check and verify the player DOES have the item required for a request. Also, remove the item if he has.
	 * @param player the player
	 * @param itemId the item id
	 * @param itemAmount the item amount
	 * @return true, if successful
	 */
	public boolean playerIsEligible(PlayerInstance player, int itemId, int itemAmount)
	{
		String itemName = ItemTable.getInstance().getTemplate(itemId).getName();
		ItemInstance itemNeeded = player.getInventory().getItemByItemId(itemId);
		
		if ((itemNeeded == null) || (itemNeeded.getCount() < itemAmount))
		{
			player.sendMessage("You need atleast " + itemAmount + "  [ " + itemName + " ] to request a Rebirth!");
			return false;
		}
		
		// Player has the required items, so we're going to take them!
		player.getInventory().destroyItemByItemId("Rebirth Engine", itemId, itemAmount, player, null);
		player.sendMessage("Removed " + itemAmount + " " + itemName + " from your inventory!");
		
		return true;
	}
	
	/**
	 * Gives the available Bonus Skills to the player.
	 * @param player the player
	 */
	public void grantRebirthSkills(PlayerInstance player)
	{
		// returns the current Rebirth Level
		final int rebirthLevel = getRebirthLevel(player);
		// Returns true if BASE CLASS is a mage.
		final boolean isMage = player.getBaseTemplate().classId.isMage();
		
		// Simply return since no bonus skills are granted.
		if (rebirthLevel == 0)
		{
			return;
		}
		
		// Load the bonus skills unto the player.
		CreatureSay rebirthText = null;
		for (int i = 0; i < rebirthLevel; i++)
		{
			final Skill bonusSkill = getRebirthSkill((i + 1), isMage);
			player.addSkill(bonusSkill, false);
			
			// If you'd rather make it simple, simply comment this out and replace with a simple player.sendmessage();
			rebirthText = new CreatureSay(0, 18, "Rebirth Manager ", " Granted you [ " + bonusSkill.getName() + " ] level [ " + bonusSkill.getLevel() + " ]!");
			player.sendPacket(rebirthText);
		}
	}
	
	/**
	 * Return the player's current Rebirth Level.
	 * @param player the player
	 * @return the rebirth level
	 */
	public int getRebirthLevel(PlayerInstance player)
	{
		final int playerId = player.getObjectId();
		
		if (_playersRebirthInfo.get(playerId) == null)
		{
			loadRebirthInfo(player);
		}
		
		return _playersRebirthInfo.get(playerId);
	}
	
	/**
	 * Return the Skill the player is going to be rewarded.
	 * @param rebirthLevel the rebirth level
	 * @param mage the mage
	 * @return the rebirth skill
	 */
	public Skill getRebirthSkill(int rebirthLevel, boolean mage)
	{
		Skill skill = null;
		
		// Player is a Mage.
		if (mage)
		{
			int loopBirth = 0;
			for (String readSkill : Config.REBIRTH_MAGE_SKILL)
			{
				final String[] currSkill = readSkill.split(",");
				if (loopBirth == (rebirthLevel - 1))
				{
					skill = SkillTable.getInstance().getInfo(Integer.parseInt(currSkill[0]), Integer.parseInt(currSkill[1]));
					break;
				}
				loopBirth++;
			}
		}
		else // Player is a Fighter.
		{
			int loopBirth = 0;
			for (String readSkill : Config.REBIRTH_FIGHTER_SKILL)
			{
				final String[] currSkill = readSkill.split(",");
				if (loopBirth == (rebirthLevel - 1))
				{
					skill = SkillTable.getInstance().getInfo(Integer.parseInt(currSkill[0]), Integer.parseInt(currSkill[1]));
					break;
				}
				loopBirth++;
			}
		}
		return skill;
	}
	
	/**
	 * Database caller to retrieve player's current Rebirth Level.
	 * @param player the player
	 */
	public void loadRebirthInfo(PlayerInstance player)
	{
		final int playerId = player.getObjectId();
		int rebirthCount = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			ResultSet rset;
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `rebirth_manager` WHERE playerId = ?");
			statement.setInt(1, playerId);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				rebirthCount = rset.getInt("rebirthCount");
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		_playersRebirthInfo.put(playerId, rebirthCount);
	}
	
	/**
	 * Stores the player's information in the DB.
	 * @param player the player
	 */
	public void storePlayerBirth(PlayerInstance player)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO `rebirth_manager` (playerId,rebirthCount) VALUES (?,1)");
			statement.setInt(1, player.getObjectId());
			statement.execute();
			
			_playersRebirthInfo.put(player.getObjectId(), 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates the player's information in the DB.
	 * @param player the player
	 * @param newRebirthCount the new rebirth count
	 */
	public void updatePlayerBirth(PlayerInstance player, int newRebirthCount)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final int playerId = player.getObjectId();
			
			PreparedStatement statement = con.prepareStatement("UPDATE `rebirth_manager` SET rebirthCount = ? WHERE playerId = ?");
			statement.setInt(1, newRebirthCount);
			statement.setInt(2, playerId);
			statement.execute();
			
			_playersRebirthInfo.put(playerId, newRebirthCount);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static Rebirth getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Rebirth INSTANCE = new Rebirth();
	}
}