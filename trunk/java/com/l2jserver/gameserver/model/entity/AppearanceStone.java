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

/**
 * @author Erlandas
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
	
	int _itemId;
	StoneType _type;
	AppearanceItemType _itemType;
	int _maxGrade;
	long _price;
	int _targetItem;
	long _timeForAppearance;
	
	public AppearanceStone(int itemId, StoneType type, AppearanceItemType itemType, int maxGrade, long price, int targetItem, long timeForAppearance)
	{
		_itemId = itemId;
		_type = type;
		_itemType = itemType;
		_maxGrade = maxGrade;
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
