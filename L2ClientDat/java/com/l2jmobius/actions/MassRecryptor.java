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

import com.l2jmobius.L2ClientDat;
import com.l2jmobius.clientcryptor.DatFile;
import com.l2jmobius.clientcryptor.crypt.DatCrypter;

public class MassRecryptor
{
	private static final MassRecryptor _instance = new MassRecryptor();
	
	public static MassRecryptor getInstance()
	{
		return _instance;
	}
	
	public void recrypt(String chronicle, String path, DatCrypter decCrypter, DatCrypter enCrypter)
	{
		L2ClientDat.addLogConsole("Mass recrypt with using " + chronicle + " chronicles by path [" + path + "]", true);
		File baseDir = new File(path);
		if (!baseDir.exists())
		{
			L2ClientDat.addLogConsole("Directory [" + path + "] does not exists.", true);
			return;
		}
		String recryptDirPath = path + "/" + "!recrypted";
		File recryptDir = new File(recryptDirPath);
		if (!recryptDir.exists() && !recryptDir.mkdir())
		{
			L2ClientDat.addLogConsole("Cannot create recrypt directory [" + recryptDirPath + "].", true);
			return;
		}
		long startTime = System.currentTimeMillis();
		for (File file : baseDir.listFiles(pathname -> pathname.getName().endsWith(".dat") || pathname.getName().endsWith(".ini") || pathname.getName().endsWith(".htm")))
		{
			try
			{
				try (FileInputStream fis = new FileInputStream(file);)
				{
					if (fis.available() < 28)
					{
						L2ClientDat.addLogConsole(file.getName() + " The file is too small.", true);
						return;
					}
					byte[] head = new byte[28];
					fis.read(head);
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
					L2ClientDat.addLogConsole("Recrypting [" + file.getName() + "]", true);
					DatFile dat = new DatFile(file.getAbsolutePath());
					dat.decrypt(decCrypter);
					DatFile.encrypt(dat.getBuff().array(), recryptDirPath + "/" + file.getName(), enCrypter);
				}
			}
			catch (Exception e)
			{
				L2ClientDat.addLogConsole(file.getName() + " Decrypt failed.", true);
			}
		}
		long diffTime = (System.currentTimeMillis() - startTime) / 1000L;
		L2ClientDat.addLogConsole("Completed. Elapsed ".concat(String.valueOf(diffTime)).concat(" sec"), true);
	}
}
