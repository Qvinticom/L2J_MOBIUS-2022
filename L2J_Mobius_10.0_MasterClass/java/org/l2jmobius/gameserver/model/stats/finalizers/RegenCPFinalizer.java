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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.IStatFunction;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author UnAfraid
 */
public class RegenCPFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		throwIfPresent(base);
		if (!creature.isPlayer())
		{
			return 0;
		}
		
		final PlayerInstance player = creature.getActingPlayer();
		double baseValue = player.getTemplate().getBaseCpRegen(creature.getLevel()) * creature.getLevelMod() * BaseStat.CON.calcBonus(creature) * Config.CP_REGEN_MULTIPLIER;
		if (player.isSitting())
		{
			baseValue *= 1.5; // Sitting
		}
		else if (!player.isMoving())
		{
			baseValue *= 1.1; // Staying
		}
		else if (player.isRunning())
		{
			baseValue *= 0.7; // Running
		}
		return Stat.defaultValue(player, stat, baseValue);
	}
}
