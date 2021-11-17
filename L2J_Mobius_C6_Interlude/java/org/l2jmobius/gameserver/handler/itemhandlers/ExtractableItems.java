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
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.xml.ExtractableItemData;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.ExtractableItem;
import org.l2jmobius.gameserver.model.ExtractableProductItem;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author FBIagent 11/12/2006
 */
public class ExtractableItems implements IItemHandler
{
	private static final Logger LOGGER = Logger.getLogger(ExtractableItems.class.getName());
	
	private static final int[] ITEM_IDS = ExtractableItemData.getInstance().getAllItemIds();
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		final Player player = (Player) playable;
		final int itemId = item.getItemId();
		final ExtractableItem extractable = ExtractableItemData.getInstance().getExtractableItem(itemId);
		if (extractable == null)
		{
			return;
		}
		
		// Destroy item first.
		player.destroyItemByItemId("Extract", itemId, 1, player.getTarget(), true);
		int createItemId = 0;
		int createAmount = 0;
		float chanceFrom = 0;
		final float random = Rnd.get(100);
		for (ExtractableProductItem expi : extractable.getProductItems())
		{
			final float chance = expi.getChance();
			if ((random >= chanceFrom) && (random <= (chance + chanceFrom)))
			{
				createItemId = expi.getId();
				createAmount = expi.getAmmount();
				break;
			}
			chanceFrom += chance;
		}
		
		if (createItemId > 0)
		{
			if (ItemTable.getInstance().createDummyItem(createItemId) == null)
			{
				LOGGER.warning("Extractable item with id " + createItemId + " does not have a template!");
				player.sendMessage("Nothing happened.");
				return;
			}
			
			if (ItemTable.getInstance().createDummyItem(createItemId).isStackable())
			{
				player.addItem("Extract", createItemId, createAmount, item, false);
			}
			else
			{
				for (int i = 0; i < createAmount; i++)
				{
					player.addItem("Extract", createItemId, 1, item, false);
				}
			}
			
			SystemMessage sm;
			if (createAmount > 1)
			{
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
				sm.addItemName(createItemId);
				sm.addNumber(createAmount);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
				sm.addItemName(createItemId);
			}
			player.sendPacket(sm);
		}
		else
		{
			player.sendMessage("Nothing happened.");
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}