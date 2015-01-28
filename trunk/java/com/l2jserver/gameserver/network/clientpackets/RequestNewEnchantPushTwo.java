/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.serverpackets.ExEnchantTwoFail;
import com.l2jserver.gameserver.network.serverpackets.ExEnchantTwoOK;

/**
 * @author Erlandys
 */
public final class RequestNewEnchantPushTwo extends L2GameClientPacket
{
	private static final String _C__D0_F6_REQUESTNEWENCHANTPUSHTWO = "[C] D0:F6 RequestNewEnchantPushTwo";
	
	private int _itemId;
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemId);
		if (item == null)
		{
			return;
		}
		final int firstCompoundOID = activeChar.getFirstCompoundOID();
		final L2ItemInstance firstItem = activeChar.getInventory().getItemByObjectId(firstCompoundOID);
		if ((item.getItem().getBodyPart() != L2Item.SLOT_BROOCH_JEWEL) || ((firstItem != null) && ((firstItem.getObjectId() == item.getObjectId()) || (firstItem.getId() != item.getId()))) || ((item.getId() == 38931) || ((item.getId() % 10) == 4) || ((item.getId() % 10) == 9)))
		{
			activeChar.sendPacket(new ExEnchantTwoFail());
		}
		else
		{
			activeChar.setSecondCompoundOID(_itemId);
			activeChar.sendPacket(new ExEnchantTwoOK());
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_F6_REQUESTNEWENCHANTPUSHTWO;
	}
}
