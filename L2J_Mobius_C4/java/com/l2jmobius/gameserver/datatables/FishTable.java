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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.model.FishData;

import javolution.util.FastList;

/**
 * @author -Nemesiss-
 */
public class FishTable
{
	private static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static final FishTable _instance = new FishTable();
	
	private static List<FishData> _Fishs;
	private static List<FishData> _Fishs_Newbie;
	
	public static FishTable getInstance()
	{
		return _instance;
	}
	
	private FishTable()
	{
		// Create table that contains all fish data
		int count = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, level, name, hp, hpregen, fish_type, fish_group, fish_guts, guts_check_time, wait_time, combat_time FROM fish ORDER BY id");
			ResultSet Fishes = statement.executeQuery())
		{
			_Fishs_Newbie = new FastList<>();
			_Fishs = new FastList<>();
			
			FishData fish;
			
			while (Fishes.next())
			{
				final int id = Fishes.getInt("id");
				final int lvl = Fishes.getInt("level");
				final String name = Fishes.getString("name");
				final int hp = Fishes.getInt("hp");
				final int hpreg = Fishes.getInt("hpregen");
				final int type = Fishes.getInt("fish_type");
				final int group = Fishes.getInt("fish_group");
				final int fish_guts = Fishes.getInt("fish_guts");
				final int guts_check_time = Fishes.getInt("guts_check_time");
				final int wait_time = Fishes.getInt("wait_time");
				final int combat_time = Fishes.getInt("combat_time");
				fish = new FishData(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time);
				if (fish.getGroup() == 0)
				{
					_Fishs_Newbie.add(fish);
				}
				else
				{
					_Fishs.add(fish);
				}
			}
			
			count = _Fishs_Newbie.size() + _Fishs.size();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "error while creating fishes table" + e);
		}
		
		_log.config("FishTable: Loaded " + count + " Fishes.");
	}
	
	/**
	 * @param lvl
	 * @param type
	 * @param group
	 * @return List of Fish that can be fished
	 */
	public List<FishData> getfish(int lvl, int type, int group)
	{
		final List<FishData> result = new FastList<>();
		List<FishData> _Fishing = null;
		if (group == 0)
		{
			_Fishing = _Fishs_Newbie;
		}
		else
		{
			_Fishing = _Fishs;
		}
		
		if ((_Fishing == null) || _Fishing.isEmpty())
		{
			// the fish list is empty
			_log.warning("Fish are not defined!");
			return null;
		}
		
		for (final FishData f : _Fishing)
		{
			if (f.getLevel() != lvl)
			{
				continue;
			}
			if (f.getType() != type)
			{
				continue;
			}
			
			result.add(f);
		}
		
		if (result.size() == 0)
		{
			_log.warning("Cant Find Any Fish!? - Lvl: " + lvl + " Type: " + type);
		}
		
		return result;
	}
}