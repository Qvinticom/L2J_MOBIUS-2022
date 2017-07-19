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
package com.l2jmobius.gameserver.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.entity.TvTEvent;

public class EventEngine
{
	public static void load()
	{
		// Load all Events and their settings
		final Properties eventSettings = new Properties();
		try (InputStream is = new FileInputStream(new File(Config.EVENTS_CONFIG_FILE)))
		{
			eventSettings.load(is);
		}
		catch (final Exception e)
		{
			System.err.println("Error while loading Events Settings.");
			e.printStackTrace();
		}
		
		// TvT Event
		TvTEvent.initialize(eventSettings);
	}
}