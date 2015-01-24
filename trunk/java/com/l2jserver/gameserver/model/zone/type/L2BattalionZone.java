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
package com.l2jserver.gameserver.model.zone.type;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.itemcontainer.PcInventory;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.zone.L2ZoneRespawn;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.Rnd;

/**
 * Another type of damage zone with skills
 * @author kerberos
 */
public class L2BattalionZone extends L2ZoneRespawn
{
	private final static Skill NOBLESS_SKILL = SkillData.getInstance().getSkill(1323, 1);
	private final static String[] GRADE_NAMES =
	{
		"",
		"D",
		"C",
		"B",
		"A",
		"S",
		"S80",
		"S84",
		"R",
		"R95",
		"R99"
	};
	
	public L2BattalionZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		character.setInsideZone(ZoneId.BATTALION, true);
		character.setInsideZone(ZoneId.PVP, true);
		if (!Config.BTZ_STORE_ZONE)
		{
			character.setInsideZone(ZoneId.NO_STORE, true);
		}
		
		if (character.isPlayer())
		{
			final L2PcInstance activeChar = character.getActingPlayer();
			if ((Config.BTZ_CLASSES != null) && Config.BTZ_CLASSES.contains("" + activeChar.getClassId().getId()))
			{
				activeChar.teleToLocation(83597, 147888, -3405);
				activeChar.sendMessage("Your class is not allowed in the Battalion zone.");
				return;
			}
			
			for (L2ItemInstance o : activeChar.getInventory().getItems())
			{
				if (o.isEquipable() && o.isEquipped() && !checkItem(o))
				{
					int slot = activeChar.getInventory().getSlotFromItem(o);
					activeChar.getInventory().unEquipItemInBodySlot(slot);
					activeChar.sendMessage(o.getName() + " unequiped because is not allowed inside this zone.");
				}
			}
			activeChar.sendMessage("You entered into the Battalion zone.");
			clear(activeChar);
			if (Config.BTZ_GIVE_NOBLES)
			{
				NOBLESS_SKILL.applyEffects(activeChar, activeChar);
			}
			if (Config.BTZ_PVP_ENABLED)
			{
				activeChar.updatePvPFlag(1);
			}
			
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		character.setInsideZone(ZoneId.BATTALION, false);
		character.setInsideZone(ZoneId.PVP, false);
		if (!Config.BTZ_STORE_ZONE)
		{
			character.setInsideZone(ZoneId.NO_STORE, false);
		}
		
		if (character.isPlayer())
		{
			final L2PcInstance activeChar = character.getActingPlayer();
			activeChar.sendMessage("You left from a Battalion zone.");
			
			if (Config.BTZ_PVP_ENABLED)
			{
				activeChar.stopPvPFlag();
			}
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
		if (character.isPlayer())
		{
			final L2PcInstance activeChar = character.getActingPlayer();
			if (Config.BTZ_REVIVE)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{
					activeChar.doRevive();
					heal(activeChar);
					int[] loc = Config.BTZ_SPAWN_LOCATIONS[Rnd.get(Config.BTZ_SPAWN_LOCATIONS.length)];
					activeChar.teleToLocation(loc[0] + Rnd.get(-Config.BTZ_RADIUS, Config.BTZ_RADIUS), loc[1] + Rnd.get(-Config.BTZ_RADIUS, Config.BTZ_RADIUS), loc[2]);
				}, Config.BTZ_REVIVE_DELAY * 1000);
				
				ExShowScreenMessage revive = new ExShowScreenMessage("You will be respawned in " + Config.BTZ_REVIVE_DELAY + " seconds.", 5000, true, 2); // 5 Seconds display
				activeChar.sendPacket(revive);
			}
		}
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		if (character.isPlayer())
		{
			final L2PcInstance activeChar = character.getActingPlayer();
			if (Config.BTZ_REVIVE_NOBLESS)
			{
				NOBLESS_SKILL.applyEffects(activeChar, activeChar);
			}
			if (Config.BTZ_REVIVE_HEAL)
			{
				heal(activeChar);
			}
		}
	}
	
	private void clear(L2PcInstance player)
	{
		if (Config.BTZ_REMOVE_BUFFS)
		{
			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			if (Config.BTZ_REMOVE_PETS)
			{
				L2Summon pet = player.getPet();
				if (pet != null)
				{
					pet.stopAllEffectsExceptThoseThatLastThroughDeath();
					pet.unSummon(player);
				}
			}
		}
		else
		{
			if (Config.BTZ_REMOVE_PETS)
			{
				L2Summon pet = player.getPet();
				if (pet != null)
				{
					pet.unSummon(player);
				}
			}
		}
	}
	
	private static void heal(L2PcInstance activeChar)
	{
		activeChar.setCurrentHp(activeChar.getMaxHp());
		activeChar.setCurrentCp(activeChar.getMaxCp());
		activeChar.setCurrentMp(activeChar.getMaxMp());
	}
	
	public static void givereward(L2PcInstance player)
	{
		if (player.isInsideZone(ZoneId.BATTALION))
		{
			SystemMessage systemMessage = null;
			
			for (int[] reward : Config.BTZ_REWARDS)
			{
				final PcInventory inv = player.getInventory();
				
				if (ItemTable.getInstance().getTemplate(reward[0]).isStackable())
				{
					inv.addItem("L2MultiFunctionZone ", reward[0], reward[1], player, player);
					
					if (reward[1] > 1)
					{
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
						systemMessage.addItemName(reward[0]);
						systemMessage.addLong(reward[1]);
					}
					else
					{
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
						systemMessage.addItemName(reward[0]);
					}
					player.sendPacket(systemMessage);
				}
				else
				{
					for (int i = 0; i < reward[1]; ++i)
					{
						inv.addItem("L2MultiFunctionZone ", reward[0], 1, player, player);
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
						systemMessage.addItemName(reward[0]);
						player.sendPacket(systemMessage);
					}
				}
			}
		}
	}
	
	public static boolean checkItem(L2ItemInstance item)
	{
		final int o = item.getItem().getCrystalType().ordinal();
		final int e = item.getEnchantLevel();
		
		if ((Config.BTZ_ENCHANT != 0) && (e >= Config.BTZ_ENCHANT))
		{
			return false;
		}
		
		if (Config.BTZ_GRADES.contains(GRADE_NAMES[o]))
		{
			return false;
		}
		
		if ((Config.BTZ_ITEMS != null) && Config.BTZ_ITEMS.contains("" + item.getId()))
		{
			return false;
		}
		return true;
	}
}