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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.TradeList.TradeItem;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:40 $
 */
public class PrivateStoreManageListBuy implements IClientOutgoingPacket
{
	private final Player _player;
	private int _playerAdena;
	private final List<Item> _itemList;
	private final List<TradeItem> _buyList;
	
	public PrivateStoreManageListBuy(Player player)
	{
		_player = player;
		if (Config.SELL_BY_ITEM)
		{
			_playerAdena = _player.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			_playerAdena = _player.getAdena();
		}
		
		_itemList = _player.getInventory().getUniqueItems(false, true, true);
		_buyList = _player.getBuyList().getItems();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PRIVATE_STORE_MANAGE_LIST_BUY.writeId(packet);
		// section 1
		packet.writeD(_player.getObjectId());
		packet.writeD(_playerAdena);
		
		// section2
		packet.writeD(_itemList.size()); // inventory items for potential buy
		for (Item item : _itemList)
		{
			packet.writeD(item.getItemId());
			packet.writeH(item.getEnchantLevel()); // show enchant level, but you can't buy enchanted weapons because of L2 Interlude Client bug
			packet.writeD(item.getCount());
			packet.writeD(item.getReferencePrice());
			packet.writeH(0x00);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getItem().getType2());
		}
		
		// section 3
		packet.writeD(_buyList.size()); // count for all items already added for buy
		for (TradeList.TradeItem item : _buyList)
		{
			packet.writeD(item.getItem().getItemId());
			packet.writeH(item.getEnchant());
			packet.writeD(item.getCount());
			packet.writeD(item.getItem().getReferencePrice());
			packet.writeH(0x00);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getItem().getType2());
			packet.writeD(item.getPrice()); // your price
			packet.writeD(item.getItem().getReferencePrice()); // fixed store price
		}
		return true;
	}
}