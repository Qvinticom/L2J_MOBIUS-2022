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
package com.l2jmobius.gameserver.network.clientpackets.pledgeV2;

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.xml.impl.ClanShopData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ClanShopProductHolder;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.pledgeV2.ExPledgeItemBuy;

/**
 * @author Mobius
 */
public class RequestExPledgeItemBuy implements IClientIncomingPacket
{
	private int _itemId;
	private int _count;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_itemId = packet.readD();
		_count = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance activeChar = client.getActiveChar();
		if ((activeChar == null) || (activeChar.getClan() == null))
		{
			client.sendPacket(new ExPledgeItemBuy(1));
			return;
		}
		
		final ClanShopProductHolder product = ClanShopData.getInstance().getProduct(_itemId);
		if (product == null)
		{
			client.sendPacket(new ExPledgeItemBuy(1));
			return;
		}
		
		if (activeChar.getClan().getLevel() < product.getClanLevel())
		{
			client.sendPacket(new ExPledgeItemBuy(2));
			return;
		}
		
		final long slots = product.getTradeItem().getItem().isStackable() ? 1 : product.getTradeItem().getCount() * _count;
		final long weight = product.getTradeItem().getItem().getWeight() * product.getTradeItem().getCount() * _count;
		if (!activeChar.getInventory().validateWeight(weight) || !activeChar.getInventory().validateCapacity(slots))
		{
			client.sendPacket(new ExPledgeItemBuy(3));
			return;
		}
		
		if ((activeChar.getAdena() < (product.getAdena() * _count)) || (activeChar.getFame() < (product.getFame() * _count)))
		{
			client.sendPacket(new ExPledgeItemBuy(3));
			return;
		}
		
		if (product.getAdena() > 0)
		{
			activeChar.reduceAdena("ClanShop", product.getAdena() * _count, activeChar, true);
		}
		if (product.getFame() > 0)
		{
			activeChar.setFame(activeChar.getFame() - (product.getFame() * _count));
		}
		
		activeChar.addItem("ClanShop", _itemId, product.getCount() * _count, activeChar, true);
		client.sendPacket(new ExPledgeItemBuy(0));
	}
}