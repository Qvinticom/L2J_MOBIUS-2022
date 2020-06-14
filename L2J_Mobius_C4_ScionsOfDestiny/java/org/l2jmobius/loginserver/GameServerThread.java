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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.crypt.NewCrypt;
import org.l2jmobius.loginserver.network.gameserverpackets.BlowFishKey;
import org.l2jmobius.loginserver.network.gameserverpackets.ChangeAccessLevel;
import org.l2jmobius.loginserver.network.gameserverpackets.GameServerAuth;
import org.l2jmobius.loginserver.network.gameserverpackets.PlayerAuthRequest;
import org.l2jmobius.loginserver.network.gameserverpackets.PlayerInGame;
import org.l2jmobius.loginserver.network.gameserverpackets.PlayerLogout;
import org.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;
import org.l2jmobius.loginserver.network.loginserverpackets.AuthResponse;
import org.l2jmobius.loginserver.network.loginserverpackets.InitLS;
import org.l2jmobius.loginserver.network.loginserverpackets.KickPlayer;
import org.l2jmobius.loginserver.network.loginserverpackets.LoginServerFail;
import org.l2jmobius.loginserver.network.loginserverpackets.PlayerAuthResponse;
import org.l2jmobius.loginserver.network.serverpackets.ServerBasePacket;

/**
 * @author -Wooden-
 */
public class GameServerThread extends Thread
{
	protected static Logger LOGGER = Logger.getLogger(GameServerThread.class.getName());
	private final Socket _connection;
	private InputStream _in;
	private OutputStream _out;
	private final RSAPublicKey _publicKey;
	private final RSAPrivateKey _privateKey;
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private boolean _isAuthed = false;
	private final String _connectionIp;
	private int _max_players;
	private final List<String> _accountsInGame;
	private int _server_id;
	private boolean _isTestServer;
	private boolean _PvpServer;
	private int _gamePort;
	private byte[] _hexID;
	private String connectionIpAddress;
	private String _gameExternalHost;
	private String _gameInternalHost;
	private String _gameExternalIP;
	private String _gameInternalIP;
	
	/**
	 * @return Returns the hexID.
	 */
	public byte[] getHexID()
	{
		return _hexID;
	}
	
	public List<String> getPlayersInGame()
	{
		return _accountsInGame;
	}
	
