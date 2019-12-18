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
package org.l2jmobius.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.loginserver.data.AccountData;
import org.l2jmobius.loginserver.network.ClientThread;

public class LoginServer extends Thread
{
	private ServerSocket _serverSocket;
	private final AccountData _logins;
	private String _ip;
	private final int _gamePort;
	static Logger _log = Logger.getLogger(LoginServer.class.getName());
	
	public static void main(String[] args) throws IOException
	{
		final LoginServer server = new LoginServer();
		_log.config("LoginServer Listening on port 2106");
		server.start();
	}
	
	@Override
	public void run()
	{
		do
		{
			try
			{
				do
				{
					// _log.fine("Waiting for client connection...");
					final Socket connection = _serverSocket.accept();
					// _log.fine("Connection from " + connection.getInetAddress());
					final String connectedIp = connection.getInetAddress().getHostAddress();
					if (connectedIp.startsWith("192.168.") || connectedIp.startsWith("10."))
					{
						// _log.fine("Using internal ip as server ip " + Config.INTERNAL_HOST_NAME);
						new ClientThread(connection, _logins, Config.INTERNAL_HOST_NAME, _gamePort);
						continue;
					}
					// _log.fine("Using external ip as server ip " + Config.EXTERNAL_HOST_NAME);
					new ClientThread(connection, _logins, Config.EXTERNAL_HOST_NAME, _gamePort);
				}
				while (true);
			}
			catch (IOException e)
			{
			}
		}
		while (true);
	}
	
	public LoginServer() throws IOException
	{
		super("LoginServer");
		
		if (!Config.LOGIN_HOST_NAME.equals("*"))
		{
			final InetAddress adr = InetAddress.getByName(Config.LOGIN_HOST_NAME);
			_ip = adr.getHostAddress();
			_log.config("LoginServer listening on IP:" + _ip + " Port 2106");
			_serverSocket = new ServerSocket(2106, 50, adr);
		}
		else
		{
			_log.config("LoginServer listening on all available IPs on Port 2106");
			_serverSocket = new ServerSocket(2106);
		}
		_log.config("Hostname for external connections is: " + Config.EXTERNAL_HOST_NAME);
		_log.config("Hostname for internal connections is: " + Config.INTERNAL_HOST_NAME);
		_logins = new AccountData(Config.AUTO_CREATE_ACCOUNTS);
		_gamePort = Config.SERVER_PORT;
		
		try
		{
			final File bannedFile = new File("banned_ip.cfg");
			if (bannedFile.isFile() && bannedFile.exists())
			{
				int count = 0;
				final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(bannedFile)));
				String line = null;
				while ((line = lnr.readLine()) != null)
				{
					if ((line = line.trim()).length() <= 0)
					{
						continue;
					}
					++count;
					ClientThread.addBannedIP(line);
				}
				lnr.close();
				_log.info(count + " banned IPs defined.");
			}
			else
			{
				_log.info("banned_ip.cfg not found.");
			}
		}
		catch (Exception e)
		{
			_log.warning("error while reading banned file:" + e);
		}
	}
}
