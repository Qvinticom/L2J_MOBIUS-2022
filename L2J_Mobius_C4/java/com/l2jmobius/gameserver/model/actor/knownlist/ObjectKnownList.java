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
package com.l2jmobius.gameserver.model.actor.knownlist;

import java.util.Map;

import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.util.Util;

import javolution.util.FastMap;

public class ObjectKnownList
{
	// =========================================================
	// Data Field
	private final L2Object _ActiveObject;
	private Map<Integer, L2Object> _KnownObjects;
	
	// =========================================================
	// Constructor
	public ObjectKnownList(L2Object activeObject)
	{
		_ActiveObject = activeObject;
	}
	
	// =========================================================
	// Method - Public
	public boolean addKnownObject(L2Object object)
	{
		return addKnownObject(object, null);
	}
	
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if (object == null)
		{
			return false;
		}
		
		// Check if already knows object
		if (knowsObject(object))
		{
			if (getActiveObject() instanceof L2Character)
			{
				return ((L2Character) getActiveObject()).isSummoned();
			}
			
			return false;
		}
		
		// Check if object is not inside distance to watch object
		if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveObject(), object, true))
		{
			return false;
		}
		
		return (getKnownObjects().put(object.getObjectId(), object) == null);
	}
	
	public final boolean knowsObject(L2Object object)
	{
		return (getActiveObject() == object) || getKnownObjects().containsKey(object.getObjectId());
	}
	
	/** Remove all L2Object from _knownObjects */
	public void removeAllKnownObjects()
	{
		getKnownObjects().clear();
	}
	
	public boolean removeKnownObject(L2Object object)
	{
		if (object == null)
		{
			return false;
		}
		return (getKnownObjects().remove(object.getObjectId()) != null);
	}
	
	public void forgetObjects(boolean fullCheck)
	{
		// Go through knownObjects
		for (final L2Object object : getKnownObjects().values())
		{
			if (!fullCheck && !(object instanceof L2PlayableInstance))
			{
				continue;
			}
			
			// Remove all objects invisible or too far
			if (!object.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true))
			{
				removeKnownObject(object);
			}
		}
	}
	
	// =========================================================
	// Property - Public
	public L2Object getActiveObject()
	{
		return _ActiveObject;
	}
	
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}
	
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}
	
	/**
	 * Return the _knownObjects containing all L2Object known by the L2Character.
	 * @return
	 */
	public final Map<Integer, L2Object> getKnownObjects()
	{
		if (_KnownObjects == null)
		{
			_KnownObjects = new FastMap<Integer, L2Object>().shared();
		}
		return _KnownObjects;
	}
}