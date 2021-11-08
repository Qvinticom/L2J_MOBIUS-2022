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
package org.l2jmobius.loginserver;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseBackup;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.enums.ServerMode;
import org.l2jmobius.commons.util.PropertiesParser;
import org.l2jmobius.loginserver.network.AbstractClientPacket;
import org.l2jmobius.loginserver.network.LoginClient;
import org.l2jmobius.loginserver.ui.Gui;
import org.l2jmobius.telnet.TelnetStatusThread;

/**
 * This class ...
 * @version $Revision: 1.9.4.4 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginServer extends FloodProtectedListener
{
	public static Logger LOGGER = Logger.getLogger(LoginServer.class.getName());
	
	public static int PROTOCOL_REV = 0x0102;
	private static LoginServer INSTANCE;
	private Thread _restartLoginServer;
	private static GameServerListener _gameServerListener;
	private final ThreadPoolExecutor _generalPacketsExecutor;
	private TelnetStatusThread _statusServer;
	private ServerSocket _serverSocket;
	
	public static void main(String[] args)
	{
		// GUI
		final PropertiesParser interfaceConfig = new PropertiesParser(Config.INTERFACE_CONFIG_FILE);
		Config.ENABLE_GUI = interfaceConfig.getBoolean("EnableGUI", true);
		if (Config.ENABLE_GUI && !GraphicsEnvironment.isHeadless())
		{
			Config.DARK_THEME = interfaceConfig.getBoolean("DarkTheme", true);
			System.out.println("LoginServer: Running in GUI mode.");
			new Gui();
		}
		
		// Load log folder first
		loadLogFolder();
		
		// Initialize config
		Config.load(ServerMode.LOGIN);
		
		try
		{
			INSTANCE = new LoginServer();
			INSTANCE.start();
			LOGGER.info("Login Server ready on " + Config.LOGIN_BIND_ADDRESS + ":" + Config.PORT_LOGIN);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Failed to start the Login Server Listener. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
	}
	
	private LoginServer() throws IOException
	{
		super(Config.LOGIN_BIND_ADDRESS, Config.PORT_LOGIN);
		
		// Prepare Database
		try
		{
			DatabaseFactory.init();
		}
		catch (Exception e)
		{
			LOGGER.severe("FATAL: Failed initializing database. Reason: " + e.getMessage());
			System.exit(1);
		}
		
		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Failed initializing LoginController. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		try
		{
			GameServerTable.load();
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Failed to load GameServerTable. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		// Load Ban file
		loadBanFile();
		
		// start packet executor
		_generalPacketsExecutor = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		
		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			LOGGER.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Failed to start the Game Server Listener. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		if (Config.IS_TELNET_ENABLED)
		{
			try
			{
				_statusServer = new TelnetStatusThread();
				_statusServer.start();
			}
			catch (IOException e)
			{
				LOGGER.severe("Failed to start the Telnet Server. Reason: " + e.getMessage());
			}
		}
		else
		{
			// System.out.println("Telnet server is currently disabled.");
		}
	}
	
	private static void loadLogFolder()
	{
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		final File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadBanFile()
	{
		final File bannedFile = new File("./banned_ip.cfg");
		if (bannedFile.exists() && bannedFile.isFile())
		{
			String line;
			String[] parts;
			
			try (FileInputStream fis = new FileInputStream(bannedFile);
				InputStreamReader ir = new InputStreamReader(fis);
				LineNumberReader reader = new LineNumberReader(ir))
			{
				while ((line = reader.readLine()) != null)
				{
					line = line.trim();
					// check if this line isnt a comment line
					if ((line.length() > 0) && (line.charAt(0) != '#'))
					{
						// split comments if any
						parts = line.split("#");
						
						// discard comments in the line, if any
						line = parts[0];
						
						parts = line.split(" ");
						
						final String address = parts[0];
						
						long duration = 0;
						
						if (parts.length > 1)
						{
							try
							{
								duration = Long.parseLong(parts[1]);
							}
							catch (NumberFormatException e)
							{
								LOGGER.warning("Skipped: Incorrect ban duration (" + parts[1] + ") on (" + bannedFile.getName() + "). Line: " + reader.getLineNumber());
								continue;
							}
						}
						
						LoginController.getInstance().addBanForAddress(address, duration);
					}
				}
			}
			catch (IOException e)
			{
				LOGGER.warning("Error while reading the bans file (" + bannedFile.getName() + "). Details: " + e.getMessage());
			}
			LOGGER.config("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
		}
		else
		{
			LOGGER.config("IP Bans file (" + bannedFile.getName() + ") is missing or is a directory, skipped.");
		}
		
		if (Config.LOGIN_SERVER_SCHEDULE_RESTART)
		{
			LOGGER.info("Scheduled LS restart after " + Config.LOGIN_SERVER_SCHEDULE_RESTART_TIME + " hours");
			_restartLoginServer = new LoginServerRestart();
			_restartLoginServer.setDaemon(true);
			_restartLoginServer.start();
		}
	}
	
	public TelnetStatusThread getStatusServer()
	{
		return _statusServer;
	}
	
	public static GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
	
	/**
	 * @param ipAddress
	 * @return
	 */
	public boolean unblockIp(String ipAddress)
	{
		if (LoginController.getInstance().ipBlocked(ipAddress))
		{
			return true;
		}
		
		return false;
	}
	
	public static class ForeignConnection
	{
		public ForeignConnection(long time)
		{
			lastConnection = time;
			connectionNumber = 1;
		}
		
		public int connectionNumber;
		public long lastConnection;
	}
	
	public void execute(AbstractClientPacket packet)
	{
		try
		{
			_generalPacketsExecutor.execute(packet);
		}
		catch (RejectedExecutionException e)
		{
			// if the server is shutdown we ignore
			if (!_generalPacketsExecutor.isShutdown())
			{
				LOGGER.severe("Failed executing: " + packet.getClass().getSimpleName() + " for IP: " + packet.getClient().getSocket().getInetAddress().getHostAddress());
			}
		}
	}
	
	class LoginServerRestart extends Thread
	{
		public LoginServerRestart()
		{
			setName("LoginServerRestart");
		}
		
		@Override
		public void run()
		{
			while (!isInterrupted())
			{
				try
				{
					Thread.sleep(Config.LOGIN_SERVER_SCHEDULE_RESTART_TIME * 3600000);
				}
				catch (InterruptedException e)
				{
					return;
				}
				shutdown(true);
			}
		}
	}
	
	public void shutdown(boolean restart)
	{
		// Backup database.
		if (Config.BACKUP_DATABASE)
		{
			DatabaseBackup.performBackup();
		}
		
		// shut down executor
		try
		{
			_generalPacketsExecutor.awaitTermination(1, TimeUnit.SECONDS);
			_generalPacketsExecutor.shutdown();
		}
		catch (Throwable t)
		{
		}
		
		_gameServerListener.interrupt();
		GameServerTable.getInstance().shutDown();
		
		try
		{
			if (_serverSocket != null)
			{
				_serverSocket.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2jmobius.loginserver.FloodProtectedListener#addClient(java.net.Socket)
	 */
	@Override
	public void addClient(Socket socket)
	{
		new LoginClient(socket);
	}
	
	public static LoginServer getInstance()
	{
		return INSTANCE;
	}
}