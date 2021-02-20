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
package org.l2jmobius.gameserver.geoengine.pathfinding;

import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.geoengine.geodata.Cell;

/**
 * @author -Nemesiss-, HorridoJoho
 */
public class NodeLoc extends AbstractNodeLoc
{
	private int _x;
	private int _y;
	private boolean _goNorth;
	private boolean _goEast;
	private boolean _goSouth;
	private boolean _goWest;
	private int _geoHeight;
	
	public NodeLoc(int x, int y, int z)
	{
		set(x, y, z);
	}
	
	public void set(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_goNorth = GeoEngine.getInstance().checkNearestNswe(x, y, z, Cell.NSWE_NORTH);
		_goEast = GeoEngine.getInstance().checkNearestNswe(x, y, z, Cell.NSWE_EAST);
		_goSouth = GeoEngine.getInstance().checkNearestNswe(x, y, z, Cell.NSWE_SOUTH);
		_goWest = GeoEngine.getInstance().checkNearestNswe(x, y, z, Cell.NSWE_WEST);
		_geoHeight = GeoEngine.getInstance().getNearestZ(x, y, z);
	}
	
	public boolean canGoNorth()
	{
		return _goNorth;
	}
	
	public boolean canGoEast()
	{
		return _goEast;
	}
	
	public boolean canGoSouth()
	{
		return _goSouth;
	}
	
	public boolean canGoWest()
	{
		return _goWest;
	}
	
	public boolean canGoAll()
	{
		return canGoNorth() && canGoEast() && canGoSouth() && canGoWest();
	}
	
	@Override
	public int getX()
	{
		return GeoEngine.getInstance().getWorldX(_x);
	}
	
	@Override
	public int getY()
	{
		return GeoEngine.getInstance().getWorldY(_y);
	}
	
	@Override
	public int getZ()
	{
		return _geoHeight;
	}
	
	@Override
	public int getNodeX()
	{
		return _x;
	}
	
	@Override
	public int getNodeY()
	{
		return _y;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + _x;
		result = (prime * result) + _y;
		
		int nswe = 0;
		if (canGoNorth())
		{
			nswe |= Cell.NSWE_NORTH;
		}
		if (canGoEast())
		{
			nswe |= Cell.NSWE_EAST;
		}
		if (canGoSouth())
		{
			nswe |= Cell.NSWE_SOUTH;
		}
		if (canGoWest())
		{
			nswe |= Cell.NSWE_WEST;
		}
		
		result = (prime * result) + (((_geoHeight & 0xFFFF) << 1) | nswe);
		return result;
		// return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof NodeLoc))
		{
			return false;
		}
		final NodeLoc other = (NodeLoc) obj;
		if (_x != other._x)
		{
			return false;
		}
		if (_y != other._y)
		{
			return false;
		}
		if (_goNorth != other._goNorth)
		{
			return false;
		}
		if (_goEast != other._goEast)
		{
			return false;
		}
		if (_goSouth != other._goSouth)
		{
			return false;
		}
		if (_goWest != other._goWest)
		{
			return false;
		}
		if (_geoHeight != other._geoHeight)
		{
			return false;
		}
		return true;
	}
}
