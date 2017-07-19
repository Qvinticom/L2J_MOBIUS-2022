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
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - help path = shows /data/html/admin/path file to char, should not be used by GM's directly
 * @version $Revision: 1.2.4.3 $ $Date: 2005/04/11 10:06:02 $
 */
public class AdminHelpPage implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_help"
	};
	private static final int REQUIRED_LEVEL = Config.GM_MIN;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!checkLevel(activeChar.getAccessLevel()))
			{
				return false;
			}
		}
		
		if (command.startsWith("admin_help"))
		{
			try
			{
				final String val = command.substring(11);
				showHelpPage(activeChar, val);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// case of empty filename
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
	
	// FIXME: implement method to send html to player in L2PcInstance directly
	// PUBLIC & STATIC so other classes from package can include it directly
	public static void showHelpPage(L2PcInstance targetChar, String filename)
	{
		final String content = HtmCache.getInstance().getHtmForce("data/html/admin/" + filename);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		targetChar.sendPacket(adminReply);
	}
}