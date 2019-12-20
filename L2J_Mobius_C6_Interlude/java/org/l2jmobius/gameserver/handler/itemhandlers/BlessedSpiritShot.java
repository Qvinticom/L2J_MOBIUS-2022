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
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:07 $
 */
public class BlessedSpiritShot implements IItemHandler
{
	// All the items ids that this handler knows
	private static final int[] ITEM_IDS =
	{
		3947,
		3948,
		3949,
		3950,
		3951,
		3952
	};
	private static final int[] SKILL_IDS =
	{
		2061,
		2160,
		2161,
		2162,
		2163,
		2164
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		PlayerInstance player = (PlayerInstance) playable;
		ItemInstance weaponInst = player.getActiveWeaponInstance();
		Weapon weaponItem = player.getActiveWeaponItem();
		
		final int itemId = item.getItemId();
		
		if (player.isInOlympiadMode())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			sm.addString(item.getItemName());
			player.sendPacket(sm);
			
			return;
		}
		
		// Check if Blessed Spiritshot can be used
		if ((weaponInst == null) || (weaponItem.getSpiritShotCount() == 0))
		{
			if (!player.getAutoSoulShot().containsKey(itemId))
			{
				player.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
			}
			
			return;
		}
		
		// Check if Blessed Spiritshot is already active (it can be charged over Spiritshot)
		if (weaponInst.getChargedSpiritshot() != ItemInstance.CHARGED_NONE)
		{
			return;
		}
		
		// Check for correct grade
		final int weaponGrade = weaponItem.getCrystalType();
		if (((weaponGrade == Item.CRYSTAL_NONE) && (itemId != 3947)) || ((weaponGrade == Item.CRYSTAL_D) && (itemId != 3948)) || ((weaponGrade == Item.CRYSTAL_C) && (itemId != 3949)) || ((weaponGrade == Item.CRYSTAL_B) && (itemId != 3950)) || ((weaponGrade == Item.CRYSTAL_A) && (itemId != 3951)) || ((weaponGrade == Item.CRYSTAL_S) && (itemId != 3952)))
		{
			if (!player.getAutoSoulShot().containsKey(itemId))
			{
				player.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
			}
			return;
		}
		
		// Consume Blessed Spiritshot if player has enough of them
		if (!Config.DONT_DESTROY_SS && !player.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (player.getAutoSoulShot().containsKey(itemId))
			{
				player.removeAutoSoulShot(itemId);
				player.sendPacket(new ExAutoSoulShot(itemId, 0));
				SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
				sm.addString(item.getItem().getName());
				player.sendPacket(sm);
				
				return;
			}
			
			player.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
			return;
		}
		
		// Charge Blessed Spiritshot
		weaponInst.setChargedSpiritshot(ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
		
		// Send message to client
		player.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
		Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, player, SKILL_IDS[weaponGrade], 1, 0, 0), 360000/* 600 */);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
