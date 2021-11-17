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

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.model.item.instance.Item;
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
	public void useItem(Playable playable, Item item)
	{
		final int itemId = item.getItemId();
		if (!(playable instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) playable;
		final WorldObject target = player.getTarget();
		if (!(target instanceof Door))
		{
			player.sendPacket(SystemMessageId.INVALID_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final Door door = (Door) target;
		if (!player.isInsideRadius2D(door, INTERACTION_DISTANCE))
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
		
		if ((_lastOpen + 1800000) > Chronos.currentTimeMillis()) // 30 * 60 * 1000 = 1800000
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
			DoorData.getInstance().getDoor(23150003).openMe();
			DoorData.getInstance().getDoor(23150004).openMe();
			DoorData.getInstance().getDoor(23150003).onOpen();
			DoorData.getInstance().getDoor(23150004).onOpen();
			player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
			_lastOpen = Chronos.currentTimeMillis();
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
