/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.l2jmobius.gameserver.GameServer;
import org.l2jmobius.gameserver.ui.Gui;
import org.l2jmobius.loginserver.LoginServer;
import org.l2jmobius.util.PropertiesParser;

public class Server
{
	public static void main(String[] args) throws Exception
	{
		// Create log folder
		final File logFolder = new File(".", "log");
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("log.cfg")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Load configs.
		Config.load();
		
		// GUI
		final PropertiesParser interfaceConfig = new PropertiesParser(Config.INTERFACE_CONFIG_FILE);
		Config.ENABLE_GUI = interfaceConfig.getBoolean("EnableGUI", true);
		if (Config.ENABLE_GUI && !GraphicsEnvironment.isHeadless())
		{
			Config.DARK_THEME = interfaceConfig.getBoolean("DarkTheme", true);
			System.out.println("Server: Running in GUI mode.");
			new Gui();
		}
		
		// Start game server.
		final GameServer gameServer = new GameServer();
		gameServer.start();
		
		// Start login server.
		final LoginServer loginServer = new LoginServer();
		loginServer.start();
	}
}
