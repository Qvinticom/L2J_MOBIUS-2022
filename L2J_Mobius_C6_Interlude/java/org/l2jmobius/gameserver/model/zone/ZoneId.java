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
package org.l2jmobius.gameserver.model.zone;

/**
 * Zone Ids.
 * @author Mobius
 */
public enum ZoneId
{
	PVP,
	PEACE,
	SIEGE,
	MOTHERTREE,
	CLAN_HALL,
	UNUSED,
	NO_LANDING,
	WATER,
	JAIL,
	MONSTER_TRACK,
	SWAMP,
	NO_SUMMON_FRIEND,
	OLYMPIAD,
	NO_HQ,
	DANGER_AREA,
	NO_STORE;
	
	public static int getZoneCount()
	{
		return values().length;
	}
}
