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

import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.skills.Env;

public class EffectStunSelf extends L2Effect
{
	public EffectStunSelf(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.STUN_SELF;
	}
	
	@Override
	public void onStart()
	{
		if (getEffector() != null)
		{
			getEffector().startStunning();
		}
	}
	
	@Override
	public void onExit()
	{
		if (getEffector() != null)
		{
			getEffector().stopStunning(this);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
