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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.instancemanager.DayNightSpawnManager;
import org.l2jmobius.gameserver.model.actor.Creature;

/**
 * Game Time task manager class.
 * @author Forsaiken
 */
public class GameTimeTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(GameTimeTaskManager.class.getName());
	
	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
	
	protected static int _gameTicks;
	protected static long _gameStartTime;
	protected static boolean _isNight = false;
	
	private static final Set<Creature> _movingObjects = ConcurrentHashMap.newKeySet();
	
	protected static TimerThread _timer;
	private final ScheduledFuture<?> _timerWatcher;
	
	protected GameTimeTaskManager()
	{
		_gameStartTime = Chronos.currentTimeMillis() - 3600000; // offset so that the server starts a day begin
		_gameTicks = 3600000 / MILLIS_IN_TICK; // offset so that the server starts a day begin
		
		_timer = new TimerThread();
		_timer.start();
		
		_timerWatcher = ThreadPool.scheduleAtFixedRate(new TimerWatcher(), 0, 1000);
		ThreadPool.scheduleAtFixedRate(new BroadcastSunState(), 0, 600000);
	}
	
	public boolean isNight()
	{
		return _isNight;
	}
	
	public int getGameTime()
	{
		return _gameTicks / (TICKS_PER_SECOND * 10);
	}
	
	public static int getGameTicks()
	{
		return _gameTicks;
	}
	
	public int getGameHour()
	{
		return getGameTime() / 60;
	}
	
	public int getGameMinute()
	{
		return getGameTime() % 60;
	}
	
	/**
	 * Add a Creature to movingObjects of GameTimeTaskManager.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All Creature in movement are identified in <b>movingObjects</b> of GameTimeTaskManager.
	 * @param creature The Creature to add to movingObjects of GameTimeTaskManager
	 */
	public void registerMovingObject(Creature creature)
	{
		if (creature == null)
		{
			return;
		}
		
		_movingObjects.add(creature);
	}
	
	/**
	 * Move all Creatures contained in movingObjects of GameTimeTaskManager.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All Creature in movement are identified in <b>movingObjects</b> of GameTimeTaskManager.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Update the position of each Creature</li>
	 * <li>If movement is finished, the Creature is removed from movingObjects</li>
	 * <li>Create a task to update the _knownObject and _knowPlayers of each Creature that finished its movement and of their already known WorldObject then notify AI with EVT_ARRIVED</li>
	 */
	protected void moveObjects()
	{
		final List<Creature> finished = new LinkedList<>();
		for (Creature creature : _movingObjects)
		{
			if (creature.updatePosition(_gameTicks))
			{
				finished.add(creature);
			}
		}
		
		if (!finished.isEmpty())
		{
			for (Creature creature : finished)
			{
				_movingObjects.remove(creature);
			}
			ThreadPool.execute(new MovingObjectArrived(finished));
		}
	}
	
	public void stopTimer()
	{
		_timerWatcher.cancel(true);
		_timer.interrupt();
	}
	
	private class TimerThread extends Thread
	{
		protected Exception _error;
		
		public TimerThread()
		{
			super("GameTimeTaskManager");
			setDaemon(true);
			setPriority(MAX_PRIORITY);
		}
		
		@Override
		public void run()
		{
			for (;;)
			{
				final int _oldTicks = _gameTicks; // save old ticks value to avoid moving objects 2x in same tick
				long runtime = Chronos.currentTimeMillis() - _gameStartTime; // from server boot to now
				
				_gameTicks = (int) (runtime / MILLIS_IN_TICK); // new ticks value (ticks now)
				
				if (_oldTicks != _gameTicks)
				{
					moveObjects(); // XXX: if this makes objects go slower, remove it
					// but I think it can't make that effect. is it better to call moveObjects() twice in same
					// tick to make-up for missed tick ? or is it better to ignore missed tick ?
					// (will happen very rarely but it will happen ... on garbage collection definitely)
				}
				
				runtime = Chronos.currentTimeMillis() - _gameStartTime - runtime;
				
				// calculate sleep time... time needed to next tick minus time it takes to call moveObjects()
				final int sleepTime = (1 + MILLIS_IN_TICK) - ((int) runtime % MILLIS_IN_TICK);
				
				// LOGGER.finest("TICK: "+_gameTicks);
				
				try
				{
					sleep(sleepTime); // hope other threads will have much more cpu time available now
				}
				catch (InterruptedException e)
				{
					// nothing
				}
				// SelectorThread most of all
			}
		}
	}
	
	protected class TimerWatcher implements Runnable
	{
		@Override
		public void run()
		{
			if (!_timer.isAlive())
			{
				final String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
				LOGGER.warning(time + " TimerThread stop with following error. restart it.");
				if (_timer._error != null)
				{
					_timer._error.printStackTrace();
				}
				
				_timer = new TimerThread();
				_timer.start();
			}
		}
	}
	
	/**
	 * Update the _knownObject and _knowPlayers of each Creature that finished its movement and of their already known WorldObject then notify AI with EVT_ARRIVED.
	 */
	private class MovingObjectArrived implements Runnable
	{
		private final List<Creature> _finished;
		
		MovingObjectArrived(List<Creature> finished)
		{
			_finished = finished;
		}
		
		@Override
		public void run()
		{
			for (int i = 0; i < _finished.size(); i++)
			{
				try
				{
					final Creature creature = _finished.get(i);
					creature.getKnownList().updateKnownObjects();
					creature.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				}
				catch (Exception e)
				{
				}
			}
		}
	}
	
	protected class BroadcastSunState implements Runnable
	{
		@Override
		public void run()
		{
			final int h = (getGameTime() / 60) % 24; // Time in hour
			final boolean tempIsNight = h < 6;
			
			// If diff day/night state
			if (tempIsNight != _isNight)
			{
				// Set current day/night varible to value of temp varible
				_isNight = tempIsNight;
				DayNightSpawnManager.getInstance().notifyChangeMode();
			}
		}
	}
	
	public static final GameTimeTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GameTimeTaskManager INSTANCE = new GameTimeTaskManager();
	}
}
