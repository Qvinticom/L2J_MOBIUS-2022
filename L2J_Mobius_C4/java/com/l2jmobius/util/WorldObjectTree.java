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
package com.l2jmobius.util;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.l2jmobius.gameserver.model.L2Object;

/**
 * @author dishkols
 * @param <T>
 */
public class WorldObjectTree<T extends L2Object>extends L2ObjectMap<T>
{
	private final TreeMap<Integer, T> objectMap = new TreeMap<>();
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();
	
	/**
	 * @see com.l2jmobius.util.L2ObjectMap#size()
	 */
	@Override
	public int size()
	{
		r.lock();
		try
		{
			return objectMap.size();
		}
		finally
		{
			r.unlock();
		}
	}
	
	/**
	 * @see com.l2jmobius.util.L2ObjectMap#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		r.lock();
		try
		{
			return objectMap.isEmpty();
		}
		finally
		{
			r.unlock();
		}
	}
	
	/**
	 * @see com.l2jmobius.util.L2ObjectMap#clear()
	 */
	@Override
	public void clear()
	{
		w.lock();
		try
		{
			objectMap.clear();
		}
		finally
		{
			w.unlock();
		}
	}
	
	@Override
	public void put(T obj)
	{
		if (obj != null)
		{
			w.lock();
			try
			{
				objectMap.put(obj.getObjectId(), obj);
			}
			finally
			{
				w.unlock();
			}
		}
	}
	
	@Override
	public void remove(T obj)
	{
		if (obj != null)
		{
			w.lock();
			try
			{
				objectMap.remove(obj.getObjectId());
			}
			finally
			{
				w.unlock();
			}
		}
	}
	
	/**
	 * @see com.l2jmobius.util.L2ObjectMap#get(int)
	 */
	@Override
	public T get(int id)
	{
		r.lock();
		try
		{
			return objectMap.get(id);
		}
		finally
		{
			r.unlock();
		}
	}
	
	@Override
	public boolean contains(T obj)
	{
		if (obj == null)
		{
			return false;
		}
		r.lock();
		try
		{
			return objectMap.containsValue(obj);
		}
		finally
		{
			r.unlock();
		}
	}
	
	/**
	 * @see com.l2jmobius.util.L2ObjectMap#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		r.lock();
		try
		{
			return objectMap.values().iterator();
		}
		finally
		{
			r.unlock();
		}
	}
}