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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.l2jmobius.config.ConfigDebug;
import com.l2jmobius.xml.Variant;

public class DebugUtil
{
	private static Logger _log = LogManager.getLogger(DebugUtil.class);
	
	public static void debug(String message)
	{
		if (ConfigDebug.DAT_DEBUG_MSG)
		{
			_log.info(message);
		}
	}
	
	public static void debugPos(int pos, String name, Variant val)
	{
		if (ConfigDebug.DAT_DEBUG_POS)
		{
			_log.info("pos: " + pos + " " + name + ": " + val);
			if ((ConfigDebug.DAT_DEBUG_POS_LIMIT != 0) && (pos > ConfigDebug.DAT_DEBUG_POS_LIMIT))
			{
				System.exit(0);
			}
		}
	}
	
	public static void save(ByteBuffer buffer, File path)
	{
		if (ConfigDebug.SAVE_DECODE)
		{
			try
			{
				String unpackDirPath = path.getParent() + "/" + "!decrypted";
				File decryptedDir = new File(unpackDirPath);
				decryptedDir.mkdir();
				File file = new File(decryptedDir + "/" + path.getName());
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(buffer.array());
				fos.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static Logger getLogger()
	{
		return _log;
	}
	
	static
	{
		DOMConfigurator.configure("./config/log4j.xml");
	}
}
