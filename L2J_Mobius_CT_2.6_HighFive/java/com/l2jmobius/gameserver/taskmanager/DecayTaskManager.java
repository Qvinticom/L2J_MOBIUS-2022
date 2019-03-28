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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.gameserver.model.actor.Attackable;
import com.l2jmobius.gameserver.model.actor.Creature;
import com.l2jmobius.gameserver.model.actor.templates.NpcTemplate;

/**
 * @author NosBit
 */
public final class DecayTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(DecayTaskManager.class.getName());
	
	protected final Map<Creature, ScheduledFuture<?>> _decayTasks = new ConcurrentHashMap<>();
	
	/**
	 * Adds a decay task for the specified character.<br>
	 * <br>
	 * If the decay task already exists it cancels it and re-adds it.
	 * @param creature the creature
	 */
	public void add(Creature creature)
	{
		if (creature == null)
		{
			return;
		}
		
		long delay;
		if (creature.getTemplate() instanceof NpcTemplate)
		{
			delay = ((NpcTemplate) creature.getTemplate()).getCorpseTime();
		}
		else
		{
			delay = Config.DEFAULT_CORPSE_TIME;
		}
		
		if (creature.isAttackable() && (((Attackable) creature).isSpoiled() || ((Attackable) creature).isSeeded()))
		{
			delay += Config.SPOILED_CORPSE_EXTEND_TIME;
		}
		
		// Remove entries that became null.
		_decayTasks.entrySet().removeIf(Objects::isNull);
		
		try
		{
			_decayTasks.putIfAbsent(creature, ThreadPool.schedule(new DecayTask(creature), delay * 1000));
		}
		catch (Exception e)
		{
			LOGGER.warning("DecayTaskManager add " + creature + " caused [" + e.getMessage() + "] exception.");
		}
	}
	
	/**
	 * Cancels the decay task of the specified character.
	 * @param creature the creature
	 */
	public void cancel(Creature creature)
	{
		final ScheduledFuture<?> decayTask = _decayTasks.remove(creature);
		if (decayTask != null)
		{
			decayTask.cancel(false);
		}
	}
	
	/**
	 * Gets the remaining time of the specified character's decay task.
	 * @param creature the creature
	 * @return if a decay task exists the remaining time, {@code Long.MAX_VALUE} otherwise
	 */
	public long getRemainingTime(Creature creature)
	{
		final ScheduledFuture<?> decayTask = _decayTasks.get(creature);
		if (decayTask != null)
		{
			return decayTask.getDelay(TimeUnit.MILLISECONDS);
		}
		
		return Long.MAX_VALUE;
	}
	
	private class DecayTask implements Runnable
	{
		private final Creature _creature;
		
		protected DecayTask(Creature creature)
		{
			_creature = creature;
		}
		
		@Override
		public void run()
		{
			_decayTasks.remove(_creature);
			_creature.onDecay();
		}
	}
	
	@Override
	public String toString()
	{
		final StringBuilder ret = new StringBuilder();
		ret.append("============= DecayTask Manager Report ============");
		ret.append(Config.EOL);
		ret.append("Tasks count: ");
		ret.append(_decayTasks.size());
		ret.append(Config.EOL);
		ret.append("Tasks dump:");
		ret.append(Config.EOL);
		
		for (Entry<Creature, ScheduledFuture<?>> entry : _decayTasks.entrySet())
		{
			ret.append("Class/Name: ");
			ret.append(entry.getKey().getClass().getSimpleName());
			ret.append('/');
			ret.append(entry.getKey().getName());
			ret.append(" decay timer: ");
			ret.append(entry.getValue().getDelay(TimeUnit.MILLISECONDS));
			ret.append(Config.EOL);
		}
		
		return ret.toString();
	}
	
	public static DecayTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DecayTaskManager _instance = new DecayTaskManager();
	}
}
