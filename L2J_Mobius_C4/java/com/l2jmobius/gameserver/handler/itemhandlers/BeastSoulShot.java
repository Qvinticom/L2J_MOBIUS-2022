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
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2BabyPetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Broadcast;

/**
 * Beast SoulShot Handler
 * @author Tempy
 */
public class BeastSoulShot implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static int[] _itemIds =
	{
		6645
	};
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (playable == null)
		{
			return;
		}
		
		L2PcInstance activeOwner = null;
		
		if (playable instanceof L2Summon)
		{
			activeOwner = ((L2Summon) playable).getOwner();
			activeOwner.sendPacket(new SystemMessage(SystemMessage.PET_CANNOT_USE_ITEM));
			return;
		}
		
		else if (playable instanceof L2PcInstance)
		{
			activeOwner = (L2PcInstance) playable;
		}
		
		if (activeOwner == null)
		{
			return;
		}
		
		final L2Summon activePet = activeOwner.getPet();
		
		if (activePet == null)
		{
			activeOwner.sendPacket(new SystemMessage(574));
			return;
		}
		
		if (activePet.isDead())
		{
			activeOwner.sendPacket(new SystemMessage(1598));
			return;
		}
		
		final int itemId = 6645;
		final short shotConsumption = activePet.getSoulShotsPerHit();
		final int shotCount = item.getCount();
		
		if (shotCount < shotConsumption)
		{
			// Not enough Soulshots to use.
			if (!activeOwner.disableAutoShot(itemId))
			{
				activeOwner.sendPacket(new SystemMessage(1701));
			}
			return;
		}
		
		L2ItemInstance weaponInst = null;
		
		if ((activePet instanceof L2PetInstance) && !(activePet instanceof L2BabyPetInstance))
		{
			weaponInst = ((L2PetInstance) activePet).getActiveWeaponInstance();
		}
		
		if (weaponInst == null)
		{
			
			if (activePet.getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
			{
				return;
			}
			
			activePet.setChargedSoulShot(L2ItemInstance.CHARGED_SOULSHOT);
			
		}
		else
		{
			if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
			{
				// SoulShots are already active.
				return;
			}
			
			weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
		}
		
		// If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
		if (!activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
		{
			if (!activeOwner.disableAutoShot(itemId))
			{
				activeOwner.sendPacket(new SystemMessage(1701));
			}
			return;
		}
		
		// Pet uses the power of spirit.
		activeOwner.sendPacket(new SystemMessage(1576));
		
		Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(activePet, activePet, 2033, 1, 0, 0), 360000/* 600 */);
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
