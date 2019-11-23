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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.l2jmobius.gameserver.model.TradeItem;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class PrivateBuyListBuy extends ServerBasePacket
{
	private static final String _S__D1_PRIVATEBUYLISTBUY = "[S] D1 PrivateBuyListBuy";
	private final PlayerInstance _buyer;
	private final PlayerInstance _seller;
	
	public PrivateBuyListBuy(PlayerInstance buyer, PlayerInstance seller)
	{
		_buyer = buyer;
		_seller = seller;
	}
	
	@Override
	public byte[] getContent()
	{
		TradeItem temp3;
		TradeItem temp2;
		writeC(209);
		writeD(_buyer.getObjectId());
		writeD(_seller.getAdena());
		final List<TradeItem> buyerslist = _buyer.getBuyList();
		final Collection<ItemInstance> sellerItems = _seller.getInventory().getItems();
		final ArrayList<TradeItem> sellerslist = new ArrayList<>();
		int count = buyerslist.size();
		for (int i = 0; i < count; ++i)
		{
			temp2 = buyerslist.get(i);
			boolean add = false;
			for (ItemInstance item : sellerItems)
			{
				if (temp2.getItemId() != item.getItemId())
				{
					continue;
				}
				temp3 = new TradeItem();
				temp3.setCount(item.getCount());
				temp3.setItemId(item.getItemId());
				temp3.setObjectId(item.getObjectId());
				temp2.setObjectId(item.getObjectId());
				temp3.setOwnersPrice(temp2.getOwnersPrice());
				temp3.setstorePrice(item.getPrice());
				if (sellerslist.contains(temp3))
				{
					break;
				}
				sellerslist.add(temp3);
				add = true;
				break;
			}
			if (add)
			{
				continue;
			}
			temp3 = new TradeItem();
			temp3.setCount(0);
			temp3.setItemId(temp2.getItemId());
			temp3.setOwnersPrice(temp2.getOwnersPrice());
			sellerslist.add(temp3);
		}
		count = sellerslist.size();
		writeD(count);
		for (int i = 0; i < count; ++i)
		{
			temp2 = buyerslist.get(i);
			temp3 = sellerslist.get(i);
			final int buyCount = temp2.getCount();
			final int sellCount = temp3.getCount();
			writeD(temp3.getObjectId());
			writeD(temp3.getItemId());
			writeH(1);
			if (sellCount > buyCount)
			{
				writeD(buyCount);
			}
			else
			{
				writeD(sellCount);
			}
			writeD(temp3.getStorePrice());
			writeH(2);
			if (sellCount > buyCount)
			{
				writeD(buyCount);
			}
			else
			{
				writeD(sellCount);
			}
			writeH(3);
			writeD(temp3.getOwnersPrice());
			if (buyCount > sellCount)
			{
				writeD(sellCount);
				continue;
			}
			writeD(buyCount);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__D1_PRIVATEBUYLISTBUY;
	}
}
