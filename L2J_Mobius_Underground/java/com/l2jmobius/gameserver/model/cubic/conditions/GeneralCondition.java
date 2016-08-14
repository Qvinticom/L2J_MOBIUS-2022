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
package com.l2jmobius.gameserver.model.cubic.conditions;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.cubic.CubicInstance;

/**
 * @author UnAfraid
 */
public class GeneralCondition implements ICubicCondition
{
	private final GeneralConditionType _type;
	private final int _hpPer;
	private final int _hp;
	
	public GeneralCondition(GeneralConditionType type, int hpPer, int hp)
	{
		_type = type;
		_hpPer = hpPer;
		_hp = hp;
	}
	
	@Override
	public boolean test(CubicInstance cubic, L2Character owner, L2Character target)
	{
		final double hpPer = target.getCurrentHpPercent();
		switch (_type)
		{
			case GREATER:
			{
				if (hpPer < _hpPer)
				{
					return false;
				}
				if (target.getCurrentHp() < _hp)
				{
					return false;
				}
				break;
			}
			case LESSER:
			{
				if (hpPer > _hpPer)
				{
					return false;
				}
				if (target.getCurrentHp() > _hp)
				{
					return false;
				}
				break;
			}
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " chance: " + _hpPer + " range: " + _hp;
	}
	
	public static enum GeneralConditionType
	{
		GREATER,
		LESSER;
	}
}
