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
package ai.bosses.Fafurion;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.model.variables.NpcVariables;

import ai.AbstractNpcAI;

/**
 * @author NviX
 */
public class FafurionBoss extends AbstractNpcAI
{
	// NPCs
	private static final int FAFURION_STAGE_1 = 29361;
	private static final int FAFURION_STAGE_2 = 29362;
	private static final int FAFURION_STAGE_3 = 29363;
	private static final int FAFURION_STAGE_4 = 29364;
	private static final int FAFURION_STAGE_5 = 29365;
	private static final int FAFURION_STAGE_6 = 29366;
	private static final int FAFURION_STAGE_7 = 29367;
	// Skills
	private static final SkillHolder FAFURION_NORMAL_ATTACK = new SkillHolder(32705, 1);
	private static final SkillHolder FAFURION_BITE = new SkillHolder(32706, 1);
	private static final SkillHolder FAFURION_WATER_BLAST = new SkillHolder(32708, 1);
	private static final SkillHolder FAFURION_WATER_SPIRAL = new SkillHolder(32709, 1);
	private static final SkillHolder FAFURION_HYDRO_BLAST = new SkillHolder(32711, 1);
	private static final SkillHolder FAFURION_HYDRO_SPIRAL = new SkillHolder(32712, 1);
	private static final SkillHolder FAFURION_TAIL_STRIKE_1 = new SkillHolder(32713, 1);
	private static final SkillHolder FAFURION_TAIL_STRIKE_2 = new SkillHolder(32714, 1);
	private static final SkillHolder FAFURION_WATER_WAVE = new SkillHolder(32715, 1);
	private static final SkillHolder FAFURION_BREATH = new SkillHolder(32716, 1);
	private static final SkillHolder FAFURION_FEAR = new SkillHolder(32717, 1);
	private static final SkillHolder FAFURION_TIDAL_WAVE = new SkillHolder(32723, 1);
	
