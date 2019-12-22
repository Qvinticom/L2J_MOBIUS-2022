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
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.StatsSet;
import org.l2jmobius.gameserver.model.items.Henna;

public class HennaTable
{
	private static final Logger LOGGER = Logger.getLogger(HennaTable.class.getName());
	
	private final Map<Integer, Henna> _henna;
	private final boolean _initialized = true;
	
	private HennaTable()
	{
		_henna = new HashMap<>();
		restoreHennaData();
	}
	
	private void restoreHennaData()
	{
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			final File fileData = new File(Config.DATAPACK_ROOT + "/data/csv/henna.csv");
			
			reader = new FileReader(fileData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);
			
			String line = null;
			
			while ((line = lnr.readLine()) != null)
			{
				// ignore comments
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				
				final StringTokenizer st = new StringTokenizer(line, ";");
				
				final StatsSet hennaDat = new StatsSet();
				final int id = Integer.parseInt(st.nextToken());
				hennaDat.set("symbol_id", id);
				st.nextToken(); // next token...ignore name
				hennaDat.set("dye", Integer.parseInt(st.nextToken()));
				hennaDat.set("amount", Integer.parseInt(st.nextToken()));
				hennaDat.set("price", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_INT", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_STR", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_CON", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_MEM", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_DEX", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_WIT", Integer.parseInt(st.nextToken()));
				
				final Henna template = new Henna(hennaDat);
				_henna.put(id, template);
			}
			
			LOGGER.info("HennaTable: Loaded " + _henna.size() + " Templates.");
		}
		catch (FileNotFoundException e)
		{
			LOGGER.warning(Config.DATAPACK_ROOT + "/data/csv/henna.csv is missing in data folder");
		}
		catch (IOException e0)
		{
			LOGGER.warning("Error while creating table: " + e0.getMessage() + "\n" + e0);
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
					LOGGER.warning("Problem with HennaTable: " + e1.getMessage());
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
					LOGGER.warning("Problem with HennaTable: " + e1.getMessage());
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
					LOGGER.warning("Problem with HennaTable: " + e1.getMessage());
				}
			}
		}
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	public Henna getTemplate(int id)
	{
		return _henna.get(id);
	}
	
	public static HennaTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HennaTable INSTANCE = new HennaTable();
	}
}
