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
package com.l2jmobius.gameserver.handler.usercommandhandlers;

import java.text.SimpleDateFormat;

import com.l2jmobius.gameserver.handler.IUserCommandHandler;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import javolution.text.TextBuilder;

/**
 * Support for clan penalty user command.
 * @author Tempy
 */
public class ClanPenalty implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		100
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IUserCommandHandler#useUserCommand(int, com.l2jmobius.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		boolean penalty = false;
		final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		final TextBuilder htmlContent = new TextBuilder("<html><body>");
		htmlContent.append("<center><table width=270 border=0 bgcolor=111111>");
		htmlContent.append("<tr><td width=170>Penalty</td>");
		htmlContent.append("<td width=100 align=center>Expiration Date</td></tr>");
		htmlContent.append("</table><table width=270 border=0><tr>");
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<tr><td width=170>Unable to join a clan.</td>");
			htmlContent.append("<td width=100 align=center>" + format.format(activeChar.getClanJoinExpiryTime()) + "</td></tr>");
			penalty = true;
		}
		
		if (activeChar.getClanCreateExpiryTime() > System.currentTimeMillis())
		{
			
			htmlContent.append("<tr><td width=170>Unable to create a clan.</td>");
			htmlContent.append("<td width=100 align=center>" + format.format(activeChar.getClanCreateExpiryTime()) + "</td></tr>");
			penalty = true;
		}
		
		if (activeChar.getClan() != null)
		{
			if (activeChar.getClan().getCharPenaltyExpiryTime() > System.currentTimeMillis())
			{
				htmlContent.append("<tr><td width=170>Unable to invite players to clan.</td>");
				htmlContent.append("<td width=100 align=center>" + format.format(activeChar.getClan().getCharPenaltyExpiryTime()) + "</td></tr>");
				
				penalty = true;
			}
			
			if (activeChar.getClan().getRecoverPenaltyExpiryTime() > System.currentTimeMillis())
			{
				htmlContent.append("<tr><td width=170>Unable to dissolve clan.</td>");
				htmlContent.append("<td width=100 align=center>" + format.format(activeChar.getClan().getRecoverPenaltyExpiryTime()) + "</td></tr>");
				
				penalty = true;
			}
		}
		
		if (!penalty)
		
		{
			htmlContent.append("<td width=170>No penalties currently in effect.</td>");
			htmlContent.append("<td width=100 align=center> </td>");
		}
		
		htmlContent.append("</tr></table><img src=\"L2UI.SquareWhite\" width=270 height=1>");
		htmlContent.append("</center></body></html>");
		
		final NpcHtmlMessage penaltyHtml = new NpcHtmlMessage(0);
		penaltyHtml.setHtml(htmlContent.toString());
		activeChar.sendPacket(penaltyHtml);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}