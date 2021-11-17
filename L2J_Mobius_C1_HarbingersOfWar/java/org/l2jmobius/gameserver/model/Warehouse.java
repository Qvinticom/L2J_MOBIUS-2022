/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.item.instance.Item;

public class Warehouse
{
	private final List<Item> _items = new ArrayList<>();
	
	public int getSize()
	{
		return _items.size();
	}
	
	public List<Item> getItems()
	{
		return _items;
	}
	
	public Item addItem(Item newItem)
	{
		Item old;
		Item result = newItem;
		boolean stackableFound = false;
		if (newItem.isStackable() && ((old = findItemId(newItem.getItemId())) != null))
		{
			old.setCount(old.getCount() + newItem.getCount());
			stackableFound = true;
			old.setLastChange(2);
			result = old;
		}
		if (!stackableFound)
		{
			_items.add(newItem);
			newItem.setLastChange(1);
		}
		return result;
	}
	
	private Item findItemId(int itemId)
	{
		for (int i = 0; i < _items.size(); ++i)
		{
			final Item temp = _items.get(i);
			if (temp.getItemId() != itemId)
			{
				continue;
			}
			return temp;
		}
		return null;
	}
	
	public Item getItem(int objectId)
	{
		for (int i = 0; i < _items.size(); ++i)
		{
			final Item temp = _items.get(i);
			if (temp.getObjectId() != objectId)
			{
				continue;
			}
			return temp;
		}
		return null;
	}
	
	public Item destroyItem(int itemId, int count)
	{
		final Item item = findItemId(itemId);
		if (item != null)
		{
			if (item.getCount() == count)
			{
				_items.remove(item);
				item.setCount(0);
				item.setLastChange(3);
			}
			else
			{
				item.setCount(item.getCount() - count);
				item.setLastChange(2);
			}
		}
		return item;
	}
}
