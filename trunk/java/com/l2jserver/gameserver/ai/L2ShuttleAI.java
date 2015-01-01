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
package com.l2jserver.gameserver.ai;

import com.l2jserver.gameserver.model.actor.instance.L2AirShipInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ShuttleInstance;
import com.l2jserver.gameserver.network.serverpackets.shuttle.ExShuttleMove;

/**
 * @author UnAfraid
 */
public class L2ShuttleAI extends L2VehicleAI
{
	public L2ShuttleAI(L2AirShipInstance.AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	public void moveTo(int x, int y, int z)
	{
		if (!_actor.isMovementDisabled())
		{
			_clientMoving = true;
			_accessor.moveTo(x, y, z);
			_actor.broadcastPacket(new ExShuttleMove(getActor(), x, y, z));
		}
	}
	
	@Override
	public L2ShuttleInstance getActor()
	{
		return (L2ShuttleInstance) _actor;
	}
}
