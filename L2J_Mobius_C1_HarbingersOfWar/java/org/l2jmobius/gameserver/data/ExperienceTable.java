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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class ExperienceTable
{
	private static Logger _log = Logger.getLogger(ExperienceTable.class.getName());
	private static final Map<Integer, Integer> _exp = new HashMap<>();
	private static ExperienceTable _instance;
	
	private ExperienceTable()
	{
		try
		{
			File expData = new File("data/exp.csv");
			if (expData.isFile() && expData.exists())
			{
				LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(expData)));
				String line = null;
				while ((line = lnr.readLine()) != null)
				{
					if (line.trim().isEmpty() || line.startsWith("#"))
					{
						continue;
					}
					StringTokenizer expLine = new StringTokenizer(line, ";");
					String level = expLine.nextToken().trim();
					String exp = expLine.nextToken().trim();
					_exp.put(Integer.parseInt(level), Integer.parseInt(exp));
				}
				lnr.close();
				_log.config("Loaded " + _exp.size() + " exp mappings.");
			}
			else
			{
				_log.warning("File exp.csv is missing in data folder.");
			}
		}
		catch (Exception e)
		{
			_log.warning("Error while creating exp map " + e);
		}
	}
	
	public static ExperienceTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ExperienceTable();
		}
		return _instance;
	}
	
	public int getExp(int level)
	{
		if (_exp.containsKey(level))
		{
			return _exp.get(level);
		}
		return Integer.MAX_VALUE;
	}
}
