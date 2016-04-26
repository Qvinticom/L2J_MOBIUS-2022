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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2PremiumItem;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExGetPremiumItemList;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author Gnacik
 */
public final class RequestWithDrawPremiumItem extends L2GameClientPacket
{
	private static final String _C__D0_52_REQUESTWITHDRAWPREMIUMITEM = "[C] D0:52 RequestWithDrawPremiumItem";
	
	private int _itemNum;
	// private int _charId;
	private long _itemCount;
	
	@Override
	protected void readImpl()
	{
		_itemNum = (int) readQ();
		// _charId = readD();
		_itemCount = readQ();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || (_itemCount <= 0))
		{
			return;
		}
		
		if (activeChar.getPremiumItemList().isEmpty())
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Player: " + activeChar.getName() + " try to get item with empty list!", Config.DEFAULT_PUNISH);
			return;
		}
		if ((activeChar.getWeightPenalty() >= 3) || !activeChar.isInventoryUnder90(false))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_THE_DIMENSIONAL_ITEM_BECAUSE_YOU_HAVE_EXCEED_YOUR_INVENTORY_WEIGHT_QUANTITY_LIMIT);
			return;
		}
		if (activeChar.isProcessingTransaction())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_A_DIMENSIONAL_ITEM_DURING_AN_EXCHANGE);
			return;
		}
		
		final L2PremiumItem _item = activeChar.getPremiumItemList().get(_itemNum);
		if ((_item == null) || (_item.getCount() < _itemCount))
		{
			return;
		}
		
		final long itemsLeft = _item.getCount() - _itemCount;
		
		activeChar.addItem("PremiumItem", _item.getItemId(), _itemCount, activeChar.getTarget(), true);
		
		if (itemsLeft > 0)
		{
			_item.updateCount(itemsLeft);
			activeChar.updatePremiumItem(_itemNum, itemsLeft);
		}
		else
		{
			activeChar.getPremiumItemList().remove(_itemNum);
			activeChar.deletePremiumItem(_itemNum);
		}
		
		if (activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_DIMENSIONAL_ITEMS_TO_BE_FOUND);
		}
		else
		{
			activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_52_REQUESTWITHDRAWPREMIUMITEM;
	}
}
