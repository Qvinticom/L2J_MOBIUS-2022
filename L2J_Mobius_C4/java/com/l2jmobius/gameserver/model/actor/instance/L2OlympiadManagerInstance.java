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

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.Olympiad;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Multisell;
import com.l2jmobius.gameserver.network.serverpackets.ExHeroList;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

import javolution.text.TextBuilder;
import javolution.util.FastList;

/**
 * Olympiad Npc's Instance
 * @author godson
 */

public class L2OlympiadManagerInstance extends L2FolkInstance
{
	private static Logger _logOlymp = Logger.getLogger(L2OlympiadManagerInstance.class.getName());
	
	private static final int _gatePass = Config.ALT_OLY_COMP_RITEM;
	
	public L2OlympiadManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("OlympiadDesc"))
		{
			final int val = Integer.parseInt(command.substring(13, 14));
			final String suffix = command.substring(14);
			showChatWindow(player, val, suffix);
		}
		else if (command.startsWith("OlympiadNoble"))
		{
			if (!player.isNoble() || (player.getClassId().getId() < 88))
			{
				return;
			}
			
			final int val = Integer.parseInt(command.substring(14));
			NpcHtmlMessage reply;
			TextBuilder replyMSG;
			
			switch (val)
			{
				case 1:
					Olympiad.getInstance().unRegisterNoble(player);
					break;
				case 2:
					int classed = 0;
					int nonClassed = 0;
					final int[] array = Olympiad.getInstance().getWaitingList();
					
					if (array != null)
					{
						classed = array[0];
						nonClassed = array[1];
						
					}
					
					reply = new NpcHtmlMessage(getObjectId());
					replyMSG = new TextBuilder("<html><body>");
					replyMSG.append("The number of people on the waiting list for " + "Grand Olympiad" + "<center>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>" + "<table width=270 border=0 bgcolor=\"000000\">" + "<tr>" + "<td align=\"left\">Class</td>" + "<td align=\"right\">" + classed + "</td>" + "</tr>" + "<tr>" + "<td align=\"left\">No Class</td>" + "<td align=\"right\">" + nonClassed + "</td>" + "</tr>" + "</table><br>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>" + "<button value=\"Back\" action=\"bypass -h npc_" + getObjectId() + "_Chat 0\" " + "width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
					
					replyMSG.append("</body></html>");
					
					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 3:
					final int points = Olympiad.getInstance().getNoblePoints(player.getObjectId());
					if (points >= 0)
					{
						reply = new NpcHtmlMessage(getObjectId());
						replyMSG = new TextBuilder("<html><body>");
						replyMSG.append("There are " + points + " Grand Olympiad " + "points granted for this event.<br><br>" + "<a action=\"bypass -h npc_" + getObjectId() + "_OlympiadDesc 2a\">Return</a>");
						replyMSG.append("</body></html>");
						
						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 4:
					Olympiad.getInstance().registerNoble(player, false);
					break;
				case 5:
					Olympiad.getInstance().registerNoble(player, true);
					break;
				case 6:
					final int passes = Olympiad.getInstance().getNoblessePasses(player.getObjectId());
					if (passes > 0)
					{
						final L2ItemInstance item = player.getInventory().addItem("Olympiad", _gatePass, passes, player, this);
						
						final InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(item);
						player.sendPacket(iu);
						
						final SystemMessage sm = new SystemMessage(SystemMessage.EARNED_ITEM);
						sm.addNumber(passes);
						sm.addItemName(item.getItemId());
						player.sendPacket(sm);
					}
					else
					{
						player.sendMessage("Not enough points, or not currently in Validation Period.");
						// TODO Send HTML packet "Saying not enough olympiad points.
					}
					break;
				case 7:
					L2Multisell.getInstance().createMultiSell(102, player, false, this);
					break;
				default:
					_logOlymp.warning("Olympiad System: Couldnt send packet for request " + val);
					break;
				
			}
		}
		else if (command.startsWith("Olympiad"))
		{
			final int val = Integer.parseInt(command.substring(9, 10));
			
			final NpcHtmlMessage reply = new NpcHtmlMessage(getObjectId());
			final TextBuilder replyMSG = new TextBuilder("<html><body>");
			
			switch (val)
			{
				case 1:
					final String[] matches = Olympiad.getInstance().getMatchList();
					
					int stad;
					int showbattle;
					replyMSG.append("Grand Olympiad Competition View<br><br>" + "* Caution: Please note, if you watch an Olympiad " + "game, the summoning of your Servitors or Pets will be " + "cancelled. Be careful.<br><br>");
					
					if (matches != null)
					{
						for (final String matche : matches)
						{
							showbattle = Integer.parseInt(matche.substring(1, 2));
							stad = Integer.parseInt(matche.substring(4, 5));
							if (showbattle == 1)
							{
								replyMSG.append("<br><a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 3_" + stad + "\">" + matche + "</a>");
							}
							
						}
					}
					else
					{
						replyMSG.append("<br>There are no matches at the moment.");
					}
					
					replyMSG.append("</body></html>");
					
					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 2:
					// for example >> Olympiad 1_88
					final int classId = Integer.parseInt(command.substring(11));
					if (classId >= 88)
					{
						replyMSG.append("<center>Grand Olympiad Ranking");
						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");
						
						final FastList<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
						if (names.size() != 0)
						{
							replyMSG.append("<table width=270 border=0 bgcolor=\"000000\">");
							
							int index = 1;
							
							for (final String name : names)
							{
								replyMSG.append("<tr>");
								replyMSG.append("<td align=\"left\">" + index + "</td>");
								replyMSG.append("<td align=\"right\">" + name + "</td>");
								replyMSG.append("</tr>");
								index++;
							}
							
							replyMSG.append("</table>");
						}
						
						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
						replyMSG.append("</center>");
						replyMSG.append("</body></html>");
						
						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 3:
					
					final int id = Integer.parseInt(command.substring(11));
					Olympiad.getInstance().addSpectator(id, player, true);
					break;
				case 4:
					player.sendPacket(new ExHeroList());
					break;
				default:
					_logOlymp.warning("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void showChatWindow(L2PcInstance player, int val, String suffix)
	{
		String filename = Olympiad.OLYMPIAD_HTML_FILE;
		
		filename += "noble_desc" + val;
		filename += (suffix != null) ? suffix + ".htm" : ".htm";
		
		if (filename.equals(Olympiad.OLYMPIAD_HTML_FILE + "noble_desc0.htm"))
		{
			filename = Olympiad.OLYMPIAD_HTML_FILE + "noble_main.htm";
		}
		
		showChatWindow(player, filename);
	}
}