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
package instances.ResidenceOfQueenNebula;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skills.SkillCaster;

import instances.AbstractInstance;

/**
 * @author RobikBobik
 * @NOTE: Retail like working
 * @TODO: Rewrite code to modern style.
 * @TODO: Nebula uses attacks on a surface, summons minions (Water Slime) and casts
 * @TODO: The less Nebula's HP, the more damage she deals.
 */
public class ResidenceOfQueenNebula extends AbstractInstance
{
	// NPCs
	private static final int IRIS = 34046;
	private static final int NEBULA = 29106;
	private static final int WATER_SLIME = 29111;
	// Misc
	private static final int TEMPLATE_ID = 196;
	// Skills
	// Debuffs which reduces Speed and increases the damage received (the effect stacks up to 5 times). When it's stacked to 5 times, the character becomes unable to move or make any actions.
	private static SkillHolder AQUA_RAGE_1 = new SkillHolder(50036, 1);
	private static SkillHolder AQUA_RAGE_2 = new SkillHolder(50036, 2);
	private static SkillHolder AQUA_RAGE_3 = new SkillHolder(50036, 3);
	private static SkillHolder AQUA_RAGE_4 = new SkillHolder(50036, 4);
	private static SkillHolder AQUA_RAGE_5 = new SkillHolder(50036, 5);
	
	public ResidenceOfQueenNebula()
	{
		super(TEMPLATE_ID);
		addStartNpc(IRIS);
		addKillId(NEBULA);
		addSpawnId(NEBULA);
		addInstanceLeaveId(TEMPLATE_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		switch (event)
		{
			case "ENTER":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				break;
			}
			case "SPAWN_WATER_SLIME":
			{
				startQuestTimer("CAST_AQUA_RAGE", 5000, npc, player);
				if (npc.getId() == NEBULA)
				{
					for (int i = 0; i < getRandom(4, 6); i++)
					{
						addSpawn(npc, WATER_SLIME, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, -1, true, npc.getInstanceId());
						startQuestTimer("SPAWN_WATER_SLIME", 300000, npc, null);
					}
				}
				break;
			}
			case "PLAYER_PARA":
			{
				player.setIsImmobilized(true);
				startQuestTimer("PLAYER_UNPARA", 30000, npc, player);
				break;
			}
			case "PLAYER_UNPARA":
			{
				player.getEffectList().stopSkillEffects(true, AQUA_RAGE_5.getSkill());
				player.setIsImmobilized(false);
				break;
			}
			case "CAST_AQUA_RAGE":
			{
				startQuestTimer("CAST_AQUA_RAGE", 10000, npc, player);
				if (!player.isAffectedBySkill(AQUA_RAGE_1))
				{
					if (SkillCaster.checkUseConditions(npc, AQUA_RAGE_1.getSkill()))
					{
						npc.doCast(AQUA_RAGE_1.getSkill());
					}
				}
				else if (player.isAffectedBySkill(AQUA_RAGE_1))
				{
					if (SkillCaster.checkUseConditions(npc, AQUA_RAGE_2.getSkill()))
					{
						player.getEffectList().stopSkillEffects(true, AQUA_RAGE_1.getSkill());
						npc.doCast(AQUA_RAGE_2.getSkill());
					}
				}
				else if (player.isAffectedBySkill(AQUA_RAGE_2))
				{
					if (SkillCaster.checkUseConditions(npc, AQUA_RAGE_3.getSkill()))
					{
						player.getEffectList().stopSkillEffects(true, AQUA_RAGE_2.getSkill());
						npc.doCast(AQUA_RAGE_3.getSkill());
					}
				}
				else if (player.isAffectedBySkill(AQUA_RAGE_3))
				{
					if (SkillCaster.checkUseConditions(npc, AQUA_RAGE_4.getSkill()))
					{
						player.getEffectList().stopSkillEffects(true, AQUA_RAGE_3.getSkill());
						npc.doCast(AQUA_RAGE_4.getSkill());
					}
				}
				else if (player.isAffectedBySkill(AQUA_RAGE_4))
				{
					if (SkillCaster.checkUseConditions(npc, AQUA_RAGE_5.getSkill()))
					{
						player.getEffectList().stopSkillEffects(true, AQUA_RAGE_4.getSkill());
						npc.doCast(AQUA_RAGE_5.getSkill());
						startQuestTimer("PLAYER_PARA", 1000, npc, player);
					}
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimer("SPAWN_WATER_SLIME", 12000, npc, null);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		if (npc.getId() == NEBULA)
		{
			final Instance world = npc.getInstanceWorld();
			if (world != null)
			{
				world.finishInstance();
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new ResidenceOfQueenNebula();
	}
}
