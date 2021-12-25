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
package instances.EtisVanEtinaSolo;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.FriendlyNpc;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureAttacked;
import org.l2jmobius.gameserver.model.events.impl.instance.OnInstanceStatusChange;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

import ai.AbstractNpcAI;

/**
 * Etina Helper Aliber AI.
 * @author CostyKiller
 */
public class EtinaHelperAliber extends AbstractNpcAI
{
	// NPCs
	private static final int ETINA_HELPER_ALIBER = 34475;
	// Corridor Mobs
	//@formatter:off
	private static final int[] CORRIDOR_MOBS_1 = {24173, 24174, 24175, 24176};
	private static final int[] CORRIDOR_MOBS_2 = {24180, 24181, 24182, 24183};
	private static final int[] CORRIDOR_MOBS_3 = {24184, 24185, 24186, 24187, 24188, 24189, 24190, 24191, 24192};
	private static final int[] NOT_ATK_NPCS =
	{
		33798, // Common Decoration - Barricade
		19677, // Seal of Gnosis
		19678, // Seal of Strife
		19679, // Seal of Avarice
		19680, // Seal of Punishment
		19681, // Seal of Awakening
		19682, // Seal of Calamity
		19683, // Seal of Destruction
	};
	private static final int[] ETINA_HELPERS =
	{
		34471, // Leona Blackbird
		34472, // Devianne
		34473, // Elikia
		34474, // Sporcha
		34475, // Aliber
	};
	// Skill Cast Locations
	private static final Location[] SKILL_CAST_LOCATIONS =
	{
		new Location(-245751, 182321, 2861, 16612), // First barricade line
		new Location(-245751, 184001, 3025, 16612), // Second barricade line
		new Location(-245751, 185298, 3031, 16612), // Third barricade line
		new Location(-245751, 186612, 3038, 16612), // Fourth barricade line
	};
	// Support Npcs Skills
	private static final SkillHolder BARRICADE_DESTRUCTION = new SkillHolder(32286, 1);
	// Misc
	private static final int[] ETINA_SOLO_INSTANCES =
	{
		292, // Fall of Etina (Solo)
		293, // Fall of Etina (Solo)
	};
	//@formatter:on
	
	private EtinaHelperAliber()
	{
		addSpellFinishedId(ETINA_HELPER_ALIBER);
		addCreatureSeeId(ETINA_HELPER_ALIBER);
		setCreatureAttackedId(this::onCreatureAttacked, ETINA_HELPER_ALIBER);
		setInstanceStatusChangeId(this::onInstanceStatusChange, ETINA_SOLO_INSTANCES);
	}
	
