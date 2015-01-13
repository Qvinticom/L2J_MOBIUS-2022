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
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.serverpackets.ExRemoveEnchantSupportItemResult;

/**
 * @author Sdw
 */
public class RequestExRemoveEnchantSupportItem extends L2GameClientPacket
{
	private static final String _C__D0_E4_REQUESTEXREMOVEENCHANTSUPPORTITEM = "[C] D0:E4 RequestExRemoveEnchantSupportItem";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
		
		final L2ItemInstance supportItem = activeChar.getInventory().getItemByItemId(activeChar.getActiveEnchantSupportItemId());
		
		if ((supportItem == null) || (supportItem.getCount() < 1))
		{
			activeChar.sendPacket(ExRemoveEnchantSupportItemResult.STATIC_PACKET);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_E4_REQUESTEXREMOVEENCHANTSUPPORTITEM;
	}
}
