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
import org.l2jmobius.gameserver.model.TradeItem;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class PrivateStoreManageListBuy implements IClientOutgoingPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final Collection<Item> _itemList;
	private final Collection<TradeItem> _buyList;
	
	public PrivateStoreManageListBuy(Player player)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdena();
		_itemList = player.getInventory().getUniqueItems(false, true);
		_buyList = player.getBuyList().getItems();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PRIVATE_STORE_BUY_MANAGE_LIST.writeId(packet);
		// section 1
		packet.writeD(_objId);
		packet.writeQ(_playerAdena);
		
		// section2
		packet.writeD(_itemList.size()); // inventory items for potential buy
		for (Item item : _itemList)
		{
			packet.writeD(item.getId());
			packet.writeH(0); // show enchant level as 0, as you can't buy enchanted weapons
			packet.writeQ(item.getCount());
			packet.writeQ(item.getReferencePrice());
			packet.writeH(0x00);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getItem().getType2());
			
			// T1
			packet.writeH(item.getAttackElementType());
			packet.writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				packet.writeH(item.getElementDefAttr(i));
			}
			
			for (int op : item.getEnchantOptions())
			{
				packet.writeH(op);
			}
		}
		
		// section 3
		packet.writeD(_buyList.size()); // count for all items already added for buy
		for (TradeItem item : _buyList)
		{
			packet.writeD(item.getItem().getId());
			packet.writeH(0);
			packet.writeQ(item.getCount());
			packet.writeQ(item.getItem().getReferencePrice());
			packet.writeH(0x00);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getItem().getType2());
			packet.writeQ(item.getPrice()); // your price
			packet.writeQ(item.getItem().getReferencePrice()); // fixed store price
			
			// T1
			packet.writeH(item.getAttackElementType());
			packet.writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				packet.writeH(item.getElementDefAttr(i));
			}
			
			for (int op : item.getEnchantOptions())
			{
				packet.writeH(op);
			}
		}
		return true;
	}
}
