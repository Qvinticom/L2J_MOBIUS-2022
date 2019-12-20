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
package org.l2jmobius.gameserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.commons.util.Util;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

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
			int activePlayers = 0;
			int offlinePlayers = 0;
			
			for (PlayerInstance player : World.getInstance().getAllPlayers())
			{
				if (player.isInOfflineMode())
				{
					offlinePlayers++;
				}
				else
				{
					activePlayers++;
				}
			}
			
			Util.printSection("Server Status");
			LOGGER.info("Server Time: " + fmt.format(new Date(System.currentTimeMillis())));
			LOGGER.info("Active Players Online: " + activePlayers);
			LOGGER.info("Offline Players Online: " + offlinePlayers);
			LOGGER.info("Threads: " + Thread.activeCount());
			LOGGER.info("Free Memory: " + (((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576) + " MB");
			LOGGER.info("Used memory: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MB");
			Util.printSection("Server Status");
		}
	}
	
	public static ServerStatus getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ServerStatus INSTANCE = new ServerStatus();
	}
}