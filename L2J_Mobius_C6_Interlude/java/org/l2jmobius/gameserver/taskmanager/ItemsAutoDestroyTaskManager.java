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
package org.l2jmobius.gameserver.taskmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;

public class ItemsAutoDestroyTaskManager implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(ItemsAutoDestroyTaskManager.class.getName());
	
	private final Set<Item> _items = ConcurrentHashMap.newKeySet();
	
	protected ItemsAutoDestroyTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 5000, 5000);
	}
	
	@Override
	public void run()
	{
		if (_items.isEmpty())
		{
			return;
		}
		
		final long curtime = Chronos.currentTimeMillis();
		for (Item item : _items)
		{
			if ((item == null) || (item.getDropTime() == 0) || (item.getItemLocation() != Item.ItemLocation.VOID))
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
			else if ((curtime - item.getDropTime()) > (Config.AUTODESTROY_ITEM_AFTER * 1000))
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
	
	public void addItem(Item item)
	{
		item.setDropTime(Chronos.currentTimeMillis());
		_items.add(item);
	}
	
	public static ItemsAutoDestroyTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsAutoDestroyTaskManager INSTANCE = new ItemsAutoDestroyTaskManager();
	}
}
