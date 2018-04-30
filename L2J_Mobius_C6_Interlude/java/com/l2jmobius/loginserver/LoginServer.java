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
package com.l2jmobius.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.Server;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.commons.mmocore.NetcoreConfig;
import com.l2jmobius.commons.mmocore.SelectorConfig;
import com.l2jmobius.commons.mmocore.SelectorThread;
import com.l2jmobius.commons.util.Util;
import com.l2jmobius.status.Status;

public class LoginServer
{
	private final Logger LOGGER = Logger.getLogger(LoginServer.class.getName());
	
	public static final int PROTOCOL_REV = 0x0102;
	
	private static LoginServer _instance;
	
	private GameServerListener _gameServerListener;
	private SelectorThread<L2LoginClient> _selectorThread;
	private Status _statusServer;
	
	public static void main(String[] args)
	{
		_instance = new LoginServer();
	}
	
	public static LoginServer getInstance()
	{
		return _instance;
	}
	
	public LoginServer()
	{
		_instance = this;
		Server.serverMode = Server.MODE_LOGINSERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		final File logFolder = new File(".", LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		catch (IOException e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		// Load LoginServer Configs
		Config.load();
		
		Util.printSection("Database");
		// Prepare Database
		DatabaseFactory.getInstance();
		
		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.severe("Failed initializing LoginController " + e);
			System.exit(1);
		}
		
		try
		{
			GameServerTable.load();
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.severe("Failed to load GameServerTable" + e);
			System.exit(1);
		}
		catch (Exception e)
		{
			LOGGER.severe("Failed to load GameServerTable" + e);
			System.exit(1);
		}
		
		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch (UnknownHostException e1)
			{
				LOGGER.warning("WARNING: The LoginServer bind address is invalid, using all avaliable IPs " + e1);
			}
		}
		// Load telnet status
		if (Config.IS_TELNET_ENABLED)
		{
			try
			{
				_statusServer = new Status(Server.serverMode);
				_statusServer.start();
			}
			catch (IOException e)
			{
				LOGGER.warning("Failed to start the Telnet Server. Reason: " + e.getMessage());
			}
		}
		
		final SelectorConfig sc = new SelectorConfig();
		sc.setMaxReadPerPass(NetcoreConfig.getInstance().MMO_MAX_READ_PER_PASS);
		sc.setMaxSendPerPass(NetcoreConfig.getInstance().MMO_MAX_SEND_PER_PASS);
		sc.setSleepTime(NetcoreConfig.getInstance().MMO_SELECTOR_SLEEP_TIME);
		sc.setHelperBufferCount(NetcoreConfig.getInstance().MMO_HELPER_BUFFER_COUNT);
		
		final L2LoginPacketHandler lph = new L2LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
		}
		catch (IOException e)
		{
			LOGGER.severe("Failed to open Selector " + e);
			System.exit(1);
		}
		
		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			LOGGER.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		}
		catch (IOException e)
		{
			LOGGER.severe("Failed to start the Game Server Listener" + e);
			System.exit(1);
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
			_selectorThread.start();
			LOGGER.info("Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);
			
		}
		catch (IOException e)
		{
			LOGGER.warning("Failed to open server socket" + e);
			System.exit(1);
		}
		
		// load bannedIps
		Config.loadBanFile();
	}
	
	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
	
	public void shutdown(boolean restart)
	{
		LoginController.getInstance().shutdown();
		System.gc();
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}
