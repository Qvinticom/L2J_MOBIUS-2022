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
package com.l2jmobius.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.Logger;

import com.l2jmobius.Config;

public final class L2Properties extends Properties
{
	protected static final Logger LOGGER = Logger.getLogger(Config.class.getName());
	
	private boolean _warn = false;
	
	public L2Properties()
	{
	}
	
	public L2Properties setLog(boolean warn)
	{
		_warn = warn;
		
		return this;
	}
	
	public L2Properties(String name) throws IOException
	{
		load(new FileInputStream(name));
	}
	
	public L2Properties(File file) throws IOException
	{
		load(new FileInputStream(file));
	}
	
	public L2Properties(InputStream inStream)
	{
		load(inStream);
	}
	
	public L2Properties(Reader reader)
	{
		load(reader);
	}
	
	public void load(String name) throws IOException
	{
		load(new FileInputStream(name));
	}
	
	public void load(File file) throws IOException
	{
		load(new FileInputStream(file));
	}
	
	@Override
	public synchronized void load(InputStream inStream)
	{
		try
		{
			super.load(inStream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (inStream != null)
			{
				try
				{
					inStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public synchronized void load(Reader reader)
	{
		try
		{
			super.load(reader);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public String getProperty(String key)
	{
		final String property = super.getProperty(key);
		
		if (property == null)
		{
			if (_warn)
			{
				LOGGER.warning("L2Properties: Missing property for key - " + key);
			}
			return null;
		}
		return property.trim();
	}
	
	@Override
	public String getProperty(String key, String defaultValue)
	{
		final String property = super.getProperty(key, defaultValue);
		
		if (property == null)
		{
			if (_warn)
			{
				LOGGER.warning("L2Properties: Missing defaultValue for key - " + key);
			}
			return null;
		}
		return property.trim();
	}
}