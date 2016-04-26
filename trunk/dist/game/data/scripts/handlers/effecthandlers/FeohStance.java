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

import java.util.HashSet;

import com.l2jmobius.gameserver.datatables.SkillData;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author Ofelin
 */
public class FeohStance extends AbstractEffect
{
	private static final int FIRE_STANCE = 11007;
	private static final int WATER_STANCE = 11008;
	private static final int WIND_STANCE = 11009;
	private static final int EARTH_STANCE = 11010;
	private static final int ELEMENTAL_SPIKE = 11011;
	private static final int ELEMENTAL_CRASH = 11017;
	private static final int ELEMENTAL_DESTRUCTION = 11023;
	private static final int ELEMENTAL_BLAST = 11034;
	private static final int ELEMENTAL_STORM = 11040;
	private final HashSet<Skill> _skillList = new HashSet<>();
	private int _stanceId;
	
	public FeohStance(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2PcInstance player = (L2PcInstance) info.getEffector();
		final Skill elementalSpike = player.getKnownSkill(ELEMENTAL_SPIKE);
		final Skill elementalCrash = player.getKnownSkill(ELEMENTAL_CRASH);
		final Skill elementalDestruction = player.getKnownSkill(ELEMENTAL_DESTRUCTION);
		final Skill elementalBlast = player.getKnownSkill(ELEMENTAL_BLAST);
		final Skill elementalStorm = player.getKnownSkill(ELEMENTAL_STORM);
		if (elementalSpike != null)
		{
			_skillList.add(elementalSpike);
		}
		if (elementalCrash != null)
		{
			_skillList.add(elementalCrash);
			
		}
		if (elementalDestruction != null)
		{
			_skillList.add(elementalDestruction);
		}
		if (elementalBlast != null)
		{
			_skillList.add(elementalBlast);
		}
		if (elementalStorm != null)
		{
			_skillList.add(elementalStorm);
		}
		_stanceId = info.getSkill().getId();
		
		switch (_stanceId)
		{
			case FIRE_STANCE:
			{
				for (Skill skill : _skillList)
				{
					final int skillId = skill.getId();
					final int skillLevel = player.getSkillLevel(skillId);
					player.removeSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
					player.addSkill(SkillData.getInstance().getSkill(skillId + 1, skillLevel), false);
					player.replaceShortCuts(skillId, skillId + 1);
				}
				break;
			}
			case WATER_STANCE:
			{
				for (Skill skill : _skillList)
				{
					final int skillId = skill.getId();
					final int skillLevel = player.getSkillLevel(skillId);
					player.removeSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
					player.addSkill(SkillData.getInstance().getSkill(skillId + 2, skillLevel), false);
					player.replaceShortCuts(skillId, skillId + 2);
				}
				break;
			}
			case WIND_STANCE:
			{
				for (Skill skill : _skillList)
				{
					final int skillId = skill.getId();
					final int skillLevel = player.getSkillLevel(skillId);
					player.removeSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
					player.addSkill(SkillData.getInstance().getSkill(skillId + 3, skillLevel), false);
					player.replaceShortCuts(skillId, skillId + 3);
				}
				break;
			}
			case EARTH_STANCE:
			{
				for (Skill skill : _skillList)
				{
					final int skillId = skill.getId();
					final int skillLevel = player.getSkillLevel(skillId);
					player.removeSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
					player.addSkill(SkillData.getInstance().getSkill(skillId + 4, skillLevel), false);
					player.replaceShortCuts(skillId, skillId + 4);
				}
				break;
			}
		}
		player.sendSkillList();
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		final L2PcInstance player = (L2PcInstance) info.getEffector();
		switch (_stanceId)
		{
			case FIRE_STANCE:
			{
				for (Skill skill : _skillList)
				{
					final int skillId = skill.getId();
					final int skillLevel = player.getSkillLevel(skillId + 1);
					player.removeSkill(SkillData.getInstance().getSkill(skillId + 1, skillLevel), false);
					player.addSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
					player.replaceShortCuts(skillId + 1, skillId);
				}
				break;
			}
			case WATER_STANCE:
			{
				for (Skill skill : _skillList)
				{
					final int skillId = skill.getId();
					final int skillLevel = player.getSkillLevel(skillId + 2);
					player.removeSkill(SkillData.getInstance().getSkill(skillId + 2, skillLevel), false);
					player.addSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
					player.replaceShortCuts(skillId + 2, skillId);
				}
				break;
			}
			case WIND_STANCE:
			{
				for (Skill skill : _skillList)
				{
					final int skillId = skill.getId();
					final int skillLevel = player.getSkillLevel(skillId + 3);
					player.removeSkill(SkillData.getInstance().getSkill(skillId + 3, skillLevel), false);
					player.addSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
					player.replaceShortCuts(skillId + 3, skillId);
				}
				break;
			}
			case EARTH_STANCE:
			{
				for (Skill skill : _skillList)
				{
					final int skillId = skill.getId();
					final int skillLevel = player.getSkillLevel(skillId + 4);
					player.removeSkill(SkillData.getInstance().getSkill(skillId + 4, skillLevel), false);
					player.addSkill(SkillData.getInstance().getSkill(skillId, skillLevel), false);
					player.replaceShortCuts(skillId + 4, skillId);
				}
				break;
			}
		}
		player.sendSkillList();
	}
}
