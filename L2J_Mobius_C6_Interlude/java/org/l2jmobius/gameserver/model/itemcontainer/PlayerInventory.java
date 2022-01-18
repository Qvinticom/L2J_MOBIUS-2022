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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.TradeList.TradeItem;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;

public class PlayerInventory extends Inventory
{
	public static final int ADENA_ID = 57;
	public static final int ANCIENT_ADENA_ID = 5575;
	
	private final Player _owner;
	private Item _adena;
	private Item _ancientAdena;
	
	public PlayerInventory(Player owner)
	{
		_owner = owner;
	}
	
	@Override
	public Player getOwner()
	{
		return _owner;
	}
	
	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.INVENTORY;
	}
	
	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PAPERDOLL;
	}
	
	public Item getAdenaInstance()
	{
		return _adena;
	}
	
	@Override
	public int getAdena()
	{
		return _adena != null ? _adena.getCount() : 0;
	}
	
	public Item getAncientAdenaInstance()
	{
		return _ancientAdena;
	}
	
	public int getAncientAdena()
	{
		return _ancientAdena != null ? _ancientAdena.getCount() : 0;
	}
	
	/**
	 * Returns the list of items in inventory available for transaction
	 * @param allowAdena
	 * @param allowAncientAdena
	 * @param allowEquipped
	 * @return Item : items in inventory
	 */
	public List<Item> getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean allowEquipped)
	{
		return getUniqueItems(allowAdena, allowAncientAdena, true, allowEquipped);
	}
	
	public List<Item> getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable, boolean allowEquipped)
	{
		final List<Item> list = new LinkedList<>();
		for (Item item : _items)
		{
			if (!allowAdena && (item.getItemId() == 57))
			{
				continue;
			}
			if (!allowAncientAdena && (item.getItemId() == 5575))
			{
				continue;
			}
			
			boolean isDuplicate = false;
			for (Item litem : list)
			{
				if ((litem.getItemId() == item.getItemId()) && item.isStackable()) // to duplicate more not stackable item
				{
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate && (!onlyAvailable || (item.getItem().isSellable() && item.isAvailable(_owner, false, allowEquipped))))
			{
				list.add(item);
			}
		}
		
		return list;
	}
	
	/**
	 * Returns the list of items in inventory available for transaction Allows an item to appear twice if and only if there is a difference in enchantment level.
	 * @param allowAdena
	 * @param allowAncientAdena
	 * @param allowEquipped
	 * @return Item : items in inventory
	 */
	public List<Item> getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean allowEquipped)
	{
		return getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true, allowEquipped);
	}
	
	public List<Item> getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable, boolean allowEquipped)
	{
		final List<Item> list = new LinkedList<>();
		for (Item item : _items)
		{
			if (!allowAdena && (item.getItemId() == 57))
			{
				continue;
			}
			if (!allowAncientAdena && (item.getItemId() == 5575))
			{
				continue;
			}
			
			boolean isDuplicate = false;
			for (Item litem : list)
			{
				if ((litem.getItemId() == item.getItemId()) && (litem.getEnchantLevel() == item.getEnchantLevel()))
				{
					isDuplicate = true;
					break;
				}
			}
			
			if (!isDuplicate && (!onlyAvailable || (item.getItem().isSellable() && item.isAvailable(_owner, false, allowEquipped))))
			{
				list.add(item);
			}
		}
		
		return list;
	}
	
	/**
	 * Returns the list of all items in inventory that have a given item id.
	 * @param itemId
	 * @return List<Item> : matching items from inventory
	 */
	public List<Item> getAllItemsByItemId(int itemId)
	{
		final List<Item> list = new LinkedList<>();
		for (Item item : _items)
		{
			if (item.getItemId() == itemId)
			{
				list.add(item);
			}
		}
		return list;
	}
	
	/**
	 * Returns the list of all items in inventory that have a given item id AND a given enchantment level.
	 * @param itemId
	 * @param enchantment
	 * @return List<Item> : matching items from inventory
	 */
	public List<Item> getAllItemsByItemId(int itemId, int enchantment)
	{
		final List<Item> list = new LinkedList<>();
		for (Item item : _items)
		{
			if ((item.getItemId() == itemId) && (item.getEnchantLevel() == enchantment))
			{
				list.add(item);
			}
		}
		return list;
	}
	
	/**
	 * Returns the list of items in inventory available for transaction
	 * @param allowAdena
	 * @return Item : items in inventory
	 */
	public List<Item> getAvailableItems(boolean allowAdena)
	{
		final List<Item> list = new LinkedList<>();
		for (Item item : _items)
		{
			if ((item != null) && item.isAvailable(_owner, allowAdena, false))
			{
				list.add(item);
			}
		}
		return list;
	}
	
	/**
	 * Get all augmented items
	 * @return
	 */
	public List<Item> getAugmentedItems()
	{
		final List<Item> list = new LinkedList<>();
		for (Item item : _items)
		{
			if ((item != null) && item.isAugmented())
			{
				list.add(item);
			}
		}
		return list;
	}
	
	/**
	 * Returns the list of items in inventory available for transaction adjusted by tradeList
	 * @param tradeList
	 * @return Item : items in inventory
	 */
	public List<TradeItem> getAvailableItems(TradeList tradeList)
	{
		final List<TradeItem> list = new LinkedList<>();
		for (Item item : _items)
		{
			if (item.isAvailable(_owner, false, false))
			{
				final TradeItem adjItem = tradeList.adjustAvailableItem(item);
				if (adjItem != null)
				{
					list.add(adjItem);
				}
			}
		}
		return list;
	}
	
	/**
	 * Adjust TradeItem according his status in inventory
	 * @param item : Item to be adjusted
	 * @param list
	 * @return
	 */
	public TradeItem adjustAvailableItem(TradeItem item, List<TradeItem> list)
	{
		for (Item adjItem : _items)
		{
			if (adjItem.isStackable())
			{
				if ((adjItem.getItemId() == item.getItem().getItemId()) && (adjItem.getEnchantLevel() == item.getEnchant()))
				{
					item.setObjectId(adjItem.getObjectId());
					
					if (adjItem.getCount() < item.getCount())
					{
						item.setCurCount(adjItem.getCount());
					}
					else
					{
						item.setCurCount(item.getCount());
					}
					return item;
				}
			}
			else if ((adjItem.getItemId() == item.getItem().getItemId()) && (adjItem.getEnchantLevel() == item.getEnchant()))
			{
				boolean found = false;
				for (TradeItem actual : list)
				{
					if (actual.getObjectId() == adjItem.getObjectId())
					{
						found = true;
						break;
					}
				}
				
				if (found)
				{
					continue;
				}
				
				item.setObjectId(adjItem.getObjectId());
				if (adjItem.getCount() < item.getCount())
				{
					item.setCurCount(adjItem.getCount());
				}
				else
				{
					item.setCurCount(item.getCount());
				}
				return item;
			}
		}
		item.setCurCount(0);
		return item;
	}
	
	/**
	 * Adds adena to PCInventory
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be added
	 * @param actor : Player Player requesting the item add
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void addAdena(String process, int count, Player actor, WorldObject reference)
	{
		if (count > 0)
		{
			addItem(process, ADENA_ID, count, actor, reference);
		}
	}
	
	/**
	 * Removes adena to PCInventory
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be removed
	 * @param actor : Player Player requesting the item add
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void reduceAdena(String process, int count, Player actor, WorldObject reference)
	{
		if (count > 0)
		{
			destroyItemByItemId(process, ADENA_ID, count, actor, reference);
		}
	}
	
	/**
	 * Adds specified amount of ancient adena to player inventory.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be added
	 * @param actor : Player Player requesting the item add
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void addAncientAdena(String process, int count, Player actor, WorldObject reference)
	{
		if (count > 0)
		{
			addItem(process, ANCIENT_ADENA_ID, count, actor, reference);
		}
	}
	
	/**
	 * Removes specified amount of ancient adena from player inventory.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be removed
	 * @param actor : Player Player requesting the item add
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void reduceAncientAdena(String process, int count, Player actor, WorldObject reference)
	{
		if (count > 0)
		{
			destroyItemByItemId(process, ANCIENT_ADENA_ID, count, actor, reference);
		}
	}
	
	/**
	 * Adds item in inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be added
	 * @param actor : Player Player requesting the item add
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	@Override
	public Item addItem(String process, Item item, Player actor, WorldObject reference)
	{
		Item addedItem = super.addItem(process, item, actor, reference);
		
		if ((addedItem != null) && (addedItem.getItemId() == ADENA_ID) && !addedItem.equals(_adena))
		{
			_adena = addedItem;
		}
		
		if ((addedItem != null) && (addedItem.getItemId() == ANCIENT_ADENA_ID) && !addedItem.equals(_ancientAdena))
		{
			_ancientAdena = addedItem;
		}
		
		return addedItem;
	}
	
	/**
	 * Adds item in inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : int Quantity of items to be added
	 * @param actor : Player Player requesting the item creation
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	@Override
	public Item addItem(String process, int itemId, int count, Player actor, WorldObject reference)
	{
		final Item item = super.addItem(process, itemId, count, actor, reference);
		
		if ((item != null) && (item.getItemId() == ADENA_ID) && !item.equals(_adena))
		{
			_adena = item;
		}
		
		if ((item != null) && (item.getItemId() == ANCIENT_ADENA_ID) && !item.equals(_ancientAdena))
		{
			_ancientAdena = item;
		}
		
		return item;
	}
	
	/**
	 * Transfers item to another inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Identifier of the item to be transfered
	 * @param count : int Quantity of items to be transfered
	 * @param actor : Player Player requesting the item transfer
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	@Override
	public Item transferItem(String process, int objectId, int count, ItemContainer target, Player actor, WorldObject reference)
	{
		final Item item = super.transferItem(process, objectId, count, target, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		return item;
	}
	
	/**
	 * Destroy item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public Item destroyItem(String process, Item item, Player actor, WorldObject reference)
	{
		final Item destroyedItem = super.destroyItem(process, item, actor, reference);
		
		if ((_adena != null) && (_adena.getCount() <= 0))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && (_ancientAdena.getCount() <= 0))
		{
			_ancientAdena = null;
		}
		
		return destroyedItem;
	}
	
	/**
	 * Destroys item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public Item destroyItem(String process, int objectId, int count, Player actor, WorldObject reference)
	{
		final Item item = super.destroyItem(process, objectId, count, actor, reference);
		
		if ((_adena != null) && (_adena.getCount() <= 0))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && (_ancientAdena.getCount() <= 0))
		{
			_ancientAdena = null;
		}
		
		return item;
	}
	
	/**
	 * Destroy item from inventory by using its <b>itemId</b> and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public Item destroyItemByItemId(String process, int itemId, int count, Player actor, WorldObject reference)
	{
		final Item item = super.destroyItemByItemId(process, itemId, count, actor, reference);
		
		if ((_adena != null) && (_adena.getCount() <= 0))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && (_ancientAdena.getCount() <= 0))
		{
			_ancientAdena = null;
		}
		
		return item;
	}
	
	/**
	 * Drop item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be dropped
	 * @param actor : Player Player requesting the item drop
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public Item dropItem(String process, Item item, Player actor, WorldObject reference)
	{
		final Item droppedItem = super.dropItem(process, item, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		return droppedItem;
	}
	
	/**
	 * Drop item from inventory by using its <b>objectID</b> and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : int Quantity of items to be dropped
	 * @param actor : Player Player requesting the item drop
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public Item dropItem(String process, int objectId, int count, Player actor, WorldObject reference)
	{
		final Item item = super.dropItem(process, objectId, count, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		return item;
	}
	
	/**
	 * <b>Overloaded</b>, when removes item from inventory, remove also owner shortcuts.
	 * @param item : Item to be removed from inventory
	 */
	@Override
	protected void removeItem(Item item)
	{
		// Removes any reference to the item from Shortcut bar
		_owner.removeItemFromShortCut(item.getObjectId());
		
		// Removes active Enchant Scroll
		if (item.equals(_owner.getActiveEnchantItem()))
		{
			_owner.setActiveEnchantItem(null);
		}
		
		if (item.getItemId() == ADENA_ID)
		{
			_adena = null;
		}
		else if (item.getItemId() == ANCIENT_ADENA_ID)
		{
			_ancientAdena = null;
		}
		
		super.removeItem(item);
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		_owner.refreshOverloaded();
	}
	
	/**
	 * Get back items in inventory from database
	 */
	@Override
	public void restore()
	{
		super.restore();
		_adena = getItemByItemId(ADENA_ID);
		_ancientAdena = getItemByItemId(ANCIENT_ADENA_ID);
	}
	
	public static int[][] restoreVisibleInventory(int objectId)
	{
		final int[][] paperdoll = new int[0x12][3];
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement2 = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
			statement2.setInt(1, objectId);
			final ResultSet invdata = statement2.executeQuery();
			
			while (invdata.next())
			{
				final int slot = invdata.getInt("loc_data");
				paperdoll[slot][0] = invdata.getInt("object_id");
				paperdoll[slot][1] = invdata.getInt("item_id");
				paperdoll[slot][2] = invdata.getInt("enchant_level");
			}
			
			invdata.close();
			statement2.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore inventory: " + e);
		}
		return paperdoll;
	}
	
	public boolean validateCapacity(Item item)
	{
		int slots = 0;
		if ((!item.isStackable() || (getItemByItemId(item.getItemId()) == null)) && (item.getItemType() != EtcItemType.HERB))
		{
			slots++;
		}
		return validateCapacity(slots);
	}
	
	public boolean validateCapacity(List<Item> items)
	{
		int slots = 0;
		for (Item item : items)
		{
			if ((!item.isStackable() || (getItemByItemId(item.getItemId()) == null)))
			{
				slots++;
			}
		}
		return validateCapacity(slots);
	}
	
	public boolean validateCapacityByItemId(int itemId)
	{
		int slots = 0;
		final Item invItem = getItemByItemId(itemId);
		if (((invItem == null) || !invItem.isStackable()))
		{
			slots++;
		}
		return validateCapacity(slots);
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots) <= _owner.getInventoryLimit();
	}
	
	@Override
	public boolean validateWeight(int weight)
	{
		return (_totalWeight + weight) <= _owner.getMaxLoad();
	}
	
	public boolean validateCapacity(ItemTemplate item)
	{
		int slots = 0;
		if ((!item.isStackable() || (getItemByItemId(item.getItemId()) == null)) && (item.getItemType() != EtcItemType.HERB))
		{
			slots++;
		}
		return validateCapacity(slots);
	}
	
	public boolean checkIfEquipped(int itemId)
	{
		if (_items == null)
		{
			return false;
		}
		
		for (Item item : _items)
		{
			if (item.isEquipped() && (item.getItemId() == itemId))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public int checkHowManyEquipped(int itemId)
	{
		if (_items == null)
		{
			return 0;
		}
		
		int count = 0;
		for (Item item : _items)
		{
			if (item.isEquipped() && (item.getItemId() == itemId))
			{
				count++;
			}
		}
		
		return count;
	}
}
