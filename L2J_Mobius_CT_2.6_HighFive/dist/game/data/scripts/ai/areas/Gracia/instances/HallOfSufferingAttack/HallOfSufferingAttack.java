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
package ai.areas.Gracia.instances.HallOfSufferingAttack;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;
import quests.Q00694_BreakThroughTheHallOfSuffering.Q00694_BreakThroughTheHallOfSuffering;

public class HallOfSufferingAttack extends AbstractNpcAI
{
	private class HSAWorld extends InstanceWorld
	{
		public HSAWorld()
		{
		}
		
		public Map<Npc, Boolean> npcList = new HashMap<>();
		public Npc klodekus = null;
		public Npc klanikus = null;
		public boolean isBossesAttacked = false;
		public long[] storeTime =
		{
			0,
			0
		}; // 0: instance start, 1: finish time
	}
	
	private static final int INSTANCEID = 115;
	private static final boolean debug = false;
	
	// NPCs
	private static final int MOUTHOFEKIMUS = 32537;
	private static final int TEPIOS = 32530;
	
	// mobs
	private static final int KLODEKUS = 25665;
	private static final int KLANIKUS = 25666;
	private static final int TUMOR_ALIVE = 18704;
	private static final int TUMOR_DEAD = 32531;
	
	//@formatter:off
	private static final int[] ENTER_TELEPORT = {-187567,205570,-9538};
	private static final int[] TUMOR_MOBIDS = {22509,22510,22511,22512,22513,22514,22515};
	private static final int[] TWIN_MOBIDS = {22509,22510,22511,22512,22513};
	
	// Doors/Walls/Zones
	private static final int[][] ROOM_1_MOBS =
	{
		{22509, -186296, 208200, -9544}, {22509, -186161, 208345, -9544}, {22509, -186296, 208403, -9544},
		{22510, -186107, 208113, -9528}, {22510, -186350, 208200, -9544}
	};
	private static final int[][] ROOM_2_MOBS =
	{
		{22511, -184433, 210953, -9536}, {22511, -184406, 211301, -9536}, {22509, -184541, 211272, -9544},
		{22510, -184244, 211098, -9536}, {22510, -184352, 211243, -9536}, {22510, -184298, 211330, -9528}
	};
	private static final int[][] ROOM_3_MOBS =
	{
		{22512, -182611, 213984, -9520}, {22512, -182908, 214071, -9520}, {22512, -182962, 213868, -9512},
		{22509, -182881, 213955, -9512}, {22511, -182827, 213781, -9504}, {22511, -182530, 213984, -9528},
		{22510, -182935, 213723, -9512}, {22510, -182557, 213868, -9520}
	};
	private static final int[][] ROOM_4_MOBS =
	{
		{22514, -180958, 216860, -9544}, {22514, -181012, 216628, -9536}, {22514, -181120, 216715, -9536},
		{22513, -180661, 216599, -9536}, {22513, -181039, 216599, -9536}, {22511, -180715, 216599, -9536},
		{22511, -181012, 216889, -9536}, {22512, -180931, 216918, -9536}, {22512, -180742, 216628, -9536}
	};
	private static final int[][] ROOM_5_MOBS =
	{
		{22512, -177372, 217854, -9536}, {22512, -177237, 218140, -9536}, {22512, -177021, 217647, -9528},
		{22513, -177372, 217792, -9544}, {22513, -177372, 218053, -9536}, {22514, -177291, 217734, -9544},
		{22514, -177264, 217792, -9544}, {22514, -177264, 218053, -9536}, {22515, -177156, 217792, -9536},
		{22515, -177075, 217647, -9528}
	};
	private static final int[][] TUMOR_SPAWNS =
	{
		{-186327,208286,-9544},{-184429,211155,-9544},{-182811,213871,-9496},
		{-181039,216633,-9528},{-177264,217760,-9544}
	};
	private static final int[][] TWIN_SPAWNS = {{25665,-173727,218169,-9536},{25666,-173727,218049,-9536}};
	private static final int[] TEPIOS_SPAWN = {-173727,218109,-9536};
	//@formatter:on
	
