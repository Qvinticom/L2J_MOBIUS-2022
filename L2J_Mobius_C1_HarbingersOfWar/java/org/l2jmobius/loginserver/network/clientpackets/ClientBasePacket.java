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
package org.l2jmobius.loginserver.network.clientpackets;

import java.nio.charset.StandardCharsets;

public abstract class ClientBasePacket
{
	private final byte[] _decrypt;
	private int _off;
	
	public ClientBasePacket(byte[] decrypt)
	{
		_decrypt = decrypt;
		_off = 1;
	}
	
	public int readD()
	{
		int result = _decrypt[_off++] & 0xFF;
		result |= (_decrypt[_off++] << 8) & 0xFF00;
		result |= (_decrypt[_off++] << 16) & 0xFF0000;
		return result |= (_decrypt[_off++] << 24) & 0xFF000000;
	}
	
	public int readC()
	{
		return _decrypt[_off++] & 0xFF;
	}
	
	public int readH()
	{
		int result = _decrypt[_off++] & 0xFF;
		return result |= (_decrypt[_off++] << 8) & 0xFF00;
	}
	
	public double readF()
	{
		long result = _decrypt[_off++] & 0xFF;
		result |= (_decrypt[_off++] << 8) & 0xFF00;
		result |= (_decrypt[_off++] << 16) & 0xFF0000;
		result |= (_decrypt[_off++] << 24) & 0xFF000000;
		result |= (_decrypt[_off++] << 32) & 0xFF00000000L;
		result |= (_decrypt[_off++] << 40) & 0xFF0000000000L;
		result |= (_decrypt[_off++] << 48) & 0xFF000000000000L;
		return Double.longBitsToDouble(result |= (_decrypt[_off++] << 56) & 0xFF00000000000000L);
	}
	
	public String readS()
	{
		String result = null;
		try
		{
			result = new String(_decrypt, _off, _decrypt.length - _off, StandardCharsets.UTF_16LE);
			result = result.substring(0, result.indexOf(0));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (result != null)
		{
			_off = (result.length() * 2) + 3;
		}
		return result;
	}
}
