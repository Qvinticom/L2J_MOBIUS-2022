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

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.loginserver.network.LoginClient;

/**
 * @version $Revision: 1.7.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginController
{
	protected static Logger LOGGER = Logger.getLogger(LoginController.class.getName());
	
	private static LoginController _instance;
	
	/** Time before kicking the client if he didn't logged yet */
	public final static int LOGIN_TIMEOUT = 60 * 1000;
	
	/** this map contains the connections of the players that are in the loginserver */
	private final Map<String, LoginClient> _accountsInLogin;
	private final Map<String, BanInfo> _bannedIps;
	
	private int _maxAllowedOnlinePlayers;
	private final Map<String, Integer> _hackProtection;
	private final Map<String, String> _lastPassword;
	protected KeyPairGenerator _keyGen;
	protected ScrambledKeyPair[] _keyPairs;
	
	protected byte[][] _blowfishKeys;
	
	public static void load() throws GeneralSecurityException
	{
		synchronized (LoginController.class)
		{
			if (_instance == null)
			{
				_instance = new LoginController();
			}
			else
			{
				throw new IllegalStateException("LoginController can only be loaded a single time.");
			}
		}
	}
	
	public static LoginController getInstance()
	{
		return _instance;
	}
	
	private LoginController() throws GeneralSecurityException
	{
		_accountsInLogin = new ConcurrentHashMap<>();
		_bannedIps = new ConcurrentHashMap<>();
		
		_hackProtection = new ConcurrentHashMap<>();
		_lastPassword = new ConcurrentHashMap<>();
		_keyPairs = new ScrambledKeyPair[10];
		
		_keyGen = KeyPairGenerator.getInstance("RSA");
		final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
		_keyGen.initialize(spec);
		
		// generate the initial set of keys
		for (int i = 0; i < 10; i++)
		{
			_keyPairs[i] = new ScrambledKeyPair(_keyGen.generateKeyPair());
		}
		LOGGER.info("Cached 10 KeyPairs for RSA communication.");
		testCipher((RSAPrivateKey) _keyPairs[0]._pair.getPrivate());
	}
	
	/**
	 * This is mostly to force the initialization of the Crypto Implementation, avoiding it being done on runtime when its first needed.<BR>
	 * In short it avoids the worst-case execution time on runtime by doing it on loading.
	 * @param key Any private RSA Key just for testing purposes.
	 * @throws GeneralSecurityException if a underlying exception was thrown by the Cipher
	 */
	private void testCipher(RSAPrivateKey key) throws GeneralSecurityException
	{
		// avoid worst-case execution, KenM
		final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
		rsaCipher.init(Cipher.DECRYPT_MODE, key);
	}
	
	public void assignKeyToLogin(LoginClient client)
	{
		client.setSessionKey(new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt()));
		
		_accountsInLogin.put(client.getAccount(), client);
	}
	
	public SessionKey getKeyForAccount(String account)
	{
		if (_accountsInLogin.get(account) == null)
		{
			return null;
		}
		
		return _accountsInLogin.get(account).getSessionKey();
	}
	
	public void removeLoginClient(String account)
	{
		if (account != null)
		{
			_accountsInLogin.remove(account);
		}
	}
	
	public LoginClient getConnectedClient(String account)
	{
		if (account == null)
		{
			return null;
		}
		
		return _accountsInLogin.get(account);
	}
	
	public int getTotalOnlinePlayerCount()
	{
		int playerCount = 0;
		final List<GameServerThread> gslist = LoginServer.getGameServerListener().getGameServerThreads();
		synchronized (gslist)
		{
			for (GameServerThread gs : gslist)
			{
				playerCount += gs.getCurrentPlayers();
			}
		}
		return playerCount;
	}
	
	public int getOnlinePlayerCount(int serverId)
	{
		final List<GameServerThread> gslist = LoginServer.getGameServerListener().getGameServerThreads();
		synchronized (gslist)
		{
			for (GameServerThread gs : gslist)
			{
				if (gs.getServerID() == serverId)
				{
					return gs.getCurrentPlayers();
				}
			}
		}
		return 0;
	}
	
	public int getMaxAllowedOnlinePlayers(int serverId)
	{
		final List<GameServerThread> gslist = LoginServer.getGameServerListener().getGameServerThreads();
		synchronized (gslist)
		{
			for (GameServerThread gs : gslist)
			{
				if (gs.getServerID() == serverId)
				{
					return gs.getMaxPlayers();
				}
			}
		}
		return 0;
	}
	
	public void setMaxAllowedOnlinePlayers(int maxAllowedOnlinePlayers)
	{
		_maxAllowedOnlinePlayers = maxAllowedOnlinePlayers;
	}
	
	/**
	 * @param access
	 * @param serverId
	 * @return
	 */
	public boolean loginPossible(int access, int serverId)
	{
		return ((getOnlinePlayerCount(serverId) < _maxAllowedOnlinePlayers) || (access >= 50));
	}
	
	public void setAccountAccessLevel(String account, int banLevel)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE accounts SET accessLevel=? WHERE login=?"))
		{
			statement.setInt(1, banLevel);
			statement.setString(2, account);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not set accessLevel:" + e);
		}
	}
	
	/**
	 * <p>
	 * This method returns one of the 10 {@link ScrambledKeyPair}.
	 * </p>
	 * @return a scrambled keypair
	 */
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return _keyPairs[Rnd.get(10)];
	}
	
	public void saveLastServer(String account, int serverId)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE accounts SET lastServer = ? WHERE login = ?"))
		{
			statement.setInt(1, serverId);
			statement.setString(2, account);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not set lastServer: " + e);
		}
	}
	
	/**
	 * user name is not case sensitive any more
	 * @param user
	 * @param password
	 * @param client
	 * @return
	 */
	public boolean isLoginValid(String user, String password, LoginClient client)
	{
		boolean ok = false;
		final InetAddress address = client.getSocket().getInetAddress();
		
		// Log.add("'" + (user == null ? "null" : user) + "' " + (address == null ? "null" : address.getHostAddress()), "logins_ip");
		
		if ((address == null) || (user == null))
		{
			return false;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final MessageDigest md = MessageDigest.getInstance("SHA");
			final byte[] raw = password.getBytes("UTF-8");
			final byte[] hash = md.digest(raw);
			
			byte[] expected = null;
			int access = 0;
			int lastServer = 1;
			
			try (PreparedStatement statement = con.prepareStatement("SELECT password, accessLevel, lastServer FROM accounts WHERE login=?"))
			{
				statement.setString(1, user);
				try (ResultSet rset = statement.executeQuery())
				{
					if (rset.next())
					{
						expected = Base64.getDecoder().decode(rset.getString("password"));
						access = rset.getInt("accessLevel");
						lastServer = rset.getInt("lastServer");
					}
				}
			}
			
			if (expected == null)
			{
				if (Config.AUTO_CREATE_ACCOUNTS)
				{
					if ((user.length() >= 2) && (user.length() <= 14))
					{
						try (PreparedStatement statement = con.prepareStatement("INSERT INTO accounts (login,password,lastactive,accessLevel,lastIP) values(?,?,?,?,?)"))
						{
							statement.setString(1, user);
							statement.setString(2, Base64.getEncoder().encodeToString(hash));
							statement.setLong(3, Chronos.currentTimeMillis());
							statement.setInt(4, 0);
							statement.setString(5, address.getHostAddress());
							statement.execute();
						}
						
						LOGGER.info("Created new account for " + user);
						return true;
					}
					LOGGER.warning("Invalid username creation/use attempt: " + user);
					return false;
				}
				LOGGER.warning("[" + address.getHostAddress() + "]: account missing for user " + user);
				return false;
			}
			
			ok = true;
			for (int i = 0; i < expected.length; i++)
			{
				if (hash[i] != expected[i])
				{
					ok = false;
					break;
				}
			}
			if (ok)
			{
				client.setAccessLevel(access);
				client.setLastServer(lastServer);
				try (PreparedStatement statement = con.prepareStatement("UPDATE accounts SET lastactive=?, lastIP=? WHERE login=?"))
				{
					statement.setLong(1, Chronos.currentTimeMillis());
					statement.setString(2, address.getHostAddress());
					statement.setString(3, user);
					statement.execute();
				}
			}
		}
		catch (Exception e)
		{
			// digest algo not found ??
			// out of bounds should not be possible
			LOGGER.warning("could not check password:" + e);
			ok = false;
		}
		
		if (!ok)
		{
			// Log.add("'" + user + "' " + address.getHostAddress(), "logins_ip_fails");
			
			final Integer failedConnects = _hackProtection.get(address.getHostAddress());
			final String lastPassword = _lastPassword.get(address.getHostAddress());
			
			// add 1 to the failed counter for this IP
			int failedCount = 1;
			if (failedConnects != null)
			{
				failedCount = failedConnects.intValue() + 1;
			}
			
			if (password != lastPassword)
			{
				_hackProtection.put(address.getHostAddress(), failedCount);
				_lastPassword.put(address.getHostAddress(), password);
			}
			
			if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
			{
				LOGGER.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds due to " + failedCount + " invalid user/pass attempts");
				addBanForAddress(address.getHostAddress(), Chronos.currentTimeMillis() + (Config.LOGIN_BLOCK_AFTER_BAN * 1000));
			}
		}
		else
		{
			// for long running servers, this should prevent blocking
			// of users that mistype their passwords once every day :)
			_hackProtection.remove(address.getHostAddress());
			_lastPassword.remove(address.getHostAddress());
			// Log.add("'" + user + "' " + address.getHostAddress(), "logins_ip");
		}
		
		return ok;
	}
	
	public boolean ipBlocked(String ipAddress)
	{
		int tries = 0;
		
		if (_hackProtection.containsKey(ipAddress))
		{
			tries = _hackProtection.get(ipAddress);
		}
		
		if (tries > Config.LOGIN_TRY_BEFORE_BAN)
		{
			_hackProtection.remove(ipAddress);
			LOGGER.warning("Removed host from hacklist! IP number: " + ipAddress);
			return true;
		}
		
		return false;
	}
	
	public void addBanForAddress(String address, long expiration)
	{
		_bannedIps.put(address, new BanInfo(expiration));
	}
	
	public boolean isBannedAddress(String address)
	{
		final BanInfo ban = _bannedIps.get(address);
		if (ban != null)
		{
			if (!ban.hasExpired())
			{
				return true;
			}
			_bannedIps.remove(address);
		}
		return false;
	}
	
	public Map<String, BanInfo> getBannedIps()
	{
		return _bannedIps;
	}
	
	private class BanInfo
	{
		private final long _time;
		
		public BanInfo(long time)
		{
			_time = time;
		}
		
		public boolean hasExpired()
		{
			return (Chronos.currentTimeMillis() > _time) && (_time > 0);
		}
	}
	
	public static class ScrambledKeyPair
	{
		public KeyPair _pair;
		public byte[] _scrambledModulus;
		
		public ScrambledKeyPair(KeyPair pPair)
		{
			_pair = pPair;
			_scrambledModulus = scrambleModulus(((RSAPublicKey) _pair.getPublic()).getModulus());
		}
		
		private byte[] scrambleModulus(BigInteger modulus)
		{
			byte[] scrambledMod = modulus.toByteArray();
			
			if ((scrambledMod.length == 0x81) && (scrambledMod[0] == 0x00))
			{
				final byte[] temp = new byte[0x80];
				System.arraycopy(scrambledMod, 1, temp, 0, 0x80);
				scrambledMod = temp;
			}
			// step 1 : 0x4d-0x50 <-> 0x00-0x04
			for (int i = 0; i < 4; i++)
			{
				final byte temp = scrambledMod[0x00 + i];
				scrambledMod[0x00 + i] = scrambledMod[0x4d + i];
				scrambledMod[0x4d + i] = temp;
			}
			// step 2 : xor first 0x40 bytes with last 0x40 bytes
			for (int i = 0; i < 0x40; i++)
			{
				scrambledMod[i] = (byte) (scrambledMod[i] ^ scrambledMod[0x40 + i]);
			}
			// step 3 : xor bytes 0x0d-0x10 with bytes 0x34-0x38
			for (int i = 0; i < 4; i++)
			{
				scrambledMod[0x0d + i] = (byte) (scrambledMod[0x0d + i] ^ scrambledMod[0x34 + i]);
			}
			// step 4 : xor last 0x40 bytes with first 0x40 bytes
			for (int i = 0; i < 0x40; i++)
			{
				scrambledMod[0x40 + i] = (byte) (scrambledMod[0x40 + i] ^ scrambledMod[i]);
			}
			LOGGER.fine("Modulus was scrambled.");
			
			return scrambledMod;
		}
	}
}