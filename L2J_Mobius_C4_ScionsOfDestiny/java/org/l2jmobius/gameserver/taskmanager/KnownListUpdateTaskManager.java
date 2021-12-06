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
package org.l2jmobius.gameserver.taskmanager;

import java.util.logging.Logger;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.WorldRegion;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Playable;

public class KnownListUpdateTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(KnownListUpdateTaskManager.class.getName());
	
	protected KnownListUpdateTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(new KnownListUpdate(), 1000, 750);
	}
	
	protected class KnownListUpdate implements Runnable
	{
		boolean _toggle = false;
		boolean _fullUpdate = true;
		
		protected KnownListUpdate()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				for (WorldRegion[] regions : World.getInstance().getAllWorldRegions())
				{
					for (WorldRegion r : regions) // go through all world regions
					{
						if (r.isActive()) // and check only if the region is active
						{
							updateRegion(r, _fullUpdate, _toggle);
						}
					}
				}
				if (_toggle)
				{
					_toggle = false;
				}
				else
				{
					_toggle = true;
				}
				if (_fullUpdate)
				{
					_fullUpdate = false;
				}
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.toString());
			}
		}
	}
	
	public void updateRegion(WorldRegion region, boolean fullUpdate, boolean forgetObjects)
	{
		for (WorldObject object : region.getVisibleObjects()) // and for all members in region
		{
			if (!object.isSpawned())
			{
				continue; // skip dying objects
			}
			if (forgetObjects)
			{
				object.getKnownList().forgetObjects();
				continue;
			}
			if ((object instanceof Playable) || fullUpdate)
			{
				for (WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
				{
					for (WorldObject _object : regi.getVisibleObjects())
					{
						if (_object != object)
						{
							object.getKnownList().addKnownObject(_object);
						}
					}
				}
			}
			else if (object instanceof Creature)
			{
				for (WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
				{
					if (regi.isActive())
					{
						for (WorldObject _object : regi.getVisibleObjects())
						{
							if (_object != object)
							{
								object.getKnownList().addKnownObject(_object);
							}
						}
					}
				}
			}
		}
	}
	
	public static KnownListUpdateTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final KnownListUpdateTaskManager INSTANCE = new KnownListUpdateTaskManager();
	}
}
