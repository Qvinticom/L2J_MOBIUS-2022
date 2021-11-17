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
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.TradeList.TradeItem;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.7.2.2.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PrivateStoreListBuy implements IClientOutgoingPacket
{
	private final Player _storePlayer;
	private final Player _player;
	private int _playerAdena;
	private final List<TradeItem> _items;
	
	public PrivateStoreListBuy(Player player, Player storePlayer)
	{
		_storePlayer = storePlayer;
		_player = player;
		if (Config.SELL_BY_ITEM)
		{
			_player.sendPacket(new CreatureSay(0, ChatType.PARTYROOM_COMMANDER, "", "ATTENTION: Store System is not based on Adena, be careful!"));
			_playerAdena = _player.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			_playerAdena = _player.getAdena();
		}
		
		// _storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		// this items must be the items available into the _activeChar (seller) inventory
		_items = _storePlayer.getBuyList().getAvailableItems(_player.getInventory());
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PRIVATE_STORE_LIST_BUY.writeId(packet);
		packet.writeD(_storePlayer.getObjectId());
		packet.writeD(_playerAdena);
		packet.writeD(_items.size());
		for (TradeList.TradeItem item : _items)
		{
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItem().getItemId());
			packet.writeH(item.getEnchant());
			// writeD(item.getCount()); //give max possible sell amount
			packet.writeD(item.getCurCount());
			packet.writeD(item.getItem().getReferencePrice());
			packet.writeH(0);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getItem().getType2());
			packet.writeD(item.getPrice()); // buyers price
			packet.writeD(item.getCount()); // maximum possible tradecount
		}
		return true;
	}
}