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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.templates.Item;
import org.l2jmobius.gameserver.templates.Weapon;

public class Inventory
{
	private static Logger _log = Logger.getLogger(Inventory.class.getName());
	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_LEAR = 1;
	public static final int PAPERDOLL_REAR = 2;
	public static final int PAPERDOLL_NECK = 3;
	public static final int PAPERDOLL_LFINGER = 4;
	public static final int PAPERDOLL_RFINGER = 5;
	public static final int PAPERDOLL_HEAD = 6;
	public static final int PAPERDOLL_RHAND = 7;
	public static final int PAPERDOLL_LHAND = 8;
	public static final int PAPERDOLL_GLOVES = 9;
	public static final int PAPERDOLL_CHEST = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_BACK = 13;
	public static final int PAPERDOLL_LRHAND = 14;
	private final ItemInstance[] _paperdoll = new ItemInstance[16];
	private ItemInstance _adena;
	private final List<ItemInstance> _items = new CopyOnWriteArrayList<>();
	private int _totalWeight;
	
	public int getSize()
	{
		return _items.size();
	}
	
	public Collection<ItemInstance> getItems()
	{
		return _items;
	}
	
	public ItemInstance addItem(ItemInstance newItem)
	{
		ItemInstance old;
		ItemInstance result = newItem;
		boolean stackableFound = false;
		if (newItem.getItemId() == 57)
		{
			addAdena(newItem.getCount());
			return _adena;
		}
		if (newItem.isStackable() && ((old = findItemByItemId(newItem.getItemId())) != null))
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
		refreshWeight();
		return result;
	}
	
	public ItemInstance findItemByItemId(int itemId)
	{
		for (int i = 0; i < _items.size(); ++i)
		{
			ItemInstance temp = _items.get(i);
			if (temp.getItemId() != itemId)
			{
				continue;
			}
			return temp;
		}
		return null;
	}
	
