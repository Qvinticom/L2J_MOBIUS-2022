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
package org.l2jmobius.gameserver.data;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.sql.PetDataTable;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.actor.instance.RaidBoss;
import org.l2jmobius.gameserver.model.items.Armor;
import org.l2jmobius.gameserver.model.items.EtcItem;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.model.items.instance.Item.ItemLocation;
import org.l2jmobius.gameserver.util.DocumentItem;

/**
 * This class serves as a container for all item templates in the game.
 */
public class ItemTable
{
	private static final Logger LOGGER = Logger.getLogger(ItemTable.class.getName());
	private static final Logger LOGGER_ITEMS = Logger.getLogger("item");
	
	private ItemTemplate[] _allTemplates;
	private final Map<Integer, EtcItem> _etcItems;
	private final Map<Integer, Armor> _armors;
	private final Map<Integer, Weapon> _weapons;
	private final List<File> _itemFiles = new ArrayList<>();
	
	private static final Map<String, Integer> _crystalTypes = new HashMap<>();
	static
	{
		_crystalTypes.put("s", ItemTemplate.CRYSTAL_S);
		_crystalTypes.put("a", ItemTemplate.CRYSTAL_A);
		_crystalTypes.put("b", ItemTemplate.CRYSTAL_B);
		_crystalTypes.put("c", ItemTemplate.CRYSTAL_C);
		_crystalTypes.put("d", ItemTemplate.CRYSTAL_D);
		_crystalTypes.put("none", ItemTemplate.CRYSTAL_NONE);
	}
	
	protected ItemTable()
	{
		hashFiles("data/stats/items", _itemFiles);
		_etcItems = new HashMap<>();
		_armors = new HashMap<>();
		_weapons = new HashMap<>();
		load();
	}
	
