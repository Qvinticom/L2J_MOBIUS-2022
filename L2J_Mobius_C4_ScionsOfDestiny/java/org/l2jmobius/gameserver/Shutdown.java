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

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseBackup;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.SchemeBufferTable;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.OfflineTraderTable;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jmobius.gameserver.instancemanager.PrecautionaryRestartManager;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jmobius.gameserver.instancemanager.TradeManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.sevensigns.SevenSignsFestival;
import org.l2jmobius.gameserver.network.ClientNetworkManager;
import org.l2jmobius.gameserver.network.EventLoopGroupManager;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.loginserverpackets.game.ServerStatus;
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;
import org.l2jmobius.gameserver.network.serverpackets.ServerClose;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * This class provides the functions for shutting down and restarting the server.<br>
 * It closes all open client connections and saves all data.
 * @version $Revision: 1.2.4.6 $ $Date: 2009/05/12 19:45:09 $
 */
public class Shutdown extends Thread
{
	public enum ShutdownModeType1
	{
		SIGTERM("Terminating"),
		SHUTDOWN("Shutting down"),
		RESTART("Restarting"),
		ABORT("Aborting");
		
		private final String _modeText;
		
		ShutdownModeType1(String modeText)
		{
			_modeText = modeText;
		}
		
		public String getText()
		{
			return _modeText;
		}
	}
	
	protected static final Logger LOGGER = Logger.getLogger(Shutdown.class.getName());
	
	private static Shutdown _counterInstance = null;
	
	private int _secondsShut;
	
	private int _shutdownMode;
	
	private boolean _shutdownStarted;
	
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	
	private static final String[] MODE_TEXT =
	{
		"SIGTERM",
		"shutting down",
		"restarting",
		"aborting"
	};
	
