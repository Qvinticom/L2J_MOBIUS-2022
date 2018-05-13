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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.l2jmobius.L2ClientDat;
import com.l2jmobius.clientcryptor.DatFile;
import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.data.GameDataName;
import com.l2jmobius.util.DebugUtil;
import com.l2jmobius.xml.Descriptor;
import com.l2jmobius.xml.DescriptorParser;
import com.l2jmobius.xml.DescriptorReader;

public class OpenDat
{
	public static void start(String dir, File path, String name, DatCrypter crypter) throws Exception
	{
		ByteBuffer buffer;
		boolean crypt = true;
		if (!path.exists())
		{
			L2ClientDat.addLogConsole("File does not exist.", true);
			return;
		}
		if (!path.canRead())
		{
			L2ClientDat.addLogConsole("Unable to read file.", true);
			return;
		}
		FileInputStream fis = new FileInputStream(path);
		if (fis.available() < 28)
		{
			L2ClientDat.addLogConsole("The file is too small.", true);
			fis.close();
			return;
		}
		byte[] head = new byte[28];
		fis.read(head);
		String header = new String(head, "UTF-16LE");
		if (!header.startsWith("Lineage2Ver"))
		{
			L2ClientDat.addLogConsole("File " + name + " not encrypted. Skip decrypt.", true);
			crypt = false;
		}
		fis.close();
		buffer = null;
		if (crypt)
		{
			L2ClientDat.addLogConsole("File " + name + " encrypted. " + header + " decrypt ...", true);
			try
			{
				DatFile dat = new DatFile(path.getPath());
				dat.decrypt(crypter);
				buffer = dat.getBuff();
				if (buffer == null)
				{
					L2ClientDat.addLogConsole("Error decrypt file. Empty buffer.", true);
					return;
				}
			}
			catch (Exception e)
			{
				L2ClientDat.addLogConsole("Error decrypt file.", true);
				return;
			}
			DebugUtil.save(buffer, path);
			L2ClientDat.addLogConsole("Decrypt successfully.", true);
		}
		else
		{
			try
			{
				try (FileInputStream fIn = new FileInputStream(path);)
				{
					FileChannel fChan = fIn.getChannel();
					ByteBuffer mBuf = ByteBuffer.allocate((int) fChan.size());
					fChan.read(mBuf);
					buffer = mBuf;
					fChan.close();
					fIn.close();
				}
			}
			catch (IOException exc)
			{
				L2ClientDat.addLogConsole("Error reading.", true);
			}
		}
		if (name.contains(".ini") || name.contains(".txt"))
		{
			if ((buffer != null) && buffer.hasArray())
			{
				L2ClientDat.addText(new String(buffer.array(), 0, buffer.array().length, "UTF-8"));
			}
		}
		else if (name.contains(".htm"))
		{
			if ((buffer != null) && buffer.hasArray())
			{
				L2ClientDat.addText(new String(buffer.array(), 0, buffer.array().length, "UTF-16"));
			}
		}
		else if (crypter.isUseStructure())
		{
			L2ClientDat.addLogConsole("Read the file structure ...", true);
			String data = null;
			Descriptor desc = DescriptorParser.getInstance().findDescriptorForFile(dir, name);
			if ((desc != null) && (buffer != null))
			{
				buffer.position(0);
				DebugUtil.debug("Buffer size: " + buffer.limit());
				GameDataName.getInstance().clear();
				data = DescriptorReader.getInstance().parseData(path, crypter, desc, buffer);
			}
			if (data == null)
			{
				L2ClientDat.addLogConsole("Structure is not found in the directory: " + dir + " file: " + name, true);
				return;
			}
			L2ClientDat.addText(data);
		}
		L2ClientDat.addLogConsole("Completed.", true);
	}
}
