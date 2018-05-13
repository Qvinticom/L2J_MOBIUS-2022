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
package com.l2jmobius.clientcryptor.crypt;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class XorDatCrypter extends DatCrypter
{
	private ByteArrayOutputStream _result;
	private final boolean encrypt;
	private final int xorKey;
	
	public XorDatCrypter(int code, int key, boolean deCrypt)
	{
		super(code);
		encrypt = !deCrypt;
		xorKey = key;
	}
	
	@Override
	public ByteBuffer decryptResult()
	{
		return ByteBuffer.wrap(_result.toByteArray());
	}
	
	@Override
	public ByteBuffer encryptResult()
	{
		return ByteBuffer.wrap(_result.toByteArray());
	}
	
	@Override
	public void update(byte[] bArray)
	{
		for (byte b : bArray)
		{
			_result.write(b ^ xorKey);
		}
	}
	
	@Override
	public int getChunkSize(int available)
	{
		return 1;
	}
	
	@Override
	public int getSkipSize()
	{
		return 0;
	}
	
	@Override
	public boolean isLock()
	{
		return false;
	}
	
	@Override
	public boolean isEncrypt()
	{
		return encrypt;
	}
	
	@Override
	public void unlock()
	{
	}
	
	@Override
	public void aquire()
	{
		super.aquire();
		_result = new ByteArrayOutputStream(128);
	}
}
