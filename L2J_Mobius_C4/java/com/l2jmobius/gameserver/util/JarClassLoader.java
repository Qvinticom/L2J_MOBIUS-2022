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
package com.l2jmobius.gameserver.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This is a class loader for the dynamic extensions used by DynamicExtension class.
 * @version $Revision: $ $Date: $
 * @author galun
 */
public class JarClassLoader extends ClassLoader
{
	private static Logger log = Logger.getLogger(JarClassLoader.class.getCanonicalName());
	HashSet<String> jars = new HashSet<>();
	
	public void addJarFile(String filename)
	{
		jars.add(filename);
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException
	{
		try
		{
			final byte[] b = loadClassData(name);
			return defineClass(name, b, 0, b.length);
		}
		catch (final Exception e)
		{
			throw new ClassNotFoundException(name);
		}
	}
	
	private byte[] loadClassData(String name) throws IOException
	{
		byte[] classData = null;
		for (final String jarFile : jars)
		{
			final File file = new File(jarFile);
			@SuppressWarnings("resource")
			final ZipFile zipFile = new ZipFile(file);
			final String fileName = name.replace('.', '/') + ".class";
			final ZipEntry entry = zipFile.getEntry(fileName);
			if (entry == null)
			{
				continue;
			}
			
			try (DataInputStream zipStream = new DataInputStream(zipFile.getInputStream(entry)))
			{
				classData = new byte[(int) entry.getSize()];
				zipStream.readFully(classData, 0, (int) entry.getSize());
				break;
			}
			catch (final IOException e)
			{
				log.log(Level.WARNING, jarFile + ":" + e.toString(), e);
				continue;
			}
		}
		
		if (classData == null)
		{
			throw new IOException("class not found in " + jars);
		}
		return classData;
	}
}