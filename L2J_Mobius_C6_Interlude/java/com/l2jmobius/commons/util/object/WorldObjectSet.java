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
package com.l2jmobius.commons.util.object;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jmobius.gameserver.model.L2Object;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @param <T>
 */
public class WorldObjectSet<T extends L2Object>extends L2ObjectSet<T>
{
	private final Map<Integer, T> _objectMap;
	
	public WorldObjectSet()
	{
		_objectMap = new ConcurrentHashMap<>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#size()
	 */
	@Override
	public int size()
	{
		return _objectMap.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return _objectMap.isEmpty();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#clear()
	 */
	@Override
	public void clear()
	{
		_objectMap.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#put(T)
	 */
	@Override
	public void put(T obj)
	{
		_objectMap.put(obj.getObjectId(), obj);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#remove(T)
	 */
	@Override
	public void remove(T obj)
	{
		_objectMap.remove(obj.getObjectId());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#contains(T)
	 */
	@Override
	public boolean contains(T obj)
	{
		return _objectMap.containsKey(obj.getObjectId());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return _objectMap.values().iterator();
	}
}
