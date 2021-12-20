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
import org.l2jmobius.gameserver.model.StoreTradeList;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * sample 1d 1e 00 00 00 // ?? 5c 4a a0 7c // buy list id 02 00 // item count 04 00 // itemType1 0-weapon/ring/earring/necklace 1-armor/shield 4-item/questitem/adena 00 00 00 00 // objectid 32 04 00 00 // itemid 00 00 00 00 // count 05 00 // itemType2 0-weapon 1-shield/armor 2-ring/earring/necklace
 * 3-questitem 4-adena 5-item 00 00 60 09 00 00 // price 00 00 00 00 00 00 b6 00 00 00 00 00 00 00 00 00 00 00 80 00 // body slot these 4 values are only used if itemtype1 = 0 or 1 00 00 // 00 00 // 00 00 // 50 c6 0c 00 format dd h (h dddhh hhhh d) revision 377 format dd h (h dddhh dhhh d)
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class BuyList implements IClientOutgoingPacket
{
	private final int _listId;
	private final List<Item> _list;
	private final int _money;
	private double _taxRate = 0;
	
	public BuyList(StoreTradeList list, int currentMoney)
	{
		_listId = list.getListId();
		_list = list.getItems();
		_money = currentMoney;
	}
	
	public BuyList(StoreTradeList list, int currentMoney, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getItems();
		_money = currentMoney;
		_taxRate = taxRate;
	}
	
	public BuyList(List<Item> list, int listId, int currentMoney)
	{
		_listId = listId;
		_list = list;
		_money = currentMoney;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.BUY_LIST.writeId(packet);
		packet.writeD(_money); // current money
		packet.writeD(_listId);
		packet.writeH(_list.size());
		for (Item item : _list)
		{
			if ((item.getCount() > 0) || (item.getCount() == -1))
			{
				packet.writeH(item.getItem().getType1()); // item type1
				packet.writeD(item.getObjectId());
				packet.writeD(item.getItemId());
				if (item.getCount() < 0)
				{
					packet.writeD(0); // max amount of items that a player can buy at a time (with this itemid)
				}
				else
				{
					packet.writeD(item.getCount());
				}
				packet.writeH(item.getItem().getType2()); // item type2
				packet.writeH(0); // ?
				if (item.getItem().getType1() != ItemTemplate.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					packet.writeD(item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
					packet.writeH(item.getEnchantLevel()); // enchant level
					packet.writeH(0); // ?
					packet.writeH(0);
				}
				else
				{
					packet.writeD(0); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
					packet.writeH(0); // enchant level
					packet.writeH(0); // ?
					packet.writeH(0);
				}
				if ((item.getItemId() >= 3960) && (item.getItemId() <= 4026))
				{
					packet.writeD((int) (item.getPriceToSell() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
				}
				else
				{
					packet.writeD((int) (item.getPriceToSell() * (1 + _taxRate)));
				}
			}
		}
		return true;
	}
}
