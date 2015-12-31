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
package ai.npc.Teleports.GainakUndergroundEntrance;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;

import ai.npc.AbstractNpcAI;

/**
 * Gainak Underground Entrance teleport AI.
 * @author Mobius
 */
public final class GainakUndergroundEntrance extends AbstractNpcAI
{
	// Zones
	private static final int ZONE_ID_1 = 200207;
	private static final int ZONE_ID_2 = 200208;
	private static final int ZONE_ID_3 = 200209;
	private static final int ZONE_ID_4 = 200210;
	private static final int ZONE_ID_5 = 200211;
	private static final int ZONE_ID_6 = 200212;
	// Teleport Locations
	private static final Location TELEPORT_LOC_1 = new Location(-49596, -150715, -14472);
	private static final Location TELEPORT_LOC_2 = new Location(17600, -113803, -312);
	private static final Location TELEPORT_LOC_3 = new Location(-55283, -147410, -14728);
	private static final Location TELEPORT_LOC_4 = new Location(17067, -111738, -320);
	private static final Location TELEPORT_LOC_5 = new Location(-46867, -149309, -14216);
	private static final Location TELEPORT_LOC_6 = new Location(18784, -115648, -248);
	
	private GainakUndergroundEntrance()
	{
		super(GainakUndergroundEntrance.class.getSimpleName(), "ai/npc/Teleports");
		addEnterZoneId(ZONE_ID_1, ZONE_ID_2, ZONE_ID_3, ZONE_ID_4, ZONE_ID_5, ZONE_ID_6);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.isPlayer())
		{
			switch (zone.getId())
			{
				case ZONE_ID_1:
				{
					character.teleToLocation(TELEPORT_LOC_1);
					break;
				}
				case ZONE_ID_2:
				{
					character.teleToLocation(TELEPORT_LOC_2);
					break;
				}
				case ZONE_ID_3:
				{
					character.teleToLocation(TELEPORT_LOC_3);
					break;
				}
				case ZONE_ID_4:
				{
					character.teleToLocation(TELEPORT_LOC_4);
					break;
				}
				case ZONE_ID_5:
				{
					character.teleToLocation(TELEPORT_LOC_5);
					break;
				}
				case ZONE_ID_6:
				{
					character.teleToLocation(TELEPORT_LOC_6);
					break;
				}
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	public static void main(String[] args)
	{
		new GainakUndergroundEntrance();
	}
}