/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package instances.LabyrinthOfBelis;

import instances.AbstractInstance;

import java.util.List;

import quests.Q10331_StartOfFate.Q10331_StartOfFate;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.enums.ChatType;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.zone.L2ZoneType;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Labyrinth of Belis Instance Zone.
 * @author Mobius
 */
public final class LabyrinthOfBelis extends AbstractInstance
{
	// Npcs
	private static final int OFFICER = 19155;
	private static final int NEMERTESS = 22984;
	private static final int EMBRYO_HANDYMAN = 22997;
	private static final int EMBRYO_OPERATIVE = 22998;
	private static final int VERIFICATION_SYSTEM = 33215;
	private static final int ELECTRICITY_GENERATOR = 33216;
	// Items
	private static final int SARIL_NECKLACE = 17580;
	private static final int BELIS_MARK = 17615;
	// Locations
	private static final Location TERIAN_SPAWN_LOC = new Location(-119063, 211160, -8592, 32000);
	private static final Location TERIAN_ROOM_2_CORIDOR = new Location(-117996, 211484, -8596);
	private static final Location TERIAN_ROOM_2_WAIT_LOC = new Location(-117041, 212521, -8592);
	private static final Location TERIAN_ROOM_3_CORIDOR = new Location(-116818, 213281, -8596);
	private static final Location TERIAN_ROOM_3_WAIT_LOC = new Location(-117873, 214233, -8592);
	private static final Location TERIAN_ROOM_3_INSIDE = new Location(-118248, 214676, -8590);
	private static final Location TERIAN_ROOM_3_MONSTER_SPAWN = new Location(-116669, 213220, -8594);
	private static final Location TERIAN_ROOM_4_CORIDOR = new Location(-119180, 214033, -8592);
	private static final Location TERIAN_ROOM_4_WAIT_LOC = new Location(-119153, 213732, -8595);
	private static final Location TERIAN_ROOM_4_INSIDE = new Location(-118336, 212973, -8680);
	private static final Location GENERATOR_SPAWN_LOC = new Location(-118253, 214706, -8584, 57541);
	private static final Location NEMERTESS_SPAWN_LOC = new Location(-118336, 212973, -8680);
	private static final Location START_LOC = new Location(-119942, 211142, -8591);
	private static final Location EXIT_LOC = new Location(-111733, 231790, -3168);
	// Misc
	private static final int TEMPLATE_ID = 178;
	private static final int DOOR_1 = 16240001;
	private static final int DOOR_2 = 16240002;
	private static final int DOOR_3 = 16240003;
	private static final int DOOR_4 = 16240004;
	private static final int DOOR_5 = 16240005;
	private static final int DOOR_6 = 16240006;
	private static final int DOOR_7 = 16240007;
	private static final int DOOR_8 = 16240008;
	private static final int DAMAGE_ZONE_ID = 10331;
	
	protected class LOBWorld extends InstanceWorld
	{
		protected L2QuestGuardInstance terian = null;
		protected L2Npc generator = null;
		protected List<L2Npc> savedSpawns = null;
		protected boolean assistPlayer = false;
	}
	
