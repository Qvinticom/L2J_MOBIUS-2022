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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.instancemanager.MapRegionManager;
import com.l2jmobius.gameserver.model.holders.WallHolder;
import com.l2jmobius.util.data.xml.IXmlReader;

/**
 * Loads Wall Data.
 * @author Mobius
 */
public class WallData implements IXmlReader
{
	private final Map<Integer, List<WallHolder>> _walls = new HashMap<>();
	
	protected WallData()
	{
		if (!Config.ENABLE_WALL_DATA)
		{
			return;
		}
		load();
	}
	
	@Override
	public void load()
	{
		_walls.clear();
		parseDatapackDirectory("walls", false);
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		int point1X;
		int point1Y;
		int point2X;
		int point2Y;
		int minZ;
		int maxZ;
		int region;
		int counter = 0;
		for (Node a = doc.getFirstChild(); a != null; a = a.getNextSibling())
		{
			if ("list".equalsIgnoreCase(a.getNodeName()))
			{
				for (Node b = a.getFirstChild(); b != null; b = b.getNextSibling())
				{
					if ("wall".equalsIgnoreCase(b.getNodeName()))
					{
						final NamedNodeMap attrs = b.getAttributes();
						point1X = parseInteger(attrs, "point1X");
						point1Y = parseInteger(attrs, "point1Y");
						point2X = parseInteger(attrs, "point2X");
						point2Y = parseInteger(attrs, "point2Y");
						minZ = parseInteger(attrs, "minZ");
						maxZ = parseInteger(attrs, "maxZ");
						region = MapRegionManager.getInstance().getMapRegionLocId(point1X, point1Y);
						if (!_walls.containsKey(region))
						{
							_walls.put(region, new ArrayList<WallHolder>());
						}
						_walls.get(region).add(new WallHolder(point1X, point1Y, point2X, point2Y, minZ, maxZ));
						counter++;
					}
				}
			}
		}
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + counter + " wall data.");
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param tx
	 * @param ty
	 * @param tz
	 * @return {@code boolean}
	 */
	public boolean checkIfWallsBetween(int x, int y, int z, int tx, int ty, int tz)
	{
		if (!Config.ENABLE_WALL_DATA)
		{
			return false;
		}
		
		final Collection<WallHolder> allWalls = _walls.get(MapRegionManager.getInstance().getMapRegionLocId(x, y));
		if (allWalls == null)
		{
			return false;
		}
		
		for (WallHolder wall : allWalls)
		{
			boolean intersectFace = false;
			for (int i = 0; i < 2; i++)
			{
				// lower part of the multiplier fraction, if it is 0 we avoid an error and also know that the lines are parallel
				final int denominator = ((ty - y) * (wall.getPoint1X() - wall.getPoint2X())) - ((tx - x) * (wall.getPoint1Y() - wall.getPoint2Y()));
				if (denominator == 0)
				{
					continue;
				}
				
				// multipliers to the equations of the lines. If they are lower than 0 or bigger than 1, we know that segments don't intersect
				final float multiplier1 = (float) (((wall.getPoint2X() - wall.getPoint1X()) * (y - wall.getPoint1Y())) - ((wall.getPoint2Y() - wall.getPoint1Y()) * (x - wall.getPoint1X()))) / denominator;
				final float multiplier2 = (float) (((tx - x) * (y - wall.getPoint1Y())) - ((ty - y) * (x - wall.getPoint1X()))) / denominator;
				if ((multiplier1 >= 0) && (multiplier1 <= 1) && (multiplier2 >= 0) && (multiplier2 <= 1))
				{
					final int intersectZ = Math.round(z + (multiplier1 * (tz - z)));
					// now checking if the resulting point is between door's min and max z
					if ((intersectZ > wall.getZMin()) && (intersectZ < wall.getZMax()))
					{
						if (intersectFace)
						{
							return true;
						}
						intersectFace = true;
					}
				}
			}
		}
		return false;
	}
	
	public List<WallHolder> getRegionWalls(int x, int y)
	{
		return _walls.get(MapRegionManager.getInstance().getMapRegionLocId(x, y));
	}
	
	public static WallData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final WallData _instance = new WallData();
	}
}
