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
package com.l2jmobius.gameserver;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.DoorTable;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.util.GeoUtils;
import com.l2jmobius.gameserver.util.LinePointIterator;
import com.l2jmobius.gameserver.util.LinePointIterator3D;
import com.l2jserver.gameserver.geoengine.Direction;
import com.l2jserver.gameserver.geoengine.NullDriver;
import com.l2jserver.gameserver.geoengine.abstraction.IGeoDriver;

/**
 * @author -Nemesiss-, FBIagent
 */
public class GeoData implements IGeoDriver
{
	private static Logger LOGGER = Logger.getLogger(GeoData.class.getName());
	private static final int ELEVATED_SEE_OVER_DISTANCE = 2;
	private static final int MAX_SEE_OVER_HEIGHT = 48;
	
	private final IGeoDriver _driver;
	private static GeoData _instance;
	
	public static GeoData getInstance()
	{
		if (_instance == null)
		{
			_instance = new GeoData();
		}
		return _instance;
	}
	
	protected GeoData()
	{
		if (Config.GEODATA > 0)
		{
			IGeoDriver driver = null;
			try
			{
				final Class<?> cls = Class.forName(Config.GEODATA_DRIVER);
				if (!IGeoDriver.class.isAssignableFrom(cls))
				{
					throw new ClassCastException("Geodata driver class needs to implement IGeoDriver!");
				}
				
				final Constructor<?> ctor = cls.getConstructor(Properties.class);
				final Properties props = new Properties();
				try (FileInputStream fis = new FileInputStream(Paths.get("config", "GeoDriver.ini").toString()))
				{
					props.load(fis);
				}
				driver = (IGeoDriver) ctor.newInstance(props);
			}
			catch (final Exception ex)
			{
				LOGGER.log(Level.SEVERE, "Failed to load geodata driver!", ex);
				System.exit(1);
			}
			// we do it this way so it's predictable for the compiler
			_driver = driver;
		}
		else
		{
			_driver = new NullDriver(null);
		}
	}
	
	public boolean isNullDriver()
	{
		return _driver instanceof NullDriver;
	}
	
	@Override
	public int getGeoX(int worldX)
	{
		return _driver.getGeoX(worldX);
	}
	
	@Override
	public int getGeoY(int worldY)
	{
		return _driver.getGeoY(worldY);
	}
	
	@Override
	public int getWorldX(int geoX)
	{
		return _driver.getWorldX(geoX);
	}
	
	@Override
	public int getWorldY(int geoY)
	{
		return _driver.getWorldY(geoY);
	}
	
	@Override
	public boolean hasGeoPos(int geoX, int geoY)
	{
		return _driver.hasGeoPos(geoX, geoY);
	}
	
