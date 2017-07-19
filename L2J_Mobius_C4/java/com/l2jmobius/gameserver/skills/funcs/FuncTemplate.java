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
package com.l2jmobius.gameserver.skills.funcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.l2jmobius.gameserver.skills.Env;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.skills.conditions.Condition;

/**
 * @author mkizub TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public final class FuncTemplate
{
	public Condition _attachCond;
	public Condition _applayCond;
	public final Class<?> _func;
	public final Constructor<?> _constructor;
	public final Stats _stat;
	public final int _order;
	public final Lambda _lambda;
	
	public FuncTemplate(Condition attachCond, Condition applayCond, String func, Stats stat, int order, Lambda lambda)
	{
		_attachCond = attachCond;
		_applayCond = applayCond;
		_stat = stat;
		_order = order;
		_lambda = lambda;
		
		try
		{
			_func = Class.forName("com.l2jmobius.gameserver.skills.funcs.Func" + func);
		}
		catch (final ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			_constructor = _func.getConstructor(new Class[]
			{
				Stats.class, // stats to update
				Integer.TYPE, // order of execution
				Object.class, // owner
				Lambda.class // value for function
			});
		}
		catch (final NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Func getFunc(Env env, Object owner)
	{
		if ((_attachCond != null) && !_attachCond.test(env))
		{
			return null;
		}
		
		try
		{
			final Func f = (Func) _constructor.newInstance(_stat, _order, owner, _lambda);
			if (_applayCond != null)
			{
				f.setCondition(_applayCond);
			}
			return f;
		}
		catch (final IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (final InstantiationException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (final InvocationTargetException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}