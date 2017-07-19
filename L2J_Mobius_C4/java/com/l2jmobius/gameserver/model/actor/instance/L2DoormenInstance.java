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
package com.l2jmobius.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.entity.ClanHall;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * @version $Revision$ $Date$
 */
public class L2DoormenInstance extends L2FolkInstance
{
	// private static Logger _log = Logger.getLogger(L2DoormenInstance.class.getName());
	
	private ClanHall _ClanHall;
	private static int Cond_All_False = 0;
	private static int Cond_Busy_Because_Of_Siege = 1;
	private static int Cond_Castle_Owner = 2;
	private static int Cond_Hall_Owner = 3;
	
	/**
	 * @param objectID
	 * @param template
	 */
	public L2DoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	public final ClanHall getClanHall()
	{
		// _log.warning(this.getName()+" searching ch");
		if (_ClanHall == null)
		{
			_ClanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
		}
		
		return _ClanHall;
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		final int condition = validateCondition(player);
		if (condition <= Cond_All_False)
		{
			return;
		}
		if (condition == Cond_Busy_Because_Of_Siege)
		{
			return;
		}
		else if ((condition == Cond_Castle_Owner) || (condition == Cond_Hall_Owner))
		{
			if (command.startsWith("Chat"))
			{
				showMessageWindow(player);
				return;
			}
			else if (command.startsWith("open_doors"))
			{
				if (condition == Cond_Hall_Owner)
				{
					getClanHall().openCloseDoors(true);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><head><body>You have <font color=\"LEVEL\">opened</font> the clan hall door.<br>Outsiders may enter the clan hall while the door is open. Please close it when you've finished your business.<br><center><button value=\"Close\" action=\"bypass -h npc_" + getObjectId() + "_close_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"));
				}
				else
				{
					
					final StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid
					
					if (condition == 2)
					{
						while (st.hasMoreTokens())
						{
							getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
						}
						return;
					}
				}
			}
			else if (command.startsWith("close_doors"))
			{
				if (condition == Cond_Hall_Owner)
				{
					getClanHall().openCloseDoors(false);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><head><body>You have <font color=\"LEVEL\">closed</font> the clan hall door.<br>Good day!<br><center><button value=\"To Begining\" action=\"bypass -h npc_" + getObjectId() + "_Chat\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"));
				}
				else
				{
					
					final StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid
					
					if (condition == 2)
					{
						while (st.hasMoreTokens())
						{
							getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
						}
						return;
					}
				}
			}
		}
		
		super.onBypassFeedback(player, command);
	}
	
	/**
	 * this is called when a player interacts with this NPC
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		player.setLastFolkNPC(this);
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	public void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		String filename = "data/html/doormen/" + getTemplate().npcId + "-no.htm";
		
		final int condition = validateCondition(player);
		if (condition == Cond_Busy_Because_Of_Siege)
		{
			filename = "data/html/doormen/" + getTemplate().npcId + "-busy.htm"; // Busy because of siege
		}
		else if (condition == Cond_Castle_Owner)
		{
			filename = "data/html/doormen/" + getTemplate().npcId + ".htm"; // Owner message window
		}
		
		// Prepare doormen for clan hall
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		if (getClanHall() != null)
		{
			if (condition == Cond_Hall_Owner)
			{
				str = "<html><body>Hello!<br><font color=\"55FFFF\">" + player.getName() + "</font>, I am honored to serve your clan.<br>How may i serve you?<br>";
				str += "<center><table><tr><td><button value=\"Open Door\" action=\"bypass -h npc_%objectId%_open_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1></td></tr></table><br>";
				str += "<table><tr><td><button value=\"Close Door\" action=\"bypass -h npc_%objectId%_close_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center></body></html>";
			}
			else
			{
				final L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
				if ((owner != null) && (owner.getLeader() != null))
				{
					str = "<html><body>Hello there!<br>This clan hall is owned by <font color=\"55FFFF\">" + owner.getLeader().getName() + " who is the Lord of the ";
					str += owner.getName() + "</font> clan.<br>";
					str += "I am sorry, but only the clan members who belong to the <font color=\"55FFFF\">" + owner.getName() + "</font> clan can enter the clan hall.</body></html>";
				}
				else
				{
					str = "<html><body>" + getName() + ":<br1>Clan hall <font color=\"LEVEL\">" + getClanHall().getName() + "</font> has no owner.<br>You can rent it at auctioneers.</body></html>";
				}
			}
			html.setHtml(str);
		}
		else
		{
			html.setFile(filename);
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	private int validateCondition(L2PcInstance player)
	{
		if (player.getClan() != null)
		{
			// Prepare doormen for clan hall
			if (getClanHall() != null)
			{
				if (player.getClanId() == getClanHall().getOwnerId())
				{
					return Cond_Hall_Owner;
				}
				return Cond_All_False;
			}
			if ((getCastle() != null) && (getCastle().getCastleId() > 0))
			{
				if (getCastle().getSiege().getIsInProgress())
				{
					return Cond_Busy_Because_Of_Siege; // Busy because of siege
				}
				else if (getCastle().getOwnerId() == player.getClanId())
				{
					return Cond_Castle_Owner; // Owner
				}
			}
		}
		
		return Cond_All_False;
	}
}