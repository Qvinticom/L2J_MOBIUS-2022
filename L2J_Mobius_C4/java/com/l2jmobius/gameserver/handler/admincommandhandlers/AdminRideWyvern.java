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
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author TODO nothing.
 */
public class AdminRideWyvern implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_ride_wyvern",
		"admin_ride_strider",
		"admin_unride_wyvern",
		"admin_unride_strider",
		"admin_unride",
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_RIDER;
	private int PetRideId;
	
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
		
		if (command.startsWith("admin_ride"))
		{
			if (activeChar.isMounted() || (activeChar.getPet() != null))
			{
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Already Have a Pet or Mounted.");
				activeChar.sendPacket(sm);
				return false;
			}
			if (command.startsWith("admin_ride_wyvern"))
			{
				PetRideId = 12621;
			}
			else if (command.startsWith("admin_ride_strider"))
			{
				PetRideId = 12526;
			}
			else
			{
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Command '" + command + "' not recognized");
				activeChar.sendPacket(sm);
				return false;
			}
			
			activeChar.mount(PetRideId, 0, false);
			
			return false;
		}
		else if (command.startsWith("admin_unride"))
		{
			activeChar.dismount();
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