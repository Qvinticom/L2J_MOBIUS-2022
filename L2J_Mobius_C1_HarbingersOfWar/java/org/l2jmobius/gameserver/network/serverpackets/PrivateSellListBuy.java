/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.List;

import org.l2jmobius.gameserver.model.TradeItem;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class PrivateSellListBuy extends ServerBasePacket
{
	private final PlayerInstance _buyer;
	
	public PrivateSellListBuy(PlayerInstance buyer)
	{
		_buyer = buyer;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xD0);
		writeD(_buyer.getObjectId());
		writeD(_buyer.getAdena());
		final TradeList list = new TradeList(0);
		final List<TradeItem> buyList = _buyer.getBuyList();
		for (ItemInstance item : _buyer.getInventory().getItems())
		{
			if (item.isEquipped() || (item.getItem().getType2() == 3) || ((item.getItem().getType2() == 4) && (item.getItem().getType1() == 4)) || ((item.getItem().getType2() == 1) && (item.getItem().getType1() == 1)))
			{
				continue;
			}
			list.addItem(item);
		}
		int count = list.getItems().size();
		writeD(count);
		for (ItemInstance item : list.getItems())
		{
			writeD(item.getItemId());
			writeH(0);
			writeD(item.getCount());
			writeD(item.getPrice());
			writeH(0);
			writeD(0);
			writeH(0);
		}
		count = buyList.size();
		writeD(count);
		if (count != 0)
		{
			for (TradeItem tradeItem : buyList)
			{
				writeD(tradeItem.getItemId());
				writeH(0);
				writeD(tradeItem.getCount());
				writeD(tradeItem.getStorePrice());
				writeH(0);
				writeD(0);
				writeH(0);
				writeD(tradeItem.getOwnersPrice());
				writeD(55);
			}
		}
	}
}
