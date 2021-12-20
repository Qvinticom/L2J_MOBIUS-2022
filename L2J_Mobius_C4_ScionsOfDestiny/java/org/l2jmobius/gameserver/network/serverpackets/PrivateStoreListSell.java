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
 * @version $Revision: 1.2.2.3.2.6 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreListSell implements IClientOutgoingPacket
{
	private final Player _storePlayer;
	private final Player _player;
	private int _playerAdena;
	private final boolean _packageSale;
	private final List<TradeItem> _items;
	
	// player's private shop
	public PrivateStoreListSell(Player player, Player storePlayer)
	{
		_player = player;
		_storePlayer = storePlayer;
		if (Config.SELL_BY_ITEM)
		{
			_player.sendPacket(new CreatureSay(0, ChatType.PARTYROOM_COMMANDER, "", "ATTENTION: Store System is not based on Adena, be careful!"));
			_playerAdena = _player.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			_playerAdena = _player.getAdena();
		}
		_storePlayer.getSellList().updateItems();
		_items = _storePlayer.getSellList().getItems();
		_packageSale = _storePlayer.getSellList().isPackaged();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PRIVATE_STORE_LIST_SELL.writeId(packet);
		packet.writeD(_storePlayer.getObjectId());
		packet.writeD(_packageSale ? 1 : 0);
		packet.writeD(_playerAdena);
		packet.writeD(_items.size());
		for (TradeList.TradeItem item : _items)
		{
			packet.writeD(item.getItem().getType2());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItem().getItemId());
			packet.writeD(item.getCount());
			packet.writeH(0);
			packet.writeH(item.getEnchant());
			packet.writeH(0);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeD(item.getPrice()); // your price
			packet.writeD(item.getItem().getReferencePrice()); // store price
		}
		return true;
	}
}