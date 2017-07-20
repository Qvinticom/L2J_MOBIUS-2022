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

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.loginserver.network.L2LoginClient;
import com.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;
import com.l2jmobius.loginserver.network.serverpackets.ServerList;
import com.l2jmobius.util.Rnd;
import com.l2jmobius.util.Util;

import javolution.io.UTF8StreamReader;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamReaderImpl;

public class GameServerTable
{
	protected static Logger _log = Logger.getLogger(GameServerTable.class.getName());
	private static GameServerTable _instance;
	private final List<GameServer> _gameServerList = new ArrayList<>();
	public Map<Integer, String> serverNames = new HashMap<>();
	private static final int KEYS_SIZE = 10;
	private KeyPair[] _keyPairs;
	
	public static void load() throws GeneralSecurityException
	{
		synchronized (GameServerTable.class)
		{
			if (_instance == null)
			{
				_instance = new GameServerTable();
			}
			else
			{
				throw new IllegalStateException("Load can only be invoked a single time.");
			}
		}
	}
	
	public static GameServerTable getInstance()
	{
		return _instance;
	}
	
	public GameServerTable() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		loadServerNames();
		loadRegisteredGameServers();
		loadRSAKeys();
	}
	
	public void shutDown()
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.gst != null)
			{
				gs.gst.interrupt();
			}
		}
	}
	
	private void loadRSAKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		final KeyPairGenerator _keyGen = KeyPairGenerator.getInstance("RSA");
		final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
		_keyGen.initialize(spec);
		
		_keyPairs = new KeyPair[KEYS_SIZE];
		for (int i = 0; i < 10; i++)
		{
			_keyPairs[i] = _keyGen.generateKeyPair();
		}
		
		_log.info("Cached " + _keyPairs.length + " RSA keys for Game Server communication.");
	}
	
	public void loadRegisteredGameServers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM gameservers");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				_gameServerList.add(new GameServer(stringToHex(rset.getString("hexid")), rset.getInt("server_id")));
			}
			
			_log.info("Loaded " + _gameServerList.size() + " registered Game Servers.");
		}
		catch (final Exception e)
		{
			_log.warning("Error while loading Server List from gameservers table.");
			e.printStackTrace();
		}
	}
	
	/**
	 *
	 */
	private void loadServerNames()
	{
		try (InputStream in = new FileInputStream("data/servername.xml");
			UTF8StreamReader utf8 = new UTF8StreamReader())
		{
			final XMLStreamReaderImpl xpp = new XMLStreamReaderImpl();
			
			xpp.setInput(utf8.setInput(in));
			for (int e = xpp.getEventType(); e != XMLStreamConstants.END_DOCUMENT; e = xpp.next())
			
			{
				if (e == XMLStreamConstants.START_ELEMENT)
				
				{
					if (xpp.getLocalName().toString().equals("server"))
					
					{
						final Integer id = new Integer(xpp.getAttributeValue(null, "id").toString());
						final String name = xpp.getAttributeValue(null, "name").toString();
						serverNames.put(id, name);
					}
				}
			}
			xpp.close();
			_log.info("Loaded " + serverNames.size() + " server names.");
		}
		catch (final Exception e)
		{
			_log.info(getClass().getSimpleName() + ": Cannot load servername.xml!");
		}
	}
	
	/**
	 * @param string
	 * @return
	 */
	private byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	private String hexToString(byte[] hex)
	{
		if (hex == null)
		{
			return null;
		}
		
		return new BigInteger(hex).toString(16);
	}
	
	public void setServerReallyDown(int id)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == id)
			{
				gs.ip = null;
				gs.internal_ip = null;
				gs.port = 0;
				gs.gst = null;
			}
		}
	}
	
	public GameServerThread getGameServerThread(int ServerID)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == ServerID)
			{
				return gs.gst;
			}
		}
		return null;
	}
	
	public int getGameServerStatus(int ServerID)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == ServerID)
			{
				return gs.status;
			}
		}
		return -1;
	}
	
	public void addServer(GameServerThread gst)
	{
		final GameServer gameServer = new GameServer(gst);
		GameServer toReplace = null;
		
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == gst.getServerID())
			{
				toReplace = gs;
			}
		}
		
		if (toReplace != null)
		{
			_gameServerList.remove(toReplace);
		}
		
		_gameServerList.add(gameServer);
		orderList();
		
		if (Config.DEBUG)
		{
			for (final GameServer gs : _gameServerList)
			{
				_log.info(gs.toString());
			}
			
		}
		gst.setAuthed(true);
	}
	
	public int getServerIDforHex(byte[] hex)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (Arrays.equals(hex, gs.hexID))
			{
				return gs.server_id;
			}
		}
		return 0;
	}
	
	public boolean isIDfree(int id)
	{
		for (final GameServer gs : _gameServerList)
		{
			if ((gs.server_id == id) && (gs.hexID != null))
			{
				return false;
			}
		}
		return true;
	}
	
	public void createServer(GameServer gs)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)"))
		{
			statement.setString(1, hexToString(gs.hexID));
			statement.setInt(2, gs.server_id);
			if (gs.gst != null)
			{
				statement.setString(3, gs.gst.getGameExternalHost());
			}
			else
			{
				statement.setString(3, "*");
			}
			statement.executeUpdate();
		}
		catch (final SQLException e)
		{
			_log.warning("SQL error while saving gameserver :" + e);
		}
	}
	
	public boolean isARegisteredServer(byte[] hex)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (Arrays.equals(hex, gs.hexID))
			{
				return true;
			}
		}
		return false;
	}
	
	public int findFreeID()
	{
		for (int i = 0; i < 128; i++)
		{
			if (isIDfree(i))
			{
				return i;
			}
		}
		return 0;
	}
	
	public void deleteServer(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM gameservers WHERE gameservers.server_id=?"))
		{
			statement.setInt(1, id);
			statement.executeUpdate();
		}
		catch (final SQLException e)
		{
			_log.warning("SQL error while deleting gameserver :" + e);
		}
	}
	
	public void createServerList(L2LoginClient client)
	{
		
		orderList();
		final ServerList list = new ServerList(client.getLastServer());
		
		for (final GameServer gs : _gameServerList)
		{
			
			int status = gs.status;
			
			String gameIp = gs.ip;
			
			if (Util.isInternalIP(client.getSocket().getInetAddress().getHostAddress()))
			{
				gameIp = gs.internal_ip;
			}
			
			if (status == ServerStatus.STATUS_AUTO)
			{
				if (gameIp == null)
				{
					status = ServerStatus.STATUS_DOWN;
				}
			}
			else if (status == ServerStatus.STATUS_GM_ONLY)
			{
				if (client.getAccessLevel() < Config.GM_MIN)
				{
					status = ServerStatus.STATUS_DOWN;
				}
				else
				{
					if (gameIp == null)
					{
						status = ServerStatus.STATUS_DOWN;
					}
				}
			}
			
			list.addServer(gameIp, gs, status);
			
		}
		
		// send server list to client
		client.sendPacket(list);
	}
	
	/**
	 *
	 */
	private void orderList()
	{
		Collections.sort(_gameServerList, gsComparator);
	}
	
	private final Comparator<GameServer> gsComparator = (gs1, gs2) -> (gs1.server_id < gs2.server_id ? -1 : gs1.server_id == gs2.server_id ? 0 : 1);
	
	/**
	 * @param thread
	 */
	public void createServer(GameServerThread thread)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)"))
		{
			statement.setString(1, hexToString(thread.getHexID()));
			statement.setInt(2, thread.getServerID());
			statement.setString(3, thread.getGameExternalHost());
			statement.executeUpdate();
		}
		catch (final SQLException e)
		{
			_log.warning("SQL error while saving gameserver :" + e);
		}
	}
	
	/**
	 * @param value
	 * @param serverID
	 */
	public void setMaxPlayers(int value, int serverID)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == serverID)
			{
				gs.maxPlayers = value;
				gs.gst.setMaxPlayers(value);
			}
		}
	}
	
	/**
	 * @param b
	 * @param serverID
	 */
	public void setBracket(boolean b, int serverID)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == serverID)
			{
				gs.brackets = b;
			}
			
		}
	}
	
	/**
	 * @param b
	 * @param serverID
	 */
	public void setClock(boolean b, int serverID)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == serverID)
			{
				gs.clock = b;
			}
		}
	}
	
	/**
	 * @param b
	 * @param serverID
	 */
	public void setTestServer(boolean b, int serverID)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == serverID)
			{
				gs.testServer = b;
			}
		}
	}
	
	/**
	 * @param value
	 * @param serverID
	 */
	public void setStatus(int value, int serverID)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == serverID)
			{
				gs.status = value;
				if (Config.DEBUG)
				{
					_log.info("Status Changed for server " + serverID);
				}
			}
		}
	}
	
	public boolean isServerAuthed(int serverID)
	{
		for (final GameServer gs : _gameServerList)
		{
			if (gs.server_id == serverID)
			{
				if ((gs.ip != null) && (gs.gst != null) && gs.gst.isAuthed())
				{
					return true;
				}
				
			}
		}
		return false;
	}
	
	public List<String> status()
	{
		final List<String> str = new ArrayList<>();
		str.add("There are " + _gameServerList.size() + " GameServers");
		for (final GameServer gs : _gameServerList)
		{
			str.add(gs.toString());
		}
		
		return str;
	}
	
	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}
	
	public List<GameServer> getGameServerList()
	{
		return _gameServerList;
	}
	
	public class GameServer
	{
		public String ip;
		public int server_id;
		public int port;
		public boolean pvp = true;
		public boolean testServer = false;
		public int maxPlayers;
		public byte[] hexID;
		public GameServerThread gst;
		public boolean brackets = false;
		public boolean clock = false;
		public int status = ServerStatus.STATUS_AUTO;
		public String internal_ip;
		
		GameServer(GameServerThread gamest)
		{
			gst = gamest;
			ip = gst.getGameExternalIP();
			port = gst.getPort();
			pvp = gst.getPvP();
			testServer = gst.isTestServer();
			maxPlayers = gst.getMaxPlayers();
			hexID = gst.getHexID();
			server_id = gst.getServerID();
			internal_ip = gst.getGameInternalIP();
		}
		
		@Override
		public String toString()
		{
			return "GameServer: " + serverNames.get(server_id) + " id:" + server_id + " hex:" + hexToString(hexID) + " ip:" + ip + ":" + port + " status: " + ServerStatus.statusString[status];
		}
		
		private String hexToString(byte[] hex)
		{
			if (hex == null)
			{
				return null;
			}
			
			return new BigInteger(hex).toString(16);
		}
		
		public GameServer(byte[] hex, int id)
		{
			hexID = hex;
			server_id = id;
		}
		
		public GameServer(int id)
		{
			server_id = id;
		}
	}
}