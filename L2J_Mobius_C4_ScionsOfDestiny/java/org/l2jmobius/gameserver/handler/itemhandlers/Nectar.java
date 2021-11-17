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

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Gourd;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;

public class Nectar implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6391
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) playable;
		if (!(player.getTarget() instanceof Gourd))
		{
			player.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}
		
		if (!player.getName().equalsIgnoreCase(((Gourd) player.getTarget()).getOwner()))
		{
			player.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}
		
		final WorldObject[] targets = new WorldObject[1];
		targets[0] = player.getTarget();
		
		final int itemId = item.getItemId();
		if (itemId == 6391)
		{
			player.useMagic(SkillTable.getInstance().getSkill(9998, 1), false, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
