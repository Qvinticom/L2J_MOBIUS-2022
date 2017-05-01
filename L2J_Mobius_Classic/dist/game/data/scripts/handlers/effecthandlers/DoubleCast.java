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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.effects.EffectFlag;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * Double Casting effect implementation.
 * @author Nik
 */
public final class DoubleCast extends AbstractEffect
{
	private static final SkillHolder[] TOGGLE_SKILLS = new SkillHolder[]
	{
		new SkillHolder(11007, 1),
		new SkillHolder(11009, 1),
		new SkillHolder(11008, 1),
		new SkillHolder(11010, 1)
	};
	
	private final Map<Integer, List<SkillHolder>> _addedToggles;
	
	public DoubleCast(StatsSet params)
	{
		_addedToggles = new HashMap<>();
	}
	
	@Override
	public long getEffectFlags()
	{
		return EffectFlag.DOUBLE_CAST.getMask();
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isPlayer())
		{
			for (SkillHolder holder : TOGGLE_SKILLS)
			{
				final Skill skill = holder.getSkill();
				if ((skill != null) && !info.getEffected().isAffectedBySkill(holder))
				{
					_addedToggles.computeIfAbsent(info.getEffected().getObjectId(), v -> new ArrayList<>()).add(holder);
					skill.applyEffects(info.getEffected(), info.getEffected());
				}
			}
		}
		super.onStart(info);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (info.getEffected().isPlayer())
		{
			_addedToggles.computeIfPresent(info.getEffected().getObjectId(), (k, v) ->
			{
				v.forEach(h -> info.getEffected().stopSkillEffects(h.getSkill()));
				return null;
			});
		}
		super.onExit(info);
	}
}