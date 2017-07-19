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
package com.l2jmobius.gameserver.handler.admincommandhandlers;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.SevenSigns;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.AutoSpawnHandler;
import com.l2jmobius.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Admin Command Handler for Mammon NPCs
 * @author Tempy
 */
public class AdminMammon implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_mammon_find",
		"admin_mammon_respawn",
		"admin_list_spawns",
		"admin_msg"
	};
	private static final int REQUIRED_LEVEL = Config.GM_MENU;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			{
				return false;
			}
		}
		
		final boolean _isSealValidation = SevenSigns.getInstance().isSealValidationPeriod();
		
		int npcId = 0;
		int teleportIndex = -1;
		final AutoSpawnInstance blackSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false);
		final AutoSpawnInstance merchSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false);
		
		if (command.startsWith("admin_mammon_find"))
		{
			try
			{
				if (command.length() > 17)
				{
					teleportIndex = Integer.parseInt(command.substring(18));
				}
			}
			catch (final Exception NumberFormatException)
			{
				activeChar.sendMessage("Command format is //mammon_find <teleportIndex> (where 1 = Blacksmith, 2 = Merchant)");
			}
			
			if (!_isSealValidation)
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}
			
			final L2NpcInstance[] blackInst = blackSpawnInst.getNPCInstanceList();
			final L2NpcInstance[] merchInst = merchSpawnInst.getNPCInstanceList();
			
			if (blackInst.length > 0)
			{
				activeChar.sendMessage("Blacksmith of Mammon: " + blackInst[0].getX() + " " + blackInst[0].getY() + " " + blackInst[0].getZ());
				
				if (teleportIndex == 0)
				{
					activeChar.teleToLocation(blackInst[0].getX(), blackInst[0].getY(), blackInst[0].getZ(), true);
				}
			}
			
			if (merchInst.length > 0)
			{
				activeChar.sendMessage("Merchant of Mammon: " + merchInst[0].getX() + " " + merchInst[0].getY() + " " + merchInst[0].getZ());
				
				if (teleportIndex == 1)
				{
					activeChar.teleToLocation(merchInst[0].getX(), merchInst[0].getY(), merchInst[0].getZ(), true);
				}
			}
		}
		
		else if (command.startsWith("admin_mammon_respawn"))
		{
			if (!_isSealValidation)
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}
			
			final long blackRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(blackSpawnInst);
			final long merchRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(merchSpawnInst);
			
			activeChar.sendMessage("The Merchant of Mammon will respawn in " + (merchRespawn / 60000) + " minute(s).");
			activeChar.sendMessage("The Blacksmith of Mammon will respawn in " + (blackRespawn / 60000) + " minute(s).");
		}
		
		else if (command.startsWith("admin_list_spawns"))
		{
			try
			{ // admin_list_spawns x[xxxx] x[xx]
				final String[] params = command.split(" ");
				
				npcId = Integer.parseInt(params[1]);
				
				if (params.length > 2)
				{
					teleportIndex = Integer.parseInt(params[2]);
				}
			}
			catch (final Exception e)
			{
				activeChar.sendPacket(SystemMessage.sendString("Command format is //list_spawns <NPC_ID> <TELE_INDEX>"));
			}
			
			SpawnTable.getInstance().findNPCInstances(activeChar, npcId, teleportIndex);
		}
		
		// Used for testing SystemMessage IDs - Use //msg <ID>
		else if (command.startsWith("admin_msg"))
		{
			int msgId = -1;
			
			try
			{
				msgId = Integer.parseInt(command.substring(10).trim());
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Command format: //msg <SYSTEM_MSG_ID>");
				return true;
			}
			
			activeChar.sendPacket(new SystemMessage(msgId));
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
}