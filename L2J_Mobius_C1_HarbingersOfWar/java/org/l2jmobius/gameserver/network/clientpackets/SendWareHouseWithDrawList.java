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

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class SendWareHouseWithDrawList extends ClientBasePacket
{
	public SendWareHouseWithDrawList(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int i;
		final PlayerInstance activeChar = client.getActiveChar();
		final int itemCount = readD();
		int weight = 0;
		final ItemInstance[] items = new ItemInstance[itemCount];
		for (i = 0; i < itemCount; ++i)
		{
			final int itemId = readD();
			final int count = readD();
			final ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(count);
			items[i] = inst;
			weight += items[i].getItem().getWeight() * count;
		}
		if ((activeChar.getMaxLoad() - activeChar.getCurrentLoad()) >= weight)
		{
			for (i = 0; i < items.length; ++i)
			{
				activeChar.getInventory().addItem(items[i]);
				activeChar.getWarehouse().destroyItem(items[i].getItemId(), items[i].getCount());
			}
			final StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
			activeChar.sendPacket(su);
			activeChar.sendPacket(new ItemList(activeChar, false));
		}
		else
		{
			for (i = 0; i < items.length; ++i)
			{
				World.getInstance().removeVisibleObject(items[i]);
			}
			activeChar.sendPacket(new SystemMessage(SystemMessage.WEIGHT_LIMIT_EXCEEDED));
		}
	}
}
