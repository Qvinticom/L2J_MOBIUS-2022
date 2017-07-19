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
import java.util.Map;

import com.l2jmobius.gameserver.model.L2Object;

import javolution.util.FastMap;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @param <T>
 */
public class WorldObjectSet<T extends L2Object>extends L2ObjectSet<T>
{
	private final Map<Integer, T> objectMap;
	
	public WorldObjectSet()
	{
		objectMap = new FastMap<Integer, T>().shared();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#size()
	 */
	@Override
	public int size()
	{
		return objectMap.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return objectMap.isEmpty();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#clear()
	 */
	@Override
	public void clear()
	{
		objectMap.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#put(T)
	 */
	@Override
	public void put(T obj)
	{
		objectMap.put(obj.getObjectId(), obj);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#remove(T)
	 */
	@Override
	public void remove(T obj)
	{
		objectMap.remove(obj.getObjectId());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#contains(T)
	 */
	@Override
	public boolean contains(T obj)
	{
		return objectMap.containsKey(obj.getObjectId());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.util.L2ObjectSet#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return objectMap.values().iterator();
	}
	
}