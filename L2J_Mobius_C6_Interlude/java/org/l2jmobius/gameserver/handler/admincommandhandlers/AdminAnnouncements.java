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

import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.announce.Announcement;
import org.l2jmobius.gameserver.model.announce.AnnouncementType;
import org.l2jmobius.gameserver.model.announce.AutoAnnouncement;
import org.l2jmobius.gameserver.model.announce.IAnnouncement;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - announce text = announces text to all players - list_announcements = show menu - reload_announcements = reloads announcements from txt file - announce_announcements = announce all stored announcements to all players - add_announcement text = adds
 * text to startup announcements - del_announcement id = deletes announcement with respective id
 * @version $Revision: 1.4.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminAnnouncements implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_list_announcements",
		"admin_reload_announcements",
		"admin_announce_announcements",
		"admin_add_announcement",
		"admin_del_announcement",
		"admin_announce",
		"admin_critannounce",
		"admin_announce_menu",
		"admin_list_autoannouncements",
		"admin_add_autoannouncement",
		"admin_del_autoannouncement",
		"admin_autoannounce"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final String comm = st.nextToken();
		if (comm == null)
		{
			return false;
		}
		
		String text = "";
		int index = 0;
		
		switch (comm)
		{
			case "admin_list_announcements":
			{
				listAnnouncements(activeChar);
				return true;
			}
			case "admin_reload_announcements":
			{
				AnnouncementsTable.getInstance().load();
				listAnnouncements(activeChar);
				return true;
			}
			case "admin_announce_menu":
			{
				if (st.hasMoreTokens())
				{
					text = command.replace(comm + " ", "");
					// text = st.nextToken();
				}
				if (!text.equals(""))
				{
					AnnouncementsTable.getInstance().announceToAll(text);
				}
				listAnnouncements(activeChar);
				return true;
			}
			case "admin_announce_announcements":
			{
				for (Player player : World.getInstance().getAllPlayers())
				{
					AnnouncementsTable.getInstance().showAnnouncements(player);
				}
				listAnnouncements(activeChar);
				return true;
			}
			case "admin_add_announcement":
			{
				if (st.hasMoreTokens())
				{
					text = command.replace(comm + " ", "");
				}
				if (!text.equals(""))
				{
					AnnouncementsTable.getInstance().addAnnouncement(new Announcement(AnnouncementType.NORMAL, text, activeChar.getName()));
					listAnnouncements(activeChar);
					return true;
				}
				BuilderUtil.sendSysMessage(activeChar, "You cannot announce Empty message");
				return false;
			}
			case "admin_del_announcement":
			{
				if (st.hasMoreTokens())
				{
					final String index_s = st.nextToken();
					try
					{
						index = Integer.parseInt(index_s);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //del_announcement <index> (number >=0)");
					}
				}
				if (index >= 0)
				{
					AnnouncementsTable.getInstance().deleteAnnouncement(index);
					listAnnouncements(activeChar);
					return true;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //del_announcement <index> (number >=0)");
				return false;
			}
			case "admin_announce":
			{
				AnnouncementsTable.getInstance().announceToAll((Config.GM_ANNOUNCER_NAME ? command + " [ " + activeChar.getName() + " ]" : command).substring(15));
				return true;
			}
			case "admin_critannounce":
			{
				String text1 = command.substring(19);
				if (Config.GM_CRITANNOUNCER_NAME && (text1.length() > 0))
				{
					text1 = activeChar.getName() + ": " + text1;
				}
				Broadcast.toAllOnlinePlayers(new CreatureSay(activeChar.getObjectId(), ChatType.CRITICAL_ANNOUNCE, "", text1));
				return true;
			}
			case "admin_list_autoannouncements":
			{
				listAutoAnnouncements(activeChar);
				return true;
			}
			case "admin_add_autoannouncement":
			{
				if (st.hasMoreTokens())
				{
					int delay = 0;
					try
					{
						delay = Integer.parseInt(st.nextToken().trim());
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
						return false;
					}
					if (st.hasMoreTokens())
					{
						text = st.nextToken();
						if (delay >= 30)
						{
							while (st.hasMoreTokens())
							{
								text = text + " " + st.nextToken();
							}
							AnnouncementsTable.getInstance().addAnnouncement(new AutoAnnouncement(AnnouncementType.AUTO_NORMAL, text, activeChar.getName(), 0, delay, -1));
							listAutoAnnouncements(activeChar);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
						return false;
					}
					BuilderUtil.sendSysMessage(activeChar, "Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
					return false;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
				return false;
			}
			case "admin_del_autoannouncement":
			{
				if (st.hasMoreTokens())
				{
					try
					{
						index = Integer.parseInt(st.nextToken());
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //del_autoannouncement <index> (number >= 0)");
						return false;
					}
					if (index >= 0)
					{
						AnnouncementsTable.getInstance().deleteAnnouncement(index);
						listAutoAnnouncements(activeChar);
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //del_autoannouncement <index> (number >= 0)");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //del_autoannouncement <index> (number >= 0)");
					return false;
				}
				return false;
			}
			case "admin_autoannounce":
			{
				listAutoAnnouncements(activeChar);
				return true;
			}
		}
		
		return false;
	}
	
	private void listAnnouncements(Player player)
	{
		final String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce.htm");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		final StringBuilder replyMSG = new StringBuilder("<br>");
		for (Entry<Integer, IAnnouncement> entry : AnnouncementsTable.getInstance().getAllAnnouncements().entrySet())
		{
			final IAnnouncement announcement = entry.getValue();
			if ((announcement.getType() == AnnouncementType.CRITICAL) || (announcement.getType() == AnnouncementType.NORMAL))
			{
				replyMSG.append("<table width=260><tr><td width=220>[" + entry.getKey() + "] " + announcement.getContent() + "</td><td width=40>");
				replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + entry.getKey() + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
			}
		}
		adminReply.replace("%announces%", replyMSG.toString());
		player.sendPacket(adminReply);
	}
	
	private void listAutoAnnouncements(Player player)
	{
		final String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce_auto.htm");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		final StringBuilder replyMSG = new StringBuilder("<br>");
		for (Entry<Integer, IAnnouncement> entry : AnnouncementsTable.getInstance().getAllAnnouncements().entrySet())
		{
			final IAnnouncement announcement = entry.getValue();
			if ((announcement.getType() == AnnouncementType.AUTO_CRITICAL) || (announcement.getType() == AnnouncementType.AUTO_NORMAL))
			{
				replyMSG.append("<table width=260><tr><td width=220>[" + entry.getKey() + " (" + ((AutoAnnouncement) announcement).getDelay() + "s)] " + announcement.getContent() + "</td><td width=40>");
				replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + entry.getKey() + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
			}
		}
		adminReply.replace("%announces%", replyMSG.toString());
		player.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}