	@Override
	public int getNearestZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNearestZ(geoX, geoY, worldZ);
	}
	
	@Override
	public int getNextLowerZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNextLowerZ(geoX, geoY, worldZ);
	}
	
	@Override
	public int getNextHigherZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNextHigherZ(geoX, geoY, worldZ);
	}
	
	@Override
	public boolean canEnterNeighbors(int geoX, int geoY, int worldZ, Direction first, Direction... more)
	{
		return _driver.canEnterNeighbors(geoX, geoY, worldZ, first, more);
	}
	
	@Override
	public boolean canEnterAllNeighbors(int geoX, int geoY, int worldZ)
	{
		return _driver.canEnterAllNeighbors(geoX, geoY, worldZ);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return Nearles Z
	 */
	public int getHeight(int x, int y, int z)
	{
		return getNearestZ(getGeoX(x), getGeoY(y), z);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param zmin
	 * @param zmax
	 * @return
	 */
	public int getSpawnHeight(int x, int y, int zmin, int zmax)
	{
		// + 30, defend against defective geodata and invalid spawn z :(
		return getNextLowerZ(getGeoX(x), getGeoY(y), zmax + 30);
	}
	
	private int getLosGeoZ(int prevX, int prevY, int prevGeoZ, int curX, int curY, Direction dir)
	{
		boolean can = true;
		
		switch (dir)
		{
			case NORTH_EAST:
				can = canEnterNeighbors(prevX, prevY - 1, prevGeoZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevGeoZ, Direction.NORTH);
				break;
			case NORTH_WEST:
				can = canEnterNeighbors(prevX, prevY - 1, prevGeoZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevGeoZ, Direction.NORTH);
				break;
			case SOUTH_EAST:
				can = canEnterNeighbors(prevX, prevY + 1, prevGeoZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevGeoZ, Direction.SOUTH);
				break;
			case SOUTH_WEST:
				can = canEnterNeighbors(prevX, prevY + 1, prevGeoZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevGeoZ, Direction.SOUTH);
				break;
		}
		
		if (can && canEnterNeighbors(prevX, prevY, prevGeoZ, dir))
		{
			return getNearestZ(curX, curY, prevGeoZ);
		}
		
		return getNextHigherZ(curX, curY, prevGeoZ);
	}
	
	/**
	 * Can see target. Doors as target always return true. Checks doors between.
	 * @param cha
	 * @param target
	 * @return True if cha can see target (LOS)
	 */
	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		if (target instanceof L2DoorInstance)
		{
			// can always see doors :o
			return true;
		}
		
		if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()))
		{
			return false;
		}
		
		return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ());
	}
	
	/**
	 * Can see target. Does not check doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @return {@code true} if there is line of sight between the given coordinate sets, {@code false} otherwise
	 */
	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		int tGeoX = getGeoX(tx);
		int tGeoY = getGeoY(ty);
		
		z = getNearestZ(geoX, geoY, z);
		tz = getNearestZ(tGeoX, tGeoY, tz);
		
		if ((geoX == tGeoX) && (geoY == tGeoY))
		{
			if (hasGeoPos(tGeoX, tGeoY))
			{
				return z == tz;
			}
			return true;
		}
		
		if (tz > z)
		{
			int tmp = tx;
			tx = x;
			x = tmp;
			
			tmp = ty;
			ty = y;
			y = tmp;
			
			tmp = tz;
			tz = z;
			z = tmp;
			
			tmp = tGeoX;
			tGeoX = geoX;
			geoX = tmp;
			
			tmp = tGeoY;
			tGeoY = geoY;
			geoY = tmp;
		}
		
		final LinePointIterator3D pointIter = new LinePointIterator3D(geoX, geoY, z, tGeoX, tGeoY, tz);
		// first point is guaranteed to be available, skip it, we can always see our own position
		pointIter.next();
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		final int prevZ = pointIter.z();
		int prevGeoZ = prevZ;
		int ptIndex = 0;
		
		while (pointIter.next())
		{
			final int curX = pointIter.x();
			final int curY = pointIter.y();
			
			if ((curX == prevX) && (curY == prevY))
			{
				continue;
			}
			
			final int beeCurZ = pointIter.z();
			int curGeoZ = prevGeoZ;
			
			// the current position has geodata
			if (hasGeoPos(curX, curY))
			{
				final int beeCurGeoZ = getNearestZ(curX, curY, beeCurZ);
				final Direction dir = GeoUtils.computeDirection(prevX, prevY, curX, curY);
				curGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, curX, curY, dir);
				
				int maxHeight;
				if (ptIndex < ELEVATED_SEE_OVER_DISTANCE)
				{
					maxHeight = z + MAX_SEE_OVER_HEIGHT;
				}
				else
				{
					maxHeight = beeCurZ + MAX_SEE_OVER_HEIGHT;
				}
				
				boolean canSeeThrough = false;
				if ((curGeoZ <= maxHeight) && (curGeoZ <= beeCurGeoZ))
				{
					switch (dir)
					{
						case NORTH_EAST:
						{
							final int northGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY - 1, Direction.EAST);
							final int eastGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX + 1, prevY, Direction.NORTH);
							canSeeThrough = (northGeoZ <= maxHeight) && (eastGeoZ <= maxHeight) && (northGeoZ <= getNearestZ(prevX, prevY - 1, beeCurZ)) && (eastGeoZ <= getNearestZ(prevX + 1, prevY, beeCurZ));
							break;
						}
						case NORTH_WEST:
						{
							final int northGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY - 1, Direction.WEST);
							final int westGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX - 1, prevY, Direction.NORTH);
							canSeeThrough = (northGeoZ <= maxHeight) && (westGeoZ <= maxHeight) && (northGeoZ <= getNearestZ(prevX, prevY - 1, beeCurZ)) && (westGeoZ <= getNearestZ(prevX - 1, prevY, beeCurZ));
							break;
						}
						case SOUTH_EAST:
						{
							final int southGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY + 1, Direction.EAST);
							final int eastGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX + 1, prevY, Direction.SOUTH);
							canSeeThrough = (southGeoZ <= maxHeight) && (eastGeoZ <= maxHeight) && (southGeoZ <= getNearestZ(prevX, prevY + 1, beeCurZ)) && (eastGeoZ <= getNearestZ(prevX + 1, prevY, beeCurZ));
							break;
						}
						case SOUTH_WEST:
						{
							final int southGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY + 1, Direction.WEST);
							final int westGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX - 1, prevY, Direction.SOUTH);
							canSeeThrough = (southGeoZ <= maxHeight) && (westGeoZ <= maxHeight) && (southGeoZ <= getNearestZ(prevX, prevY + 1, beeCurZ)) && (westGeoZ <= getNearestZ(prevX - 1, prevY, beeCurZ));
							break;
						}
						default:
						{
							canSeeThrough = true;
							break;
						}
					}
				}
				
				if (!canSeeThrough)
				{
					return false;
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevGeoZ = curGeoZ;
			++ptIndex;
		}
		return true;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param tx
	 * @param ty
	 * @param tz
	 * @return Last Location (x,y,z) where player can walk - just before wall
	 */
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
	{
		final int geoX = getGeoX(x);
		final int geoY = getGeoY(y);
		z = getNearestZ(geoX, geoY, z);
		final int tGeoX = getGeoX(tx);
		final int tGeoY = getGeoY(ty);
		tz = getNearestZ(tGeoX, tGeoY, tz);
		
		if (DoorTable.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz))
		{
			return new Location(x, y, getHeight(x, y, z));
		}
		
		final LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available
		pointIter.next();
		
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		int prevZ = z;
		
		while (pointIter.next())
		{
			final int curX = pointIter.x();
			final int curY = pointIter.y();
			final int curZ = getNearestZ(curX, curY, prevZ);
			
			if (hasGeoPos(prevX, prevY))
			{
				final Direction dir = GeoUtils.computeDirection(prevX, prevY, curX, curY);
				boolean canEnter = false;
				if (canEnterNeighbors(prevX, prevY, prevZ, dir))
				{
					// check diagonal movement
					switch (dir)
					{
						case NORTH_EAST:
							canEnter = canEnterNeighbors(prevX, prevY - 1, prevZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevZ, Direction.NORTH);
							break;
						case NORTH_WEST:
							canEnter = canEnterNeighbors(prevX, prevY - 1, prevZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevZ, Direction.NORTH);
							break;
						case SOUTH_EAST:
							canEnter = canEnterNeighbors(prevX, prevY + 1, prevZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevZ, Direction.SOUTH);
							break;
						case SOUTH_WEST:
							canEnter = canEnterNeighbors(prevX, prevY + 1, prevZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevZ, Direction.SOUTH);
							break;
						default:
							canEnter = true;
							break;
					}
				}
				
				if (!canEnter)
				{
					// can't move, return previous location
					return new Location(getWorldX(prevX), getWorldY(prevY), prevZ);
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevZ = curZ;
		}
		
		if (hasGeoPos(prevX, prevY) && (prevZ != tz))
		{
			// different floors, return start location
			return new Location(x, y, z);
		}
		return new Location(tx, ty, tz);
	}
	
	public int traceTerrainZ(int x, int y, int z, int tx, int ty)
	{
		final int geoX = getGeoX(x);
		final int geoY = getGeoY(y);
		z = getNearestZ(geoX, geoY, z);
		final int tGeoX = getGeoX(tx);
		final int tGeoY = getGeoY(ty);
		
		final LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available
		pointIter.next();
		int prevZ = z;
		
		while (pointIter.next())
		{
			final int curX = pointIter.x();
			final int curY = pointIter.y();
			final int curZ = getNearestZ(curX, curY, prevZ);
			
			prevZ = curZ;
		}
		
		return prevZ;
	}
	
	/**
	 * Checks if its possible to move from one location to another.
	 * @param fromX the X coordinate to start checking from
	 * @param fromY the Y coordinate to start checking from
	 * @param fromZ the Z coordinate to start checking from
	 * @param toX the X coordinate to end checking at
	 * @param toY the Y coordinate to end checking at
	 * @param toZ the Z coordinate to end checking at
	 * @return {@code true} if the character at start coordinates can move to end coordinates, {@code false} otherwise
	 */
	public boolean canMove(int fromX, int fromY, int fromZ, int toX, int toY, int toZ)
	{
		final int geoX = getGeoX(fromX);
		final int geoY = getGeoY(fromY);
		fromZ = getNearestZ(geoX, geoY, fromZ);
		final int tGeoX = getGeoX(toX);
		final int tGeoY = getGeoY(toY);
		toZ = getNearestZ(tGeoX, tGeoY, toZ);
		
		if (DoorTable.getInstance().checkIfDoorsBetween(fromX, fromY, fromZ, toX, toY, toZ))
		{
			return false;
		}
		
		final LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		
		// first point is guaranteed to be available
		pointIter.next();
		
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		int prevZ = fromZ;
		
		while (pointIter.next())
		{
			final int curX = pointIter.x();
			final int curY = pointIter.y();
			final int curZ = getNearestZ(curX, curY, prevZ);
			
			if (hasGeoPos(prevX, prevY))
			{
				final Direction dir = GeoUtils.computeDirection(prevX, prevY, curX, curY);
				boolean canEnter = false;
				if (canEnterNeighbors(prevX, prevY, prevZ, dir))
				{
					// check diagonal movement
					switch (dir)
					{
						case NORTH_EAST:
							canEnter = canEnterNeighbors(prevX, prevY - 1, prevZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevZ, Direction.NORTH);
							break;
						case NORTH_WEST:
							canEnter = canEnterNeighbors(prevX, prevY - 1, prevZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevZ, Direction.NORTH);
							break;
						case SOUTH_EAST:
							canEnter = canEnterNeighbors(prevX, prevY + 1, prevZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevZ, Direction.SOUTH);
							break;
						case SOUTH_WEST:
							canEnter = canEnterNeighbors(prevX, prevY + 1, prevZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevZ, Direction.SOUTH);
							break;
						default:
							canEnter = true;
							break;
					}
				}
				
				if (!canEnter)
				{
					return false;
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevZ = curZ;
		}
		
		if (hasGeoPos(prevX, prevY) && (prevZ != toZ))
		{
			// different floors
			return false;
		}
		return true;
	}
	
	public boolean hasGeo(int x, int y)
	{
		return hasGeoPos(getGeoX(x), getGeoY(y));
	}
}