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
package org.l2jmobius.gameserver.network;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.network.serverpackets.ServerBasePacket;

public class Connection
{
	private static Logger _log = Logger.getLogger(Connection.class.getName());
	private final Crypt _inCrypt;
	private final Crypt _outCrypt;
	private final byte[] _cryptkey;
	private final Socket _csocket;
	private final InputStream _in;
	private final OutputStream _out;
	
	public Connection(Socket client, byte[] cryptKey) throws IOException
	{
		_csocket = client;
		_in = client.getInputStream();
		_out = new BufferedOutputStream(client.getOutputStream());
		_inCrypt = new Crypt();
		_outCrypt = new Crypt();
		_cryptkey = cryptKey;
	}
	
	public byte[] getPacket() throws IOException
	{
		int receivedBytes;
		int lengthHi = 0;
		int lengthLo = 0;
		int length = 0;
		lengthLo = _in.read();
		lengthHi = _in.read();
		length = (lengthHi * 256) + lengthLo;
		if (lengthHi < 0)
		{
			// _log.warning("client terminated connection");
			throw new IOException("EOF");
		}
		final byte[] incoming = new byte[length];
		incoming[0] = (byte) lengthLo;
		incoming[1] = (byte) lengthHi;
		int newBytes = 0;
		for (receivedBytes = 0; (newBytes != -1) && (receivedBytes < (length - 2)); receivedBytes += newBytes)
		{
			newBytes = _in.read(incoming, 2, length - 2);
		}
		if (receivedBytes != (length - 2))
		{
			_log.warning("Incomplete Packet is sent to the server, closing connection.");
			throw new IOException();
		}
		final byte[] decrypt = new byte[incoming.length - 2];
		System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
		_inCrypt.decrypt(decrypt);
		// int packetType = decrypt[0] & 0xFF;
		return decrypt;
	}
	
	public void sendPacket(byte[] data) throws IOException
	{
		final Connection connection = this;
		synchronized (connection)
		{
			// _log.config("\n" + printData(data, data.length));
			_outCrypt.encrypt(data);
			final int length = data.length + 2;
			_out.write(length & 0xFF);
			_out.write((length >> 8) & 0xFF);
			_out.flush();
			_out.write(data);
			_out.flush();
		}
	}
	
	public void sendPacket(ServerBasePacket packet) throws IOException
	{
		packet.writeImpl();
		sendPacket(packet.getBytes());
	}
	
	public void activateCryptKey()
	{
		_inCrypt.setKey(_cryptkey);
		_outCrypt.setKey(_cryptkey);
	}
	
	public byte[] getCryptKey()
	{
		return _cryptkey;
	}
	
	public void close() throws IOException
	{
		_csocket.close();
	}
	
	@SuppressWarnings("unused")
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
}
