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
package org.l2jmobius.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class Announcements
{
	private static Logger _log = Logger.getLogger(Announcements.class.getName());
	private static Announcements _instance;
	private final List<String> _announcements = new ArrayList<>();
	
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
		final File file = new File("data/announcements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.config("data/announcements.txt does not exist.");
		}
	}
	
	public void showAnnouncements(PlayerInstance activeChar)
	{
		for (int i = 0; i < _announcements.size(); ++i)
		{
			final CreatureSay cs = new CreatureSay(0, 10, activeChar.getName(), _announcements.get(i));
			activeChar.sendPacket(cs);
		}
	}
	
	public void listAnnouncements(PlayerInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Announcements:</title>");
		replyMSG.append("<body>");
		for (int i = 0; i < _announcements.size(); ++i)
		{
			replyMSG.append(_announcements.get(i));
			replyMSG.append("<center><button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		}
		replyMSG.append("<br>");
		replyMSG.append("Add a new announcement:");
		replyMSG.append("<center><multiedit var=\"add_announcement\" width=240 height=40></center>");
		replyMSG.append("<center><button value=\"Add\" action=\"bypass -h admin_add_announcement $add_announcement\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Reload\" action=\"bypass -h admin_reload_announcements\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Announce\" action=\"bypass -h admin_announce_announcements\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br><br>");
		replyMSG.append("<right><button value=\"Back\" action=\"bypass -h admin_show\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></right>");
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
		int i = 0;
		String line = null;
		try
		{
			final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
			while ((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (!st.hasMoreTokens())
				{
					continue;
				}
				final String announcement = st.nextToken();
				_announcements.add(announcement);
				++i;
			}
			_log.config("Loaded " + i + " announcements.");
			lnr.close();
		}
		catch (FileNotFoundException e)
		{
			_log.warning("File announcements.txt does not exist.");
		}
		catch (Exception e)
		{
			_log.warning("There was a problem loading annoucements.");
		}
	}
	
	private void saveToDisk()
	{
		try
		{
			final FileWriter save = new FileWriter(new File("data/announcements.txt"));
			for (int i = 0; i < _announcements.size(); ++i)
			{
				save.write(_announcements.get(i));
				save.write("\r\n");
			}
			save.close();
		}
		catch (IOException e)
		{
			_log.warning("Saving the announcements file has failed: " + e);
		}
	}
}
