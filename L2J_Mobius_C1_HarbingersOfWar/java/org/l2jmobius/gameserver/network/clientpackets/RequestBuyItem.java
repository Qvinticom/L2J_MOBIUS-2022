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

import java.io.IOException;

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.TradeController;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.Connection;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestBuyItem extends ClientBasePacket
{
	private static final String _C__1F_REQUESTBUYITEM = "[C] 1F RequestBuyItem";
	
	public RequestBuyItem(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		int i;
		final int listId = readD();
		final int count = readD();
		final ItemInstance[] items = new ItemInstance[count];
		for (int i2 = 0; i2 < count; ++i2)
		{
			final int itemId = readD();
			final int cnt = readD();
			final ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(cnt);
			items[i2] = inst;
		}
		final PlayerInstance activeChar = client.getActiveChar();
		final Connection con = client.getConnection();
		double neededMoney = 0.0;
		final long currentMoney = activeChar.getAdena();
		final TradeList list = TradeController.getInstance().getBuyList(listId);
		for (i = 0; i < items.length; ++i)
		{
			final double count2 = items[i].getCount();
			final int id = items[i].getItemId();
			int price = list.getPriceForItemId(id);
			if (price == -1)
			{
				_log.warning("ERROR, no price found .. wrong buylist ??");
				price = 1000000;
			}
			neededMoney += Math.abs(count2) * price;
		}
		if ((neededMoney > currentMoney) || (neededMoney < 0.0) || (currentMoney <= 0L))
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA);
			con.sendPacket(sm);
			return;
		}
		activeChar.reduceAdena((int) neededMoney);
		for (i = 0; i < items.length; ++i)
		{
			activeChar.getInventory().addItem(items[i]);
		}
		final ItemList il = new ItemList(activeChar, false);
		con.sendPacket(il);
		final StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}
	
	@Override
	public String getType()
	{
		return _C__1F_REQUESTBUYITEM;
	}
}
