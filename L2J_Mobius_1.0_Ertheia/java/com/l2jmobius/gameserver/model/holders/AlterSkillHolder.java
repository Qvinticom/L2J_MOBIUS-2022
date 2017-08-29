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
package com.l2jmobius.gameserver.model.holders;

import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.skills.AbnormalType;

/**
 * @author UnAfraid
 */
public class AlterSkillHolder extends SkillHolder
{
	private final int _chance;
	private final AbnormalType _abnormalType;
	
	public AlterSkillHolder(int skillId, int skillLevel, int chance, AbnormalType abnormalType)
	{
		super(skillId, skillLevel);
		_chance = chance;
		_abnormalType = abnormalType;
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	public AbnormalType getAbnormalType()
	{
		return _abnormalType;
	}
	
	public static AlterSkillHolder fromStatsSet(StatsSet set)
	{
		return new AlterSkillHolder(set.getInt(".id"), set.getInt(".level", 1), set.getInt(".chance"), set.getEnum(".abnormalType", AbnormalType.class));
	}
}
