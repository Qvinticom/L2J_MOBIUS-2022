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
import com.l2jmobius.gameserver.model.L2ExtractableItem;
import com.l2jmobius.gameserver.model.L2ExtractableProductItem;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * @author FBIagent
 */
public class ExtractableItemsData
{
	private final FastMap<Integer, L2ExtractableItem> _items;
	
	private static ExtractableItemsData _instance = null;
	
	public static ExtractableItemsData getInstance()
	{
		if (_instance == null)
		{
			_instance = new ExtractableItemsData();
		}
		
		return _instance;
	}
	
	public ExtractableItemsData()
	{
		_items = new FastMap<>();
		
		try (Scanner s = new Scanner(new File(Config.DATAPACK_ROOT + "/data/extractable_items.csv")))
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
				
				if (line.isEmpty())
				{
					continue;
				}
				
				final String[] lineSplit = line.split(";");
				boolean ok = true;
				int itemID = 0;
				
				try
				{
					itemID = Integer.parseInt(lineSplit[0]);
				}
				catch (final Exception e)
				{
					System.out.println("Extractable items data: Error in line " + lineCount + " -> invalid item id or wrong seperator after item id!");
					System.out.println("        " + line);
					ok = false;
				}
				
				if (!ok)
				{
					continue;
				}
				
				final FastList<L2ExtractableProductItem> product_temp = new FastList<>();
				
				for (int i = 0; i < (lineSplit.length - 1); i++)
				{
					ok = true;
					
					final String[] lineSplit2 = lineSplit[i + 1].split(",");
					
					if (lineSplit2.length != 3)
					{
						System.out.println("Extractable items data: Error in line " + lineCount + " -> wrong seperator!");
						System.out.println("            " + line);
						ok = false;
					}
					
					if (!ok)
					{
						continue;
					}
					
					int production = 0, amount = 0, chance = 0;
					
					try
					{
						production = Integer.parseInt(lineSplit2[0]);
						amount = Integer.parseInt(lineSplit2[1]);
						chance = Integer.parseInt(lineSplit2[2]);
					}
					catch (final Exception e)
					{
						System.out.println("Extractable items data: Error in line " + lineCount + " -> incomplete/invalid production data or wrong seperator!");
						System.out.println("        " + line);
						ok = false;
					}
					
					if (!ok)
					{
						continue;
					}
					
					final L2ExtractableProductItem product = new L2ExtractableProductItem(production, amount, chance);
					product_temp.add(product);
				}
				
				int fullChances = 0;
				
				for (final L2ExtractableProductItem Pi : product_temp)
				{
					fullChances += Pi.getChance();
				}
				
				if (fullChances > 100)
				{
					System.out.println("Extractable items data: Error in line " + lineCount + " -> all chances together are more then 100!");
					System.out.println("        " + line);
					continue;
				}
				
				final L2ExtractableItem product = new L2ExtractableItem(itemID, product_temp);
				_items.put(itemID, product);
			}
			
			System.out.println("Extractable items data: Loaded " + _items.size() + " extractable items!");
		}
		catch (final Exception e)
		{
			System.out.println("Extractable items data: Cannot find '" + Config.DATAPACK_ROOT + "/data/extractable_items.csv'");
		}
	}
	
	public L2ExtractableItem getExtractableItem(int itemID)
	{
		return _items.get(itemID);
	}
	
	public int[] itemIDs()
	{
		final int size = _items.size();
		final int[] result = new int[size];
		int i = 0;
		for (final L2ExtractableItem ei : _items.values())
		{
			result[i] = ei.getItemId();
			i++;
		}
		return result;
	}
}