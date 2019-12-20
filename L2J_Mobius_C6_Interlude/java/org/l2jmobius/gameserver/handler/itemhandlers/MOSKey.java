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

import org.l2jmobius.gameserver.datatables.csv.DoorTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.DoorInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

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
	public static long _lastOpen = 0;
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		final int itemId = item.getItemId();
		
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		PlayerInstance player = (PlayerInstance) playable;
		WorldObject target = player.getTarget();
		
		if (!(target instanceof DoorInstance))
		{
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final DoorInstance door = (DoorInstance) target;
		
		if (!player.isInsideRadius(door, INTERACTION_DISTANCE, false, false))
		{
			player.sendMessage("Door is to far.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((player.getAbnormalEffect() > 0) || player.isInCombat())
		{
			player.sendMessage("You can`t use the key right now.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((_lastOpen + 1800000) > System.currentTimeMillis()) // 30 * 60 * 1000 = 1800000
		{
			player.sendMessage("You can`t use the key right now.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return;
		}
		
		if ((itemId == 8056) && ((door.getDoorId() == 23150003) || (door.getDoorId() == 23150004)))
		{
			DoorTable.getInstance().getDoor(23150003).openMe();
			DoorTable.getInstance().getDoor(23150004).openMe();
			DoorTable.getInstance().getDoor(23150003).onOpen();
			DoorTable.getInstance().getDoor(23150004).onOpen();
			player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
			_lastOpen = System.currentTimeMillis();
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
