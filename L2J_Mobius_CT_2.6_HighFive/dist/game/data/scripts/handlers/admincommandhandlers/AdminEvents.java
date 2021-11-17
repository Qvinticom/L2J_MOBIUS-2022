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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Event;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BuilderUtil;

public class AdminEvents implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_event_menu",
		"admin_event_start",
		"admin_event_stop",
		"admin_event_start_menu",
		"admin_event_stop_menu",
		"admin_event_bypass"
	};
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		String eventName = "";
		String eventBypass = "";
		final StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		if (st.hasMoreTokens())
		{
			eventName = st.nextToken();
		}
		if (st.hasMoreTokens())
		{
			eventBypass = st.nextToken();
		}
		
		if (command.contains("_menu"))
		{
			showMenu(activeChar);
		}
		
		if (command.startsWith("admin_event_start"))
		{
			try
			{
				if (eventName != null)
				{
					final Event event = (Event) QuestManager.getInstance().getQuest(eventName);
					if (event != null)
					{
						if (event.eventStart(activeChar))
						{
							BuilderUtil.sendSysMessage(activeChar, "Event " + eventName + " started.");
							return true;
						}
						
						BuilderUtil.sendSysMessage(activeChar, "There is problem starting " + eventName + " event.");
						return true;
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //event_start <eventname>");
				return false;
			}
		}
		else if (command.startsWith("admin_event_stop"))
		{
			try
			{
				if (eventName != null)
				{
					final Event event = (Event) QuestManager.getInstance().getQuest(eventName);
					if (event != null)
					{
						if (event.eventStop())
						{
							BuilderUtil.sendSysMessage(activeChar, "Event " + eventName + " stopped.");
							return true;
						}
						
						BuilderUtil.sendSysMessage(activeChar, "There is problem with stoping " + eventName + " event.");
						return true;
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //event_start <eventname>");
				return false;
			}
		}
		else if (command.startsWith("admin_event_bypass"))
		{
			try
			{
				if (eventName != null)
				{
					final Event event = (Event) QuestManager.getInstance().getQuest(eventName);
					if (event != null)
					{
						event.eventBypass(activeChar, eventBypass);
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //event_bypass <eventname> <bypass>");
				return false;
			}
		}
		return false;
	}
	
	private void showMenu(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar, "data/html/admin/gm_events.htm");
		final StringBuilder cList = new StringBuilder(500);
		for (Quest event : QuestManager.getInstance().getScripts().values())
		{
			if (event instanceof Event)
			{
				cList.append("<tr><td><font color=\"LEVEL\">" + event.getName() + ":</font></td><br><td><button value=\"Start\" action=\"bypass -h admin_event_start_menu " + event.getName() + "\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td><button value=\"Stop\" action=\"bypass -h admin_event_stop_menu " + event.getName() + "\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			}
		}
		html.replace("%LIST%", cList.toString());
		activeChar.sendPacket(html);
	}
}