	public LabyrinthOfBelis()
	{
		super(LabyrinthOfBelis.class.getSimpleName());
		addStartNpc(OFFICER);
		addFirstTalkId(OFFICER, VERIFICATION_SYSTEM, ELECTRICITY_GENERATOR);
		addKillId(EMBRYO_OPERATIVE, EMBRYO_HANDYMAN, NEMERTESS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Q10331_StartOfFate.class.getSimpleName());
		if (qs == null)
		{
			return null;
		}
		
		if (event.equals("enter_instance"))
		{
			enterInstance(player, new LOBWorld(), "LabyrinthOfBelis.xml", TEMPLATE_ID);
			return null;
		}
		
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if ((tmpworld == null) || !(tmpworld instanceof LOBWorld))
		{
			return null;
		}
		
		final LOBWorld world = (LOBWorld) tmpworld;
		
		switch (event)
		{
			case "enter_instance":
			{
				enterInstance(player, new LOBWorld(), "LabyrinthOfBelis.xml", TEMPLATE_ID);
				break;
			}
			case "officer_wait_1":
			{
				if (world.getStatus() == 1)
				{
					showOnScreenMsg(player, NpcStringId.LET_ME_KNOW_WHEN_YOU_RE_ALL_READY, ExShowScreenMessage.TOP_CENTER, 4000);
					broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.LET_ME_KNOW_WHEN_YOU_RE_ALL_READY, 1000);
					startQuestTimer("officer_wait_1", 5000, world.terian, player);
				}
				break;
			}
			case "room_1":
			{
				openDoor(DOOR_2, player.getInstanceId());
				world.setStatus(3);
				world.assistPlayer = true;
				startQuestTimer("assist_player", 3000, world.terian, player);
				return null;
			}
			case "assist_player":
			{
				if (world.assistPlayer)
				{
					world.terian.setIsRunning(true);
					if (player.isInCombat() && (player.getTarget() != null) && player.getTarget().isMonster() && !((L2MonsterInstance) player.getTarget()).isAlikeDead())
					{
						if (world.terian.calculateDistance(player.getTarget(), false, false) > 50)
						{
							world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, player.getTarget().getLocation());
						}
						else if (world.terian.getTarget() != player.getTarget())
						{
							world.terian.addDamageHate((L2Character) player.getTarget(), 0, 1000);
						}
					}
					else
					{
						world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
					}
					startQuestTimer("assist_player", 1000, world.terian, player);
				}
				break;
			}
			case "officer_goto_2":
			{
				if (world.terian.calculateDistance(TERIAN_ROOM_2_CORIDOR, false, false) > 10)
				{
					world.terian.setIsRunning(true);
					world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_2_CORIDOR);
					startQuestTimer("officer_goto_2", 1000, world.terian, player);
				}
				else
				{
					startQuestTimer("officer_wait_2", 1000, world.terian, player);
				}
				break;
			}
			case "officer_wait_2":
			{
				if (world.terian.calculateDistance(TERIAN_ROOM_2_WAIT_LOC, false, false) > 10)
				{
					world.terian.setIsRunning(true);
					world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_2_WAIT_LOC);
					startQuestTimer("officer_wait_2", 1000, world.terian, player);
				}
				else
				{
					world.terian.setHeading(world.terian.getHeading() + 32500);
					world.terian.broadcastInfo();
					broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.HEY_YOU_RE_NOT_ALL_BAD_LET_ME_KNOW_WHEN_YOU_RE_READY, 1000);
					world.setStatus(4);
				}
				break;
			}
			case "room_2":
			{
				world.setStatus(5);
				openDoor(DOOR_4, player.getInstanceId());
				showOnScreenMsg(player, NpcStringId.MARK_OF_BELIS_CAN_BE_ACQUIRED_FROM_ENEMIES_NUSE_THEM_IN_THE_BELIS_VERIFICATION_SYSTEM, ExShowScreenMessage.TOP_CENTER, 5000);
				world.assistPlayer = true;
				startQuestTimer("assist_player", 3000, world.terian, player);
				return null;
			}
			case "insert_belis_marks":
			{
				if (getQuestItemsCount(player, BELIS_MARK) > 2)
				{
					takeItems(player, BELIS_MARK, 3);
					openDoor(DOOR_5, player.getInstanceId());
					world.assistPlayer = false;
					broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.COME_ON_ONTO_THE_NEXT_PLACE, 1000);
					startQuestTimer("officer_goto_3", 5000, world.terian, player);
					return "33215-02.html";
				}
				return "33215-03.html";
			}
			case "officer_goto_3":
			{
				if (world.terian.calculateDistance(TERIAN_ROOM_3_CORIDOR, false, false) > 10)
				{
					world.terian.setIsRunning(true);
					world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_3_CORIDOR);
					startQuestTimer("officer_goto_3", 1000, world.terian, player);
				}
				else
				{
					startQuestTimer("officer_wait_3", 1000, world.terian, player);
				}
				break;
			}
			case "officer_wait_3":
			{
				if (world.terian.calculateDistance(TERIAN_ROOM_3_WAIT_LOC, false, false) > 10)
				{
					world.terian.setIsRunning(true);
					world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_3_WAIT_LOC);
					startQuestTimer("officer_wait_3", 1000, world.terian, player);
				}
				else
				{
					world.terian.setHeading(world.terian.getHeading() + 32500);
					world.terian.broadcastInfo();
					broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.READY_LET_ME_KNOW, 1000);
					world.setStatus(6);
				}
				break;
			}
			case "room_3":
			{
				world.setStatus(7);
				world.generator.setState(1);
				final L2ZoneType dmgZone = ZoneManager.getInstance().getZoneById(DAMAGE_ZONE_ID);
				if (dmgZone != null)
				{
					dmgZone.setEnabled(true);
				}
				openDoor(DOOR_6, player.getInstanceId());
				world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_3_INSIDE);
				broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.DON_T_COME_BACK_HERE, 1000);
				startQuestTimer("room_3_spawns", 10000, world.terian, player);
				return null;
			}
			case "room_3_spawns":
			{
				showOnScreenMsg(player, NpcStringId.BEHIND_YOU_THE_ENEMY_IS_AMBUSHING_YOU, ExShowScreenMessage.TOP_CENTER, 4000);
				broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.DON_T_COME_BACK_HERE, 1000);
				// TODO:
				/*
				 * if (getRandomBoolean()) { showOnScreenMsg(player, NpcStringId.IF_TERAIN_DIES_THE_MISSION_WILL_FAIL, ExShowScreenMessage.TOP_CENTER, 4000); }
				 */
				
				final L2Npc invader;
				if (getRandomBoolean())
				{
					invader = addSpawn(EMBRYO_HANDYMAN, TERIAN_ROOM_3_MONSTER_SPAWN, false, 0, true, world.getInstanceId());
				}
				else
				{
					invader = addSpawn(EMBRYO_OPERATIVE, TERIAN_ROOM_3_MONSTER_SPAWN, false, 0, true, world.getInstanceId());
				}
				invader.setSpawn(null);
				((L2Attackable) invader).addDamageHate(world.terian, 0, 1000);
				invader.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_3_INSIDE);
				invader.setRunning();
				
				if (world.getStatus() < 12)
				{
					startQuestTimer("room_3_spawns", 12000, world.terian, player);
				}
				else
				{
					final L2ZoneType dmgZone = ZoneManager.getInstance().getZoneById(DAMAGE_ZONE_ID);
					if (dmgZone != null)
					{
						dmgZone.setEnabled(false);
					}
					if (world.generator != null)
					{
						world.generator.deleteMe();
					}
					openDoor(DOOR_7, player.getInstanceId());
					showOnScreenMsg(player, NpcStringId.ELECTRONIC_DEVICE_HAS_BEEN_DESTROYED, ExShowScreenMessage.TOP_CENTER, 4000);
					broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.DEVICE_DESTROYED_LET_S_GO_ONTO_THE_NEXT, 1000);
					startQuestTimer("officer_goto_4", 1000, world.terian, player);
				}
				break;
			}
			case "officer_goto_4":
			{
				if (world.terian.calculateDistance(TERIAN_ROOM_4_CORIDOR, false, false) > 10)
				{
					world.terian.setIsRunning(true);
					world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_4_CORIDOR);
					startQuestTimer("officer_goto_4", 1000, world.terian, player);
				}
				else
				{
					startQuestTimer("officer_wait_4", 1000, world.terian, player);
				}
				break;
			}
			case "officer_wait_4":
			{
				if (world.terian.calculateDistance(TERIAN_ROOM_4_WAIT_LOC, false, false) > 10)
				{
					world.terian.setIsRunning(true);
					world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_4_WAIT_LOC);
					startQuestTimer("officer_wait_4", 1000, world.terian, player);
				}
				else
				{
					world.terian.setHeading(world.terian.getHeading() + 32500);
					world.terian.broadcastInfo();
					broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.SOMETHING_OMINOUS_IN_THERE_I_HOPE_YOU_RE_REALLY_READY_FOR_THIS_LET_ME_KNOW, 1000);
					world.setStatus(13);
				}
				break;
			}
			case "room_4":
			{
				world.setStatus(14);
				player.showQuestMovie(43);
				openDoor(DOOR_8, player.getInstanceId());
				startQuestTimer("spawn_boss", 47000, world.terian, player);
				break;
			}
			case "spawn_boss":
			{
				addSpawn(NEMERTESS, NEMERTESS_SPAWN_LOC, false, 0, false, world.getInstanceId());
				break;
			}
			case "officer_goto_end":
			{
				world.terian.setIsRunning(true);
				world.terian.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, TERIAN_ROOM_4_INSIDE);
				break;
			}
			case "exit_instance":
			{
				if (world.terian != null)
				{
					world.terian.deleteMe();
				}
				world.removeAllowed(player.getObjectId());
				teleportPlayer(player, EXIT_LOC, 0);
				break;
			}
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		switch (npc.getId())
		{
			case OFFICER:
			{
				final InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				switch (world.getStatus())
				{
					case 1:
					{
						world.incStatus();
						return "19155-01.html";
					}
					case 2:
					{
						return "19155-01.html";
					}
					case 4:
					{
						return "19155-02.html";
					}
					case 6:
					{
						return "19155-03.html";
					}
					case 13:
					{
						return "19155-04.html";
					}
					case 15:
					{
						return "19155-05.html";
					}
					default:
					{
						return "19155-06.html";
					}
				}
			}
			case VERIFICATION_SYSTEM:
			{
				return "33215-01.html";
			}
			case ELECTRICITY_GENERATOR:
			{
				final InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if (world.getStatus() < 12)
				{
					return "33216-01.html";
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(killer);
		if ((tmpworld == null) || !(tmpworld instanceof LOBWorld))
		{
			return null;
		}
		final LOBWorld world = (LOBWorld) tmpworld;
		
		switch (npc.getId())
		{
			case EMBRYO_OPERATIVE:
			{
				if ((world.getStatus() > 6) && (world.getStatus() < 12))
				{
					world.incStatus();
				}
				else
				{
					world.savedSpawns.remove(0);
					if ((world.getStatus() == 3) && world.savedSpawns.isEmpty())
					{
						world.assistPlayer = false;
						openDoor(DOOR_3, npc.getInstanceId());
						broadcastNpcSay(world.terian, ChatType.NPC_GENERAL, NpcStringId.ALL_RIGHT_LET_S_MOVE_OUT, 1000);
						startQuestTimer("officer_goto_2", 100, world.terian, killer);
					}
				}
				break;
			}
			case EMBRYO_HANDYMAN:
			{
				if ((world.getStatus() > 6) && (world.getStatus() < 12))
				{
					world.incStatus();
				}
				else
				{
					if (getRandomBoolean())
					{
						npc.dropItem(killer, BELIS_MARK, 1);
					}
				}
				break;
			}
			case NEMERTESS:
			{
				world.incStatus();
				npc.deleteMe();
				killer.showQuestMovie(44);
				startQuestTimer("officer_goto_end", 20000, world.terian, killer);
				final QuestState qs = killer.getQuestState(Q10331_StartOfFate.class.getSimpleName());
				if (qs == null)
				{
					return null;
				}
				giveItems(killer, SARIL_NECKLACE, 1);
				qs.setCond(5);
				break;
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		final LOBWorld tmpworld = (LOBWorld) world;
		if (firstEntrance)
		{
			tmpworld.setStatus(1);
			tmpworld.addAllowed(player.getObjectId());
			tmpworld.terian = (L2QuestGuardInstance) addSpawn(OFFICER, TERIAN_SPAWN_LOC, false, 0, false, tmpworld.getInstanceId());
			tmpworld.terian.setSpawn(null);
			tmpworld.savedSpawns = spawnGroup("room_1", tmpworld.getInstanceId());
			tmpworld.generator = addSpawn(ELECTRICITY_GENERATOR, GENERATOR_SPAWN_LOC, false, 0, false, tmpworld.getInstanceId());
			openDoor(DOOR_1, tmpworld.getInstanceId());
			startQuestTimer("officer_wait_1", 5000, tmpworld.terian, player);
		}
		teleportPlayer(player, START_LOC, tmpworld.getInstanceId());
	}
}