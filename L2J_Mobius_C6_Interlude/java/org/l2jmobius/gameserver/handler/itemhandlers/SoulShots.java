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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.Item;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.skills.Stats;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Broadcast;

public class SoulShots implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		5789,
		1835,
		1463,
		1464,
		1465,
		1466,
		1467
	};
	private static final int[] SKILL_IDS =
	{
		2039,
		2150,
		2151,
		2152,
		2153,
		2154
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		final ItemInstance weaponInst = player.getActiveWeaponInstance();
		final Weapon weaponItem = player.getActiveWeaponItem();
		final int itemId = item.getItemId();
		
		// Check if Soulshot can be used
		if ((weaponInst == null) || (weaponItem.getSoulShotCount() == 0))
		{
			if (!player.getAutoSoulShot().containsKey(itemId))
			{
				player.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
			}
			return;
		}
		
		// Check for correct grade
		final int weaponGrade = weaponItem.getCrystalType();
		if (((weaponGrade == Item.CRYSTAL_NONE) && (itemId != 5789) && (itemId != 1835)) || ((weaponGrade == Item.CRYSTAL_D) && (itemId != 1463)) || ((weaponGrade == Item.CRYSTAL_C) && (itemId != 1464)) || ((weaponGrade == Item.CRYSTAL_B) && (itemId != 1465)) || ((weaponGrade == Item.CRYSTAL_A) && (itemId != 1466)) || ((weaponGrade == Item.CRYSTAL_S) && (itemId != 1467)))
		{
			if (!player.getAutoSoulShot().containsKey(itemId))
			{
				player.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
			}
			return;
		}
		
		player.soulShotLock.lock();
		try
		{
			// Check if Soulshot is already active
			if (weaponInst.getChargedSoulshot() != ItemInstance.CHARGED_NONE)
			{
				return;
			}
			
			// Consume Soulshots if player has enough of them
			final int saSSCount = (int) player.getStat().calcStat(Stats.SOULSHOT_COUNT, 0, null, null);
			final int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;
			
			if (!Config.DONT_DESTROY_SS && !player.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
			{
				if (player.getAutoSoulShot().containsKey(itemId))
				{
					player.removeAutoSoulShot(itemId);
					player.sendPacket(new ExAutoSoulShot(itemId, 0));
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
					sm.addString(item.getItem().getName());
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
				}
				return;
			}
			
			// Charge soulshot
			weaponInst.setChargedSoulshot(ItemInstance.CHARGED_SOULSHOT);
		}
		finally
		{
			player.soulShotLock.unlock();
		}
		
		// Send message to client
		player.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
		Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, player, SKILL_IDS[weaponGrade], 1, 0, 0), 360000/* 600 */);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
