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
package com.l2jmobius.gameserver.model.entity.event.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.logging.Logger;

import com.l2jmobius.commons.concurrent.ThreadPool;

/**
 * @author Shyla
 */
public class EventsGlobalTask implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(EventsGlobalTask.class.getName());
	
	private static EventsGlobalTask instance;
	
	private boolean destroy = false;
	
	private final Hashtable<String, ArrayList<EventTask>> time_to_tasks = new Hashtable<>(); // time is in hh:mm
	private final Hashtable<String, ArrayList<EventTask>> eventid_to_tasks = new Hashtable<>();
	
	private EventsGlobalTask()
	{
		ThreadPool.schedule(this, 5000);
	}
	
	public static EventsGlobalTask getInstance()
	{
		if (instance == null)
		{
			instance = new EventsGlobalTask();
		}
		
		return instance;
	}
	
	public void registerNewEventTask(EventTask event)
	{
		if ((event == null) || (event.getEventIdentifier() == null) || event.getEventIdentifier().equals("") || (event.getEventStartTime() == null) || event.getEventStartTime().equals(""))
		{
			LOGGER.warning("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		ArrayList<EventTask> savedTasksForTime = time_to_tasks.get(event.getEventStartTime());
		ArrayList<EventTask> savedTasksForId = eventid_to_tasks.get(event.getEventIdentifier());
		
		if (savedTasksForTime != null)
		{
			if (!savedTasksForTime.contains(event))
			{
				savedTasksForTime.add(event);
			}
		}
		else
		{
			savedTasksForTime = new ArrayList<>();
			savedTasksForTime.add(event);
		}
		
		time_to_tasks.put(event.getEventStartTime(), savedTasksForTime);
		
		if (savedTasksForId != null)
		{
			if (!savedTasksForId.contains(event))
			{
				savedTasksForId.add(event);
			}
		}
		else
		{
			savedTasksForId = new ArrayList<>();
			savedTasksForId.add(event);
		}
		
		eventid_to_tasks.put(event.getEventIdentifier(), savedTasksForId);
	}
	
	public void clearEventTasksByEventName(String eventId)
	{
		if (eventId == null)
		{
			LOGGER.warning("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		if (eventId.equalsIgnoreCase("all"))
		{
			time_to_tasks.clear();
			eventid_to_tasks.clear();
		}
		else
		{
			final ArrayList<EventTask> oldTasksForId = eventid_to_tasks.get(eventId);
			
			if (oldTasksForId != null)
			{
				for (EventTask actual : oldTasksForId)
				{
					final ArrayList<EventTask> oldTasksForTime = time_to_tasks.get(actual.getEventStartTime());
					
					if (oldTasksForTime != null)
					{
						oldTasksForTime.remove(actual);
						
						time_to_tasks.put(actual.getEventStartTime(), oldTasksForTime);
					}
				}
				
				eventid_to_tasks.remove(eventId);
			}
		}
	}
	
	public void deleteEventTask(EventTask event)
	{
		if ((event == null) || (event.getEventIdentifier() == null) || event.getEventIdentifier().equals("") || (event.getEventStartTime() == null) || event.getEventStartTime().equals(""))
		{
			LOGGER.warning("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		if (time_to_tasks.size() < 0)
		{
			return;
		}
		
		final ArrayList<EventTask> oldTasksForId = eventid_to_tasks.get(event.getEventIdentifier());
		final ArrayList<EventTask> oldTasksForTime = time_to_tasks.get(event.getEventStartTime());
		
		if (oldTasksForId != null)
		{
			oldTasksForId.remove(event);
			eventid_to_tasks.put(event.getEventIdentifier(), oldTasksForId);
		}
		
		if (oldTasksForTime != null)
		{
			oldTasksForTime.remove(event);
			time_to_tasks.put(event.getEventStartTime(), oldTasksForTime);
		}
	}
	
	private void checkRegisteredEvents()
	{
		if (time_to_tasks.size() < 0)
		{
			return;
		}
		
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final int min = calendar.get(Calendar.MINUTE);
		
		String hourStr = "";
		String minStr = "";
		
		if (hour < 10)
		{
			hourStr = "0" + hour;
		}
		else
		{
			hourStr = "" + hour;
		}
		
		if (min < 10)
		{
			minStr = "0" + min;
		}
		else
		{
			minStr = "" + min;
		}
		
		final String currentTime = hourStr + ":" + minStr;
		final ArrayList<EventTask> registeredEventsAtCurrentTime = time_to_tasks.get(currentTime);
		if (registeredEventsAtCurrentTime != null)
		{
			for (EventTask actualEvent : registeredEventsAtCurrentTime)
			{
				ThreadPool.schedule(actualEvent, 5000);
			}
		}
	}
	
	public void destroyLocalInstance()
	{
		destroy = true;
		instance = null;
	}
	
	@Override
	public void run()
	{
		while (!destroy)
		{
			// start time checker
			checkRegisteredEvents();
			
			try
			{
				Thread.sleep(60000); // 1 minute
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
}
