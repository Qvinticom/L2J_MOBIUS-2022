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
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

/**
 * @author eX1steam
 */
public class CastleBlacksmith extends Folk
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public CastleBlacksmith(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the Player already target the Npc
		if (this != player.getTarget())
		{
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			
			// Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player)) // Calculate the distance between the Player and the Npc
		{
			// Notify the Player AI with AI_INTENTION_INTERACT
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else
		{
			showMessageWindow(player, 0);
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
		{
			return;
		}
		
		if ((condition != COND_BUSY_BECAUSE_OF_SIEGE) && (condition == COND_OWNER))
		{
			if (command.startsWith("Chat"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch (IndexOutOfBoundsException | NumberFormatException ioobe)
				{
				}
				showMessageWindow(player, val);
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
		}
	}
	
	private void showMessageWindow(Player player, int value)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/castleblacksmith/castleblacksmith-no.htm";
		
		final int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/castleblacksmith/castleblacksmith-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER)
			{
				// Clan owns castle
				if (value == 0)
				{
					filename = "data/html/castleblacksmith/castleblacksmith.htm";
				}
				else
				{
					filename = "data/html/castleblacksmith/castleblacksmith-" + value + ".htm";
				}
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%castleid%", Integer.toString(getCastle().getCastleId()));
		player.sendPacket(html);
	}
	
	protected int validateCondition(Player player)
	{
		if (player.isGM())
		{
			return COND_OWNER;
		}
		
		if ((getCastle() != null) && (getCastle().getCastleId() > 0) && (player.getClan() != null))
		{
			if (getCastle().getSiege().isInProgress())
			{
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			}
			else if ((getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
				&& ((player.getClanPrivileges() & Clan.CP_CS_MANOR_ADMIN) == Clan.CP_CS_MANOR_ADMIN))
			{
				return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
