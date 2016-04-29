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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.Shutdown;
import com.l2jmobius.gameserver.ThreadPoolManager;

/**
 * @author Gigi
 */
public class ServerRestartManager
{
	static final Logger _log = Logger.getLogger(ServerRestartManager.class.getName());
	
	private String nextRestartTime = "unknown";
	
	protected ServerRestartManager()
	{
		try
		{
			final Calendar currentTime = Calendar.getInstance();
			final Calendar restartTime = currentTime;
			Calendar lastRestart = currentTime;
			restartTime.setLenient(true);
			long delay = 0;
			long lastDelay = 0;
			int count = 0;
			
			for (String timeOfDay : Config.SERVER_RESTART_SCHEDULE_HOURS)
			{
				final String[] splitTimeOfDay = timeOfDay.split(":");
				restartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				restartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				restartTime.set(Calendar.SECOND, 00);
				
				if (restartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					restartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				delay = restartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				if (count == 0)
				{
					lastDelay = delay;
					lastRestart = restartTime;
				}
				if (delay < lastDelay)
				{
					lastDelay = delay;
					lastRestart = restartTime;
				}
				count++;
			}
			
			nextRestartTime = new SimpleDateFormat("HH:mm").format(lastRestart.getTime());
			ThreadPoolManager.getInstance().scheduleGeneral(new ServerRestartTask(), lastDelay);
			_log.info("Scheduled server restart at " + lastRestart.getTime().toString() + ".");
		}
		catch (Exception e)
		{
			_log.info("The scheduled server restart config is not set properly, please correct it!");
		}
	}
	
	public String getNextRestartTime()
	{
		return nextRestartTime;
	}
	
	class ServerRestartTask implements Runnable
	{
		@Override
		public void run()
		{
			Shutdown.getInstance().startShutdown(null, Config.SERVER_RESTART_SCHEDULE_COUNTDOWN, true);
		}
	}
	
	public static ServerRestartManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ServerRestartManager _instance = new ServerRestartManager();
	}
}