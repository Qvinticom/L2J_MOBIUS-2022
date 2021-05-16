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
package org.l2jmobius.gameserver.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.Skill;

/**
 * @author Mobius
 */
public class HeroSkillTable
{
	private static final Map<Integer, Skill> HERO_SKILLS = new HashMap<>();
	static
	{
		HERO_SKILLS.put(395, SkillTable.getInstance().getSkill(395, 1));
		HERO_SKILLS.put(396, SkillTable.getInstance().getSkill(396, 1));
		HERO_SKILLS.put(1374, SkillTable.getInstance().getSkill(1374, 1));
		HERO_SKILLS.put(1375, SkillTable.getInstance().getSkill(1375, 1));
		HERO_SKILLS.put(1376, SkillTable.getInstance().getSkill(1376, 1));
	}
	
	public static Collection<Skill> getHeroSkills()
	{
		return HERO_SKILLS.values();
	}
	
	public static boolean isHeroSkill(int skillId)
	{
		return HERO_SKILLS.containsKey(skillId);
	}
}
