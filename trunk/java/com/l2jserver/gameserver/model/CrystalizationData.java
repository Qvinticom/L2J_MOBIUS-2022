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
package com.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.holders.ItemChanceHolder;

/**
 * @author UnAfraid
 */
public class CrystalizationData
{
	private final int _id;
	private final List<ItemChanceHolder> _items = new ArrayList<>();
	
	public CrystalizationData(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void addItem(ItemChanceHolder item)
	{
		_items.add(item);
	}
	
	public List<ItemChanceHolder> getItems()
	{
		return _items;
	}
}