	private static final int BOSS_INVUL_TIME = 30000;
	private static final int BOSS_MINION_SPAWN_TIME = 60000;
	private static final int BOSS_RESSURECT_TIME = 20000;
	
	private static final int INSTANCEPENALTY = 24;
	
	public HallOfSufferingAttack()
	{
		addStartNpc(MOUTHOFEKIMUS);
		addStartNpc(TEPIOS);
		addTalkId(MOUTHOFEKIMUS);
		addTalkId(TEPIOS);
		
		addKillId(TUMOR_ALIVE);
		addKillId(KLODEKUS);
		addKillId(KLANIKUS);
		
		addAttackId(KLODEKUS);
		addAttackId(KLANIKUS);
		
		addSkillSeeId(TUMOR_MOBIDS);
		addKillId(TUMOR_MOBIDS);
	}
	
	private boolean checkConditions(Player player)
	{
		if (debug || player.isGM())
		{
			return true;
		}
		
		final Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
			return false;
		}
		for (Player partyMember : party.getMembers())
		{
			if ((partyMember.getLevel() < 75) || (partyMember.getLevel() > 82))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			
			final Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (Chronos.currentTimeMillis() < reentertime)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			
			final QuestState qs = partyMember.getQuestState(Q00694_BreakThroughTheHallOfSuffering.class.getSimpleName());
			if ((qs == null) || !qs.isCond(1))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	private void teleportPlayer(Player player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	protected void enterInstance(Player player, int[] coords)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof HSAWorld))
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
				return;
			}
			teleportPlayer(player, coords, world.getInstanceId());
			return;
		}
		
		if (checkConditions(player))
		{
			world = new HSAWorld();
			world.setInstance(InstanceManager.getInstance().createDynamicInstance(INSTANCEID));
			((HSAWorld) world).storeTime[0] = Chronos.currentTimeMillis();
			InstanceManager.getInstance().addWorld(world);
			LOGGER.info("Hall Of Suffering Attack started " + INSTANCEID + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
			runTumors((HSAWorld) world);
			
			if (player.getParty() == null)
			{
				teleportPlayer(player, coords, world.getInstanceId());
				world.addAllowed(player);
			}
			else
			{
				for (Player partyMember : player.getParty().getMembers())
				{
					teleportPlayer(partyMember, coords, world.getInstanceId());
					world.addAllowed(partyMember);
				}
			}
		}
	}
	
	protected boolean checkKillProgress(Npc mob, HSAWorld world)
	{
		if (world.npcList.containsKey(mob))
		{
			world.npcList.put(mob, true);
		}
		for (boolean isDead : world.npcList.values())
		{
			if (!isDead)
			{
				return false;
			}
		}
		return true;
	}
	
	protected int[][] getRoomSpawns(int room)
	{
		switch (room)
		{
			case 0:
			{
				return ROOM_1_MOBS;
			}
			case 1:
			{
				return ROOM_2_MOBS;
			}
			case 2:
			{
				return ROOM_3_MOBS;
			}
			case 3:
			{
				return ROOM_4_MOBS;
			}
			case 4:
			{
				return ROOM_5_MOBS;
			}
		}
		return new int[][] {};
	}
	
	protected void runTumors(HSAWorld world)
	{
		for (int[] mob : getRoomSpawns(world.getStatus()))
		{
			final Npc npc = addSpawn(mob[0], mob[1], mob[2], mob[3], 0, false, 0, false, world.getInstanceId());
			world.npcList.put(npc, false);
		}
		final Npc mob = addSpawn(TUMOR_ALIVE, TUMOR_SPAWNS[world.getStatus()][0], TUMOR_SPAWNS[world.getStatus()][1], TUMOR_SPAWNS[world.getStatus()][2], 0, false, 0, false, world.getInstanceId());
		mob.disableCoreAI(true);
		mob.setImmobilized(true);
		mob.setCurrentHp(mob.getMaxHp() * 0.5);
		world.npcList.put(mob, false);
		world.incStatus();
	}
	
	protected void runTwins(HSAWorld world)
	{
		world.incStatus();
		world.klodekus = addSpawn(TWIN_SPAWNS[0][0], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, world.getInstanceId());
		world.klanikus = addSpawn(TWIN_SPAWNS[1][0], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, world.getInstanceId());
		world.klanikus.setMortal(false);
		world.klodekus.setMortal(false);
	}
	
	protected void bossSimpleDie(Npc boss)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (boss.isDead())
			{
				return;
			}
			// now reset currentHp to zero
			boss.setCurrentHp(0);
			boss.setDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		boss.setTarget(null);
		
		// Stop movement
		boss.stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		boss.getStatus().stopHpMpRegeneration();
		
		boss.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		boss.broadcastStatusUpdate();
		
		// Notify Creature AI
		boss.getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		
		// if (boss.getWorldRegion() != null)
		// {
		// boss.getWorldRegion().onDeath(boss);
		// }
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		if (skill.hasEffectType(EffectType.REBALANCE_HP, EffectType.HEAL))
		{
			int hate = 2 * skill.getEffectPoint();
			if (hate < 2)
			{
				hate = 1000;
			}
			((Attackable) npc).addDamageHate(caster, 0, hate);
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc);
		if (tmpworld instanceof HSAWorld)
		{
			final HSAWorld world = (HSAWorld) tmpworld;
			if (event.equalsIgnoreCase("spawnBossGuards"))
			{
				if (!world.klanikus.isInCombat() && !world.klodekus.isInCombat())
				{
					world.isBossesAttacked = false;
					return "";
				}
				Npc mob = addSpawn(getRandomEntry(TWIN_MOBIDS), TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, npc.getInstanceId());
				((Attackable) mob).addDamageHate(((Attackable) npc).getMostHated(), 0, 1);
				if (getRandom(100) < 33)
				{
					mob = addSpawn(getRandomEntry(TWIN_MOBIDS), TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, npc.getInstanceId());
					((Attackable) mob).addDamageHate(((Attackable) npc).getMostHated(), 0, 1);
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
			}
			else if (event.equalsIgnoreCase("isTwinSeparated"))
			{
				if (Util.checkIfInRange(500, world.klanikus, world.klodekus, false))
				{
					world.klanikus.setInvul(false);
					world.klodekus.setInvul(false);
				}
				else
				{
					world.klanikus.setInvul(true);
					world.klodekus.setInvul(true);
				}
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (event.equalsIgnoreCase("ressurectTwin"))
			{
				final Skill skill = SkillData.getInstance().getSkill(5824, 1);
				final Npc aliveTwin = (world.klanikus == npc ? world.klodekus : world.klanikus);
				npc.doRevive();
				npc.doCast(skill);
				npc.setCurrentHp(aliveTwin.getCurrentHp());
				
				// get most hated of other boss
				final Creature hated = ((Monster) aliveTwin).getMostHated();
				if (hated != null)
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, 1000);
				}
				
				aliveTwin.setInvul(true); // make other boss invul
				startQuestTimer("uninvul", BOSS_INVUL_TIME, aliveTwin, null);
			}
			else if (event.equals("uninvul"))
			{
				npc.setInvul(false);
			}
		}
		return "";
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc);
		if (tmpworld instanceof HSAWorld)
		{
			final HSAWorld world = (HSAWorld) tmpworld;
			if (!world.isBossesAttacked)
			{
				world.isBossesAttacked = true;
				final Calendar reenter = Calendar.getInstance();
				reenter.add(Calendar.HOUR, INSTANCEPENALTY);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.INSTANT_ZONE_S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_POSSIBLE_ENTRY_TIME_BY_USING_THE_COMMAND_INSTANCEZONE);
				sm.addInstanceName(INSTANCEID);
				
				// set instance reenter time for all allowed players
				for (Player player : tmpworld.getAllowed())
				{
					if (player != null)
					{
						InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID, reenter.getTimeInMillis());
						if (player.isOnline())
						{
							player.sendPacket(sm);
						}
					}
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (damage >= npc.getCurrentHp())
			{
				if (world.klanikus.isDead())
				{
					world.klanikus.setDead(false);
					world.klanikus.doDie(attacker);
					world.klodekus.doDie(attacker);
				}
				else if (((HSAWorld) tmpworld).klodekus.isDead())
				{
					world.klodekus.setDead(false);
					world.klodekus.doDie(attacker);
					world.klanikus.doDie(attacker);
				}
				else
				{
					bossSimpleDie(npc);
					startQuestTimer("ressurectTwin", BOSS_RESSURECT_TIME, npc, null);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc);
		if (tmpworld instanceof HSAWorld)
		{
			final HSAWorld world = (HSAWorld) tmpworld;
			
			if (npc.getId() == TUMOR_ALIVE)
			{
				npc.deleteMe();
				addSpawn(TUMOR_DEAD, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, npc.getInstanceId());
			}
			if (world.getStatus() < 5)
			{
				if (checkKillProgress(npc, world))
				{
					runTumors(world);
				}
			}
			else if (world.getStatus() == 5)
			{
				if (checkKillProgress(npc, world))
				{
					runTwins(world);
				}
			}
			else if (((world.getStatus() == 6) && ((npc.getId() == KLODEKUS) || (npc.getId() == KLANIKUS))) //
				&& (world.klanikus.isDead() && world.klodekus.isDead()))
			{
				world.incStatus();
				world.storeTime[1] = Chronos.currentTimeMillis();
				
				final Long finishDiff = world.storeTime[1] - world.storeTime[0];
				if (finishDiff < 1260000)
				{
					world.setParameter("tag", 13777);
				}
				else if (finishDiff < 1380000)
				{
					world.setParameter("tag", 13778);
				}
				else if (finishDiff < 1500000)
				{
					world.setParameter("tag", 13779);
				}
				else if (finishDiff < 1620000)
				{
					world.setParameter("tag", 13780);
				}
				else if (finishDiff < 1740000)
				{
					world.setParameter("tag", 13781);
				}
				else if (finishDiff < 1860000)
				{
					world.setParameter("tag", 13782);
				}
				else if (finishDiff < 1980000)
				{
					world.setParameter("tag", 13783);
				}
				else if (finishDiff < 2100000)
				{
					world.setParameter("tag", 13784);
				}
				else if (finishDiff < 2220000)
				{
					world.setParameter("tag", 13785);
				}
				else
				{
					world.setParameter("tag", 13786);
				}
				
				world.klanikus = null;
				world.klodekus = null;
				cancelQuestTimers("ressurectTwin");
				cancelQuestTimers("spawnBossGuards");
				cancelQuestTimers("isTwinSeparated");
				addSpawn(TEPIOS, TEPIOS_SPAWN[0], TEPIOS_SPAWN[1], TEPIOS_SPAWN[2], 0, false, 0, false, world.getInstanceId());
				
				for (Player killer : world.getAllowed())
				{
					if (killer != null)
					{
						killer.sendPacket(new ExSendUIEvent(killer, true, true, 0, 0, ""));
					}
				}
				
				final Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
				inst.setDuration(5 * 60000);
				inst.setEmptyDestroyTime(0);
			}
		}
		return "";
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		if (npc.getId() == MOUTHOFEKIMUS)
		{
			enterInstance(player, ENTER_TELEPORT);
			return null;
		}
		return "";
	}
}