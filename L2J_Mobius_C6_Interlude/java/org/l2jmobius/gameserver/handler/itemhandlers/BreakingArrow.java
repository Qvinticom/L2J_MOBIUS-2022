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

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class BreakingArrow implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		8192
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		final int itemId = item.getItemId();
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		final PlayerInstance player = (PlayerInstance) playable;
		final WorldObject target = player.getTarget();
		if (!(target instanceof GrandBossInstance))
		{
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final GrandBossInstance frintezza = (GrandBossInstance) target;
		if (!player.isInsideRadius(frintezza, 500, false, false))
		{
			player.sendMessage("The purpose is inaccessible");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if ((itemId == 8192) && (frintezza.getObjectId() == 29045))
		{
			frintezza.broadcastPacket(new SocialAction(frintezza.getObjectId(), 2));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}