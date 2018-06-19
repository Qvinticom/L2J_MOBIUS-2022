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
package com.l2jmobius.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import com.l2jmobius.util.DebugUtil;

import jfork.nproperty.Cfg;
import jfork.nproperty.CfgIgnore;
import jfork.nproperty.ConfigParser;

@Cfg
public class ConfigWindow extends ConfigParser
{
	@CfgIgnore
	private static final ConfigWindow _instance = new ConfigWindow();
	@CfgIgnore
	private static final String PATH = "./config/config_window.ini";
	public static String FILE_OPEN_CURRENT_DIRECTORY_UNPACK = ".";
	public static String FILE_OPEN_CURRENT_DIRECTORY_PACK = ".";
	public static String FILE_OPEN_CURRENT_DIRECTORY = ".";
	public static String FILE_SAVE_CURRENT_DIRECTORY = ".";
	public static String CURRENT_CHRONICLE = "";
	public static int WINDOW_HEIGHT = 600;
	public static int WINDOW_WIDTH = 800;
	public static String CURRENT_ENCRYPT = ".";
	public static String CURRENT_DECRYPT = ".";
	public static String LAST_FILE_SELECTED = ".";
	
	public static void load()
	{
		try
		{
			ConfigParser.parse((_instance), PATH);
		}
		catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e)
		{
			DebugUtil.getLogger().error("Failed to load configuration file.", e);
		}
	}
	
	public static void save(String key, String var)
	{
		try
		{
			Properties props = new Properties();
			props.load(new FileInputStream(PATH));
			props.setProperty(key, var);
			FileOutputStream output = new FileOutputStream(PATH);
			props.store(output, "Saved settings");
			output.close();
			load();
		}
		catch (Exception props)
		{
			// empty catch block
		}
	}
}
