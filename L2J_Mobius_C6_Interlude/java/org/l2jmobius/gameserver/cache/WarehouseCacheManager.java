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
package org.l2jmobius.gameserver.cache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author -Nemesiss-
 */
public class WarehouseCacheManager
{
	protected final Map<Player, Long> _cachedWh;
	protected final long _cacheTime;
	
	protected WarehouseCacheManager()
	{
		_cacheTime = Config.WAREHOUSE_CACHE_TIME * 60000; // 60*1000 = 60000
		_cachedWh = new ConcurrentHashMap<>();
		ThreadPool.scheduleAtFixedRate(new CacheScheduler(), 120000, 60000);
	}
	
	public void addCacheTask(Player pc)
	{
		_cachedWh.put(pc, Chronos.currentTimeMillis());
	}
	
	public void remCacheTask(Player pc)
	{
		_cachedWh.remove(pc);
	}
	
	public class CacheScheduler implements Runnable
	{
		@Override
		public void run()
		{
			final long cTime = Chronos.currentTimeMillis();
			for (Entry<Player, Long> entry : _cachedWh.entrySet())
			{
				if ((cTime - entry.getValue().longValue()) > _cacheTime)
				{
					final Player player = entry.getKey();
					player.clearWarehouse();
					_cachedWh.remove(player);
				}
			}
		}
	}
	
	public static WarehouseCacheManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final WarehouseCacheManager INSTANCE = new WarehouseCacheManager();
	}
}
