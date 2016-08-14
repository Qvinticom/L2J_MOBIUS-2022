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
package com.l2jmobius.gameserver.model.cubic;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.cubic.conditions.ICubicCondition;
import com.l2jmobius.gameserver.model.holders.SkillHolder;

/**
 * @author UnAfraid
 */
public class CubicSkill extends SkillHolder implements ICubicConditionHolder
{
	private final int _triggerRate;
	private final int _successRate;
	private final boolean _canUseOnStaticObjects;
	private final CubicSkillTargetType _targetType;
	private final List<ICubicCondition> _conditions = new ArrayList<>();
	private final boolean _targetDebuff;
	
	public CubicSkill(StatsSet set)
	{
		super(set.getInt("id"), set.getInt("level"));
		_triggerRate = set.getInt("triggerRate", 100);
		_successRate = set.getInt("successRate", 100);
		_canUseOnStaticObjects = set.getBoolean("canUseOnStaticObjects", false);
		_targetType = set.getEnum("target", CubicSkillTargetType.class, CubicSkillTargetType.TARGET);
		_targetDebuff = set.getBoolean("targetDebuff", false);
	}
	
	public int getTriggerRate()
	{
		return _triggerRate;
	}
	
	public int getSuccessRate()
	{
		return _successRate;
	}
	
	public boolean canUseOnStaticObjects()
	{
		return _canUseOnStaticObjects;
	}
	
	public CubicSkillTargetType getTargetType()
	{
		return _targetType;
	}
	
	public boolean isTargetingDebuff()
	{
		return _targetDebuff;
	}
	
	@Override
	public boolean validateConditions(CubicInstance cubic, L2Character owner, L2Character target)
	{
		return (!_targetDebuff || (_targetDebuff && target.getEffectList().hasDebuffs())) && (_conditions.isEmpty() || _conditions.stream().allMatch(condition -> condition.test(cubic, owner, target)));
	}
	
	@Override
	public void addCondition(ICubicCondition condition)
	{
		_conditions.add(condition);
	}
	
	@Override
	public String toString()
	{
		return "Cubic skill id: " + getSkillId() + " level: " + getSkillLvl() + " triggerRate: " + _triggerRate + " successRate: " + _successRate + " canUseOnStaticObjects: " + _canUseOnStaticObjects + " targetType: " + _targetType + " isTargetingDebuff: " + _targetDebuff + Config.EOL;
	}
}
