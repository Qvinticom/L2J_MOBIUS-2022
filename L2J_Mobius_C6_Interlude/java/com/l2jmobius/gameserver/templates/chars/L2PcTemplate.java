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
package com.l2jmobius.gameserver.templates.chars;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.datatables.xml.ItemTable;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.base.Race;
import com.l2jmobius.gameserver.templates.StatsSet;
import com.l2jmobius.gameserver.templates.item.L2Item;

/**
 * @author mkizub
 */
public class L2PcTemplate extends L2CharTemplate
{
	/** The Class object of the L2PcInstance */
	public final Race race;
	public final ClassId classId;
	
	public final int _currentCollisionRadius;
	public final int _currentCollisionHeight;
	public final String className;
	
	public final int spawnX;
	public final int spawnY;
	public final int spawnZ;
	
	public final int classBaseLevel;
	public final float lvlHpAdd;
	public final float lvlHpMod;
	public final float lvlCpAdd;
	public final float lvlCpMod;
	public final float lvlMpAdd;
	public final float lvlMpMod;
	
	private final List<L2Item> _items = new ArrayList<>();
	
	public L2PcTemplate(StatsSet set)
	{
		super(set);
		classId = ClassId.values()[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");
		_currentCollisionRadius = set.getInteger("collision_radius");
		_currentCollisionHeight = set.getInteger("collision_height");
		
		spawnX = set.getInteger("spawnX");
		spawnY = set.getInteger("spawnY");
		spawnZ = set.getInteger("spawnZ");
		
		classBaseLevel = set.getInteger("classBaseLevel");
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
		final L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item != null)
		{
			_items.add(item);
		}
	}
	
	/**
	 * @return itemIds of all the starter equipment
	 */
	public L2Item[] getItems()
	{
		return _items.toArray(new L2Item[_items.size()]);
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
		if ((classId.getRace() == Race.darkelf) || (classId.getRace() == Race.elf))
		{
			return classId.isMage() ? (female ? 330 : 300) : female ? 380 : 350;
		}
		else if (classId.getRace() == Race.dwarf)
		{
			return female ? 200 : 180;
		}
		else if (classId.getRace() == Race.human)
		{
			return classId.isMage() ? (female ? 220 : 200) : female ? 270 : 250;
		}
		else if (classId.getRace() == Race.orc)
		{
			return classId.isMage() ? (female ? 280 : 250) : female ? 220 : 200;
		}
		
		return 400;
	}
	
	public final int getFallHeight()
	{
		return 333; // TODO: unhardcode it
	}
}
