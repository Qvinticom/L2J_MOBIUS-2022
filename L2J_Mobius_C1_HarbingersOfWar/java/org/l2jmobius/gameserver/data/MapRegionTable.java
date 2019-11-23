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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class MapRegionTable
{
	private static int[][] _regions = new int[19][21];
	private static MapRegionTable _instance;
	private static int[][] _townPositions = new int[13][3];
	private static int[][] _karmaPositions = new int[13][3];
	
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
		int count = 0;
		try
		{
			File regionDataFile = new File("data/mapregion.csv");
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(regionDataFile)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, ";");
				for (int j = 0; j < 10; ++j)
				{
					MapRegionTable._regions[j][count] = Integer.parseInt(st.nextToken());
				}
				++count;
			}
			lnr.close();
		}
		catch (Exception e)
		{
		}
		
		_townPositions[0][0] = -84176;
		_townPositions[0][1] = 243382;
		_townPositions[0][2] = -3126;
		_townPositions[1][0] = 45525;
		_townPositions[1][1] = 48376;
		_townPositions[1][2] = -3059;
		_townPositions[2][0] = 12181;
		_townPositions[2][1] = 16675;
		_townPositions[2][2] = -4580;
		_townPositions[3][0] = -45232;
		_townPositions[3][1] = -113603;
		_townPositions[3][2] = -224;
		_townPositions[4][0] = 115074;
		_townPositions[4][1] = -178115;
		_townPositions[4][2] = -880;
		_townPositions[5][0] = -14138;
		_townPositions[5][1] = 122042;
		_townPositions[5][2] = -2988;
		_townPositions[6][0] = -82856;
		_townPositions[6][1] = 150901;
		_townPositions[6][2] = -3128;
		_townPositions[7][0] = 18823;
		_townPositions[7][1] = 145048;
		_townPositions[7][2] = -3126;
		_townPositions[8][0] = 83235;
		_townPositions[8][1] = 148497;
		_townPositions[8][2] = -3404;
		_townPositions[9][0] = 80853;
		_townPositions[9][1] = 54653;
		_townPositions[9][2] = -1524;
		_townPositions[10][0] = 147391;
		_townPositions[10][1] = 25967;
		_townPositions[10][2] = -2012;
		_townPositions[11][0] = 117163;
		_townPositions[11][1] = 76511;
		_townPositions[11][2] = -2712;
		_townPositions[12][0] = 83235;
		_townPositions[12][1] = 148497;
		_townPositions[12][2] = -3404;
		
		// FIXME: Custom locations.
		_karmaPositions[0][0] = -88708;
		_karmaPositions[0][1] = 237685;
		_karmaPositions[0][2] = -3672;
		_karmaPositions[1][0] = 40659;
		_karmaPositions[1][1] = 56770;
		_karmaPositions[1][2] = -3651;
		_karmaPositions[2][0] = -346;
		_karmaPositions[2][1] = 21889;
		_karmaPositions[2][2] = -3256;
		_karmaPositions[3][0] = -48359;
		_karmaPositions[3][1] = -108190;
		_karmaPositions[3][2] = -371;
		_karmaPositions[4][0] = 119952;
		_karmaPositions[4][1] = -188167;
		_karmaPositions[4][2] = -3320;
		_karmaPositions[5][0] = -9643;
		_karmaPositions[5][1] = 130653;
		_karmaPositions[5][2] = -3542;
		_karmaPositions[6][0] = -82930;
		_karmaPositions[6][1] = 156775;
		_karmaPositions[6][2] = -3156;
		_karmaPositions[7][0] = 15436;
		_karmaPositions[7][1] = 148426;
		_karmaPositions[7][2] = -3371;
		_karmaPositions[8][0] = 78274;
		_karmaPositions[8][1] = 145178;
		_karmaPositions[8][2] = -3598;
		_karmaPositions[9][0] = 76658;
		_karmaPositions[9][1] = 56229;
		_karmaPositions[9][2] = -2980;
		_karmaPositions[10][0] = 152543;
		_karmaPositions[10][1] = 29202;
		_karmaPositions[10][2] = -2337;
		_karmaPositions[11][0] = 111115;
		_karmaPositions[11][1] = 66811;
		_karmaPositions[11][2] = -2764;
		_karmaPositions[12][0] = 79253;
		_karmaPositions[12][1] = 159441;
		_karmaPositions[12][2] = -3207;
	}
	
	public int getMapRegion(int posX, int posY)
	{
		int tileX = (posX >> 15) + 4;
		int tileY = (posY >> 15) + 10;
		return _regions[tileX][tileY];
	}
	
	public int[] getClosestTownCords(PlayerInstance player)
	{
		int closest = getMapRegion(player.getX(), player.getY());
		int[] closestCords;
		if (player.getKarma() > 0)
		{
			closestCords = new int[]
			{
				_karmaPositions[closest][0],
				_karmaPositions[closest][1],
				_karmaPositions[closest][2]
			};
		}
		else
		{
			closestCords = new int[]
			{
				_townPositions[closest][0],
				_townPositions[closest][1],
				_townPositions[closest][2]
			};
		}
		return closestCords;
	}
}
