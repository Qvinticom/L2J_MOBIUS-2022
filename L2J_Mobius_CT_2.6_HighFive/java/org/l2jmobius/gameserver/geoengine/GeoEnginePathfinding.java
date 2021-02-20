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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.geoengine.pathfinding.AbstractNode;
import org.l2jmobius.gameserver.geoengine.pathfinding.AbstractNodeLoc;
import org.l2jmobius.gameserver.geoengine.pathfinding.CellNode;
import org.l2jmobius.gameserver.geoengine.pathfinding.CellNodeBuffer;
import org.l2jmobius.gameserver.geoengine.pathfinding.NodeLoc;
import org.l2jmobius.gameserver.model.World;

/**
 * @author -Nemesiss-
 */
public class GeoEnginePathfinding
{
	private static final Logger LOGGER = Logger.getLogger(GeoEnginePathfinding.class.getName());
	
	private BufferInfo[] _buffers;
	
	protected GeoEnginePathfinding()
	{
		try
		{
			final String[] array = Config.PATHFIND_BUFFERS.split(";");
			
			_buffers = new BufferInfo[array.length];
			
			String buf;
			String[] args;
			for (int i = 0; i < array.length; i++)
			{
				buf = array[i];
				args = buf.split("x");
				if (args.length != 2)
				{
					throw new Exception("Invalid buffer definition: " + buf);
				}
				
				_buffers[i] = new BufferInfo(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "CellPathFinding: Problem during buffer init: " + e.getMessage(), e);
			throw new Error("CellPathFinding: load aborted");
		}
	}
	
	public boolean pathNodesExist(short regionoffset)
	{
		return false;
	}
	
	public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, int instanceId)
	{
		final int gx = GeoEngine.getInstance().getGeoX(x);
		final int gy = GeoEngine.getInstance().getGeoY(y);
		if (!GeoEngine.getInstance().hasGeo(x, y))
		{
			return null;
		}
		final int gz = GeoEngine.getInstance().getHeight(x, y, z);
		final int gtx = GeoEngine.getInstance().getGeoX(tx);
		final int gty = GeoEngine.getInstance().getGeoY(ty);
		if (!GeoEngine.getInstance().hasGeo(tx, ty))
		{
			return null;
		}
		final int gtz = GeoEngine.getInstance().getHeight(tx, ty, tz);
		final CellNodeBuffer buffer = alloc(64 + (2 * Math.max(Math.abs(gx - gtx), Math.abs(gy - gty))));
		if (buffer == null)
		{
			return null;
		}
		
		List<AbstractNodeLoc> path = null;
		try
		{
			final CellNode result = buffer.findPath(gx, gy, gz, gtx, gty, gtz);
			
			if (result == null)
			{
				return null;
			}
			
			path = constructPath(result);
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
			return null;
		}
		finally
		{
			buffer.free();
		}
		
		// check path
		if (path.size() < 3)
		{
			return path;
		}
		
		int currentX, currentY, currentZ;
		ListIterator<AbstractNodeLoc> middlePoint;
		
		middlePoint = path.listIterator();
		currentX = x;
		currentY = y;
		currentZ = z;
		
		while (middlePoint.hasNext())
		{
			final AbstractNodeLoc locMiddle = middlePoint.next();
			if (!middlePoint.hasNext())
			{
				break;
			}
			
			final AbstractNodeLoc locEnd = path.get(middlePoint.nextIndex());
			if (GeoEngine.getInstance().canMoveToTarget(currentX, currentY, currentZ, locEnd.getX(), locEnd.getY(), locEnd.getZ(), instanceId))
			{
				middlePoint.remove();
			}
			else
			{
				currentX = locMiddle.getX();
				currentY = locMiddle.getY();
				currentZ = locMiddle.getZ();
			}
		}
		
		return path;
	}
	
	/**
	 * Convert geodata position to pathnode position
	 * @param geo_pos
	 * @return pathnode position
	 */
	public short getNodePos(int geo_pos)
	{
		return (short) (geo_pos >> 3); // OK?
	}
	
	/**
	 * Convert node position to pathnode block position
	 * @param node_pos
	 * @return pathnode block position (0...255)
	 */
	public short getNodeBlock(int node_pos)
	{
		return (short) (node_pos % 256);
	}
	
	public byte getRegionX(int node_pos)
	{
		return (byte) ((node_pos >> 8) + World.TILE_X_MIN);
	}
	
	public byte getRegionY(int node_pos)
	{
		return (byte) ((node_pos >> 8) + World.TILE_Y_MIN);
	}
	
	public short getRegionOffset(byte rx, byte ry)
	{
		return (short) ((rx << 5) + ry);
	}
	
	/**
	 * Convert pathnode x to World x position
	 * @param node_x rx
	 * @return
	 */
	public int calculateWorldX(short node_x)
	{
		return World.MAP_MIN_X + (node_x * 128) + 48;
	}
	
	/**
	 * Convert pathnode y to World y position
	 * @param node_y
	 * @return
	 */
	public int calculateWorldY(short node_y)
	{
		return World.MAP_MIN_Y + (node_y * 128) + 48;
	}
	
	private List<AbstractNodeLoc> constructPath(AbstractNode<NodeLoc> nodeValue)
	{
		final LinkedList<AbstractNodeLoc> path = new LinkedList<>();
		int previousDirectionX = Integer.MIN_VALUE;
		int previousDirectionY = Integer.MIN_VALUE;
		int directionX, directionY;
		
		AbstractNode<NodeLoc> node = nodeValue;
		while (node.getParent() != null)
		{
			directionX = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
			directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
			
			// only add a new route point if moving direction changes
			if ((directionX != previousDirectionX) || (directionY != previousDirectionY))
			{
				previousDirectionX = directionX;
				previousDirectionY = directionY;
				
				path.addFirst(node.getLoc());
				node.setLoc(null);
			}
			
			node = node.getParent();
		}
		
		return path;
	}
	
	private CellNodeBuffer alloc(int size)
	{
		CellNodeBuffer current = null;
		for (BufferInfo i : _buffers)
		{
			if (i.mapSize >= size)
			{
				for (CellNodeBuffer buf : i.buffer)
				{
					if (buf.lock())
					{
						current = buf;
						break;
					}
				}
				if (current != null)
				{
					break;
				}
				
				// not found, allocate temporary buffer
				current = new CellNodeBuffer(i.mapSize);
				current.lock();
				if (i.buffer.size() < i.count)
				{
					i.buffer.add(current);
					break;
				}
			}
		}
		
		return current;
	}
	
	private static final class BufferInfo
	{
		final int mapSize;
		final int count;
		ArrayList<CellNodeBuffer> buffer;
		
		public BufferInfo(int size, int cnt)
		{
			mapSize = size;
			count = cnt;
			buffer = new ArrayList<>(count);
		}
	}
	
	public static GeoEnginePathfinding getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GeoEnginePathfinding INSTANCE = new GeoEnginePathfinding();
	}
}
