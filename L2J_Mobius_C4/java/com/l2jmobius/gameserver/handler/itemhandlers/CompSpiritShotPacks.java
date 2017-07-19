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
package com.l2jmobius.gameserver.handler.itemhandlers;

import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:30:07 $
 */

public class CompSpiritShotPacks implements IItemHandler
{
	private static int[] _itemIds =
	{
		5140,
		5141,
		5142,
		5143,
		5144,
		5145,
		5256,
		5257,
		5258,
		5259,
		5260,
		5261
	};
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		final int itemId = item.getItemId();
		int itemToCreateId;
		int amount;
		
		if (itemId < 5200)
		{ // Normal Compressed Package of SpiritShots
			itemToCreateId = itemId - 2631; // Gives id of matching item for this pack
			amount = 300;
		}
		else
		{ // Greater Compressed Package of Spirithots
			itemToCreateId = itemId - 2747; // Gives id of matching item for this pack
			amount = 1000;
		}
		
		activeChar.getInventory().destroyItem("Extract", item, activeChar, null);
		activeChar.getInventory().addItem("Extract", itemToCreateId, amount, activeChar, item);
		
		final SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
		sm.addItemName(itemToCreateId);
		sm.addNumber(amount);
		activeChar.sendPacket(sm);
		
		final ItemList playerUI = new ItemList(activeChar, false);
		activeChar.sendPacket(playerUI);
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
