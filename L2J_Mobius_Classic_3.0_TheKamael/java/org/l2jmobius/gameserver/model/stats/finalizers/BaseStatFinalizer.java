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

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;

import org.l2jmobius.gameserver.data.xml.impl.ArmorSetsData;
import org.l2jmobius.gameserver.model.ArmorSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.IStatFunction;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author UnAfraid
 */
public class BaseStatFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		throwIfPresent(base);
		
		// Apply template value
		double baseValue = creature.getTemplate().getBaseValue(stat, 0);
		
		// Should not apply armor set and henna bonus to summons.
		if (creature.isPlayer())
		{
			final PlayerInstance player = creature.getActingPlayer();
			final Set<ArmorSet> appliedSets = new HashSet<>(2);
			
			// Armor sets calculation
			for (ItemInstance item : player.getInventory().getPaperdollItems())
			{
				for (ArmorSet set : ArmorSetsData.getInstance().getSets(item.getId()))
				{
					if ((set.getPiecesCount(player, ItemInstance::getId) >= set.getMinimumPieces()) && appliedSets.add(set))
					{
						baseValue += set.getStatsBonus(BaseStat.valueOf(stat));
					}
				}
			}
			
			// Henna calculation
			baseValue += player.getHennaValue(BaseStat.valueOf(stat));
		}
		return validateValue(creature, Stat.defaultValue(creature, stat, baseValue), 1, BaseStat.MAX_STAT_VALUE - 1);
	}
	
}
