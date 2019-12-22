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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.SummonItem;

public class SummonItemsData
{
	private static final Logger LOGGER = Logger.getLogger(SummonItemsData.class.getName());
	
	private final Map<Integer, SummonItem> _summonitems;
	
	public SummonItemsData()
	{
		_summonitems = new HashMap<>();
		
		Scanner s = null;
		
		try
		{
			s = new Scanner(new File(Config.DATAPACK_ROOT + "/data/csv/summon_items.csv"));
			
			int lineCount = 0;
			
			while (s.hasNextLine())
			{
				lineCount++;
				
				final String line = s.nextLine();
				
				if (line.startsWith("#"))
				{
					continue;
				}
				else if (line.equals(""))
				{
					continue;
				}
				
				final String[] lineSplit = line.split(";");
				
				boolean ok = true;
				int itemID = 0;
				int npcID = 0;
				byte summonType = 0;
				
				try
				{
					itemID = Integer.parseInt(lineSplit[0]);
					npcID = Integer.parseInt(lineSplit[1]);
					summonType = Byte.parseByte(lineSplit[2]);
				}
				catch (Exception e)
				{
					LOGGER.info("Summon items data: Error in line " + lineCount + " -> incomplete/invalid data or wrong seperator!");
					LOGGER.info("		" + line);
					ok = false;
				}
				
				if (!ok)
				{
					continue;
				}
				
				final SummonItem summonitem = new SummonItem(itemID, npcID, summonType);
				_summonitems.put(itemID, summonitem);
			}
		}
		catch (Exception e)
		{
			LOGGER.info("Summon items data: Can not find './data/csv/summon_items.csv'");
		}
		finally
		{
			if (s != null)
			{
				s.close();
			}
		}
		
		LOGGER.info("Summon items data: Loaded " + _summonitems.size() + " summon items.");
	}
	
	public SummonItem getSummonItem(int itemId)
	{
		return _summonitems.get(itemId);
	}
	
	public int[] itemIDs()
	{
		final int size = _summonitems.size();
		final int[] result = new int[size];
		int i = 0;
		
		for (SummonItem si : _summonitems.values())
		{
			result[i] = si.getItemId();
			i++;
		}
		return result;
	}
	
	public static SummonItemsData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonItemsData INSTANCE = new SummonItemsData();
	}
}