	/**
	 * Default constructor is only used internal to create the shutdown-hook instance
	 */
	public Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
		_shutdownStarted = false;
	}
	
	/**
	 * This creates a count down instance of Shutdown.
	 * @param seconds how many seconds until shutdown
	 * @param restart true is the server shall restart after shutdown
	 */
	private Shutdown(int seconds, boolean restart)
	{
		_secondsShut = Math.max(0, seconds);
		
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
		
		_shutdownStarted = false;
	}
	
	public boolean isShutdownStarted()
	{
		boolean output = _shutdownStarted;
		
		// if a counter is started, the value of shutdownstarted is of counterinstance
		if (_counterInstance != null)
		{
			output = _counterInstance._shutdownStarted;
		}
		
		return output;
	}
	
	/**
	 * this function is called, when a new thread starts if this thread is the thread of getInstance, then this is the shutdown hook and we save all data and disconnect all clients. after this thread ends, the server will completely exit if this is not the thread of getInstance, then this is a
	 * countdown thread. we start the countdown, and when we finished it, and it was not aborted, we tell the shutdown-hook why we call exit, and then call exit when the exit status of the server is 1, startServer.sh / startServer.bat will restart the server.
	 */
	@Override
	public void run()
	{
		if (this == getInstance())
		{
			closeServer();
		}
		else
		{
			// GM shutdown: send warnings and then call exit to start shutdown sequence
			countdown();
			
			if (_shutdownMode != ABORT)
			{
				// last point where logging is operational :(
				LOGGER.warning("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
				
				closeServer();
			}
		}
	}
	
	/**
	 * This functions starts a shutdown countdown
	 * @param player GM who issued the shutdown command
	 * @param seconds seconds until shutdown
	 * @param restart true if the server will restart after shutdown
	 */
	public void startShutdown(Player player, int seconds, boolean restart)
	{
		final AnnouncementsTable announcements = AnnouncementsTable.getInstance();
		
		LOGGER.warning((player != null ? "GM: " + player.getName() + "(" + player.getObjectId() + ")" : "Server") + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
		
		if (_shutdownMode > 0)
		{
			announcements.announceToAll("Server is " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
			announcements.announceToAll("Please exit game now!!");
		}
		
		if (_counterInstance != null)
		{
			_counterInstance.abort();
		}
		
		if (Config.PRECAUTIONARY_RESTART_ENABLED)
		{
			PrecautionaryRestartManager.getInstance().restartEnabled();
		}
		
		// the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}
	
	public int getCountdown()
	{
		return _secondsShut;
	}
	
	/**
	 * This function aborts a running countdown
	 * @param player GM who issued the abort command
	 */
	public void abort(Player player)
	{
		final AnnouncementsTable announcements = AnnouncementsTable.getInstance();
		
		LOGGER.warning((player != null ? "GM: " + player.getName() + "(" + player.getObjectId() + ")" : "Server") + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		
		announcements.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		
		if (_counterInstance != null)
		{
			_counterInstance.abort();
		}
		
		if (Config.PRECAUTIONARY_RESTART_ENABLED)
		{
			PrecautionaryRestartManager.getInstance().restartAborted();
		}
	}
	
	/**
	 * set shutdown mode to ABORT
	 */
	private void abort()
	{
		_shutdownMode = ABORT;
	}
	
	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	private void countdown()
	{
		try
		{
			while (_secondsShut > 0)
			{
				int seconds;
				int minutes;
				int hours;
				
				seconds = _secondsShut;
				minutes = seconds / 60;
				hours = seconds / 3600;
				
				// announce only every minute after 10 minutes left and every second after 20 seconds
				if (((seconds <= 20) || (seconds == (minutes * 10))) && (seconds <= 600) && (hours <= 1))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECOND_S_PLEASE_FIND_A_SAFE_PLACE_TO_LOG_OUT);
					sm.addString(Integer.toString(seconds));
					Broadcast.toAllOnlinePlayers(sm);
				}
				
				try
				{
					if (seconds <= 60)
					{
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN);
					}
				}
				catch (Exception e)
				{
					// do nothing, we maybe are not connected to LS anymore
				}
				
				_secondsShut--;
				
				Thread.sleep(1000);
				
				if (_shutdownMode == ABORT)
				{
					break;
				}
			}
		}
		catch (InterruptedException e)
		{
		}
	}
	
	private void closeServer()
	{
		// Save all data and quit this server.
		_shutdownStarted = true;
		
		try
		{
			LoginServerThread.getInstance().interrupt();
		}
		catch (Throwable t)
		{
		}
		
		// saveData sends messages to exit players, so shutdown selector after it
		saveData();
		
		try
		{
			GameTimeTaskManager.getInstance().stopTimer();
		}
		catch (Throwable t)
		{
		}
		
		// saveData sends messages to exit players, so shutdown selector after it
		try
		{
			ClientNetworkManager.getInstance().stop();
			EventLoopGroupManager.getInstance().shutdown();
			LOGGER.info("Game Server: Selector thread has been shut down.");
		}
		catch (Throwable t)
		{
			// ignore
		}
		
		// stop all threadpolls
		try
		{
			ThreadPool.shutdown();
		}
		catch (Throwable t)
		{
		}
		
		LOGGER.info("Committing all data, last chance...");
		
		// commit data, last chance
		try
		{
			DatabaseFactory.close();
		}
		catch (Throwable t)
		{
		}
		
		LOGGER.info("All database data committed.");
		
		// Backup database.
		if (Config.BACKUP_DATABASE)
		{
			DatabaseBackup.performBackup();
		}
		
		LOGGER.info("[STATUS] Server shutdown successfully.");
		
		if (getInstance()._shutdownMode == GM_RESTART)
		{
			Runtime.getRuntime().halt(2);
		}
		else
		{
			Runtime.getRuntime().halt(0);
		}
	}
	
	/**
	 * This sends a last byebye, disconnects all players and saves data.
	 */
	private synchronized void saveData()
	{
		final AnnouncementsTable announcements = AnnouncementsTable.getInstance();
		switch (_shutdownMode)
		{
			case SIGTERM:
			{
				LOGGER.info("SIGTERM received. Shutting down NOW!");
				break;
			}
			case GM_SHUTDOWN:
			{
				LOGGER.info("GM shutdown received. Shutting down NOW!");
				break;
			}
			case GM_RESTART:
			{
				LOGGER.info("GM restart received. Restarting NOW!");
				break;
			}
		}
		try
		{
			announcements.announceToAll("Server is " + MODE_TEXT[_shutdownMode] + " NOW!");
		}
		catch (Throwable t)
		{
		}
		
		try
		{
			if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
			{
				OfflineTraderTable.getInstance().storeOffliners();
			}
		}
		catch (Throwable t)
		{
			LOGGER.warning("Error saving offline shops. " + t);
		}
		
		// Disconnect all the players from the server
		disconnectAllCharacters();
		
		// Save players data!
		saveAllPlayers();
		
		// Seven Signs data is now saved along with Festival data.
		if (!SevenSigns.getInstance().isSealValidationPeriod())
		{
			SevenSignsFestival.getInstance().saveFestivalData(false);
		}
		
		// Save Seven Signs data before closing. :)
		SevenSigns.getInstance().saveSevenSignsData(null, true);
		LOGGER.info("SevenSigns: All info saved!!");
		
		// Save all raidboss status
		RaidBossSpawnManager.getInstance().cleanUp();
		LOGGER.info("RaidBossSpawnManager: All raidboss info saved!!");
		
		// Save all Grandboss status
		GrandBossManager.getInstance().cleanUp();
		LOGGER.info("GrandBossManager: All Grand Boss info saved!!");
		
		// Save clan data.
		ClanTable.getInstance().shutdown();
		LOGGER.info("Clan System: Data saved!!");
		
		// Save data CountStore
		TradeManager.getInstance().dataCountStore();
		LOGGER.info("TradeController: All count Item Saved!!");
		
		// Save Olympiad status
		try
		{
			Olympiad.getInstance().saveOlympiadStatus();
		}
		catch (Exception e)
		{
			LOGGER.warning("Problem while saving Olympiad: " + e.getMessage());
		}
		LOGGER.info("Olympiad System: Data saved!!");
		
		// Save all manor data
		CastleManorManager.getInstance().save();
		
		// Schemes save.
		SchemeBufferTable.getInstance().saveSchemes();
		LOGGER.info("BufferTable data has been saved!!");
		
		// Save all global (non-player specific) Quest data that needs to persist after reboot
		if (!Config.ALT_DEV_NO_QUESTS)
		{
			QuestManager.getInstance().save();
		}
		
		// Save all global variables data
		GlobalVariablesManager.getInstance().storeMe();
		LOGGER.info("Global Variables Manager: Variables have been saved!!");
		
		// Save items on ground before closing
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			ItemsOnGroundManager.getInstance().cleanUp();
			LOGGER.info("ItemsOnGroundManager: All items on ground saved!!");
		}
		
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e)
		{
			// never happens :p
		}
	}
	
	private void saveAllPlayers()
	{
		LOGGER.info("Saving all players data...");
		
		for (Player player : World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			// Logout Character
			try
			{
				// Unsummon pets
				if (player.getPet() != null)
				{
					player.getPet().unSummon(player);
				}
				
				// Save player status
				player.store();
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	/**
	 * this disconnects all clients from the server
	 */
	private void disconnectAllCharacters()
	{
		LOGGER.info("Disconnecting all players from the Server...");
		
		for (Player player : World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			try
			{
				// Player Disconnect
				if (player.getClient() != null)
				{
					player.getClient().sendPacket(ServerClose.STATIC_PACKET);
					player.getClient().close(LeaveWorld.STATIC_PACKET);
					player.getClient().setPlayer(null);
					player.setClient(null);
				}
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	/**
	 * Get the shutdown-hook instance the shutdown-hook instance is created by the first call of this function, but it has to be registered externally.
	 * @return instance of Shutdown, to be used as shutdown hook
	 */
	public static Shutdown getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Shutdown INSTANCE = new Shutdown();
	}
}
