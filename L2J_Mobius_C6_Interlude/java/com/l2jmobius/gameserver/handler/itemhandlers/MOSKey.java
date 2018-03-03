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

import com.l2jmobius.gameserver.datatables.csv.DoorTable;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.SocialAction;

/**
 * @author chris
 */
public class MOSKey implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		8056
	};
	public static final int INTERACTION_DISTANCE = 150;
	public static long LAST_OPEN = 0;
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		final int itemId = item.getItemId();
		
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();
		
		if (!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final L2DoorInstance door = (L2DoorInstance) target;
		
		if (!activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false))
		{
			activeChar.sendMessage("Door is to far.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((activeChar.getAbnormalEffect() > 0) || activeChar.isInCombat())
		{
			activeChar.sendMessage("You can`t use the key right now.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((LAST_OPEN + 1800000) > System.currentTimeMillis()) // 30 * 60 * 1000 = 1800000
		{
			activeChar.sendMessage("You can`t use the key right now.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return;
		}
		
		if (itemId == 8056)
		{
			if ((door.getDoorId() == 23150003) || (door.getDoorId() == 23150004))
			{
				DoorTable.getInstance().getDoor(23150003).openMe();
				DoorTable.getInstance().getDoor(23150004).openMe();
				DoorTable.getInstance().getDoor(23150003).onOpen();
				DoorTable.getInstance().getDoor(23150004).onOpen();
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
				LAST_OPEN = System.currentTimeMillis();
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
