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

/**
 * This class handles following admin commands:
 * <ul>
 * <li>admin_unblockip</li>
 * </ul>
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminUnblockIp implements IAdminCommandHandler
{
	// private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());
	
	private static final int REQUIRED_LEVEL = Config.GM_UNBLOCK;
	
	private static String[] _adminCommands =
	{
		"admin_unblockip"
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, com.l2jmobius.gameserver.model.L2PcInstance)
	 */
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
		
		if (command.startsWith("admin_unblockip "))
		{
			try
			{
				activeChar.sendMessage("Please remove the IP address of your choice from banned_ip.cfg file, and restart Login Server.");
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Send syntax to the user
				activeChar.sendMessage("Usage mode: //unblockip <ip>");
			}
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