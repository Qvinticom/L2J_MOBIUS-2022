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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Broadcast;

public class SpiritShot implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		5790,
		2509,
		2510,
		2511,
		2512,
		2513,
		2514
	};
	private static final int[] SKILL_IDS =
	{
		2061,
		2155,
		2156,
		2157,
		2158,
		2159
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) playable;
		final Item weaponInst = player.getActiveWeaponInstance();
		final Weapon weaponItem = player.getActiveWeaponItem();
		final int itemId = item.getItemId();
		
		// Check if Spiritshot can be used
		if ((weaponInst == null) || (weaponItem.getSpiritShotCount() == 0))
		{
			if (!player.getAutoSoulShot().containsKey(itemId))
			{
				player.sendPacket(SystemMessageId.YOU_MAY_NOT_USE_SPIRITSHOTS);
			}
			return;
		}
		
		// Check if Spiritshot is already active
		if (weaponInst.getChargedSpiritshot() != Item.CHARGED_NONE)
		{
			return;
		}
		
		// Check for correct grade
		final int weaponGrade = weaponItem.getCrystalType();
		if (((weaponGrade == ItemTemplate.CRYSTAL_NONE) && (itemId != 5790) && (itemId != 2509)) || ((weaponGrade == ItemTemplate.CRYSTAL_D) && (itemId != 2510)) || ((weaponGrade == ItemTemplate.CRYSTAL_C) && (itemId != 2511)) || ((weaponGrade == ItemTemplate.CRYSTAL_B) && (itemId != 2512)) || ((weaponGrade == ItemTemplate.CRYSTAL_A) && (itemId != 2513)) || ((weaponGrade == ItemTemplate.CRYSTAL_S) && (itemId != 2514)))
		{
			if (!player.getAutoSoulShot().containsKey(itemId))
			{
				player.sendPacket(SystemMessageId.THE_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPON_S_GRADE);
			}
			return;
		}
		
		// Consume Spiritshot if player has enough of them
		if (!Config.DONT_DESTROY_SS && !player.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (player.getAutoSoulShot().containsKey(itemId))
			{
				player.removeAutoSoulShot(itemId);
				player.sendPacket(new ExAutoSoulShot(itemId, 0));
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
				sm.addString(item.getItem().getName());
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SPIRITSHOTS_FOR_THAT);
			}
			return;
		}
		
		// Charge Spiritshot
		if (weaponInst.getChargedSpiritshot() != itemId)
		{
			Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, player, SKILL_IDS[weaponGrade], 1, 0, 0), 360000);
		}
		weaponInst.setChargedSpiritshot(Item.CHARGED_SPIRITSHOT);
		
		// Send message to client
		player.sendPacket(SystemMessageId.POWER_OF_MANA_ENABLED);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
