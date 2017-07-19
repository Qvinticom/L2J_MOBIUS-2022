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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands: - changelvl = change a character's access level Can be used for character ban (as opposed to regular //ban that affects accounts) or to grant mod/GM privileges ingame
 * @version $Revision: 1.1.2.2.2.3 $ $Date: 2005/04/11 10:06:00 $
 */
public class AdminChangeAccessLevel implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_changelvl"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;
	
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
		
		handleChangeLevel(command, activeChar);
		final String target = (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
		GMAudit.auditGMAction(activeChar.getName(), command, target, "");
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
	
	/**
	 * If no character name is specified, tries to change GM's target access level. Else if a character name is provided, will try to reach it either from L2World or from a database connection.
	 * @param command
	 * @param activeChar
	 */
	private void handleChangeLevel(String command, L2PcInstance activeChar)
	{
		final String[] parts = command.split(" ");
		if (parts.length == 2)
		{
			try
			{
				final int lvl = Integer.parseInt(parts[1]);
				if (activeChar.getTarget() instanceof L2PcInstance)
				{
					((L2PcInstance) activeChar.getTarget()).setAccessLevel(lvl);
					if (lvl < 0)
					{
						((L2PcInstance) activeChar.getTarget()).logout();
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Target.");
				}
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
			}
		}
		else if (parts.length == 3)
		{
			final String name = parts[1];
			final int lvl = Integer.parseInt(parts[2]);
			final L2PcInstance player = L2World.getInstance().getPlayer(name);
			if (player != null)
			{
				player.setAccessLevel(lvl);
				if (lvl < 0)
				{
					player.logout();
				}
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?"))
				{
					statement.setInt(1, lvl);
					statement.setString(2, name);
					statement.execute();
					final int count = statement.getUpdateCount();
					if (count == 0)
					{
						activeChar.sendMessage("Character not found or access level unaltered.");
					}
					else
					{
						activeChar.sendMessage("Character access level updated.");
					}
				}
				catch (final SQLException se)
				{
					activeChar.sendMessage("Error while changing character's access level.");
					if (Config.DEBUG)
					{
						se.printStackTrace();
					}
				}
			}
		}
	}
}