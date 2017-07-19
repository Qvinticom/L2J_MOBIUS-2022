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
package com.l2jmobius.gameserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.clientpackets.Say2;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.script.DateRange;

import javolution.text.TextBuilder;
import javolution.util.FastList;

/**
 * This class ...
 * @version $Revision: 1.5.2.1.2.7 $ $Date: 2005/03/29 23:15:14 $
 */
public class Announcements
{
	private static Logger _log = Logger.getLogger(Announcements.class.getName());
	
	private static Announcements _instance;
	private final List<String> _announcements = new FastList<>();
	private final List<List<Object>> eventAnnouncements = new FastList<>();
	
	public Announcements()
	{
		loadAnnouncements();
	}
	
	public static Announcements getInstance()
	{
		if (_instance == null)
		{
			_instance = new Announcements();
		}
		return _instance;
	}
	
	public void loadAnnouncements()
	{
		_announcements.clear();
		final File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.config("data/announcements.txt doesn't exist");
		}
	}
	
	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (int i = 0; i < _announcements.size(); i++)
		{
			final CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, activeChar.getName(), _announcements.get(i).toString());
			activeChar.sendPacket(cs);
		}
		
		for (int i = 0; i < eventAnnouncements.size(); i++)
		{
			final List<Object> entry = eventAnnouncements.get(i);
			final DateRange validDateRange = (DateRange) entry.get(0);
			final String[] msg = (String[]) entry.get(1);
			final Date currentDate = new Date();
			
			if (!validDateRange.isValid() || validDateRange.isWithinRange(currentDate))
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
				for (final String element : msg)
				{
					sm.addString(element);
				}
				
				activeChar.sendPacket(sm);
			}
		}
	}
	
	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
		final List<Object> entry = new FastList<>();
		entry.add(validDateRange);
		entry.add(msg);
		eventAnnouncements.add(entry);
	}
	
	public void listAnnouncements(L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Announcement Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Add or announce a new announcement:</center>");
		replyMSG.append("<center><multiedit var=\"new_announcement\" width=240 height=30></center><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_announcement $new_announcement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Announce\" action=\"bypass -h admin_announce_menu $new_announcement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Reload\" action=\"bypass -h admin_announce_announcements\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			replyMSG.append("<table width=260><tr><td width=220>" + _announcements.get(i).toString() + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		}
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}
	
	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}
	
	private void readFromDisk(File file)
	{
		try (FileReader fr = new FileReader(file);
			LineNumberReader lnr = new LineNumberReader(fr))
		{
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					final String announcement = st.nextToken();
					_announcements.add(announcement);
				}
			}
			
			_log.config("Announcements: Loaded " + _announcements.size() + " Announcements.");
		}
		catch (final IOException e1)
		{
			_log.log(Level.SEVERE, "Error reading announcements", e1);
		}
	}
	
	private void saveToDisk()
	{
		final File file = new File("data/announcements.txt");
		try (FileWriter save = new FileWriter(file))
		{
			for (int i = 0; i < _announcements.size(); i++)
			{
				save.write(_announcements.get(i).toString());
				save.write("\r\n");
			}
			save.flush();
		}
		catch (final IOException e)
		{
			_log.warning("saving the announcements file has failed: " + e);
		}
	}
	
	public void announceToAll(String text)
	{
		final CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			player.sendPacket(cs);
		}
	}
	
	public void announceToAll(SystemMessage sm)
	{
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			player.sendPacket(sm);
		}
	}
	
	// Method fo handling announcements from admin
	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			final String text = command.substring(lengthToTrim);
			Announcements.getInstance().announceToAll(text);
		}
		catch (final StringIndexOutOfBoundsException e)
		{
			// Nobody cares!
			// empty message.. ignore
		}
	}
}