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

import java.io.IOException;

public class NewCrypt
{
	BlowfishEngine _crypt;
	BlowfishEngine _decrypt;
	
	public NewCrypt(String key)
	{
		byte[] keybytes = key.getBytes();
		_crypt = new BlowfishEngine();
		_crypt.init(true, keybytes);
		_decrypt = new BlowfishEngine();
		_decrypt.init(false, keybytes);
	}
	
	public boolean checksum(byte[] raw)
	{
		long ecx;
		long chksum = 0L;
		int count = raw.length - 8;
		int i = 0;
		for (i = 0; i < count; i += 4)
		{
			ecx = raw[i] & 0xFF;
			ecx |= (raw[i + 1] << 8) & 0xFF00;
			ecx |= (raw[i + 2] << 16) & 0xFF0000;
			chksum ^= (ecx |= (raw[i + 3] << 24) & 0xFF000000);
		}
		ecx = raw[i] & 0xFF;
		ecx |= (raw[i + 1] << 8) & 0xFF00;
		ecx |= (raw[i + 2] << 16) & 0xFF0000;
		raw[i] = (byte) (chksum & 0xFFL);
		raw[i + 1] = (byte) ((chksum >> 8) & 0xFFL);
		raw[i + 2] = (byte) ((chksum >> 16) & 0xFFL);
		raw[i + 3] = (byte) ((chksum >> 24) & 0xFFL);
		return (ecx |= (raw[i + 3] << 24) & 0xFF000000) == chksum;
	}
	
	public byte[] decrypt(byte[] raw) throws IOException
	{
		byte[] result = new byte[raw.length];
		int count = raw.length / 8;
		for (int i = 0; i < count; ++i)
		{
			_decrypt.processBlock(raw, i * 8, result, i * 8);
		}
		return result;
	}
	
	public byte[] crypt(byte[] raw) throws IOException
	{
		int count = raw.length / 8;
		byte[] result = new byte[raw.length];
		for (int i = 0; i < count; ++i)
		{
			_crypt.processBlock(raw, i * 8, result, i * 8);
		}
		return result;
	}
}
