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
import org.l2jmobius.gameserver.model.TradeList.TradeItem;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * 3 section to this packet 1)playerinfo which is always sent dd 2)list of items which can be added to sell d(hhddddhhhd) 3)list of items which have already been setup for sell in previous sell private store sell manageent d(hhddddhhhdd) *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */

/*
 * In memory of our friend Vadim 03/11/2014
 */
public class PrivateStoreManageListSell implements IClientOutgoingPacket
{
	private final Player _player;
	private int _playerAdena;
	private final boolean _packageSale;
	private final List<TradeItem> _itemList;
	private final List<TradeItem> _sellList;
	
	public PrivateStoreManageListSell(Player player)
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
		
		_player.getSellList().updateItems();
		_packageSale = _player.getSellList().isPackaged();
		_itemList = _player.getInventory().getAvailableItems(_player.getSellList());
		_sellList = _player.getSellList().getItems();
	}
	
	/**
	 * During store set no packets will be received from client just when store definition is finished.
	 */
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PRIVATE_STORE_MANAGE_LIST_SELL.writeId(packet);
		// section 1
		packet.writeD(_player.getObjectId());
		packet.writeD(_packageSale ? 1 : 0); // Package sell
		packet.writeD(_playerAdena);
		
		// section2
		packet.writeD(_itemList.size()); // for potential sells
		for (TradeItem item : _itemList)
		{
			packet.writeD(item.getItem().getType2());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItem().getItemId());
			packet.writeD(item.getCount());
			packet.writeH(0);
			packet.writeH(item.getEnchant()); // enchant level
			packet.writeH(0);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeD(item.getPrice()); // store price
		}
		
		// section 3
		packet.writeD(_sellList.size()); // count for any items already added for sell
		for (TradeItem item : _sellList)
		{
			packet.writeD(item.getItem().getType2());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItem().getItemId());
			packet.writeD(item.getCount());
			packet.writeH(0);
			packet.writeH(item.getEnchant()); // enchant level
			packet.writeH(0x00);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeD(item.getPrice()); // your price
			packet.writeD(item.getItem().getReferencePrice()); // store price
		}
		return true;
	}
}