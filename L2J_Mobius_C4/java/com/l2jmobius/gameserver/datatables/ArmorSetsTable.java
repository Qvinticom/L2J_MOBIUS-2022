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
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.model.L2ArmorSet;

import javolution.util.FastMap;

/**
 * @author Luno
 */
public class ArmorSetsTable
{
	private static Logger _log = Logger.getLogger(ArmorSetsTable.class.getName());
	private static ArmorSetsTable _instance;
	
	private final FastMap<Integer, L2ArmorSet> _armorSets;
	
	public static ArmorSetsTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ArmorSetsTable();
		}
		return _instance;
	}
	
	private ArmorSetsTable()
	{
		_armorSets = new FastMap<>();
		loadData();
	}
	
	private void loadData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id FROM armorsets");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				final int chest = rset.getInt("chest");
				final int legs = rset.getInt("legs");
				final int head = rset.getInt("head");
				final int gloves = rset.getInt("gloves");
				final int feet = rset.getInt("feet");
				final int skill_id = rset.getInt("skill_id");
				final int shield = rset.getInt("shield");
				final int shield_skill_id = rset.getInt("shield_skill_id");
				_armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id));
			}
			
			_log.config("ArmorSetsTable: Loaded " + _armorSets.size() + " armor sets.");
		}
		catch (final Exception e)
		{
			_log.severe("ArmorSetsTable: Error reading ArmorSets table: " + e);
		}
		
		if (Config.CUSTOM_ARMORSETS_TABLE)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id FROM custom_armorsets");
				ResultSet rset = statement.executeQuery())
			{
				final int cSets = _armorSets.size();
				
				while (rset.next())
				{
					final int chest = rset.getInt("chest");
					final int legs = rset.getInt("legs");
					final int head = rset.getInt("head");
					final int gloves = rset.getInt("gloves");
					final int feet = rset.getInt("feet");
					final int skill_id = rset.getInt("skill_id");
					final int shield = rset.getInt("shield");
					final int shield_skill_id = rset.getInt("shield_skill_id");
					_armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id));
				}
				
				_log.config("ArmorSetsTable: Loaded " + (_armorSets.size() - cSets) + " custom armor sets.");
			}
			catch (final Exception e)
			{
				_log.severe("ArmorSetsTable: Error reading Custom ArmorSets table: " + e);
			}
		}
	}
	
	public boolean setExists(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}
	
	public L2ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}
}