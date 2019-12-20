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
package org.l2jmobius.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.ArmorSet;

public class ArmorSetsTable
{
	private static final Logger LOGGER = Logger.getLogger(ArmorSetsTable.class.getName());
	
	public Map<Integer, ArmorSet> armorSets;
	private final Map<Integer, ArmorDummy> cusArmorSets;
	
	private ArmorSetsTable()
	{
		armorSets = new HashMap<>();
		cusArmorSets = new HashMap<>();
		loadData();
	}
	
	private void loadData()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill FROM armorsets");
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int id = rset.getInt("id");
				final int chest = rset.getInt("chest");
				final int legs = rset.getInt("legs");
				final int head = rset.getInt("head");
				final int gloves = rset.getInt("gloves");
				final int feet = rset.getInt("feet");
				final int skill_id = rset.getInt("skill_id");
				final int shield = rset.getInt("shield");
				final int shield_skill_id = rset.getInt("shield_skill_id");
				final int enchant6skill = rset.getInt("enchant6skill");
				
				armorSets.put(chest, new ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill));
				cusArmorSets.put(id, new ArmorDummy(chest, legs, head, gloves, feet, skill_id, shield));
			}
			
			LOGGER.info("Loaded: " + armorSets.size() + " armor sets.");
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while loading armor sets data " + e);
		}
	}
	
	public boolean setExists(int chestId)
	{
		return armorSets.containsKey(chestId);
	}
	
	public ArmorSet getSet(int chestId)
	{
		return armorSets.get(chestId);
	}
	
	public void addObj(int v, ArmorSet s)
	{
		armorSets.put(v, s);
	}
	
	public ArmorDummy getCusArmorSets(int id)
	{
		return cusArmorSets.get(id);
	}
	
	public class ArmorDummy
	{
		private final int _chest;
		private final int _legs;
		private final int _head;
		private final int _gloves;
		private final int _feet;
		private final int _skill_id;
		private final int _shield;
		
		public ArmorDummy(int chest, int legs, int head, int gloves, int feet, int skillId, int shield)
		{
			_chest = chest;
			_legs = legs;
			_head = head;
			_gloves = gloves;
			_feet = feet;
			_skill_id = skillId;
			_shield = shield;
		}
		
		public int getChest()
		{
			return _chest;
		}
		
		public int getLegs()
		{
			return _legs;
		}
		
		public int getHead()
		{
			return _head;
		}
		
		public int getGloves()
		{
			return _gloves;
		}
		
		public int getFeet()
		{
			return _feet;
		}
		
		public int getSkill_id()
		{
			return _skill_id;
		}
		
		public int getShield()
		{
			return _shield;
		}
	}
	
	public static ArmorSetsTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ArmorSetsTable INSTANCE = new ArmorSetsTable();
	}
}
