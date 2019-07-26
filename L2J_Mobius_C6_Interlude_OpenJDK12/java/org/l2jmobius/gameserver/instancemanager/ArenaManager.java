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
package org.l2jmobius.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.zone.type.ArenaZone;

public class ArenaManager
{
	private static ArenaManager _instance;
	private static final Logger LOGGER = Logger.getLogger(ArenaManager.class.getName());
	
	public static final ArenaManager getInstance()
	{
		if (_instance == null)
		{
			LOGGER.info("Initializing ArenaManager");
			_instance = new ArenaManager();
		}
		return _instance;
	}
	
	private List<ArenaZone> _arenas;
	
	public ArenaManager()
	{
	}
	
	public void addArena(ArenaZone arena)
	{
		if (_arenas == null)
		{
			_arenas = new ArrayList<>();
		}
		
		_arenas.add(arena);
	}
	
	public final ArenaZone getArena(Creature creature)
	{
		if (_arenas != null)
		{
			for (ArenaZone temp : _arenas)
			{
				if (temp.isCharacterInZone(creature))
				{
					return temp;
				}
			}
		}
		
		return null;
	}
	
	public final ArenaZone getArena(int x, int y, int z)
	{
		if (_arenas != null)
		{
			for (ArenaZone temp : _arenas)
			{
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		
		return null;
	}
}
