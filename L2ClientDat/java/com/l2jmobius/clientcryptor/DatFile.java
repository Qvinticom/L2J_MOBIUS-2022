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
package com.l2jmobius.clientcryptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.config.ConfigDebug;

public class DatFile extends File
{
	private ByteBuffer _buff;
	
	public DatFile(String pathname)
	{
		super(pathname);
	}
	
	public ByteBuffer getBuff()
	{
		return _buff;
	}
	
	public void decrypt(DatCrypter crypter) throws IOException
	{
		loadInfo(crypter);
		try
		{
			try (FileInputStream fis = new FileInputStream(this);)
			{
				crypter.unlock();
				crypter.aquire();
				fis.skip(28L);
				byte[] buff = new byte[crypter.getChunkSize(fis.available())];
				for (int len = fis.available() - crypter.getSkipSize(); len > 0; len -= fis.read(buff))
				{
					crypter.update(buff);
					if (!crypter.isLock())
					{
						continue;
					}
				}
				if (crypter.isLock())
				{
					_buff = null;
					return;
				}
				_buff = crypter.decryptResult();
				crypter.release();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void encrypt(byte[] buff, String file, DatCrypter crypter) throws IOException
	{
		crypter.unlock();
		crypter.aquire();
		FileOutputStream os = new FileOutputStream(file, false);
		String header = "Lineage2Ver" + crypter.getCode();
		os.write(header.getBytes("UTF-16LE"));
		crypter.update(buff);
		byte[] res = crypter.encryptResult().array();
		os.write(res);
		if (ConfigDebug.DAT_ADD_END_BYTES)
		{
			byte[] endBytes = new byte[20];
			endBytes[19] = 100;
			os.write(endBytes);
		}
		os.close();
		crypter.release();
	}
	
	@SuppressWarnings("resource")
	private void loadInfo(DatCrypter crypter) throws IOException
	{
		if (!exists() || !canRead())
		{
			throw new IOException("Can not read the dat file");
		}
		FileInputStream fis = new FileInputStream(this);
		if (fis.available() < 28)
		{
			throw new IOException("Can not read the dat file : too small");
		}
		byte[] head = new byte[28];
		fis.read(head);
		String header = new String(head, "UTF-16LE");
		if (!header.startsWith("Lineage2Ver"))
		{
			throw new IOException("Can not read the dat file : wrong header");
		}
		if (header.endsWith("111") || header.endsWith("120"))
		{
			return;
		}
		if (header.endsWith("211") || header.endsWith("212"))
		{
			return;
		}
		if (header.endsWith("311"))
		{
			return;
		}
		if (header.endsWith("411") || header.endsWith("412") || header.endsWith("413") || header.endsWith("414"))
		{
			if (fis.available() < 20)
			{
				throw new IOException("Can not read the dat file : too small");
			}
		}
		else
		{
			throw new IOException("Can not read the dat file : unknown header : '" + header + "'");
		}
		fis.close();
	}
}
