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
package org.l2jmobius.gameserver.geoengine;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.data.xml.FenceData;
import org.l2jmobius.gameserver.geoengine.geodata.Cell;
import org.l2jmobius.gameserver.geoengine.geodata.IRegion;
import org.l2jmobius.gameserver.geoengine.geodata.NullRegion;
import org.l2jmobius.gameserver.geoengine.geodata.Region;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.util.GeoUtils;
import org.l2jmobius.gameserver.util.LinePointIterator;
import org.l2jmobius.gameserver.util.LinePointIterator3D;

/**
 * @author -Nemesiss-, HorridoJoho
 */
public class GeoEngine
{
	private static final Logger LOGGER = Logger.getLogger(GeoEngine.class.getName());
	
	private static final int WORLD_MIN_X = -655360;
	private static final int WORLD_MIN_Y = -589824;
	private static final int WORLD_MIN_Z = -16384;
	
	/** Regions in the world on the x axis */
	public static final int GEO_REGIONS_X = 32;
	/** Regions in the world on the y axis */
	public static final int GEO_REGIONS_Y = 32;
	/** Region in the world */
	public static final int GEO_REGIONS = GEO_REGIONS_X * GEO_REGIONS_Y;
	
	/** Blocks in the world on the x axis */
	public static final int GEO_BLOCKS_X = GEO_REGIONS_X * IRegion.REGION_BLOCKS_X;
	/** Blocks in the world on the y axis */
	public static final int GEO_BLOCKS_Y = GEO_REGIONS_Y * IRegion.REGION_BLOCKS_Y;
	/** Blocks in the world */
	public static final int GEO_BLOCKS = GEO_REGIONS * IRegion.REGION_BLOCKS;
	
	/** The regions array */
	private final AtomicReferenceArray<IRegion> _regions = new AtomicReferenceArray<>(GEO_REGIONS);
	
	private static final String FILE_NAME_FORMAT = "%d_%d.l2j";
	private static final int ELEVATED_SEE_OVER_DISTANCE = 2;
	private static final int MAX_SEE_OVER_HEIGHT = 48;
	
