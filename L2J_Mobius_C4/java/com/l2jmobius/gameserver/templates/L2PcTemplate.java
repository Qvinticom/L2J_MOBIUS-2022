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
package com.l2jmobius.gameserver.templates;

import java.util.List;

import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.base.Race;

import javolution.util.FastList;

/**
 * @author mkizub TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class L2PcTemplate extends L2CharTemplate
{
	/** The Class object of the L2PcInstance */
	public final ClassId classId;
	
	public final Race race;
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
	
	public final double collisionHeight_female;
	public final double collisionRadius_female;
	
	private final List<L2Item> _items = new FastList<>();
	
	public L2PcTemplate(StatsSet set)
	{
		super(set);
		classId = ClassId.values()[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");
		
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
		
		collisionRadius_female = set.getDouble("collision_radius_female");
		collisionHeight_female = set.getDouble("collision_height_female");
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
	
	public final int getFallHeight()
	{
		return 333;
	}
}