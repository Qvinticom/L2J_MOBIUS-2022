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
import java.net.Socket;
import java.net.SocketException;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.commons.util.crypt.NewCrypt;
import org.l2jmobius.loginserver.LoginController.ScrambledKeyPair;
import org.l2jmobius.loginserver.network.serverpackets.Init;
import org.l2jmobius.loginserver.network.AbstractClientPacket;
import org.l2jmobius.loginserver.network.AbstractServerPacket;

/**
 * @version $Revision: 1.15.2.5.2.5 $ $Date: 2005/04/06 16:13:46 $
 */
public class LoginClient extends Thread
{
	private final static Logger LOGGER = Logger.getLogger(LoginClient.class.getName());
	
	public static enum LoginClientState
	{
		CONNECTED,
		AUTHED_GG,
		AUTHED_LOGIN
	}
	
	private LoginClientState _state;
	
	private InputStream _in = null;
	private OutputStream _out = null;
	private final NewCrypt _crypt;
	private final Socket _csocket;
	
	private final ScrambledKeyPair _scrambledPair;
	private SessionKey _sessionKey;
	private final int _sessionId;
	
	private boolean _hasAgreed;
	
	private String _account;
	private int _accessLevel;
	private int _lastServer;
	
	public LoginClient(Socket client)
	{
		super("Login Client " + client.getInetAddress());
		setDaemon(true);
		
		_state = LoginClientState.CONNECTED;
		_csocket = client;
		
		_scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
		_sessionId = Rnd.nextInt();
		
		_crypt = new NewCrypt("_;5.]94-31==-%xT!^[$\000");
		
		_hasAgreed = false;
		
		try
		{
			_in = client.getInputStream();
			_out = new BufferedOutputStream(client.getOutputStream());
			
			// ensure that no errors occured and start thread
			start();
		}
		catch (IOException e)
		{
			try
			{
				client.close();
			}
			catch (IOException e1)
			{
			}
		}
	}
	
	@Override
	public void run()
	{
		int lengthHi = 0;
		int lengthLo = 0;
		int length = 0;
		boolean checksumOk = false;
		
		try
		{
			// initialize client
			sendPacket(new Init(this));
			
			while (true)
			{
				lengthLo = _in.read();
				lengthHi = _in.read();
				length = (lengthHi * 256) + lengthLo;
				
				if (length < 2)
				{
					LOGGER.finer("LoginServer: Client terminated the connection or sent illegal packet size.");
					break;
				}
				
				final byte[] incoming = new byte[length];
				incoming[0] = (byte) lengthLo;
				incoming[1] = (byte) lengthHi;
				
				int receivedBytes = 0;
				int newBytes = 0;
				while ((newBytes != -1) && (receivedBytes < (length - 2)))
				{
					newBytes = _in.read(incoming, 2, length - 2);
					receivedBytes = receivedBytes + newBytes;
				}
				
				if (receivedBytes != (length - 2))
				{
					LOGGER.warning("Incomplete Packet is sent to the server, closing connection.");
					break;
				}
				
				byte[] decrypt = new byte[length - 2];
				System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
				// decrypt if we have a key
				_crypt.decrypt(decrypt, 0, decrypt.length);
				checksumOk = NewCrypt.verifyChecksum(decrypt);
				
				if (!checksumOk)
				{
					LOGGER.warning("Client is not using latest Authentication method. (Min is " + Config.MIN_PROTOCOL_REVISION + ")");
					break;
				}
				
				// Execute client packet.
				final AbstractClientPacket packet = LoginPacketHandler.handlePacket(decrypt, this);
				if (packet != null)
				{
					LoginServer.getInstance().execute(packet);
				}
			}
		}
		catch (SocketException e)
		{
			LOGGER.fine("Connection closed unexpectedly.");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.INFO, "", e);
		}
		finally
		{
			LoginServer.getInstance().removeFloodProtection(_csocket.getInetAddress().getHostAddress());
			
			try
			{
				_csocket.close();
			}
			catch (Exception e1)
			{
				// ignore problems
			}
			
			if (_account != null)
			{
				LoginController.getInstance().removeLoginClient(_account);
				_account = null;
			}
		}
	}
	
	/**
	 * @param sl
	 */
	public void sendPacket(AbstractServerPacket sl)
	{
		try
		{
			byte[] data = sl.getContent();
			
			if (!(sl instanceof Init))
			{
				NewCrypt.appendChecksum(data);
				_crypt.crypt(data, 0, data.length);
			}
			
			final int len = data.length + 2;
			_out.write(len & 0xff);
			_out.write((len >> 8) & 0xff);
			_out.write(data);
			_out.flush();
		}
		catch (IOException e)
		{
		}
	}
	
	public LoginClientState getClientState()
	{
		return _state;
	}
	
	public void setState(LoginClientState state)
	{
		_state = state;
	}
	
	public Socket getSocket()
	{
		return _csocket;
	}
	
	public void setHasAgreed(boolean ag)
	{
		_hasAgreed = ag;
	}
	
	public boolean hasAgreed()
	{
		return _hasAgreed;
	}
	
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}
	
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}
	
	public String getAccount()
	{
		return _account;
	}
	
	public void setAccount(String account)
	{
		_account = account;
	}
	
	public void setAccessLevel(int accessLevel)
	{
		_accessLevel = accessLevel;
	}
	
	public int getAccessLevel()
	{
		return _accessLevel;
	}
	
	public void setLastServer(int lastServer)
	{
		_lastServer = lastServer;
	}
	
	public int getLastServer()
	{
		return _lastServer;
	}
	
	public byte[] getScrambledModulus()
	{
		return _scrambledPair._scrambledModulus;
	}
	
	public RSAPrivateKey getPrivateKey()
	{
		return (RSAPrivateKey) _scrambledPair._pair.getPrivate();
	}
}