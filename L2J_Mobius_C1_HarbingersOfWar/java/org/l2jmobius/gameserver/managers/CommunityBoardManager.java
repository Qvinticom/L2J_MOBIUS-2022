/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.managers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.GameTimeController;
import org.l2jmobius.gameserver.data.CharTemplateTable;
import org.l2jmobius.gameserver.data.ExperienceTable;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.ClanMember;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;

public class CommunityBoardManager
{
	private static CommunityBoardManager _instance;
	
	public static CommunityBoardManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new CommunityBoardManager();
		}
		return _instance;
	}
	
	public void handleCommands(ClientThread client, String command)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if (command.startsWith("bbs_"))
		{
			final StringBuilder htmlCode = new StringBuilder("<html imgsrc=\"sek.cbui353\"><body><br><table border=0><tr><td FIXWIDTH=15></td><td align=center>Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
			if (command.equals("bbs_default"))
			{
				final Collection<PlayerInstance> players = World.getInstance().getAllPlayers();
				htmlCode.append("<table border=0>");
				final int t = GameTimeController.getInstance().getGameTime();
				final int h = t / 60;
				final int m = t % 60;
				final SimpleDateFormat format = new SimpleDateFormat("h:mm a");
				final Calendar cal = Calendar.getInstance();
				cal.set(11, h);
				cal.set(12, m);
				htmlCode.append("<tr><td>Game Time: " + format.format(cal.getTime()) + "</td></tr>");
				htmlCode.append("<tr><td>XP Rate: " + Config.RATE_XP + "</td></tr>");
				htmlCode.append("<tr><td>SP Rate: " + Config.RATE_SP + "</td></tr>");
				htmlCode.append("<tr><td>Adena Rate: " + Config.RATE_ADENA + "</td></tr>");
				htmlCode.append("<tr><td>Drop Rate: " + Config.RATE_DROP + "</td></tr>");
				htmlCode.append("<tr><td><img src=\"sek.cbui355\" width=610 height=1><br></td></tr>");
				htmlCode.append("<tr><td>" + players.size() + " Player(s) Online:</td></tr><tr><td><table border=0><tr>");
				int n = 1;
				for (PlayerInstance player : players)
				{
					htmlCode.append("<td><a action=\"bypass bbs_player_info " + player.getName() + "\">" + player.getName() + "</a></td><td FIXWIDTH=15></td>");
					if (n == 5)
					{
						htmlCode.append("</tr><tr>");
						n = 0;
					}
					++n;
				}
				htmlCode.append("</tr></table></td></tr></table>");
			}
			else if (command.equals("bbs_top"))
			{
				htmlCode.append("<center>" + command + "</center>");
			}
			else if (command.equals("bbs_up"))
			{
				htmlCode.append("<center>" + command + "</center>");
			}
			else if (command.equals("bbs_favorate"))
			{
				htmlCode.append("<center>" + command + "</center>");
			}
			else if (command.equals("bbs_add_fav"))
			{
				htmlCode.append("<center>" + command + "</center>");
			}
			else if (command.equals("bbs_region"))
			{
				htmlCode.append("<center>" + command + "</center>");
			}
			else if (command.equals("bbs_clan"))
			{
				final Clan clan = activeChar.getClan();
				htmlCode.append("<table border=0><tr><td>" + clan.getName() + " (Level " + clan.getLevel() + "):</td></tr><tr><td><table border=0>");
				String title = "";
				if (!clan.getClanMember(clan.getLeaderName()).getTitle().equals(""))
				{
					title = "<td>[" + clan.getClanMember(clan.getLeaderName()).getTitle() + "]</td><td FIXWIDTH=5></td>";
				}
				String name = clan.getLeaderName();
				if (clan.getClanMember(clan.getLeaderName()).isOnline())
				{
					name = "<a action=\"bypass bbs_player_info " + clan.getLeaderName() + "\">" + clan.getLeaderName() + "</a>";
				}
				htmlCode.append("<tr>" + title + "<td>" + name + "</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
				for (ClanMember member : clan.getMembers())
				{
					if (member.getName().equals(clan.getLeaderName()))
					{
						continue;
					}
					title = "";
					if (!member.getTitle().equals(""))
					{
						title = "<td>[" + member.getTitle() + "]</td><td FIXWIDTH=5></td>";
					}
					name = member.getName();
					if (member.isOnline())
					{
						name = "<a action=\"bypass bbs_player_info " + member.getName() + "\">" + member.getName() + "</a>";
					}
					htmlCode.append("<tr>" + title + "<td>" + name + "</td></tr>");
				}
				htmlCode.append("</table></td></tr></table>");
			}
			else if (command.startsWith("bbs_player_info"))
			{
				final String name = command.substring(16);
				final PlayerInstance player = World.getInstance().getPlayer(name);
				String sex = "Male";
				if (player.getSex() == 1)
				{
					sex = "Female";
				}
				htmlCode.append("<table border=0><tr><td>" + player.getName() + " (" + sex + " " + CharTemplateTable.getInstance().getTemplate(player.getClassId()).getClassName() + "):</td></tr>");
				htmlCode.append("<tr><td>Level: " + player.getLevel() + "</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
				int nextLevelExp = 0;
				int nextLevelExpNeeded = 0;
				if (player.getLevel() < 60)
				{
					nextLevelExp = ExperienceTable.getInstance().getExp(player.getLevel() + 1);
					nextLevelExpNeeded = ExperienceTable.getInstance().getExp(player.getLevel() + 1) - player.getExp();
				}
				htmlCode.append("<tr><td>Experience: " + player.getExp() + "/" + nextLevelExp + "</td></tr>");
				htmlCode.append("<tr><td>Experience needed for level up: " + nextLevelExpNeeded + "</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
				final int uptime = (int) player.getUptime() / 1000;
				final int h = uptime / 3600;
				final int m = (uptime - (h * 3600)) / 60;
				final int s = uptime - (h * 3600) - (m * 60);
				htmlCode.append("<tr><td>Uptime: " + h + "h " + m + "m " + s + "s</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
				if (player.getClan() != null)
				{
					htmlCode.append("<tr><td>Clan: " + player.getClan().getName() + "</td></tr>");
					htmlCode.append("<tr><td><br></td></tr>");
				}
				htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"bypass bbs_player_pm " + player.getName() + " $pm\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass bbs_default\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
			}
			else if (command.startsWith("bbs_player_pm"))
			{
				try
				{
					final String val = command.substring(14);
					final StringTokenizer st = new StringTokenizer(val);
					final String name = st.nextToken();
					final String message = val.substring(name.length() + 1);
					final PlayerInstance reciever = World.getInstance().getPlayer(name);
					final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), 2, activeChar.getName(), message);
					reciever.sendPacket(cs);
					activeChar.sendPacket(cs);
					htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass bbs_player_info " + reciever.getName() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
				}
				catch (StringIndexOutOfBoundsException e)
				{
					// empty catch block
				}
			}
			htmlCode.append("</td></tr></table></body></html>");
			final ShowBoard sb = new ShowBoard(activeChar, htmlCode.toString());
			activeChar.sendPacket(sb);
		}
	}
}
