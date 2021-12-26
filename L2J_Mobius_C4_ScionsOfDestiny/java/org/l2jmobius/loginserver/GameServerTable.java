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

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.loginserver.network.LoginClient;
import org.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;
import org.l2jmobius.loginserver.network.serverpackets.ServerList;

public class GameServerTable
{
	protected static Logger LOGGER = Logger.getLogger(GameServerTable.class.getName());
	private static GameServerTable _instance;
	private final List<GameServer> _gameServerList = new ArrayList<>();
	public Map<Integer, String> _serverNames = new HashMap<>();
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
		for (GameServer gs : _gameServerList)
		{
			if (gs.gst != null)
			{
				gs.gst.interrupt();
			}
		}
	}
	
	private void loadRSAKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
		keyGen.initialize(spec);
		
		_keyPairs = new KeyPair[KEYS_SIZE];
		for (int i = 0; i < 10; i++)
		{
			_keyPairs[i] = keyGen.generateKeyPair();
		}
		
		LOGGER.info("Cached " + _keyPairs.length + " RSA keys for Game Server communication.");
	}
	
	public void loadRegisteredGameServers()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM gameservers");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				_gameServerList.add(new GameServer(stringToHex(rset.getString("hexid")), rset.getInt("server_id")));
			}
			
			LOGGER.info("Loaded " + _gameServerList.size() + " registered Game Servers.");
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while loading Server List from gameservers table.");
			e.printStackTrace();
		}
	}
	
	private void loadServerNames()
	{
		try
		{
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(new File("./data/servername.xml"));
			final Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("server"))
				{
					final NamedNodeMap attrs = d.getAttributes();
					final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					final String name = attrs.getNamedItem("name").getNodeValue();
					_serverNames.put(id, name);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("GameServerTable: servername.xml could not be loaded.");
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
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == id)
			{
				gs.ip = null;
				gs.internalIp = null;
				gs.port = 0;
				gs.gst = null;
			}
		}
	}
	
	public GameServerThread getGameServerThread(int serverId)
	{
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == serverId)
			{
				return gs.gst;
			}
		}
		return null;
	}
	
	public int getGameServerStatus(int serverId)
	{
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == serverId)
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
		
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == gst.getServerID())
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
		
		gst.setAuthed(true);
	}
	
	public int getServerIDforHex(byte[] hex)
	{
		for (GameServer gs : _gameServerList)
		{
			if (Arrays.equals(hex, gs.hexId))
			{
				return gs.serverId;
			}
		}
		return 0;
	}
	
	public boolean isIDfree(int id)
	{
		for (GameServer gs : _gameServerList)
		{
			if ((gs.serverId == id) && (gs.hexId != null))
			{
				return false;
			}
		}
		return true;
	}
	
	public void createServer(GameServer gs)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)"))
		{
			statement.setString(1, hexToString(gs.hexId));
			statement.setInt(2, gs.serverId);
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
		catch (SQLException e)
		{
			LOGGER.warning("SQL error while saving gameserver :" + e);
		}
	}
	
	public boolean isARegisteredServer(byte[] hex)
	{
		for (GameServer gs : _gameServerList)
		{
			if (Arrays.equals(hex, gs.hexId))
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
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM gameservers WHERE gameservers.server_id=?"))
		{
			statement.setInt(1, id);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.warning("SQL error while deleting gameserver :" + e);
		}
	}
	
	public void createServerList(LoginClient client)
	{
		orderList();
		final ServerList list = new ServerList(client.getLastServer());
		
		for (GameServer gs : _gameServerList)
		{
			int status = gs.status;
			String gameIp = gs.ip;
			
			if (isInternalIP(client.getSocket().getInetAddress().getHostAddress()))
			{
				gameIp = gs.internalIp;
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
				if (client.getAccessLevel() <= 0)
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
	
	private static boolean isInternalIP(String ipAddress)
	{
		InetAddress addr = null;
		try
		{
			addr = java.net.InetAddress.getByName(ipAddress);
			return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	private void orderList()
	{
		Collections.sort(_gameServerList, gsComparator);
	}
	
	private final Comparator<GameServer> gsComparator = (gs1, gs2) -> (gs1.serverId < gs2.serverId ? -1 : gs1.serverId == gs2.serverId ? 0 : 1);
	
	public void createServer(GameServerThread thread)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)"))
		{
			statement.setString(1, hexToString(thread.getHexID()));
			statement.setInt(2, thread.getServerID());
			statement.setString(3, thread.getGameExternalHost());
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.warning("SQL error while saving gameserver :" + e);
		}
	}
	
	/**
	 * @param value
	 * @param serverID
	 */
	public void setMaxPlayers(int value, int serverID)
	{
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == serverID)
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
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == serverID)
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
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == serverID)
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
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == serverID)
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
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == serverID)
			{
				gs.status = value;
			}
		}
	}
	
	public boolean isServerAuthed(int serverID)
	{
		for (GameServer gs : _gameServerList)
		{
			if (gs.serverId == serverID)
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
		for (GameServer gs : _gameServerList)
		{
			str.add(gs.toString());
		}
		
		return str;
	}
	
	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.get(10)];
	}
	
	public List<GameServer> getGameServerList()
	{
		return _gameServerList;
	}
	
	public class GameServer
	{
		public String ip;
		public int serverId;
		public int port;
		public boolean pvp = true;
		public boolean testServer = false;
		public int maxPlayers;
		public byte[] hexId;
		public GameServerThread gst;
		public boolean brackets = false;
		public boolean clock = false;
		public int status = ServerStatus.STATUS_AUTO;
		public String internalIp;
		
		GameServer(GameServerThread gamest)
		{
			gst = gamest;
			ip = gst.getGameExternalIP();
			port = gst.getPort();
			pvp = gst.getPvP();
			testServer = gst.isTestServer();
			maxPlayers = gst.getMaxPlayers();
			hexId = gst.getHexID();
			serverId = gst.getServerID();
			internalIp = gst.getGameInternalIP();
		}
		
		@Override
		public String toString()
		{
			return "GameServer: " + _serverNames.get(serverId) + " id:" + serverId + " hex:" + hexToString(hexId) + " ip:" + ip + ":" + port + " status: " + ServerStatus.STATUS_STRING[status];
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
			hexId = hex;
			serverId = id;
		}
		
		public GameServer(int id)
		{
			serverId = id;
		}
		
		public void setStatus(int value)
		{
			status = value;
		}
	}
}