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
package org.l2jmobius.gameserver.model.skill.funcs;

import org.l2jmobius.gameserver.model.skill.Env;
import org.l2jmobius.gameserver.model.skill.Stat;
import org.l2jmobius.gameserver.model.skill.conditions.Condition;

/**
 * A Func object is a component of a Calculator created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematics function:<br>
 * <br>
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<br>
 * <br>
 * When the calc method of a calculator is launched, each mathematics function is called according to its priority <b>_order</b>. Indeed, Func with lowest priority order is executed first and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in the
 * value property of an Env class instance.
 */
public abstract class Func
{
	/** Statistics, that is affected by this function (See Creature.CALCULATOR_XXX constants) */
	public Stat stat;
	
	/**
	 * Order of functions calculation. Functions with lower order are executed first. Functions with the same order are executed in unspecified order. Usually add/subtract functions has lowest order, then bonus/penalty functions (Multiply/divide) are applied, then functions that do more complex
	 * calculations (non-linear functions).
	 */
	public int order;
	
	/**
	 * Owner can be an armor, weapon, skill, system event, quest, etc Used to remove all functions added by this owner.
	 */
	public Object funcOwner;
	
	/** Function may be disabled by attached condition. */
	public Condition cond;
	
	/**
	 * Constructor of Func.
	 * @param pStat
	 * @param pOrder
	 * @param owner
	 */
	public Func(Stat pStat, int pOrder, Object owner)
	{
		stat = pStat;
		order = pOrder;
		funcOwner = owner;
	}
	
	/**
	 * Add a condition to the Func.
	 * @param pCond
	 */
	public void setCondition(Condition pCond)
	{
		cond = pCond;
	}
	
	/**
	 * Run the mathematics function of the Func.
	 * @param env
	 */
	public abstract void calc(Env env);
}
