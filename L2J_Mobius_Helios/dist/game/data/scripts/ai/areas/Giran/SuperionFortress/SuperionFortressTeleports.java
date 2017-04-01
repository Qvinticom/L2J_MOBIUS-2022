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
package ai.areas.Giran.SuperionFortress;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;

import ai.AbstractNpcAI;

/**
 * Superion Fortress Teleports AI
 * @author Gigi
 * @date 2017-03-19 - [12:30:43]
 */
public class SuperionFortressTeleports extends AbstractNpcAI
{
	// Zones
	private static final int ZONE_ID_1 = 60166;
	private static final int ZONE_ID_2 = 60167;
	private static final int ZONE_ID_3 = 60168;
	private static final int ZONE_ID_4 = 60169;
	// Teleport Locations
	private static final Location TELEPORT_LOC_1 = new Location(79331, 194233, -10232);
	private static final Location TELEPORT_LOC_2 = new Location(81728, 155577, 480);
	private static final Location TELEPORT_LOC_3 = new Location(76178, 194233, -10248);
	private static final Location TELEPORT_LOC_4 = new Location(82702, 154180, 480);
	
	public SuperionFortressTeleports()
	{
		addEnterZoneId(ZONE_ID_1); // Out of Fortress ---> Superion Port
		addEnterZoneId(ZONE_ID_2); // Superion Port ---> Out of Fortress
		addEnterZoneId(ZONE_ID_3); // Out of Fortress ---> Superion Starboard
		addEnterZoneId(ZONE_ID_4); // Superion Starboard ---> Out of Fortress
	}
	
	@Override
	public String onEnterZone(L2Character creature, L2ZoneType zone)
	{
		if (creature.isPlayer())
		{
			switch (zone.getId())
			{
				case ZONE_ID_1:
				{
					creature.teleToLocation(TELEPORT_LOC_1);
					break;
				}
				case ZONE_ID_2:
				{
					creature.teleToLocation(TELEPORT_LOC_2);
					break;
				}
				case ZONE_ID_3:
				{
					creature.teleToLocation(TELEPORT_LOC_3);
					break;
				}
				case ZONE_ID_4:
				{
					creature.teleToLocation(TELEPORT_LOC_4);
					break;
				}
			}
		}
		return super.onEnterZone(creature, zone);
	}
	
	public static void main(String[] args)
	{
		new SuperionFortressTeleports();
	}
}
