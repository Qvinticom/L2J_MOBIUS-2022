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
 * @author Sdw
 */
public class FuncMatkAccuracy extends AbstractFunction
{
	private static final FuncMatkAccuracy _faa_instance = new FuncMatkAccuracy();
	
	public static AbstractFunction getInstance()
	{
		return _faa_instance;
	}
	
	private FuncMatkAccuracy()
	{
		super(Stats.ACCURACY_MAGIC, 1, null, 0, null);
	}
	
	@Override
	public double calc(L2Character effector, L2Character effected, Skill skill, double initVal)
	{
		return initVal + (Math.sqrt(effector.getWIT()) * 3) + (effector.getLevel() * 2);
	}
}