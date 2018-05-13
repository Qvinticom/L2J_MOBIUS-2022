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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESDatCrypter extends DatCrypter
{
	private ByteArrayOutputStream _result;
	private final boolean encrypt;
	private final Cipher _cipher;
	
	public DESDatCrypter(int code, String sKey, boolean deCrypt) throws Exception
	{
		super(code);
		encrypt = !deCrypt;
		byte[] key = sKey.getBytes();
		byte[] keyXor = new byte[key.length];
		for (int i = 0; i < key.length; ++i)
		{
			byte[] arrby = keyXor;
			int n = i % 8;
			arrby[n] = (byte) (arrby[n] ^ key[i]);
		}
		DESKeySpec dks = new DESKeySpec(keyXor);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		SecretKey desKey = skf.generateSecret(dks);
		_cipher = Cipher.getInstance("DES/ECB/NoPadding");
		_cipher.init(deCrypt ? 2 : 1, desKey);
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
		try
		{
			if (!encrypt)
			{
				int size;
				_result = new ByteArrayOutputStream(bArray.length);
				byte[] bytes = new byte[8];
				for (int position = 0; position < bArray.length; position += size)
				{
					size = Math.min(8, bArray.length - position);
					System.arraycopy(bArray, position, bytes, 0, size);
					_result.write(size == 8 ? _cipher.doFinal(bytes) : bytes, 0, size);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public int getChunkSize(int available)
	{
		return available;
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
	}
}
