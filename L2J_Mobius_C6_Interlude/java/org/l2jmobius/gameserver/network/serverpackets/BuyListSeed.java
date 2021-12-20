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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.StoreTradeList;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Format: c ddh[hdddhhd] c - id (0xE8) d - money d - manor id h - size [ h - item type 1 d - object id d - item id d - count h - item type 2 h d - price ]
 * @author l3x
 */
public class BuyListSeed implements IClientOutgoingPacket
{
	private final int _manorId;
	private List<Item> _list = new ArrayList<>();
	private final int _money;
	
	public BuyListSeed(StoreTradeList list, int manorId, int currentMoney)
	{
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.BUY_LIST_SEED.writeId(packet);
		packet.writeD(_money); // current money
		packet.writeD(_manorId); // manor id
		packet.writeH(_list.size()); // list length
		for (Item item : _list)
		{
			packet.writeH(4); // item->type1
			packet.writeD(0); // objectId
			packet.writeD(item.getItemId()); // item id
			packet.writeD(item.getCount()); // item count
			packet.writeH(4); // item->type2
			packet.writeH(0); // unknown :)
			packet.writeD(item.getPriceToSell()); // price
		}
		return true;
	}
}
