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

import com.l2jmobius.gameserver.model.L2Attackable;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2Skill.SkillTargetType;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Env;

class EffectDamOverTime extends L2Effect
{
	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.DMG_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		double damage = calc();
		if (damage >= (getEffected().getCurrentHp() - 1))
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(new SystemMessage(610));
				return false;
			}
			
			if (getEffected().getCurrentHp() <= 1)
			{
				return true;
			}
			
			damage = getEffected().getCurrentHp() - 1;
		}
		
		final boolean awake = !(getEffected() instanceof L2Attackable) && !((getSkill().getTargetType() == SkillTargetType.TARGET_SELF) && getSkill().isToggle());
		
		getEffected().reduceCurrentHp(damage, getEffector(), awake);
		
		return true;
	}
}