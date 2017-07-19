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
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Env;

class EffectRelax extends L2Effect
{
	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.RELAXING;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		if (getEffected().getCurrentHp() == getEffected().getMaxHp())
		{
			if (getSkill().isToggle())
			{
				
				getEffected().sendPacket(new SystemMessage(175));
				return;
				
			}
		}
		
		if (getEffected() instanceof L2PcInstance)
		{
			
			((L2PcInstance) getEffected()).setRelax(true);
			((L2PcInstance) getEffected()).sitDown();
			
		}
		
		super.onStart();
	}
	
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).setRelax(false);
		}
		
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		
		if (getEffected().isDead())
		{
			return false;
		}
		
		if (!((L2PcInstance) getEffected()).isSitting())
		{
			return false;
		}
		
		if (getEffected().getCurrentHp() == getEffected().getMaxHp())
		
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(new SystemMessage(175));
				return false;
				
			}
		}
		
		final double manaDam = calc();
		if (manaDam > getEffected().getCurrentMp())
		{
			if (getSkill().isToggle())
			{
				
				getEffected().sendPacket(new SystemMessage(140));
				return false;
			}
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
}