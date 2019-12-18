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
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.templates.Npc;

public class SpawnTable
{
	private static Logger _log = Logger.getLogger(SpawnTable.class.getName());
	private static SpawnTable _instance;
	private final Map<Integer, Spawn> _spawntable = new HashMap<>();
	private int _highestId;
	
	public static SpawnTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new SpawnTable();
		}
		return _instance;
	}
	
	private SpawnTable()
	{
		try
		{
			final File spawnDataFile = new File("data/spawnlist.csv");
			final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(spawnDataFile)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				try
				{
					if (line.trim().isEmpty() || line.startsWith("#"))
					{
						continue;
					}
					final Spawn spawn = parseList(line);
					_spawntable.put(spawn.getId(), spawn);
					if (spawn.getId() <= _highestId)
					{
						continue;
					}
					_highestId = spawn.getId();
				}
				catch (Exception e1)
				{
					_log.warning("Spawn could not be initialized: " + e1);
				}
			}
			lnr.close();
			_log.config("Created " + _spawntable.size() + " spawn handlers.");
		}
		catch (FileNotFoundException e)
		{
			_log.warning("spawnlist.csv is missing in data folder");
		}
		catch (Exception e)
		{
			_log.warning("error while creating spawn list " + e);
		}
	}
	
	private Spawn parseList(String line) throws ClassNotFoundException
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		final int spawnId = Integer.parseInt(st.nextToken());
		final String location = st.nextToken();
		final int count = Integer.parseInt(st.nextToken());
		final int npcId = Integer.parseInt(st.nextToken());
		final Npc template1 = NpcTable.getInstance().getTemplate(npcId);
		if (template1 == null)
		{
			_log.warning("Monster data for id:" + npcId + " missing in npc.csv");
			return null;
		}
		final Spawn spawnDat = new Spawn(template1);
		spawnDat.setId(spawnId);
		spawnDat.setLocation(location); // ?
		spawnDat.setAmount(count);
		spawnDat.setLocx(Integer.parseInt(st.nextToken()));
		spawnDat.setLocy(Integer.parseInt(st.nextToken()));
		spawnDat.setLocz(Integer.parseInt(st.nextToken()));
		spawnDat.setRandomx(Integer.parseInt(st.nextToken()));
		spawnDat.setRandomy(Integer.parseInt(st.nextToken()));
		spawnDat.setHeading(Integer.parseInt(st.nextToken()));
		spawnDat.setRespawnDelay(Integer.parseInt(st.nextToken()));
		spawnDat.init();
		return spawnDat;
	}
	
	public Spawn getTemplate(int id)
	{
		return _spawntable.get(id);
	}
	
	public void addNewSpawn(Spawn spawn)
	{
		++_highestId;
		spawn.setId(_highestId);
		_spawntable.put(spawn.getId(), spawn);
	}
}
