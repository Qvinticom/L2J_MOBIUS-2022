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

final class EffectParalyze extends L2Effect
{
	public EffectParalyze(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.PARALYZE;
	}
	
	@Override
	public void onStart()
	{
		getEffected().startAbnormalEffect((short) 0x0400);
		
		getEffected().startParalyze();
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect((short) 0x0400);
		getEffected().stopParalyze(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}