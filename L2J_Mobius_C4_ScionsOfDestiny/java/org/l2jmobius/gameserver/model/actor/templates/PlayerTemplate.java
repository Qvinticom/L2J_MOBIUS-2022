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

import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.holders.ItemHolder;

/**
 * @author mkizub
 */
public class PlayerTemplate extends CreatureTemplate
{
	private final Race _race;
	private final ClassId _classId;
	private final String _className;
	private final int _classBaseLevel;
	private final int _spawnX;
	private final int _spawnY;
	private final int _spawnZ;
	private final List<ItemHolder> _items = new ArrayList<>();
	private final float _fCollisionRadiusFemale;
	private final float _fCollisionHeightFemale;
	private final float[] _hpTable;
	private final float[] _mpTable;
	private final float[] _cpTable;
	
	public PlayerTemplate(StatSet set)
	{
		super(set);
		_classId = ClassId.getClassId(set.getInt("id"));
		_race = Enum.valueOf(Race.class, set.getString("race"));
		_className = set.getString("name");
		_spawnX = set.getInt("spawnX");
		_spawnY = set.getInt("spawnY");
		_spawnZ = set.getInt("spawnZ");
		_classBaseLevel = set.getInt("baseLevel");
		_fCollisionRadiusFemale = set.getFloat("collision_radius_female");
		_fCollisionHeightFemale = set.getFloat("collision_height_female");
		
		String[] item;
		for (String split : set.getString("items").split(";"))
		{
			item = split.split(",");
			_items.add(new ItemHolder(Integer.parseInt(item[0]), Integer.parseInt(item[1])));
		}
		
		int counter = 0;
		final String[] hpValues = set.getString("hpTable").split(";");
		_hpTable = new float[hpValues.length];
		for (String value : hpValues)
		{
			_hpTable[counter++] = Float.valueOf(value);
		}
		
		counter = 0;
		final String[] mpValues = set.getString("mpTable").split(";");
		_mpTable = new float[mpValues.length];
		for (String value : mpValues)
		{
			_mpTable[counter++] = Float.valueOf(value);
		}
		
		counter = 0;
		final String[] cpValues = set.getString("cpTable").split(";");
		_cpTable = new float[cpValues.length];
		for (String value : cpValues)
		{
			_cpTable[counter++] = Float.valueOf(value);
		}
	}
	
	public List<ItemHolder> getItems()
	{
		return _items;
	}
	
	public Race getRace()
	{
		return _race;
	}
	
	public ClassId getClassId()
	{
		return _classId;
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
	
	public float getFCollisionRadiusFemale()
	{
		return _fCollisionRadiusFemale;
	}
	
	public float getFCollisionHeightFemale()
	{
		return _fCollisionHeightFemale;
	}
	
	public float getBaseHpMax(int level)
	{
		if (level > _hpTable.length)
		{
			return _hpTable[_hpTable.length - 1];
		}
		return _hpTable[level - 1];
	}
	
	public float getBaseMpMax(int level)
	{
		if (level > _mpTable.length)
		{
			return _mpTable[_mpTable.length - 1];
		}
		return _mpTable[level - 1];
	}
	
	public float getBaseCpMax(int level)
	{
		if (level > _cpTable.length)
		{
			return _cpTable[_cpTable.length - 1];
		}
		return _cpTable[level - 1];
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
