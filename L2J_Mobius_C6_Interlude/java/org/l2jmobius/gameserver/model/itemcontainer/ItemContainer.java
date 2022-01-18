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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;

/**
 * @author Advi
 */
public abstract class ItemContainer
{
	protected static final Logger LOGGER = Logger.getLogger(ItemContainer.class.getName());
	
	protected final Set<Item> _items = ConcurrentHashMap.newKeySet(1);
	
	protected abstract Creature getOwner();
	
	protected abstract ItemLocation getBaseLocation();
	
	/**
	 * Returns the ownerID of the inventory
	 * @return int
	 */
	public int getOwnerId()
	{
		return getOwner() == null ? 0 : getOwner().getObjectId();
	}
	
	/**
	 * Returns the quantity of items in the inventory
	 * @return int
	 */
	public int getSize()
	{
		return _items.size();
	}
	
	/**
	 * Returns the list of items in inventory
	 * @return Item : items in inventory
	 */
	public Collection<Item> getItems()
	{
		return _items;
	}
	
	/**
	 * Returns the item from inventory by using its <b>itemId</b>
	 * @param itemId : int designating the ID of the item
	 * @return Item designating the item or null if not found in inventory
	 */
	public Item getItemByItemId(int itemId)
	{
		for (Item item : _items)
		{
			if ((item != null) && (item.getItemId() == itemId))
			{
				return item;
			}
		}
		return null;
	}
	
	/**
	 * Returns the item from inventory by using its <b>itemId</b>
	 * @param itemId : int designating the ID of the item
	 * @param itemToIgnore : used during a loop, to avoid returning the same item
	 * @return Item designating the item or null if not found in inventory
	 */
	public Item getItemByItemId(int itemId, Item itemToIgnore)
	{
		for (Item item : _items)
		{
			if ((item != null) && (item.getItemId() == itemId) && !item.equals(itemToIgnore))
			{
				return item;
			}
		}
		return null;
	}
	
	/**
	 * Returns item from inventory by using its <b>objectId</b>
	 * @param objectId : int designating the ID of the object
	 * @return Item designating the item or null if not found in inventory
	 */
	public Item getItemByObjectId(int objectId)
	{
		for (Item item : _items)
		{
			if (item == null)
			{
				_items.remove(item);
				continue;
			}
			
			if (item.getObjectId() == objectId)
			{
				return item;
			}
		}
		return null;
	}
	
	/**
	 * Gets count of item in the inventory
	 * @param itemId : Item to look for
	 * @param enchantLevel : enchant level to match on, or -1 for ANY enchant level
	 * @return int corresponding to the number of items matching the above conditions.
	 */
	public int getInventoryItemCount(int itemId, int enchantLevel)
	{
		int count = 0;
		for (Item item : _items)
		{
			if ((item != null) && (item.getItemId() == itemId) && ((item.getEnchantLevel() == enchantLevel) || (enchantLevel < 0)))
			{
				if (item.isStackable())
				{
					count = item.getCount();
				}
				else
				{
					count++;
				}
			}
		}
		return count;
	}
	
