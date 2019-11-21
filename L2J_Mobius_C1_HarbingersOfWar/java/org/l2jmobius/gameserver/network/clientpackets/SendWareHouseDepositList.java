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
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.Connection;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class SendWareHouseDepositList extends ClientBasePacket
{
	private static final String _C__31_SENDWAREHOUSEDEPOSITLIST = "[C] 31 SendWareHouseDepositList";
	
	public SendWareHouseDepositList(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		int i;
		PlayerInstance activeChar = client.getActiveChar();
		Connection con = client.getConnection();
		int count = readD();
		int price = 30;
		int neededMoney = count * price;
		int currentMoney = activeChar.getAdena();
		if (neededMoney > currentMoney)
		{
			SystemMessage sm = new SystemMessage(279);
			con.sendPacket(sm);
			return;
		}
		ItemInstance[] items = new ItemInstance[count];
		for (i = 0; i < count; ++i)
		{
			int itemId = readD();
			int cnt = readD();
			ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(cnt);
			items[i] = inst;
		}
		neededMoney = 0;
		for (i = 0; i < items.length; ++i)
		{
			if (items[i].getItemId() == 57)
			{
				continue;
			}
			activeChar.getWarehouse().addItem(items[i]);
			activeChar.getInventory().destroyItemByItemId(items[i].getItemId(), items[i].getCount());
			neededMoney += price;
		}
		activeChar.reduceAdena(neededMoney);
		ItemList il = new ItemList(activeChar, false);
		con.sendPacket(il);
	}
	
	@Override
	public String getType()
	{
		return _C__31_SENDWAREHOUSEDEPOSITLIST;
	}
}
