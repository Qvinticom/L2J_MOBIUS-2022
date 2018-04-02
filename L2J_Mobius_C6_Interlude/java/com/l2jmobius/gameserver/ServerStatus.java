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
package com.l2jmobius.gameserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.commons.util.Memory;
import com.l2jmobius.commons.util.Util;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * Server status
 * @author Nefer
 * @version 1.0
 */
public class ServerStatus
{
	protected static final Logger LOGGER = Logger.getLogger(ServerStatus.class.getName());
	protected ScheduledFuture<?> _scheduledTask;
	
	protected ServerStatus()
	{
		_scheduledTask = ThreadPool.scheduleAtFixedRate(new ServerStatusTask(), 1800000, 3600000);
	}
	
	protected class ServerStatusTask implements Runnable
	{
		protected final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
		
		@Override
		public void run()
		{
			int ActivePlayers = 0;
			int OfflinePlayers = 0;
			
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player.isInOfflineMode())
				{
					OfflinePlayers++;
				}
				else
				{
					ActivePlayers++;
				}
			}
			
			Util.printSection("Server Status");
			LOGGER.info("Server Time: " + fmt.format(new Date(System.currentTimeMillis())));
			LOGGER.info("Active Players Online: " + ActivePlayers);
			LOGGER.info("Offline Players Online: " + OfflinePlayers);
			LOGGER.info("Threads: " + Thread.activeCount());
			LOGGER.info("Free Memory: " + Memory.getFreeMemory() + " MB");
			LOGGER.info("Used memory: " + Memory.getUsedMemory() + " MB");
			Util.printSection("Server Status");
		}
	}
	
	public static ServerStatus getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ServerStatus _instance = new ServerStatus();
	}
}