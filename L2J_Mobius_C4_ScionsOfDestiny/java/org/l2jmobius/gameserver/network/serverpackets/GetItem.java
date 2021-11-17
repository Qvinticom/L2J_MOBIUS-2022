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
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * sample 0000: 17 1a 95 20 48 9b da 12 40 44 17 02 00 03 f0 fc ff 98 f1 ff ff ..... format ddddd
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class GetItem implements IClientOutgoingPacket
{
	private final Item _item;
	private final int _playerId;
	
	public GetItem(Item item, int playerId)
	{
		_item = item;
		_playerId = playerId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GET_ITEM.writeId(packet);
		packet.writeD(_playerId);
		packet.writeD(_item.getObjectId());
		
		packet.writeD(_item.getX());
		packet.writeD(_item.getY());
		packet.writeD(_item.getZ());
		return true;
	}
}
