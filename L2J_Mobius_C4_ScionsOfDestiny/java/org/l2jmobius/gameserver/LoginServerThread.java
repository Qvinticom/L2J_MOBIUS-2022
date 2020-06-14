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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.crypt.NewCrypt;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ConnectionState;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.gameserverpackets.AuthRequest;
import org.l2jmobius.gameserver.network.gameserverpackets.BlowFishKey;
import org.l2jmobius.gameserver.network.gameserverpackets.ChangeAccessLevel;
import org.l2jmobius.gameserver.network.gameserverpackets.GameServerBasePacket;
import org.l2jmobius.gameserver.network.gameserverpackets.PlayerAuthRequest;
import org.l2jmobius.gameserver.network.gameserverpackets.PlayerInGame;
import org.l2jmobius.gameserver.network.gameserverpackets.PlayerLogout;
import org.l2jmobius.gameserver.network.gameserverpackets.ServerStatus;
import org.l2jmobius.gameserver.network.loginserverpackets.AuthResponse;
import org.l2jmobius.gameserver.network.loginserverpackets.InitLS;
import org.l2jmobius.gameserver.network.loginserverpackets.KickPlayer;
import org.l2jmobius.gameserver.network.loginserverpackets.LoginServerFail;
import org.l2jmobius.gameserver.network.loginserverpackets.PlayerAuthResponse;
import org.l2jmobius.gameserver.network.serverpackets.AuthLoginFail;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectInfo;

