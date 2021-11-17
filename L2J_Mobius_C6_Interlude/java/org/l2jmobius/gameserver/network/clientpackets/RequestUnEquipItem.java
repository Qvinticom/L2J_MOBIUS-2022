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

import java.util.List;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestUnEquipItem implements IClientIncomingPacket
{
	private int _slot;
	
	/**
	 * packet type id 0x11 format: cd
	 */
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_slot = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Item item = player.getInventory().getPaperdollItemByItemId(_slot);
		if ((item != null) && item.isWear())
		{
			// Wear-items are not to be unequipped
			return;
		}
		
		// Prevent of unequiping a cursed weapon
		if ((_slot == ItemTemplate.SLOT_LR_HAND) && player.isCursedWeaponEquiped())
		{
			// Message ?
			return;
		}
		
		// Prevent player from unequipping items in special conditions
		if (player.isStunned() || player.isConfused() || player.isParalyzed() || player.isSleeping() || player.isAlikeDead())
		{
			player.sendMessage("Your status does not allow you to do that.");
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			return;
		}
		
		if (player.isMoving() && player.isAttackingNow() && ((_slot == ItemTemplate.SLOT_LR_HAND) || (_slot == ItemTemplate.SLOT_L_HAND) || (_slot == ItemTemplate.SLOT_R_HAND)))
		{
			final WorldObject target = player.getTarget();
			player.setTarget(null);
			player.stopMove(null);
			player.setTarget(target);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK);
		}
		
		// Remove augmentation bonus
		if ((item != null) && item.isAugmented())
		{
			item.getAugmentation().removeBonus(player);
		}
		
		final List<Item> unequipped = player.getInventory().unEquipItemInBodySlotAndRecord(_slot);
		
		// show the update in the inventory
		final InventoryUpdate iu = new InventoryUpdate();
		for (Item itm : unequipped)
		{
			player.checkSSMatch(null, itm);
			iu.addModifiedItem(itm);
		}
		player.sendPacket(iu);
		player.broadcastUserInfo();
		
		// this can be 0 if the user pressed the right mouse button twice very fast
		if (!unequipped.isEmpty())
		{
			SystemMessage sm = null;
			final Item unequippedItem = unequipped.get(0);
			if (unequippedItem.getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
				sm.addNumber(unequippedItem.getEnchantLevel());
				sm.addItemName(unequippedItem.getItemId());
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DISARMED);
				sm.addItemName(unequippedItem.getItemId());
			}
			player.sendPacket(sm);
		}
	}
}
