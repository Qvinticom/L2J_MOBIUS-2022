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
package instances.MuseumDungeon;

import instances.AbstractInstance;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import quests.Q10327_IntruderWhoWantsTheBookOfGiants.Q10327_IntruderWhoWantsTheBookOfGiants;

import com.l2jserver.gameserver.enums.ChatType;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author Neanrakyr
 */
public final class MuseumDungeon extends AbstractInstance
{
	// Npcs
	private static final int PANTHEON = 32972;
	private static final int TOYRON = 33004;
	private static final int DESK = 33126;
	private static final int THIEF = 23121;
	// Items
	private static final int THE_WAR_OF_GODS_AND_GIANTS = 17575;
	// Locations
	private static final Location START_LOC = new Location(-114711, 243911, -7968);
	private static final Location TOYRON_SPAWN = new Location(-114707, 245428, -7968);
	// Misc
	private static final int TEMPLATE_ID = 182;
	private static final NpcStringId[] TOYRON_SHOUT =
	{
		NpcStringId.YOUR_NORMAL_ATTACKS_AREN_T_WORKING,
		NpcStringId.LOOKS_LIKE_ONLY_SKILL_BASED_ATTACKS_DAMAGE_THEM
	};
	private static final NpcStringId[] THIEF_SHOUT =
	{
		NpcStringId.YOU_LL_NEVER_LEAVE_WITH_THAT_BOOK,
		NpcStringId.FINALLY_I_THOUGHT_I_WAS_GOING_TO_DIE_WAITING
	};
	
	protected class MDWorld extends InstanceWorld
	{
		protected L2QuestGuardInstance toyron = null;
		protected L2MonsterInstance thief = null;
		protected List<L2Npc> desks;
		protected Set<L2Npc> spawnedThiefs = Collections.newSetFromMap(new ConcurrentHashMap<L2Npc, Boolean>());
		protected int bookDesk = 0;
		protected int killedThiefs = 0;
	}
	
	public MuseumDungeon()
	{
		super(MuseumDungeon.class.getSimpleName());
		addStartNpc(PANTHEON);
		addTalkId(PANTHEON);
		addTalkId(TOYRON);
		addFirstTalkId(DESK);
		addAttackId(THIEF);
		addKillId(THIEF);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("enter_instance"))
		{
			enterInstance(player, new MDWorld(), "MuseumDungeon.xml", TEMPLATE_ID);
		}
		else
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
			
