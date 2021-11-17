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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Maktakien
 */
public class MoveToLocationInVehicle implements IClientOutgoingPacket
{
	private int _objectId;
	private int _boatId;
	private Location _destination;
	private Location _origin;
	
	public MoveToLocationInVehicle(Creature actor, Location destination, Location origin)
	{
		if (!(actor instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) actor;
		if (player.getBoat() == null)
		{
			return;
		}
		
		_objectId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.MOVE_TO_LOCATION_IN_VEHICLE.writeId(packet);
		packet.writeD(_objectId);
		packet.writeD(_boatId);
		packet.writeD(_destination.getX());
		packet.writeD(_destination.getY());
		packet.writeD(_destination.getZ());
		packet.writeD(_origin.getX());
		packet.writeD(_origin.getY());
		packet.writeD(_origin.getZ());
		return true;
	}
}
