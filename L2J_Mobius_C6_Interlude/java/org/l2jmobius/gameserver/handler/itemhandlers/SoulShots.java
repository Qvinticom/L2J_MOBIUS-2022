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
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skills.Stat;
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
		if (((weaponGrade == ItemTemplate.CRYSTAL_NONE) && (itemId != 5789) && (itemId != 1835)) || ((weaponGrade == ItemTemplate.CRYSTAL_D) && (itemId != 1463)) || ((weaponGrade == ItemTemplate.CRYSTAL_C) && (itemId != 1464)) || ((weaponGrade == ItemTemplate.CRYSTAL_B) && (itemId != 1465)) || ((weaponGrade == ItemTemplate.CRYSTAL_A) && (itemId != 1466)) || ((weaponGrade == ItemTemplate.CRYSTAL_S) && (itemId != 1467)))
		{
			if (!player.getAutoSoulShot().containsKey(itemId))
			{
				player.sendPacket(SystemMessageId.THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON);
			}
			return;
		}
		
		// Check if Soulshot is already active
		if (weaponInst.getChargedSoulshot() != Item.CHARGED_NONE)
		{
			return;
		}
		
		// Consume Soulshots if player has enough of them
		final int saSSCount = (int) player.getStat().calcStat(Stat.SOULSHOT_COUNT, 0, null, null);
		final int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;
		if (!Config.DONT_DESTROY_SS && !player.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
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
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SOULSHOTS_FOR_THAT);
			}
			return;
		}
		
		// Charge soulshot
		if (weaponInst.getChargedSoulshot() != itemId)
		{
			Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, player, SKILL_IDS[weaponGrade], 1, 0, 0), 360000);
		}
		weaponInst.setChargedSoulshot(Item.CHARGED_SOULSHOT);
		
		// Send message to client
		player.sendPacket(SystemMessageId.POWER_OF_THE_SPIRITS_ENABLED);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
