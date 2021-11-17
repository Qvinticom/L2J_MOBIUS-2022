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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class NobleCustomItem implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		Config.NOOBLE_CUSTOM_ITEM_ID
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (Config.NOBLE_CUSTOM_ITEMS)
		{
			if (!(playable instanceof Player))
			{
				return;
			}
			
			final Player player = (Player) playable;
			if (player.isInOlympiadMode())
			{
				player.sendMessage("This item cannot be used in Olympiad mode.");
			}
			
			if (player.isNoble())
			{
				player.sendMessage("You are already a noblesse!");
			}
			else
			{
				player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
				player.setNoble(true);
				player.sendMessage("You are now a noble, you have been granted noblesse status and skills.");
				player.broadcastUserInfo();
				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
				player.getInventory().addItem("NobleCustomItem", 7694, 1, player, null);
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
