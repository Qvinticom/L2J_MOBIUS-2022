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
package instances.KartiasLabyrinth;

import java.util.List;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureAttacked;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureDeath;
import org.l2jmobius.gameserver.model.events.impl.instance.OnInstanceStatusChange;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Kartia Helper Adolph AI.
 * @author flanagak
 */
public class KartiaHelperAdolph extends AbstractNpcAI
{
	// NPCs
	private static final int[] KARTIA_ADOLPH =
	{
		33609, // Adolph (Kartia 85)
		33620, // Adolph (Kartia 90)
		33631, // Adolph (Kartia 95)
	};
	private static final int[] MIRRORS =
	{
		33798, // Life Plunderer (85)
		33799, // Life Plunderer (90)
		33800, // Life Plunderer (95)
	};
	private static final int[] KARTIA_FRIENDS =
	{
		33617, // Elise (Kartia 85)
		33628, // Elise (Kartia 90)
		33639, // Elise (Kartia 95)
		33609, // Adolph (Kartia 85)
		33620, // Adolph (Kartia 90)
		33631, // Adolph (Kartia 95)
		33611, // Barton (Kartia 85)
		33622, // Barton (Kartia 90)
		33633, // Barton (Kartia 95)
		33615, // Eliyah (Kartia 85)
		33626, // Eliyah (Kartia 90)
		33637, // Eliyah (Kartia 95)
		33613, // Hayuk (Kartia 85)
		33624, // Hayuk (Kartia 90)
		33635, // Hayuk (Kartia 95)
		33618, // Eliyah's Guardian Spirit (Kartia 85)
		33629, // Eliyah's Guardian Spirit (Kartia 90)
		33640, // Eliyah's Guardian Spirit (Kartia 95)
	};
	// Misc
	private static final int[] KARTIA_SOLO_INSTANCES =
	{
		205, // Solo 85
		206, // Solo 90
		207, // Solo 95
	};
	
	private KartiaHelperAdolph()
	{
		addSpellFinishedId(KARTIA_ADOLPH);
		addCreatureSeeId(KARTIA_ADOLPH);
		setCreatureKillId(this::onCreatureKill, KARTIA_ADOLPH);
		setCreatureAttackedId(this::onCreatureAttacked, KARTIA_ADOLPH);
		setInstanceStatusChangeId(this::onInstanceStatusChange, KARTIA_SOLO_INSTANCES);
	}
	
	@Override
	public void onTimerEvent(String event, StatSet params, Npc npc, Player player)
	{
		final Instance instance = npc.getInstanceWorld();
		if ((instance != null) && event.equals("CHECK_ACTION"))
		{
			final StatSet npcVars = npc.getVariables();
			final Player plr = npcVars.getObject("PLAYER_OBJECT", Player.class);
			if (plr != null)
			{
				final double distance = npc.calculateDistance2D(plr);
				if (distance > 300)
				{
					final Location loc = new Location(plr.getX(), plr.getY(), plr.getZ() + 50);
					final Location randLoc = new Location(loc.getX() + getRandom(-100, 100), loc.getY() + getRandom(-100, 100), loc.getZ());
					if (distance > 600)
					{
						npc.teleToLocation(loc);
					}
					else
					{
						npc.setRunning();
					}
					addMoveToDesire(npc, randLoc, 23);
				}
				else if (!npc.isInCombat() || (npc.getTarget() == null))
				{
					final List<Monster> monsterList = World.getInstance().getVisibleObjectsInRange(npc, Monster.class, 500);
					if (!monsterList.isEmpty())
					{
						final Monster monster = monsterList.get(getRandom(monsterList.size()));
						if (monster.isTargetable() && GeoEngine.getInstance().canSeeTarget(npc, monster) && !CommonUtil.contains(MIRRORS, monster.getId()) && !CommonUtil.contains(KARTIA_FRIENDS, monster.getId()))
						{
							addAttackDesire(npc, monster);
						}
					}
				}
			}
		}
		else if ((instance != null) && event.equals("USE_SKILL"))
		{
			if (npc.isInCombat() || npc.isAttackingNow() || (npc.getTarget() != null))
			{
				if ((npc.getCurrentMpPercent() > 25) && !CommonUtil.contains(KARTIA_FRIENDS, npc.getTargetId()))
				{
					useRandomSkill(npc);
				}
			}
		}
	}
	
