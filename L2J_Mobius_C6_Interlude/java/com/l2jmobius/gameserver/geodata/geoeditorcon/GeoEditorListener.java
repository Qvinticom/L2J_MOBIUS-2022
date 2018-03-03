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
package com.l2jmobius.gameserver.geodata.geoeditorcon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.l2jmobius.Config;

public class GeoEditorListener extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(GeoEditorListener.class.getName());
	
	private static final int PORT = Config.GEOEDITOR_PORT;
	
	private static final class SingletonHolder
	{
		protected static final GeoEditorListener INSTANCE = new GeoEditorListener();
	}
	
	public static GeoEditorListener getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private ServerSocket _serverSocket;
	private GeoEditorThread _geoEditor;
	
	protected GeoEditorListener()
	{
		try
		{
			_serverSocket = new ServerSocket(PORT);
		}
		catch (IOException e)
		{
			LOGGER.warning("Error creating geoeditor listener! " + e);
			System.exit(1);
		}
		start();
		LOGGER.info("GeoEditorListener Initialized.");
	}
	
	public GeoEditorThread getThread()
	{
		return _geoEditor;
	}
	
	public String getStatus()
	{
		if ((_geoEditor != null) && _geoEditor.isWorking())
		{
			return "Geoeditor connected.";
		}
		return "Geoeditor not connected.";
	}
	
	@Override
	public void run()
	{
		Socket connection = null;
		try
		{
			while (true)
			{
				connection = _serverSocket.accept();
				if ((_geoEditor != null) && _geoEditor.isWorking())
				{
					LOGGER.warning("Geoeditor already connected!");
					connection.close();
					continue;
				}
				LOGGER.info("Received geoeditor connection from: " + connection.getInetAddress().getHostAddress());
				_geoEditor = new GeoEditorThread(connection);
				_geoEditor.start();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("GeoEditorListener: " + e);
			try
			{
				if (connection != null)
				{
					connection.close();
				}
			}
			catch (Exception e2)
			{
			}
		}
		finally
		{
			try
			{
				_serverSocket.close();
			}
			catch (IOException io)
			{
				LOGGER.warning(io.getMessage());
			}
			LOGGER.warning("GeoEditorListener Closed!");
		}
	}
}