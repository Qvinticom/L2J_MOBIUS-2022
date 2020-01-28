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
package org.l2jmobius.gameserver.handler.itemhandlers;

import java.util.logging.Logger;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.datatables.ItemTable;
import org.l2jmobius.gameserver.datatables.csv.ExtractableItemData;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.ExtractableItem;
import org.l2jmobius.gameserver.model.ExtractableProductItem;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author FBIagent 11/12/2006
 */
public class ExtractableItems implements IItemHandler
{
	private static final Logger LOGGER = Logger.getLogger(ExtractableItems.class.getName());
	
	public void doExtract(Playable playable, ItemInstance item, int count)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		final PlayerInstance player = (PlayerInstance) playable;
		final int itemID = item.getItemId();
		
		if (count > item.getCount())
		{
			return;
		}
		while (count-- > 0)
		{
			final ExtractableItem exitem = ExtractableItemData.getInstance().getExtractableItem(itemID);
			if (exitem == null)
			{
				return;
			}
			int createItemID = 0;
			int createAmount = 0;
			final int rndNum = Rnd.get(100);
			int chanceFrom = 0;
			for (ExtractableProductItem expi : exitem.getProductItems())
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
			
			if (createItemID == 0)
			{
				player.sendMessage("Nothing happened.");
				return;
			}
			
			if (createItemID > 0)
			{
				if (ItemTable.getInstance().createDummyItem(createItemID) == null)
				{
					LOGGER.warning("createItemID " + createItemID + " doesn't have template!");
					player.sendMessage("Nothing happened.");
					return;
				}
				
				if (ItemTable.getInstance().createDummyItem(createItemID).isStackable())
				{
					player.addItem("Extract", createItemID, createAmount, item, false);
				}
				else
				{
					for (int i = 0; i < createAmount; i++)
					{
						player.addItem("Extract", createItemID, 1, item, false);
					}
				}
				SystemMessage sm;
				
				if (createAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(createItemID);
					sm.addNumber(createAmount);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(createItemID);
				}
				player.sendPacket(sm);
			}
			else
			{
				player.sendMessage("Item failed to open"); // TODO: Put a more proper message here.
			}
			
			player.destroyItemByItemId("Extract", itemID, 1, player.getTarget(), true);
		}
	}
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		if (item.getCount() > 1)
		{
			String message = HtmCache.getInstance().getHtm("data/html/others/extractable.htm");
			if (message == null)
			{
				doExtract(playable, item, 1);
			}
			else
			{
				message = message.replace("%objectId%", String.valueOf(item.getObjectId()));
				message = message.replace("%itemname%", item.getItemName());
				message = message.replace("%count%", String.valueOf(item.getCount()));
				playable.sendPacket(new NpcHtmlMessage(5, message));
			}
		}
		else
		{
			doExtract(playable, item, 1);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ExtractableItemData.getInstance().itemIDs();
	}
}