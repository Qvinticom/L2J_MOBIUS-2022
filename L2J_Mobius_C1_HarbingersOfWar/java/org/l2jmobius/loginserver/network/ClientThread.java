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
package org.l2jmobius.loginserver.network;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;
import org.l2jmobius.loginserver.LoginController;
import org.l2jmobius.loginserver.data.AccountData;
import org.l2jmobius.loginserver.network.clientpackets.RequestAuthLogin;
import org.l2jmobius.loginserver.network.serverpackets.Init;
import org.l2jmobius.loginserver.network.serverpackets.LoginFail;
import org.l2jmobius.loginserver.network.serverpackets.LoginOk;
import org.l2jmobius.loginserver.network.serverpackets.PlayOk;
import org.l2jmobius.loginserver.network.serverpackets.ServerBasePacket;
import org.l2jmobius.loginserver.network.serverpackets.ServerList;

public class ClientThread extends Thread
{
	private static final Logger _log = Logger.getLogger(ClientThread.class.getName());
	
	private final InputStream _in;
	private final OutputStream _out;
	private final NewCrypt _crypt;
	private final AccountData _logins;
	private final Socket _csocket;
	private final String _gameServerHost;
	private final int _gameServerPort;
	private static List<String> _bannedIPs = new ArrayList<>();
	
	public ClientThread(Socket client, AccountData logins, String host, int port) throws IOException
	{
		_csocket = client;
		final String ip = client.getInetAddress().getHostAddress();
		if (_bannedIPs.contains(ip))
		{
			throw new IOException("banned IP");
		}
		_in = client.getInputStream();
		_out = new BufferedOutputStream(client.getOutputStream());
		_crypt = new NewCrypt("[;'.]94-31==-%&@!^+]\u0000");
		_logins = logins;
		_gameServerHost = host;
		_gameServerPort = port;
		start();
	}
	
	@Override
	public void run()
	{
		int lengthHi = 0;
		int lengthLo = 0;
		int length = 0;
		@SuppressWarnings("unused")
		boolean checksumOk = false;
		int sessionKey = -1;
		String account = null;
		String gameServerIp = null;
		try
		{
			final InetAddress adr = InetAddress.getByName(_gameServerHost);
			gameServerIp = adr.getHostAddress();
			final Init startPacket = new Init();
			_out.write(startPacket.getLength() & 255);
			_out.write((startPacket.getLength() >> 8) & 255);
			_out.write(startPacket.getContent());
			_out.flush();
			do
			{
				lengthLo = _in.read();
				lengthHi = _in.read();
				length = (lengthHi * 256) + lengthLo;
				if (lengthHi < 0)
				{
					break;
				}
				final byte[] incoming = new byte[length];
				incoming[0] = (byte) lengthLo;
				incoming[1] = (byte) lengthHi;
				int newBytes = 0;
				int receivedBytes;
				for (receivedBytes = 0; (newBytes != -1) && (receivedBytes < (length - 2)); receivedBytes += newBytes)
				{
					newBytes = _in.read(incoming, 2, length - 2);
				}
				if (receivedBytes != (length - 2))
				{
					_log.warning("Incomplete Packet is sent to the server, closing connection.");
					break;
				}
				byte[] decrypt = new byte[length - 2];
				System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
				decrypt = _crypt.decrypt(decrypt);
				checksumOk = _crypt.checksum(decrypt);
				final int packetType = decrypt[0] & 255;
				switch (packetType)
				{
					case 0x00:
					{
						final RequestAuthLogin ral = new RequestAuthLogin(decrypt);
						account = ral.getUser().toLowerCase();
						final LoginController lc = LoginController.getInstance();
						if (_logins.loginValid(account, ral.getPassword(), _csocket.getInetAddress()))
						{
							if (!lc.isAccountInGameServer(account) && !lc.isAccountInLoginServer(account))
							{
								final int accessLevel = _logins.getAccessLevel(account);
								if (accessLevel < 0)
								{
									final LoginFail lok = new LoginFail(LoginFail.REASON_ACCOUNT_BANNED);
									sendPacket(lok);
									break;
								}
								sessionKey = lc.assignSessionKeyToLogin(account, accessLevel, _csocket);
								final LoginOk lok = new LoginOk();
								sendPacket(lok);
								break;
							}
							if (lc.isAccountInLoginServer(account))
							{
								// _log.warning("Account is in use on Login server (kicking off):" + account);
								lc.getLoginServerConnection(account).close();
								lc.removeLoginServerLogin(account);
							}
							if (lc.isAccountInGameServer(account))
							{
								// _log.warning("Account is in use on Game server (kicking off):" + account);
								lc.getClientConnection(account).sendPacket(new LeaveWorld());
								lc.getClientConnection(account).close();
								lc.removeGameServerLogin(account);
							}
							final LoginFail lok = new LoginFail(LoginFail.REASON_ACCOUNT_IN_USE);
							sendPacket(lok);
							break;
						}
						final LoginFail lok = new LoginFail(LoginFail.REASON_USER_OR_PASS_WRONG);
						sendPacket(lok);
						break;
					}
					case 0x02:
					{
						// RequestServerLogin rsl = new RequestServerLogin(decrypt);
						final PlayOk po = new PlayOk(sessionKey);
						sendPacket(po);
						break;
					}
					case 0x05:
					{
						// RequestServerList rsl = new RequestServerList(decrypt);
						final ServerList sl = new ServerList();
						final int current = LoginController.getInstance().getOnlinePlayerCount();
						final int max = LoginController.getInstance().getMaxAllowedOnlinePlayers();
						sl.addServer(gameServerIp, _gameServerPort, true, false, current, max);
						sendPacket(sl);
						break;
					}
				}
				if (Config.LOG_UNKNOWN_PACKETS)
				{
					_log.warning("Unknown Packet: " + packetType);
					_log.warning(printData(decrypt, decrypt.length));
				}
			}
			while (true);
			
			try
			{
				_csocket.close();
			}
			catch (Exception e1)
			{
			}
			LoginController.getInstance().removeLoginServerLogin(account);
			return;
		}
		catch (HackingException e)
		{
			ClientThread._bannedIPs.add(e.getIP());
			try
			{
				_csocket.close();
			}
			catch (Exception e1)
			{
			}
			LoginController.getInstance().removeLoginServerLogin(account);
			return;
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			try
			{
				_csocket.close();
			}
			catch (Exception e1)
			{
				// empty catch block
			}
			LoginController.getInstance().removeLoginServerLogin(account);
			return;
		}
	}
	
