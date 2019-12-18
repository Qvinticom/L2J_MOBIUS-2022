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

import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class UseItem extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(UseItem.class.getName());
	
	public UseItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int objectId = readD();
		
		final PlayerInstance activeChar = client.getActiveChar();
		final ItemInstance item = activeChar.getInventory().getItem(objectId);
		if ((item != null) && item.isEquipable() && !activeChar.isInCombat())
		{
			final List<ItemInstance> items = activeChar.getInventory().equipItem(item);
			if (item.getItem().getType2() == 0)
			{
				activeChar.updatePAtk();
				activeChar.updateMAtk();
			}
			else if (item.getItem().getType2() == 1)
			{
				activeChar.updatePDef();
			}
			else if (item.getItem().getType2() == 2)
			{
				activeChar.updateMDef();
			}
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_EQUIPPED);
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
			final InventoryUpdate iu = new InventoryUpdate(items);
			activeChar.sendPacket(iu);
			final UserInfo ui = new UserInfo(activeChar);
			activeChar.sendPacket(ui);
			activeChar.setAttackStatus(false);
			final CharInfo info = new CharInfo(activeChar);
			activeChar.broadcastPacket(info);
		}
		else if (item != null)
		{
			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
			if (handler == null)
			{
				_log.warning("no itemhandler registered for itemId:" + item.getItemId());
			}
			else
			{
				final int count = handler.useItem(activeChar, item);
				if (count > 0)
				{
					final ItemInstance itemInstance = activeChar.getInventory().destroyItem(item.getObjectId(), count);
					final InventoryUpdate iu = new InventoryUpdate();
					if (itemInstance.getCount() == 0)
					{
						iu.addRemovedItem(itemInstance);
					}
					else
					{
						iu.addModifiedItem(itemInstance);
					}
					activeChar.sendPacket(iu);
				}
			}
		}
	}
}
