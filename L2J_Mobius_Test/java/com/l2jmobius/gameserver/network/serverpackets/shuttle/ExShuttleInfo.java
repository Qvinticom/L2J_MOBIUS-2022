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
package com.l2jmobius.gameserver.network.serverpackets.shuttle;

import java.util.List;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.instance.L2ShuttleInstance;
import com.l2jmobius.gameserver.model.shuttle.L2ShuttleStop;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author UnAfraid
 */
public class ExShuttleInfo extends L2GameServerPacket
{
	private final L2ShuttleInstance _shuttle;
	private final List<L2ShuttleStop> _stops;
	
	public ExShuttleInfo(L2ShuttleInstance shuttle)
	{
		_shuttle = shuttle;
		_stops = shuttle.getStops();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xCB);
		
		writeD(_shuttle.getObjectId());
		writeD(_shuttle.getX());
		writeD(_shuttle.getY());
		writeD(_shuttle.getZ());
		writeD(_shuttle.getHeading());
		writeD(_shuttle.getId());
		writeD(_stops.size());
		for (L2ShuttleStop stop : _stops)
		{
			writeD(stop.getId());
			for (Location loc : stop.getDimensions())
			{
				writeD(loc.getX());
				writeD(loc.getY());
				writeD(loc.getZ());
			}
			writeD(stop.isDoorOpen() ? 0x01 : 0x00);
			writeD(stop.hasDoorChanged() ? 0x01 : 0x00);
		}
	}
}
