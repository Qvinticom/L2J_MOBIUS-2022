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
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.LoginServerThread;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands: - kill = kills target L2Character
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBan implements IAdminCommandHandler
{
	// private static Logger _log = Logger.getLogger(AdminBan.class.getName());
	private static String[] _adminCommands =
	{
		"admin_ban",
		"admin_unban",
		"admin_jail",
		"admin_unjail"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (activeChar.getAccessLevel() < Config.GM_BAN)
			{
				return false;
			}
			
		}
		
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String player = "";
		L2PcInstance plyr = null;
		if (command.startsWith("admin_ban"))
		{
			try
			{
				player = st.nextToken();
				plyr = L2World.getInstance().getPlayer(player);
			}
			catch (final Exception e)
			{
				final L2Object target = activeChar.getTarget();
				if ((target != null) && (target instanceof L2PcInstance))
				{
					plyr = (L2PcInstance) target;
				}
				else
				{
					activeChar.sendMessage("Wrong parameter or target.");
				}
			}
			
			if ((plyr != null) && (plyr == activeChar))
			{
				activeChar.sendMessage("You cannot use it on yourself.");
			}
			else if (plyr == null)
			{
				LoginServerThread.getInstance().sendAccessLevel(player, -100);
				activeChar.sendMessage("This player is not ingame, so an account ban request was sent for " + player + " in case this name belongs to an account.");
			}
			else
			{
				plyr.setAccountAccesslevel(-100);
				activeChar.sendMessage("Account of " + plyr.getName() + " was banned.");
				plyr.logout();
			}
		}
		else if (command.startsWith("admin_unban"))
		{
			try
			{
				player = st.nextToken();
				LoginServerThread.getInstance().sendAccessLevel(player, 0);
				activeChar.sendMessage("Unban request was sent for account " + player + ".");
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //unban <account_name>");
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_jail"))
		{
			try
			{
				player = st.nextToken();
				int delay = 0;
				try
				{
					delay = Integer.parseInt(st.nextToken());
				}
				catch (final NumberFormatException nfe)
				{
					activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes]");
				}
				catch (final NoSuchElementException nsee)
				{
				}
				final L2PcInstance playerObj = L2World.getInstance().getPlayer(player);
				
				if (playerObj != null)
				{
					playerObj.setInJail(true, delay);
					activeChar.sendMessage("Character " + player + " has been jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
				}
				else
				{
					jailOfflinePlayer(activeChar, player, delay);
				}
			}
			catch (final NoSuchElementException nsee)
			{
				activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes]");
			}
			catch (final Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_unjail"))
		{
			try
			{
				player = st.nextToken();
				final L2PcInstance playerObj = L2World.getInstance().getPlayer(player);
				
				if (playerObj != null)
				{
					playerObj.setInJail(false, 0);
					activeChar.sendMessage("Character " + player + " removed from jail.");
				}
				else
				{
					unjailOfflinePlayer(activeChar, player);
				}
			}
			catch (final NoSuchElementException nsee)
			{
				activeChar.sendMessage("Specify a character name.");
			}
			catch (final Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		
		GMAudit.auditGMAction(activeChar.getName(), command, player, "");
		return true;
	}
	
	private void jailOfflinePlayer(L2PcInstance activeChar, String name, int delay)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?"))
		{
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, 1);
			statement.setLong(5, delay * 60000);
			statement.setString(6, name);
			
			statement.execute();
			final int count = statement.getUpdateCount();
			
			if (count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
		}
		catch (final SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");
			if (Config.DEBUG)
			{
				se.printStackTrace();
			}
		}
	}
	
	private void unjailOfflinePlayer(L2PcInstance activeChar, String name)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?"))
		{
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			
			statement.execute();
			final int count = statement.getUpdateCount();
			
			if (count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " removed from jail.");
			}
		}
		catch (final SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player.");
			if (Config.DEBUG)
			{
				se.printStackTrace();
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}