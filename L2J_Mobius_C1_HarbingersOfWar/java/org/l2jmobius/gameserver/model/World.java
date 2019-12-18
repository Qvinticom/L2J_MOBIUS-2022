/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.managers.PlayerCountManager;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class World
{
	public static final int TILE_SIZE = 32768;
	public static final int TILE_X_MIN = 11;
	public static final int TILE_Y_MIN = 10;
	// public static final int TILE_X_MAX = 26;
	// public static final int TILE_Y_MAX = 26;
	public static final int TILE_ZERO_COORD_X = 20;
	public static final int TILE_ZERO_COORD_Y = 18;
	public static final int MAP_MIN_X = (TILE_X_MIN - TILE_ZERO_COORD_X) * TILE_SIZE;
	public static final int MAP_MIN_Y = (TILE_Y_MIN - TILE_ZERO_COORD_Y) * TILE_SIZE;
	
	private final Map<String, PlayerInstance> _allPlayers = new ConcurrentHashMap<>();
	private final Map<Integer, WorldObject> _allObjects = new ConcurrentHashMap<>();
	private final Map<Integer, WorldObject> _visibleObjects = new ConcurrentHashMap<>();
	private static World _instance;
	
	private World()
	{
	}
	
	public static World getInstance()
	{
		if (_instance == null)
		{
			_instance = new World();
		}
		return _instance;
	}
	
	public void storeObject(WorldObject temp)
	{
		_allObjects.put(temp.getObjectId(), temp);
	}
	
	public void removeObject(WorldObject object)
	{
		_allObjects.remove(object.getObjectId());
	}
	
	public WorldObject findObject(int oID)
	{
		return _allObjects.get(oID);
	}
	
	public void addVisibleObject(WorldObject object)
	{
		if (object instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) object;
			_allPlayers.put(player.getName().toLowerCase(), player);
			for (WorldObject element : getVisibleObjects(object, 2000))
			{
				object.addKnownObject(element);
				if ((object instanceof ItemInstance) && element.getKnownObjects().contains(object))
				{
					continue;
				}
				element.addKnownObject(object);
			}
			PlayerCountManager.getInstance().incConnectedCount();
		}
		else if ((_allPlayers.size() != 0) && !(object instanceof PetInstance) && !(object instanceof ItemInstance))
		{
			final int x = object.getX();
			final int y = object.getY();
			final int sqRadius = 4000000;
			for (PlayerInstance player : _allPlayers.values())
			{
				long dy;
				final int x1 = player.getX();
				final long dx = x1 - x;
				final long sqDist = (dx * dx) + ((dy = player.getY() - y) * dy);
				if (sqDist >= sqRadius)
				{
					continue;
				}
				player.addKnownObject(object);
				object.addKnownObject(player);
			}
		}
		_visibleObjects.put(object.getObjectId(), object);
	}
	
	public void removeVisibleObject(WorldObject object)
	{
		_visibleObjects.remove(object.getObjectId());
		for (WorldObject wo : object.getKnownObjects())
		{
			wo.removeKnownObject(object);
			object.removeKnownObject(wo);
		}
		if (object instanceof PlayerInstance)
		{
			_allPlayers.remove(((PlayerInstance) object).getName().toLowerCase());
			
			// TODO: Make sure the normal way works.
			// PlayerCountManager.getInstance().decConnectedCount();
			PlayerCountManager.getInstance().setConnectedCount(_allPlayers.size());
		}
	}
	
	public Collection<WorldObject> getVisibleObjects(WorldObject object, int radius)
	{
		final int x = object.getX();
		final int y = object.getY();
		final List<WorldObject> result = new ArrayList<>();
		for (WorldObject worldObject : _visibleObjects.values())
		{
			if ((worldObject == null) || worldObject.equals(object) || (Math.sqrt(Math.pow(x - worldObject.getX(), 2) + Math.pow(y - worldObject.getY(), 2)) > radius))
			{
				continue;
			}
			result.add(worldObject);
		}
		return result;
	}
	
	public Collection<PlayerInstance> getAllPlayers()
	{
		return _allPlayers.values();
	}
	
	public PlayerInstance getPlayer(String name)
	{
		return _allPlayers.get(name.toLowerCase());
	}
}
