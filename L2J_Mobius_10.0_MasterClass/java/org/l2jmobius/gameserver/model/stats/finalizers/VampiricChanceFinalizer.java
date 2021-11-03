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

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.stats.IStatFunction;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Sdw
 */
public class VampiricChanceFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		throwIfPresent(base);
		
		final double amount = creature.getStat().getValue(Stat.ABSORB_DAMAGE_PERCENT, 0) * 100;
		final double vampiricSum = creature.getStat().getVampiricSum();
		return amount > 0 ? Stat.defaultValue(creature, stat, Math.min(1.0, vampiricSum / amount / 100)) : 0;
	}
}
