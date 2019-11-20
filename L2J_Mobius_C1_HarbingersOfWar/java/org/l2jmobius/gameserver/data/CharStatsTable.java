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

import org.l2jmobius.gameserver.model.StatModifiers;

public class CharStatsTable
{
	private static Logger _log = Logger.getLogger(CharStatsTable.class.getName());
	private static CharStatsTable _instance;
	private final Map<Integer, StatModifiers> _modifiers = new HashMap<>();
	
	public static CharStatsTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new CharStatsTable();
		}
		return _instance;
	}
	
	private CharStatsTable()
	{
		BufferedReader lnr = null;
		try
		{
			File ModifierData = new File("data/char_stats.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(ModifierData)));
			String line = null;
			while ((line = ((LineNumberReader) lnr).readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				StatModifiers modif = parseList(line);
				_modifiers.put(modif.getClassid(), modif);
			}
			_log.config("Loaded " + _modifiers.size() + " character stat modifiers.");
		}
		catch (FileNotFoundException e)
		{
			_log.warning("char_stats.csv is missing in data folder.");
		}
		catch (Exception e)
		{
			_log.warning("Error while creating character modifier table " + e);
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
	
	private StatModifiers parseList(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		StatModifiers modifier = new StatModifiers();
		modifier.setClassid(Integer.parseInt(st.nextToken()));
		modifier.setModstr(Integer.parseInt(st.nextToken()));
		modifier.setModcon(Integer.parseInt(st.nextToken()));
		modifier.setModdex(Integer.parseInt(st.nextToken()));
		modifier.setModint(Integer.parseInt(st.nextToken()));
		modifier.setModmen(Integer.parseInt(st.nextToken()));
		modifier.setModwit(Integer.parseInt(st.nextToken()));
		return modifier;
	}
	
	public StatModifiers getTemplate(int id)
	{
		return _modifiers.get(id);
	}
}