	private void hashFiles(String dirname, List<File> hash)
	{
		final File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			LOGGER.info("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		final File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml") && !f.getName().startsWith("custom"))
			{
				hash.add(f);
			}
		}
		final File customfile = new File(Config.DATAPACK_ROOT, dirname + "/custom.xml");
		if (customfile.exists())
		{
			hash.add(customfile);
		}
	}
	
	public List<ItemTemplate> loadItems()
	{
		final List<ItemTemplate> list = new ArrayList<>();
		for (File f : _itemFiles)
		{
			final DocumentItem document = new DocumentItem(f);
			document.parse();
			list.addAll(document.getItemList());
		}
		return list;
	}
	
	private void load()
	{
		int highest = 0;
		_armors.clear();
		_etcItems.clear();
		_weapons.clear();
		for (ItemTemplate item : loadItems())
		{
			if (highest < item.getItemId())
			{
				highest = item.getItemId();
			}
			if (item instanceof EtcItem)
			{
				_etcItems.put(item.getItemId(), (EtcItem) item);
			}
			else if (item instanceof Armor)
			{
				_armors.put(item.getItemId(), (Armor) item);
			}
			else
			{
				_weapons.put(item.getItemId(), (Weapon) item);
			}
		}
		buildFastLookupTable(highest);
	}
	
	/**
	 * Builds a variable in which all items are putting in in function of their ID.
	 * @param size
	 */
	private void buildFastLookupTable(int size)
	{
		// Create a FastLookUp Table called _allTemplates of size : value of the highest item ID
		LOGGER.info("Highest item id used: " + size);
		_allTemplates = new ItemTemplate[size + 1];
		
		// Insert armor item in Fast Look Up Table
		for (Armor item : _armors.values())
		{
			_allTemplates[item.getItemId()] = item;
		}
		
		// Insert weapon item in Fast Look Up Table
		for (Weapon item : _weapons.values())
		{
			_allTemplates[item.getItemId()] = item;
		}
		
		// Insert etcItem item in Fast Look Up Table
		for (EtcItem item : _etcItems.values())
		{
			_allTemplates[item.getItemId()] = item;
		}
	}
	
	/**
	 * Returns the item corresponding to the item ID
	 * @param id : int designating the item
	 * @return Item
	 */
	public ItemTemplate getTemplate(int id)
	{
		if (id >= _allTemplates.length)
		{
			return null;
		}
		return _allTemplates[id];
	}
	
	/**
	 * Create the Item corresponding to the Item Identifier and quantitiy add logs the activity. <b><u>Actions</u>:</b>
	 * <li>Create and Init the Item corresponding to the Item Identifier and quantity</li>
	 * <li>Add the Item object to _allObjects of L2world</li>
	 * <li>Logs Item creation according to log settings</li><br>
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be created
	 * @param count : int Quantity of items to be created for stackable items
	 * @param actor : Player Player requesting the item creation
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item
	 */
	public Item createItem(String process, int itemId, int count, Player actor, WorldObject reference)
	{
		// Create and Init the Item corresponding to the Item Identifier
		final Item item = new Item(IdManager.getInstance().getNextId(), itemId);
		
		// create loot schedule also if autoloot is enabled
		if (process.equalsIgnoreCase("loot")/* && !Config.AUTO_LOOT */)
		{
			ScheduledFuture<?> itemLootShedule;
			long delay = 0;
			// if in CommandChannel and was killing a World/RaidBoss
			if ((reference instanceof GrandBoss) || (reference instanceof RaidBoss))
			{
				if ((((Attackable) reference).getFirstCommandChannelAttacked() != null) && ((Attackable) reference).getFirstCommandChannelAttacked().meetRaidWarCondition(reference))
				{
					item.setOwnerId(((Attackable) reference).getFirstCommandChannelAttacked().getChannelLeader().getObjectId());
					delay = 300000;
				}
				else
				{
					delay = 15000;
					item.setOwnerId(actor.getObjectId());
				}
			}
			else
			{
				item.setOwnerId(actor.getObjectId());
				delay = 15000;
			}
			itemLootShedule = ThreadPool.schedule(new ResetOwner(item), delay);
			item.setItemLootShedule(itemLootShedule);
		}
		
		// Add the Item object to _allObjects of L2world
		World.getInstance().storeObject(item);
		
		// Set Item parameters
		if (item.isStackable() && (count > 1))
		{
			item.setCount(count);
		}
		
		if (Config.LOG_ITEMS)
		{
			final LogRecord record = new LogRecord(Level.INFO, "CREATE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				item,
				actor,
				reference
			});
			LOGGER_ITEMS.log(record);
		}
		return item;
	}
	
	public Item createItem(String process, int itemId, int count, Player actor)
	{
		return createItem(process, itemId, count, actor, null);
	}
	
	/**
	 * Returns a dummy (fr = factice) item.<br>
	 * <u><i>Concept :</i></u><br>
	 * Dummy item is created by setting the ID of the object in the world at null value
	 * @param itemId : int designating the item
	 * @return Item designating the dummy item created
	 */
	public Item createDummyItem(int itemId)
	{
		final ItemTemplate item = getTemplate(itemId);
		if (item == null)
		{
			return null;
		}
		
		Item temp = new Item(0, item);
		
		try
		{
			temp = new Item(0, itemId);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// this can happen if the item templates were not initialized
		}
		
		if (temp.getItem() == null)
		{
			LOGGER.warning("ItemTable: Item Template missing for Id: " + itemId);
		}
		
		return temp;
	}
	
	/**
	 * Destroys the Item.<br>
	 * <br>
	 * <b><u>Actions</u>:</b>
	 * <ul>
	 * <li>Sets Item parameters to be unusable</li>
	 * <li>Removes the Item object to _allObjects of L2world</li>
	 * <li>Logs Item deletion according to log settings</li>
	 * </ul>
	 * @param process a string identifier of process triggering this action.
	 * @param item the item instance to be destroyed.
	 * @param actor the player requesting the item destroy.
	 * @param reference the object referencing current action like NPC selling item or previous item in transformation.
	 */
	public void destroyItem(String process, Item item, Player actor, WorldObject reference)
	{
		synchronized (item)
		{
			item.setCount(0);
			item.setOwnerId(0);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(Item.REMOVED);
			
			World.getInstance().removeObject(item);
			IdManager.getInstance().releaseId(item.getObjectId());
			
			// if it's a pet control item, delete the pet as well
			if (PetDataTable.isPetItem(item.getItemId()))
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					final PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
					statement.setInt(1, item.getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("Could not delete pet objectid " + e);
				}
			}
		}
	}
	
	public void reload()
	{
		load();
	}
	
	public Set<Integer> getAllArmorsId()
	{
		return _armors.keySet();
	}
	
	public Set<Integer> getAllWeaponsId()
	{
		return _weapons.keySet();
	}
	
	public int getArraySize()
	{
		return _allTemplates.length;
	}
	
	protected static class ResetOwner implements Runnable
	{
		Item _item;
		
		public ResetOwner(Item item)
		{
			_item = item;
		}
		
		@Override
		public void run()
		{
			_item.setOwnerId(0);
			_item.setItemLootShedule(null);
		}
	}
	
	public static ItemTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemTable INSTANCE = new ItemTable();
	}
}
