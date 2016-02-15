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
package com.l2jmobius.gameserver.model.holders;

/**
 * @author Mobius
 */
public class WallHolder
{
	private final int _point1X;
	private final int _point1Y;
	private final int _point2X;
	private final int _point2Y;
	private final int _zMin;
	private final int _zMax;
	
	public WallHolder(int point1X, int point1Y, int point2X, int point2Y, int zMin, int zMax)
	{
		_point1X = point1X;
		_point1Y = point1Y;
		_point2X = point2X;
		_point2Y = point2Y;
		_zMin = zMin;
		_zMax = zMax;
	}
	
	public int getPoint1X()
	{
		return _point1X;
	}
	
	public int getPoint1Y()
	{
		return _point1Y;
	}
	
	public int getPoint2X()
	{
		return _point2X;
	}
	
	public int getPoint2Y()
	{
		return _point2Y;
	}
	
	public int getZMin()
	{
		return _zMin;
	}
	
	public int getZMax()
	{
		return _zMax;
	}
}
