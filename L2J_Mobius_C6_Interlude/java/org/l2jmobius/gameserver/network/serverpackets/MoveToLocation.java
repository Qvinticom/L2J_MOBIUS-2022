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
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * 0000: 01 7a 73 10 4c b2 0b 00 00 a3 fc 00 00 e8 f1 ff .zs.L........... 0010: ff bd 0b 00 00 b3 fc 00 00 e8 f1 ff ff ............. ddddddd
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class MoveToLocation implements IClientOutgoingPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _xDst;
	private final int _yDst;
	private final int _zDst;
	
	public MoveToLocation(Creature creature)
	{
		_objectId = creature.getObjectId();
		_x = creature.getX();
		_y = creature.getY();
		_z = creature.getZ();
		_xDst = creature.getXdestination();
		_yDst = creature.getYdestination();
		_zDst = creature.getZdestination();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHAR_MOVE_TO_LOCATION.writeId(packet);
		
		packet.writeD(_objectId);
		
		packet.writeD(_xDst);
		packet.writeD(_yDst);
		packet.writeD(_zDst);
		
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		return true;
	}
}
