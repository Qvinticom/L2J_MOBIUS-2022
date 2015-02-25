/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.items.appearance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.type.ArmorType;
import com.l2jserver.gameserver.model.items.type.CrystalType;
import com.l2jserver.gameserver.model.items.type.WeaponType;

/**
 * @author UnAfraid
 */
public class AppearanceStone
{
	private final int _id;
	private final int _cost;
	private final int _visualId;
	private final long _lifeTime;
	private final AppearanceType _type;
	private final WeaponType _weaponType;
	private final ArmorType _armorType;
	private final AppearanceHandType _handType;
	private final AppearanceMagicType _magicType;
	private List<CrystalType> _crystalTypes;
	private List<AppearanceTargetType> _targetTypes;
	private List<Integer> _bodyParts;
	private List<Race> _races;
	private List<Race> _racesNot;
	
	public AppearanceStone(StatsSet set)
	{
		_id = set.getInt("id");
		_visualId = set.getInt("visualId", 0);
		_cost = set.getInt("cost", 0);
		_lifeTime = set.getDuration("lifeTime", Duration.ofSeconds(0)).toMillis();
		_type = set.getEnum("type", AppearanceType.class, AppearanceType.NONE);
		_weaponType = set.getEnum("weaponType", WeaponType.class, WeaponType.NONE);
		_armorType = set.getEnum("armorType", ArmorType.class, ArmorType.NONE);
		_handType = set.getEnum("handType", AppearanceHandType.class, AppearanceHandType.NONE);
		_magicType = set.getEnum("magicType", AppearanceMagicType.class, AppearanceMagicType.NONE);
		
		final CrystalType crystalType = set.getEnum("crystalType", CrystalType.class, CrystalType.NONE);
		if (crystalType != CrystalType.NONE)
		{
			addCrystalType(crystalType);
		}
		final AppearanceTargetType targetType = set.getEnum("targetType", AppearanceTargetType.class, AppearanceTargetType.NONE);
		if (targetType != AppearanceTargetType.NONE)
		{
			addTargetType(targetType);
		}
		
		final int bodyPart = ItemTable._slots.get(set.getString("bodyPart", "none"));
		if (bodyPart != L2Item.SLOT_NONE)
		{
			addBodyPart(bodyPart);
		}
		
		final Race race = set.getEnum("race", Race.class, Race.NONE);
		if (race != Race.NONE)
		{
			addRace(race);
		}
		
		final Race raceNot = set.getEnum("raceNot", Race.class, Race.NONE);
		if (raceNot != Race.NONE)
		{
			addRaceNot(raceNot);
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getVisualId()
	{
		return _visualId;
	}
	
	public int getCost()
	{
		return _cost;
	}
	
	public long getLifeTime()
	{
		return _lifeTime;
	}
	
	public AppearanceType getType()
	{
		return _type;
	}
	
	public WeaponType getWeaponType()
	{
		return _weaponType;
	}
	
	public ArmorType getArmorType()
	{
		return _armorType;
	}
	
	public AppearanceHandType getHandType()
	{
		return _handType;
	}
	
	public AppearanceMagicType getMagicType()
	{
		return _magicType;
	}
	
	public void addCrystalType(CrystalType type)
	{
		if (_crystalTypes == null)
		{
			_crystalTypes = new ArrayList<>();
		}
		_crystalTypes.add(type);
	}
	
	public List<CrystalType> getCrystalTypes()
	{
		return _crystalTypes != null ? _crystalTypes : Collections.emptyList();
	}
	
	public void addTargetType(AppearanceTargetType type)
	{
		if (_targetTypes == null)
		{
			_targetTypes = new ArrayList<>();
		}
		_targetTypes.add(type);
	}
	
	public List<AppearanceTargetType> getTargetTypes()
	{
		return _targetTypes != null ? _targetTypes : Collections.emptyList();
	}
	
	public void addBodyPart(Integer part)
	{
		if (_bodyParts == null)
		{
			_bodyParts = new ArrayList<>();
		}
		_bodyParts.add(part);
	}
	
	public List<Integer> getBodyParts()
	{
		return _bodyParts != null ? _bodyParts : Collections.emptyList();
	}
	
	public void addRace(Race race)
	{
		if (_races == null)
		{
			_races = new ArrayList<>();
		}
		_races.add(race);
	}
	
	public List<Race> getRaces()
	{
		return _races != null ? _races : Collections.emptyList();
	}
	
	public void addRaceNot(Race race)
	{
		if (_racesNot == null)
		{
			_racesNot = new ArrayList<>();
		}
		_racesNot.add(race);
	}
	
	public List<Race> getRacesNot()
	{
		return _racesNot != null ? _racesNot : Collections.emptyList();
	}
}
