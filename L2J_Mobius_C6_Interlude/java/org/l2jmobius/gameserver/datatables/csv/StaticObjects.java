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
package org.l2jmobius.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.idfactory.IdFactory;
import org.l2jmobius.gameserver.model.actor.instance.StaticObjectInstance;

public class StaticObjects
{
	private static final Logger LOGGER = Logger.getLogger(StaticObjects.class.getName());
	
	private final Map<Integer, StaticObjectInstance> _staticObjects;
	
	public StaticObjects()
	{
		_staticObjects = new HashMap<>();
		parseData();
		LOGGER.info("StaticObject: Loaded " + _staticObjects.size() + " StaticObject Templates.");
	}
	
	private void parseData()
	{
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			final File doorData = new File(Config.DATAPACK_ROOT, "data/csv/staticobjects.csv");
			
			reader = new FileReader(doorData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);
			
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				
				StaticObjectInstance obj = parse(line);
				_staticObjects.put(obj.getStaticObjectId(), obj);
			}
		}
		catch (FileNotFoundException e)
		{
			LOGGER.warning("staticobjects.csv is missing in data csv folder");
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while creating StaticObjects table " + e);
		}
		finally
		{
			if (lnr != null)
			{
				try
				{
					lnr.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with StaticObjects: " + e1.getMessage());
				}
			}
			
			if (buff != null)
			{
				try
				{
					buff.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with StaticObjects: " + e1.getMessage());
				}
			}
			
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with StaticObjects: " + e1.getMessage());
				}
			}
		}
	}
	
	public static StaticObjectInstance parse(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		
		st.nextToken(); // Pass over static object name (not used in server)
		
		final int id = Integer.parseInt(st.nextToken());
		final int x = Integer.parseInt(st.nextToken());
		final int y = Integer.parseInt(st.nextToken());
		final int z = Integer.parseInt(st.nextToken());
		final int type = Integer.parseInt(st.nextToken());
		final String texture = st.nextToken();
		final int map_x = Integer.parseInt(st.nextToken());
		final int map_y = Integer.parseInt(st.nextToken());
		
		final StaticObjectInstance obj = new StaticObjectInstance(IdFactory.getInstance().getNextId());
		obj.setType(type);
		obj.setStaticObjectId(id);
		obj.setXYZ(x, y, z);
		obj.setMap(texture, map_x, map_y);
		obj.spawnMe();
		
		return obj;
	}
	
	public static StaticObjects getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final StaticObjects INSTANCE = new StaticObjects();
	}
}
