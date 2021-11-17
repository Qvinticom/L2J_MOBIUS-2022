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
package handlers.skillconditionhandlers;

import org.l2jmobius.gameserver.enums.SkillConditionAffectType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.skill.AbnormalType;
import org.l2jmobius.gameserver.model.skill.ISkillCondition;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * @author UnAfraid
 */
public class OpCheckAbnormalSkillCondition implements ISkillCondition
{
	private final AbnormalType _type;
	private final int _level;
	private final boolean _hasAbnormal;
	private final SkillConditionAffectType _affectType;
	
	public OpCheckAbnormalSkillCondition(StatSet params)
	{
		_type = params.getEnum("type", AbnormalType.class);
		_level = params.getInt("level");
		_hasAbnormal = params.getBoolean("hasAbnormal");
		_affectType = params.getEnum("affectType", SkillConditionAffectType.class, SkillConditionAffectType.TARGET);
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		switch (_affectType)
		{
			case CASTER:
			{
				return caster.getEffectList().hasAbnormalType(_type, info -> (info.getSkill().getAbnormalLevel() >= _level)) == _hasAbnormal;
			}
			case TARGET:
			{
				if ((target != null) && target.isCreature())
				{
					return ((Creature) target).getEffectList().hasAbnormalType(_type, info -> (info.getSkill().getAbnormalLevel() >= _level)) == _hasAbnormal;
				}
			}
		}
		return false;
	}
}
