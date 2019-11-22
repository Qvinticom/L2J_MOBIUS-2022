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
	private static final String _S__B3_PRIVATESELLLISTSELL = "[S] B3 PrivateSellListSell";
	private final PlayerInstance _seller;
	
	public PrivateSellListSell(PlayerInstance seller)
	{
		_seller = seller;
	}
	
	@Override
	public byte[] getContent()
	{
		TradeItem temp2;
		int i;
		int i2;
		writeC(179);
		writeD(_seller.getObjectId());
		writeD(_seller.getAdena());
		TradeList list = new TradeList(0);
		List<TradeItem> sellList = _seller.getSellList();
		int count = _seller.getInventory().getSize();
		for (ItemInstance item : _seller.getInventory().getItems())
		{
			if (item.isEquipped() || (item.getItem().getType2() == 3) || ((item.getItem().getType2() == 4) && (item.getItem().getType1() == 4)) || ((item.getItem().getType2() == 1) && (item.getItem().getType1() == 1)) || item.isEquipped())
			{
				continue;
			}
			list.addItem(item);
		}
		if (sellList.size() != 0)
		{
			for (i = 0; i < sellList.size(); ++i)
			{
				temp2 = sellList.get(i);
				list.removeItem(temp2.getObjectId(), temp2.getCount());
			}
		}
		count = list.getItems().size();
		writeD(count);
		for (i2 = 0; i2 < count; ++i2)
		{
			ItemInstance temp = list.getItems().get(i2);
			writeD(0);
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
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
			for (i2 = 0; i2 < count; ++i2)
			{
				temp2 = sellList.get(i2);
				System.out.println("item:" + temp2.getObjectId());
				writeD(0);
				writeD(temp2.getObjectId());
				writeD(temp2.getItemId());
				writeD(temp2.getCount());
				writeD(0);
				writeH(0);
				writeH(0);
				writeH(0);
				writeD(temp2.getOwnersPrice());
				writeD(temp2.getStorePrice());
			}
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__B3_PRIVATESELLLISTSELL;
	}
}
