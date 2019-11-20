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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.LvlupData;

public class LevelUpData
{
	private static Logger _log = Logger.getLogger(LevelUpData.class.getName());
	
	private final Map<Integer, LvlupData> _lvltable = new HashMap<>();
	private static LevelUpData _instance;
	
	public static LevelUpData getInstance()
	{
		if (_instance == null)
		{
			_instance = new LevelUpData();
		}
		return _instance;
	}
	
	private LevelUpData()
	{
		BufferedReader lnr = null;
		try
		{
			File spawnDataFile = new File("data/lvlupgain.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(spawnDataFile)));
			String line = null;
			while ((line = ((LineNumberReader) lnr).readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				LvlupData lvlupData = parseList(line);
				_lvltable.put(lvlupData.getClassid(), lvlupData);
			}
			_log.config("Loaded " + _lvltable.size() + " Lvl up data templates.");
		}
		catch (FileNotFoundException e)
		{
			_log.warning("lvlupgain.csv is missing in data folder.");
		}
		catch (Exception e)
		{
			_log.warning("Error while creating npc data table " + e);
		}
		finally
		{
			try
			{
				if (lnr != null)
				{
					lnr.close();
				}
			}
			catch (Exception e1)
			{
			}
		}
	}
	
	private LvlupData parseList(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		LvlupData lvlDat = new LvlupData();
		lvlDat.setClassid(Integer.parseInt(st.nextToken()));
		lvlDat.setDefaulthp(Double.parseDouble(st.nextToken()));
		lvlDat.setDefaulthpadd(Double.parseDouble(st.nextToken()));
		lvlDat.setDefaulthpbonus(Double.parseDouble(st.nextToken()));
		lvlDat.setDefaultmp(Double.parseDouble(st.nextToken()));
		lvlDat.setDefaultmpadd(Double.parseDouble(st.nextToken()));
		lvlDat.setDefaultmpbonus(Double.parseDouble(st.nextToken()));
		return lvlDat;
	}
	
	public LvlupData getTemplate(int classId)
	{
		return _lvltable.get(classId);
	}
}
