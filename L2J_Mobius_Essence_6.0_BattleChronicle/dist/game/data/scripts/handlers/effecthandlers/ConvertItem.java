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
package handlers.effecthandlers;

import java.util.List;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.enchant.attribute.AttributeHolder;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Convert Item effect implementation.
 * @author Zoey76
 */
public class ConvertItem extends AbstractEffect
{
	public ConvertItem(StatSet params)
	{
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		if (effected.isAlikeDead() || !effected.isPlayer())
		{
			return;
		}
		
		final PlayerInstance player = effected.getActingPlayer();
		if (player.hasItemRequest())
		{
			return;
		}
		
		final Weapon weaponItem = player.getActiveWeaponItem();
		if (weaponItem == null)
		{
			return;
		}
		
		ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		if ((wpn == null) || wpn.isAugmented() || (weaponItem.getChangeWeaponId() == 0))
		{
			return;
		}
		
		final int newItemId = weaponItem.getChangeWeaponId();
		if (newItemId == -1)
		{
			return;
		}
		
		final int enchantLevel = wpn.getEnchantLevel();
		final AttributeHolder elementals = wpn.getAttributes() == null ? null : wpn.getAttackAttribute();
		final List<ItemInstance> unequipped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
		final InventoryUpdate iu = new InventoryUpdate();
		for (ItemInstance unequippedItem : unequipped)
		{
			iu.addModifiedItem(unequippedItem);
		}
		player.sendInventoryUpdate(iu);
		
		if (unequipped.isEmpty())
		{
			return;
		}
		
		byte count = 0;
		for (ItemInstance unequippedItem : unequipped)
		{
			if (!(unequippedItem.getItem() instanceof Weapon))
			{
				count++;
				continue;
			}
			
			final SystemMessage sm;
			if (unequippedItem.getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_S2_UNEQUIPPED);
				sm.addInt(unequippedItem.getEnchantLevel());
				sm.addItemName(unequippedItem);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_UNEQUIPPED);
				sm.addItemName(unequippedItem);
			}
			player.sendPacket(sm);
		}
		
		if (count == unequipped.size())
		{
			return;
		}
		
		final ItemInstance destroyItem = player.getInventory().destroyItem("ChangeWeapon", wpn, player, null);
		if (destroyItem == null)
		{
			return;
		}
		
		final ItemInstance newItem = player.getInventory().addItem("ChangeWeapon", newItemId, 1, player, destroyItem);
		if (newItem == null)
		{
			return;
		}
		
		if (elementals != null)
		{
			newItem.setAttribute(elementals, true);
		}
		newItem.setEnchantLevel(enchantLevel);
		player.getInventory().equipItem(newItem);
		
		final SystemMessage msg;
		if (newItem.getEnchantLevel() > 0)
		{
			msg = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
			msg.addInt(newItem.getEnchantLevel());
			msg.addItemName(newItem);
		}
		else
		{
			msg = new SystemMessage(SystemMessageId.YOU_HAVE_EQUIPPED_YOUR_S1);
			msg.addItemName(newItem);
		}
		player.sendPacket(msg);
		
		final InventoryUpdate u = new InventoryUpdate();
		u.addRemovedItem(destroyItem);
		u.addItem(newItem);
		player.sendInventoryUpdate(u);
		
		player.broadcastUserInfo();
	}
}
