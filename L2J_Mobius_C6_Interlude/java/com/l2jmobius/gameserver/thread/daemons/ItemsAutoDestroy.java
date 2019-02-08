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
package com.l2jmobius.gameserver.thread.daemons;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.templates.item.L2EtcItemType;

public class ItemsAutoDestroy
{
	protected static final Logger LOGGER = Logger.getLogger(ItemsAutoDestroy.class.getName());
	private static ItemsAutoDestroy _instance;
	protected List<L2ItemInstance> _items = null;
	protected static long _sleep;
	
	private ItemsAutoDestroy()
	{
		_items = new CopyOnWriteArrayList<>();
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
	
	public synchronized void addItem(L2ItemInstance item)
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
		
		for (L2ItemInstance item : _items)
		{
			if ((item == null) || (item.getDropTime() == 0) || (item.getLocation() != L2ItemInstance.ItemLocation.VOID))
			{
				_items.remove(item);
			}
			else if (item.getItemType() == L2EtcItemType.HERB)
			{
				if ((curtime - item.getDropTime()) > Config.HERB_AUTO_DESTROY_TIME)
				{
					L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
					L2World.getInstance().removeObject(item);
					_items.remove(item);
					
					if (Config.SAVE_DROPPED_ITEM)
					{
						ItemsOnGroundManager.getInstance().removeObject(item);
					}
				}
			}
			else if ((curtime - item.getDropTime()) > _sleep)
			{
				L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
				L2World.getInstance().removeObject(item);
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
