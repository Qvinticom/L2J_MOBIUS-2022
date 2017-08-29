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
package com.l2jmobius.gameserver.model.teleporter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author UnAfraid
 */
public class TeleportHolder
{
	private final int _npcId;
	private final Map<TeleportType, Map<Integer, TeleportLocation>> _teleportLocations = new EnumMap<>(TeleportType.class);
	
	public TeleportHolder(int id)
	{
		_npcId = id;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public void addLocation(TeleportType type, TeleportLocation loc)
	{
		_teleportLocations.computeIfAbsent(type, val -> new TreeMap<>()).put(loc.getId(), loc);
	}
	
	public TeleportLocation getLocation(TeleportType type, int index)
	{
		return _teleportLocations.getOrDefault(type, Collections.emptyMap()).get(index);
	}
	
	public Collection<TeleportLocation> getLocations(TeleportType type)
	{
		return _teleportLocations.getOrDefault(type, Collections.emptyMap()).values();
	}
}