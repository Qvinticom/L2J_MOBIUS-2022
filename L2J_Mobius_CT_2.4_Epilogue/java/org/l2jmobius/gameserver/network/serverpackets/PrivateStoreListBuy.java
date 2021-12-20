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
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class PrivateStoreListBuy implements IClientOutgoingPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final Collection<TradeItem> _items;
	
	public PrivateStoreListBuy(Player player, Player storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = player.getAdena();
		storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		_items = storePlayer.getBuyList().getAvailableItems(player.getInventory());
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PRIVATE_STORE_BUY_LIST.writeId(packet);
		packet.writeD(_objId);
		packet.writeQ(_playerAdena);
		packet.writeD(_items.size());
		for (TradeItem item : _items)
		{
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItem().getId());
			packet.writeH(item.getEnchant());
			packet.writeQ(item.getCount()); // give max possible sell amount
			packet.writeQ(item.getItem().getReferencePrice());
			packet.writeH(0);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getItem().getType2());
			packet.writeQ(item.getPrice()); // buyers price
			packet.writeQ(item.getStoreCount()); // maximum possible tradecount
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
