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
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import com.l2jmobius.L2ClientDat;
import com.l2jmobius.clientcryptor.DatFile;
import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.config.ConfigDebug;
import com.l2jmobius.data.GameDataName;
import com.l2jmobius.util.DebugUtil;
import com.l2jmobius.xml.Descriptor;
import com.l2jmobius.xml.DescriptorParser;
import com.l2jmobius.xml.DescriptorWriter;
import com.l2jmobius.xml.exceptions.PackDataException;

public class MassTxtPacker
{
	private static final MassTxtPacker _instance = new MassTxtPacker();
	
	public static MassTxtPacker getInstance()
	{
		return _instance;
	}
	
	public void pack(String chronicle, String path, DatCrypter crypter) throws Exception
	{
		L2ClientDat.addLogConsole("Mass packer with using " + chronicle + " chronicles by path [" + path + "]", true);
		File baseDir = new File(path);
		if (!baseDir.exists())
		{
			L2ClientDat.addLogConsole("Directory [" + path + "] does not exists.", true);
			return;
		}
		String packDirPath = path + "/" + "!packed";
		File packDir = new File(packDirPath);
		if (!packDir.exists() && !packDir.mkdir())
		{
			L2ClientDat.addLogConsole("Cannot create directory [" + packDir + "].", true);
			return;
		}
		File[] fList = baseDir.listFiles(pathname -> crypter.checkFileExtension(pathname.getName()));
		GameDataName.getInstance().clear();
		long startTime = System.currentTimeMillis();
		CountDownLatch doneSignal = new CountDownLatch(fList.length);
		Semaphore semaphore = new Semaphore(Runtime.getRuntime().availableProcessors());
		for (File file : fList)
		{
			new Thread(() ->
			{
				try
				{
					semaphore.acquire();
					L2ClientDat.addLogConsole("Packing [" + file.getName() + "]", true);
					try
					{
						byte[] buff = null;
						String name = file.getName();
						if (crypter.isUseStructure())
						{
							name = name.replace(".txt", ".dat");
							byte[] array = Files.readAllBytes(file.toPath());
							String joined = new String(array, 0, array.length, "UTF-8");
							Descriptor desc = DescriptorParser.getInstance().findDescriptorForFile(chronicle, name);
							if (desc != null)
							{
								buff = DescriptorWriter.parseData(file, crypter, desc, joined);
							}
							else
							{
								L2ClientDat.addLogConsole("Not found the structure of the file: " + file.getName(), true);
							}
						}
						else
						{
							buff = Files.readAllBytes(file.toPath());
						}
						String fResult = packDir + "/" + name;
						if (buff != null)
						{
							if (ConfigDebug.ENCRYPT)
							{
								DatFile.encrypt(buff, fResult, crypter);
							}
							else
							{
								FileOutputStream os = new FileOutputStream(fResult, false);
								os.write(buff);
								os.close();
							}
						}
					}
					catch (PackDataException e)
					{
						DebugUtil.getLogger().error(e);
					}
					catch (Exception e)
					{
						DebugUtil.getLogger().error(e.getMessage(), e);
					}
					doneSignal.countDown();
					semaphore.release();
				}
				catch (InterruptedException e)
				{
					DebugUtil.getLogger().error(e.getMessage(), e);
				}
			}).start();
		}
		doneSignal.await();
		GameDataName.getInstance().checkAndUpdate(packDir.getPath(), crypter);
		long diffTime = (System.currentTimeMillis() - startTime) / 1000L;
		L2ClientDat.addLogConsole("Completed. Elapsed ".concat(String.valueOf(diffTime)).concat(" sec"), true);
	}
}
