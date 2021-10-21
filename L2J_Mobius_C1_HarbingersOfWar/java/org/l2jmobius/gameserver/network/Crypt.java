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

/**
 * @author KenM
 */
public class Crypt
{
	private byte[] _inKey;
	private byte[] _outKey;
	
	public void setKey(byte[] key)
	{
		_inKey = new byte[key.length];
		_outKey = new byte[key.length];
		System.arraycopy(key, 0, _inKey, 0, key.length);
		System.arraycopy(key, 0, _outKey, 0, key.length);
	}
	
	public void decrypt(byte[] raw)
	{
		if (_inKey == null)
		{
			return;
		}
		
		int temp = 0;
		int j = 0;
		for (int i = 0; i < raw.length; ++i)
		{
			final int temp2 = raw[i] & 0xff;
			raw[i] = (byte) (temp2 ^ (_inKey[j++] & 0xff) ^ temp);
			temp = temp2;
			if (j <= 7)
			{
				continue;
			}
			j = 0;
		}
		
		// Shift key.
		int old = _inKey[0] & 0xff;
		old |= (_inKey[1] << 8) & 0xff00;
		old |= (_inKey[2] << 16) & 0xff0000;
		old |= (_inKey[3] << 24) & 0xff000000;
		old += raw.length;
		_inKey[0] = (byte) (old & 0xff);
		_inKey[1] = (byte) ((old >> 8) & 0xff);
		_inKey[2] = (byte) ((old >> 16) & 0xff);
		_inKey[3] = (byte) ((old >> 24) & 0xff);
	}
	
	public void encrypt(byte[] raw)
	{
		if (_outKey == null)
		{
			return;
		}
		
		int temp = 0;
		int j = 0;
		for (int i = 0; i < raw.length; ++i)
		{
			final int temp2 = raw[i] & 0xff;
			raw[i] = (byte) (temp2 ^ (_outKey[j++] & 0xff) ^ temp);
			temp = raw[i];
			if (j <= 7)
			{
				continue;
			}
			j = 0;
		}
		
		// Shift key.
		int old = _outKey[0] & 0xff;
		old |= (_outKey[1] << 8) & 0xff00;
		old |= (_outKey[2] << 16) & 0xff0000;
		old |= (_outKey[3] << 24) & 0xff000000;
		old += raw.length;
		_outKey[0] = (byte) (old & 0xff);
		_outKey[1] = (byte) ((old >> 8) & 0xff);
		_outKey[2] = (byte) ((old >> 16) & 0xff);
		_outKey[3] = (byte) ((old >> 24) & 0xff);
	}
}
