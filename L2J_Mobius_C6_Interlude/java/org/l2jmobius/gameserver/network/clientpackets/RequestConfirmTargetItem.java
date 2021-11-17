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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExConfirmVariationItem;

/**
 * Format:(ch) d
 * @author -Wooden-
 */
public class RequestConfirmTargetItem implements IClientIncomingPacket
{
	private int _itemObjId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_itemObjId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		final Item item = (Item) World.getInstance().findObject(_itemObjId);
		if (item == null)
		{
			return;
		}
		
		if (player.getLevel() < 46)
		{
			player.sendMessage("You have to be level 46 in order to augment an item");
			return;
		}
		
		// check if the item is augmentable
		final int itemGrade = item.getItem().getItemGrade();
		final int itemType = item.getItem().getType2();
		if (item.isAugmented())
		{
			player.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
			return;
		}
		// TODO: can do better? : currently: using isdestroyable() as a check for hero / cursed weapons
		else if ((itemGrade < ItemTemplate.CRYSTAL_C) || (itemType != ItemTemplate.TYPE2_WEAPON) || !item.isDestroyable() || item.isShadowItem())
		{
			player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		
		// check if the player can augment
		if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return;
		}
		
		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return;
		}
		
		if (player.isParalyzed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return;
		}
		
		if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return;
		}
		
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return;
		}
		
		player.sendPacket(new ExConfirmVariationItem(_itemObjId));
		player.sendPacket(SystemMessageId.SELECT_THE_CATALYST_FOR_AUGMENTATION);
	}
}
