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
package org.l2jmobius.gameserver.data.xml;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.FenceState;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldRegion;
import org.l2jmobius.gameserver.model.actor.instance.Fence;

/**
 * @author HoridoJoho / FBIagent
 */
public class FenceData
{
	private static final Logger LOGGER = Logger.getLogger(FenceData.class.getSimpleName());
	
	private static final int MAX_Z_DIFF = 100;
	
	private final Map<Integer, Fence> _fences = new ConcurrentHashMap<>();
	
	protected FenceData()
	{
		load();
	}
	
	public void load()
	{
		final File xml = new File(Config.DATAPACK_ROOT, "data/FenceData.xml");
		if (!xml.exists())
		{
			LOGGER.warning(getClass().getSimpleName() + ": FenceData.xml not found!");
			return;
		}
		
		Document doc = null;
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		try
		{
			doc = factory.newDocumentBuilder().parse(xml);
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not parse FenceData.xml: " + e.getMessage());
			return;
		}
		
		if (!_fences.isEmpty())
		{
			// Remove old fences when reloading
			_fences.values().forEach(this::removeFence);
		}
		
		final Node table = doc.getFirstChild();
		for (Node fence = table.getFirstChild(); fence != null; fence = fence.getNextSibling())
		{
			if (fence.getNodeName().equals("fence"))
			{
				spawnFence(fence);
			}
		}
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _fences.size() + " Fences.");
	}
	
	public int getLoadedElementsCount()
	{
		return _fences.size();
	}
	
	private void spawnFence(Node fenceNode)
	{
		final NamedNodeMap attrs = fenceNode.getAttributes();
		final String name = attrs.getNamedItem("name").getNodeValue();
		final int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
		final int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
		final int z = Integer.parseInt(attrs.getNamedItem("z").getNodeValue());
		final int width = Integer.parseInt(attrs.getNamedItem("width").getNodeValue());
		final int length = Integer.parseInt(attrs.getNamedItem("length").getNodeValue());
		final int height = Integer.parseInt(attrs.getNamedItem("height").getNodeValue());
		final FenceState state = FenceState.valueOf(attrs.getNamedItem("state").getNodeValue());
		spawnFence(x, y, z, name, width, length, height, state);
	}
	
	public Fence spawnFence(int x, int y, int z, String name, int width, int length, int height, FenceState state)
	{
		final Fence fence = new Fence(x, y, name, width, length, height, state);
		fence.spawnMe(x, y, z);
		addFence(fence);
		
		return fence;
	}
	
	private void addFence(Fence fence)
	{
		_fences.put(fence.getObjectId(), fence);
	}
	
	public void removeFence(Fence fence)
	{
		_fences.remove(fence.getObjectId());
	}
	
	public Map<Integer, Fence> getFences()
	{
		return _fences;
	}
	
	public Fence getFence(int objectId)
	{
		return _fences.get(objectId);
	}
	
	public boolean checkIfFenceBetween(int x, int y, int z, int tx, int ty, int tz, int instanceId)
	{
		final WorldRegion region = World.getInstance().getRegion(x, y);
		final List<Fence> fences = region != null ? region.getFences() : null;
		if ((fences == null) || fences.isEmpty())
		{
			return false;
		}
		
		for (Fence fence : fences)
		{
			// Check if fence is geodata enabled.
			if (!fence.getState().isGeodataEnabled() || (fence.getInstanceId() != instanceId))
			{
				continue;
			}
			
			final int xMin = fence.getXMin();
			final int xMax = fence.getXMax();
			final int yMin = fence.getYMin();
			final int yMax = fence.getYMax();
			if ((x < xMin) && (tx < xMin))
			{
				continue;
			}
			if ((x > xMax) && (tx > xMax))
			{
				continue;
			}
			if ((y < yMin) && (ty < yMin))
			{
				continue;
			}
			if ((y > yMax) && (ty > yMax))
			{
				continue;
			}
			if ((x > xMin) && (tx > xMin) && (x < xMax) && (tx < xMax) && (y > yMin) && (ty > yMin) && (y < yMax) && (ty < yMax))
			{
				continue;
			}
			if ((crossLinePart(xMin, yMin, xMax, yMin, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMax, yMin, xMax, yMax, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMax, yMax, xMin, yMax, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMin, yMax, xMin, yMin, x, y, tx, ty, xMin, yMin, xMax, yMax)) && (z > (fence.getZ() - MAX_Z_DIFF)) && (z < (fence.getZ() + MAX_Z_DIFF)))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean crossLinePart(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double xMin, double yMin, double xMax, double yMax)
	{
		final double[] result = intersection(x1, y1, x2, y2, x3, y3, x4, y4);
		if (result == null)
		{
			return false;
		}
		
		final double xCross = result[0];
		final double yCross = result[1];
		if ((xCross <= xMax) && (xCross >= xMin))
		{
			return true;
		}
		if ((yCross <= yMax) && (yCross >= yMin))
		{
			return true;
		}
		
		return false;
	}
	
	private double[] intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		final double d = ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
		if (d == 0)
		{
			return null;
		}
		
		final double xi = (((x3 - x4) * ((x1 * y2) - (y1 * x2))) - ((x1 - x2) * ((x3 * y4) - (y3 * x4)))) / d;
		final double yi = (((y3 - y4) * ((x1 * y2) - (y1 * x2))) - ((y1 - y2) * ((x3 * y4) - (y3 * x4)))) / d;
		return new double[]
		{
			xi,
			yi
		};
	}
	
	public static FenceData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FenceData INSTANCE = new FenceData();
	}
}