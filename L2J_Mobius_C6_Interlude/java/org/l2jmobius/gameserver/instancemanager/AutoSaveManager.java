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
package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author Shyla
 */
public class AutoSaveManager
{
	protected static final Logger LOGGER = Logger.getLogger(AutoSaveManager.class.getName());
	private ScheduledFuture<?> _autoSaveInDB;
	private ScheduledFuture<?> _autoCheckConnectionStatus;
	private ScheduledFuture<?> _autoCleanDatabase;
	
	public static final AutoSaveManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public AutoSaveManager()
	{
		LOGGER.info("Initializing AutoSaveManager");
	}
	
	public void stopAutoSaveManager()
	{
		if (_autoSaveInDB != null)
		{
			_autoSaveInDB.cancel(true);
			_autoSaveInDB = null;
		}
		
		if (_autoCheckConnectionStatus != null)
		{
			_autoCheckConnectionStatus.cancel(true);
			_autoCheckConnectionStatus = null;
		}
		if (_autoCleanDatabase != null)
		{
			_autoCleanDatabase.cancel(true);
			_autoCleanDatabase = null;
		}
	}
	
	public void startAutoSaveManager()
	{
		stopAutoSaveManager();
		_autoSaveInDB = ThreadPool.scheduleAtFixedRate(new AutoSaveTask(), Config.AUTOSAVE_INITIAL_TIME, Config.AUTOSAVE_DELAY_TIME);
		_autoCheckConnectionStatus = ThreadPool.scheduleAtFixedRate(new ConnectionCheckTask(), Config.CHECK_CONNECTION_INITIAL_TIME, Config.CHECK_CONNECTION_DELAY_TIME);
		_autoCleanDatabase = ThreadPool.scheduleAtFixedRate(new AutoCleanDBTask(), Config.CLEANDB_INITIAL_TIME, Config.CLEANDB_DELAY_TIME);
	}
	
	protected class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			int playerscount = 0;
			
			final Collection<PlayerInstance> players = World.getInstance().getAllPlayers();
			
			for (PlayerInstance player : players)
			{
				if (player != null)
				{
					try
					{
						playerscount++;
						player.store();
					}
					catch (Exception e)
					{
						LOGGER.warning("Error saving player character: " + player.getName() + " " + e);
					}
				}
			}
			LOGGER.info("[AutoSaveManager] AutoSaveTask, " + playerscount + " players data saved.");
		}
	}
	
	protected class ConnectionCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			final Collection<PlayerInstance> players = World.getInstance().getAllPlayers();
			
			for (PlayerInstance player : players)
			{
				if ((player != null) && !player.isInOfflineMode())
				{
					if ((player.getClient() == null) || (player.isOnline() == 0))
					{
						LOGGER.info("[AutoSaveManager] Player " + player.getName() + " status == 0 ---> Closing Connection..");
						player.store();
						player.deleteMe();
					}
					else if (!player.getClient().isConnectionAlive())
					{
						try
						{
							LOGGER.info("[AutoSaveManager] Player " + player.getName() + " connection is not alive ---> Closing Connection..");
							player.getClient().onDisconnection();
						}
						catch (Exception e)
						{
							LOGGER.warning("[AutoSaveManager] Error saving player character: " + player.getName() + " " + e);
						}
					}
					else if (player.checkTeleportOverTime())
					{
						try
						{
							LOGGER.info("[AutoSaveManager] Player " + player.getName() + " has a teleport overtime ---> Closing Connection..");
							player.getClient().onDisconnection();
						}
						catch (Exception e)
						{
							LOGGER.warning("[AutoSaveManager] Error saving player character: " + player.getName() + " " + e);
						}
					}
				}
			}
			LOGGER.info("[AutoSaveManager] ConnectionCheckTask, players connections checked.");
		}
	}
	
	protected class AutoCleanDBTask implements Runnable
	{
		@Override
		public void run()
		{
			int erased = 0;
			
			// Perform the clean here instead of every time that the skills are saved in order to do it in once step because if skill have 0 reuse delay doesn't affect the game, just makes the table grows bigger
			try (Connection con = DatabaseFactory.getConnection())
			{
				PreparedStatement statement;
				statement = con.prepareStatement("DELETE FROM character_skills_save WHERE reuse_delay=0 && restore_type=1");
				erased = statement.executeUpdate();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.info("[AutoSaveManager] Error while cleaning skill with 0 reuse time from table.");
			}
			
			LOGGER.info("[AutoSaveManager] AutoCleanDBTask, " + erased + " entries cleaned from db.");
		}
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSaveManager INSTANCE = new AutoSaveManager();
	}
}