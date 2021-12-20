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

public class PrivateStoreManageListSell implements IClientOutgoingPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final Collection<TradeItem> _itemList;
	private final Collection<TradeItem> _sellList;
	
	public PrivateStoreManageListSell(Player player, boolean isPackageSale)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdena();
		player.getSellList().updateItems();
		_packageSale = isPackageSale;
		_itemList = player.getInventory().getAvailableItems(player.getSellList());
		_sellList = player.getSellList().getItems();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PRIVATE_STORE_SELL_MANAGE_LIST.writeId(packet);
		// section 1
		packet.writeD(_objId);
		packet.writeD(_packageSale ? 1 : 0); // Package sell
		packet.writeQ(_playerAdena);
		// section2
		packet.writeD(_itemList.size()); // for potential sells
		for (TradeItem item : _itemList)
		{
			packet.writeD(item.getItem().getType2());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItem().getId());
			packet.writeQ(item.getCount());
			packet.writeH(0);
			packet.writeH(item.getEnchant()); // enchant level
			packet.writeH(item.getCustomType2());
			packet.writeD(item.getItem().getBodyPart());
			packet.writeQ(item.getPrice()); // store price
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
		packet.writeD(_sellList.size()); // count for any items already added for sell
		for (TradeItem item : _sellList)
		{
			packet.writeD(item.getItem().getType2());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItem().getId());
			packet.writeQ(item.getCount());
			packet.writeH(0);
			packet.writeH(item.getEnchant()); // enchant level
			packet.writeH(0);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeQ(item.getPrice()); // your price
			packet.writeQ(item.getItem().getReferencePrice()); // store price
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
