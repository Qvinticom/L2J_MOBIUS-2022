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
package org.l2jmobius.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.LoginServerThread;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - handles every admin menu command
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminMenu implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminMenu.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_char_manage",
		"admin_teleport_character_to_menu",
		"admin_recall_char_menu",
		"admin_recall_party_menu",
		"admin_recall_clan_menu",
		"admin_goto_char_menu",
		"admin_kick_menu",
		"admin_kill_menu",
		"admin_ban_menu",
		"admin_unban_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_char_manage"))
		{
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_teleport_character_to_menu"))
		{
			final String[] data = command.split(" ");
			if (data.length == 5)
			{
				final String playerName = data[1];
				final Player player = World.getInstance().getPlayer(playerName);
				if (player != null)
				{
					teleportCharacter(player, Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), activeChar, "Admin is teleporting you.");
				}
			}
			
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_recall_char_menu"))
		{
			try
			{
				final String targetName = command.substring(23);
				final Player player = World.getInstance().getPlayer(targetName);
				teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar, "Admin is teleporting you.");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_recall_party_menu"))
		{
			final int x = activeChar.getX();
			final int y = activeChar.getY();
			final int z = activeChar.getZ();
			
			try
			{
				final String targetName = command.substring(24);
				final Player player = World.getInstance().getPlayer(targetName);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return true;
				}
				
				if (!player.isInParty())
				{
					BuilderUtil.sendSysMessage(activeChar, "Player is not in party.");
					teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
					return true;
				}
				
				for (Player pm : player.getParty().getPartyMembers())
				{
					teleportCharacter(pm, x, y, z, activeChar, "Your party is being teleported by an Admin.");
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_recall_clan_menu"))
		{
			final int x = activeChar.getX();
			final int y = activeChar.getY();
			final int z = activeChar.getZ();
			try
			{
				final String targetName = command.substring(23);
				final Player player = World.getInstance().getPlayer(targetName);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return true;
				}
				
				final Clan clan = player.getClan();
				if (clan == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Player is not in a clan.");
					teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
					return true;
				}
				
				for (Player member : clan.getOnlineMembers())
				{
					teleportCharacter(member, x, y, z, activeChar, "Your clan is being teleported by an Admin.");
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_goto_char_menu"))
		{
			try
			{
				final String targetName = command.substring(21);
				final Player player = World.getInstance().getPlayer(targetName);
				teleportToCharacter(activeChar, player);
			}
			catch (StringIndexOutOfBoundsException e)
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
			if (st.countTokens() > 1)
			{
				st.nextToken();
				final String player = st.nextToken();
				final Player plyr = World.getInstance().getPlayer(player);
				if (plyr != null)
				{
					plyr.logout();
					BuilderUtil.sendSysMessage(activeChar, "You kicked " + plyr.getName() + " from the game.");
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Player " + player + " was not found in the game.");
				}
			}
			
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_ban_menu"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				final String player = st.nextToken();
				final Player plyr = World.getInstance().getPlayer(player);
				if (plyr != null)
				{
					plyr.logout();
				}
				
				setAccountAccessLevel(player, activeChar, -100);
			}
			
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_unban_menu"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				final String player = st.nextToken();
				setAccountAccessLevel(player, activeChar, 0);
			}
			
			showMainPage(activeChar);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleKill(Player activeChar)
	{
		handleKill(activeChar, null);
	}
	
	private void handleKill(Player activeChar, String player)
	{
		final WorldObject obj = activeChar.getTarget();
		Creature target = (Creature) obj;
		String filename = "main_menu.htm";
		if (player != null)
		{
			final Player plyr = World.getInstance().getPlayer(player);
			if (plyr != null)
			{
				target = plyr;
				BuilderUtil.sendSysMessage(activeChar, "You killed " + player);
			}
		}
		
		if (target != null)
		{
			if (target instanceof Player)
			{
				target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
				filename = "charmanage.htm";
			}
			else if (Config.CHAMPION_ENABLE && target.isChampion())
			{
				target.reduceCurrentHp((target.getMaxHp() * Config.CHAMPION_HP) + 1, activeChar);
			}
			else
			{
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
		}
		
		AdminHelpPage.showHelpPage(activeChar, filename);
	}
	
	private void teleportCharacter(Player player, int x, int y, int z, Player activeChar, String message)
	{
		if (player != null)
		{
			player.sendMessage(message);
			player.teleToLocation(x, y, z, true);
		}
		
		showMainPage(activeChar);
	}
	
	private void teleportToCharacter(Player activeChar, WorldObject target)
	{
		Player player = null;
		if ((target != null) && (target instanceof Player))
		{
			player = (Player) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_ON_YOURSELF);
		}
		else
		{
			activeChar.teleToLocation(player.getX(), player.getY(), player.getZ(), true);
			BuilderUtil.sendSysMessage(activeChar, "You're teleporting yourself to character " + player.getName());
		}
		
		showMainPage(activeChar);
	}
	
	private void showMainPage(Player activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
	
	private void setAccountAccessLevel(String player, Player activeChar, int banLevel)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final String stmt = "SELECT account_name FROM characters WHERE char_name = ?";
			final PreparedStatement statement = con.prepareStatement(stmt);
			statement.setString(1, player);
			final ResultSet result = statement.executeQuery();
			if (result.next())
			{
				final String accName = result.getString(1);
				if (accName.length() > 0)
				{
					LoginServerThread.getInstance().sendAccessLevel(accName, banLevel);
					BuilderUtil.sendSysMessage(activeChar, "Account Access Level for " + player + " set to " + banLevel + ".");
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Couldn't find player: " + player + ".");
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Specified player name didn't lead to a valid account.");
			}
			
			statement.close();
			result.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not set accessLevel:" + e);
		}
	}
}
