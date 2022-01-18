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
package org.l2jmobius.gameserver.model.skill;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.StatSet;

/**
 * @author kombat
 */
public class ChanceCondition
{
	public static final int EVT_HIT = 1;
	public static final int EVT_CRIT = 2;
	public static final int EVT_CAST = 4;
	public static final int EVT_PHYSICAL = 8;
	public static final int EVT_MAGIC = 16;
	public static final int EVT_MAGIC_GOOD = 32;
	public static final int EVT_MAGIC_OFFENSIVE = 64;
	public static final int EVT_ATTACKED = 128;
	public static final int EVT_ATTACKED_HIT = 256;
	public static final int EVT_ATTACKED_CRIT = 512;
	public static final int EVT_HIT_BY_SKILL = 1024;
	public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
	public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
	
	private final ChanceConditionType _triggerType;
	
	private final int _chance;
	
	private ChanceCondition(ChanceConditionType trigger, int chance)
	{
		_triggerType = trigger;
		_chance = chance;
	}
	
	public static ChanceCondition parse(StatSet set)
	{
		try
		{
			final ChanceConditionType trigger = set.getEnum("chanceType", ChanceConditionType.class);
			final int chance = set.getInt("activationChance", 0);
			if ((trigger != null) && (chance > 0))
			{
				return new ChanceCondition(trigger, chance);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean trigger(int event)
	{
		return _triggerType.check(event) && (Rnd.get(100) < _chance);
	}
	
	@Override
	public String toString()
	{
		return "Trigger[" + _chance + ";" + _triggerType + "]";
	}
}
