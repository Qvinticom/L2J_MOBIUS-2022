/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.BuffInfo;

/**
 * Force Skill effect implementation.
 * @author Mobius
 */
public final class TriggerForce extends AbstractEffect
{
	private final SkillHolder _skill;
	
	/**
	 * @param attachCond
	 * @param applyCond
	 * @param set
	 * @param params
	 */
	public TriggerForce(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_skill = new SkillHolder(params.getInt("skillId", 0), 1);
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2PcInstance effector = info.getEffector().getActingPlayer();
		final L2PcInstance effected = info.getEffected().getActingPlayer();
		
		if (effected.isDead() || (effector == null))
		{
			return;
		}
		
		if (_skill.getSkillId() == 0)
		{
			return;
		}
		
		if (effector.getParty() != null)
		{
			for (L2PcInstance member : effector.getParty().getMembers())
			{
				member.makeTriggerCast(_skill.getSkill(), effector);
			}
		}
		else
		{
			effector.makeTriggerCast(_skill.getSkill(), effector);
		}
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		final L2PcInstance effected = info.getEffected().getActingPlayer();
		final int skillId = info.getSkill().getId();
		
		if ((effected.getEffectList().getBuffInfoBySkillId(skillId) == null) && (effected.getEffectList().getBuffInfoBySkillId(skillId + 1) != null))
		{
			effected.getEffectList().remove(true, effected.getEffectList().getBuffInfoBySkillId(skillId + 1));
		}
		
		if (effected.getParty() != null)
		{
			for (L2PcInstance member : effected.getParty().getMembers())
			{
				member.makeTriggerCast(_skill.getSkill(), effected);
			}
		}
	}
}