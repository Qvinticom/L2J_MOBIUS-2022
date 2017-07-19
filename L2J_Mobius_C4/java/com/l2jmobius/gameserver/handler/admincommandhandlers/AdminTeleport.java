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
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

import javolution.text.TextBuilder;

/**
 * This class handles following admin commands: - show_moves - show_teleport - teleport_to_character - move_to - teleport_character
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminTeleport implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_show_moves",
		"admin_show_moves_other",
		"admin_show_teleport",
		"admin_teleport_to_character",
		"admin_teleportto",
		"admin_move_to",
		"admin_teleport_character",
		"admin_recall",
		"admin_walk",
		"admin_explore",
		"teleportto",
		"recall",
		"admin_recall_npc",
		"admin_gonorth",
		"admin_gosouth",
		"admin_goeast",
		"admin_gowest",
		"admin_goup",
		"admin_godown",
		"admin_tele",
		"admin_teleto",
		"admin_failed"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_TELEPORT;
	private static final int REQUIRED_LEVEL2 = Config.GM_TELEPORT_OTHER;
	
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
		
		if (command.equals("admin_teleto"))
		{
			activeChar.setTeleMode(1);
		}
		if (command.equals("admin_teleto r"))
		{
			activeChar.setTeleMode(2);
		}
		if (command.equals("admin_teleto end"))
		{
			activeChar.setTeleMode(0);
		}
		if (command.equals("admin_show_moves"))
		{
			AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
		}
		if (command.equals("admin_show_moves_other"))
		{
			AdminHelpPage.showHelpPage(activeChar, "tele/other.html");
		}
		else if (command.equals("admin_show_teleport"))
		{
			showTeleportCharWindow(activeChar);
		}
		else if (command.equals("admin_recall_npc"))
		{
			recallNPC(activeChar);
		}
		else if (command.equals("admin_teleport_to_character"))
		{
			teleportToCharacter(activeChar, activeChar.getTarget());
		}
		else if (command.equals("admin_explore") && Config.ACTIVATE_POSITION_RECORDER)
		{
			activeChar._exploring = !activeChar._exploring;
			activeChar.explore();
		}
		else if (command.startsWith("admin_walk"))
		{
			try
			{
				final String val = command.substring(11);
				final StringTokenizer st = new StringTokenizer(val);
				final String x1 = st.nextToken();
				final int x = Integer.parseInt(x1);
				final String y1 = st.nextToken();
				final int y = Integer.parseInt(y1);
				final String z1 = st.nextToken();
				final int z = Integer.parseInt(z1);
				final L2CharPosition pos = new L2CharPosition(x, y, z, 0);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
			}
			catch (final Exception e)
			{
				if (Config.DEBUG)
				{
					_log.info("admin_walk: " + e);
				}
			}
		}
		else if (command.startsWith("admin_move_to"))
		{
			try
			{
				final String val = command.substring(14);
				teleportTo(activeChar, val);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty co-ordinates
				activeChar.sendMessage("Wrong or no Co-ordinates given.");
			}
		}
		else if (command.startsWith("admin_teleport_character"))
		{
			try
			{
				final String val = command.substring(25);
				
				if (activeChar.getAccessLevel() >= REQUIRED_LEVEL2)
				{
					teleportCharacter(activeChar, val);
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty co-ordinates
				activeChar.sendMessage("Wrong or no Co-ordinates given.");
				
				showTeleportCharWindow(activeChar); // back to character teleport
			}
		}
		else if (command.startsWith("admin_teleportto "))
		{
			try
			{
				final String targetName = command.substring(17);
				final L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				teleportToCharacter(activeChar, player);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_recall "))
		{
			try
			{
				if (activeChar.getAccessLevel() < REQUIRED_LEVEL2)
				{
					return false;
				}
				
				final String[] param = command.split(" ");
				if (param.length != 2)
				{
					activeChar.sendMessage("Usage: //recall <playername>");
					return false;
				}
				final String targetName = param[1];
				final L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				if (player != null)
				{
					teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar);
				}
				else
				{
					changeCharacterPosition(activeChar, targetName);
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_failed"))
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("Trying ActionFailed...");
			activeChar.sendPacket(sm);
			activeChar.sendPacket(new ActionFailed());
		}
		else if (command.equals("admin_tele"))
		{
			showTeleportWindow(activeChar);
		}
		else if (command.equals("admin_goup"))
		{
			final int x = activeChar.getX();
			final int y = activeChar.getY();
			final int z = activeChar.getZ() + 150;
			activeChar.teleToLocation(x, y, z, false);
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_goup"))
		{
			try
			{
				final String val = command.substring(11);
				final int intVal = Integer.parseInt(val);
				final int x = activeChar.getX();
				final int y = activeChar.getY();
				final int z = activeChar.getZ() + intVal;
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
		}
		else if (command.equals("admin_godown"))
		{
			final int x = activeChar.getX();
			final int y = activeChar.getY();
			final int z = activeChar.getZ();
			activeChar.teleToLocation(x, y, z - 150, false);
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_godown"))
		{
			try
			{
				final String val = command.substring(13);
				final int intVal = Integer.parseInt(val);
				final int x = activeChar.getX();
				final int y = activeChar.getY();
				final int z = activeChar.getZ() - intVal;
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
		}
		else if (command.equals("admin_goeast"))
		{
			final int x = activeChar.getX();
			final int y = activeChar.getY();
			final int z = activeChar.getZ();
			activeChar.teleToLocation(x + 150, y, z, false);
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_goeast"))
		{
			try
			{
				final String val = command.substring(13);
				final int intVal = Integer.parseInt(val);
				final int x = activeChar.getX() + intVal;
				final int y = activeChar.getY();
				final int z = activeChar.getZ();
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
		}
		else if (command.equals("admin_gowest"))
		{
			final int x = activeChar.getX();
			final int y = activeChar.getY();
			final int z = activeChar.getZ();
			activeChar.teleToLocation(x - 150, y, z, false);
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_gowest"))
		{
			try
			{
				final String val = command.substring(13);
				final int intVal = Integer.parseInt(val);
				final int x = activeChar.getX() - intVal;
				final int y = activeChar.getY();
				final int z = activeChar.getZ();
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
		}
		else if (command.equals("admin_gosouth"))
		{
			final int x = activeChar.getX();
			final int y = activeChar.getY() + 150;
			final int z = activeChar.getZ();
			activeChar.teleToLocation(x, y, z, false);
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_gosouth"))
		{
			try
			{
				final String val = command.substring(14);
				final int intVal = Integer.parseInt(val);
				final int x = activeChar.getX();
				final int y = activeChar.getY() + intVal;
				final int z = activeChar.getZ();
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
		}
		else if (command.equals("admin_gonorth"))
		{
			final int x = activeChar.getX();
			final int y = activeChar.getY();
			final int z = activeChar.getZ();
			activeChar.teleToLocation(x, y - 150, z, false);
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_gonorth"))
		{
			try
			{
				final String val = command.substring(14);
				final int intVal = Integer.parseInt(val);
				final int x = activeChar.getX();
				final int y = activeChar.getY() - intVal;
				final int z = activeChar.getZ();
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
			catch (final NumberFormatException nfe)
			{
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
	
	private void teleportTo(L2PcInstance activeChar, String Cords)
	{
		try
		{
			final StringTokenizer st = new StringTokenizer(Cords);
			final String x1 = st.nextToken();
			final int x = Integer.parseInt(x1);
			final String y1 = st.nextToken();
			final int y = Integer.parseInt(y1);
			final String z1 = st.nextToken();
			final int z = Integer.parseInt(z1);
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, false);
			
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("You have been teleported to " + Cords);
			activeChar.sendPacket(sm);
		}
		catch (final NoSuchElementException nsee)
		{
			activeChar.sendMessage("Wrong or no Co-ordinates given.");
		}
	}
	
	private void showTeleportWindow(L2PcInstance activeChar)
	{
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><title>Teleport Menu</title>");
		replyMSG.append("<body>");
		
		replyMSG.append("<br>");
		replyMSG.append("<center><table>");
		
		replyMSG.append("<tr><td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"North\" action=\"bypass -h admin_gonorth\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Up\" action=\"bypass -h admin_goup\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"West\" action=\"bypass -h admin_gowest\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"East\" action=\"bypass -h admin_goeast\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"South\" action=\"bypass -h admin_gosouth\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Down\" action=\"bypass -h admin_godown\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		
		replyMSG.append("</table></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showTeleportCharWindow(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendMessage("Incorrect target.");
			return;
		}
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><title>Teleport Character</title>");
		replyMSG.append("<body>");
		replyMSG.append("The character you will teleport is " + player.getName() + ".");
		replyMSG.append("<br>");
		
		replyMSG.append("Co-ordinate x");
		replyMSG.append("<edit var=\"char_cord_x\" width=110>");
		replyMSG.append("Co-ordinate y");
		replyMSG.append("<edit var=\"char_cord_y\" width=110>");
		replyMSG.append("Co-ordinate z");
		replyMSG.append("<edit var=\"char_cord_z\" width=110>");
		replyMSG.append("<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void teleportCharacter(L2PcInstance activeChar, String Cords)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendMessage("Incorrect target.");
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendMessage("You cannot teleport your character.");
		}
		else
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(Cords);
				final String x1 = st.nextToken();
				final int x = Integer.parseInt(x1);
				final String y1 = st.nextToken();
				final int y = Integer.parseInt(y1);
				final String z1 = st.nextToken();
				final int z = Integer.parseInt(z1);
				teleportCharacter(player, x, y, z, null);
			}
			catch (final NoSuchElementException nsee)
			{
			}
		}
	}
	
	/**
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 * @param activeChar
	 */
	private void teleportCharacter(L2PcInstance player, int x, int y, int z, L2PcInstance activeChar)
	{
		if (player != null)
		{
			// Check for jail
			if (player.isInJail())
			{
				activeChar.sendMessage("Sorry, player " + player.getName() + " is in Jail.");
				return;
			}
			
			// Information
			if (activeChar != null)
			{
				activeChar.sendMessage("You have recalled " + player.getName());
			}
			player.sendMessage("Admin is teleporting you.");
			
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.teleToLocation(x, y, z, true);
		}
	}
	
	private void changeCharacterPosition(L2PcInstance activeChar, String name)
	{
		final int x = activeChar.getX();
		final int y = activeChar.getY();
		final int z = activeChar.getZ();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?"))
		{
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);
			statement.setString(4, name);
			statement.execute();
			final int count = statement.getUpdateCount();
			if (count == 0)
			{
				activeChar.sendMessage("Character not found or position is not altered.");
			}
			else
			{
				activeChar.sendMessage("Player's [" + name + "] position is now set to (" + x + "," + y + "," + z + ").");
			}
		}
		catch (final SQLException se)
		{
			activeChar.sendMessage("SQLException while changing offline character's position");
		}
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
			activeChar.sendMessage("Incorrect target.");
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("You cannot self teleport.");
		}
		else
		{
			final int x = player.getX();
			final int y = player.getY();
			final int z = player.getZ();
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, true);
			
			activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
		}
	}
	
	private void recallNPC(L2PcInstance activeChar)
	{
		final L2Object obj = activeChar.getTarget();
		if ((obj != null) && (obj instanceof L2NpcInstance))
		{
			final L2NpcInstance target = (L2NpcInstance) obj;
			
			final int monsterTemplate = target.getTemplate().npcId;
			final L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
			if (template1 == null)
			{
				activeChar.sendMessage("Incorrect monster template.");
				_log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' template.");
				return;
			}
			
			L2Spawn spawn = target.getSpawn();
			
			if (spawn == null)
			{
				activeChar.sendMessage("Incorrect monster spawn.");
				_log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
				return;
			}
			
			final int respawnTime = spawn.getRespawnDelay();
			
			target.deleteMe();
			spawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(spawn, true);
			
			try
			{
				// L2MonsterInstance mob = new L2MonsterInstance(monsterTemplate, template1);
				
				spawn = new L2Spawn(template1);
				spawn.setLocx(activeChar.getX());
				spawn.setLocy(activeChar.getY());
				spawn.setLocz(activeChar.getZ());
				spawn.setAmount(1);
				spawn.setHeading(activeChar.getHeading());
				spawn.setRespawnDelay(respawnTime);
				SpawnTable.getInstance().addNewSpawn(spawn, true);
				spawn.init();
				
				final SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
				sm.addString("Created " + template1.name + " on " + target.getObjectId() + ".");
				activeChar.sendPacket(sm);
				
				if (Config.DEBUG)
				{
					_log.fine("Spawn at X=" + spawn.getLocx() + " Y=" + spawn.getLocy() + " Z=" + spawn.getLocz());
					_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") moved NPC " + target.getObjectId());
				}
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Target is not in game.");
			}
			
		}
		else
		{
			activeChar.sendMessage("Incorrect target.");
		}
	}
}