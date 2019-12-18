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
package org.l2jmobius.loginserver.network.serverpackets;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public abstract class ServerBasePacket
{
	private final ByteArrayOutputStream _bao = new ByteArrayOutputStream();
	
	protected void writeD(int value)
	{
		_bao.write(value & 0xFF);
		_bao.write((value >> 8) & 0xFF);
		_bao.write((value >> 16) & 0xFF);
		_bao.write((value >> 24) & 0xFF);
	}
	
	protected void writeH(int value)
	{
		_bao.write(value & 0xFF);
		_bao.write((value >> 8) & 0xFF);
	}
	
	protected void writeC(int value)
	{
		_bao.write(value & 0xFF);
	}
	
	protected void writeF(double org)
	{
		final long value = Double.doubleToRawLongBits(org);
		_bao.write((int) (value & 0xFFL));
		_bao.write((int) ((value >> 8) & 0xFFL));
		_bao.write((int) ((value >> 16) & 0xFFL));
		_bao.write((int) ((value >> 24) & 0xFFL));
		_bao.write((int) ((value >> 32) & 0xFFL));
		_bao.write((int) ((value >> 40) & 0xFFL));
		_bao.write((int) ((value >> 48) & 0xFFL));
		_bao.write((int) ((value >> 56) & 0xFFL));
	}
	
	protected void writeS(String text)
	{
		try
		{
			if (text != null)
			{
				_bao.write(text.getBytes(StandardCharsets.UTF_16LE));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		_bao.write(0);
		_bao.write(0);
	}
	
	public int getLength()
	{
		return _bao.size() + 2;
	}
	
	public byte[] getBytes()
	{
		writeD(0);
		final int padding = _bao.size() % 8;
		if (padding != 0)
		{
			for (int i = padding; i < 8; ++i)
			{
				writeC(0);
			}
		}
		return _bao.toByteArray();
	}
	
	public abstract void writeImpl();
}
