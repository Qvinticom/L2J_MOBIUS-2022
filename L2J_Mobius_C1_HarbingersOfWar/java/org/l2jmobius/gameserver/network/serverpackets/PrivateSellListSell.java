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

public class PrivateSellListSell extends ServerBasePacket
{
	private final PlayerInstance _seller;
	
	public PrivateSellListSell(PlayerInstance seller)
	{
		_seller = seller;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xB3);
		writeD(_seller.getObjectId());
		writeD(_seller.getAdena());
		final TradeList list = new TradeList(0);
		final List<TradeItem> sellList = _seller.getSellList();
		for (ItemInstance item : _seller.getInventory().getItems())
		{
			if (item.isEquipped() || (item.getItem().getType2() == 3) || ((item.getItem().getType2() == 4) && (item.getItem().getType1() == 4)) || ((item.getItem().getType2() == 1) && (item.getItem().getType1() == 1)))
			{
				continue;
			}
			list.addItem(item);
		}
		if (!sellList.isEmpty())
		{
			for (TradeItem tradeItem : sellList)
			{
				list.removeItem(tradeItem.getObjectId(), tradeItem.getCount());
			}
		}
		int count = list.getItems().size();
		writeD(count);
		for (ItemInstance item : list.getItems())
		{
			writeD(0);
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeD(0);
			writeH(0);
			writeH(0);
			writeH(0);
			writeD(500);
		}
		count = sellList.size();
		writeD(count);
		if (count != 0)
		{
			for (TradeItem tradeItem : sellList)
			{
				writeD(0);
				writeD(tradeItem.getObjectId());
				writeD(tradeItem.getItemId());
				writeD(tradeItem.getCount());
				writeD(0);
				writeH(0);
				writeH(0);
				writeH(0);
				writeD(tradeItem.getOwnersPrice());
				writeD(tradeItem.getStorePrice());
			}
		}
	}
}
