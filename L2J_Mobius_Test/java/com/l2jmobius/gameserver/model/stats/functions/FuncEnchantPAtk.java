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
package com.l2jmobius.gameserver.model.stats.functions;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.stats.Stats;

public class FuncEnchantPAtk extends AbstractFunction
{
	private static final double blessedBonus = 1.5;
	
	public FuncEnchantPAtk(Stats stat, int order, Object owner, double value, Condition applayCond)
	{
		super(stat, order, owner, value, applayCond);
	}
	
	@Override
	public double calc(L2Character effector, L2Character effected, Skill skill, double initVal)
	{
		double value = initVal;
		if ((getApplayCond() != null) && !getApplayCond().test(effector, effected, skill))
		{
			return value;
		}
		
		final L2ItemInstance item = (L2ItemInstance) getFuncOwner();
		if (item.getEnchantLevel() > 3)
		{
			// Increases Phys.Atk for chest
			if (item.getEnchantLevel() == 4)
			{
				value += 2 * blessedBonus;
			}
			else
			{
				value += 2 * blessedBonus * ((item.getEnchantLevel() * 2) - 9);
			}
		}
		return initVal;
	}
}
