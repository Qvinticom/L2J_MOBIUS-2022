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
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Chest;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public class ChestKey implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;
	
	private static final int[] ITEM_IDS =
	{
		6665,
		6666,
		6667,
		6668,
		6669,
		6670,
		6671,
		6672, // deluxe key
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
		final WorldObject target = player.getTarget();
		if (!(target instanceof Chest))
		{
			player.sendPacket(SystemMessageId.INVALID_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			final Chest chest = (Chest) target;
			if (chest.isDead() || chest.isInteracted())
			{
				player.sendMessage("The chest is empty.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			final Skill skill = SkillTable.getInstance().getSkill(2229, itemId - 6664); // box key skill
			player.useMagic(skill, false, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
