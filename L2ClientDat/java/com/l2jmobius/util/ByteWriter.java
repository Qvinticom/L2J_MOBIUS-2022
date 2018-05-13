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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class ByteWriter
{
	private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	private static Charset defaultCharset = Charset.forName("ascii");
	private static Charset utf16leCharset = Charset.forName("utf-16le");
	
	public static Buffer writeChar(byte value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(1).order(BYTE_ORDER);
		buffer.put(value);
		return buffer;
	}
	
	public static Buffer writeCompactInt(int count)
	{
		byte[] b = ByteWriter.compactIntToByteArray(count);
		ByteBuffer buffer = ByteBuffer.allocate(b.length).order(BYTE_ORDER);
		buffer.put(b);
		return buffer;
	}
	
	public static Buffer writeByte(byte value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(1).order(BYTE_ORDER);
		buffer.put(value);
		return buffer;
	}
	
	public static Buffer writeUByte(short value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(1).order(BYTE_ORDER);
		buffer.put((byte) value);
		return buffer;
	}
	
	public static Buffer writeInt(int value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4).order(BYTE_ORDER);
		buffer.putInt(value);
		return buffer;
	}
	
	public static Buffer writeUInt(int value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4).order(BYTE_ORDER);
		buffer.putInt(value);
		return buffer;
	}
	
	public static Buffer writeShort(short value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(2).order(BYTE_ORDER);
		buffer.putShort(value);
		return buffer;
	}
	
	public static Buffer writeUShort(int value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(2).order(BYTE_ORDER);
		buffer.put((byte) (value & 255));
		buffer.put((byte) ((value & 65280) >> 8));
		return buffer;
	}
	
	public static Buffer writeRGB(String rgb)
	{
		ByteBuffer buffer = ByteBuffer.allocate(3).order(BYTE_ORDER);
		buffer.put((byte) Integer.parseInt(rgb.substring(0, 2), 16));
		buffer.put((byte) Integer.parseInt(rgb.substring(2, 4), 16));
		buffer.put((byte) Integer.parseInt(rgb.substring(4, 6), 16));
		return buffer;
	}
	
	public static Buffer writeRGBA(String rgba)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4).order(BYTE_ORDER);
		buffer.put((byte[]) ByteWriter.writeRGB(rgba.substring(0, 6)).array());
		buffer.put((byte) Integer.parseInt(rgba.substring(6, 8), 16));
		return buffer;
	}
	
	public static Buffer writeUtfString(String str, boolean isRaw)
	{
		int size = str.length();
		if (size <= 0)
		{
			return ByteBuffer.allocate(4).order(BYTE_ORDER).putInt(0);
		}
		if (!isRaw)
		{
			str = ByteWriter.checkAndReplaceNewLine(str);
			size = str.length();
		}
		ByteBuffer buffer = ByteBuffer.allocate((size * 2) + 4).order(BYTE_ORDER);
		buffer.putInt(size * 2);
		for (int i = 0; i < size; ++i)
		{
			buffer.putChar(str.charAt(i));
		}
		return buffer;
	}
	
	public static Buffer writeString(String s, boolean isRaw)
	{
		if ((s == null) || s.isEmpty())
		{
			return ByteWriter.writeCompactInt(0);
		}
		if (!isRaw)
		{
			s = ByteWriter.checkAndReplaceNewLine(s);
		}
		s = s + '\u0000';
		boolean def = defaultCharset.newEncoder().canEncode(s);
		byte[] bytes = s.getBytes(def ? defaultCharset : utf16leCharset);
		byte[] bSize = ByteWriter.compactIntToByteArray(def ? bytes.length : (-bytes.length) / 2);
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length + bSize.length).order(BYTE_ORDER);
		buffer.put(bSize);
		buffer.put(bytes);
		return buffer;
	}
	
	public static Buffer writeDouble(double value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(8).order(BYTE_ORDER);
		buffer.putDouble(value);
		return buffer;
	}
	
	public static Buffer writeFloat(float value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4).order(BYTE_ORDER);
		buffer.putFloat(value);
		return buffer;
	}
	
	public static Buffer writeLong(long value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(8).order(BYTE_ORDER);
		buffer.putLong(value);
		return buffer;
	}
	
	private static byte[] compactIntToByteArray(int v)
	{
		boolean negative = v < 0;
		v = Math.abs(v);
		int[] bytes = new int[]
		{
			v & 63,
			(v >> 6) & 127,
			(v >> 13) & 127,
			(v >> 20) & 127,
			(v >> 27) & 127
		};
		if (negative)
		{
			int[] arrn = bytes;
			arrn[0] = arrn[0] | 128;
		}
		int size = 5;
		for (int i = 4; (i > 0) && (bytes[i] == 0); --i)
		{
			--size;
		}
		byte[] res = new byte[size];
		for (int i = 0; i < size; ++i)
		{
			if (i != (size - 1))
			{
				int[] arrn = bytes;
				int n = i;
				arrn[n] = arrn[n] | (i == 0 ? 64 : 128);
			}
			res[i] = (byte) bytes[i];
		}
		return res;
	}
	
	private static String checkAndReplaceNewLine(String str)
	{
		if (str.contains("\\r\\n"))
		{
			str = str.replace("\\r\\n", "\r\n");
		}
		return str;
	}
}
