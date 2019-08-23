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
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

/**
 * @author xAddytzu moded by Bobi
 */
public class RaidBossManagerInstance extends NpcInstance
{
	public RaidBossManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the PlayerInstance already target the Npc
		if (this != player.getTarget())
		{
			// Set the target of the PlayerInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			
			// Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player)) // Calculate the distance between the PlayerInstance and the Npc
		{
			// Notify the PlayerInstance AI with AI_INTENTION_INTERACT
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else
		{
			showChatWindow(player);
		}
		// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void showChatWindow(PlayerInstance player, int val)
	{
		final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(rbWindow(player));
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	private String rbWindow(PlayerInstance player)
	{
		final StringBuilder tb = new StringBuilder();
		tb.append("<html><title>L2 Raidboss Manager</title><body>");
		tb.append("<center>");
		tb.append("<br>");
		tb.append("<font color=\"999999\">Raidboss Manager</font><br>");
		tb.append("<img src=\"L2UI.SquareGray\" width=\"200\" height=\"1\"><br>");
		tb.append("Welcome " + player.getName() + "<br>");
		tb.append("<table width=\"85%\"><tr><td>We gatekeepers use the will of the gods to open the doors of time and space and teleport others. Which door would you like to open?</td></tr></table><br>");
		
		tb.append("<img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\"></center><br>");
		tb.append("<table width=180>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_40\">Raidboss Level (40-45)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_45\">Raidboss Level (45-50)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_50\">Raidboss Level (50-55)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_55\">Raidboss Level (55-60)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_60\">Raidboss Level (60-65)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_65\">Raidboss Level (65-70)</a></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><a action=\"bypass -h npc_%objectId%_RaidbossLvl_70\">Raidboss Level (70-75)</a></center></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		tb.append("<font color=\"999999\">Gates of Fire</font></center>");
		tb.append("</body></html>");
		return tb.toString();
	}
}