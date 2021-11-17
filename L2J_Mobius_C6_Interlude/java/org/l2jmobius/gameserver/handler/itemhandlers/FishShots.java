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
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @author -Nemesiss-
 */
public class FishShots implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		6535,
		6536,
		6537,
		6538,
		6539,
		6540
	};
	private static final int[] SKILL_IDS =
	{
		2181,
		2182,
		2183,
		2184,
		2185,
		2186
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
		if ((weaponInst == null) || (weaponItem.getItemType() != WeaponType.ROD))
		{
			return;
		}
		
		if (weaponInst.getChargedFishshot())
		{
			// spiritshot is already active
			return;
		}
		
		final int FishshotId = item.getItemId();
		final int grade = weaponItem.getCrystalType();
		final int count = item.getCount();
		if (((grade == ItemTemplate.CRYSTAL_NONE) && (FishshotId != 6535)) || ((grade == ItemTemplate.CRYSTAL_D) && (FishshotId != 6536)) || ((grade == ItemTemplate.CRYSTAL_C) && (FishshotId != 6537)) || ((grade == ItemTemplate.CRYSTAL_B) && (FishshotId != 6538)) || ((grade == ItemTemplate.CRYSTAL_A) && (FishshotId != 6539)) || ((grade == ItemTemplate.CRYSTAL_S) && (FishshotId != 6540)))
		{
			// 1479 - This fishing shot is not fit for the fishing pole crystal.
			player.sendPacket(SystemMessageId.THAT_IS_THE_WRONG_GRADE_OF_SOULSHOT_FOR_THAT_FISHING_POLE);
			return;
		}
		
		if (count < 1)
		{
			return;
		}
		
		weaponInst.setChargedFishshot(true);
		player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
		Broadcast.toSelfAndKnownPlayers(player, new MagicSkillUse(player, player, SKILL_IDS[grade], 1, 0, 0));
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
