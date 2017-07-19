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
package com.l2jmobius.gameserver.taskmanager;

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.L2WorldRegion;
import com.l2jmobius.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;

public class KnownListUpdateTaskManager
{
	protected static final Logger _log = Logger.getLogger(KnownListUpdateTaskManager.class.getName());
	
	private static KnownListUpdateTaskManager _instance;
	
	private final static int FULL_UPDATE_TIMER = 100;
	public static boolean updatePass = true;
	
	// Do full update every FULL_UPDATE_TIMER * KNOWNLIST_UPDATE_INTERVAL
	public static int _fullUpdateTimer = FULL_UPDATE_TIMER;
	
	public KnownListUpdateTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(), 1000, Config.KNOWNLIST_UPDATE_INTERVAL);
	}
	
	public static KnownListUpdateTaskManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new KnownListUpdateTaskManager();
		}
		
		return _instance;
	}
	
	private class KnownListUpdate implements Runnable
	{
		
		protected KnownListUpdate()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			try
			{
				for (final L2WorldRegion regions[] : L2World.getInstance().getAllWorldRegions())
				{
					for (final L2WorldRegion r : regions) // go through all world regions
					{
						try
						{
							if (r.isActive())
							{
								updateRegion(r, (_fullUpdateTimer == FULL_UPDATE_TIMER), updatePass);
							}
						}
						catch (final Exception e)
						{
							e.printStackTrace();
						}
						
					}
				}
				
				updatePass = !updatePass;
				if (_fullUpdateTimer > 0)
				{
					_fullUpdateTimer--;
				}
				else
				{
					_fullUpdateTimer = FULL_UPDATE_TIMER;
				}
				
			}
			catch (final Exception e)
			{
				_log.warning(e.toString());
			}
		}
	}
	
	public void updateRegion(L2WorldRegion region, boolean fullUpdate, boolean forgetObjects)
	{
		for (final L2Object object : region.getVisibleObjects()) // and for all members in region
		{
			if ((object == null) || !object.isVisible())
			{
				continue; // skip dying objects
			}
			
			if (forgetObjects)
			{
				object.getKnownList().forgetObjects((Config.GUARD_ATTACK_AGGRO_MOB && (object instanceof L2GuardInstance)) || fullUpdate);
				continue;
			}
			
			if ((object instanceof L2PlayableInstance) || (Config.GUARD_ATTACK_AGGRO_MOB && (object instanceof L2GuardInstance)) || fullUpdate)
			{
				for (final L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
				{
					for (final L2Object _object : regi.getVisibleObjects())
					{
						if (_object != object)
						{
							object.getKnownList().addKnownObject(_object);
						}
						
					}
				}
			}
			else if (object instanceof L2Character)
			{
				for (final L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
				{
					if (regi.isActive())
					{
						for (final L2Object _object : regi.getVisiblePlayable())
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
}