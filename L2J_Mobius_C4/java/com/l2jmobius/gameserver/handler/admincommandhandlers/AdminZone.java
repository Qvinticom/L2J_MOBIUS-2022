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

import java.util.StringTokenizer;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

public class AdminZone implements IAdminCommandHandler
{
	private static final int REQUIRED_LEVEL = Config.GM_TEST;
	public static final String[] ADMIN_ZONE_COMMANDS =
	{
		"admin_zone_check"
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, com.l2jmobius.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (activeChar.getAccessLevel() < REQUIRED_LEVEL)
			{
				return false;
			}
			
		}
		
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		
		if (actualCommand.equalsIgnoreCase("admin_zone_check"))
		{
			
			if (activeChar.isInsideZone(L2Character.ZONE_PVP))
			{
				activeChar.sendMessage("This is a PvP zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_NOLANDING))
			{
				activeChar.sendMessage("This is a non-landing zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_PEACE))
			{
				activeChar.sendMessage("This is a Peace zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_SIEGE))
			{
				activeChar.sendMessage("This is a Siege zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				activeChar.sendMessage("This is a Mother Tree zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_CLANHALL))
			{
				activeChar.sendMessage("This is a Clan Hall zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_WATER))
			{
				activeChar.sendMessage("This is a Water zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_JAIL))
			{
				activeChar.sendMessage("This is a Jail zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_MONSTERTRACK))
			{
				activeChar.sendMessage("This is a Monster Track zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_NOHQ))
			{
				activeChar.sendMessage("This is a Castle zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_UNUSED))
			{
				activeChar.sendMessage("This zone is not used.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_BOSS))
			{
				activeChar.sendMessage("This is a Boss zone.");
			}
			if (activeChar.isInsideZone(L2Character.ZONE_EFFECT))
			{
				activeChar.sendMessage("This is an Effect zone.");
			}
			
			activeChar.sendMessage("Closest Town: " + MapRegionTable.getInstance().getClosestTownName(activeChar));
		}
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_ZONE_COMMANDS;
	}
}