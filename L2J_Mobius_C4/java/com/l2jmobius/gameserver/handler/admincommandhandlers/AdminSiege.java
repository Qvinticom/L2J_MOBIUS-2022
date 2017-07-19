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
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.instancemanager.AuctionManager;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Castle;
import com.l2jmobius.gameserver.model.entity.ClanHall;
import com.l2jmobius.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

/**
 * This class handles all siege commands: Todo: change the class name, and neaten it up
 */
public class AdminSiege implements IAdminCommandHandler
{
	// private static Logger _log = Logger.getLogger(AdminSiege.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_siege",
		"admin_add_attacker",
		"admin_add_defender",
		"admin_add_guard",
		"admin_list_siege_clans",
		"admin_clear_siege_list",
		"admin_move_defenders",
		"admin_spawn_doors",
		"admin_endsiege",
		"admin_startsiege",
		"admin_setcastle",
		"admin_clanhall",
		"admin_clanhallset",
		"admin_clanhalldel",
		"admin_clanhallopendoors",
		"admin_clanhallclosedoors",
		"admin_clanhallteleportself"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if ((activeChar.getAccessLevel() < REQUIRED_LEVEL) || !activeChar.isGM())
			{
				return false;
			}
		}
		
		final StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get castle
		Castle castle = null;
		ClanHall clanhall = null;
		if (command.startsWith("admin_clanhall"))
		{
			clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(st.nextToken()));
		}
		else if (st.hasMoreTokens())
		{
			castle = CastleManager.getInstance().getCastle(st.nextToken());
		}
		
		// Get castle
		String val = "";
		if (st.hasMoreTokens())
		{
			val = st.nextToken();
		}
		
		if (((castle == null) || (castle.getCastleId() < 0)) && (clanhall == null))
		{
			// No castle specified
			showCastleSelectPage(activeChar);
		}
		else
		{
			final L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_attacker"))
			{
				if (player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				}
				else
				{
					if ((castle != null) && !SiegeManager.getInstance().checkIsRegistered(player.getClan(), castle.getCastleId()))
					{
						castle.getSiege().registerAttacker(player, true);
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_add_defender"))
			{
				if (player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				}
				else
				{
					if ((castle != null) && !SiegeManager.getInstance().checkIsRegistered(player.getClan(), castle.getCastleId()))
					{
						castle.getSiege().registerDefender(player, true);
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_add_guard"))
			{
				if ((castle != null) && (val != ""))
				{
					try
					{
						final int npcId = Integer.parseInt(val);
						castle.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, npcId);
					}
					catch (final Exception e)
					{
						activeChar.sendMessage("Value entered for Npc Id wasn't an integer");
					}
				}
				else
				{
					activeChar.sendMessage("Missing Npc Id");
				}
			}
			else if ((castle != null) && command.equalsIgnoreCase("admin_clear_siege_list"))
			{
				castle.getSiege().clearSiegeClan();
			}
			else if ((castle != null) && command.equalsIgnoreCase("admin_endsiege"))
			{
				castle.getSiege().endSiege();
			}
			else if ((castle != null) && command.equalsIgnoreCase("admin_list_siege_clans"))
			{
				castle.getSiege().listRegisterClan(activeChar);
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_defenders"))
			{
				activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."));
			}
			else if (command.equalsIgnoreCase("admin_setcastle"))
			{
				if ((player == null) || (player.getClan() == null))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				}
				else if ((castle != null) && (player.getClan().getHasCastle() == 0))
				{
					castle.setOwner(player.getClan());
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhallset"))
			{
				if ((player == null) || (player.getClan() == null))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				}
				else
				{
					if (player.getClan().getHasHideout() == 0)
					{
						if ((clanhall != null) && (clanhall.getOwnerId() == 0))
						{
							clanhall.setOwner(player.getClan());
							
							if (AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null)
							{
								AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).cancelBid(player.getClan().getClanId());
							}
							
							if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null)
							{
								if (!AuctionManager.getInstance().getAuction(clanhall.getId()).getBidders().isEmpty())
								{
									AuctionManager.getInstance().getAuction(clanhall.getId()).removeBids();
								}
								AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
							}
						}
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhalldel"))
			{
				if ((clanhall != null) && (clanhall.getOwnerId() > 0))
				{
					clanhall.setOwner(null);
				}
			}
			else if ((clanhall != null) && command.equalsIgnoreCase("admin_clanhallopendoors"))
			{
				clanhall.openCloseDoors(true);
			}
			else if ((clanhall != null) && command.equalsIgnoreCase("admin_clanhallclosedoors"))
			{
				clanhall.openCloseDoors(false);
			}
			else if (command.equalsIgnoreCase("admin_clanhallteleportself"))
			{
				if (clanhall != null)
				{
					final L2ClanHallZone zone = clanhall.getZone();
					if (zone != null)
					{
						activeChar.teleToLocation(zone.getSpawnLoc(), true);
					}
				}
			}
			else if ((castle != null) && command.equalsIgnoreCase("admin_spawn_doors"))
			{
				castle.spawnDoor();
			}
			else if ((castle != null) && (clanhall != null) && command.equalsIgnoreCase("admin_startsiege"))
			{
				castle.getSiege().startSiege();
			}
			
			if (clanhall != null)
			{
				showClanHallPage(activeChar, clanhall);
			}
			else if (castle != null)
			{
				showSiegePage(activeChar, castle.getName());
			}
		}
		
		return true;
	}
	
	public void showCastleSelectPage(L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Siege Castle ClanHall Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<center>");
		replyMSG.append("<br>Please select<br1>");
		replyMSG.append("<table width=320><tr>");
		replyMSG.append("<td>Castles:<br></td><td>ClanHalls:<br></td><td></td></tr><tr>");
		replyMSG.append("<td>");
		
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null)
			{
				replyMSG.append("<a action=\"bypass -h admin_siege " + castle.getName() + "\">" + castle.getName() + "</a><br1>");
			}
		}
		replyMSG.append("</td><td>");
		int id = 0;
		for (final ClanHall clanhall : ClanHallManager.getInstance().getClanHalls())
		{
			id++;
			if (id > 15)
			{
				replyMSG.append("</td><td>");
				id = 0;
			}
			if (clanhall != null)
			{
				replyMSG.append("<a action=\"bypass -h admin_clanhall " + clanhall.getId() + "\">" + clanhall.getName() + "</a><br1>");
			}
		}
		replyMSG.append("</td></tr></table>");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void showSiegePage(L2PcInstance activeChar, String castleName)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Siege Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_siege\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<center>");
		replyMSG.append("<br><br><br>Castle: " + castleName + "<br><br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Add Attacker\" action=\"bypass -h admin_add_attacker " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Add Defender\" action=\"bypass -h admin_add_defender " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"List Clans\" action=\"bypass -h admin_list_siege_clans " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Clear List\" action=\"bypass -h admin_clear_siege_list " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Move Defenders\" action=\"bypass -h admin_move_defenders " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Spawn Doors\" action=\"bypass -h admin_spawn_doors " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Start Siege\" action=\"bypass -h admin_startsiege " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"End Siege\" action=\"bypass -h admin_endsiege " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Give Castle\" action=\"bypass -h admin_setcastle " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>NpcId: <edit var=\"value\" width=40>");
		replyMSG.append("<td><button value=\"Add Guard\" action=\"bypass -h admin_add_guard " + castleName + " $value\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void showClanHallPage(L2PcInstance activeChar, ClanHall clanhall)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Siege Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_siege\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<center>");
		replyMSG.append("<br><br><br>ClanHall: " + clanhall.getName() + "<br>");
		
		final L2Clan owner = ClanTable.getInstance().getClan(clanhall.getOwnerId());
		if (owner == null)
		{
			replyMSG.append("ClanHall Owner: none<br><br>");
		}
		else
		{
			replyMSG.append("ClanHall Owner: " + owner.getName() + "<br><br>");
		}
		
		// replyMSG.append("<table>");
		// replyMSG.append("<tr><td><button value=\" Owner\" action=\"bypass -h admin_clanhallset " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		// replyMSG.append("</table>");
		replyMSG.append("<br>");
		// replyMSG.append("<td><button value=\"Add Defender\" action=\"bypass -h admin_add_defender " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		// replyMSG.append("<tr><td><button value=\"List Clans\" action=\"bypass -h admin_list_siege_clans " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		// replyMSG.append("<td><button value=\"Clear List\" action=\"bypass -h admin_clear_siege_list " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		// replyMSG.append("<table>");
		// replyMSG.append("<tr><td><button value=\"Move Defenders\" action=\"bypass -h admin_move_defenders " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		// replyMSG.append("<td><button value=\"Spawn Doors\" action=\"bypass -h admin_spawn_doors " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		// replyMSG.append("</table>");
		replyMSG.append("<br>");
		// replyMSG.append("<table>");
		// replyMSG.append("<tr><td><button value=\"Start Siege\" action=\"bypass -h admin_startsiege " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		// replyMSG.append("<td><button value=\"End Siege\" action=\"bypass -h admin_endsiege " + castleName + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		// replyMSG.append("</table>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Open Doors\" action=\"bypass -h admin_clanhallopendoors " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Close Doors\" action=\"bypass -h admin_clanhallclosedoors " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Give ClanHall\" action=\"bypass -h admin_clanhallset " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Take ClanHall\" action=\"bypass -h admin_clanhalldel " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table><tr>");
		// replyMSG.append("<tr><td>NpcId: <edit var=\"value\" width=40>");
		replyMSG.append("<td><button value=\"Teleport self\" action=\"bypass -h admin_clanhallteleportself " + clanhall.getId() + " \" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}