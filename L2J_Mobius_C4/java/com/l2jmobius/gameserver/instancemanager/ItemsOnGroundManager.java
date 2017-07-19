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
package com.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.ItemsAutoDestroy;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;

import javolution.util.FastList;

/**
 * This class manage all items on ground
 * @version $Revision: $ $Date: $
 * @author DiezelMax - original ideea
 * @author Enforcer - actual build
 */
public class ItemsOnGroundManager
{
	static final Logger _log = Logger.getLogger(ItemsOnGroundManager.class.getName());
	private static ItemsOnGroundManager _Instance;
	protected List<L2ItemInstance> _items = null;
	
	private ItemsOnGroundManager()
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		_items = new FastList<>();
		if (Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new storeInDb(), Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
		}
	}
	
	public static final ItemsOnGroundManager getInstance()
	{
		if (_Instance == null)
		{
			_Instance = new ItemsOnGroundManager();
			_Instance.load();
		}
		return _Instance;
	}
	
	private void load()
	{
		// If SaveDroppedItem is false, may want to delete all items previously stored to avoid add old items on reactivate
		if (!Config.SAVE_DROPPED_ITEM && Config.CLEAR_DROPPED_ITEM_TABLE)
		{
			emptyTable();
		}
		
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		// if DestroyPlayerDroppedItem was previously false, items curently protected will be added to ItemsAutoDestroy
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			String str = null;
			if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				str = "update Itemsonground set drop_time=? where drop_time=-1 and equipable=0";
			}
			else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				str = "update Itemsonground set drop_time=? where drop_time=-1";
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(str))
			{
				statement.setLong(1, System.currentTimeMillis());
				statement.execute();
			}
			catch (final Exception e)
			{
				_log.log(Level.SEVERE, "error while updating table ItemsOnGround " + e);
				e.printStackTrace();
			}
		}
		
		// Add items to world
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet result = s.executeQuery("select object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable from Itemsonground"))
		{
			int count = 0;
			
			while (result.next())
			{
				final L2ItemInstance item = new L2ItemInstance(result.getInt(1), result.getInt(2));
				L2World.getInstance().storeObject(item);
				if (item.isStackable() && (result.getInt(3) > 1))
				{
					item.setCount(result.getInt(3));
				}
				if (result.getInt(4) > 0)
				{
					item.setEnchantLevel(result.getInt(4));
				}
				item.getPosition().setWorldPosition(result.getInt(5), result.getInt(6), result.getInt(7));
				item.getPosition().setWorldRegion(L2World.getInstance().getRegion(item.getPosition().getWorldPosition()));
				item.getPosition().getWorldRegion().addVisibleObject(item);
				item.setDropTime(result.getLong(8));
				if (result.getLong(8) == -1)
				{
					item.setProtected(true);
				}
				else
				{
					item.setProtected(false);
				}
				item.setIsVisible(true);
				
				L2World.getInstance().addVisibleObject(item, item.getPosition().getWorldRegion(), null);
				_items.add(item);
				count++;
				
				// add to ItemsAutoDestroy only items not protected
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
				{
					
					if (result.getLong(8) > -1)
					{
						if (Config.AUTODESTROY_ITEM_AFTER > 0)
						{
							ItemsAutoDestroy.getInstance().addItem(item);
						}
					}
				}
			}
			
			if (count > 0)
			{
				System.out.println("ItemsOnGroundManager: restored " + count + " items.");
			}
			else
			{
				System.out.println("Initializing ItemsOnGroundManager.");
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "error while loading ItemsOnGround " + e);
			e.printStackTrace();
		}
		
		if (Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
		{
			emptyTable();
		}
	}
	
	public void Save(L2ItemInstance item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		_items.add(item);
	}
	
	public void removeObject(L2Object item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		_items.remove(item);
	}
	
	public void saveInDb()
	{
		new storeInDb().run();
	}
	
	public void cleanUp()
	{
		_items.clear();
	}
	
	public void emptyTable()
	{
		try (Connection conn = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement del = conn.prepareStatement("delete from Itemsonground"))
		{
			del.execute();
		}
		catch (final Exception e1)
		{
			_log.log(Level.SEVERE, "error while cleaning table ItemsOnGround " + e1);
			e1.printStackTrace();
		}
	}
	
	protected class storeInDb extends Thread
	{
		@Override
		public void run()
		{
			if (!Config.SAVE_DROPPED_ITEM)
			{
				return;
			}
			
			emptyTable();
			
			if (_items.isEmpty())
			{
				if (Config.DEBUG)
				{
					_log.warning("ItemsOnGroundManager: nothing to save...");
				}
				return;
			}
			
			for (final L2ItemInstance item : _items)
			{
				if (item == null)
				{
					continue;
				}
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("insert into Itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) values(?,?,?,?,?,?,?,?,?)"))
				{
					statement.setInt(1, item.getObjectId());
					statement.setInt(2, item.getItemId());
					statement.setInt(3, item.getCount());
					statement.setInt(4, item.getEnchantLevel());
					statement.setInt(5, item.getX());
					statement.setInt(6, item.getY());
					statement.setInt(7, item.getZ());
					if (item.isProtected())
					{
						statement.setLong(8, -1); // item will be protected
					}
					else
					{
						statement.setLong(8, item.getDropTime()); // item will be added to ItemsAutoDestroy
					}
					if (item.isEquipable())
					{
						statement.setLong(9, 1); // set equipable
					}
					else
					{
						statement.setLong(9, 0);
					}
					statement.execute();
				}
				catch (final Exception e)
				{
					_log.log(Level.SEVERE, "error while inserting into table ItemsOnGround " + e);
					e.printStackTrace();
				}
			}
			
			if (Config.DEBUG)
			{
				_log.warning("ItemsOnGroundManager: " + _items.size() + " items on ground saved");
			}
		}
	}
}