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
package org.l2jmobius.gameserver.model.skill.holders;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.Skill;

/**
 * @author BiggBoss Simple class for storing skill id/level
 */
public class SkillHolder
{
	private final int _skillId;
	private final int _skillLevel;
	
	public SkillHolder(int skillId, int skillLevel)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
	}
	
	public SkillHolder(Skill skill)
	{
		_skillId = skill.getId();
		_skillLevel = skill.getLevel();
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	public Skill getSkill()
	{
		return SkillTable.getInstance().getSkill(_skillId, _skillLevel);
	}
}