	@Override
	public void run()
	{
		try
		{
			final InitLS startPacket = new InitLS(_publicKey.getModulus().toByteArray());
			sendPacket(startPacket);
			// register server and pass this to a GameServerThread
			connectionIpAddress = _connection.getInetAddress().getHostAddress();
			if (isBannedGameserverIP(connectionIpAddress))
			{
				final LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_IP_BANNED);
				sendPacket(lsf);
				// throw new IOException("banned IP");
				LOGGER.info("GameServerRegistration: IP Address " + connectionIpAddress + " is on Banned IP list.");
			}
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			while (true)
			{
				lengthLo = _in.read();
				lengthHi = _in.read();
				length = (lengthHi * 256) + lengthLo;
				
				if ((lengthHi < 0) || _connection.isClosed())
				{
					LOGGER.finer("LoginServerThread: Login terminated the connection.");
					break;
				}
				
				byte[] data = new byte[length - 2];
				
				int receivedBytes = 0;
				int newBytes = 0;
				int left = length - 2;
				while ((newBytes != -1) && (receivedBytes < (length - 2)))
				{
					newBytes = _in.read(data, receivedBytes, left);
					receivedBytes = receivedBytes + newBytes;
					left -= newBytes;
				}
				
				if (receivedBytes != (length - 2))
				{
					LOGGER.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
					break;
				}
				
				// decrypt if we have a key
				_blowfish.decrypt(data, 0, data.length);
				checksumOk = NewCrypt.verifyChecksum(data);
				if (!checksumOk)
				{
					LOGGER.warning("Incorrect packet checksum, closing connection (LS)");
					return;
				}
				
				final int packetType = data[0] & 0xff;
				switch (packetType)
				{
					case 00:
					{
						final BlowFishKey bfk = new BlowFishKey(data, _privateKey);
						_blowfishKey = bfk.getKey();
						_blowfish = new NewCrypt(_blowfishKey);
						break;
					}
					case 01:
					{
						final GameServerAuth gsa = new GameServerAuth(data);
						// _log.info("Auth request received");
						handleRegistrationProcess(gsa);
						if (_isAuthed)
						{
							final AuthResponse ar = new AuthResponse(_server_id);
							sendPacket(ar);
							broadcastToTelnet("GameServer [" + _server_id + "] " + GameServerTable.getInstance()._serverNames.get(_server_id) + " is connected.");
						}
						else
						{
							LOGGER.info("Closing connection.");
							_connection.close();
						}
						break;
					}
					case 02:
					{
						if (!_isAuthed)
						{
							final LoginServerFail lsf = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(lsf);
							_connection.close();
							break;
						}
						final PlayerInGame pig = new PlayerInGame(data);
						for (String account : pig.getAccounts())
						{
							_accountsInGame.add(account);
							broadcastToTelnet("Account " + account + " logged in GameServer " + _server_id);
						}
						break;
					}
					case 03:
					{
						if (!_isAuthed)
						{
							final LoginServerFail lsf = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(lsf);
							_connection.close();
							break;
						}
						final PlayerLogout plo = new PlayerLogout(data);
						_accountsInGame.remove(plo.getAccount());
						broadcastToTelnet("Player " + plo.getAccount() + " disconnected from GameServer " + _server_id);
						break;
					}
					case 04:
					{
						if (!_isAuthed)
						{
							final LoginServerFail lsf = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(lsf);
							_connection.close();
							break;
						}
						final ChangeAccessLevel cal = new ChangeAccessLevel(data);
						LoginController.getInstance().setAccountAccessLevel(cal.getAccount(), cal.getLevel());
						LOGGER.info("Changed " + cal.getAccount() + " access level to" + cal.getLevel());
						break;
					}
					case 05:
					{
						if (!_isAuthed)
						{
							final LoginServerFail lsf = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(lsf);
							_connection.close();
							break;
						}
						
						final PlayerAuthRequest par = new PlayerAuthRequest(data);
						final SessionKey key = LoginController.getInstance().getKeyForAccount(par.getAccount());
						if ((key != null) && key.equals(par.getKey()))
						{
							sendPacket(new PlayerAuthResponse(par.getAccount(), true));
						}
						else
						{
							sendPacket(new PlayerAuthResponse(par.getAccount(), false));
						}
						LoginController.getInstance().removeLoginClient(par.getAccount());
						break;
					}
					case 06:
					{
						if (!_isAuthed)
						{
							final LoginServerFail lsf = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(lsf);
							_connection.close();
							break;
						}
						
						@SuppressWarnings("unused")
						final ServerStatus ss = new ServerStatus(data, _server_id); // will do the actions by itself
						break;
					}
				}
			}
		}
		catch (IOException e)
		{
			final String serverName = (_server_id != -1 ? "[" + _server_id + "] " + GameServerTable.getInstance()._serverNames.get(_server_id) : "(" + connectionIpAddress + ")");
			final String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage();
			LOGGER.info(msg);
			broadcastToTelnet(msg);
		}
		finally
		{
			if (_isAuthed)
			{
				GameServerTable.getInstance().setServerReallyDown(_server_id);
				LOGGER.info("Server " + GameServerTable.getInstance()._serverNames.get(_server_id) + " (" + _server_id + ") : Set as disconnected");
			}
			LoginServer.getGameServerListener().removeGameServer(this);
			LoginServer.getGameServerListener().removeFloodProtection(_connectionIp);
		}
	}
	
	/**
	 * @param gameServerauth
	 */
	private void handleRegistrationProcess(GameServerAuth gameServerauth)
	{
		try
		{
			if (GameServerTable.getInstance().isARegisteredServer(gameServerauth.getHexID()))
			{
				_server_id = GameServerTable.getInstance().getServerIDforHex(gameServerauth.getHexID());
				if (GameServerTable.getInstance().isServerAuthed(_server_id))
				{
					final LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_ALREADY_LOGGED8IN);
					sendPacket(lsf);
					_connection.close();
					return;
				}
				_gamePort = gameServerauth.getPort();
				setGameHosts(gameServerauth.getExternalHost(), gameServerauth.getInternalHost());
				_max_players = gameServerauth.getMaxPlayers();
				_hexID = gameServerauth.getHexID();
				GameServerTable.getInstance().addServer(this);
			}
			else if (Config.ACCEPT_NEW_GAMESERVER)
			{
				if (!gameServerauth.acceptAlternateID())
				{
					if (GameServerTable.getInstance().isIDfree(gameServerauth.getDesiredID()))
					{
						_server_id = gameServerauth.getDesiredID();
						_gamePort = gameServerauth.getPort();
						setGameHosts(gameServerauth.getExternalHost(), gameServerauth.getInternalHost());
						_max_players = gameServerauth.getMaxPlayers();
						_hexID = gameServerauth.getHexID();
						GameServerTable.getInstance().createServer(this);
						GameServerTable.getInstance().addServer(this);
					}
					else
					{
						final LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_ID_RESERVED);
						sendPacket(lsf);
						_connection.close();
						return;
					}
				}
				else
				{
					int id;
					if (!GameServerTable.getInstance().isIDfree(gameServerauth.getDesiredID()))
					{
						id = GameServerTable.getInstance().findFreeID();
						if (id < 0)
						{
							final LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_NO_FREE_ID);
							sendPacket(lsf);
							_connection.close();
							return;
						}
					}
					else
					{
						id = gameServerauth.getDesiredID();
					}
					_server_id = id;
					_gamePort = gameServerauth.getPort();
					setGameHosts(gameServerauth.getExternalHost(), gameServerauth.getInternalHost());
					_max_players = gameServerauth.getMaxPlayers();
					_hexID = gameServerauth.getHexID();
					GameServerTable.getInstance().createServer(this);
					GameServerTable.getInstance().addServer(this);
				}
			}
			else
			{
				LOGGER.info("Wrong HexID");
				final LoginServerFail lsf = new LoginServerFail(LoginServerFail.REASON_WRONG_HEXID);
				sendPacket(lsf);
				_connection.close();
				return;
			}
			
		}
		catch (IOException e)
		{
			LOGGER.info("Error while registering GameServer " + GameServerTable.getInstance()._serverNames.get(_server_id) + " (ID:" + _server_id + ")");
		}
	}
	
	/**
	 * @param ipAddress
	 * @return
	 */
	public static boolean isBannedGameserverIP(String ipAddress)
	{
		return false;
	}
	
	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
		try
		{
			_in = _connection.getInputStream();
			_out = new BufferedOutputStream(_connection.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		final KeyPair pair = GameServerTable.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
		_accountsInGame = new CopyOnWriteArrayList<>();
		start();
	}
	
	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(ServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		_blowfish.crypt(data, 0, data.length);
		
		final int len = data.length + 2;
		synchronized (_out)
		{
			_out.write(len & 0xff);
			_out.write((len >> 8) & 0xff);
			_out.write(data);
			_out.flush();
		}
	}
	
	private void broadcastToTelnet(String msg)
	{
		if (LoginServer.getInstance().getStatusServer() != null)
		{
			LoginServer.getInstance().getStatusServer().sendMessageToTelnets(msg);
		}
	}
	
	public void kickPlayer(String account)
	{
		final KickPlayer kp = new KickPlayer(account);
		try
		{
			sendPacket(kp);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @return Returns the max_players.
	 */
	public int getMaxPlayers()
	{
		return _max_players;
	}
	
	/**
	 * @return Returns the current_players.
	 */
	public int getCurrentPlayers()
	{
		return _accountsInGame.size();
	}
	
	/**
	 * @return Returns the server_id.
	 */
	public int getServerID()
	{
		return _server_id;
	}
	
	/**
	 * @return Returns the external game Host.
	 */
	public String getGameExternalHost()
	{
		return _gameExternalHost;
	}
	
	/**
	 * @return Returns the internal game Host.
	 */
	public String getGameInternalHost()
	{
		return _gameInternalHost;
	}
	
	/**
	 * @return
	 */
	public int getPort()
	{
		return _gamePort;
	}
	
	/**
	 * @return
	 */
	public boolean getPvP()
	{
		return _PvpServer;
	}
	
	/**
	 * @return
	 */
	public boolean isTestServer()
	{
		return _isTestServer;
	}
	
	/**
	 * @param gameExternalHost The game External Host to set.
	 * @param gameInternalHost The game Internal Host to set.
	 */
	public void setGameHosts(String gameExternalHost, String gameInternalHost)
	{
		final String oldInternal = _gameInternalHost;
		final String oldExternal = _gameExternalHost;
		_gameExternalHost = gameExternalHost;
		_gameInternalHost = gameInternalHost;
		if (!_gameExternalHost.equals("*"))
		{
			try
			{
				_gameExternalIP = InetAddress.getByName(_gameExternalHost).getHostAddress();
			}
			catch (UnknownHostException e)
			{
				LOGGER.warning("Couldn't resolve hostname \"" + _gameExternalHost + "\"");
			}
		}
		else
		{
			_gameExternalIP = _connectionIp;
		}
		if (!_gameInternalHost.equals("*"))
		{
			try
			{
				_gameInternalIP = InetAddress.getByName(_gameInternalHost).getHostAddress();
			}
			catch (UnknownHostException e)
			{
				LOGGER.warning("Couldn't resolve hostname \"" + _gameExternalHost + "\"");
			}
		}
		else
		{
			_gameInternalIP = _connectionIp;
		}
		
		final String serverName = GameServerTable.getInstance()._serverNames.get(_server_id);
		if ((oldInternal == null) || !oldInternal.equalsIgnoreCase(_gameInternalIP))
		{
			LOGGER.info("Updated Gameserver " + serverName + " Internal IP to: " + _gameInternalIP);
		}
		if ((oldExternal == null) || !oldExternal.equalsIgnoreCase(_gameExternalIP))
		{
			LOGGER.info("Updated Gameserver " + serverName + " External IP to: " + _gameExternalIP);
		}
	}
	
	/**
	 * @return Returns the game server's external IP.
	 */
	public String getGameExternalIP()
	{
		return _gameExternalIP;
	}
	
	/**
	 * @return Returns the game server's internal IP.
	 */
	public String getGameInternalIP()
	{
		return _gameInternalIP;
	}
	
	/**
	 * @return Returns the isAuthed.
	 */
	public boolean isAuthed()
	{
		return _isAuthed;
	}
	
	/**
	 * @param isAuthed The isAuthed to set.
	 */
	public void setAuthed(boolean isAuthed)
	{
		_isAuthed = isAuthed;
	}
	
	/**
	 * @param value
	 */
	public void setMaxPlayers(int value)
	{
		_max_players = value;
	}
	
	/**
	 * @return Returns the connectionIpAddress.
	 */
	public String getConnectionIpAddress()
	{
		return connectionIpAddress;
	}
}