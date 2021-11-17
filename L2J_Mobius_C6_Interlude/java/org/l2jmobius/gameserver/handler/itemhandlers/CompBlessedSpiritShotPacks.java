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

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class CompBlessedSpiritShotPacks implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5146,
		5147,
		5148,
		5149,
		5150,
		5151,
		5262,
		5263,
		5264,
		5265,
		5266,
		5267
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) playable;
		final int itemId = item.getItemId();
		int itemToCreateId;
		int amount;
		if (itemId < 5200) // Normal Compressed Package of SpiritShots
		{
			itemToCreateId = itemId - 1199; // Gives id of matching item for this pack
			amount = 300;
		}
		else // Greater Compressed Package of Spirithots
		{
			itemToCreateId = itemId - 1315; // Gives id of matching item for this pack
			amount = 1000;
		}
		
		player.getInventory().destroyItem("Extract", item, player, null);
		player.getInventory().addItem("Extract", itemToCreateId, amount, player, item);
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
		sm.addItemName(itemToCreateId);
		sm.addNumber(amount);
		player.sendPacket(sm);
		
		player.sendPacket(new ItemList(player, false));
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