	public ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}
	
	public int getPaperdollItemId(int slot)
	{
		if (_paperdoll[slot] != null)
		{
			return _paperdoll[slot].getItemId();
		}
		return 0;
	}
	
	public int getPaperdollObjectId(int slot)
	{
		if (_paperdoll[slot] != null)
		{
			return _paperdoll[slot].getObjectId();
		}
		return 0;
	}
	
	public void setPaperdollItem(int slot, ItemInstance item)
	{
		_paperdoll[slot] = item;
		item.setEquipSlot(slot);
		refreshWeight();
	}
	
	public Collection<ItemInstance> unEquipItemInBodySlot(int slot)
	{
		List<ItemInstance> unequipedItems = new ArrayList<>();
		int pdollSlot = -1;
		switch (slot)
		{
			case 4:
			{
				pdollSlot = 1;
				break;
			}
			case 2:
			{
				pdollSlot = 2;
				break;
			}
			case 8:
			{
				pdollSlot = 3;
				break;
			}
			case 16:
			{
				pdollSlot = 5;
				break;
			}
			case 32:
			{
				pdollSlot = 4;
				break;
			}
			case 64:
			{
				pdollSlot = 6;
				break;
			}
			case 128:
			{
				pdollSlot = 7;
				break;
			}
			case 256:
			{
				pdollSlot = 8;
				break;
			}
			case 512:
			{
				pdollSlot = 9;
				break;
			}
			case 1024:
			case 32768:
			{
				pdollSlot = 10;
				break;
			}
			case 2048:
			{
				pdollSlot = 11;
				break;
			}
			case 16384:
			{
				unEquipSlot(unequipedItems, 8);
				unEquipSlot(7);
				pdollSlot = 14;
				break;
			}
			case 8192:
			{
				pdollSlot = 13;
				break;
			}
			case 4096:
			{
				pdollSlot = 12;
				break;
			}
			case 1:
			{
				pdollSlot = 0;
			}
		}
		unEquipSlot(unequipedItems, pdollSlot);
		return unequipedItems;
	}
	
	public Collection<ItemInstance> unEquipItemOnPaperdoll(int pdollSlot)
	{
		List<ItemInstance> unequipedItems = new ArrayList<>();
		if (pdollSlot == 14)
		{
			unEquipSlot(unequipedItems, 8);
			unEquipSlot(7);
		}
		unEquipSlot(unequipedItems, pdollSlot);
		return unequipedItems;
	}
	
	public List<ItemInstance> equipItem(ItemInstance item)
	{
		ArrayList<ItemInstance> changedItems = new ArrayList<>();
		int targetSlot = item.getItem().getBodyPart();
		switch (targetSlot)
		{
			case 16384:
			{
				ItemInstance arrow;
				unEquipSlot(changedItems, 8);
				ItemInstance old1 = unEquipSlot(14);
				if (old1 != null)
				{
					changedItems.add(old1);
					unEquipSlot(7);
					unEquipSlot(changedItems, 8);
				}
				else
				{
					unEquipSlot(changedItems, 7);
				}
				setPaperdollItem(7, item);
				setPaperdollItem(14, item);
				if ((((Weapon) item.getItem()).getWeaponType() != 5) || ((arrow = findArrowForBow(item.getItem())) == null))
				{
					break;
				}
				setPaperdollItem(8, arrow);
				arrow.setLastChange(2);
				changedItems.add(arrow);
				break;
			}
			case 256:
			{
				ItemInstance old1 = unEquipSlot(14);
				if (old1 != null)
				{
					unEquipSlot(changedItems, 7);
				}
				unEquipSlot(changedItems, 8);
				setPaperdollItem(8, item);
				break;
			}
			case 128:
			{
				if (unEquipSlot(changedItems, 14))
				{
					unEquipSlot(changedItems, 8);
					unEquipSlot(7);
				}
				else
				{
					unEquipSlot(changedItems, 7);
				}
				setPaperdollItem(7, item);
				break;
			}
			case 6:
			{
				if (_paperdoll[1] == null)
				{
					setPaperdollItem(1, item);
					break;
				}
				if (_paperdoll[2] == null)
				{
					setPaperdollItem(2, item);
					break;
				}
				unEquipSlot(changedItems, 1);
				setPaperdollItem(1, item);
				break;
			}
			case 48:
			{
				if (_paperdoll[4] == null)
				{
					setPaperdollItem(4, item);
					break;
				}
				if (_paperdoll[5] == null)
				{
					setPaperdollItem(5, item);
					break;
				}
				unEquipSlot(changedItems, 4);
				setPaperdollItem(4, item);
				break;
			}
			case 8:
			{
				unEquipSlot(changedItems, 3);
				setPaperdollItem(3, item);
				break;
			}
			case 32768:
			{
				unEquipSlot(changedItems, 10);
				unEquipSlot(changedItems, 11);
				setPaperdollItem(10, item);
				break;
			}
			case 1024:
			{
				unEquipSlot(changedItems, 10);
				setPaperdollItem(10, item);
				break;
			}
			case 2048:
			{
				ItemInstance chest = getPaperdollItem(10);
				if ((chest != null) && (chest.getItem().getBodyPart() == 32768))
				{
					unEquipSlot(changedItems, 10);
				}
				unEquipSlot(changedItems, 11);
				setPaperdollItem(11, item);
				break;
			}
			case 4096:
			{
				unEquipSlot(changedItems, 12);
				setPaperdollItem(12, item);
				break;
			}
			case 512:
			{
				unEquipSlot(changedItems, 9);
				setPaperdollItem(9, item);
				break;
			}
			case 64:
			{
				unEquipSlot(changedItems, 6);
				setPaperdollItem(6, item);
				break;
			}
			case 1:
			{
				unEquipSlot(changedItems, 0);
				setPaperdollItem(0, item);
				break;
			}
			case 8192:
			{
				unEquipSlot(changedItems, 13);
				setPaperdollItem(13, item);
				break;
			}
			default:
			{
				_log.warning("unknown body slot:" + targetSlot);
			}
		}
		changedItems.add(item);
		item.setLastChange(2);
		return changedItems;
	}
	
	private ItemInstance unEquipSlot(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			item.setEquipSlot(-1);
			item.setLastChange(2);
			_paperdoll[slot] = null;
		}
		return item;
	}
	
	private boolean unEquipSlot(List<ItemInstance> changedItems, int slot)
	{
		if (slot == -1)
		{
			return false;
		}
		ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			item.setEquipSlot(-1);
			changedItems.add(item);
			item.setLastChange(2);
			_paperdoll[slot] = null;
		}
		return item != null;
	}
	
	public ItemInstance getItem(int objectId)
	{
		for (int i = 0; i < _items.size(); ++i)
		{
			ItemInstance temp = _items.get(i);
			if (temp.getObjectId() != objectId)
			{
				continue;
			}
			return temp;
		}
		return null;
	}
	
	public ItemInstance getAdenaInstance()
	{
		return _adena;
	}
	
	public int getAdena()
	{
		if (_adena == null)
		{
			return 0;
		}
		return _adena.getCount();
	}
	
	public void setAdena(int adena)
	{
		if (adena == 0)
		{
			if ((_adena != null) && _items.contains(_adena))
			{
				_items.remove(_adena);
				_adena = null;
			}
		}
		else
		{
			if (_adena == null)
			{
				_adena = ItemTable.getInstance().createItem(57);
			}
			_adena.setCount(adena);
			if (!_items.contains(_adena))
			{
				_items.add(_adena);
			}
		}
	}
	
	public void addAdena(int adena)
	{
		setAdena(getAdena() + adena);
	}
	
	public void reduceAdena(int adena)
	{
		setAdena(getAdena() - adena);
	}
	
	public ItemInstance destroyItem(int objectId, int count)
	{
		ItemInstance item = getItem(objectId);
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
		refreshWeight();
		return item;
	}
	
	public ItemInstance destroyItemByItemId(int itemId, int count)
	{
		ItemInstance item = findItemByItemId(itemId);
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
		refreshWeight();
		return item;
	}
	
	public ItemInstance dropItem(int objectId, int count)
	{
		ItemInstance oldItem = getItem(objectId);
		return this.dropItem(oldItem, count);
	}
	
	public ItemInstance dropItem(ItemInstance oldItem, int count)
	{
		if (oldItem == null)
		{
			_log.warning("DropItem: item id does not exist in inventory");
			return null;
		}
		if (oldItem.isEquipped())
		{
			unEquipItemInBodySlot(oldItem.getItem().getBodyPart());
		}
		if (oldItem.getItemId() == 57)
		{
			reduceAdena(count);
			ItemInstance adena = ItemTable.getInstance().createItem(oldItem.getItemId());
			adena.setCount(count);
			return adena;
		}
		if (oldItem.getCount() == count)
		{
			_items.remove(oldItem);
			oldItem.setLastChange(3);
			refreshWeight();
			return oldItem;
		}
		oldItem.setCount(oldItem.getCount() - count);
		oldItem.setLastChange(2);
		ItemInstance newItem = ItemTable.getInstance().createItem(oldItem.getItemId());
		newItem.setCount(count);
		refreshWeight();
		return newItem;
	}
	
	private void refreshWeight()
	{
		int weight = 0;
		for (ItemInstance item : _items)
		{
			weight += item.getItem().getWeight() * item.getCount();
		}
		_totalWeight = weight;
	}
	
	public int getTotalWeight()
	{
		return _totalWeight;
	}
	
	public ItemInstance findArrowForBow(Item bow)
	{
		int arrowsId = 0;
		switch (bow.getCrystalType())
		{
			case 1:
			{
				arrowsId = 17;
				break;
			}
			case 2:
			{
				arrowsId = 1341;
				break;
			}
			case 3:
			{
				arrowsId = 1342;
				break;
			}
			case 4:
			{
				arrowsId = 1343;
				break;
			}
			case 5:
			{
				arrowsId = 1344;
				break;
			}
			case 6:
			{
				arrowsId = 1345;
				break;
			}
			default:
			{
				arrowsId = 17;
			}
		}
		return findItemByItemId(arrowsId);
	}
}
