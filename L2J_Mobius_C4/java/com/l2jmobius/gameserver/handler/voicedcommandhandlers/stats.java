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
package com.l2jmobius.gameserver.handler.voicedcommandhandlers;

import java.util.Iterator;

import com.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import javolution.text.TextBuilder;

/**
 * 
 *
 */
public class stats implements IVoicedCommandHandler
{
	private static String[] _voicedCommands =
	{
		"stats"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("stats"))
		{
			final L2PcInstance pc = L2World.getInstance().getPlayer(target);
			if (pc != null)
			{
				final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				
				final TextBuilder replyMSG = new TextBuilder("<html><body>");
				
				replyMSG.append("<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br>");
				replyMSG.append("<br>Statistics for player <font color=\"LEVEL\">" + pc.getName() + "</font><br>");
				replyMSG.append("Total kills <font color=\"FF0000\">" + pc.kills.size() + "</font><br>");
				replyMSG.append("<br>Detailed list: <br>");
				final Iterator<?> it = pc.kills.iterator();
				while (it.hasNext())
				{
					replyMSG.append("<font color=\"FF0000\">" + it.next() + "</font><br>");
				}
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}