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

import org.l2jmobius.gameserver.datatables.SkillTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.MonsterInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
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
	PlayerInstance _player;
	MonsterInstance _target;
	
	@Override
	public void useItem(Playable playable, ItemInstance _item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		if (CastleManorManager.getInstance().isDisabled())
		{
			return;
		}
		
		_player = (PlayerInstance) playable;
		if ((_player.getTarget() == null) || !(_player.getTarget() instanceof MonsterInstance))
		{
			_player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		_target = (MonsterInstance) _player.getTarget();
		if ((_target == null) || !_target.isDead())
		{
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Skill skill = SkillTable.getInstance().getInfo(2098, 1); // harvesting skill
		_player.useMagic(skill, false, false);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
