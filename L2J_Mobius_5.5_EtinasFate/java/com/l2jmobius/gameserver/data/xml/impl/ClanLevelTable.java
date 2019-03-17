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
package com.l2jmobius.gameserver.data.xml.impl;

/**
 * @author Mobius
 */
public class ClanLevelTable
{
	// TODO: Move to XML.
	private final static int[] CLAN_LEVEL_REQUIREMENTS =
	{
		35000,
		80000, // Level 2 requirement.
		140000,
		315000,
		560000,
		965000,
		2690000,
		4050000,
		5930000,
		7560000,
		11830000,
		19110000,
		27300000,
		36400000,
		46410000,
		0 // Max level (15).
	};
	
	public static int getLevelRequirement(int clanLevel)
	{
		return CLAN_LEVEL_REQUIREMENTS[clanLevel];
	}
}
