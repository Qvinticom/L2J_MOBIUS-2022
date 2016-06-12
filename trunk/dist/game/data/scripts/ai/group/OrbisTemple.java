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
package ai.group;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;

import ai.AbstractNpcAI;

/**
 * Orbis Temple AI.
 * @author Gladicek
 */
public final class OrbisTemple extends AbstractNpcAI
{
	private static final Map<Integer, Location> TELEPORT_DATA = new HashMap<>();
	
	static
	{
		TELEPORT_DATA.put(12036, new Location(213983, 53250, -8176));
		TELEPORT_DATA.put(12037, new Location(198022, 90032, -192));
		TELEPORT_DATA.put(12038, new Location(213799, 53253, -14432));
		TELEPORT_DATA.put(12039, new Location(215056, 50467, -8416));
		TELEPORT_DATA.put(12040, new Location(211641, 115547, -12736));
		TELEPORT_DATA.put(12041, new Location(211137, 50501, -14624));
	}
	
	public OrbisTemple()
	{
		addEnterZoneId(TELEPORT_DATA.keySet());
	}
	
	@Override
	public String onEnterZone(L2Character creature, L2ZoneType zone)
	{
		if (creature.isPlayer() && (creature.getInstanceWorld() == null))
		{
			creature.teleToLocation(TELEPORT_DATA.get(zone.getId()));
		}
		return super.onEnterZone(creature, zone);
	}
	
	public static void main(String[] args)
	{
		new OrbisTemple();
	}
}