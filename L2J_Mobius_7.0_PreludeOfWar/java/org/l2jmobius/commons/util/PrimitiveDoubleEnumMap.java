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
package org.l2jmobius.commons.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mobius
 */
public class PrimitiveDoubleEnumMap
{
	private Entry[] _array;
	private final int _initialSize;
	private int _size = 0;
	
	private class Entry
	{
		private final Enum<?> key;
		private double value;
		
		public Entry(Enum<?> key, double value)
		{
			this.key = key;
			this.value = value;
		}
	}
	
	public PrimitiveDoubleEnumMap()
	{
		_initialSize = 10;
		_array = new Entry[_initialSize];
	}
	
	public PrimitiveDoubleEnumMap(int size)
	{
		_initialSize = size;
		_array = new Entry[size];
	}
	
	public PrimitiveDoubleEnumMap(PrimitiveDoubleEnumMap map)
	{
		_initialSize = map.getInitialSize();
		_array = new Entry[map.size()];
		
		for (Entry element : map.getEntries())
		{
			if (element != null)
			{
				put(element.key, element.value);
			}
		}
	}
	
	public int size()
	{
		return _size;
	}
	
	public int getInitialSize()
	{
		return _initialSize;
	}
	
	public void clear()
	{
		_size = 0;
		_array = new Entry[_initialSize];
	}
	
	public Entry[] getEntries()
	{
		return _array;
	}
	
	public double get(Enum<?> key)
	{
		return getOrDefault(key, 0);
	}
	
	public double getOrDefault(Enum<?> key, double defaultValue)
	{
		for (Entry element : _array)
		{
			if ((element != null) && (element.key == key))
			{
				return element.value;
			}
		}
		return defaultValue;
	}
	
	public void put(Enum<?> key, double value)
	{
		if (_array.length >= _size)
		{
			for (int i = 0; i < _array.length; i++)
			{
				if (_array[i] == null)
				{
					_size++;
					_array[i] = new Entry(key, value);
				}
			}
		}
		else
		{
			int newPosition = _array.length;
			increaseSize(_array.length); // Double size.
			_size++;
			_array[newPosition] = new Entry(key, value);
		}
	}
	
	public void increaseSize(int count)
	{
		Entry[] temp = new Entry[_array.length + count];
		for (int i = 0; i < _array.length; i++)
		{
			temp[i] = _array[i];
		}
		_array = temp;
		temp = null;
	}
	
	public void remove(Enum<?> key)
	{
		for (int i = 0; i < _array.length; i++)
		{
			if (_array[i] != null)
			{
				_size--;
				_array[i] = null;
			}
		}
	}
	
	public Set<Enum<?>> keySet()
	{
		final Set<Enum<?>> result = new HashSet<>(_size);
		for (Entry element : _array)
		{
			if (element != null)
			{
				result.add(element.key);
			}
		}
		return result;
	}
	
	public double[] values()
	{
		double[] result = new double[_size];
		int position = 0;
		for (Entry element : _array)
		{
			if (element != null)
			{
				result[position++] = element.value;
			}
		}
		return result;
	}
	
	public boolean containsKey(Enum<?> key)
	{
		for (Entry element : _array)
		{
			if ((element != null) && (element.key == key))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean containsValue(double value)
	{
		for (Entry element : _array)
		{
			if ((element != null) && (element.value == value))
			{
				return true;
			}
		}
		return false;
	}
	
	public void mergeAdd(Enum<?> key, double value)
	{
		// Existing key.
		for (Entry element : _array)
		{
			if ((element != null) && (element.key == key))
			{
				element.value += value;
				return;
			}
		}
		// Non existing key.
		put(key, value);
	}
	
	public void mergeMul(Enum<?> key, double value)
	{
		// Existing key.
		for (Entry element : _array)
		{
			if ((element != null) && (element.key == key))
			{
				element.value *= value;
				return;
			}
		}
		// Non existing key.
		put(key, value);
	}
}
