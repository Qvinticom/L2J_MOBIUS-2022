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
package ai.npc.Teleports.OrbisTemple;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.zone.L2ZoneType;

/**
 * Orbis Temple teleport AI.
 * @author Mobius
 */
public final class OrbisTemple extends AbstractNpcAI
{
	// Zones
	private static final int ZONE_ID_1 = 200201;
	private static final int ZONE_ID_2 = 200202;
	private static final int ZONE_ID_3 = 200203;
	private static final int ZONE_ID_4 = 200204;
	private static final int ZONE_ID_5 = 200205;
	private static final int ZONE_ID_6 = 200206;
	// Teleport Locations
	private static final Location TELEPORT_LOC_1 = new Location(198022, 90032, -192);
	private static final Location TELEPORT_LOC_2 = new Location(213983, 53250, -8176);
	private static final Location TELEPORT_LOC_3 = new Location(215056, 50467, -8416);
	private static final Location TELEPORT_LOC_4 = new Location(213799, 53253, -14432);
	private static final Location TELEPORT_LOC_5 = new Location(211137, 50501, -14624);
	private static final Location TELEPORT_LOC_6 = new Location(211641, 115547, -12736);
	
	private OrbisTemple()
	{
		super(OrbisTemple.class.getSimpleName(), "ai/npc/Teleports");
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
					character.teleToLocation(TELEPORT_LOC_2);
					break;
				}
				case ZONE_ID_2:
				{
					character.teleToLocation(TELEPORT_LOC_1);
					break;
				}
				case ZONE_ID_3:
				{
					character.teleToLocation(TELEPORT_LOC_4);
					break;
				}
				case ZONE_ID_4:
				{
					character.teleToLocation(TELEPORT_LOC_3);
					break;
				}
				case ZONE_ID_5:
				{
					character.teleToLocation(TELEPORT_LOC_6);
					break;
				}
				case ZONE_ID_6:
				{
					character.teleToLocation(TELEPORT_LOC_5);
					break;
				}
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	public static void main(String[] args)
	{
		new OrbisTemple();
	}
}