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

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author -Wooden-
 */
public class PackageSendableList implements IClientOutgoingPacket
{
	private final List<Item> _items;
	private final int _playerObjId;
	private final int _adena;
	
	public PackageSendableList(List<Item> items, int playerObjId, int adena)
	{
		_items = items;
		_playerObjId = playerObjId;
		_adena = adena;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PACKAGE_SENDABLE_LIST.writeId(packet);
		packet.writeD(_playerObjId);
		packet.writeD(_adena);
		packet.writeD(_items.size());
		for (Item item : _items) // format inside the for taken from SellList part use should be about the same
		{
			packet.writeH(item.getItem().getType1());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItemId());
			packet.writeD(item.getCount());
			packet.writeH(item.getItem().getType2());
			packet.writeH(0);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getEnchantLevel());
			packet.writeH(0);
			packet.writeH(0);
			packet.writeD(item.getObjectId()); // some item identifier later used by client to answer (see RequestPackageSend) not item id nor object id maybe some freight system id??
		}
		return true;
	}
}