	@Override
	public void onTimerEvent(String event, StatSet params, Npc npc, Player player)
	{
		final Instance instance = npc.getInstanceWorld();
		if (instance != null)
		{
			switch (event)
			{
				case "CHECK_ACTION":
				{
					final StatSet npcVars = npc.getVariables();
					final Player plr = npcVars.getObject("PLAYER_OBJECT", Player.class);
					if (plr != null)
					{
						final double distance = npc.calculateDistance2D(plr);
						if (!npc.isAttackingNow() && !npc.isMoving() && (distance > 250))
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
							((FriendlyNpc) npc).setCanReturnToSpawnPoint(false);
						}
						else if (!npc.isInCombat() || !npc.isAttackingNow() || (npc.getTarget() == null))
						{
							WorldObject target = npc.getTarget();
							if (target == null)
							{
								npc.setTarget(getRandomEntry(World.getInstance().getVisibleObjectsInRange(npc, Monster.class, 2500)));
							}
							if ((target != null) && !target.isInvul() && target.isTargetable() && GeoEngine.getInstance().canSeeTarget(npc, target) && !CommonUtil.contains(NOT_ATK_NPCS, target.getId()) && !CommonUtil.contains(ETINA_HELPERS, target.getId()))
							{
								npc.setInvul(true);
								npc.setRunning();
								((FriendlyNpc) npc).setCanReturnToSpawnPoint(false);
								addAttackDesire(npc, (Creature) target);
								// addSkillCastDesire(npc, target, ALIBER_SPLASH, 23);
								// npc.reduceCurrentHp(1, monster, null);
							}
						}
					}
					break;
				}
				case "DESTROY_BARRICADES":
				{
					final FriendlyNpc aliber = (FriendlyNpc) instance.getNpc(ETINA_HELPER_ALIBER);
					boolean destroyed = instance.getParameters().getBoolean("BARRICADE_DESTROYED", false);
					if (!instance.getNpcsOfGroup("BARRICADES_1").isEmpty())
					{
						aliber.setRunning();
						aliber.setInvul(true);
						aliber.setCanReturnToSpawnPoint(false);
						aliber.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SKILL_CAST_LOCATIONS[0]);
						if (aliber.getLocation().isIn2DRadius(SKILL_CAST_LOCATIONS[0], 200) && !destroyed)
						{
							aliber.broadcastPacket(new MagicSkillUse(aliber, aliber, BARRICADE_DESTRUCTION.getSkillId(), 1, 500, 0));
							aliber.broadcastPacket(new ExShowScreenMessage(NpcStringId.ALIBER_IS_CASTING_A_SPELL_TO_DESTROY_THE_BARRICADE, ExShowScreenMessage.TOP_CENTER, 7000, true));
							instance.getNpcsOfGroup("BARRICADES_1").forEach(barricade ->
							{
								if (barricade != null)
								{
									barricade.decayMe();
								}
							});
							instance.getParameters().set("BARRICADE_DESTROYED", true);
							instance.despawnGroup("BARRICADES_1");
						}
					}
					else if (!instance.getNpcsOfGroup("BARRICADES_2").isEmpty() && (instance.getAliveNpcCount(CORRIDOR_MOBS_1) == 0) && instance.getNpcsOfGroup("BARRICADES_1").isEmpty())
					{
						aliber.setRunning();
						aliber.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SKILL_CAST_LOCATIONS[1]);
						if (aliber.getLocation().isIn2DRadius(SKILL_CAST_LOCATIONS[1], 200) && !destroyed)
						{
							aliber.broadcastPacket(new MagicSkillUse(aliber, aliber, BARRICADE_DESTRUCTION.getSkillId(), 1, 500, 0));
							aliber.broadcastPacket(new ExShowScreenMessage(NpcStringId.ALIBER_IS_CASTING_A_SPELL_TO_DESTROY_THE_BARRICADE, ExShowScreenMessage.TOP_CENTER, 7000, true));
							instance.getNpcsOfGroup("BARRICADES_2").forEach(barricade ->
							{
								if (barricade != null)
								{
									barricade.decayMe();
								}
							});
							instance.getParameters().set("BARRICADE_DESTROYED", true);
							instance.despawnGroup("BARRICADES_2");
						}
					}
					else if (!instance.getNpcsOfGroup("BARRICADES_3").isEmpty() && (instance.getAliveNpcCount(CORRIDOR_MOBS_2) == 0) && instance.getNpcsOfGroup("BARRICADES_2").isEmpty())
					{
						aliber.setRunning();
						aliber.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SKILL_CAST_LOCATIONS[2]);
						if (aliber.getLocation().isIn2DRadius(SKILL_CAST_LOCATIONS[2], 200) && !destroyed)
						{
							aliber.broadcastPacket(new MagicSkillUse(aliber, aliber, BARRICADE_DESTRUCTION.getSkillId(), 1, 500, 0));
							aliber.broadcastPacket(new ExShowScreenMessage(NpcStringId.ALIBER_IS_CASTING_A_SPELL_TO_DESTROY_THE_BARRICADE, ExShowScreenMessage.TOP_CENTER, 7000, true));
							instance.getNpcsOfGroup("BARRICADES_3").forEach(barricade ->
							{
								if (barricade != null)
								{
									barricade.decayMe();
								}
							});
							instance.getParameters().set("BARRICADE_DESTROYED", true);
							instance.despawnGroup("BARRICADES_3");
						}
					}
					else if (!instance.getNpcsOfGroup("BARRICADES_4").isEmpty() && (instance.getAliveNpcCount(CORRIDOR_MOBS_3) == 0) && instance.getNpcsOfGroup("BARRICADES_3").isEmpty())
					{
						aliber.setRunning();
						aliber.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SKILL_CAST_LOCATIONS[3]);
						if (aliber.getLocation().isIn2DRadius(SKILL_CAST_LOCATIONS[3], 200) && !destroyed)
						{
							aliber.broadcastPacket(new MagicSkillUse(aliber, aliber, BARRICADE_DESTRUCTION.getSkillId(), 1, 500, 0));
							aliber.broadcastPacket(new ExShowScreenMessage(NpcStringId.ALIBER_IS_CASTING_A_SPELL_TO_DESTROY_THE_BARRICADE, ExShowScreenMessage.TOP_CENTER, 7000, true));
							instance.getNpcsOfGroup("BARRICADES_4").forEach(barricade ->
							{
								if (barricade != null)
								{
									barricade.decayMe();
								}
							});
							instance.getParameters().set("BARRICADE_DESTROYED", true);
							instance.despawnGroup("BARRICADES_4");
						}
					}
					break;
				}
			}
		}
	}
	
	public void onCreatureAttacked(OnCreatureAttacked event)
	{
		final FriendlyNpc npc = (FriendlyNpc) event.getTarget();
		if ((npc != null) && !npc.isInCombat())
		{
			final Instance instance = npc.getInstanceWorld();
			if ((instance != null) && !event.getAttacker().isPlayable() && (getRandom(50) < 5) && !CommonUtil.contains(ETINA_HELPERS, event.getAttacker().getId()))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.RAGE_AGAINST_ALL_THAT_STANDS_IN_OUR_WAY);
			}
		}
	}
	
	public void onInstanceStatusChange(OnInstanceStatusChange event)
	{
		final int status = event.getStatus();
		if ((status == 1) || (status == 2) || (status == 3))
		{
			final Instance instance = event.getWorld();
			instance.getAliveNpcs(ETINA_HELPER_ALIBER).forEach(etinaHelperAliber -> getTimers().addRepeatingTimer("CHECK_ACTION", 3000, etinaHelperAliber, null));
			instance.getAliveNpcs(ETINA_HELPER_ALIBER).forEach(etinaHelperAliber -> getTimers().addRepeatingTimer("USE_SKILL", 6000, etinaHelperAliber, null));
			instance.getAliveNpcs(ETINA_HELPER_ALIBER).forEach(etinaHelperAliber -> getTimers().addRepeatingTimer("DESTROY_BARRICADES", 5000, etinaHelperAliber, null));
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
		new EtinaHelperAliber();
	}
}