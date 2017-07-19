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
package com.l2jmobius.gameserver.handler.itemhandlers;

import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;

/**
 * @author AlterEgo
 */
public class ChestKey implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;
	
	private static int[] _itemIds =
	{
		5197,
		5198,
		5199,
		5200,
		5201,
		5202,
		5203,
		5204, // chest key
		6665,
		6666,
		6667,
		6668,
		6669,
		6670,
		6671,
		6672 // deluxe key
	};
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemId = item.getItemId();
		final L2Skill skill = SkillTable.getInstance().getInfo(2229, itemId - 6664); // box key skill
		final L2Object target = activeChar.getTarget();
		
		if (!(target instanceof L2ChestInstance))
		{
			activeChar.sendMessage("Invalid target.");
			activeChar.sendPacket(new ActionFailed());
		}
		else
		{
			final L2ChestInstance chest = (L2ChestInstance) target;
			if (chest.isDead() || chest.isInteracted())
			{
				activeChar.sendMessage("The chest is empty.");
				activeChar.sendPacket(new ActionFailed());
				return;
			}
			
			activeChar.useMagic(skill, false, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}