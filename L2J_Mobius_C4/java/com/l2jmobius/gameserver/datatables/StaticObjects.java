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
package com.l2jmobius.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.model.actor.instance.L2StaticObjectInstance;

import javolution.util.FastMap;

public class StaticObjects
{
	private static Logger _log = Logger.getLogger(StaticObjects.class.getName());
	
	private static StaticObjects _instance;
	private final Map<Integer, L2StaticObjectInstance> _staticObjects;
	
	public static StaticObjects getInstance()
	{
		if (_instance == null)
		{
			_instance = new StaticObjects();
		}
		return _instance;
	}
	
	public StaticObjects()
	{
		_staticObjects = new FastMap<>();
		parseData();
		_log.config("StaticObject: Loaded " + _staticObjects.size() + " StaticObject Templates.");
	}
	
	private void parseData()
	{
		final File objectData = new File(Config.DATAPACK_ROOT, "data/staticobjects.csv");
		try (FileReader fr = new FileReader(objectData);
			BufferedReader br = new BufferedReader(fr);
			LineNumberReader lnr = new LineNumberReader(br))
		{
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				
				final L2StaticObjectInstance obj = parse(line);
				_staticObjects.put(obj.getStaticObjectId(), obj);
			}
		}
		catch (final FileNotFoundException e)
		{
			_log.warning("staticobjects.csv is missing in data folder");
		}
		catch (final Exception e)
		{
			_log.warning("error while creating StaticObjects table " + e);
		}
	}
	
	public static L2StaticObjectInstance parse(String line)
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		
		st.nextToken(); // Pass over static object name (not used in server)
		
		final int id = Integer.parseInt(st.nextToken());
		final int x = Integer.parseInt(st.nextToken());
		final int y = Integer.parseInt(st.nextToken());
		final int z = Integer.parseInt(st.nextToken());
		final int type = Integer.parseInt(st.nextToken());
		final String texture = st.nextToken();
		final int map_x = Integer.parseInt(st.nextToken());
		final int map_y = Integer.parseInt(st.nextToken());
		
		final L2StaticObjectInstance obj = new L2StaticObjectInstance(IdFactory.getInstance().getNextId());
		obj.setType(type);
		obj.setStaticObjectId(id);
		obj.setXYZ(x, y, z);
		obj.setMap(texture, map_x, map_y);
		obj.spawnMe();
		
		return obj;
	}
}