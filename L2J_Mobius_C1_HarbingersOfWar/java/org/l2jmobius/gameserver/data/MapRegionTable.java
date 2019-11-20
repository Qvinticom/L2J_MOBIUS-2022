/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.actor.Creature;

public class MapRegionTable
{
	private static Logger _log = Logger.getLogger(MapRegionTable.class.getName());
	
	private static int[][] _regions = new int[19][21];
	private static MapRegionTable _instance;
	
	public static MapRegionTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new MapRegionTable();
		}
		return _instance;
	}
	
	private MapRegionTable()
	{
		super();
		int count = 0;
		int count2 = 0;
		LineNumberReader lnr = null;
		try
		{
			File regionDataFile = new File("data/mapregion.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(regionDataFile)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, ";");
				for (int j = 0; j < 10; ++count2, ++j)
				{
					MapRegionTable._regions[j][count] = Integer.parseInt(st.nextToken());
				}
				++count;
			}
			
			try
			{
				_log.fine("Loaded " + count2 + " map regions.");
				lnr.close();
				return;
			}
			catch (FileNotFoundException e)
			{
				_log.warning("mapregion.csv is missing in data folder.");
				try
				{
				}
				catch (Exception e1)
				{
					return;
				}
				_log.fine("Loaded " + count2 + " map regions.");
				lnr.close();
				return;
			}
			catch (Exception e)
			{
				_log.warning("Rrror while creating map region data: " + e);
				try
				{
				}
				catch (Exception e1)
				{
					return;
				}
				_log.fine("Loaded " + count2 + " map regions.");
				lnr.close();
				return;
			}
		}
		catch (Throwable throwable)
		{
			_log.fine("Loaded " + count2 + " map regions.");
			try
			{
				if (lnr != null)
				{
					lnr.close();
				}
			}
			catch (IOException e)
			{
			}
		}
	}
	
	public int getMapRegion(int posX, int posY)
	{
		int tileX = (posX >> 15) + 4;
		int tileY = (posY >> 15) + 10;
		return _regions[tileX][tileY];
	}
	
	public String getClosestTownCords(Creature activeChar)
	{
		int[][] pos = new int[13][3];
		pos[0][0] = -84176;
		pos[0][1] = 243382;
		pos[0][2] = -3126;
		pos[1][0] = 45525;
		pos[1][1] = 48376;
		pos[1][2] = -3059;
		pos[2][0] = 12181;
		pos[2][1] = 16675;
		pos[2][2] = -4580;
		pos[3][0] = -45232;
		pos[3][1] = -113603;
		pos[3][2] = -224;
		pos[4][0] = 115074;
		pos[4][1] = -178115;
		pos[4][2] = -880;
		pos[5][0] = -14138;
		pos[5][1] = 122042;
		pos[5][2] = -2988;
		pos[6][0] = -82856;
		pos[6][1] = 150901;
		pos[6][2] = -3128;
		pos[7][0] = 18823;
		pos[7][1] = 145048;
		pos[7][2] = -3126;
		pos[8][0] = 83235;
		pos[8][1] = 148497;
		pos[8][2] = -3404;
		pos[9][0] = 80853;
		pos[9][1] = 54653;
		pos[9][2] = -1524;
		pos[10][0] = 147391;
		pos[10][1] = 25967;
		pos[10][2] = -2012;
		pos[11][0] = 117163;
		pos[11][1] = 76511;
		pos[11][2] = -2712;
		pos[12][0] = 83235;
		pos[12][1] = 148497;
		pos[12][2] = -3404;
		int closest = getMapRegion(activeChar.getX(), activeChar.getY());
		String ClosestTownCords = pos[closest][0] + "!" + pos[closest][1] + "!" + pos[closest][2];
		return ClosestTownCords;
	}
}
