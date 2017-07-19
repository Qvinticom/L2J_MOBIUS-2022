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
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class EnergyStone implements IItemHandler
{
	private static int[] _itemIds =
	{
		5589
	};
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		
		L2PcInstance activeChar = null;
		if (playable instanceof L2PcInstance)
		{
			activeChar = (L2PcInstance) playable;
		}
		else if (playable instanceof L2PetInstance)
		{
			activeChar = ((L2PetInstance) playable).getOwner();
		}
		
		if (activeChar == null)
		{
			return;
		}
		
		if (item.getItemId() != 5589)
		{
			return;
		}
		
		if (activeChar.isAllSkillsDisabled())
		{
			return;
		}
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_MOVE_SITTING));
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(2165, 1);
		if (skill != null)
		{
			activeChar.useMagic(skill, false, false);
		}
		
		activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
		
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}