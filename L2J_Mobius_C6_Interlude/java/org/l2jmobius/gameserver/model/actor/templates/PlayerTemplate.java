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
package org.l2jmobius.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.datatables.xml.ItemTable;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.StatsSet;
import org.l2jmobius.gameserver.model.base.ClassId;
import org.l2jmobius.gameserver.model.items.Item;

/**
 * @author mkizub
 */
public class PlayerTemplate extends CreatureTemplate
{
	/** The Class object of the PlayerInstance */
	public Race race;
	public ClassId classId;
	
	public int _currentCollisionRadius;
	public int _currentCollisionHeight;
	public String className;
	
	public int spawnX;
	public int spawnY;
	public int spawnZ;
	
	public int classBaseLevel;
	public float lvlHpAdd;
	public float lvlHpMod;
	public float lvlCpAdd;
	public float lvlCpMod;
	public float lvlMpAdd;
	public float lvlMpMod;
	
	private final List<Item> _items = new ArrayList<>();
	
	public PlayerTemplate(StatsSet set)
	{
		super(set);
		classId = ClassId.values()[set.getInt("classId")];
		race = Race.values()[set.getInt("raceId")];
		className = set.getString("className");
		_currentCollisionRadius = set.getInt("collision_radius");
		_currentCollisionHeight = set.getInt("collision_height");
		
		spawnX = set.getInt("spawnX");
		spawnY = set.getInt("spawnY");
		spawnZ = set.getInt("spawnZ");
		
		classBaseLevel = set.getInt("classBaseLevel");
		lvlHpAdd = set.getFloat("lvlHpAdd");
		lvlHpMod = set.getFloat("lvlHpMod");
		lvlCpAdd = set.getFloat("lvlCpAdd");
		lvlCpMod = set.getFloat("lvlCpMod");
		lvlMpAdd = set.getFloat("lvlMpAdd");
		lvlMpMod = set.getFloat("lvlMpMod");
	}
	
	/**
	 * add starter equipment
	 * @param itemId
	 */
	public void addItem(int itemId)
	{
		final Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item != null)
		{
			_items.add(item);
		}
	}
	
	/**
	 * @return itemIds of all the starter equipment
	 */
	public Item[] getItems()
	{
		return _items.toArray(new Item[_items.size()]);
	}
	
	/**
	 * @return
	 */
	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}
	
	/**
	 * @return
	 */
	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	public int getBaseFallSafeHeight(boolean female)
	{
		if ((classId.getRace() == Race.DARK_ELF) || (classId.getRace() == Race.ELF))
		{
			return classId.isMage() ? (female ? 330 : 300) : female ? 380 : 350;
		}
		else if (classId.getRace() == Race.DWARF)
		{
			return female ? 200 : 180;
		}
		else if (classId.getRace() == Race.HUMAN)
		{
			return classId.isMage() ? (female ? 220 : 200) : female ? 270 : 250;
		}
		else if (classId.getRace() == Race.ORC)
		{
			return classId.isMage() ? (female ? 280 : 250) : female ? 220 : 200;
		}
		
		return 400;
	}
	
	public int getFallHeight()
	{
		return 333; // TODO: unhardcode it
	}
}
