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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.actor.Npc;

/**
 * @author Mobius
 */
public class RespawnTaskManager
{
	private static final Map<Npc, List<Long>> PENDING_RESPAWNS = new ConcurrentHashMap<>();
	
	public RespawnTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(() ->
		{
			final long time = System.currentTimeMillis();
			for (Entry<Npc, List<Long>> entry : PENDING_RESPAWNS.entrySet())
			{
				final Npc npc = entry.getKey();
				final List<Long> schedules = entry.getValue();
				for (Long respawnTime : schedules)
				{
					if (time > respawnTime)
					{
						schedules.remove(respawnTime);
						if (schedules.isEmpty())
						{
							PENDING_RESPAWNS.remove(npc);
						}
						final Spawn spawn = npc.getSpawn();
						if (spawn != null)
						{
							spawn.respawnNpc(npc);
							spawn._scheduledCount--;
						}
					}
				}
			}
		}, 0, 1000);
	}
	
	public void add(Npc npc, Long time)
	{
		if (!PENDING_RESPAWNS.containsKey(npc))
		{
			PENDING_RESPAWNS.put(npc, new CopyOnWriteArrayList<>());
		}
		PENDING_RESPAWNS.get(npc).add(time);
	}
	
	public static RespawnTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RespawnTaskManager INSTANCE = new RespawnTaskManager();
	}
}
