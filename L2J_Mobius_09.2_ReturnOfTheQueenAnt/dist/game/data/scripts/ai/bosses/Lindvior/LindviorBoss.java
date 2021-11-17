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
package ai.bosses.Lindvior;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;

import ai.AbstractNpcAI;

/**
 * LindviorBoss AI
 * @author Gigi
 * @date 2017-08-02 - [11:05:21]
 */
public class LindviorBoss extends AbstractNpcAI
{
	// Boss
	private static final int LINDVIOR_GROUND = 25899;
	private static final int LINDVIOR_RAID = 29240;
	private static final int LINDVIOR_FLY = 19424;
	// Skills
	private static final SkillHolder MASS_HELL_BINDING = new SkillHolder(11052, 6);
	private static final SkillHolder MIGHTY_WIND_STRIKE = new SkillHolder(15274, 1);
	private static final SkillHolder WIND_PULL = new SkillHolder(15591, 1);
	private static final SkillHolder LINDVIORS_JUMP = new SkillHolder(15430, 1);
	private static final SkillHolder BODY_SLAM = new SkillHolder(15271, 1);
	private static final SkillHolder SOAR = new SkillHolder(15279, 1);
	private static final SkillHolder WIND_BREAT = new SkillHolder(15272, 1);
	private static final SkillHolder TAIL_SWIPE = new SkillHolder(15273, 1);
	private static final SkillHolder TORNADO = new SkillHolder(15275, 1);
	private static final SkillHolder LINDVIORS_ATTACK = new SkillHolder(15600, 1);
	// Chances
	private static final int CHANCE_WIND_PULL = 4;
	private static final int CHANCE_LINDVIORS_JUMP = 7;
	private static final int CHANCE_BODY_SLAM = 2;
	private static final int CHANCE_SOAR = 8;
	private static final int CHANCE_WIND_BREAT = 3;
	private static final int CHANCE_TAIL_SWIPE = 5;
	private static final int CHANCE_TORNADO = 6;
	private static final int CHANCE_LINDVIORS_ATTACK = 1;
	
	public LindviorBoss()
	{
		super();
		addAttackId(LINDVIOR_GROUND, LINDVIOR_FLY, LINDVIOR_RAID);
		addSpawnId(LINDVIOR_FLY);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if (!npc.isCastingNow())
		{
			final int chance = getRandom(100);
			switch (npc.getId())
			{
				case LINDVIOR_GROUND:
				{
					if (chance <= CHANCE_WIND_PULL)
					{
						npc.setTarget(attacker);
						npc.doCast(WIND_PULL.getSkill());
					}
					else if (chance <= CHANCE_LINDVIORS_JUMP)
					{
						npc.setTarget(attacker);
						npc.doCast(LINDVIORS_JUMP.getSkill());
					}
					else if (chance <= CHANCE_BODY_SLAM)
					{
						npc.setTarget(attacker);
						npc.doCast(BODY_SLAM.getSkill());
					}
					else if (chance <= CHANCE_TAIL_SWIPE)
					{
						npc.setTarget(attacker);
						npc.doCast(TAIL_SWIPE.getSkill());
					}
					else
					{
						npc.setTarget(attacker);
						npc.doCast(MIGHTY_WIND_STRIKE.getSkill());
					}
					break;
				}
				case LINDVIOR_FLY:
				{
					if (chance <= CHANCE_SOAR)
					{
						npc.setTarget(attacker);
						npc.doCast(SOAR.getSkill());
					}
					else
					{
						npc.setTarget(attacker);
						npc.doCast(MIGHTY_WIND_STRIKE.getSkill());
					}
					break;
				}
				case LINDVIOR_RAID:
				{
					if (chance <= CHANCE_WIND_BREAT)
					{
						npc.setTarget(attacker);
						npc.doCast(WIND_BREAT.getSkill());
					}
					else if (chance <= CHANCE_WIND_PULL)
					{
						npc.setTarget(attacker);
						npc.doCast(WIND_PULL.getSkill());
					}
					else if (chance <= CHANCE_TAIL_SWIPE)
					{
						npc.setTarget(attacker);
						npc.doCast(TAIL_SWIPE.getSkill());
					}
					else if (chance <= CHANCE_TORNADO)
					{
						npc.setTarget(attacker);
						npc.doCast(TORNADO.getSkill());
					}
					else if (chance <= CHANCE_LINDVIORS_ATTACK)
					{
						npc.setTarget(attacker);
						npc.doCast(LINDVIORS_ATTACK.getSkill());
					}
					else
					{
						npc.setTarget(attacker);
						npc.doCast(MIGHTY_WIND_STRIKE.getSkill());
					}
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.setRandomWalking(true);
		npc.doCast(MASS_HELL_BINDING.getSkill());
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new LindviorBoss();
	}
}