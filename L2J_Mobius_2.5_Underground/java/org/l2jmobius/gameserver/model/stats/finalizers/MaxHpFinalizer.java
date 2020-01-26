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
package org.l2jmobius.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import org.l2jmobius.gameserver.data.xml.impl.EnchantItemHPBonusData;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.Item;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.IStatFunction;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author UnAfraid
 */
public class MaxHpFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		throwIfPresent(base);
		
		double baseValue = creature.getTemplate().getBaseValue(stat, 0);
		if (creature.isPet())
		{
			final PetInstance pet = (PetInstance) creature;
			baseValue = pet.getPetLevelData().getPetMaxHP();
		}
		else if (creature.isPlayer())
		{
			final PlayerInstance player = creature.getActingPlayer();
			if (player != null)
			{
				baseValue = player.getTemplate().getBaseHpMax(player.getLevel());
				
				// Apply enchanted item's bonus HP
				for (ItemInstance item : player.getInventory().getPaperdollItems(ItemInstance::isEnchanted))
				{
					if (item.isArmor())
					{
						final int bodyPart = item.getItem().getBodyPart();
						if ((bodyPart != Item.SLOT_NECK) && (bodyPart != Item.SLOT_LR_EAR) && (bodyPart != Item.SLOT_LR_FINGER))
						{
							baseValue += EnchantItemHPBonusData.getInstance().getHPBonus(item);
						}
					}
				}
			}
		}
		final double chaBonus = creature.isPlayer() ? BaseStat.CHA.calcBonus(creature) : 1.;
		final double conBonus = creature.getCON() > 0 ? BaseStat.CON.calcBonus(creature) : 1.;
		baseValue *= conBonus * chaBonus;
		return Stat.defaultValue(creature, stat, baseValue);
	}
}
