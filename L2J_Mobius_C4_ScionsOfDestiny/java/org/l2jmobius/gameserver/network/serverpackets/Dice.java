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
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:40 $
 */
public class Dice implements IClientOutgoingPacket
{
	private final int _objectId;
	private final int _itemId;
	private final int _number;
	private final int _x;
	private final int _y;
	private final int _z;
	
	/**
	 * 0xd4 Dice dddddd
	 * @param charObjId
	 * @param itemId
	 * @param number
	 * @param x
	 * @param y
	 * @param z
	 */
	public Dice(int charObjId, int itemId, int number, int x, int y, int z)
	{
		_objectId = charObjId;
		_itemId = itemId;
		_number = number;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.DICE.writeId(packet);
		packet.writeD(_objectId); // object id of player
		packet.writeD(_itemId); // item id of dice (spade) 4625,4626,4627,4628
		packet.writeD(_number); // number rolled
		packet.writeD(_x); // x
		packet.writeD(_y); // y
		packet.writeD(_z); // z
		return true;
	}
}
