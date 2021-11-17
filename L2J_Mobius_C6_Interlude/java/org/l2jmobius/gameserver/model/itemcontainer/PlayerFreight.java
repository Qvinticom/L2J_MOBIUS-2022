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
package org.l2jmobius.gameserver.model.itemcontainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.model.items.instance.Item.ItemLocation;

public class PlayerFreight extends ItemContainer
{
	private final Player _owner;
	private int _activeLocationId;
	
	public PlayerFreight(Player owner)
	{
		_owner = owner;
	}
	
	@Override
	public Player getOwner()
	{
		return _owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.FREIGHT;
	}
	
	public void setActiveLocation(int locationId)
	{
		_activeLocationId = locationId;
	}
	
	public int getactiveLocation()
	{
		return _activeLocationId;
	}
	
	/**
	 * Returns the quantity of items in the inventory
	 * @return int
	 */
	@Override
	public int getSize()
	{
		int size = 0;
		for (Item item : _items)
		{
			if ((item.getEquipSlot() == 0) || (_activeLocationId == 0) || (item.getEquipSlot() == _activeLocationId))
			{
				size++;
			}
		}
		return size;
	}
	
	/**
	 * Returns the list of items in inventory
	 * @return Item : items in inventory
	 */
	@Override
	public Collection<Item> getItems()
	{
		final List<Item> result = new ArrayList<>();
		for (Item item : _items)
		{
			if ((item.getEquipSlot() == 0) || (item.getEquipSlot() == _activeLocationId))
			{
				result.add(item);
			}
		}
		return result;
	}
	
	/**
	 * Returns the item from inventory by using its <b>itemId</b>
	 * @param itemId : int designating the ID of the item
	 * @return Item designating the item or null if not found in inventory
	 */
	@Override
	public Item getItemByItemId(int itemId)
	{
		for (Item item : _items)
		{
			if ((item.getItemId() == itemId) && ((item.getEquipSlot() == 0) || (_activeLocationId == 0) || (item.getEquipSlot() == _activeLocationId)))
			{
				return item;
			}
		}
		return null;
	}
	
	/**
	 * Adds item to PcFreight for further adjustments.
	 * @param item : Item to be added from inventory
	 */
	@Override
	protected void addItem(Item item)
	{
		super.addItem(item);
		if (_activeLocationId > 0)
		{
			item.setLocation(item.getItemLocation(), _activeLocationId);
		}
	}
	
	/**
	 * Get back items in PcFreight from database
	 */
	@Override
	public void restore()
	{
		final int locationId = _activeLocationId;
		_activeLocationId = 0;
		super.restore();
		_activeLocationId = locationId;
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return (getSize() + slots) <= _owner.getFreightLimit();
	}
}
