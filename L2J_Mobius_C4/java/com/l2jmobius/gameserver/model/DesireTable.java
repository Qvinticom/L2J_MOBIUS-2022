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
package com.l2jmobius.gameserver.model;

import java.util.Map;

import javolution.util.FastMap;

/**
 * This class ...
 * @version $Revision$ $Date$
 */
public class DesireTable
{
	public static final DesireType[] DEFAULT_DESIRES =
	{
		DesireType.FEAR,
		DesireType.DISLIKE,
		DesireType.HATE,
		DesireType.DAMAGE
	};
	
	public enum DesireType
	{
		FEAR,
		DISLIKE,
		HATE,
		DAMAGE;
	}
	
	class DesireValue
	{
		private float value;
		
		DesireValue()
		{
			this(0f);
		}
		
		DesireValue(Float pValue)
		{
			value = pValue;
		}
		
		public void addValue(float pValue)
		{
			value += pValue;
		}
		
		public float getValue()
		{
			return value;
		}
	}
	
	class Desires
	{
		private final Map<DesireType, DesireValue> desireTable;
		
		public Desires(DesireType... desireList)
		{
			desireTable = new FastMap<>();
			
			for (final DesireType desire : desireList)
			{
				desireTable.put(desire, new DesireValue());
			}
		}
		
		public DesireValue getDesireValue(DesireType type)
		{
			return desireTable.get(type);
		}
		
		public void addValue(DesireType type, float value)
		{
			final DesireValue temp = getDesireValue(type);
			if (temp != null)
			{
				temp.addValue(value);
			}
		}
		
		public void createDesire(DesireType type)
		{
			desireTable.put(type, new DesireValue());
		}
		
		public void deleteDesire(DesireType type)
		{
			desireTable.remove(type);
		}
	}
	
	private final Map<L2Object, Desires> objectDesireTable;
	private final Desires generalDesires;
	private final DesireType[] desireTypes;
	
	public DesireTable(DesireType... desireList)
	{
		desireTypes = desireList;
		objectDesireTable = new FastMap<>();
		generalDesires = new Desires(desireTypes);
	}
	
	public float getDesireValue(DesireType type)
	{
		return generalDesires.getDesireValue(type).getValue();
	}
	
	public float getDesireValue(L2Object object, DesireType type)
	{
		final Desires desireList = objectDesireTable.get(object);
		if (desireList == null)
		{
			return 0f;
		}
		return desireList.getDesireValue(type).getValue();
	}
	
	public void addDesireValue(DesireType type, float value)
	{
		generalDesires.addValue(type, value);
	}
	
	public void addDesireValue(L2Object object, DesireType type, float value)
	{
		final Desires desireList = objectDesireTable.get(object);
		if (desireList != null)
		{
			desireList.addValue(type, value);
		}
	}
	
	public void createDesire(DesireType type)
	{
		generalDesires.createDesire(type);
	}
	
	public void deleteDesire(DesireType type)
	{
		generalDesires.deleteDesire(type);
	}
	
	public void createDesire(L2Object object, DesireType type)
	{
		final Desires desireList = objectDesireTable.get(object);
		if (desireList != null)
		{
			desireList.createDesire(type);
		}
	}
	
	public void deleteDesire(L2Object object, DesireType type)
	{
		final Desires desireList = objectDesireTable.get(object);
		if (desireList != null)
		{
			desireList.deleteDesire(type);
		}
	}
	
	public void addKnownObject(L2Object object)
	{
		if (object != null)
		{
			addKnownObject(object, DesireType.DISLIKE, DesireType.FEAR, DesireType.DAMAGE, DesireType.HATE);
		}
	}
	
	public void addKnownObject(L2Object object, DesireType... desireList)
	{
		if (object != null)
		{
			objectDesireTable.put(object, new Desires(desireList));
		}
	}
}