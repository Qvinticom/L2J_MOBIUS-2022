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
package org.l2jmobius.gameserver.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class has just one simple function to return the item id of the crown related to a castle id.
 * @author Mobius
 */
public class CrownTable
{
	public static final int CROWN_OF_THE_LORD = 6841;
	
	private static final Map<Integer, Integer> CROWNS = new HashMap<>();
	static
	{
		CROWNS.put(1, 6838); // Gludio
		CROWNS.put(2, 6835); // Dion
		CROWNS.put(3, 6839); // Giran
		CROWNS.put(4, 6837); // Oren
		CROWNS.put(5, 6840); // Aden
		CROWNS.put(6, 6834); // Innadril
		CROWNS.put(7, 6836); // Goddard
		CROWNS.put(8, 8182); // Rune
		CROWNS.put(9, 8183); // Schuttgart
	}
	
	public static Collection<Integer> getCrownList()
	{
		return CROWNS.values();
	}
	
	public static int getCrownId(int castleId)
	{
		final Integer crownId = CROWNS.get(castleId);
		if (crownId != null)
		{
			return crownId.intValue();
		}
		return 0;
	}
}
