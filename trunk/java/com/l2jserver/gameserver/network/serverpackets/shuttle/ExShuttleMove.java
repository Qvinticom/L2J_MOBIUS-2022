/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets.shuttle;

import com.l2jserver.gameserver.model.actor.instance.L2ShuttleInstance;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author UnAfraid
 */
public class ExShuttleMove extends L2GameServerPacket
{
	private final L2ShuttleInstance _shuttle;
	private final int _x, _y, _z;
	
	public ExShuttleMove(L2ShuttleInstance shuttle, int x, int y, int z)
	{
		_shuttle = shuttle;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xCE);
		writeD(_shuttle.getObjectId());
		writeD((int) _shuttle.getStat().getMoveSpeed());
		writeD((int) _shuttle.getStat().getRotationSpeed());
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
