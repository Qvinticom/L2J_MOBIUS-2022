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
package com.l2jmobius.gameserver.model.stats.functions.formulas;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.stats.Stats;
import com.l2jmobius.gameserver.model.stats.functions.AbstractFunction;

/**
 * @author UnAfraid
 */
public class FuncAtkEvasion extends AbstractFunction
{
	private static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();
	
	public static AbstractFunction getInstance()
	{
		return _fae_instance;
	}
	
	private FuncAtkEvasion()
	{
		super(Stats.EVASION_RATE, 1, null, 0, null);
	}
	
	@Override
	public double calc(L2Character effector, L2Character effected, Skill skill, double initVal)
	{
		final int level = effector.getLevel();
		// [Square(DEX)] * 5 + lvl;
		double value = initVal + (Math.sqrt(effector.getDEX()) * 5) + level;
		if (effector.isPlayer())
		{
			if (level > 69)
			{
				value += level - 69;
			}
			if (level > 77)
			{
				value += 1;
			}
			if (level > 80)
			{
				value += 2;
			}
			if (level > 87)
			{
				value += 2;
			}
			if (level > 92)
			{
				value += 1;
			}
			if (level > 97)
			{
				value += 1;
			}
		}
		else if (level > 69)
		{
			value += (level - 69) + 2;
		}
		return (int) value;
	}
}