			if ((tmpworld != null) && (tmpworld instanceof MDWorld))
			{
				final MDWorld world = (MDWorld) tmpworld;
				switch (event)
				{
					case "toyron_follow":
					{
						world.toyron.getAI().startFollow(player);
						break;
					}
					case "toyron_shout":
					{
						if (!world.toyron.canTarget(player))
						{
							cancelQuestTimer("toyron_shout", world.toyron, player);
						}
						broadcastNpcSay(world.toyron, ChatType.NPC_GENERAL, TOYRON_SHOUT[getRandom(2)]);
						break;
					}
					case "spawn_thiefs_stage_1":
					{
						final List<L2Npc> thiefs = spawnGroup("thiefs", world.getInstanceId());
						world.spawnedThiefs.addAll(thiefs);
						for (L2Npc thief : world.spawnedThiefs)
						{
							thief.setIsRunning(true);
							addAttackPlayerDesire(thief, player);
							broadcastNpcSay(thief, ChatType.NPC_GENERAL, THIEF_SHOUT[getRandom(2)]);
						}
						break;
					}
					case "spawn_thiefs_stage_2":
					{
						final List<L2Npc> thiefs = spawnGroup("thiefs", world.getInstanceId());
						world.spawnedThiefs.addAll(thiefs);
						for (L2Npc thief : world.spawnedThiefs)
						{
							thief.setIsRunning(true);
						}
						break;
					}
					case "check_follow":
					{
						if (world.toyron.canTarget(player))
						{
							startQuestTimer("toyron_follow", 500, world.toyron, player);
						}
						break;
					}
					case "kill_thief":
					{
						npc.doDie(player);
						startQuestTimer("toyron_follow", 500, world.toyron, player);
						break;
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		final MDWorld world = (MDWorld) tmpworld;
		
		if ((qs != null) && qs.isCond(2))
		{
			if (world.killedThiefs >= 1)
			{
				qs.setCond(3, true);
				showOnScreenMsg(player, NpcStringId.TALK_TO_TOYRON_TO_RETURN_TO_THE_MUSEUM_LOBBY, ExShowScreenMessage.TOP_CENTER, 4500);
			}
			else
			{
				world.killedThiefs++;
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		final MDWorld world = (MDWorld) tmpworld;
		
		if (skill != null)
		{
			broadcastNpcSay(world.toyron, ChatType.NPC_GENERAL, NpcStringId.ENOUGH_OF_THIS_COME_AT_ME);
			world.toyron.reduceCurrentHp(1, npc, null);
			npc.reduceCurrentHp(1, world.toyron, null);
			startQuestTimer("kill_thief", 2500, npc, attacker);
		}
		else
		{
			showOnScreenMsg(attacker, NpcStringId.USE_YOUR_SKILL_ATTACKS_AGAINST_THEM, ExShowScreenMessage.TOP_CENTER, 4500);
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
		final MDWorld world = (MDWorld) tmpworld;
		String htmltext = null;
		
		if (qs == null)
		{
			htmltext = "33126.html";
		}
		else if (qs.isCond(1))
		{
			if (((npc.getObjectId() == world.bookDesk) && !qs.hasQuestItems(THE_WAR_OF_GODS_AND_GIANTS)))
			{
				qs.setCond(2);
				giveItems(player, THE_WAR_OF_GODS_AND_GIANTS, 1);
				showOnScreenMsg(player, NpcStringId.WATCH_OUT_YOU_ARE_BEING_ATTACKED, ExShowScreenMessage.TOP_CENTER, 4500);
				startQuestTimer("spawn_thiefs_stage_1", 500, world.thief, player);
				startQuestTimer("toyron_follow", 500, world.toyron, player);
				htmltext = "33126-01.html";
			}
			else
			{
				htmltext = "33126-02.html";
			}
		}
		else if (qs.isCond(2))
		{
			htmltext = "33126.html";
		}
		return htmltext;
	}
	
	protected void spawnToyron(L2PcInstance player, MDWorld world)
	{
		if (world.toyron != null)
		{
			world.toyron.deleteMe();
		}
		world.toyron = (L2QuestGuardInstance) addSpawn(TOYRON, TOYRON_SPAWN, false, 0, true, world.getInstanceId());
		world.toyron.setIsRunning(true);
		world.toyron.setCanReturnToSpawnPoint(false);
	}
	
	protected void checkStage(L2PcInstance player, MDWorld world)
	{
		final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
		
		if (qs != null)
		{
			if (qs.isCond(1))
			{
				showOnScreenMsg(player, NpcStringId.AMONG_THE_4_BOOKSHELVES_FIND_THE_ONE_CONTAINING_A_VOLUME_CALLED_THE_WAR_OF_GODS_AND_GIANTS, ExShowScreenMessage.TOP_CENTER, 4500);
			}
			else if (qs.isCond(2))
			{
				if (world.spawnedThiefs.isEmpty())
				{
					startQuestTimer("spawn_thiefs_stage_2", 500, world.thief, player);
					startQuestTimer("toyron_follow", 500, world.toyron, player);
				}
				else
				{
					startQuestTimer("check_follow", 1000, world.toyron, player);
				}
			}
		}
	}
	
	protected void spawnDesks(L2PcInstance player, MDWorld world)
	{
		final List<L2Npc> desks = spawnGroup("desks", world.getInstanceId());
		for (L2Npc desk : desks)
		{
			double point = Math.random();
			int counter = 0;
			if (((point <= 0.25) && (counter == 0)) || ((point > 0.25) && (point <= 0.5) && (counter == 1)) || ((point > 0.5) && (point <= 0.75) && (counter == 2)) || ((point > 0.75) && (counter == 3)))
			{
				world.bookDesk = desk.getObjectId();
			}
			++counter;
		}
		if ((world.bookDesk == 0) && (desks.size() > 0))
		{
			world.bookDesk = desks.get(0).getObjectId();
		}
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
			spawnToyron(player, (MDWorld) world);
			spawnDesks(player, (MDWorld) world);
		}
		teleportPlayer(player, START_LOC, world.getInstanceId());
		checkStage(player, (MDWorld) world);
	}
}