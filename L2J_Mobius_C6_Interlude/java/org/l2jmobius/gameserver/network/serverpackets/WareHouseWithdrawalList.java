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

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * 0x42 WarehouseWithdrawalList dh (h dddhh dhhh d)
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class WareHouseWithdrawalList implements IClientOutgoingPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 4; // not sure
	
	private Player _player;
	private int _playerAdena;
	private Collection<Item> _items;
	private int _whType;
	
	public WareHouseWithdrawalList(Player player, int type)
	{
		_player = player;
		_whType = type;
		_playerAdena = _player.getAdena();
		if (_player.getActiveWarehouse() == null)
		{
			// Something went wrong!
			LOGGER.warning("error while sending withdraw request to: " + _player.getName());
			return;
		}
		_items = _player.getActiveWarehouse().getItems();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.WARE_HOUSE_WITHDRAWAL_LIST.writeId(packet);
		/*
		 * 0x01-Private Warehouse 0x02-Clan Warehouse 0x03-Castle Warehouse 0x04-Warehouse
		 */
		packet.writeH(_whType);
		packet.writeD(_playerAdena);
		packet.writeH(_items.size());
		for (Item item : _items)
		{
			packet.writeH(item.getItem().getType1()); // item type1 //unconfirmed, works
			packet.writeD(0x00); // unconfirmed, works
			packet.writeD(item.getItemId()); // unconfirmed, works
			packet.writeD(item.getCount()); // unconfirmed, works
			packet.writeH(item.getItem().getType2()); // item type2 //unconfirmed, works
			packet.writeH(0x00); // ?
			packet.writeD(item.getItem().getBodyPart()); // ?
			packet.writeH(item.getEnchantLevel()); // enchant level -confirmed
			packet.writeH(0x00); // ?
			packet.writeH(0x00); // ?
			packet.writeD(item.getObjectId()); // item id - confimed
			if (item.isAugmented())
			{
				packet.writeD(0x0000FFFF & item.getAugmentation().getAugmentationId());
				packet.writeD(item.getAugmentation().getAugmentationId() >> 16);
			}
			else
			{
				packet.writeQ(0x00);
			}
		}
		return true;
	}
}
