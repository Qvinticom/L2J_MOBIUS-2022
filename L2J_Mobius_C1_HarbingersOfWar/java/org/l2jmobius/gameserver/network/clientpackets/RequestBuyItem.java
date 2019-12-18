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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.TradeController;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.MerchantInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestBuyItem extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(RequestBuyItem.class.getName());
	
	public RequestBuyItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int listId = readD();
		final int count = readD();
		final ItemInstance[] items = new ItemInstance[count];
		for (int i = 0; i < count; ++i)
		{
			final int itemId = readD();
			final int cnt = readD();
			final ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(cnt);
			items[i] = inst;
		}
		
		final PlayerInstance player = client.getActiveChar();
		
		// Prevent buying items far from merchant.
		if (!player.isGM())
		{
			if (!(player.getTarget() instanceof MerchantInstance))
			{
				return;
			}
			boolean found = false;
			for (WorldObject object : player.getKnownObjects())
			{
				if ((object instanceof MerchantInstance) && (player.calculateDistance2D(object) < 250))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				return;
			}
		}
		
		double neededMoney = 0;
		final long currentMoney = player.getAdena();
		final TradeList list = TradeController.getInstance().getBuyList(listId);
		for (ItemInstance item : items)
		{
			final double itemCount = item.getCount();
			final int id = item.getItemId();
			int price = list.getPriceForItemId(id);
			if (price == -1)
			{
				_log.warning("ERROR, no price found .. wrong buylist ??");
				price = 1000000;
			}
			neededMoney += Math.abs(itemCount) * price;
		}
		if ((neededMoney > currentMoney) || (neededMoney < 0) || (currentMoney <= 0))
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		player.reduceAdena((int) neededMoney);
		final SystemMessage sma = new SystemMessage(SystemMessage.DISSAPEARED_ADENA);
		sma.addNumber((int) neededMoney);
		player.sendPacket(sma);
		
		for (ItemInstance item : items)
		{
			player.getInventory().addItem(item);
			if (item.getCount() > 1)
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_S);
				sm.addItemName(item.getItemId());
				sm.addNumber(item.getCount());
				player.sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.EARNED_ITEM);
				sm.addItemName(item.getItemId());
				player.sendPacket(sm);
			}
		}
		
		player.sendPacket(new ItemList(player, false));
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