public class LoginServerThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(LoginServerThread.class.getName());
	
	/** The LoginServerThread singleton */
	private static LoginServerThread _instance;
	
	private static final int REVISION = 0x0102;
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private Socket _loginSocket;
	private OutputStream _out;
	
	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake.
	 */
	private NewCrypt _blowfish;
	private byte[] _hexID;
	private final boolean _acceptAlternate;
	private final int _requestID;
	private int _serverID;
	private final boolean _reserveHost;
	private int _maxPlayer;
	private final List<WaitingClient> _waitingClients = new CopyOnWriteArrayList<>();
	private final Map<String, GameClient> _accountsInGameServer = new ConcurrentHashMap<>();
	private int _status;
	private String _serverName;
	private final String _gameExternalHost;
	private final String _gameInternalHost;
	
	public LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_hexID = generateHex(16);
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_requestID = Config.REQUEST_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_gameExternalHost = Config.EXTERNAL_HOSTNAME;
		_gameInternalHost = Config.INTERNAL_HOSTNAME;
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}
	
	public static LoginServerThread getInstance()
	{
		if (_instance == null)
		{
			_instance = new LoginServerThread();
		}
		return _instance;
	}
	
	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection
				LOGGER.info("Connecting to login on " + _hostname + ":" + _port);
				_loginSocket = new Socket(_hostname, _port);
				final InputStream in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());
				
				// init Blowfish
				final byte[] blowfishKey = generateHex(40);
				// Protect the new blowfish key what cannot begin with zero
				if (blowfishKey[0] == 0)
				{
					blowfishKey[0] = (byte) Rnd.get(32, 64);
				}
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!isInterrupted())
				{
					lengthLo = in.read();
					lengthHi = in.read();
					length = (lengthHi * 256) + lengthLo;
					
					if (lengthHi < 0)
					{
						LOGGER.finer("LoginServerThread: Login terminated the connection.");
						break;
					}
					
					final byte[] incoming = new byte[length - 2];
					
					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					while ((newBytes != -1) && (receivedBytes < (length - 2)))
					{
						newBytes = in.read(incoming, receivedBytes, left);
						receivedBytes = receivedBytes + newBytes;
						left -= newBytes;
					}
					
					if (receivedBytes != (length - 2))
					{
						LOGGER.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					
					// decrypt if we have a key
					_blowfish.decrypt(incoming, 0, incoming.length);
					checksumOk = NewCrypt.verifyChecksum(incoming);
					
					if (!checksumOk)
					{
						LOGGER.warning("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					switch (incoming[0] & 0xff)
					{
						case 0x00:
						{
							final InitLS init = new InitLS(incoming);
							if (init.getRevision() != REVISION)
							{
								// TODO: revision mismatch
								LOGGER.warning("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							
							RSAPublicKey publicKey;
							
							try
							{
								publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(init.getRSAKey()), RSAKeyGenParameterSpec.F4));
							}
							catch (GeneralSecurityException e)
							{
								LOGGER.warning("Trouble while init the public key send by login");
								break;
							}
							// send the blowfish key through the rsa encryption
							sendPacket(new BlowFishKey(blowfishKey, publicKey));
							// now, only accept packet with the new encryption
							_blowfish = new NewCrypt(blowfishKey);
							final AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost, _maxPlayer);
							sendPacket(ar);
							break;
						}
						case 0x01:
						{
							final LoginServerFail lsf = new LoginServerFail(incoming);
							LOGGER.info("Damn! Registration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						}
						case 0x02:
						{
							final AuthResponse aresp = new AuthResponse(incoming);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							LOGGER.info("Registered on login as Server " + _serverID + " : " + _serverName);
							final ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_CLOCK)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_TESTSERVER)
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.OFF);
							}
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							sendPacket(st);
							if (World.getAllPlayersCount() > 0)
							{
								final List<String> playerList = new ArrayList<>();
								for (PlayerInstance player : World.getInstance().getAllPlayers())
								{
									playerList.add(player.getAccountName());
								}
								final PlayerInGame pig = new PlayerInGame(playerList);
								sendPacket(pig);
							}
							break;
						}
						case 0x03:
						{
							final PlayerAuthResponse par = new PlayerAuthResponse(incoming);
							final String account = par.getAccount();
							WaitingClient wcToRemove = null;
							for (WaitingClient wc : _waitingClients)
							{
								if (wc.account.equals(account))
								{
									wcToRemove = wc;
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									final PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setState(ConnectionState.AUTHENTICATED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									final CharSelectInfo cl = new CharSelectInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.getConnection().sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								}
								else
								{
									LOGGER.warning("Session key is not correct. Closing connection for account " + wcToRemove.account + ".");
									wcToRemove.gameClient.sendPacket(new AuthLoginFail(AuthLoginFail.SYSTEM_ERROR_LOGIN_LATER));
									wcToRemove.gameClient.close(new AuthLoginFail(AuthLoginFail.SYSTEM_ERROR_LOGIN_LATER));
									_accountsInGameServer.remove(wcToRemove.account);
								}
								_waitingClients.remove(wcToRemove);
							}
							break;
						}
						case 0x04:
						{
							final KickPlayer kp = new KickPlayer(incoming);
							doKickPlayer(kp.getAccount());
							break;
						}
					}
				}
			}
			catch (UnknownHostException e)
			{
				LOGGER.info("Disconnected from Login, Trying to reconnect:");
				LOGGER.info(e.toString());
			}
			catch (IOException e)
			{
				LOGGER.info("Disconnected from Login, Trying to reconnect:");
				LOGGER.info(e.toString());
			}
			finally
			{
				try
				{
					_loginSocket.close();
				}
				catch (Exception e)
				{
				}
			}
			
			try
			{
				Thread.sleep(5000); // 5 seconds tempo.
			}
			catch (InterruptedException e)
			{
				return;
			}
		}
	}
	
	public void addWaitingClientAndSendRequest(String acc, GameClient client, SessionKey key)
	{
		final WaitingClient wc = new WaitingClient(acc, client, key);
		_waitingClients.add(wc);
		final PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		try
		{
			sendPacket(par);
		}
		catch (IOException e)
		{
			LOGGER.warning("Error while sending player auth request.");
		}
	}
	
	public void removeWaitingClient(GameClient client)
	{
		WaitingClient toRemove = null;
		for (WaitingClient c : _waitingClients)
		{
			if (c.gameClient == client)
			{
				toRemove = c;
			}
		}
		if (toRemove != null)
		{
			_waitingClients.remove(toRemove);
		}
	}
	
	public void sendLogout(String account)
	{
		if (account == null)
		{
			return;
		}
		final PlayerLogout pl = new PlayerLogout(account);
		try
		{
			sendPacket(pl);
		}
		catch (IOException e)
		{
			LOGGER.warning("Error while sending logout packet to login.");
		}
		finally
		{
			_accountsInGameServer.remove(account);
		}
	}
	
	public boolean addGameServerLogin(String account, GameClient client)
	{
		final GameClient savedClient = _accountsInGameServer.get(account);
		if (savedClient != null)
		{
			if (savedClient.isDetached())
			{
				_accountsInGameServer.put(account, client);
				return true;
			}
			
			savedClient.closeNow();
			_accountsInGameServer.remove(account);
			return false;
		}
		
		_accountsInGameServer.put(account, client);
		return true;
	}
	
	public void sendAccessLevel(String account, int level)
	{
		final ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (IOException e)
		{
		}
	}
	
	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	public void doKickPlayer(String account)
	{
		if (_accountsInGameServer.get(account) != null)
		{
			_accountsInGameServer.get(account).closeNow();
			getInstance().sendLogout(account);
		}
	}
	
	public static byte[] generateHex(int size)
	{
		final byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}
	
	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(GameServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		_blowfish.crypt(data, 0, data.length);
		
		final int len = data.length + 2;
		synchronized (_out) // avoids two threads writing in the mean time
		{
			_out.write(len & 0xff);
			_out.write((len >> 8) & 0xff);
			_out.write(data);
			_out.flush();
		}
	}
	
	/**
	 * Sets the max player.
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}
	
	/**
	 * Gets the max player.
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	/**
	 * Send server status.
	 * @param id the id
	 * @param value the value
	 */
	public void sendServerStatus(int id, int value)
	{
		final ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
		}
	}
	
	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}
	
	/**
	 * @return
	 */
	public boolean isClockShown()
	{
		return Config.SERVER_LIST_CLOCK;
	}
	
	/**
	 * @return
	 */
	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}
	
	/**
	 * Gets the server name.
	 * @return the server name.
	 */
	public String getServerName()
	{
		return _serverName;
	}
	
	/**
	 * Sets the server status.
	 * @param status the new server status
	 */
	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_DOWN:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_FULL:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_GM_ONLY:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_GOOD:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_NORMAL:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Status does not exists:" + status);
			}
		}
	}
	
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		
		/**
		 * Instantiates a new session key.
		 * @param loginOK1 the login o k1
		 * @param loginOK2 the login o k2
		 * @param playOK1 the play o k1
		 * @param playOK2 the play o k2
		 */
		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	private static class WaitingClient
	{
		public String account;
		public GameClient gameClient;
		public SessionKey session;
		
		/**
		 * Instantiates a new waiting client.
		 * @param acc the acc
		 * @param client the client
		 * @param key the key
		 */
		public WaitingClient(String acc, GameClient client, SessionKey key)
		{
			account = acc;
			gameClient = client;
			session = key;
		}
	}
}