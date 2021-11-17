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
import org.l2jmobius.gameserver.model.actor.instance.Boat;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Maktakien
 */
public class VehicleInfo implements IClientOutgoingPacket
{
	private final Boat _boat;
	
	/**
	 * @param boat
	 */
	public VehicleInfo(Boat boat)
	{
		_boat = boat;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.VEHICLE_INFO.writeId(packet);
		packet.writeD(_boat.getObjectId());
		packet.writeD(_boat.getX());
		packet.writeD(_boat.getY());
		packet.writeD(_boat.getZ());
		packet.writeD(_boat.getPosition().getHeading());
		return true;
	}
}
