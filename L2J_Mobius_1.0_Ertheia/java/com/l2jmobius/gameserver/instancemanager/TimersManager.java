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
package com.l2jmobius.gameserver.instancemanager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.timers.TimerHolder;

/**
 * @author UnAfraid
 */
public class TimersManager
{
	private final Map<Integer, Set<TimerHolder<?>>> _timers = new ConcurrentHashMap<>();
	
	public void registerTimer(TimerHolder<?> timer)
	{
		final L2Npc npc = timer.getNpc();
		if (npc != null)
		{
			_timers.computeIfAbsent(npc.getObjectId(), key -> ConcurrentHashMap.newKeySet()).add(timer);
		}
		
		final L2PcInstance player = timer.getPlayer();
		if (player != null)
		{
			_timers.computeIfAbsent(player.getObjectId(), key -> ConcurrentHashMap.newKeySet()).add(timer);
		}
	}
	
	public void cancelTimers(int objectId)
	{
		final Set<TimerHolder<?>> timers = _timers.remove(objectId);
		if (timers != null)
		{
			timers.forEach(TimerHolder::cancelTimer);
		}
	}
	
	public static TimersManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TimersManager _instance = new TimersManager();
	}
}
