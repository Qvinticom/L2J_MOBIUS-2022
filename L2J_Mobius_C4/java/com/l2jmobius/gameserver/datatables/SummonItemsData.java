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

import java.io.File;
import java.util.Scanner;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2SummonItem;

import javolution.util.FastMap;

/**
 * @author FBIagent
 */
public class SummonItemsData
{
	private final FastMap<Integer, L2SummonItem> _summonitems;
	
	private static SummonItemsData _instance;
	
	public static SummonItemsData getInstance()
	{
		if (_instance == null)
		{
			_instance = new SummonItemsData();
		}
		
		return _instance;
	}
	
	public SummonItemsData()
	{
		_summonitems = new FastMap<>();
		
		try (Scanner s = new Scanner(new File(Config.DATAPACK_ROOT + "/data/summon_items.csv")))
		{
			int lineCount = 0;
			
			while (s.hasNextLine())
			{
				lineCount++;
				
				final String line = s.nextLine();
				
				if (line.startsWith("#"))
				{
					continue;
				}
				else if (line.isEmpty())
				{
					continue;
				}
				
				final String[] lineSplit = line.split(";");
				boolean ok = true;
				int itemID = 0, npcID = 0;
				byte summonType = 0;
				
				try
				{
					itemID = Integer.parseInt(lineSplit[0]);
					npcID = Integer.parseInt(lineSplit[1]);
					summonType = Byte.parseByte(lineSplit[2]);
				}
				catch (final Exception e)
				{
					System.out.println("Summon items data: Error in line " + lineCount + " -> incomplete/invalid data or wrong seperator!");
					System.out.println("		" + line);
					ok = false;
				}
				
				if (!ok)
				{
					continue;
				}
				
				final L2SummonItem summonitem = new L2SummonItem(itemID, npcID, summonType);
				_summonitems.put(itemID, summonitem);
			}
			System.out.println("Summon items data: Loaded " + _summonitems.size() + " summon items.");
		}
		catch (final Exception e)
		{
			System.out.println("Summon items data: Cannot find '" + Config.DATAPACK_ROOT + "/data/summon_items.csv'");
		}
	}
	
	public L2SummonItem getSummonItem(int itemId)
	{
		return _summonitems.get(itemId);
	}
	
	public int[] itemIDs()
	{
		final int size = _summonitems.size();
		final int[] result = new int[size];
		int i = 0;
		for (final L2SummonItem si : _summonitems.values())
		{
			result[i] = si.getItemId();
			i++;
		}
		return result;
	}
}