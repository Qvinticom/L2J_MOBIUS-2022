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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;

import com.l2jmobius.util.DebugUtil;
import com.l2jmobius.util.Util;

public class RSADatCrypter extends DatCrypter
{
	private Cipher _cipher;
	private ByteArrayOutputStream _result;
	private boolean encrypt = false;
	private boolean _errorLock = false;
	
	public RSADatCrypter(int code, String modulus, String exp, boolean deCrypt)
	{
		super(code);
		try
		{
			_cipher = Cipher.getInstance("RSA/ECB/nopadding");
			if (deCrypt)
			{
				RSAPublicKeySpec keyspec = new RSAPublicKeySpec(new BigInteger(modulus, 16), new BigInteger(exp, 16));
				RSAPublicKey rsaKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keyspec);
				_cipher.init(2, rsaKey);
			}
			else
			{
				encrypt = true;
				RSAPrivateKeySpec keyspec = new RSAPrivateKeySpec(new BigInteger(modulus, 16), new BigInteger(exp, 16));
				RSAPrivateKey rsaKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keyspec);
				_cipher.init(1, rsaKey);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public ByteBuffer decryptResult()
	{
		if (!checkAquired())
		{
			throw new IllegalStateException("Dont even think about using a DatCrypter that you didnt aquired");
		}
		byte[] compressed = _result.toByteArray();
		int inflatedSize = compressed[0] & 255;
		inflatedSize += (compressed[1] << 8) & 65280;
		inflatedSize += (compressed[2] << 16) & 16711680;
		inflatedSize += (compressed[3] << 24) & -16777216;
		ByteArrayInputStream bais = new ByteArrayInputStream(compressed, 4, compressed.length - 4);
		InflaterInputStream iis = new InflaterInputStream(bais, new Inflater());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
		byte[] inflatedResult = new byte[128];
		try
		{
			int len;
			while ((len = iis.read(inflatedResult)) > 0)
			{
				baos.write(inflatedResult, 0, len);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if (baos.size() != inflatedSize)
		{
			DebugUtil.getLogger().error("[RSADatCrypter] Hum inflated result doesnt have the expected length..(" + baos.size() + "!=" + inflatedSize + ")");
		}
		return ByteBuffer.wrap(baos.toByteArray());
	}
	
	@Override
	public ByteBuffer encryptResult()
	{
		if (!checkAquired())
		{
			throw new IllegalStateException("Dont even think about using a DatCrypter that you didnt aquired");
		}
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try
		{
			int len;
			ByteArrayInputStream input = new ByteArrayInputStream(_result.toByteArray());
			byte[] buffer = new byte[124];
			byte[] block = new byte[128];
			while ((len = input.read(buffer)) > 0)
			{
				Arrays.fill(block, (byte) 0);
				block[0] = (byte) ((len >> 24) & 255);
				block[1] = (byte) ((len >> 16) & 255);
				block[2] = (byte) ((len >> 8) & 255);
				block[3] = (byte) (len & 255);
				System.arraycopy(buffer, 0, block, 128 - len - ((124 - len) % 4), len);
				result.write(_cipher.doFinal(block));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ByteBuffer.wrap(result.toByteArray());
	}
	
	@Override
	public void update(byte[] b)
	{
		if (!checkAquired())
		{
			throw new IllegalStateException("Dont even think about using a DatCrypter that you didnt aquired");
		}
		try
		{
			if (!encrypt)
			{
				byte[] chunk = _cipher.doFinal(b);
				int size = chunk[3];
				size += (chunk[2] << 8) & 65280;
				size += (chunk[1] << 16) & 16711680;
				int pad = (-size & 1) + (-(size += (chunk[0] << 24) & -16777216) & 2);
				DebugUtil.debug("Size:" + size + " pad:" + pad);
				if (size > 128)
				{
					_errorLock = true;
					return;
				}
				_result.write(chunk, 128 - size - pad, size);
				DebugUtil.debug("--- BLOCK:\n" + Util.printData(chunk) + "-----");
			}
			else
			{
				try
				{
					ByteArrayOutputStream s = new ByteArrayOutputStream(b.length);
					DeflaterOutputStream dos = new DeflaterOutputStream(s, new Deflater());
					dos.write(b);
					dos.finish();
					dos.close();
					int l = b.length;
					_result = new ByteArrayOutputStream(10 + s.toByteArray().length);
					_result.write(l & 255);
					_result.write((l & 65280) >> 8);
					_result.write((l & 16711680) >> 16);
					_result.write((l & -16777216) >> 24);
					_result.write(s.toByteArray());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_errorLock = true;
		}
	}
	
	@Override
	public void aquire()
	{
		super.aquire();
		_result = new ByteArrayOutputStream(128);
	}
	
	@Override
	public boolean isEncrypt()
	{
		return encrypt;
	}
	
	@Override
	public int getChunkSize(int available)
	{
		return 128;
	}
	
	@Override
	public int getSkipSize()
	{
		return 20;
	}
	
	@Override
	public boolean isLock()
	{
		return _errorLock;
	}
	
	@Override
	public void unlock()
	{
		_errorLock = false;
	}
}
