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
package org.l2jmobius.gameserver.engines.skills;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author Mobius
 */
public class SkillDataHolder
{
	public int id;
	public String name;
	public StatSet[] sets;
	public StatSet[] enchsets1;
	public StatSet[] enchsets2;
	public StatSet[] enchsets3;
	public StatSet[] enchsets4;
	public StatSet[] enchsets5;
	public StatSet[] enchsets6;
	public StatSet[] enchsets7;
	public StatSet[] enchsets8;
	public int currentLevel;
	public List<Skill> skills = new ArrayList<>();
	public List<Skill> currentSkills = new ArrayList<>();
}
