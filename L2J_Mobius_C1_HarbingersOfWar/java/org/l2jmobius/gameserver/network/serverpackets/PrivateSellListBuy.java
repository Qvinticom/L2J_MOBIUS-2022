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
	private static final String _S__D0_PRIVATESELLLISTBUY = "[S] D0 PrivateSellListBuy";
	private final PlayerInstance _buyer;
	
	public PrivateSellListBuy(PlayerInstance buyer)
	{
		_buyer = buyer;
	}
	
	@Override
	public byte[] getContent()
	{
		int i;
		writeC(208);
		writeD(_buyer.getObjectId());
		writeD(_buyer.getAdena());
		TradeList list = new TradeList(0);
		List<TradeItem> buyList = _buyer.getBuyList();
		int count = _buyer.getInventory().getSize();
		for (ItemInstance item : _buyer.getInventory().getItems())
		{
			if (item.isEquipped() || (item.getItem().getType2() == 3) || ((item.getItem().getType2() == 4) && (item.getItem().getType1() == 4)) || ((item.getItem().getType2() == 1) && (item.getItem().getType1() == 1)) || item.isEquipped())
			{
				continue;
			}
			list.addItem(item);
		}
		count = list.getItems().size();
		writeD(count);
		for (i = 0; i < count; ++i)
		{
			ItemInstance temp = list.getItems().get(i);
			writeD(temp.getItemId());
			writeH(0);
			writeD(temp.getCount());
			writeD(temp.getPrice());
			writeH(0);
			writeD(0);
			writeH(0);
		}
		count = buyList.size();
		writeD(count);
		if (count != 0)
		{
			for (i = 0; i < count; ++i)
			{
				TradeItem temp2 = buyList.get(i);
				writeD(temp2.getItemId());
				writeH(0);
				writeD(temp2.getCount());
				writeD(temp2.getStorePrice());
				writeH(0);
				writeD(0);
				writeH(0);
				writeD(temp2.getOwnersPrice());
				writeD(55);
			}
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__D0_PRIVATESELLLISTBUY;
	}
}
