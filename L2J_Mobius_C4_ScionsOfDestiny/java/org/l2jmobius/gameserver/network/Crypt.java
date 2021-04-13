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
package org.l2jmobius.gameserver.network;

import org.l2jmobius.commons.network.ICrypt;

import io.netty.buffer.ByteBuf;

/**
 * @author KenM
 */
public class Crypt implements ICrypt
{
	// private final GameClient _client;
	private final byte[] _inKey = new byte[8];
	private final byte[] _outKey = new byte[8];
	private boolean _isEnabled;
	
	public Crypt(GameClient client)
	{
		// _client = client;
	}
	
	public void setKey(byte[] key)
	{
		System.arraycopy(key, 0, _inKey, 0, 8);
		System.arraycopy(key, 0, _outKey, 0, 8);
	}
	
	@Override
	public void encrypt(ByteBuf buf)
	{
		if (!_isEnabled)
		{
			_isEnabled = true;
			onPacketSent(buf);
			return;
		}
		
		onPacketSent(buf);
		
		int a = 0;
		while (buf.isReadable())
		{
			final int b = buf.readByte() & 0xFF;
			a = b ^ _outKey[(buf.readerIndex() - 1) & 7] ^ a;
			buf.setByte(buf.readerIndex() - 1, a);
		}
		
		shiftKey(_outKey, buf.writerIndex());
	}
	
	@Override
	public void decrypt(ByteBuf buf)
	{
		if (!_isEnabled)
		{
			onPacketReceive(buf);
			return;
		}
		
		int a = 0;
		while (buf.isReadable())
		{
			final int b = buf.readByte() & 0xFF;
			buf.setByte(buf.readerIndex() - 1, b ^ _inKey[(buf.readerIndex() - 1) & 7] ^ a);
			a = b;
		}
		
		shiftKey(_inKey, buf.writerIndex());
		
		onPacketReceive(buf);
	}
	
	private void onPacketSent(ByteBuf buf)
	{
		final byte[] data = new byte[buf.writerIndex()];
		buf.getBytes(0, data);
		// EventDispatcher.getInstance().notifyEvent(new OnPacketSent(_client, data));
	}
	
	private void onPacketReceive(ByteBuf buf)
	{
		final byte[] data = new byte[buf.writerIndex()];
		buf.getBytes(0, data);
		// EventDispatcher.getInstance().notifyEvent(new OnPacketReceived(_client, data));
	}
	
	private void shiftKey(byte[] key, int size)
	{
		int old = key[0] & 0xff;
		old |= (key[1] << 8) & 0xff00;
		old |= (key[2] << 0x10) & 0xff0000;
		old |= (key[3] << 0x18) & 0xff000000;
		
		old += size;
		
		key[0] = (byte) (old & 0xff);
		key[1] = (byte) ((old >> 0x08) & 0xff);
		key[2] = (byte) ((old >> 0x10) & 0xff);
		key[3] = (byte) ((old >> 0x18) & 0xff);
	}
}
