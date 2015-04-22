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
package com.l2jserver.gameserver.network.clientpackets.compound;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.request.CompoundRequest;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.compound.ExEnchantOneFail;
import com.l2jserver.gameserver.network.serverpackets.compound.ExEnchantOneOK;

/**
 * @author UnAfraid
 */
public class RequestNewEnchantPushOne extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		else if (activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_IN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		else if (activeChar.isProcessingTransaction() || activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		
		final CompoundRequest request = new CompoundRequest(activeChar);
		if (!activeChar.addRequest(request))
		{
			activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		
		// Make sure player owns this item.
		request.setItemOne(_objectId);
		final L2ItemInstance itemOne = request.getItemOne();
		if (itemOne == null)
		{
			activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// Not implemented or not able to merge!
		if ((itemOne.getItem().getCompoundItem() == 0) || (itemOne.getItem().getCompoundChance() == 0))
		{
			activeChar.sendPacket(ExEnchantOneOK.STATIC_PACKET);
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		activeChar.sendPacket(ExEnchantOneOK.STATIC_PACKET);
	}
}