	public void useRandomSkill(Npc npc)
	{
		final Instance instance = npc.getInstanceWorld();
		final WorldObject target = npc.getTarget();
		if (target == null)
		{
			return;
		}
		
		if ((instance != null) && !npc.isCastingNow() && (!CommonUtil.contains(KARTIA_FRIENDS, target.getId())))
		{
			final StatSet instParams = instance.getTemplateParameters();
			final SkillHolder skill1 = instParams.getSkillHolder("adolphHate");
			final SkillHolder skill2 = instParams.getSkillHolder("adolphPunish");
			final SkillHolder skill3 = instParams.getSkillHolder("adolphShield");
			final SkillHolder skill4 = instParams.getSkillHolder("adolphAggression");
			final SkillHolder skill5 = instParams.getSkillHolder("adolphChainHydra");
			final SkillHolder lowHpSkill = instParams.getSkillHolder("adolphUltimate");
			final int numberOfActiveSkills = 5;
			final int randomSkill = getRandom(numberOfActiveSkills + 1);
			if ((lowHpSkill != null) && (npc.getCurrentHpPercent() < 50))
			{
				// NPC has low HP Skill and HP is < 50% HP
				if (!npc.isAffectedBySkill(lowHpSkill.getSkillId()) && (SkillCaster.checkUseConditions(npc, lowHpSkill.getSkill())))
				{
					npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.IT_S_NOT_OVER_YET_2);
					npc.doCast(lowHpSkill.getSkill());
				}
			}
			else
			{
				switch (randomSkill)
				{
					case 0:
					case 1:
					{
						if ((skill1 != null) && SkillCaster.checkUseConditions(npc, skill1.getSkill()))
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.I_WILL_SHOW_YOU_THE_JUSTICE_OF_ADEN);
							npc.doCast(skill1.getSkill(), null, true, false);
							final List<Monster> monsterList = World.getInstance().getVisibleObjectsInRange(npc, Monster.class, 300);
							if (!monsterList.isEmpty())
							{
								for (Monster monster : monsterList)
								{
									if (monster.isTargetable() && GeoEngine.getInstance().canSeeTarget(npc, monster) && !CommonUtil.contains(MIRRORS, monster.getId()) && !CommonUtil.contains(KARTIA_FRIENDS, monster.getId()))
									{
										monster.addDamageHate(npc, 100, 10000);
									}
								}
							}
						}
						break;
					}
					case 2:
					{
						if ((skill2 != null) && SkillCaster.checkUseConditions(npc, skill2.getSkill()))
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.THOSE_WHO_ARE_IN_FRONT_OF_MY_EYES_WILL_BE_DESTROYED_3);
							npc.doCast(skill2.getSkill(), null, true, false);
						}
						break;
					}
					case 3:
					{
						if ((skill3 != null) && SkillCaster.checkUseConditions(npc, skill3.getSkill()))
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.FOR_THE_GODDESS);
							npc.doCast(skill3.getSkill(), null, true, false);
						}
						break;
					}
					case 4:
					{
						if ((skill4 != null) && SkillCaster.checkUseConditions(npc, skill4.getSkill()))
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOU_WILL_BE_DESTROYED);
							npc.doCast(skill4.getSkill());
						}
						break;
					}
					case 5:
					{
						if ((skill5 != null) && SkillCaster.checkUseConditions(npc, skill5.getSkill()))
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOU_WILL_DIE);
							npc.doCast(skill5.getSkill(), null, true, false);
						}
						break;
					}
				}
			}
		}
	}
	
	public void onCreatureAttacked(OnCreatureAttacked event)
	{
		final Npc npc = (Npc) event.getTarget();
		if (npc != null)
		{
			final Instance instance = npc.getInstanceWorld();
			if ((instance != null) && !event.getAttacker().isPlayable() && !CommonUtil.contains(KARTIA_FRIENDS, event.getAttacker().getId()))
			{
				if (!npc.isInCombat())
				{
					npc.setTarget(event.getAttacker());
					addAttackDesire(npc, (Creature) npc.getTarget());
					final StatSet instParams = instance.getTemplateParameters();
					final SkillHolder hateSkill = instParams.getSkillHolder("adolphHate");
					if ((hateSkill != null) && SkillCaster.checkUseConditions(npc, hateSkill.getSkill()))
					{
						npc.doCast(hateSkill.getSkill(), null, true, false);
					}
				}
			}
		}
	}
	
	public void onCreatureKill(OnCreatureDeath event)
	{
		final Npc npc = (Npc) event.getTarget();
		final Instance world = npc.getInstanceWorld();
		if (world != null)
		{
			getTimers().cancelTimersOf(npc);
			world.destroy(); // Kartia can't continue without Adolph
		}
	}
	
	public void onInstanceStatusChange(OnInstanceStatusChange event)
	{
		final Instance instance = event.getWorld();
		final int status = event.getStatus();
		if (status == 1)
		{
			instance.getAliveNpcs(KARTIA_ADOLPH).forEach(adolph -> getTimers().addRepeatingTimer("CHECK_ACTION", 3000, adolph, null));
			instance.getAliveNpcs(KARTIA_ADOLPH).forEach(adolph -> getTimers().addRepeatingTimer("USE_SKILL", 6000, adolph, null));
		}
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		if (creature.isPlayer())
		{
			npc.getVariables().set("PLAYER_OBJECT", creature.getActingPlayer());
		}
		return super.onCreatureSee(npc, creature);
	}
	
	public static void main(String[] args)
	{
		new KartiaHelperAdolph();
	}
}