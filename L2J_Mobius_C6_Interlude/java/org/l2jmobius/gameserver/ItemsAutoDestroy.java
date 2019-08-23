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
package org.l2jmobius.gameserver;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.items.type.EtcItemType;

public class ItemsAutoDestroy
{
	protected static final Logger LOGGER = Logger.getLogger(ItemsAutoDestroy.class.getName());
	private static ItemsAutoDestroy _instance;
	protected Collection<ItemInstance> _items = null;
	protected static long _sleep;
	
	private ItemsAutoDestroy()
	{
		_items = ConcurrentHashMap.newKeySet();
		_sleep = Config.AUTODESTROY_ITEM_AFTER * 1000;
		if (_sleep == 0)
		{
			_sleep = 3600000;
		}
		ThreadPool.scheduleAtFixedRate(new CheckItemsForDestroy(), 5000, 5000);
	}
	
	public static ItemsAutoDestroy getInstance()
	{
		if (_instance == null)
		{
			LOGGER.info("Initializing ItemsAutoDestroy.");
			_instance = new ItemsAutoDestroy();
		}
		return _instance;
	}
	
	public synchronized void addItem(ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}
	
	public synchronized void removeItems()
	{
		if (_items.isEmpty())
		{
			return;
		}
		
		final long curtime = System.currentTimeMillis();
		
		for (ItemInstance item : _items)
		{
			if ((item == null) || (item.getDropTime() == 0) || (item.getItemLocation() != ItemInstance.ItemLocation.VOID))
			{
				_items.remove(item);
			}
			else if (item.getItemType() == EtcItemType.HERB)
			{
				if ((curtime - item.getDropTime()) > Config.HERB_AUTO_DESTROY_TIME)
				{
					World.getInstance().removeVisibleObject(item, item.getWorldRegion());
					World.getInstance().removeObject(item);
					_items.remove(item);
					
					if (Config.SAVE_DROPPED_ITEM)
					{
						ItemsOnGroundManager.getInstance().removeObject(item);
					}
				}
			}
			else if ((curtime - item.getDropTime()) > _sleep)
			{
				World.getInstance().removeVisibleObject(item, item.getWorldRegion());
				World.getInstance().removeObject(item);
				_items.remove(item);
				
				if (Config.SAVE_DROPPED_ITEM)
				{
					ItemsOnGroundManager.getInstance().removeObject(item);
				}
			}
		}
	}
	
	protected class CheckItemsForDestroy extends Thread
	{
		@Override
		public void run()
		{
			removeItems();
		}
	}
}
