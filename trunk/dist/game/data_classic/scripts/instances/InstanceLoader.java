/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package instances;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Instance class-loader.
 * @author FallenAngel
 */
public final class InstanceLoader
{
	private static final Logger _log = Logger.getLogger(InstanceLoader.class.getName());
	
	private static final Class<?>[] SCRIPTS = {};
	
	public static void main(String[] args)
	{
		_log.info(InstanceLoader.class.getSimpleName() + ": Loading Instances scripts.");
		for (Class<?> script : SCRIPTS)
		{
			try
			{
				script.newInstance();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, InstanceLoader.class.getSimpleName() + ": Failed loading " + script.getSimpleName() + ":", e);
			}
		}
	}
}
