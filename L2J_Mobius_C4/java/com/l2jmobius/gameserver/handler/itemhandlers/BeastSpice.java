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
import com.l2jmobius.gameserver.model.actor.instance.L2FeedableBeastInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class BeastSpice implements IItemHandler
{
	// Golden Spice, Crystal Spice
	private static int[] _itemIds =
	{
		6643,
		6644
	};
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!(activeChar.getTarget() instanceof L2FeedableBeastInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
			return;
		}
		
		final L2Object[] targets = new L2Object[1];
		targets[0] = activeChar.getTarget();
		
		final int itemId = item.getItemId();
		
		// Golden Spice
		if (itemId == 6643)
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(2188, 1), false, false);
		}
		else if (itemId == 6644)
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(2189, 1), false, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}