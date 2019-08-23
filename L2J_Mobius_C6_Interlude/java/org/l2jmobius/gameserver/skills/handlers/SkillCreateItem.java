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
package org.l2jmobius.gameserver.skills.handlers;

import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.idfactory.IdFactory;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatsSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Nemesiss
 */
public class SkillCreateItem extends Skill
{
	private final List<Integer> _createItemId;
	private final int _createItemCount;
	private final int _randomCount;
	
	public SkillCreateItem(StatsSet set)
	{
		super(set);
		_createItemId = set.getList("create_item_id", Integer.class);
		_createItemCount = set.getInt("create_item_count", 0);
		_randomCount = set.getInt("random_count", 1);
	}
	
	@Override
	public void useSkill(Creature creature, WorldObject[] targets)
	{
		if (creature.isAlikeDead())
		{
			return;
		}
		if ((_createItemId == null) || (_createItemCount == 0))
		{
			// player.sendPacket(new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE));
			return;
		}
		final PlayerInstance player = (PlayerInstance) creature;
		if (creature instanceof PlayerInstance)
		{
			final int count = _createItemCount * (Rnd.get(_randomCount) + 1);
			final int rndId = Rnd.get(_createItemId.size());
			giveItems(player, _createItemId.get(rndId), count);
		}
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 */
	public void giveItems(PlayerInstance player, int itemId, int count)
	{
		final ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setCount(count);
		player.getInventory().addItem("Skill", item, player, player);
		
		if (count > 1)
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			smsg.addItemName(item.getItemId());
			smsg.addNumber(count);
			player.sendPacket(smsg);
		}
		else
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
			smsg.addItemName(item.getItemId());
			player.sendPacket(smsg);
		}
		final ItemList il = new ItemList(player, false);
		player.sendPacket(il);
	}
}
