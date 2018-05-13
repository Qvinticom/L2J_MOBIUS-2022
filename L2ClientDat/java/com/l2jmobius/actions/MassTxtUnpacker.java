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
package com.l2jmobius.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.l2jmobius.L2ClientDat;
import com.l2jmobius.clientcryptor.DatFile;
import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.data.GameDataName;
import com.l2jmobius.util.DebugUtil;
import com.l2jmobius.xml.Descriptor;
import com.l2jmobius.xml.DescriptorParser;
import com.l2jmobius.xml.DescriptorReader;

public class MassTxtUnpacker
{
	private static final MassTxtUnpacker _instance = new MassTxtUnpacker();
	
	public static MassTxtUnpacker getInstance()
	{
		return _instance;
	}
	
	public void unpack(String chronicle, String path, DatCrypter decCrypter)
	{
		L2ClientDat.addLogConsole("Mass unpacker with using " + chronicle + " chronicles by path [" + path + "]", true);
		File baseDir = new File(path);
		if (!baseDir.exists())
		{
			L2ClientDat.addLogConsole("Directory [" + path + "] does not exists.", true);
			return;
		}
		String unpackDirPath = path + "/" + "!unpacked";
		File unpackDir = new File(unpackDirPath);
		if (!unpackDir.exists() && !unpackDir.mkdir())
		{
			L2ClientDat.addLogConsole("Cannot create recrypt directory [" + unpackDirPath + "].", true);
			return;
		}
		GameDataName.getInstance().clear();
		long startTime = System.currentTimeMillis();
		for (File file : baseDir.listFiles(pathname -> decCrypter.checkFileExtension(pathname.getName())))
		{
			try
			{
				try (FileInputStream fis = new FileInputStream(file);)
				{
					String name;
					String res;
					String charset;
					if (fis.available() < 28)
					{
						L2ClientDat.addLogConsole(file.getName() + " The file is too small.", true);
						continue;
					}
					byte[] head = new byte[28];
					fis.read(head);
					fis.close();
					String header = new String(head, "UTF-16LE");
					if (!header.startsWith("Lineage2Ver"))
					{
						L2ClientDat.addLogConsole("File " + file.getName() + " not encrypted. Skip decrypt.", true);
						continue;
					}
					if (Integer.valueOf(header.substring(11)).intValue() != decCrypter.getCode())
					{
						L2ClientDat.addLogConsole("File " + file.getName() + " encrypted code: " + header + ". Skip decrypt.", true);
						continue;
					}
					L2ClientDat.addLogConsole("Unpacking [" + file.getName() + "]", true);
					DatFile dat = new DatFile(file.getAbsolutePath());
					dat.decrypt(decCrypter);
					DebugUtil.save(dat.getBuff(), file);
					res = null;
					charset = "UTF-8";
					if (decCrypter.isUseStructure() && file.getName().endsWith(".dat"))
					{
						Descriptor desc = DescriptorParser.getInstance().findDescriptorForFile(chronicle, file.getName());
						try
						{
							res = DescriptorReader.getInstance().parseData(file, decCrypter, desc, dat.getBuff());
						}
						catch (Exception e23)
						{
							L2ClientDat.addLogConsole("Cannot parse [" + file.getName() + "]", true);
						}
						name = file.getName().replace(".dat", ".txt");
					}
					else
					{
						name = file.getName();
						if (file.getName().endsWith(".unr"))
						{
							name = "dec-" + name;
						}
						if (file.getName().endsWith(".htm"))
						{
							charset = "UTF-16";
						}
						try
						{
							try (FileOutputStream fos = new FileOutputStream(unpackDirPath + "/" + name);)
							{
								fos.write(dat.getBuff().array());
							}
							catch (Throwable throwable)
							{
								throw throwable;
							}
						}
						catch (UnsupportedEncodingException e3)
						{
							e3.printStackTrace();
						}
					}
					if (res == null)
					{
						continue;
					}
					try
					{
						try (PrintWriter fos = new PrintWriter(new OutputStreamWriter(new FileOutputStream(unpackDirPath + "/" + name), charset));)
						{
							fos.write(res);
						}
						catch (Throwable throwable)
						{
							throw throwable;
						}
					}
					catch (Exception e4)
					{
						DebugUtil.getLogger().error("Cannot write file.", e4);
					}
				}
			}
			catch (Exception e)
			{
				DebugUtil.getLogger().error(file.getName() + " Decrypt failed.", e);
			}
		}
		long diffTime = (System.currentTimeMillis() - startTime) / 1000L;
		L2ClientDat.addLogConsole("Completed. Elapsed ".concat(String.valueOf(diffTime)).concat(" sec"), true);
	}
}
