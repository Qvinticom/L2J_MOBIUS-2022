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

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.network.Connection;

public class LoginController
{
	private static LoginController _instance;
	private final Map<String, Integer> _logins = new HashMap<>();
	private final Map<String, Socket> _accountsInLoginServer;
	private final Map<String, Connection> _accountsInGameServer = new HashMap<>();
	private final Map<String, Integer> _accessLevels;
	private int _maxAllowedOnlinePlayers;
	
	private LoginController()
	{
		_accountsInLoginServer = new HashMap<>();
		_accessLevels = new HashMap<>();
	}
	
	public static LoginController getInstance()
	{
		if (_instance == null)
		{
			_instance = new LoginController();
		}
		return _instance;
	}
	
	public int assignSessionKeyToLogin(String account, int accessLevel, Socket cSocket)
	{
		int key = -1;
		key = (int) System.currentTimeMillis() & 0xFFFFFF;
		_logins.put(account, key);
		_accountsInLoginServer.put(account, cSocket);
		_accessLevels.put(account, accessLevel);
		return key;
	}
	
	public void addGameServerLogin(String account, Connection connection)
	{
		_accountsInGameServer.put(account, connection);
	}
	
	public void removeGameServerLogin(String account)
	{
		if (account != null)
		{
			_logins.remove(account);
			_accountsInGameServer.remove(account);
		}
	}
	
	public void removeLoginServerLogin(String account)
	{
		if (account != null)
		{
			_accountsInLoginServer.remove(account);
		}
	}
	
	public boolean isAccountInLoginServer(String account)
	{
		return _accountsInLoginServer.containsKey(account);
	}
	
	public boolean isAccountInGameServer(String account)
	{
		return _accountsInGameServer.containsKey(account);
	}
	
	public int getKeyForAccount(String account)
	{
		int key = 0;
		final Integer result = _logins.get(account);
		if (result != null)
		{
			key = result;
		}
		return key;
	}
	
	public int getOnlinePlayerCount()
	{
		return _accountsInGameServer.size();
	}
	
	public int getMaxAllowedOnlinePlayers()
	{
		return _maxAllowedOnlinePlayers;
	}
	
	public void setMaxAllowedOnlinePlayers(int maxAllowedOnlinePlayers)
	{
		_maxAllowedOnlinePlayers = maxAllowedOnlinePlayers;
	}
	
	public boolean loginPossible(int access)
	{
		return (_accountsInGameServer.size() < _maxAllowedOnlinePlayers) || (access >= 50);
	}
	
	public int getGmAccessLevel(String loginName)
	{
		return _accessLevels.get(loginName);
	}
	
	public Connection getClientConnection(String loginName)
	{
		return _accountsInGameServer.get(loginName);
	}
	
	public Socket getLoginServerConnection(String loginName)
	{
		return _accountsInLoginServer.get(loginName);
	}
}
