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
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class ValidateLocationInVehicle implements IClientOutgoingPacket
{
	private int _boat = 1343225858;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _playerObj;
	
	public ValidateLocationInVehicle(Creature creature)
	{
		_playerObj = creature.getObjectId();
		_x = creature.getX();
		_y = creature.getY();
		_z = creature.getZ();
		_heading = creature.getHeading();
		_boat = ((Player) creature).getBoat().getObjectId();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.VALIDATE_LOCATION_IN_VEHICLE.writeId(packet);
		packet.writeD(_playerObj);
		packet.writeD(_boat);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		packet.writeD(_heading);
		return true;
	}
}