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
		try
		{
			final File spawnDataFile = new File("data/lvlupgain.csv");
			final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(spawnDataFile)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				final StringTokenizer st = new StringTokenizer(line, ";");
				final LvlupData lvlupData = new LvlupData();
				lvlupData.setClassid(Integer.parseInt(st.nextToken()));
				lvlupData.setDefaulthp(Double.parseDouble(st.nextToken()));
				lvlupData.setDefaulthpadd(Double.parseDouble(st.nextToken()));
				lvlupData.setDefaulthpbonus(Double.parseDouble(st.nextToken()));
				lvlupData.setDefaultmp(Double.parseDouble(st.nextToken()));
				lvlupData.setDefaultmpadd(Double.parseDouble(st.nextToken()));
				lvlupData.setDefaultmpbonus(Double.parseDouble(st.nextToken()));
				_lvltable.put(lvlupData.getClassid(), lvlupData);
			}
			lnr.close();
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
	}
	
	public LvlupData getTemplate(int classId)
	{
		return _lvltable.get(classId);
	}
}
