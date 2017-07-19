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
package com.l2jmobius.gameserver;

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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.L2GameClient.GameClientState;
import com.l2jmobius.gameserver.network.gameserverpackets.AuthRequest;
import com.l2jmobius.gameserver.network.gameserverpackets.BlowFishKey;
import com.l2jmobius.gameserver.network.gameserverpackets.ChangeAccessLevel;
import com.l2jmobius.gameserver.network.gameserverpackets.GameServerBasePacket;
import com.l2jmobius.gameserver.network.gameserverpackets.PlayerAuthRequest;
import com.l2jmobius.gameserver.network.gameserverpackets.PlayerInGame;
import com.l2jmobius.gameserver.network.gameserverpackets.PlayerLogout;
import com.l2jmobius.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jmobius.gameserver.network.loginserverpackets.AuthResponse;
import com.l2jmobius.gameserver.network.loginserverpackets.InitLS;
import com.l2jmobius.gameserver.network.loginserverpackets.KickPlayer;
import com.l2jmobius.gameserver.network.loginserverpackets.LoginServerFail;
import com.l2jmobius.gameserver.network.loginserverpackets.PlayerAuthResponse;
import com.l2jmobius.gameserver.network.serverpackets.AuthLoginFail;
import com.l2jmobius.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.util.Rnd;
import com.l2jmobius.util.Util;
import com.l2jmobius.util.crypt.NewCrypt;

import javolution.util.FastList;
import javolution.util.FastMap;

public class LoginServerThread extends Thread
{
	protected static Logger _log = Logger.getLogger(LoginServerThread.class.getName());
	
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
	private final List<WaitingClient> _waitingClients;
	private final Map<String, L2GameClient> _accountsInGameServer;
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
		_waitingClients = new FastList<>();
		_accountsInGameServer = new FastMap<>();
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
				_log.info("Connecting to login on " + _hostname + ":" + _port);
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
						_log.finer("LoginServerThread: Login terminated the connection.");
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
						_log.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					
					// decrypt if we have a key
					_blowfish.decrypt(incoming, 0, incoming.length);
					checksumOk = NewCrypt.verifyChecksum(incoming);
					
					if (!checksumOk)
					{
						_log.warning("Incorrect packet checksum, ignoring packet (LS)");
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
								_log.warning("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							
							RSAPublicKey publicKey;
							
							try
							{
								publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(init.getRSAKey()), RSAKeyGenParameterSpec.F4));
							}
							catch (GeneralSecurityException e)
							{
								_log.warning("Trouble while init the public key send by login");
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
							_log.info("Damn! Registration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						}
						case 0x02:
						{
							final AuthResponse aresp = new AuthResponse(incoming);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(hexToString(_hexID));
							_log.info("Registered on login as Server " + _serverID + " : " + _serverName);
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
							if (L2World.getInstance().getAllPlayersCount() > 0)
							{
								final FastList<String> playerList = new FastList<>();
								for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
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
							synchronized (_waitingClients)
							{
								for (WaitingClient wc : _waitingClients)
								{
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
									}
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									final PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									final CharSelectInfo cl = new CharSelectInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.getConnection().sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								}
								else
								{
									_log.warning("session key is not correct. closing connection");
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
			catch (final UnknownHostException e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
			catch (final IOException e)
			{
				_log.info("Disconnected from Login, Trying to reconnect:");
				_log.info(e.toString());
			}
			finally
			{
				try
				{
					_loginSocket.close();
				}
				catch (final Exception e)
				{
				}
			}
			
			try
			{
				Thread.sleep(5000); // 5 seconds tempo.
			}
			catch (final InterruptedException e)
			{
				return;
			}
		}
	}
	
	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		if (Config.DEBUG)
		{
			System.out.println(key);
		}
		final WaitingClient wc = new WaitingClient(acc, client, key);
		synchronized (_waitingClients)
		{
			_waitingClients.add(wc);
		}
		final PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		try
		{
			sendPacket(par);
		}
		catch (final IOException e)
		{
			_log.warning("Error while sending player auth request.");
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void removeWaitingClient(L2GameClient client)
	{
		WaitingClient toRemove = null;
		synchronized (_waitingClients)
		{
			for (final WaitingClient c : _waitingClients)
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
	}
	
	public void sendLogout(String account)
	{
		final PlayerLogout pl = new PlayerLogout(account);
		try
		{
			sendPacket(pl);
		}
		catch (final IOException e)
		{
			_log.warning("Error while sending logout packet to login");
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			_accountsInGameServer.remove(account);
		}
	}
	
	public void addGameServerLogin(String account, L2GameClient client)
	{
		_accountsInGameServer.put(account, client);
	}
	
	public boolean getAccountInGameServer(String account)
	{
		return _accountsInGameServer.get(account) != null;
	}
	
	public void sendAccessLevel(String account, int level)
	{
		final ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (final IOException e)
		{
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
	
	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	public void doKickPlayer(String account)
	{
		final L2GameClient client = _accountsInGameServer.get(account);
		if (client != null)
		{
			client.cancelCleanup(); // delayed cleanup
			final L2PcInstance player = client.getActiveChar();
			if (player != null)
			{
				player.sendPacket(new SystemMessage(SystemMessage.ANOTHER_LOGIN_WITH_ACCOUNT));
				ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{
					if (player.isOnline() > 0)
					{
						player.logout(false);
					}
				}, 400);
			}
			else
			{
				client.closeNow();
			}
			LoginServerThread.getInstance().sendLogout(account);
		}
	}
	
	public static byte[] generateHex(int size)
	{
		final byte[] array = new byte[size];
		Rnd.nextBytes(array);
		if (Config.DEBUG)
		{
			_log.fine("Generated random String:  \"" + array + "\"");
		}
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
		if (Config.DEBUG)
		{
			_log.finest("[S]\n" + Util.printData(data));
		}
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
		catch (final IOException e)
		{
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatus.statusString[_status];
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
		public L2GameClient gameClient;
		public SessionKey session;
		
		/**
		 * Instantiates a new waiting client.
		 * @param acc the acc
		 * @param client the client
		 * @param key the key
		 */
		public WaitingClient(String acc, L2GameClient client, SessionKey key)
		{
			account = acc;
			gameClient = client;
			session = key;
		}
	}
}