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
package com.l2jserver.gameserver.model.entity;

import java.util.ArrayList;

/**
 * @author Erlandys
 */
public class AppearanceStone
{
	public enum StoneType
	{
		None,
		Normal,
		Blessed,
		Fixed,
		Restore
	}
	
	public enum AppearanceItemType
	{
		None,
		Weapon,
		Armor,
		Accessory,
		All
	}
	
	private final int _itemId;
	private final StoneType _type;
	private final AppearanceItemType _itemType;
	private final ArrayList<Integer> _grades;
	private final long _price;
	private final int _targetItem;
	private final long _timeForAppearance;
	private int _maxGrade;
	
	public AppearanceStone(int itemId, StoneType type, AppearanceItemType itemType, ArrayList<Integer> grades, long price, int targetItem, long timeForAppearance)
	{
		_itemId = itemId;
		_type = type;
		_itemType = itemType;
		_grades = grades;
		_maxGrade = grades.get(0);
		for (int gr : _grades)
		{
			if (_maxGrade < gr)
			{
				_maxGrade = gr;
			}
		}
		_price = price;
		_targetItem = targetItem;
		_timeForAppearance = timeForAppearance;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public StoneType getType()
	{
		return _type;
	}
	
	public AppearanceItemType getItemType()
	{
		return _itemType;
	}
	
	public ArrayList<Integer> getGrades()
	{
		return _grades;
	}
	
	public int getMaxGrade()
	{
		return _maxGrade;
	}
	
	public long getPrice()
	{
		return _price;
	}
	
	public int getTargetItem()
	{
		return _targetItem;
	}
	
	public long getTimeForAppearance()
	{
		return _timeForAppearance;
	}
}
