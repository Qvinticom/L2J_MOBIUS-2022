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
package org.l2jmobius.gameserver.ai;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.AirShip;
import org.l2jmobius.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import org.l2jmobius.gameserver.network.serverpackets.ExStopMoveAirShip;

/**
 * @author DS
 */
public class AirShipAI extends VehicleAI
{
	public AirShipAI(AirShip airShip)
	{
		super(airShip);
	}
	
	@Override
	protected void moveTo(int x, int y, int z)
	{
		if (!_actor.isMovementDisabled())
		{
			_clientMoving = true;
			_actor.moveToLocation(x, y, z, 0);
			_actor.broadcastPacket(new ExMoveToLocationAirShip(getActor()));
		}
	}
	
	@Override
	public void clientStopMoving(Location loc)
	{
		if (_actor.isMoving())
		{
			_actor.stopMove(loc);
		}
		
		if (_clientMoving || (loc != null))
		{
			_clientMoving = false;
			_actor.broadcastPacket(new ExStopMoveAirShip(getActor()));
		}
	}
	
	@Override
	public void describeStateToPlayer(Player player)
	{
		if (_clientMoving)
		{
			player.sendPacket(new ExMoveToLocationAirShip(getActor()));
		}
	}
	
	@Override
	public AirShip getActor()
	{
		return (AirShip) _actor;
	}
}