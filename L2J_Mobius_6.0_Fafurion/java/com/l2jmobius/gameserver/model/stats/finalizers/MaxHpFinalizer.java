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
package com.l2jmobius.gameserver.model.stats.finalizers;

import java.util.Optional;

import com.l2jmobius.gameserver.data.xml.impl.EnchantItemHPBonusData;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.stats.BaseStats;
import com.l2jmobius.gameserver.model.stats.IStatsFunction;
import com.l2jmobius.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class MaxHpFinalizer implements IStatsFunction
{
	@Override
	public double calc(L2Character creature, Optional<Double> base, Stats stat)
	{
		throwIfPresent(base);
		
		double baseValue = creature.getTemplate().getBaseValue(stat, 0);
		if (creature.isPet())
		{
			final L2PetInstance pet = (L2PetInstance) creature;
			baseValue = pet.getPetLevelData().getPetMaxHP();
		}
		else if (creature.isPlayer())
		{
			final L2PcInstance player = creature.getActingPlayer();
			if (player != null)
			{
				baseValue = player.getTemplate().getBaseHpMax(player.getLevel());
				
				// Apply enchanted item's bonus HP
				for (L2ItemInstance item : player.getInventory().getPaperdollItems(L2ItemInstance::isEnchanted))
				{
					if (item.isArmor())
					{
						final long bodyPart = item.getItem().getBodyPart();
						if ((bodyPart != L2Item.SLOT_NECK) && (bodyPart != L2Item.SLOT_LR_EAR) && (bodyPart != L2Item.SLOT_LR_FINGER))
						{
							baseValue += EnchantItemHPBonusData.getInstance().getHPBonus(item);
						}
					}
				}
			}
		}
		final double chaBonus = creature.isPlayer() ? BaseStats.CHA.calcBonus(creature) : 1.;
		final double conBonus = creature.getCON() > 0 ? BaseStats.CON.calcBonus(creature) : 1.;
		baseValue *= conBonus * chaBonus;
		return Stats.defaultValue(creature, stat, baseValue);
	}
}
