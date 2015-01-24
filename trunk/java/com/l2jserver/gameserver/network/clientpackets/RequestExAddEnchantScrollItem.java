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

import com.l2jserver.gameserver.data.xml.impl.EnchantItemData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.enchant.EnchantScroll;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExPutEnchantScrollItemResult;

/**
 * @author Sdw
 */
public class RequestExAddEnchantScrollItem extends L2GameClientPacket
{
	private static final String _C__D0_E8_REQUESTEXADDENCHANTSCROLLITEM = "[C] D0:E8 RequestExAddEnchantScrollItem";
	
	private int _scrollObjectId;
	private int _enchantObjectId;
	
	@Override
	protected void readImpl()
	{
		_scrollObjectId = readD();
		_enchantObjectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isEnchanting())
		{
			final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_enchantObjectId);
			final L2ItemInstance scroll = activeChar.getInventory().getItemByObjectId(_scrollObjectId);
			
			if ((item == null) || (scroll == null))
			{
				// message may be custom
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
				return;
			}
			
			final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
			
			if ((scrollTemplate == null))
			{
				// message may be custom
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.sendPacket(new ExPutEnchantScrollItemResult(0));
				return;
			}
			activeChar.sendPacket(new ExPutEnchantScrollItemResult(_scrollObjectId));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_E8_REQUESTEXADDENCHANTSCROLLITEM;
	}
}
