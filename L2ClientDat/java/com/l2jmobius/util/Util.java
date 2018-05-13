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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class Util
{
	public static void printBytes(String name, byte[] buffer)
	{
		StringBuilder builder = new StringBuilder(buffer.length);
		builder.append(name).append(": [");
		for (byte b : buffer)
		{
			builder.append(b).append(" ");
		}
		builder.append("]");
		DebugUtil.getLogger().info(builder.toString());
	}
	
	public static boolean compareBuffers(byte[] b1, byte[] b2)
	{
		if ((b1 == null) || (b2 == null) || (b1.length != b2.length))
		{
			return false;
		}
		for (int i = 0; i < b1.length; ++i)
		{
			if (b1[i] == b2[i])
			{
				continue;
			}
			return false;
		}
		return true;
	}
	
	private static String printData(byte[] data, int len)
	{
		int a;
		int charpoint;
		byte t1;
		StringBuilder result = new StringBuilder();
		int counter = 0;
		for (int i = 0; i < len; ++i)
		{
			if ((counter % 16) == 0)
			{
				result.append(Util.fillHex(i, 4) + ": ");
			}
			result.append(Util.fillHex(data[i] & 255, 2) + " ");
			if (++counter != 16)
			{
				continue;
			}
			result.append("   ");
			charpoint = i - 15;
			for (a = 0; a < 16; ++a)
			{
				if (((t1 = data[charpoint++]) > 31) && (t1 < 128))
				{
					result.append((char) t1);
					continue;
				}
				result.append('.');
			}
			result.append("\n");
			counter = 0;
		}
		int rest = data.length % 16;
		if (rest > 0)
		{
			for (int i = 0; i < (17 - rest); ++i)
			{
				result.append("   ");
			}
			charpoint = data.length - rest;
			for (a = 0; a < rest; ++a)
			{
				if (((t1 = data[charpoint++]) > 31) && (t1 < 128))
				{
					result.append((char) t1);
					continue;
				}
				result.append('.');
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	private static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		for (int i = number.length(); i < digits; ++i)
		{
			number = "0" + number;
		}
		return number;
	}
	
	public static String printData(byte[] blop)
	{
		return Util.printData(blop, blop.length);
	}
	
	public static List<File> loadFiles(String dir, String prefix)
	{
		ArrayList<File> list = new ArrayList<>();
		File folder = new File(dir);
		for (File listOfFile : folder.listFiles())
		{
			if (listOfFile.isFile())
			{
				if (!listOfFile.getName().endsWith(prefix))
				{
					continue;
				}
				list.add(listOfFile);
				continue;
			}
			if (!listOfFile.isDirectory())
			{
				continue;
			}
			File folder2 = new File(dir + listOfFile.getName() + "/");
			for (File aListOfFiles2 : folder2.listFiles())
			{
				if (!aListOfFiles2.getName().endsWith(prefix))
				{
					continue;
				}
				list.add(aListOfFiles2);
			}
		}
		return list;
	}
	
	public static String[] getDirsNames(String dir, String prefix)
	{
		ArrayList<String> list = new ArrayList<>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null)
		{
			return null;
		}
		for (File listOfFile : listOfFiles)
		{
			if (!listOfFile.isDirectory() || !listOfFile.getName().endsWith(prefix))
			{
				continue;
			}
			list.add(listOfFile.getName());
		}
		String[] text = new String[list.size()];
		int i = 0;
		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext())
		{
			text[i] = iterator.next();
			++i;
		}
		return text;
	}
	
	public static String[] getFilesNames(String dir, String prefix)
	{
		ArrayList<String> list = new ArrayList<>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null)
		{
			return null;
		}
		for (File listOfFile : listOfFiles)
		{
			if (!listOfFile.isFile() || !listOfFile.getName().endsWith(prefix))
			{
				continue;
			}
			list.add(listOfFile.getName().replace(prefix, ""));
		}
		String[] text = new String[list.size()];
		int i = 0;
		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext())
		{
			text[i] = iterator.next();
			++i;
		}
		return text;
	}
	
	public static String printData(ByteBuffer buf)
	{
		byte[] data = new byte[buf.remaining()];
		buf.get(data);
		String hex = Util.printData(data, data.length);
		buf.position(buf.position() - data.length);
		return hex;
	}
	
	public static List<String> splitList(String s)
	{
		if (s.startsWith("{"))
		{
			s = s.substring(1, s.length() - 1);
		}
		ArrayList<String> res = new ArrayList<>();
		StringBuffer buff = new StringBuffer();
		int level = 0;
		for (char part : s.toCharArray())
		{
			if ((part == '{') || (part == '['))
			{
				++level;
			}
			else if ((part == '}') || (part == ']'))
			{
				--level;
			}
			else if ((part == ';') && (level == 0))
			{
				res.add(buff.toString());
				buff = new StringBuffer();
				continue;
			}
			buff.append(part);
		}
		res.add(buff.toString());
		return res;
	}
	
	public static Map<String, String> stringToMap(String id)
	{
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		for (String str : id.split("\t"))
		{
			if (!str.contains("="))
			{
				continue;
			}
			int index = str.indexOf("=");
			String key = str.substring(0, index);
			String val = str.substring(index + 1, str.length());
			map.put(key, val);
		}
		return map;
	}
	
	public static String mapToString(Map<String, String> map)
	{
		StringBuilder builder = new StringBuilder();
		for (String key : map.keySet())
		{
			builder.append(key).append("=").append(map.get(key)).append("\t");
		}
		return builder.toString();
	}
	
	@SuppressWarnings(
	{
		"rawtypes",
		"unchecked"
	})
	public static void compileJavaClass(String sourceFile)
	{
		try
		{
			DiagnosticCollector diagnostics = new DiagnosticCollector();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
			Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Util.loadFiles(sourceFile, ".java"));
			JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnit);
			if (!task.call().booleanValue())
			{
				for (Object diagnostic : diagnostics.getDiagnostics())
				{
					final Diagnostic d = (Diagnostic) diagnostic;
					System.out.format("Error on line %d in %s%n", d.getLineNumber(), ((JavaFileObject) d.getSource()).toUri());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static Object loadJavaClass(String className, String path)
	{
		try
		{
			@SuppressWarnings("resource")
			URLClassLoader classLoader = new URLClassLoader(new URL[]
			{
				new File(path).toURI().toURL()
			});
			Class<?> loadedClass = classLoader.loadClass(className);
			return loadedClass.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
