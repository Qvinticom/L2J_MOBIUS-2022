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
package com.l2jmobius.gameserver.handler.itemhandlers;

import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2Item;
import com.l2jmobius.gameserver.templates.L2Weapon;
import com.l2jmobius.gameserver.util.Broadcast;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:07 $
 */

public class BlessedSpiritShot implements IItemHandler
{
	// all the items ids that this handler knowns
	private static int[] _itemIds =
	{
		3947,
		3948,
		3949,
		3950,
		3951,
		3952
	};
	private static int[] _skillIds =
	{
		2061,
		2160,
		2161,
		2162,
		2163,
		2164
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	@Override
	public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		final int itemId = item.getItemId();
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		// Check if Blessed Spiritshot can be used
		if ((weaponInst == null) || (weaponItem.getSpiritShotCount() == 0))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SPIRITSHOTS));
			}
			return;
		}
		
		// Check if Blessed Spiritshot is already active (it can be charged over Spiritshot)
		if (weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
		{
			return;
		}
		
		// Check for correct grade
		final int weaponGrade = weaponItem.getCrystalType();
		if (((weaponGrade == L2Item.CRYSTAL_NONE) && (itemId != 3947)) || ((weaponGrade == L2Item.CRYSTAL_D) && (itemId != 3948)) || ((weaponGrade == L2Item.CRYSTAL_C) && (itemId != 3949)) || ((weaponGrade == L2Item.CRYSTAL_B) && (itemId != 3950)) || ((weaponGrade == L2Item.CRYSTAL_A) && (itemId != 3951)) || ((weaponGrade == L2Item.CRYSTAL_S) && (itemId != 3952)))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.SPIRITSHOTS_GRADE_MISMATCH));
			}
			return;
		}
		
		// Consume Blessed Spiritshot if player has enough of them
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (!activeChar.disableAutoShot(itemId))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SPIRITSHOTS));
			}
			return;
		}
		
		// Charge Blessed Spiritshot
		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
		
		// Send message to client
		activeChar.sendPacket(new SystemMessage(SystemMessage.ENABLED_SPIRITSHOT));
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, _skillIds[weaponGrade], 1, 0, 0), 360000/* 600 */);
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}