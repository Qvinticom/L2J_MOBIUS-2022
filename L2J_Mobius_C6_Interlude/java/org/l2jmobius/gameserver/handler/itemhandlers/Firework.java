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
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

public class Firework implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6403,
		6406,
		6407
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!playable.isPlayer())
		{
			return;
		}
		final Player player = playable.getActingPlayer();
		
		if (player.isCastingNow())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
			return;
		}
		
		if (player.inObserverMode())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isSitting())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isConfused())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isStunned())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isDead())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isAlikeDead())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int itemId = item.getItemId();
		if (itemId == 6403) // elven_firecracker, xml: 2023
		{
			final MagicSkillUse msu = new MagicSkillUse(playable, player, 2023, 1, 1, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			useFw(player, 2023, 1);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 6406) // firework, xml: 2024
		{
			final MagicSkillUse msu = new MagicSkillUse(playable, player, 2024, 1, 1, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			useFw(player, 2024, 1);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 6407) // large_firework, xml: 2025
		{
			final MagicSkillUse msu = new MagicSkillUse(playable, player, 2025, 1, 1, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			useFw(player, 2025, 1);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	public void useFw(Player player, int magicId, int level)
	{
		final Skill skill = SkillTable.getInstance().getSkill(magicId, level);
		if (skill != null)
		{
			player.useMagic(skill, false, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
