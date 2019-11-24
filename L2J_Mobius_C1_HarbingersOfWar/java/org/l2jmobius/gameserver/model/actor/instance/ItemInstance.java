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
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.templates.EtcItem;
import org.l2jmobius.gameserver.templates.Item;

public class ItemInstance extends WorldObject
{
	private int _count = 1;
	private int _itemId;
	private Item _item;
	private int _equippedSlot = -1;
	private int _price;
	private int _enchantLevel;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int _lastChange = 2;
	private boolean _onTheGround;
	
	public int getCount()
	{
		return _count;
	}
	
	public void setCount(int count)
	{
		_count = count;
	}
	
	public boolean isEquipable()
	{
		return (_item.getBodyPart() != 0) && !(_item instanceof EtcItem);
	}
	
	public boolean isEquipped()
	{
		return _equippedSlot != -1;
	}
	
	public void setEquipSlot(int slot)
	{
		_equippedSlot = slot;
	}
	
	public int getEquipSlot()
	{
		return _equippedSlot;
	}
	
	public Item getItem()
	{
		return _item;
	}
	
	public void setItem(Item item)
	{
		_item = item;
		_itemId = item.getItemId();
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public void setPrice(int price)
	{
		_price = price;
	}
	
	public int getLastChange()
	{
		return _lastChange;
	}
	
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}
	
	public boolean isStackable()
	{
		return _item.isStackable();
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		player.setCurrentState(CreatureState.PICKUP_ITEM);
		player.setTarget(this);
		player.moveTo(getX(), getY(), getZ(), 0);
	}
	
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public void setEnchantLevel(int enchantLevel)
	{
		_enchantLevel = enchantLevel;
	}
	
	public boolean isOnTheGround()
	{
		return _onTheGround;
	}
	
	public void setOnTheGround(boolean b)
	{
		_onTheGround = b;
	}
	
	@Override
	public boolean isItem()
	{
		return true;
	}
}
