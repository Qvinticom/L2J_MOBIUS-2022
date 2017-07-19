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
package com.l2jmobius.gameserver.skills.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.skills.Env;
import com.l2jmobius.gameserver.skills.conditions.Condition;
import com.l2jmobius.gameserver.skills.funcs.FuncTemplate;
import com.l2jmobius.gameserver.skills.funcs.Lambda;

/**
 * @author mkizub TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public final class EffectTemplate
{
	static Logger _log = Logger.getLogger(EffectTemplate.class.getName());
	
	private final Class<?> _func;
	private final Constructor<?> _constructor;
	
	public final Condition _attachCond;
	public final Condition _applayCond;
	public final Lambda _lambda;
	public final int _counter;
	public final int _period; // in seconds
	public final short _abnormalEffect;
	public FuncTemplate[] _funcTemplates;
	
	public final String _stackType;
	public final float _stackOrder;
	public final boolean _icon;
	
	public EffectTemplate(Condition attachCond, Condition applayCond, String func, Lambda lambda, int counter, int period, short abnormalEffect, String stackType, float stackOrder, boolean showIcon)
	{
		_attachCond = attachCond;
		_applayCond = applayCond;
		_lambda = lambda;
		_counter = counter;
		_period = period;
		_abnormalEffect = abnormalEffect;
		_stackType = stackType;
		_stackOrder = stackOrder;
		_icon = showIcon;
		
		try
		{
			_func = Class.forName("com.l2jmobius.gameserver.skills.effects.Effect" + func);
		}
		catch (final ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			_constructor = _func.getConstructor(Env.class, EffectTemplate.class);
		}
		catch (final NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public L2Effect getEffect(Env env)
	{
		if ((_attachCond != null) && !_attachCond.test(env))
		{
			return null;
		}
		try
		{
			final L2Effect effect = (L2Effect) _constructor.newInstance(env, this);
			return effect;
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
			_log.warning("Error creating new instance of Class " + _func + " Exception was:");
			e.getTargetException().printStackTrace();
			return null;
		}
	}
	
	public void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			final int len = _funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
}