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
import com.l2jmobius.gameserver.LoginServerThread;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - handles ever admin menu command
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminMenu implements IAdminCommandHandler
{
	// private static final Logger _log = Logger.getLogger(AdminMenu.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_char_manage",
		"admin_teleport_character_to_menu",
		"admin_recall_char_menu",
		"admin_goto_char_menu",
		"admin_kick_menu",
		"admin_kill_menu",
		"admin_ban_menu",
		"admin_unban_menu"
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
		
		final String target = (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
		GMAudit.auditGMAction(activeChar.getName(), command, target, "");
		
		if (command.equals("admin_char_manage"))
		{
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
		}
		else if (command.startsWith("admin_teleport_character_to_menu"))
		{
			final String[] data = command.split(" ");
			if (data.length == 5)
			{
				final String playerName = data[1];
				final int x = Integer.parseInt(data[2]);
				final int y = Integer.parseInt(data[3]);
				final int z = Integer.parseInt(data[4]);
				final L2PcInstance player = L2World.getInstance().getPlayer(playerName);
				if (player != null)
				{
					teleportCharacter(player, x, y, z, activeChar);
				}
			}
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
		}
		else if (command.startsWith("admin_recall_char_menu"))
		{
			try
			{
				final String targetName = command.substring(23);
				final L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				final int x = activeChar.getX();
				final int y = activeChar.getY();
				final int z = activeChar.getZ();
				teleportCharacter(player, x, y, z, activeChar);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_goto_char_menu"))
		{
			try
			{
				final String targetName = command.substring(21);
				final L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				teleportToCharacter(activeChar, player);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.equals("admin_kill_menu"))
		{
			handleKill(activeChar);
		}
		else if (command.startsWith("admin_kick_menu"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			// System.out.println("Tokens: "+st.countTokens());
			if (st.countTokens() > 1)
			{
				st.nextToken();
				final String player = st.nextToken();
				// System.out.println("Player1 "+player);
				final L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				final SystemMessage sm = new SystemMessage(614);
				if (plyr != null)
				{
					// System.out.println("Player2 "+plyr.getName());
					plyr.logout();
					sm.addString("You kicked " + plyr.getName() + " from the game.");
				}
				else
				{
					sm.addString("Player " + player + " was not found in the game.");
				}
				activeChar.sendPacket(sm);
			}
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
		}
		else if (command.startsWith("admin_ban_menu"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				final String player = st.nextToken();
				final L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				if (plyr != null)
				{
					plyr.logout();
					LoginServerThread.getInstance().sendAccessLevel(plyr.getAccountName(), -100);
					activeChar.sendMessage("A ban request has been sent for account " + plyr.getAccountName() + ".");
				}
				else
				{
					activeChar.sendMessage("Target is not online.");
				}
				
			}
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
		}
		else if (command.startsWith("admin_unban_menu"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				final String player = st.nextToken();
				LoginServerThread.getInstance().sendAccessLevel(player, 0);
				activeChar.sendMessage("An unban request has been sent for account " + player + ".");
			}
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
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
	
	private void handleKill(L2PcInstance activeChar)
	{
		handleKill(activeChar, null);
	}
	
	private void handleKill(L2PcInstance activeChar, String player)
	{
		L2Object obj = activeChar.getTarget();
		if (player != null)
		{
			final L2PcInstance plyr = L2World.getInstance().getPlayer(player);
			if (plyr != null)
			{
				obj = plyr;
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("You killed " + plyr.getName() + ".");
				activeChar.sendPacket(sm);
			}
		}
		
		if ((obj != null) && (obj instanceof L2Character))
		{
			final L2Character target = (L2Character) obj;
			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
		}
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
	
	private void teleportCharacter(L2PcInstance player, int x, int y, int z, L2PcInstance activeChar)
	{
		if (player != null)
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Admin is teleporting you.");
			player.sendPacket(sm);
			
			player.teleToLocation(x, y, z, true);
		}
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
	
	private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
	{
		L2PcInstance player = null;
		if ((target != null) && (target instanceof L2PcInstance))
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("You cannot self teleport.");
			activeChar.sendPacket(sm);
		}
		else
		{
			final int x = player.getX();
			final int y = player.getY();
			final int z = player.getZ();
			
			activeChar.teleToLocation(x, y, z, true);
			
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("You have teleported to character " + player.getName() + ".");
			activeChar.sendPacket(sm);
		}
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
}