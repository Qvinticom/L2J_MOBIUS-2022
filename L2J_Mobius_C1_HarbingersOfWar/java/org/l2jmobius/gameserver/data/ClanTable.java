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
package org.l2jmobius.gameserver.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.IdManager;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.ClanMember;
import org.l2jmobius.gameserver.model.actor.Player;

public class ClanTable
{
	private static Logger _log = Logger.getLogger(ClanTable.class.getName());
	private static ClanTable _instance;
	private final Map<Integer, Clan> _clans = new HashMap<>();
	
	public static ClanTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanTable();
		}
		return _instance;
	}
	
	private ClanTable()
	{
		try
		{
			final File clanFolder = new File("data/clans");
			clanFolder.mkdirs();
			final File[] clans = clanFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith(".csv"));
			for (File clan2 : clans)
			{
				final Clan clan = restoreClan(clan2);
				_clans.put(clan.getClanId(), clan);
			}
			_log.config("Restored " + _clans.size() + " clans.");
		}
		catch (Exception e)
		{
			_log.warning("Error while creating clan table " + e);
		}
	}
	
	private Clan restoreClan(File file)
	{
		Clan clan = null;
		try
		{
			final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
			lnr.readLine();
			clan = parseClanData(lnr.readLine());
			lnr.readLine();
			String line = null;
			boolean first = true;
			while ((line = lnr.readLine()) != null)
			{
				final ClanMember member = parseMembers(line);
				if (first)
				{
					clan.setLeader(member);
					first = false;
					continue;
				}
				clan.addClanMember(member);
			}
			lnr.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not read clan file:" + e.getMessage());
		}
		return clan;
	}
	
	private Clan parseClanData(String line)
	{
		final Clan clan = new Clan();
		final StringTokenizer st = new StringTokenizer(line, ";");
		clan.setClanId(Integer.parseInt(st.nextToken()));
		clan.setName(st.nextToken());
		clan.setLevel(Integer.parseInt(st.nextToken()));
		clan.setHasCastle(Integer.parseInt(st.nextToken()));
		clan.setHasHideout(Integer.parseInt(st.nextToken()));
		clan.setAllyId(Integer.parseInt(st.nextToken()));
		clan.setAllyName(st.nextToken());
		return clan;
	}
	
	private ClanMember parseMembers(String line)
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		final String name = st.nextToken();
		final int level = Integer.parseInt(st.nextToken());
		final int classId = Integer.parseInt(st.nextToken());
		final int objectId = Integer.parseInt(st.nextToken());
		return new ClanMember(name, level, classId, objectId);
	}
	
	public Clan getClan(int clanId)
	{
		return _clans.get(clanId);
	}
	
	public Clan createClan(Player player, String clanName)
	{
		for (Clan oldClans : _clans.values())
		{
			if (!oldClans.getName().equalsIgnoreCase(clanName))
			{
				continue;
			}
			return null;
		}
		final Clan clan = new Clan();
		clan.setClanId(IdManager.getInstance().getNextId());
		clan.setName(clanName);
		clan.setLevel(0);
		clan.setHasCastle(0);
		clan.setHasHideout(0);
		clan.setAllyId(0);
		clan.setAllyName(" ");
		final ClanMember leader = new ClanMember(player.getName(), player.getLevel(), player.getClassId(), player.getObjectId());
		clan.setLeader(leader);
		_clans.put(clan.getClanId(), clan);
		clan.store();
		return clan;
	}
}