	private FafurionBoss()
	{
		registerMobs(FAFURION_STAGE_1, FAFURION_STAGE_2, FAFURION_STAGE_3, FAFURION_STAGE_4, FAFURION_STAGE_5, FAFURION_STAGE_6, FAFURION_STAGE_7);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "MANAGE_SKILLS":
			{
				if (npc != null)
				{
					manageSkills(npc);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		if ((npc.getId() == FAFURION_STAGE_1) || (npc.getId() == FAFURION_STAGE_2) || (npc.getId() == FAFURION_STAGE_3) || (npc.getId() == FAFURION_STAGE_4) || (npc.getId() == FAFURION_STAGE_5) || (npc.getId() == FAFURION_STAGE_6) || (npc.getId() == FAFURION_STAGE_7))
		{
			if (skill == null)
			{
				refreshAiParams(attacker, npc, (damage * 1000));
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.25))
			{
				refreshAiParams(attacker, npc, ((damage / 3) * 100));
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.5))
			{
				refreshAiParams(attacker, npc, (damage * 20));
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.75))
			{
				refreshAiParams(attacker, npc, (damage * 10));
			}
			else
			{
				refreshAiParams(attacker, npc, ((damage / 3) * 20));
			}
			manageSkills(npc);
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	private final void refreshAiParams(Creature attacker, Npc npc, int damage)
	{
		refreshAiParams(attacker, npc, damage, damage);
	}
	
	private final void refreshAiParams(Creature attacker, Npc npc, int damage, int aggro)
	{
		final int newAggroVal = damage + getRandom(3000);
		final int aggroVal = aggro + 1000;
		final NpcVariables vars = npc.getVariables();
		for (int i = 0; i < 3; i++)
		{
			if (attacker == vars.getObject("c_quest" + i, Creature.class))
			{
				if (vars.getInt("i_quest" + i) < aggroVal)
				{
					vars.set("i_quest" + i, newAggroVal);
				}
				return;
			}
		}
		final int index = CommonUtil.getIndexOfMinValue(vars.getInt("i_quest0"), vars.getInt("i_quest1"), vars.getInt("i_quest2"));
		vars.set("i_quest" + index, newAggroVal);
		vars.set("c_quest" + index, attacker);
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		startQuestTimer("MANAGE_SKILLS", 1000, npc, null);
		return super.onSpellFinished(npc, player, skill);
	}
	
	private void manageSkills(Npc npc)
	{
		if (npc.isCastingNow(SkillCaster::isAnyNormalType) || npc.isCoreAIDisabled() || !npc.isInCombat())
		{
			return;
		}
		
		final NpcVariables vars = npc.getVariables();
		for (int i = 0; i < 3; i++)
		{
			final Creature attacker = vars.getObject("c_quest" + i, Creature.class);
			if ((attacker == null) || ((npc.calculateDistance3D(attacker) > 9000) || attacker.isDead()))
			{
				vars.set("i_quest" + i, 0);
			}
		}
		final int index = CommonUtil.getIndexOfMaxValue(vars.getInt("i_quest0"), vars.getInt("i_quest1"), vars.getInt("i_quest2"));
		final Creature player = vars.getObject("c_quest" + index, Creature.class);
		final int i2 = vars.getInt("i_quest" + index);
		if ((i2 > 0) && (getRandom(100) < 70))
		{
			vars.set("i_quest" + index, 500);
		}
		
		SkillHolder skillToCast = null;
		if ((player != null) && !player.isDead())
		{
			int chance = getRandom(100);
			if (npc.getCurrentHp() > (npc.getMaxHp() * 0.8))
			{
				if (chance < 30)
				{
					skillToCast = FAFURION_WATER_BLAST;
				}
				else if (chance < 40)
				{
					skillToCast = FAFURION_FEAR;
				}
				else if (chance < 60)
				{
					skillToCast = FAFURION_BREATH;
				}
				else
				{
					skillToCast = FAFURION_NORMAL_ATTACK;
				}
			}
			else if (npc.getCurrentHp() > (npc.getMaxHp() * 0.6))
			{
				if (chance < 15)
				{
					skillToCast = FAFURION_WATER_BLAST;
				}
				else if (chance < 10)
				{
					skillToCast = FAFURION_FEAR;
				}
				else if (chance < 35)
				{
					skillToCast = FAFURION_BREATH;
				}
				else if (chance < 50)
				{
					skillToCast = FAFURION_TAIL_STRIKE_1;
				}
				else if (chance < 65)
				{
					skillToCast = FAFURION_TAIL_STRIKE_2;
				}
				else
				{
					skillToCast = FAFURION_NORMAL_ATTACK;
				}
			}
			else if (npc.getCurrentHp() > (npc.getMaxHp() * 0.3))
			{
				if (chance < 5)
				{
					skillToCast = FAFURION_WATER_BLAST;
				}
				else if (chance < 10)
				{
					skillToCast = FAFURION_FEAR;
				}
				else if (chance < 15)
				{
					skillToCast = FAFURION_BREATH;
				}
				else if (chance < 20)
				{
					skillToCast = FAFURION_TAIL_STRIKE_1;
				}
				else if (chance < 30)
				{
					skillToCast = FAFURION_TAIL_STRIKE_2;
				}
				else if (chance < 40)
				{
					skillToCast = FAFURION_BITE;
				}
				else if (chance < 50)
				{
					skillToCast = FAFURION_WATER_SPIRAL;
				}
				else if (chance < 60)
				{
					skillToCast = FAFURION_HYDRO_BLAST;
				}
				else if (chance < 70)
				{
					skillToCast = FAFURION_HYDRO_SPIRAL;
				}
				else if (chance < 80)
				{
					skillToCast = FAFURION_WATER_WAVE;
				}
				else if (chance < 90)
				{
					skillToCast = FAFURION_TIDAL_WAVE;
				}
				else
				{
					skillToCast = FAFURION_NORMAL_ATTACK;
				}
			}
		}
		
		if ((skillToCast != null) && SkillCaster.checkUseConditions(npc, skillToCast.getSkill()))
		{
			npc.setTarget(player);
			npc.doCast(skillToCast.getSkill());
		}
	}
	
	public static void main(String[] args)
	{
		new FafurionBoss();
	}
}
