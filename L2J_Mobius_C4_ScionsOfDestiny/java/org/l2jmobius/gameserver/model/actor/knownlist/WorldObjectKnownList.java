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
package org.l2jmobius.gameserver.model.actor.knownlist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Boat;
import org.l2jmobius.gameserver.util.Util;

public class WorldObjectKnownList
{
	private final WorldObject _activeObject;
	private final Map<Integer, WorldObject> _knownObjects = new ConcurrentHashMap<>();
	
	public WorldObjectKnownList(WorldObject activeObject)
	{
		_activeObject = activeObject;
	}
	
	public boolean addKnownObject(WorldObject object)
	{
		return addKnownObject(object, null);
	}
	
	public boolean addKnownObject(WorldObject object, Creature dropper)
	{
		if ((object == null) || (object == _activeObject))
		{
			return false;
		}
		
		// Check if already know object
		if (knowsObject(object))
		{
			if (!object.isSpawned())
			{
				removeKnownObject(object);
			}
			return false;
		}
		
		// Check if object is not inside distance to watch object
		if (!Util.checkIfInRange(getDistanceToWatchObject(object), _activeObject, object, true))
		{
			return false;
		}
		
		return _knownObjects.put(object.getObjectId(), object) == null;
	}
	
	public boolean knowsObject(WorldObject object)
	{
		if (object == null)
		{
			return false;
		}
		return (_activeObject == object) || _knownObjects.containsKey(object.getObjectId());
	}
	
	/** Remove all WorldObject from _knownObjects */
	public void removeAllKnownObjects()
	{
		_knownObjects.clear();
	}
	
	public boolean removeKnownObject(WorldObject object)
	{
		if (object == null)
		{
			return false;
		}
		return _knownObjects.remove(object.getObjectId()) != null;
	}
	
	/**
	 * Update the _knownObject and _knowPlayers of the Creature and of its already known WorldObject.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove invisible and too far WorldObject from _knowObject and if necessary from _knownPlayers of the Creature</li>
	 * <li>Add visible WorldObject near the Creature to _knowObject and if necessary to _knownPlayers of the Creature</li>
	 * <li>Add Creature to _knowObject and if necessary to _knownPlayers of WorldObject alreday known by the Creature</li>
	 */
	public synchronized void updateKnownObjects()
	{
		// Only bother updating knownobjects for Creature; don't for WorldObject
		if (_activeObject instanceof Creature)
		{
			findCloseObjects();
			forgetObjects();
		}
	}
	
	private final void findCloseObjects()
	{
		if (_activeObject == null)
		{
			return;
		}
		
		if (_activeObject.isPlayable())
		{
			// Go through all visible WorldObject near the Creature
			for (WorldObject object : World.getInstance().getVisibleObjects(_activeObject))
			{
				if (object == null)
				{
					continue;
				}
				
				// Try to add object to active object's known objects
				// PlayableInstance sees everything
				addKnownObject(object);
				
				// Try to add active object to object's known objects
				// Only if object is a Creature and active object is a PlayableInstance
				if (object instanceof Creature)
				{
					object.getKnownList().addKnownObject(_activeObject);
				}
			}
		}
		else
		{
			// Go through all visible WorldObject near the Creature
			for (WorldObject object : World.getInstance().getVisibleObjects(_activeObject))
			{
				if ((object == null) || !object.isPlayable())
				{
					return;
				}
				
				// Try to add object to active object's known objects
				// Creature only needs to see visible Player and PlayableInstance, when moving. Other creatures are currently only known from initial spawn area.
				// Possibly look into getDistanceToForgetObject values before modifying this approach...
				addKnownObject(object);
			}
		}
	}
	
	public void forgetObjects()
	{
		// Go through knownObjects
		for (WorldObject object : _knownObjects.values())
		{
			if (object == null)
			{
				continue;
			}
			
			// Remove all invisible objects
			// Remove all too far objects
			if (!object.isSpawned() || !Util.checkIfInRange(getDistanceToForgetObject(object), _activeObject, object, true))
			{
				if ((object instanceof Boat) && (_activeObject instanceof Player))
				{
					if (((Boat) object).getVehicleDeparture() == null)
					{
						continue;
					}
					
					if (((Player) _activeObject).isInBoat())
					{
						if (((Player) _activeObject).getBoat() != object)
						{
							removeKnownObject(object);
						}
					}
					else
					{
						removeKnownObject(object);
					}
				}
				else
				{
					removeKnownObject(object);
				}
			}
		}
	}
	
	public WorldObject getActiveObject()
	{
		return _activeObject;
	}
	
	public int getDistanceToForgetObject(WorldObject object)
	{
		return 0;
	}
	
	public int getDistanceToWatchObject(WorldObject object)
	{
		return 0;
	}
	
	/**
	 * @return the _knownObjects containing all WorldObject known by the Creature.
	 */
	public Map<Integer, WorldObject> getKnownObjects()
	{
		return _knownObjects;
	}
}