	protected GeoEngine()
	{
		LOGGER.info("GeoEngine: Initializing...");
		for (int i = 0; i < _regions.length(); i++)
		{
			_regions.set(i, NullRegion.INSTANCE);
		}
		
		int loaded = 0;
		try
		{
			for (int regionX = World.TILE_X_MIN; regionX <= World.TILE_X_MAX; regionX++)
			{
				for (int regionY = World.TILE_Y_MIN; regionY <= World.TILE_Y_MAX; regionY++)
				{
					final Path geoFilePath = Config.GEODATA_PATH.resolve(String.format(FILE_NAME_FORMAT, regionX, regionY));
					if (Files.exists(geoFilePath))
					{
						try (RandomAccessFile raf = new RandomAccessFile(geoFilePath.toFile(), "r"))
						{
							_regions.set((regionX * GEO_REGIONS_Y) + regionY, new Region(raf.getChannel().map(MapMode.READ_ONLY, 0, raf.length()).load().order(ByteOrder.LITTLE_ENDIAN)));
							loaded++;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "GeoEngine: Failed to load geodata!", e);
			System.exit(1);
		}
		
		LOGGER.info("GeoEngine: Loaded " + loaded + " geodata files.");
		
		// Avoid wrong configs when no files are loaded.
		if (loaded == 0)
		{
			if (Config.PATHFINDING)
			{
				Config.PATHFINDING = false;
				LOGGER.info("GeoEngine: Forcing PathFinding setting to false.");
			}
			if (Config.COORD_SYNCHRONIZE == 2)
			{
				Config.COORD_SYNCHRONIZE = -1;
				LOGGER.info("GeoEngine: Forcing CoordSynchronize setting to -1.");
			}
		}
	}
	
	/**
	 * @param geoX
	 * @param geoY
	 * @return the region
	 */
	private IRegion getRegion(int geoX, int geoY)
	{
		final int region = ((geoX / IRegion.REGION_CELLS_X) * GEO_REGIONS_Y) + (geoY / IRegion.REGION_CELLS_Y);
		if ((region < 0) || (region >= _regions.length()))
		{
			return null;
		}
		return _regions.get(region);
	}
	
	/**
	 * @param filePath
	 * @param regionX
	 * @param regionY
	 * @throws IOException
	 */
	public void loadRegion(Path filePath, int regionX, int regionY) throws IOException
	{
		final int regionOffset = (regionX * GEO_REGIONS_Y) + regionY;
		try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r"))
		{
			_regions.set(regionOffset, new Region(raf.getChannel().map(MapMode.READ_ONLY, 0, raf.length()).load().order(ByteOrder.LITTLE_ENDIAN)));
		}
	}
	
	/**
	 * @param regionX
	 * @param regionY
	 */
	public void unloadRegion(int regionX, int regionY)
	{
		_regions.set((regionX * GEO_REGIONS_Y) + regionY, NullRegion.INSTANCE);
	}
	
	/**
	 * @param geoX
	 * @param geoY
	 * @return if geodata exist
	 */
	public boolean hasGeoPos(int geoX, int geoY)
	{
		final IRegion region = getRegion(geoX, geoY);
		if (region == null)
		{
			return false;
		}
		return region.hasGeo();
	}
	
	/**
	 * Checks the specified position for available geodata.
	 * @param x the world x
	 * @param y the world y
	 * @return {@code true} if there is geodata for the given coordinates, {@code false} otherwise
	 */
	public boolean hasGeo(int x, int y)
	{
		return hasGeoPos(getGeoX(x), getGeoY(y));
	}
	
	/**
	 * @param geoX
	 * @param geoY
	 * @param worldZ
	 * @param nswe
	 * @return the nearest nswe check
	 */
	public boolean checkNearestNswe(int geoX, int geoY, int worldZ, int nswe)
	{
		final IRegion region = getRegion(geoX, geoY);
		if (region == null)
		{
			return true;
		}
		return region.checkNearestNswe(geoX, geoY, worldZ, nswe);
	}
	
	/**
	 * @param geoX
	 * @param geoY
	 * @param worldZ
	 * @param nswe
	 * @return the nearest nswe anti-corner cut
	 */
	public boolean checkNearestNsweAntiCornerCut(int geoX, int geoY, int worldZ, int nswe)
	{
		boolean can = true;
		if ((nswe & Cell.NSWE_NORTH_EAST) == Cell.NSWE_NORTH_EAST)
		{
			can = checkNearestNswe(geoX, geoY - 1, worldZ, Cell.NSWE_EAST) && checkNearestNswe(geoX + 1, geoY, worldZ, Cell.NSWE_NORTH);
		}
		if (can && ((nswe & Cell.NSWE_NORTH_WEST) == Cell.NSWE_NORTH_WEST))
		{
			can = checkNearestNswe(geoX, geoY - 1, worldZ, Cell.NSWE_WEST) && checkNearestNswe(geoX, geoY - 1, worldZ, Cell.NSWE_NORTH);
		}
		if (can && ((nswe & Cell.NSWE_SOUTH_EAST) == Cell.NSWE_SOUTH_EAST))
		{
			can = checkNearestNswe(geoX, geoY + 1, worldZ, Cell.NSWE_EAST) && checkNearestNswe(geoX + 1, geoY, worldZ, Cell.NSWE_SOUTH);
		}
		if (can && ((nswe & Cell.NSWE_SOUTH_WEST) == Cell.NSWE_SOUTH_WEST))
		{
			can = checkNearestNswe(geoX, geoY + 1, worldZ, Cell.NSWE_WEST) && checkNearestNswe(geoX - 1, geoY, worldZ, Cell.NSWE_SOUTH);
		}
		return can && checkNearestNswe(geoX, geoY, worldZ, nswe);
	}
	
	/**
	 * @param geoX
	 * @param geoY
	 * @param worldZ
	 * @return the nearest Z value
	 */
	public int getNearestZ(int geoX, int geoY, int worldZ)
	{
		final IRegion region = getRegion(geoX, geoY);
		if (region == null)
		{
			return worldZ;
		}
		return region.getNearestZ(geoX, geoY, worldZ);
	}
	
	/**
	 * @param geoX
	 * @param geoY
	 * @param worldZ
	 * @return the next lower Z value
	 */
	public int getNextLowerZ(int geoX, int geoY, int worldZ)
	{
		final IRegion region = getRegion(geoX, geoY);
		if (region == null)
		{
			return worldZ;
		}
		return region.getNextLowerZ(geoX, geoY, worldZ);
	}
	
	/**
	 * @param geoX
	 * @param geoY
	 * @param worldZ
	 * @return the next higher Z value
	 */
	public int getNextHigherZ(int geoX, int geoY, int worldZ)
	{
		final IRegion region = getRegion(geoX, geoY);
		if (region == null)
		{
			return worldZ;
		}
		return region.getNextHigherZ(geoX, geoY, worldZ);
	}
	
	/**
	 * Gets the Z height.
	 * @param x the world x
	 * @param y the world y
	 * @param z the world z
	 * @return the nearest Z height
	 */
	public int getHeight(int x, int y, int z)
	{
		return getNearestZ(getGeoX(x), getGeoY(y), z);
	}
	
	/**
	 * Gets the next lower Z height.
	 * @param x the world x
	 * @param y the world y
	 * @param z the world z
	 * @return the nearest Z height
	 */
	public int getLowerHeight(int x, int y, int z)
	{
		return getNextLowerZ(getGeoX(x), getGeoY(y), z);
	}
	
	/**
	 * Gets the next higher Z height.
	 * @param x the world x
	 * @param y the world y
	 * @param z the world z
	 * @return the nearest Z height
	 */
	public int getHigherHeight(int x, int y, int z)
	{
		return getNextHigherZ(getGeoX(x), getGeoY(y), z);
	}
	
	/**
	 * @param worldX
	 * @return the geo X
	 */
	public int getGeoX(int worldX)
	{
		return (worldX - WORLD_MIN_X) / 16;
	}
	
	/**
	 * @param worldY
	 * @return the geo Y
	 */
	public int getGeoY(int worldY)
	{
		return (worldY - WORLD_MIN_Y) / 16;
	}
	
	/**
	 * @param worldZ
	 * @return the geo Z
	 */
	public int getGeoZ(int worldZ)
	{
		return (worldZ - WORLD_MIN_Z) / 16;
	}
	
	/**
	 * @param geoX
	 * @return the world X
	 */
	public int getWorldX(int geoX)
	{
		return (geoX * 16) + WORLD_MIN_X + 8;
	}
	
	/**
	 * @param geoY
	 * @return the world Y
	 */
	public int getWorldY(int geoY)
	{
		return (geoY * 16) + WORLD_MIN_Y + 8;
	}
	
	/**
	 * @param geoZ
	 * @return the world Z
	 */
	public int getWorldZ(int geoZ)
	{
		return (geoZ * 16) + WORLD_MIN_Z + 8;
	}
	
	/**
	 * Can see target. Doors as target always return true. Checks doors between.
	 * @param cha the character
	 * @param target the target
	 * @return {@code true} if the character can see the target (LOS), {@code false} otherwise
	 */
	public boolean canSeeTarget(WorldObject cha, WorldObject target)
	{
		if (target.isDoor())
		{
			// Can always see doors.
			return true;
		}
		return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), cha.getInstanceId(), target.getX(), target.getY(), target.getZ(), target.getInstanceId());
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param cha the character
	 * @param worldPosition the world position
	 * @return {@code true} if the character can see the target at the given world position, {@code false} otherwise
	 */
	public boolean canSeeTarget(WorldObject cha, Location worldPosition)
	{
		return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), cha.getInstanceId(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param instanceId
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @param tInstanceId the target's instanceId
	 * @return
	 */
	public boolean canSeeTarget(int x, int y, int z, int instanceId, int tx, int ty, int tz, int tInstanceId)
	{
		return (instanceId != tInstanceId) ? false : canSeeTarget(x, y, z, instanceId, tx, ty, tz);
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param instanceId
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @return {@code true} if there is line of sight between the given coordinate sets, {@code false} otherwise
	 */
	public boolean canSeeTarget(int x, int y, int z, int instanceId, int tx, int ty, int tz)
	{
		if (DoorData.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz))
		{
			return false;
		}
		return canSeeTarget(x, y, z, tx, ty, tz);
	}
	
	/**
	 * @param prevX
	 * @param prevY
	 * @param prevGeoZ
	 * @param curX
	 * @param curY
	 * @param nswe
	 * @return the LOS Z value
	 */
	private int getLosGeoZ(int prevX, int prevY, int prevGeoZ, int curX, int curY, int nswe)
	{
		if ((((nswe & Cell.NSWE_NORTH) != 0) && ((nswe & Cell.NSWE_SOUTH) != 0)) || (((nswe & Cell.NSWE_WEST) != 0) && ((nswe & Cell.NSWE_EAST) != 0)))
		{
			throw new RuntimeException("Multiple directions!");
		}
		
		if (checkNearestNsweAntiCornerCut(prevX, prevY, prevGeoZ, nswe))
		{
			return getNearestZ(curX, curY, prevGeoZ);
		}
		
		return getNextHigherZ(curX, curY, prevGeoZ);
	}
	
	/**
	 * Can see target. Does not check doors between.
	 * @param xValue the x coordinate
	 * @param yValue the y coordinate
	 * @param zValue the z coordinate
	 * @param txValue the target's x coordinate
	 * @param tyValue the target's y coordinate
	 * @param tzValue the target's z coordinate
	 * @return {@code true} if there is line of sight between the given coordinate sets, {@code false} otherwise
	 */
	public boolean canSeeTarget(int xValue, int yValue, int zValue, int txValue, int tyValue, int tzValue)
	{
		int x = xValue;
		int y = yValue;
		int tx = txValue;
		int ty = tyValue;
		
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		int tGeoX = getGeoX(tx);
		int tGeoY = getGeoY(ty);
		
		int z = getNearestZ(geoX, geoY, zValue);
		int tz = getNearestZ(tGeoX, tGeoY, tzValue);
		
		// Fastpath.
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
		// First point is guaranteed to be available, skip it, we can always see our own position.
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
			
			// Check if the position has geodata.
			if (hasGeoPos(curX, curY))
			{
				final int beeCurGeoZ = getNearestZ(curX, curY, beeCurZ);
				final int nswe = GeoUtils.computeNswe(prevX, prevY, curX, curY); // .computeDirection(prevX, prevY, curX, curY);
				curGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, curX, curY, nswe);
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
					if ((nswe & Cell.NSWE_NORTH_EAST) == Cell.NSWE_NORTH_EAST)
					{
						final int northGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY - 1, Cell.NSWE_EAST);
						final int eastGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX + 1, prevY, Cell.NSWE_NORTH);
						canSeeThrough = (northGeoZ <= maxHeight) && (eastGeoZ <= maxHeight) && (northGeoZ <= getNearestZ(prevX, prevY - 1, beeCurZ)) && (eastGeoZ <= getNearestZ(prevX + 1, prevY, beeCurZ));
					}
					else if ((nswe & Cell.NSWE_NORTH_WEST) == Cell.NSWE_NORTH_WEST)
					{
						final int northGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY - 1, Cell.NSWE_WEST);
						final int westGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX - 1, prevY, Cell.NSWE_NORTH);
						canSeeThrough = (northGeoZ <= maxHeight) && (westGeoZ <= maxHeight) && (northGeoZ <= getNearestZ(prevX, prevY - 1, beeCurZ)) && (westGeoZ <= getNearestZ(prevX - 1, prevY, beeCurZ));
					}
					else if ((nswe & Cell.NSWE_SOUTH_EAST) == Cell.NSWE_SOUTH_EAST)
					{
						final int southGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY + 1, Cell.NSWE_EAST);
						final int eastGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX + 1, prevY, Cell.NSWE_SOUTH);
						canSeeThrough = (southGeoZ <= maxHeight) && (eastGeoZ <= maxHeight) && (southGeoZ <= getNearestZ(prevX, prevY + 1, beeCurZ)) && (eastGeoZ <= getNearestZ(prevX + 1, prevY, beeCurZ));
					}
					else if ((nswe & Cell.NSWE_SOUTH_WEST) == Cell.NSWE_SOUTH_WEST)
					{
						final int southGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY + 1, Cell.NSWE_WEST);
						final int westGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX - 1, prevY, Cell.NSWE_SOUTH);
						canSeeThrough = (southGeoZ <= maxHeight) && (westGeoZ <= maxHeight) && (southGeoZ <= getNearestZ(prevX, prevY + 1, beeCurZ)) && (westGeoZ <= getNearestZ(prevX - 1, prevY, beeCurZ));
					}
					else
					{
						canSeeThrough = true;
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
	 * Move check.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param zValue the z coordinate
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tzValue the target's z coordinate
	 * @param instanceId the instance id
	 * @return the last Location (x,y,z) where player can walk - just before wall
	 */
	public Location canMoveToTargetLoc(int x, int y, int zValue, int tx, int ty, int tzValue, int instanceId)
	{
		final int geoX = getGeoX(x);
		final int geoY = getGeoY(y);
		final int z = getNearestZ(geoX, geoY, zValue);
		final int tGeoX = getGeoX(tx);
		final int tGeoY = getGeoY(ty);
		final int tz = getNearestZ(tGeoX, tGeoY, tzValue);
		
		if (DoorData.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz))
		{
			return new Location(x, y, getHeight(x, y, z));
		}
		if (FenceData.getInstance().checkIfFenceBetween(x, y, z, tx, ty, tz))
		{
			return new Location(x, y, getHeight(x, y, z));
		}
		
		final LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// First point is guaranteed to be available.
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
				final int nswe = GeoUtils.computeNswe(prevX, prevY, curX, curY);
				if (!checkNearestNsweAntiCornerCut(prevX, prevY, prevZ, nswe))
				{
					// Can't move, return previous location.
					return new Location(getWorldX(prevX), getWorldY(prevY), prevZ);
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevZ = curZ;
		}
		
		if (hasGeoPos(prevX, prevY) && (prevZ != tz))
		{
			// Different floors, return start location.
			return new Location(x, y, z);
		}
		
		return new Location(tx, ty, tz);
	}
	
	/**
	 * Checks if its possible to move from one location to another.
	 * @param fromX the X coordinate to start checking from
	 * @param fromY the Y coordinate to start checking from
	 * @param fromZvalue the Z coordinate to start checking from
	 * @param toX the X coordinate to end checking at
	 * @param toY the Y coordinate to end checking at
	 * @param toZvalue the Z coordinate to end checking at
	 * @param instanceId the instance
	 * @return {@code true} if the character at start coordinates can move to end coordinates, {@code false} otherwise
	 */
	public boolean canMoveToTarget(int fromX, int fromY, int fromZvalue, int toX, int toY, int toZvalue, int instanceId)
	{
		final int geoX = getGeoX(fromX);
		final int geoY = getGeoY(fromY);
		final int fromZ = getNearestZ(geoX, geoY, fromZvalue);
		final int tGeoX = getGeoX(toX);
		final int tGeoY = getGeoY(toY);
		final int toZ = getNearestZ(tGeoX, tGeoY, toZvalue);
		
		if (DoorData.getInstance().checkIfDoorsBetween(fromX, fromY, fromZ, toX, toY, toZ))
		{
			return false;
		}
		if (FenceData.getInstance().checkIfFenceBetween(fromX, fromY, fromZ, toX, toY, toZ))
		{
			return false;
		}
		
		final LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// First point is guaranteed to be available.
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
				final int nswe = GeoUtils.computeNswe(prevX, prevY, curX, curY);
				if (!checkNearestNsweAntiCornerCut(prevX, prevY, prevZ, nswe))
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
			// Different floors.
			return false;
		}
		
		return true;
	}
	
	public static GeoEngine getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GeoEngine INSTANCE = new GeoEngine();
	}
}
