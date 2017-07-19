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
package com.l2jmobius.gameserver.cache;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import javolution.util.FastMap;

/**
 * @author -Nemesiss-
 */
public class WarehouseCacheManager
{
	private static WarehouseCacheManager _instance;
	protected final FastMap<L2PcInstance, Long> _CachedWh;
	protected final long _CacheTime;
	
	public static WarehouseCacheManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new WarehouseCacheManager();
		}
		return _instance;
	}
	
	private WarehouseCacheManager()
	{
		_CacheTime = Config.WAREHOUSE_CACHE_TIME * 60 * 1000;
		_CachedWh = new FastMap<L2PcInstance, Long>().shared();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CacheScheduler(), 120000, 60000);
	}
	
	public void addCacheTask(L2PcInstance pc)
	{
		_CachedWh.put(pc, System.currentTimeMillis());
	}
	
	public void remCacheTask(L2PcInstance pc)
	{
		_CachedWh.remove(pc);
	}
	
	public class CacheScheduler implements Runnable
	{
		@Override
		public void run()
		{
			final long cTime = System.currentTimeMillis();
			for (final L2PcInstance pc : _CachedWh.keySet())
			{
				if ((cTime - _CachedWh.get(pc)) > _CacheTime)
				{
					pc.clearWarehouse();
					_CachedWh.remove(pc);
				}
			}
		}
	}
}