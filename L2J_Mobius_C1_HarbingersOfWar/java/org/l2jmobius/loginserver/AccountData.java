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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class AccountData
{
	private static final String SHA = "SHA";
	private static final String UTF_8 = "UTF-8";
	private static Logger _log = Logger.getLogger(AccountData.class.getName());
	private final Map<String, byte[]> _logPass = new HashMap<>();
	private final Map<String, Integer> _accessLevels = new HashMap<>();
	private final Map<String, Integer> _hackProtection = new HashMap<>();
	private final boolean _autoCreate;
	
	public AccountData(boolean autoCreate)
	{
		_autoCreate = autoCreate;
		_log.config("Automatically creating new accounts: " + _autoCreate);
		File loginFile = new File("data/accounts.txt");
		if (loginFile.exists())
		{
			try
			{
				readFromDisk(loginFile);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public int getAccessLevel(String user)
	{
		return _accessLevels.get(user);
	}
	
	public boolean loginValid(String user, String password, InetAddress address) throws HackingException
	{
		boolean ok = false;
		Integer failedConnects = _hackProtection.get(address.getHostAddress());
		if ((failedConnects != null) && (failedConnects > 2))
		{
			_log.warning("Hacking detected from ip:" + address.getHostAddress() + " .. adding IP to banlist.");
			throw new HackingException(address.getHostAddress());
		}
		try
		{
			MessageDigest md = MessageDigest.getInstance(SHA);
			byte[] raw = password.getBytes(UTF_8);
			byte[] hash = md.digest(raw);
			byte[] expected = _logPass.get(user);
			if (expected == null)
			{
				if (_autoCreate)
				{
					_logPass.put(user, hash);
					_accessLevels.put(user, 0);
					// _log.info("Created new account for " + user);
					saveToDisk();
					return true;
				}
				_log.warning("Account missing for user " + user);
				return false;
			}
			ok = true;
			for (int i = 0; i < expected.length; ++i)
			{
				if (hash[i] == expected[i])
				{
					continue;
				}
				ok = false;
				break;
			}
		}
		catch (Exception e)
		{
			_log.warning("Could not check password:" + e);
			ok = false;
		}
		if (!ok)
		{
			int failedCount = 1;
			if (failedConnects != null)
			{
				failedCount = failedConnects + 1;
			}
			_hackProtection.put(address.getHostAddress(), failedCount);
		}
		else
		{
			_hackProtection.remove(address.getHostAddress());
		}
		return ok;
	}
	
	private void readFromDisk(File loginFile) throws NumberFormatException, IOException
	{
		BufferedReader lnr = null;
		_logPass.clear();
		int i = 0;
		String line = null;
		lnr = new LineNumberReader(new FileReader(loginFile));
		while ((line = ((LineNumberReader) lnr).readLine()) != null)
		{
			StringTokenizer st = new StringTokenizer(line, "\t\n\r");
			if (!st.hasMoreTokens())
			{
				continue;
			}
			String name = st.nextToken().toLowerCase();
			String password = st.nextToken();
			_logPass.put(name, java.util.Base64.getDecoder().decode(password));
			if (st.hasMoreTokens())
			{
				String access = st.nextToken();
				Integer level = Integer.parseInt(access);
				_accessLevels.put(name, level);
			}
			else
			{
				_accessLevels.put(name, 0);
			}
			++i;
		}
		_log.config("Found " + i + " accounts on disk.");
		try
		{
			lnr.close();
			return;
		}
		catch (Exception e)
		{
			return;
		}
	}
	
	public void saveToDisk()
	{
		File loginFile = new File("data/accounts.txt");
		FileWriter save = null;
		try
		{
			save = new FileWriter(loginFile);
			Iterator<String> iter = _logPass.keySet().iterator();
			while (iter.hasNext())
			{
				String name = iter.next();
				byte[] pass = _logPass.get(name);
				save.write(name);
				save.write("\t");
				save.write(Base64.getEncoder().encodeToString(pass));
				save.write("\t");
				save.write("" + _accessLevels.get(name));
				save.write("\r\n");
			}
		}
		catch (IOException e)
		{
			_log.warning("Could not store accouts file." + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (save != null)
				{
					save.close();
				}
			}
			catch (Exception e1)
			{
			}
		}
	}
}
