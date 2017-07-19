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
package com.l2jmobius.gameserver.util;

import com.l2jserver.gameserver.geoengine.Direction;

/**
 * @author FBIagent
 */
public final class GeoUtils
{
	public interface PointListener
	{
		/**
		 * @param x
		 * @param y
		 * @return true proceed, false abort
		 */
		boolean onPoint(int x, int y);
	}
	
	/**
	 * difference between x values: never abover 1<br>
	 * difference between y values: never above 1
	 * @param lastX
	 * @param lastY
	 * @param x
	 * @param y
	 * @return
	 */
	public static Direction computeDirection(int lastX, int lastY, int x, int y)
	{
		if (x > lastX) // east
		{
			if (y > lastY)
			{
				return Direction.SOUTH_EAST;
			}
			else if (y < lastY)
			{
				return Direction.NORTH_EAST;
			}
			else
			{
				return Direction.EAST;
			}
		}
		else if (x < lastX) // west
		{
			if (y > lastY)
			{
				return Direction.SOUTH_WEST;
			}
			else if (y < lastY)
			{
				return Direction.NORTH_WEST;
			}
			else
			{
				return Direction.WEST;
			}
		}
		else
		// unchanged x
		{
			if (y > lastY)
			{
				return Direction.SOUTH;
			}
			else if (y < lastY)
			{
				return Direction.NORTH;
			}
			else
			{
				return null;// error, should never happen, TODO: Logging
			}
		}
	}
}