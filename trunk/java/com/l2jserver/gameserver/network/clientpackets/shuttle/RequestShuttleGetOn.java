/*
 * Copyright (C) 2004-2015 L2J Server
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
package com.l2jserver.gameserver.network.clientpackets.shuttle;

import java.util.logging.Level;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ShuttleInstance;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;

/**
 * @author UnAfraid
 */
public class RequestShuttleGetOn extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	protected void readImpl()
	{
		readD(); // charId
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// TODO: better way?
		for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
		{
			if ((obj instanceof L2ShuttleInstance))
			{
				L2ShuttleInstance shuttle = (L2ShuttleInstance) obj;
				if (shuttle.calculateDistance(activeChar, false, false) < 1000)
				{
					shuttle.addPassenger(activeChar);
					activeChar.getInVehiclePosition().setXYZ(_x, _y, _z);
					break;
				}
				_log.log(Level.INFO, getClass().getSimpleName() + ": range between char and shuttle: " + shuttle.calculateDistance(activeChar, false, false));
			}
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
