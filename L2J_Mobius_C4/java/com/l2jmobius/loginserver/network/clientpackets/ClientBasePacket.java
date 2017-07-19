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
package com.l2jmobius.loginserver.network.clientpackets;

import com.l2jmobius.loginserver.network.L2LoginClient;

/**
 * This class ...
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:30:12 $
 */
public abstract class ClientBasePacket implements Runnable
{
	private final L2LoginClient _client;
	private final byte[] _decrypt;
	private int _off;
	
	public ClientBasePacket(byte[] decrypt, L2LoginClient client)
	{
		_decrypt = decrypt;
		_off = 1; // skip packet type id
		_client = client;
	}
	
	@Override
	public abstract void run();
	
	public L2LoginClient getClient()
	{
		return _client;
	}
	
	public byte[] getByteBuffer()
	{
		return _decrypt;
	}
	
	public int readD()
	{
		int result = _decrypt[_off++] & 0xff;
		result |= (_decrypt[_off++] << 8) & 0xff00;
		result |= (_decrypt[_off++] << 0x10) & 0xff0000;
		result |= (_decrypt[_off++] << 0x18) & 0xff000000;
		return result;
	}
	
	public int readC()
	{
		final int result = _decrypt[_off++] & 0xff;
		return result;
	}
}