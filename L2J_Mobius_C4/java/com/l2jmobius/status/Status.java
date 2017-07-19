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
package com.l2jmobius.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

import com.l2jmobius.Config;
import com.l2jmobius.Server;
import com.l2jmobius.util.Rnd;

import javolution.text.TextBuilder;
import javolution.util.FastList;

public class Status extends Thread
{
	private final ServerSocket statusServerSocket;
	
	private final int _uptime;
	private String _StatusPW;
	private final int _mode;
	private final List<LoginStatusThread> _loginStatus;
	
	@Override
	public void run()
	{
		setPriority(Thread.MAX_PRIORITY);
		while (true)
		{
			try
			{
				final Socket connection = statusServerSocket.accept();
				
				if (_mode == Server.MODE_GAMESERVER)
				{
					new GameStatusThread(connection, _uptime, _StatusPW);
				}
				else if (_mode == Server.MODE_LOGINSERVER)
				{
					final LoginStatusThread lst = new LoginStatusThread(connection, _uptime, _StatusPW);
					if (lst.isAlive())
					{
						_loginStatus.add(lst);
					}
				}
				if (isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch (final IOException io)
					{
						io.printStackTrace();
					}
					break;
				}
			}
			catch (final IOException e)
			{
				if (isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch (final IOException io)
					{
						io.printStackTrace();
					}
					break;
				}
			}
		}
	}
	
	public Status(int mode) throws IOException
	{
		super("Status");
		_mode = mode;
		final Properties telnetSettings = new Properties();
		try (InputStream is = new FileInputStream(new File(Config.TELNET_FILE)))
		{
			telnetSettings.load(is);
		}
		
		_StatusPW = telnetSettings.getProperty("StatusPW");
		if ((_mode == Server.MODE_GAMESERVER) || (_mode == Server.MODE_LOGINSERVER))
		{
			if (_StatusPW == null)
			{
				System.out.println("Server's Telnet Function Has No Password Defined!");
				System.out.println("A Password Has Been Automatically Created!");
				_StatusPW = RndPW(10);
				System.out.println("Password Has Been Set To: " + _StatusPW);
			}
			
			System.out.println("Telnet StatusServer started successfully, listening on Port: " + Config.TELNET_PORT);
		}
		
		statusServerSocket = new ServerSocket(Config.TELNET_PORT);
		_uptime = (int) System.currentTimeMillis();
		_loginStatus = new FastList<>();
	}
	
	private String RndPW(int length)
	{
		final TextBuilder password = new TextBuilder();
		final String lowerChar = "qwertyuiopasdfghjklzxcvbnm";
		final String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
		final String digits = "1234567890";
		
		for (int i = 0; i < length; i++)
		{
			final int charSet = Rnd.nextInt(3);
			switch (charSet)
			{
				case 0:
					password.append(lowerChar.charAt(Rnd.nextInt(lowerChar.length() - 1)));
					break;
				case 1:
					password.append(upperChar.charAt(Rnd.nextInt(upperChar.length() - 1)));
					break;
				case 2:
					password.append(digits.charAt(Rnd.nextInt(digits.length() - 1)));
					break;
			}
		}
		return password.toString();
	}
	
	public void SendMessageToTelnets(String msg)
	{
		final List<LoginStatusThread> lsToRemove = new FastList<>();
		for (final LoginStatusThread ls : _loginStatus)
		{
			if (ls.isInterrupted())
			{
				lsToRemove.add(ls);
			}
			else
			{
				ls.printToTelnet(msg);
			}
		}
	}
}