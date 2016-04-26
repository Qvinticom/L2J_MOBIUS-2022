/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.data.xml.impl.EnchantItemData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.EnchantItemRequest;
import com.l2jmobius.gameserver.model.items.enchant.EnchantScroll;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExPutEnchantScrollItemResult;

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
		
		final EnchantItemRequest request = activeChar.getRequest(EnchantItemRequest.class);
		if ((request == null) || request.isProcessing())
		{
			return;
		}
		
		request.setEnchantingItem(_enchantObjectId);
		request.setEnchantingScroll(_scrollObjectId);
		
		final L2ItemInstance item = request.getEnchantingItem();
		final L2ItemInstance scroll = request.getEnchantingScroll();
		if ((item == null) || (scroll == null))
		{
			// message may be custom
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendPacket(new ExPutEnchantScrollItemResult(0));
			request.setEnchantingItem(L2PcInstance.ID_NONE);
			request.setEnchantingScroll(L2PcInstance.ID_NONE);
			return;
		}
		
		final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
		if (scrollTemplate == null)
		{
			// message may be custom
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendPacket(new ExPutEnchantScrollItemResult(0));
			request.setEnchantingScroll(L2PcInstance.ID_NONE);
			return;
		}
		
		request.setTimestamp(System.currentTimeMillis());
		activeChar.sendPacket(new ExPutEnchantScrollItemResult(_scrollObjectId));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_E8_REQUESTEXADDENCHANTSCROLLITEM;
	}
}
