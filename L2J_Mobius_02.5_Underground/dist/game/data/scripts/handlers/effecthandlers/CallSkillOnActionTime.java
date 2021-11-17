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

import java.util.Collections;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;

/**
 * Dam Over Time effect implementation.
 */
public class CallSkillOnActionTime extends AbstractEffect
{
	private final SkillHolder _skill;
	
	public CallSkillOnActionTime(StatSet params)
	{
		_skill = new SkillHolder(params.getInt("skillId"), params.getInt("skillLevel", 1), params.getInt("skillSubLevel", 0));
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!_skill.getSkill().isSynergySkill())
		{
			effected.getEffectList().stopEffects(Collections.singleton(_skill.getSkill().getAbnormalType()));
			effected.getEffectList().addBlockedAbnormalTypes(Collections.singleton(_skill.getSkill().getAbnormalType()));
		}
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (!_skill.getSkill().isSynergySkill())
		{
			effected.getEffectList().removeBlockedAbnormalTypes(Collections.singleton(_skill.getSkill().getAbnormalType()));
		}
	}
	
	@Override
	public boolean onActionTime(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effector.isDead())
		{
			return false;
		}
		
		final Skill triggerSkill = _skill.getSkill();
		if (triggerSkill != null)
		{
			if (triggerSkill.isSynergySkill())
			{
				triggerSkill.applyEffects(effector, effector);
			}
			
			World.getInstance().forEachVisibleObjectInRange(effector, Creature.class, _skill.getSkill().getAffectRange(), c ->
			{
				final WorldObject target = triggerSkill.getTarget(effector, c, false, false, false);
				
				if ((target != null) && target.isCreature())
				{
					SkillCaster.triggerCast(effector, (Creature) target, triggerSkill);
				}
			});
		}
		else
		{
			LOGGER.warning("Skill not found effect called from " + skill);
		}
		return skill.isToggle();
	}
}