	/**
	 * Adds item to inventory
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be added
	 * @param actor : Player Player requesting the item add
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	public Item addItem(String process, Item item, Player actor, WorldObject reference)
	{
		Item newItem = item;
		final Item olditem = getItemByItemId(newItem.getItemId());
		
		// If stackable item is found in inventory just add to current quantity
		if ((olditem != null) && olditem.isStackable())
		{
			final int count = newItem.getCount();
			olditem.changeCount(process, count, actor, reference);
			olditem.setLastChange(Item.MODIFIED);
			
			// And destroys the item
			ItemTable.getInstance().destroyItem(process, newItem, actor, reference);
			newItem.updateDatabase();
			newItem = olditem;
		}
		else // If item hasn't be found in inventory, create new one
		{
			newItem.setOwnerId(process, getOwnerId(), actor, reference);
			newItem.setLocation(getBaseLocation());
			newItem.setLastChange(Item.ADDED);
			
			// Add item in inventory
			addItem(newItem);
		}
		
		refreshWeight();
		return newItem;
	}
	
	/**
	 * Adds item to inventory
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : int Quantity of items to be added
	 * @param actor : Player Player requesting the item add
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	public Item addItem(String process, int itemId, int count, Player actor, WorldObject reference)
	{
		Item item = getItemByItemId(itemId);
		
		// If stackable item is found in inventory just add to current quantity
		if ((item != null) && item.isStackable())
		{
			item.changeCount(process, count, actor, reference);
			item.setLastChange(Item.MODIFIED);
		}
		else // If item hasn't be found in inventory, create new one
		{
			for (int i = 0; i < count; i++)
			{
				final ItemTemplate template = ItemTable.getInstance().getTemplate(itemId);
				if (template == null)
				{
					LOGGER.warning((actor != null ? "[" + actor.getName() + "] " : "") + "Invalid ItemId requested: " + itemId);
					return null;
				}
				
				item = ItemTable.getInstance().createItem(process, itemId, template.isStackable() ? count : 1, actor, reference);
				item.setOwnerId(getOwnerId());
				
				if (process.equals("AutoLoot"))
				{
					item.setLocation(ItemLocation.INVENTORY);
				}
				else
				{
					item.setLocation(getBaseLocation());
				}
				
				item.setLastChange(Item.ADDED);
				
				// Add item in inventory
				addItem(item);
				
				// If stackable, end loop as entire count is included in 1 instance of item
				if (template.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				{
					break;
				}
			}
		}
		
		refreshWeight();
		return item;
	}
	
	/**
	 * Adds Wear/Try On item to inventory
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param actor : Player Player requesting the item add
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new weared item
	 */
	public Item addWearItem(String process, int itemId, Player actor, WorldObject reference)
	{
		// Surch the item in the inventory of the player
		Item item = getItemByItemId(itemId);
		
		// There is such item already in inventory
		if (item != null)
		{
			return item;
		}
		
		// Create and Init the Item corresponding to the Item Identifier and quantity
		// Add the Item object to _allObjects of L2world
		item = ItemTable.getInstance().createItem(process, itemId, 1, actor, reference);
		
		// Set Item Properties
		item.setWear(true); // "Try On" Item -> Don't save it in database
		item.setOwnerId(getOwnerId());
		item.setLocation(getBaseLocation());
		item.setLastChange(Item.ADDED);
		
		// Add item in inventory and equip it if necessary (item location defined)
		addItem(item);
		
		// Calculate the weight loaded by player
		refreshWeight();
		
		return item;
	}
	
