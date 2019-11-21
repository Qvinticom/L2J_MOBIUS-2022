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

public class Crypt
{
	byte[] _key;
	
	public void setKey(byte[] key)
	{
		_key = new byte[key.length];
		System.arraycopy(key, 0, _key, 0, key.length);
	}
	
	public long decrypt(byte[] raw)
	{
		if (_key == null)
		{
			return 0L;
		}
		int temp = 0;
		int j = 0;
		for (int i = 0; i < raw.length; ++i)
		{
			int temp2 = raw[i] & 0xFF;
			raw[i] = (byte) (temp2 ^ (_key[j++] & 0xFF) ^ temp);
			temp = temp2;
			if (j <= 7)
			{
				continue;
			}
			j = 0;
		}
		long old = _key[0] & 0xFF;
		old |= (_key[1] << 8) & 0xFF00;
		old |= (_key[2] << 16) & 0xFF0000;
		old |= (_key[3] << 24) & 0xFF000000;
		_key[0] = (byte) ((old += raw.length) & 0xFFL);
		_key[1] = (byte) ((old >> 8) & 0xFFL);
		_key[2] = (byte) ((old >> 16) & 0xFFL);
		_key[3] = (byte) ((old >> 24) & 0xFFL);
		return old;
	}
	
	public long encrypt(byte[] raw)
	{
		if (_key == null)
		{
			return 0L;
		}
		int temp = 0;
		int j = 0;
		for (int i = 0; i < raw.length; ++i)
		{
			int temp2 = raw[i] & 0xFF;
			raw[i] = (byte) (temp2 ^ (_key[j++] & 0xFF) ^ temp);
			temp = raw[i];
			if (j <= 7)
			{
				continue;
			}
			j = 0;
		}
		long old = _key[0] & 0xFF;
		old |= (_key[1] << 8) & 0xFF00;
		old |= (_key[2] << 16) & 0xFF0000;
		old |= (_key[3] << 24) & 0xFF000000;
		_key[0] = (byte) ((old += raw.length) & 0xFFL);
		_key[1] = (byte) ((old >> 8) & 0xFFL);
		_key[2] = (byte) ((old >> 16) & 0xFFL);
		_key[3] = (byte) ((old >> 24) & 0xFFL);
		return old;
	}
}
