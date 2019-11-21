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
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestUnEquipItem extends ClientBasePacket
{
	private static final String _C__11_REQUESTUNEQUIPITEM = "[C] 11 RequestUnequipItem";
	
	public RequestUnEquipItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int slot = readD();
		_log.fine("request unequip slot " + slot);
		PlayerInstance activeChar = client.getActiveChar();
		ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlot(slot);
		InventoryUpdate iu = new InventoryUpdate();
		for (ItemInstance element : unequiped)
		{
			iu.addModifiedItem(element);
		}
		activeChar.sendPacket(iu);
		activeChar.updatePDef();
		activeChar.updatePAtk();
		activeChar.updateMDef();
		activeChar.updateMAtk();
		UserInfo ui = new UserInfo(activeChar);
		activeChar.sendPacket(ui);
		activeChar.setAttackStatus(false);
		CharInfo info = new CharInfo(activeChar);
		activeChar.broadcastPacket(info);
		if (unequiped.length > 0)
		{
			SystemMessage sm = new SystemMessage(417);
			sm.addItemName(unequiped[0].getItemId());
			activeChar.sendPacket(sm);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__11_REQUESTUNEQUIPITEM;
	}
}
