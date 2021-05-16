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

import org.l2jmobius.gameserver.model.Skill;

/**
 * @author -Nemesiss-
 */
public class NobleSkillTable
{
	private static final Skill[] NOBLE_SKILLS = new Skill[]
	{
		SkillTable.getInstance().getSkill(1323, 1),
		SkillTable.getInstance().getSkill(325, 1),
		SkillTable.getInstance().getSkill(326, 1),
		SkillTable.getInstance().getSkill(327, 1),
		SkillTable.getInstance().getSkill(1324, 1),
		SkillTable.getInstance().getSkill(1325, 1),
		SkillTable.getInstance().getSkill(1326, 1),
		SkillTable.getInstance().getSkill(1327, 1)
	};
	
	public static Skill[] getNobleSkills()
	{
		return NOBLE_SKILLS;
	}
}
