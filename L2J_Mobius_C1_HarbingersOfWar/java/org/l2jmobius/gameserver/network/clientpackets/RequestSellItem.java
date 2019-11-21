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

import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;

public class RequestSellItem extends ClientBasePacket
{
	private static final String _C__1E_REQUESTSELLITEM = "[C] 1E RequestSellItem";
	
	public RequestSellItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		@SuppressWarnings("unused")
		int listId = readD();
		int count = readD();
		PlayerInstance activeChar = client.getActiveChar();
		// ItemInstance[] items = new ItemInstance[count];
		for (int i = 0; i < count; ++i)
		{
			int objectId = readD();
			int itemId = readD();
			int cnt = readD();
			if ((activeChar.getInventory().getItem(objectId).getItemId() != itemId) || (activeChar.getInventory().getItem(objectId).getCount() < cnt))
			{
				continue;
			}
			ItemInstance item = activeChar.getInventory().getItem(objectId);
			activeChar.addAdena((item.getItem().getReferencePrice() / 2) * cnt);
			activeChar.getInventory().destroyItem(objectId, cnt);
		}
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}
	
	@Override
	public String getType()
	{
		return _C__1E_REQUESTSELLITEM;
	}
}
