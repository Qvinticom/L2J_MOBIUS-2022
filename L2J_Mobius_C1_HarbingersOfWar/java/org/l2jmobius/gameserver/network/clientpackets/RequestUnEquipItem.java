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

import java.util.Collection;

import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestUnEquipItem extends ClientBasePacket
{
	public RequestUnEquipItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int slot = readD();
		final PlayerInstance activeChar = client.getActiveChar();
		final Collection<ItemInstance> unequiped = activeChar.getInventory().unEquipItemInBodySlot(slot);
		final InventoryUpdate iu = new InventoryUpdate();
		for (ItemInstance element : unequiped)
		{
			iu.addModifiedItem(element);
		}
		activeChar.sendPacket(iu);
		activeChar.updatePDef();
		activeChar.updatePAtk();
		activeChar.updateMDef();
		activeChar.updateMAtk();
		final UserInfo ui = new UserInfo(activeChar);
		activeChar.sendPacket(ui);
		activeChar.setAttackStatus(false);
		final CharInfo info = new CharInfo(activeChar);
		activeChar.broadcastPacket(info);
		if (!unequiped.isEmpty())
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_DISARMED);
			sm.addItemName(unequiped.stream().findFirst().get().getItemId());
			activeChar.sendPacket(sm);
		}
	}
}
