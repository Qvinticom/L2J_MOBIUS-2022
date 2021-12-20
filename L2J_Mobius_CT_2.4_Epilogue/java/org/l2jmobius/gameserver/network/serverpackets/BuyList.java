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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.buylist.BuyListHolder;
import org.l2jmobius.gameserver.model.buylist.Product;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class BuyList implements IClientOutgoingPacket
{
	private final int _listId;
	private final Collection<Product> _list;
	private final long _money;
	private double _taxRate = 0;
	
	public BuyList(BuyListHolder list, long currentMoney, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = currentMoney;
		_taxRate = taxRate;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_BUY_SELL_LIST.writeId(packet); // writeC(7) ?
		packet.writeQ(_money); // current money
		packet.writeD(_listId);
		packet.writeH(_list.size());
		for (Product product : _list)
		{
			if ((product.getCount() > 0) || !product.hasLimitedStock())
			{
				packet.writeH(product.getItem().getType1()); // item type1
				packet.writeD(0); // objectId
				packet.writeD(product.getItemId());
				packet.writeQ(product.getCount() < 0 ? 0 : product.getCount());
				packet.writeH(product.getItem().getType2());
				packet.writeH(0); // isEquipped
				if (product.getItem().getType1() != ItemTemplate.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					packet.writeD(product.getItem().getBodyPart());
					packet.writeH(0); // item enchant level
					packet.writeH(0); // ?
					packet.writeH(0);
				}
				else
				{
					packet.writeD(0);
					packet.writeH(0);
					packet.writeH(0);
					packet.writeH(0);
				}
				if ((product.getItemId() >= 3960) && (product.getItemId() <= 4026))
				{
					packet.writeQ((long) (product.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
				}
				else
				{
					packet.writeQ((long) (product.getPrice() * (1 + _taxRate)));
				}
				// T1
				for (byte i = 0; i < 8; i++)
				{
					packet.writeH(0);
				}
				packet.writeH(0); // Enchant effect 1
				packet.writeH(0); // Enchant effect 2
				packet.writeH(0); // Enchant effect 3
			}
		}
		return true;
	}
}
