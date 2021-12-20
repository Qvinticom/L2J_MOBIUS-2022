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
import java.util.Collection;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.buylist.BuyListHolder;
import org.l2jmobius.gameserver.model.buylist.Product;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author ShanSoft
 */
public class ExBuySellList implements IClientOutgoingPacket
{
	private final int _buyListId;
	private final List<Product> _buyList = new ArrayList<>();
	private final long _money;
	private double _taxRate = 0;
	private Collection<Item> _sellList = null;
	private Collection<Item> _refundList = null;
	private final boolean _done;
	
	public ExBuySellList(Player player, BuyListHolder list, boolean done)
	{
		_money = player.getAdena();
		_buyListId = list.getListId();
		for (Product item : list.getProducts())
		{
			if (item.hasLimitedStock() && (item.getCount() <= 0))
			{
				continue;
			}
			_buyList.add(item);
		}
		_sellList = player.getInventory().getAvailableItems(false, false, false);
		if (player.hasRefund())
		{
			_refundList = player.getRefund().getItems();
		}
		_done = done;
	}
	
	public ExBuySellList(Player player, BuyListHolder list, double taxRate, boolean done)
	{
		_money = player.getAdena();
		_taxRate = taxRate;
		_buyListId = list.getListId();
		for (Product item : list.getProducts())
		{
			if (item.hasLimitedStock() && (item.getCount() <= 0))
			{
				continue;
			}
			_buyList.add(item);
		}
		_sellList = player.getInventory().getAvailableItems(false, false, false);
		if (player.hasRefund())
		{
			_refundList = player.getRefund().getItems();
		}
		_done = done;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_BUY_SELL_LIST.writeId(packet);
		packet.writeQ(_money);
		packet.writeD(_buyListId);
		packet.writeH(_buyList.size());
		for (Product item : _buyList)
		{
			packet.writeH(item.getItem().getType1());
			packet.writeD(0); // objectId
			packet.writeD(item.getItemId());
			packet.writeQ(item.getCount() < 0 ? 0 : item.getCount());
			packet.writeH(item.getItem().getType2());
			packet.writeH(0); // ?
			if (item.getItem().getType1() != ItemTemplate.TYPE1_ITEM_QUESTITEM_ADENA)
			{
				packet.writeD(item.getItem().getBodyPart());
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
			if ((item.getItemId() >= 3960) && (item.getItemId() <= 4026))
			{
				packet.writeQ((long) (item.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE));
			}
			else
			{
				packet.writeQ((long) (item.getPrice() * (1 + (_taxRate / 2))));
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
		if ((_sellList != null) && !_sellList.isEmpty())
		{
			packet.writeH(_sellList.size());
			for (Item item : _sellList)
			{
				packet.writeH(item.getItem().getType1());
				packet.writeD(item.getObjectId());
				packet.writeD(item.getId());
				packet.writeQ(item.getCount());
				packet.writeH(item.getItem().getType2());
				packet.writeH(0);
				packet.writeD(item.getItem().getBodyPart());
				packet.writeH(item.getEnchantLevel());
				packet.writeH(0);
				packet.writeH(0);
				packet.writeQ(Config.MERCHANT_ZERO_SELL_PRICE ? 0 : item.getItem().getReferencePrice() / 2);
				// T1
				packet.writeH(item.getAttackElementType());
				packet.writeH(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					packet.writeH(item.getElementDefAttr(i));
				}
				packet.writeH(0); // Enchant effect 1
				packet.writeH(0); // Enchant effect 2
				packet.writeH(0); // Enchant effect 3
			}
		}
		else
		{
			packet.writeH(0);
		}
		if ((_refundList != null) && !_refundList.isEmpty())
		{
			packet.writeH(_refundList.size());
			int idx = 0;
			for (Item item : _refundList)
			{
				packet.writeD(idx++);
				packet.writeD(item.getId());
				packet.writeQ(item.getCount());
				packet.writeH(item.getItem().getType2());
				packet.writeH(0); // ?
				packet.writeH(item.getEnchantLevel());
				packet.writeH(0); // ?
				packet.writeQ(Config.MERCHANT_ZERO_SELL_PRICE ? 0 : (item.getItem().getReferencePrice() / 2) * item.getCount());
				// T1
				packet.writeH(item.getAttackElementType());
				packet.writeH(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					packet.writeH(item.getElementDefAttr(i));
				}
				packet.writeH(0); // Enchant effect 1
				packet.writeH(0); // Enchant effect 2
				packet.writeH(0); // Enchant effect 3
			}
		}
		else
		{
			packet.writeH(0);
		}
		packet.writeC(_done ? 1 : 0);
		return true;
	}
}
