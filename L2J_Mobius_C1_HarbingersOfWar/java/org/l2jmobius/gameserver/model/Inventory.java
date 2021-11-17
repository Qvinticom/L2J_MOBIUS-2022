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
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.templates.ItemTemplate;
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
	
	private final Item[] _paperdoll = new Item[16];
	private Item _adena;
	private final List<Item> _items = new CopyOnWriteArrayList<>();
	private int _totalWeight;
	
	public int getSize()
	{
		return _items.size();
	}
	
	public Collection<Item> getItems()
	{
		return _items;
	}
	
	public Item addItem(Item newItem)
	{
		Item old;
		Item result = newItem;
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
	
	public Item findItemByItemId(int itemId)
	{
		for (Item temp : _items)
		{
			if (temp.getItemId() != itemId)
			{
				continue;
			}
			return temp;
		}
		return null;
	}
	
	public Item getPaperdollItem(int slot)
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
	
	public void setPaperdollItem(int slot, Item item)
	{
		_paperdoll[slot] = item;
		item.setEquipSlot(slot);
		refreshWeight();
	}
	
	public Collection<Item> unEquipItemInBodySlot(int slot)
	{
		final List<Item> changedItems = new ArrayList<>();
		int pdollSlot = -1;
		switch (slot)
		{
			case ItemTemplate.SLOT_L_EAR:
			{
				pdollSlot = PAPERDOLL_LEAR;
				break;
			}
			case ItemTemplate.SLOT_R_EAR:
			{
				pdollSlot = PAPERDOLL_REAR;
				break;
			}
			case ItemTemplate.SLOT_NECK:
			{
				pdollSlot = PAPERDOLL_NECK;
				break;
			}
			case ItemTemplate.SLOT_R_FINGER:
			{
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			}
			case ItemTemplate.SLOT_L_FINGER:
			{
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			}
			case ItemTemplate.SLOT_HEAD:
			{
				pdollSlot = PAPERDOLL_HEAD;
				break;
			}
			case ItemTemplate.SLOT_R_HAND:
			{
				pdollSlot = PAPERDOLL_RHAND;
				break;
			}
			case ItemTemplate.SLOT_L_HAND:
			{
				pdollSlot = PAPERDOLL_LHAND;
				break;
			}
			case ItemTemplate.SLOT_GLOVES:
			{
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			}
			case ItemTemplate.SLOT_CHEST:
			case ItemTemplate.SLOT_FULL_ARMOR:
			{
				pdollSlot = PAPERDOLL_CHEST;
				break;
			}
			case ItemTemplate.SLOT_LEGS:
			{
				pdollSlot = PAPERDOLL_LEGS;
				break;
			}
			case ItemTemplate.SLOT_LR_HAND:
			{
				unEquipSlot(changedItems, PAPERDOLL_LHAND);
				unEquipSlot(PAPERDOLL_RHAND);
				pdollSlot = PAPERDOLL_LRHAND;
				break;
			}
			case ItemTemplate.SLOT_BACK:
			{
				pdollSlot = PAPERDOLL_BACK;
				break;
			}
			case ItemTemplate.SLOT_FEET:
			{
				pdollSlot = PAPERDOLL_FEET;
				break;
			}
			case ItemTemplate.SLOT_UNDERWEAR:
			{
				pdollSlot = PAPERDOLL_UNDER;
			}
		}
		unEquipSlot(changedItems, pdollSlot);
		return changedItems;
	}
	
	public Collection<Item> unEquipItemOnPaperdoll(int pdollSlot)
	{
		final List<Item> changedItems = new ArrayList<>();
		if (pdollSlot == 14)
		{
			unEquipSlot(changedItems, PAPERDOLL_LHAND);
			unEquipSlot(PAPERDOLL_RHAND);
		}
		unEquipSlot(changedItems, pdollSlot);
		return changedItems;
	}
	
	public List<Item> equipItem(Item item)
	{
		final List<Item> changedItems = new ArrayList<>();
		final int targetSlot = item.getItem().getBodyPart();
		switch (targetSlot)
		{
			case ItemTemplate.SLOT_LR_HAND:
			{
				Item arrow;
				unEquipSlot(changedItems, PAPERDOLL_LHAND);
				final Item old1 = unEquipSlot(PAPERDOLL_LRHAND);
				if (old1 != null)
				{
					changedItems.add(old1);
					unEquipSlot(PAPERDOLL_RHAND);
					unEquipSlot(changedItems, PAPERDOLL_LHAND);
				}
				else
				{
					unEquipSlot(changedItems, PAPERDOLL_RHAND);
				}
				setPaperdollItem(PAPERDOLL_RHAND, item);
				setPaperdollItem(PAPERDOLL_LRHAND, item);
				if ((((Weapon) item.getItem()).getWeaponType() != Weapon.WEAPON_TYPE_BOW) || ((arrow = findArrowForBow(item.getItem())) == null))
				{
					break;
				}
				setPaperdollItem(PAPERDOLL_LHAND, arrow);
				arrow.setLastChange(2);
				changedItems.add(arrow);
				break;
			}
			case ItemTemplate.SLOT_L_HAND:
			{
				final Item old1 = unEquipSlot(PAPERDOLL_LRHAND);
				if (old1 != null)
				{
					unEquipSlot(changedItems, PAPERDOLL_RHAND);
				}
				unEquipSlot(changedItems, PAPERDOLL_LHAND);
				setPaperdollItem(PAPERDOLL_LHAND, item);
				break;
			}
			case ItemTemplate.SLOT_R_HAND:
			{
				if (unEquipSlot(changedItems, PAPERDOLL_LRHAND))
				{
					unEquipSlot(changedItems, PAPERDOLL_LHAND);
					unEquipSlot(PAPERDOLL_RHAND);
				}
				else
				{
					unEquipSlot(changedItems, PAPERDOLL_RHAND);
				}
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case ItemTemplate.SLOT_R_EAR:
			case ItemTemplate.SLOT_L_EAR:
			case ItemTemplate.SLOT_R_EAR + ItemTemplate.SLOT_L_EAR:
			{
				if (_paperdoll[1] == null)
				{
					setPaperdollItem(PAPERDOLL_LEAR, item);
					break;
				}
				if (_paperdoll[2] == null)
				{
					setPaperdollItem(PAPERDOLL_REAR, item);
					break;
				}
				unEquipSlot(changedItems, PAPERDOLL_LEAR);
				setPaperdollItem(PAPERDOLL_LEAR, item);
				break;
			}
			case ItemTemplate.SLOT_R_FINGER:
			case ItemTemplate.SLOT_L_FINGER:
			case ItemTemplate.SLOT_R_FINGER + ItemTemplate.SLOT_L_FINGER:
			{
				if (_paperdoll[4] == null)
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item);
					break;
				}
				if (_paperdoll[5] == null)
				{
					setPaperdollItem(PAPERDOLL_RFINGER, item);
					break;
				}
				unEquipSlot(changedItems, PAPERDOLL_LFINGER);
				setPaperdollItem(PAPERDOLL_LFINGER, item);
				break;
			}
			case ItemTemplate.SLOT_NECK:
			{
				unEquipSlot(changedItems, PAPERDOLL_NECK);
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			}
			case ItemTemplate.SLOT_FULL_ARMOR:
			{
				unEquipSlot(changedItems, PAPERDOLL_CHEST);
				unEquipSlot(changedItems, PAPERDOLL_LEGS);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			}
			case ItemTemplate.SLOT_CHEST:
			{
				unEquipSlot(changedItems, PAPERDOLL_CHEST);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			}
			case ItemTemplate.SLOT_LEGS:
			{
				final Item chest = getPaperdollItem(10);
				if ((chest != null) && (chest.getItem().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR))
				{
					unEquipSlot(changedItems, PAPERDOLL_CHEST);
				}
				unEquipSlot(changedItems, PAPERDOLL_LEGS);
				setPaperdollItem(PAPERDOLL_LEGS, item);
				break;
			}
			case ItemTemplate.SLOT_FEET:
			{
				unEquipSlot(changedItems, PAPERDOLL_FEET);
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;
			}
			case ItemTemplate.SLOT_GLOVES:
			{
				unEquipSlot(changedItems, PAPERDOLL_GLOVES);
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;
			}
			case ItemTemplate.SLOT_HEAD:
			{
				unEquipSlot(changedItems, PAPERDOLL_HEAD);
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;
			}
			case ItemTemplate.SLOT_UNDERWEAR:
			{
				unEquipSlot(changedItems, PAPERDOLL_UNDER);
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			}
			case ItemTemplate.SLOT_BACK:
			{
				unEquipSlot(changedItems, PAPERDOLL_BACK);
				setPaperdollItem(PAPERDOLL_BACK, item);
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
	
	private Item unEquipSlot(int slot)
	{
		final Item item = _paperdoll[slot];
		if (item != null)
		{
			item.setEquipSlot(-1);
			item.setLastChange(2);
			_paperdoll[slot] = null;
		}
		return item;
	}
	
	private boolean unEquipSlot(List<Item> changedItems, int slot)
	{
		if (slot == -1)
		{
			return false;
		}
		final Item item = _paperdoll[slot];
		if (item != null)
		{
			item.setEquipSlot(-1);
			changedItems.add(item);
			item.setLastChange(2);
			_paperdoll[slot] = null;
		}
		return item != null;
	}
	
	public Item getItem(int objectId)
	{
		for (Item temp : _items)
		{
			if (temp.getObjectId() != objectId)
			{
				continue;
			}
			return temp;
		}
		return null;
	}
	
	public Item getAdenaInstance()
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
	
	public Item destroyItem(int objectId, int count)
	{
		final Item item = getItem(objectId);
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
	
	public Item destroyItemByItemId(int itemId, int count)
	{
		final Item item = findItemByItemId(itemId);
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
	
	public Item dropItem(int objectId, int count)
	{
		final Item oldItem = getItem(objectId);
		return this.dropItem(oldItem, count);
	}
	
	public Item dropItem(Item oldItem, int count)
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
			final Item adena = ItemTable.getInstance().createItem(oldItem.getItemId());
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
		final Item newItem = ItemTable.getInstance().createItem(oldItem.getItemId());
		newItem.setCount(count);
		refreshWeight();
		return newItem;
	}
	
	private void refreshWeight()
	{
		int weight = 0;
		for (Item item : _items)
		{
			weight += item.getItem().getWeight() * item.getCount();
		}
		_totalWeight = weight;
	}
	
	public int getTotalWeight()
	{
		return _totalWeight;
	}
	
	public Item findArrowForBow(ItemTemplate bow)
	{
		int arrowsId = 0;
		switch (bow.getCrystalType())
		{
			case ItemTemplate.CRYSTAL_NONE:
			{
				arrowsId = 17;
				break;
			}
			case ItemTemplate.CRYSTAL_D:
			{
				arrowsId = 1341;
				break;
			}
			case ItemTemplate.CRYSTAL_C:
			{
				arrowsId = 1342;
				break;
			}
			case ItemTemplate.CRYSTAL_B:
			{
				arrowsId = 1343;
				break;
			}
			case ItemTemplate.CRYSTAL_A:
			{
				arrowsId = 1344;
				break;
			}
			case ItemTemplate.CRYSTAL_S:
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
