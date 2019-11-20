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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.PlayerCountManager;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class World
{
	private static Logger _log = Logger.getLogger(World.class.getName());
	private final Map<String, WorldObject> _allPlayers = new ConcurrentHashMap<>();
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
			_allPlayers.put(((PlayerInstance) object).getName().toLowerCase(), object);
			WorldObject[] visible = getVisibleObjects(object, 2000);
			_log.finest("Objects in range:" + visible.length);
			for (WorldObject element : visible)
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
			int x = object.getX();
			int y = object.getY();
			int sqRadius = 4000000;
			Iterator<WorldObject> iter = _allPlayers.values().iterator();
			while (iter.hasNext())
			{
				long dy;
				PlayerInstance player = (PlayerInstance) iter.next();
				int x1 = player.getX();
				long dx = x1 - x;
				long sqDist = (dx * dx) + ((dy = player.getY() - y) * dy);
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
		// _log.fine("World has now " + this._visibleObjects.size() + " visible objects");
		Object[] temp = object.getKnownObjects().toArray();
		for (Object element : temp)
		{
			WorldObject temp1 = (WorldObject) element;
			temp1.removeKnownObject(object);
			object.removeKnownObject(temp1);
		}
		if (object instanceof PlayerInstance)
		{
			_allPlayers.remove(((PlayerInstance) object).getName().toLowerCase());
			
			// TODO: Make sure the normal way works.
			// PlayerCountManager.getInstance().decConnectedCount();
			PlayerCountManager.getInstance().setConnectedCount(_allPlayers.size());
		}
	}
	
	public WorldObject[] getVisibleObjects(WorldObject object, int radius)
	{
		int x = object.getX();
		int y = object.getY();
		int sqRadius = radius * radius;
		List<WorldObject> result = new ArrayList<>();
		Iterator<WorldObject> iter = _visibleObjects.values().iterator();
		while (iter.hasNext())
		{
			@SuppressWarnings("unused")
			int x1;
			@SuppressWarnings("unused")
			int y1;
			long dx;
			long dy;
			@SuppressWarnings("unused")
			long sqDist;
			WorldObject element = iter.next();
			if (element.equals(object) || ((sqDist = ((dx = (x1 = element.getX()) - x) * dx) + ((dy = (y1 = element.getY()) - y) * dy)) >= sqRadius))
			{
				continue;
			}
			result.add(element);
		}
		return result.toArray(new WorldObject[result.size()]);
	}
	
	public PlayerInstance[] getAllPlayers()
	{
		return _allPlayers.values().toArray(new PlayerInstance[_allPlayers.size()]);
	}
	
	public PlayerInstance getPlayer(String name)
	{
		return (PlayerInstance) _allPlayers.get(name.toLowerCase());
	}
}
