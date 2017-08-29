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
package handlers.effecthandlers;

import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.skills.SkillCaster;

/**
 * Dam Over Time effect implementation.
 */
public final class CallSkillOnActionTime extends AbstractEffect
{
	private final SkillHolder _skill;
	
	public CallSkillOnActionTime(StatsSet params)
	{
		_skill = new SkillHolder(params.getInt("skillId"), params.getInt("skillLevel", 1), params.getInt("skillSubLevel", 0));
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		return castSkill(info);
	}
	
	private boolean castSkill(BuffInfo info)
	{
		if (info.getEffector().isDead())
		{
			return false;
		}
		
		final Skill skill = _skill.getSkill();
		if (skill != null)
		{
			if (skill.isSynergySkill())
			{
				skill.applyEffects(info.getEffector(), info.getEffector());
			}
			
			L2World.getInstance().forEachVisibleObjectInRange(info.getEffector(), L2Character.class, _skill.getSkill().getAffectRange(), c ->
			{
				final L2Object target = skill.getTarget(info.getEffector(), c, false, false, false);
				
				if ((target != null) && target.isCharacter())
				{
					SkillCaster.triggerCast(info.getEffector(), (L2Character) target, skill);
				}
			});
		}
		else
		{
			_log.warning("Skill not found effect called from " + info.getSkill());
		}
		return info.getSkill().isToggle();
	}
}
