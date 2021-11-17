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
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

/**
 * @author l3x
 */
public class Harvester implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5125
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		
		if (CastleManorManager.getInstance().isDisabled())
		{
			return;
		}
		
		final Player player = (Player) playable;
		if (!(player.getTarget() instanceof Monster))
		{
			player.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Monster target = (Monster) player.getTarget();
		if ((target == null) || !target.isDead())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Skill skill = SkillTable.getInstance().getSkill(2098, 1); // harvesting skill
		player.useMagic(skill, false, false);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
