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

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestDestroyItem extends ClientBasePacket
{
	private static final String _C__59_REQUESTDESTROYITEM = "[C] 59 RequestDestroyItem";
	
	public RequestDestroyItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		InventoryUpdate iu;
		ItemInstance itemToRemove;
		int objectId = readD();
		int count = readD();
		PlayerInstance activeChar = client.getActiveChar();
		if (count == 0)
		{
			return;
		}
		if (count > activeChar.getInventory().getItem(objectId).getCount())
		{
			count = activeChar.getInventory().getItem(objectId).getCount();
		}
		if ((itemToRemove = activeChar.getInventory().getItem(objectId)).isEquipped())
		{
			ItemInstance[] unequiped = activeChar.getInventory().unEquipItemOnPaperdoll(itemToRemove.getEquipSlot());
			iu = new InventoryUpdate();
			for (ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			activeChar.updatePDef();
			activeChar.updatePAtk();
			activeChar.updateMDef();
			activeChar.updateMAtk();
		}
		ItemInstance removedItem = activeChar.getInventory().destroyItem(objectId, count);
		iu = new InventoryUpdate();
		if (removedItem.getCount() == 0)
		{
			iu.addRemovedItem(removedItem);
		}
		else
		{
			iu.addModifiedItem(removedItem);
		}
		activeChar.sendPacket(iu);
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		UserInfo ui = new UserInfo(activeChar);
		activeChar.sendPacket(ui);
		CharInfo info = new CharInfo(activeChar);
		activeChar.broadcastPacket(info);
		World world = World.getInstance();
		world.removeObject(removedItem);
	}
	
	@Override
	public String getType()
	{
		return _C__59_REQUESTDESTROYITEM;
	}
}
