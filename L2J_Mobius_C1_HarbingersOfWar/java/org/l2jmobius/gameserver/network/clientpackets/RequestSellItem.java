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

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;

public class RequestSellItem extends ClientBasePacket
{
	public RequestSellItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		@SuppressWarnings("unused")
		final int listId = readD();
		final int count = readD();
		final Player activeChar = client.getActiveChar();
		// Item[] items = new Item[count];
		for (int i = 0; i < count; ++i)
		{
			final int objectId = readD();
			final int itemId = readD();
			final int cnt = readD();
			if ((activeChar.getInventory().getItem(objectId).getItemId() != itemId) || (activeChar.getInventory().getItem(objectId).getCount() < cnt))
			{
				continue;
			}
			final Item item = activeChar.getInventory().getItem(objectId);
			activeChar.addAdena((item.getItem().getReferencePrice() / 2) * cnt);
			activeChar.getInventory().destroyItem(objectId, cnt);
		}
		final StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}
}
