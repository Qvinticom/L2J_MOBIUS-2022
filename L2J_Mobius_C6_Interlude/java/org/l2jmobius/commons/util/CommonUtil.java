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
package org.l2jmobius.commons.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @author luisantonioa
 */
public class CommonUtil
{
	public static boolean isInternalIP(String ipAddress)
	{
		return ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.startsWith("127.0.0.1");
	}
	
	/**
	 * Method to generate the hexadecimal representation of a byte array.<br>
	 * 16 bytes per row, while ascii chars or "." is shown at the end of the line.
	 * @param data the byte array to be represented in hexadecimal representation
	 * @param len the number of bytes to represent in hexadecimal representation
	 * @return byte array represented in hexadecimal format
	 */
	public static String printData(byte[] data, int len)
	{
		return new String(HexUtils.bArr2HexEdChars(data, len));
	}
	
	/**
	 * This call is equivalent to Util.printData(data, data.length)
	 * @see CommonUtil#printData(byte[],int)
	 * @param data data to represent in hexadecimal
	 * @return byte array represented in hexadecimal format
	 */
	public static String printData(byte[] data)
	{
		return printData(data, data.length);
	}
	
	/**
	 * converts a given time from minutes -> miliseconds
	 * @param minutesToConvert
	 * @return
	 */
	public static int convertMinutesToMiliseconds(int minutesToConvert)
	{
		return minutesToConvert * 60000;
	}
	
	/**
	 * Method to generate a random sequence of bytes returned as byte array
	 * @param size number of random bytes to generate
	 * @return byte array with sequence of random bytes
	 */
	public static byte[] generateHex(int size)
	{
		final byte[] array = new byte[size];
		Rnd.nextBytes(array);
		
		// Don't allow 0s inside the array!
		for (int i = 0; i < array.length; i++)
		{
			while (array[i] == 0)
			{
				array[i] = (byte) Rnd.get(Byte.MAX_VALUE);
			}
		}
		return array;
	}
	
	/**
	 * Method to get the stack trace of a Throwable into a String
	 * @param t Throwable to get the stacktrace from
	 * @return stack trace from Throwable as String
	 */
	public static String getStackTrace(Throwable t)
	{
		final StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	/**
	 * @param <T>
	 * @param array - the array to look into
	 * @param obj - the object to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise.
	 */
	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
		{
			if (element.equals(obj))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param array - the array to look into
	 * @param obj - the integer to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise
	 */
	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param array - the array to look into
	 * @param obj - the object to search for
	 * @param ignoreCase
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise.
	 */
	public static boolean contains(String[] array, String obj, boolean ignoreCase)
	{
		for (String element : array)
		{
			if (element.equals(obj) || (ignoreCase && element.equalsIgnoreCase(obj)))
			{
				return true;
			}
		}
		return false;
	}
}
