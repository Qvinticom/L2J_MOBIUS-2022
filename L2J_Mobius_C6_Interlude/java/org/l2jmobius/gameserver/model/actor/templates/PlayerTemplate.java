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
	private final Race _race;
	private final ClassId _classId;
	
	private final int _currentCollisionRadius;
	private final int _currentCollisionHeight;
	private final String _className;
	
	private final int _spawnX;
	private final int _spawnY;
	private final int _spawnZ;
	
	private final int _classBaseLevel;
	private final float _lvlHpAdd;
	private final float _lvlHpMod;
	private final float _lvlCpAdd;
	private final float _lvlCpMod;
	private final float _lvlMpAdd;
	private final float _lvlMpMod;
	
	private final List<Item> _items = new ArrayList<>();
	
	public PlayerTemplate(StatsSet set)
	{
		super(set);
		_classId = ClassId.getClassId(set.getInt("classId"));
		_race = Race.values()[set.getInt("raceId")];
		_className = set.getString("className");
		_currentCollisionRadius = set.getInt("collision_radius");
		_currentCollisionHeight = set.getInt("collision_height");
		
		_spawnX = set.getInt("spawnX");
		_spawnY = set.getInt("spawnY");
		_spawnZ = set.getInt("spawnZ");
		
		_classBaseLevel = set.getInt("classBaseLevel");
		_lvlHpAdd = set.getFloat("lvlHpAdd");
		_lvlHpMod = set.getFloat("lvlHpMod");
		_lvlCpAdd = set.getFloat("lvlCpAdd");
		_lvlCpMod = set.getFloat("lvlCpMod");
		_lvlMpAdd = set.getFloat("lvlMpAdd");
		_lvlMpMod = set.getFloat("lvlMpMod");
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
	
	public Race getRace()
	{
		return _race;
	}
	
	public ClassId getClassId()
	{
		return _classId;
	}
	
	@Override
	public int getCollisionRadius()
	{
		return _currentCollisionRadius;
	}
	
	@Override
	public int getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	public String getClassName()
	{
		return _className;
	}
	
	public int getSpawnX()
	{
		return _spawnX;
	}
	
	public int getSpawnY()
	{
		return _spawnY;
	}
	
	public int getSpawnZ()
	{
		return _spawnZ;
	}
	
	public int getClassBaseLevel()
	{
		return _classBaseLevel;
	}
	
	public float getLvlHpAdd()
	{
		return _lvlHpAdd;
	}
	
	public float getLvlHpMod()
	{
		return _lvlHpMod;
	}
	
	public float getLvlCpAdd()
	{
		return _lvlCpAdd;
	}
	
	public float getLvlCpMod()
	{
		return _lvlCpMod;
	}
	
	public float getLvlMpAdd()
	{
		return _lvlMpAdd;
	}
	
	public float getLvlMpMod()
	{
		return _lvlMpMod;
	}
	
	public int getBaseFallSafeHeight(boolean female)
	{
		if ((_classId.getRace() == Race.DARK_ELF) || (_classId.getRace() == Race.ELF))
		{
			return _classId.isMage() ? (female ? 330 : 300) : female ? 380 : 350;
		}
		else if (_classId.getRace() == Race.DWARF)
		{
			return female ? 200 : 180;
		}
		else if (_classId.getRace() == Race.HUMAN)
		{
			return _classId.isMage() ? (female ? 220 : 200) : female ? 270 : 250;
		}
		else if (_classId.getRace() == Race.ORC)
		{
			return _classId.isMage() ? (female ? 280 : 250) : female ? 220 : 200;
		}
		
		return 400;
	}
	
	public int getFallHeight()
	{
		return 333; // TODO: unhardcode it
	}
}
