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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.BuffInfo;

/**
 * Resist Skill effect implementaion.
 * @author UnAfraid
 */
public class ResistSkill extends AbstractEffect
{
	private final List<SkillHolder> _skills = new ArrayList<>();
	
	public ResistSkill(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		for (int i = 1;; i++)
		{
			final int skillId = params.getInt("skillId" + i, 0);
			final int skillLevel = params.getInt("skillLevel" + i, 0);
			if (skillId == 0)
			{
				break;
			}
			_skills.add(new SkillHolder(skillId, skillLevel));
		}
		
		if (_skills.isEmpty())
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + ": Without parameters!");
		}
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final Creature effected = info.getEffected();
		for (SkillHolder holder : _skills)
		{
			effected.addInvulAgainst(holder);
		}
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		for (SkillHolder holder : _skills)
		{
			info.getEffected().removeInvulAgainst(holder);
		}
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
}
