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
package com.l2jmobius.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.l2jmobius.L2ClientDat;
import com.l2jmobius.clientcryptor.DatFile;
import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.config.ConfigDebug;
import com.l2jmobius.util.ByteReader;
import com.l2jmobius.util.ByteWriter;
import com.l2jmobius.util.Util;
import com.l2jmobius.xml.ParamNode;

public class GameDataName
{
	private static final byte[] endFileBytes = new byte[]
	{
		12,
		83,
		97,
		102,
		101,
		80,
		97,
		99,
		107,
		97,
		103,
		101,
		0
	};
	private final Map<Integer, String> names = new HashMap<>();
	private final Map<String, Integer> nameHash = new HashMap<>();
	private final Map<String, Integer> nameLowHash = new HashMap<>();
	
	private void load(File currentFile, DatCrypter decCrypter) throws Exception
	{
		names.clear();
		nameHash.clear();
		nameLowHash.clear();
		if (decCrypter.isEncrypt())
		{
			File file = new File(currentFile.getParent(), "L2GameDataName.txt");
			if (file.exists())
			{
				List<String> list = Files.readAllLines(file.toPath());
				for (int i = 0; i < list.size(); ++i)
				{
					String str = list.get(i);
					Map<String, String> map = Util.stringToMap(str);
					String name = map.get("name");
					name = name.substring(1, name.length() - 1);
					names.put(i, name);
					nameHash.put(name, i);
					nameLowHash.put(name.toLowerCase(), i);
				}
				L2ClientDat.addLogConsole("GameDataName: Load " + names.size() + " count.", true);
			}
		}
		else
		{
			File file = new File(currentFile.getParent(), "L2GameDataName.dat");
			FileInputStream fis = new FileInputStream(file);
			if (fis.available() < 28)
			{
				L2ClientDat.addLogConsole(file.getName() + " The file is too small.", true);
				fis.close();
				return;
			}
			byte[] head = new byte[28];
			fis.read(head);
			fis.close();
			String header = new String(head, "UTF-16LE");
			if (!header.startsWith("Lineage2Ver"))
			{
				L2ClientDat.addLogConsole("GameDataName: File " + file.getName() + " not encrypted. Skip decrypt.", true);
				return;
			}
			if (Integer.valueOf(header.substring(11)).intValue() != decCrypter.getCode())
			{
				L2ClientDat.addLogConsole("GameDataName: File " + file.getName() + " encrypted code: " + header + ". Skip decrypt.", true);
				return;
			}
			L2ClientDat.addLogConsole("Unpacking [" + file.getName() + "]", true);
			DatFile dat = new DatFile(file.getAbsolutePath());
			dat.decrypt(decCrypter);
			ByteBuffer buff = dat.getBuff();
			int size = ByteReader.readUInt(buff);
			for (int i = 0; i < size; ++i)
			{
				String name = ByteReader.readUtfString(buff, false);
				if (name.contains("[") || name.contains("]"))
				{
					L2ClientDat.addLogConsole("GameDataName: Error index: " + i + " string: " + name, true);
				}
				names.put(i, name);
				nameHash.put(name, i);
				nameLowHash.put(name.toLowerCase(), i);
			}
			L2ClientDat.addLogConsole("GameDataName: Load " + names.size() + " count.", true);
		}
	}
	
	public String getString(File currentFile, DatCrypter crypter, int index) throws Exception
	{
		String val;
		if (names.isEmpty())
		{
			load(currentFile, crypter);
		}
		if (!names.containsKey(index))
		{
			L2ClientDat.addLogConsole("GameDataName: Not found index: " + index, true);
		}
		if ((val = names.getOrDefault(index, String.valueOf(index))).isEmpty())
		{
			L2ClientDat.addLogConsole("GameDataName: String name Empty!!! file: " + currentFile.getName(), true);
		}
		return "[" + val + "]";
	}
	
	public synchronized int getId(File currentFile, DatCrypter crypter, ParamNode node, String str) throws Exception
	{
		String low;
		if (!str.startsWith("[") || !str.endsWith("]"))
		{
			L2ClientDat.addLogConsole("GameDataName: String name not brackets!!! file: " + currentFile.getName() + " str: " + str + " node: " + node, true);
		}
		if ((str = str.substring(1, str.length() - 1)).isEmpty())
		{
			L2ClientDat.addLogConsole("GameDataName: String name Empty!!! file: " + currentFile.getName() + " node: " + node, true);
			return -1;
		}
		if (nameLowHash.isEmpty())
		{
			load(currentFile, crypter);
		}
		if (nameLowHash.containsKey(low = str.toLowerCase()))
		{
			return nameLowHash.get(low);
		}
		int newIndex = nameLowHash.size();
		nameLowHash.put(low, newIndex);
		nameHash.put(str, nameHash.size());
		return newIndex;
	}
	
	public void checkAndUpdate(String currentDir, DatCrypter crypter) throws Exception
	{
		if (!nameHash.isEmpty())
		{
			HashSet<String> setList = new HashSet<>();
			TreeMap<Integer, String> sortedMap = new TreeMap<>();
			for (String key : nameHash.keySet())
			{
				sortedMap.put(nameHash.get(key), key);
				if (setList.add(key.toLowerCase()))
				{
					continue;
				}
				L2ClientDat.addLogConsole("GameDataName: name " + key + " conflicted.", true);
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write((byte[]) ByteWriter.writeInt(sortedMap.size()).array());
			for (String key : sortedMap.values())
			{
				baos.write((byte[]) ByteWriter.writeUtfString(key, false).array());
			}
			byte[] bytes = baos.toByteArray();
			byte[] resultBytes = new byte[bytes.length + endFileBytes.length];
			System.arraycopy(bytes, 0, resultBytes, 0, bytes.length);
			System.arraycopy(endFileBytes, 0, resultBytes, bytes.length, endFileBytes.length);
			String file = currentDir + "/L2GameDataName.dat";
			if (ConfigDebug.ENCRYPT)
			{
				DatFile.encrypt(resultBytes, file, crypter);
			}
			else
			{
				FileOutputStream os = new FileOutputStream(file, false);
				os.write(resultBytes);
				os.close();
			}
			L2ClientDat.addLogConsole("GameDataName: packed " + sortedMap.size() + " count.", true);
			names.clear();
			nameHash.clear();
			nameLowHash.clear();
		}
	}
	
	public void clear()
	{
		names.clear();
		nameHash.clear();
		nameLowHash.clear();
	}
	
	public static GameDataName getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		static final GameDataName _instance = new GameDataName();
		
		private SingletonHolder()
		{
		}
	}
	
}
