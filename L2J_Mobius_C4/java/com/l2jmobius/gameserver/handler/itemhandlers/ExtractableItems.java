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

import java.util.logging.Logger;

import com.l2jmobius.gameserver.datatables.ExtractableItemsData;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.L2ExtractableItem;
import com.l2jmobius.gameserver.model.L2ExtractableProductItem;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.util.Rnd;

/**
 * @author FBIagent 11/12/2006
 */
public class ExtractableItems implements IItemHandler
{
	private static Logger _log = Logger.getLogger(ItemTable.class.getName());
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		final int itemID = item.getItemId();
		final L2ExtractableItem exitem = ExtractableItemsData.getInstance().getExtractableItem(itemID);
		
		if (exitem == null)
		{
			return;
		}
		
		int createItemID = 0;
		int createAmount = 0;
		final int rndNum = Rnd.get(100);
		int chanceFrom = 0;
		
		// calculate extraction
		for (final L2ExtractableProductItem expi : exitem.getProductItemsArray())
		{
			final int chance = expi.getChance();
			
			if ((rndNum >= chanceFrom) && (rndNum <= (chance + chanceFrom)))
			{
				createItemID = expi.getId();
				createAmount = expi.getAmmount();
				break;
			}
			
			chanceFrom += chance;
		}
		
		if (createItemID <= 0)
		{
			activeChar.sendMessage("Nothing happened.");
			return;
		}
		
		if (ItemTable.getInstance().createDummyItem(createItemID) == null)
		{
			_log.warning("createItemID " + createItemID + " doesn't have template!");
			activeChar.sendMessage("Nothing happened.");
			return;
		}
		
		if (!activeChar.destroyItemByItemId("Extract", itemID, 1, activeChar.getTarget(), true))
		{
			return;
		}
		
		if (ItemTable.getInstance().createDummyItem(createItemID).isStackable())
		{
			activeChar.addItem("Extract", createItemID, createAmount, item, false);
		}
		else
		{
			for (int i = 0; i < createAmount; i++)
			{
				activeChar.addItem("Extract", createItemID, 1, item, false);
			}
		}
		
		SystemMessage sm;
		
		if (createAmount > 1)
		{
			sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
			sm.addItemName(createItemID);
			sm.addNumber(createAmount);
		}
		else
		{
			sm = new SystemMessage(SystemMessage.EARNED_ITEM);
			sm.addItemName(createItemID);
		}
		activeChar.sendPacket(sm);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ExtractableItemsData.getInstance().itemIDs();
	}
}