	private void sendPacket(ServerBasePacket packet) throws IOException
	{
		packet.writeImpl();
		byte[] data = packet.getBytes();
		_crypt.checksum(data);
		data = _crypt.crypt(data);
		final int len = data.length + 2;
		_out.write(len & 0xFF);
		_out.write((len >> 8) & 0xFF);
		_out.write(data);
		_out.flush();
	}
	
	private String printData(byte[] data, int len)
	{
		int a;
		int charpoint;
		byte t1;
		final StringBuilder result = new StringBuilder();
		int counter = 0;
		for (int i = 0; i < len; ++i)
		{
			if ((counter % 16) == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}
			result.append(fillHex(data[i] & 0xFF, 2) + " ");
			if (++counter != 16)
			{
				continue;
			}
			result.append("   ");
			charpoint = i - 15;
			for (a = 0; a < 16; ++a)
			{
				if (((t1 = data[charpoint++]) > 31) && (t1 < 128))
				{
					result.append((char) t1);
					continue;
				}
				result.append('.');
			}
			result.append("\n");
			counter = 0;
		}
		final int rest = data.length % 16;
		if (rest > 0)
		{
			for (int i = 0; i < (17 - rest); ++i)
			{
				result.append("   ");
			}
			charpoint = data.length - rest;
			for (a = 0; a < rest; ++a)
			{
				if (((t1 = data[charpoint++]) > 31) && (t1 < 128))
				{
					result.append((char) t1);
					continue;
				}
				result.append('.');
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	private String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		for (int i = number.length(); i < digits; ++i)
		{
			number = "0" + number;
		}
		return number;
	}
	
	@SuppressWarnings("unused")
	private String getTerminatedString(byte[] data, int offset)
	{
		final StringBuilder result = new StringBuilder();
		for (int i = offset; (i < data.length) && (data[i] != 0); ++i)
		{
			result.append((char) data[i]);
		}
		return result.toString();
	}
	
	public static void addBannedIP(String ip)
	{
		_bannedIPs.add(ip);
	}
}
