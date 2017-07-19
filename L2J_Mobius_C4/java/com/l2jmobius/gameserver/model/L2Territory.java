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
package com.l2jmobius.gameserver.model;

import java.util.List;
import java.util.logging.Logger;

import com.l2jmobius.util.Rnd;

import javolution.util.FastList;

public class L2Territory
{
	private static Logger _log = Logger.getLogger(L2Territory.class.getName());
	
	protected class Point
	{
		protected int x, y, zmin, zmax, proc;
		
		Point(int _x, int _y, int _zmin, int _zmax, int _proc)
		{
			x = _x;
			y = _y;
			zmin = _zmin;
			zmax = _zmax;
			proc = _proc;
		}
	}
	
	private final List<Point> _points;
	private final int _terr;
	private int _x_min;
	private int _x_max;
	private int _y_min;
	private int _y_max;
	private int _z_min;
	private int _z_max;
	private int _proc_max;
	
	public L2Territory(int terr)
	{
		_points = new FastList<>();
		_terr = terr;
		_x_min = 999999;
		_x_max = -999999;
		_y_min = 999999;
		_y_max = -999999;
		_z_min = 999999;
		_z_max = -999999;
		_proc_max = 0;
	}
	
	public void add(int x, int y, int zmin, int zmax, int proc)
	{
		_points.add(new Point(x, y, zmin, zmax, proc));
		if (x < _x_min)
		{
			_x_min = x;
		}
		if (y < _y_min)
		{
			_y_min = y;
		}
		if (x > _x_max)
		{
			_x_max = x;
		}
		if (y > _y_max)
		{
			_y_max = y;
		}
		if (zmin < _z_min)
		{
			_z_min = zmin;
		}
		if (zmax > _z_max)
		{
			_z_max = zmax;
		}
		_proc_max += proc;
	}
	
	public void print()
	{
		for (final Point p : _points)
		{
			System.out.println("(" + p.x + "," + p.y + ")");
		}
	}
	
	public boolean isIntersect(int x, int y, Point p1, Point p2)
	{
		final double dy1 = p1.y - y;
		final double dy2 = p2.y - y;
		
		if (Math.signum(dy1) == Math.signum(dy2))
		{
			return false;
		}
		
		final double dx1 = p1.x - x;
		final double dx2 = p2.x - x;
		
		if ((dx1 >= 0) && (dx2 >= 0))
		{
			return true;
		}
		
		if ((dx1 < 0) && (dx2 < 0))
		{
			return false;
		}
		
		final double dx0 = (dy1 * (p1.x - p2.x)) / (p1.y - p2.y);
		
		return dx0 <= dx1;
	}
	
	public boolean isInside(int x, int y)
	{
		int intersect_count = 0;
		for (int i = 0; i < _points.size(); i++)
		{
			final Point p1 = _points.get(i > 0 ? i - 1 : _points.size() - 1);
			final Point p2 = _points.get(i);
			
			if (isIntersect(x, y, p1, p2))
			{
				intersect_count++;
			}
		}
		
		return (intersect_count % 2) == 1;
	}
	
	public int[] getRandomPoint()
	{
		int i;
		final int[] p = new int[4];
		if (_proc_max > 0)
		{
			int pos = 0;
			final int rnd = Rnd.nextInt(_proc_max);
			for (i = 0; i < _points.size(); i++)
			{
				final Point p1 = _points.get(i);
				pos += p1.proc;
				if (rnd <= pos)
				{
					p[0] = p1.x;
					p[1] = p1.y;
					p[2] = p1.zmin;
					p[3] = p1.zmax;
					return p;
				}
			}
			
		}
		for (i = 0; i < 100; i++)
		{
			p[0] = Rnd.get(_x_min, _x_max);
			p[1] = Rnd.get(_y_min, _y_max);
			if (isInside(p[0], p[1]))
			{
				double curdistance = 0;
				p[2] = _z_min + 100;
				p[3] = _z_max;
				for (i = 0; i < _points.size(); i++)
				{
					final Point p1 = _points.get(i);
					final double dx = p1.x - p[0];
					final double dy = p1.y - p[1];
					final double distance = Math.sqrt((dx * dx) + (dy * dy));
					if ((curdistance == 0) || (distance < curdistance))
					{
						curdistance = distance;
						p[2] = p1.zmin + 100;
					}
				}
				return p;
			}
		}
		_log.warning("Can't make point for territory" + _terr);
		return p;
	}
	
	public int getProcMax()
	{
		return _proc_max;
	}
}