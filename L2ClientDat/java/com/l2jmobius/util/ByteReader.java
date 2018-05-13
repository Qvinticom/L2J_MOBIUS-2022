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
package com.l2jmobius.util;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteReader
{
	private static Charset defaultCharset = Charset.forName("cp1252");
	private static Charset utf16leCharset = Charset.forName("utf-16le");
	
	public static char readChar(ByteBuffer buffer)
	{
		return (char) buffer.get();
	}
	
	public static int readUByte(ByteBuffer buffer)
	{
		return buffer.get() & 255;
	}
	
	public static int readInt(ByteBuffer buffer)
	{
		return Integer.reverseBytes(buffer.getInt());
	}
	
	public static int readUInt(ByteBuffer buffer)
	{
		return ByteReader.readInt(buffer);
	}
	
	public static short readShort(ByteBuffer buffer)
	{
		return Short.reverseBytes(buffer.getShort());
	}
	
	public static double readDouble(ByteBuffer buffer)
	{
		return Double.longBitsToDouble(Long.reverseBytes(buffer.getLong()));
	}
	
	public static long readLong(ByteBuffer buffer)
	{
		return Long.reverseBytes(buffer.getLong());
	}
	
	public static float readFloat(ByteBuffer buffer)
	{
		return Float.intBitsToFloat(Integer.reverseBytes(buffer.getInt()));
	}
	
	public static int readCompactInt(ByteBuffer input) throws IOException
	{
		int output = 0;
		boolean signed = false;
		for (int i = 0; i < 5; ++i)
		{
			int x = input.get() & 255;
			if (x < 0)
			{
				throw new EOFException();
			}
			if (i == 0)
			{
				if ((x & 128) > 0)
				{
					signed = true;
				}
				output |= x & 63;
				if ((x & 64) != 0)
				{
					continue;
				}
				break;
			}
			if (i == 4)
			{
				output |= (x & 31) << 27;
				continue;
			}
			output |= (x & 127) << (6 + ((i - 1) * 7));
			if ((x & 128) == 0)
			{
				break;
			}
		}
		if (signed)
		{
			output *= -1;
		}
		return output;
	}
	
	public static String readRGB(ByteBuffer buffer)
	{
		String g;
		String b;
		String r = Integer.toHexString(buffer.get() & 255).toUpperCase();
		if (r.length() < 2)
		{
			r = "0" + r;
		}
		if ((g = Integer.toHexString(buffer.get() & 255).toUpperCase()).length() < 2)
		{
			g = "0" + g;
		}
		if ((b = Integer.toHexString(buffer.get() & 255).toUpperCase()).length() < 2)
		{
			b = "0" + b;
		}
		return r + g + b;
	}
	
	public static String readRGBA(ByteBuffer buffer)
	{
		String a = Integer.toHexString(buffer.get() & 255).toUpperCase();
		if (a.length() < 2)
		{
			a = "0" + a;
		}
		return a + ByteReader.readRGB(buffer);
	}
	
	public static String readUtfString(ByteBuffer buffer, boolean isRaw) throws Exception
	{
		int size = ByteReader.readInt(buffer);
		if (size <= 0)
		{
			return "";
		}
		if (size > 1000000)
		{
			throw new Exception("To much data.");
		}
		byte[] bytes = new byte[size];
		try
		{
			for (int i = 0; i < size; i += 2)
			{
				bytes[i + 1] = buffer.get();
				bytes[i] = buffer.get();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ByteReader.checkAndReplaceNewLine(isRaw, new String(new String(bytes, "Unicode").getBytes("UTF-8"), "UTF-8"));
	}
	
	public static String readString(ByteBuffer input, boolean isRaw) throws IOException
	{
		int len = ByteReader.readCompactInt(input);
		if (len == 0)
		{
			return "";
		}
		byte[] bytes = new byte[len > 0 ? len : -2 * len];
		input.get(bytes);
		return ByteReader.checkAndReplaceNewLine(isRaw, new String(bytes, 0, bytes.length - (len > 0 ? 1 : 2), len > 0 ? defaultCharset : utf16leCharset).intern());
	}
	
	private static String checkAndReplaceNewLine(boolean isRaw, String str)
	{
		if (!isRaw && str.contains("\r\n"))
		{
			str = str.replace("\r\n", "\\r\\n");
		}
		return str;
	}
}
