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
package org.l2jmobius.gameserver.datatables;

import org.l2jmobius.gameserver.model.Skill;

/**
 * @author BiTi
 */
public class HeroSkillTable
{
	private static Skill[] _heroSkills;
	
	private HeroSkillTable()
	{
		_heroSkills = new Skill[5];
		_heroSkills[0] = SkillTable.getInstance().getInfo(395, 1);
		_heroSkills[1] = SkillTable.getInstance().getInfo(396, 1);
		_heroSkills[2] = SkillTable.getInstance().getInfo(1374, 1);
		_heroSkills[3] = SkillTable.getInstance().getInfo(1375, 1);
		_heroSkills[4] = SkillTable.getInstance().getInfo(1376, 1);
	}
	
	public static Skill[] getHeroSkills()
	{
		return _heroSkills;
	}
	
	public static boolean isHeroSkill(int skillid)
	{
		Integer[] _HeroSkillsId = new Integer[]
		{
			395,
			396,
			1374,
			1375,
			1376
		};
		
		for (int id : _HeroSkillsId)
		{
			if (id == skillid)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static HeroSkillTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HeroSkillTable INSTANCE = new HeroSkillTable();
	}
}