	/**
	 * Transfers item to another inventory
	 * @param process : String Identifier of process triggering this action
	 * @param objectId
	 * @param amount : int Quantity of items to be transfered
	 * @param target
	 * @param actor : Player Player requesting the item transfer
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	public Item transferItem(String process, int objectId, int amount, ItemContainer target, Player actor, WorldObject reference)
	{
		if (target == null)
		{
			return null;
		}
		
		final Item sourceitem = getItemByObjectId(objectId);
		if (sourceitem == null)
		{
			return null;
		}
		
		Item targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getItemId()) : null;
		synchronized (sourceitem)
		{
			// check if this item still present in this container
			if (getItemByObjectId(objectId) != sourceitem)
			{
				return null;
			}
			
			// Check if requested quantity is available
			int count = amount;
			if (count > sourceitem.getCount())
			{
				count = sourceitem.getCount();
			}
			
			// If possible, move entire item object
			if ((sourceitem.getCount() == count) && (targetitem == null))
			{
				removeItem(sourceitem);
				target.addItem(process, sourceitem, actor, reference);
				targetitem = sourceitem;
			}
			else
			{
				if (sourceitem.getCount() > count) // If possible, only update counts
				{
					sourceitem.changeCount(process, -count, actor, reference);
				}
				else // Otherwise destroy old item
				{
					removeItem(sourceitem);
					ItemTable.getInstance().destroyItem(process, sourceitem, actor, reference);
				}
				
				if (targetitem != null) // If possible, only update counts
				{
					targetitem.changeCount(process, count, actor, reference);
				}
				else // Otherwise add new item
				{
					targetitem = target.addItem(process, sourceitem.getItemId(), count, actor, reference);
				}
			}
			
			// Updates database
			sourceitem.updateDatabase();
			if ((targetitem != sourceitem) && (targetitem != null))
			{
				targetitem.updateDatabase();
			}
			if (sourceitem.isAugmented())
			{
				sourceitem.getAugmentation().removeBonus(actor);
			}
			refreshWeight();
			target.refreshWeight();
		}
		return targetitem;
	}
	
	/**
	 * Destroy item from inventory and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item destroyItem(String process, Item item, Player actor, WorldObject reference)
	{
		synchronized (item)
		{
			// check if item is present in this container
			if (!_items.contains(item))
			{
				return null;
			}
			
			removeItem(item);
			ItemTable.getInstance().destroyItem(process, item, actor, reference);
			item.updateDatabase();
			
			if (item.isVarkaKetraAllyQuestItem())
			{
				actor.setAllianceWithVarkaKetra(0);
			}
			
			refreshWeight();
		}
		
		return item;
	}
	
	/**
	 * Destroy item from inventory by using its <b>objectID</b> and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item destroyItem(String process, int objectId, int count, Player actor, WorldObject reference)
	{
		final Item item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		
		// Adjust item quantity
		if (item.getCount() > count)
		{
			synchronized (item)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(Item.MODIFIED);
				
				item.updateDatabase();
				refreshWeight();
			}
			
			return item;
		}
		// Directly drop entire item
		return destroyItem(process, item, actor, reference);
	}
	
	/**
	 * Destroy item from inventory by using its <b>itemId</b> and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item destroyItemByItemId(String process, int itemId, int count, Player actor, WorldObject reference)
	{
		final Item item = getItemByItemId(itemId);
		if (item == null)
		{
			return null;
		}
		
		synchronized (item)
		{
			// Adjust item quantity
			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(Item.MODIFIED);
			}
			else // Directly drop entire item
			{
				return destroyItem(process, item, actor, reference);
			}
			
			item.updateDatabase();
			refreshWeight();
		}
		
		return item;
	}
	
	/**
	 * Destroy all items from inventory and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void destroyAllItems(String process, Player actor, WorldObject reference)
	{
		for (Item item : _items)
		{
			destroyItem(process, item, actor, reference);
		}
	}
	
	/**
	 * Get warehouse adena
	 * @return
	 */
	public int getAdena()
	{
		int count = 0;
		for (Item item : _items)
		{
			if (item.getItemId() == 57)
			{
				count = item.getCount();
				return count;
			}
		}
		return count;
	}
	
	/**
	 * Adds item to inventory for further adjustments.
	 * @param item : Item to be added from inventory
	 */
	protected void addItem(Item item)
	{
		_items.add(item);
	}
	
	/**
	 * Removes item from inventory for further adjustments.
	 * @param item : Item to be removed from inventory
	 */
	protected void removeItem(Item item)
	{
		_items.remove(item);
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	protected void refreshWeight()
	{
	}
	
	/**
	 * Delete item object from world
	 */
	public void deleteMe()
	{
		try
		{
			updateDatabase();
		}
		catch (Throwable t)
		{
			LOGGER.warning("deletedMe() " + t);
		}
		
		final List<WorldObject> items = new ArrayList<>(_items);
		_items.clear();
		
		World.getInstance().removeObjects(items);
	}
	
	/**
	 * Update database with items in inventory
	 */
	public void updateDatabase()
	{
		if ((getOwner() != null) && (_items != null))
		{
			for (Item item : _items)
			{
				if (item != null)
				{
					item.updateDatabase();
				}
			}
		}
	}
	
	/**
	 * Get back items in container from database
	 */
	public void restore()
	{
		final int ownerid = getOwnerId();
		final String baseLocation = getBaseLocation().name();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=?) ORDER BY object_id DESC");
			statement.setInt(1, ownerid);
			statement.setString(2, baseLocation);
			final ResultSet inv = statement.executeQuery();
			Item item;
			
			while (inv.next())
			{
				final int objectId = inv.getInt(1);
				item = Item.restoreFromDb(objectId);
				if (item == null)
				{
					continue;
				}
				
				World.getInstance().storeObject(item);
				
				// If stackable item is found in inventory just add to current quantity
				if (item.isStackable() && (getItemByItemId(item.getItemId()) != null))
				{
					addItem("Restore", item, null, getOwner());
				}
				else
				{
					addItem(item);
				}
			}
			
			inv.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("could not restore container: " + e);
		}
		
		refreshWeight();
	}
	
	public boolean validateCapacity(int slots)
	{
		return true;
	}
	
	public boolean validateWeight(int weight)
	{
		return true;
	}
}
