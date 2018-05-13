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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.l2jmobius.util.DebugUtil;

import jfork.nproperty.Cfg;
import jfork.nproperty.CfgIgnore;
import jfork.nproperty.ConfigParser;

@Cfg
public class ConfigDebug extends ConfigParser
{
	@CfgIgnore
	private static final ConfigDebug _instance = new ConfigDebug();
	public static boolean DAT_ADD_END_BYTES = true;
	public static boolean DAT_DEBUG_MSG = false;
	public static boolean DAT_DEBUG_POS = false;
	public static int DAT_DEBUG_POS_LIMIT = 100000;
	public static boolean DAT_REPLACEMENT_NAMES = true;
	public static boolean ENCRYPT = true;
	public static boolean SAVE_DECODE = false;
	
	public static void load()
	{
		try
		{
			ConfigParser.parse((_instance), "./config/config_debug.ini");
		}
		catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e)
		{
			DebugUtil.getLogger().error("Failed to load configuration file.", e);
		}
	}
}
