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
package ai.bosses.Anakim;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.skills.SkillCaster;
import org.l2jmobius.gameserver.model.variables.NpcVariables;

import ai.AbstractNpcAI;

/**
 * @author NviX
 */
public class AnakimBoss extends AbstractNpcAI
{
	// Npc
	private static final int ANAKIM = 29348;
	// Skills
	private static final SkillHolder POWER_STRIKE = new SkillHolder(32566, 1);
	private static final SkillHolder POWER_MULTI_SHOT = new SkillHolder(32567, 1);
	private static final SkillHolder HOLY_VENGEANCE = new SkillHolder(32568, 1);
	private static final SkillHolder HOLY_DIMENSION = new SkillHolder(32569, 1);
	private static final SkillHolder HOLY_SHIELD = new SkillHolder(32570, 1);
	// Others
	private boolean _hp75 = false;
	private boolean _hp50 = false;
	private boolean _hp25 = false;
	
	private AnakimBoss()
	{
		registerMobs(ANAKIM);
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
		if (npc.getId() == ANAKIM)
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
		int chance = getRandom(100);
		if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.75)) && !_hp75)
		{
			_hp75 = true;
			npc.abortCast();
			npc.abortAttack();
			npc.setTarget(npc);
			npc.doCast(HOLY_SHIELD.getSkill());
			return;
		}
		else if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.50)) && !_hp50)
		{
			_hp50 = true;
			npc.abortCast();
			npc.abortAttack();
			npc.setTarget(npc);
			npc.doCast(HOLY_SHIELD.getSkill());
			return;
		}
		else if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.25)) && !_hp25)
		{
			_hp25 = true;
			npc.abortCast();
			npc.abortAttack();
			npc.setTarget(npc);
			npc.doCast(HOLY_SHIELD.getSkill());
			return;
		}
		if ((player != null) && !player.isDead())
		{
			if (chance < 15)
			{
				skillToCast = HOLY_DIMENSION;
			}
			else if (chance < 30)
			{
				skillToCast = HOLY_VENGEANCE;
			}
			else if (chance < 50)
			{
				skillToCast = POWER_MULTI_SHOT;
			}
			else
			{
				skillToCast = POWER_STRIKE;
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
		new AnakimBoss();
	}
}
