/*
 * Copyright (C) 2004-2015 L2J Mobius DataPack
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
package quests.Q10329_BackupSeekers;

import quests.Q10328_RequestToSealTheEvilFragment.Q10328_RequestToSealTheEvilFragment;

import com.l2jserver.gameserver.enums.ChatType;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.ListenerRegisterType;
import com.l2jserver.gameserver.model.events.annotations.RegisterEvent;
import com.l2jserver.gameserver.model.events.annotations.RegisterType;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerTransform;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExRotation;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.NpcSay;
import com.l2jserver.gameserver.util.Util;

/**
 * @author Gladicek
 */
public class Q10329_BackupSeekers extends Quest
{
	// Npcs
	private static final int KAKAI = 30565;
	private static final int ATRAN = 33448;
	private static final int BART = 33204;
	
	// Items
	private static final ItemHolder RING_OF_KNOWLEDGE = new ItemHolder(875, 2);
	private static final ItemHolder NECKLACE_OF_KNOWLEDGE = new ItemHolder(906, 1);
	
	// Misc
	private static final int MAX_LEVEL = 20;
	private static final Location BART_SPAWN_1 = new Location(-117955, 255832, -1320);
	private static final Location BART_SPAWN_2 = new Location(-114121, 252445, -1560);
	private final static Location[] BART_LOC_1 =
	{
		new Location(-117063, 255528, -1296),
		new Location(-115766, 254791, -1504),
		new Location(-114753, 254755, -1528),
		new Location(-114606, 253534, -1528),
		new Location(-114375, 252807, -1536),
	};
	private final static Location[] BART_LOC_2 =
	{
		new Location(-114410, 252220, -1591),
		new Location(-114416, 250812, -1760),
		new Location(-113217, 250445, -1912),
		new Location(-112721, 250479, -1984),
		new Location(-112093, 249769, -2248),
		new Location(-110988, 249191, -2512),
		new Location(-110110, 248374, -2688),
		new Location(-109831, 247993, -2760),
		new Location(-109702, 247037, -2960),
		new Location(-109180, 247015, -3088),
		new Location(-107839, 248766, -3216),
	};
	
	public Q10329_BackupSeekers()
	{
		super(10329, Q10329_BackupSeekers.class.getSimpleName(), "Backup Seekers");
		addStartNpc(KAKAI);
		addTalkId(KAKAI, ATRAN);
		addSpawnId(BART);
		addMoveFinishedId(BART);
		addCondMaxLevel(MAX_LEVEL, "30565-05.htm");
		addCondCompletedQuest(Q10328_RequestToSealTheEvilFragment.class.getSimpleName(), "30565-05.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "30565-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30565-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				qs.setMemoState(1);
				final L2Npc bart = addSpawn(BART, BART_SPAWN_1, false, 300000);
				bart.broadcastPacket(new NpcSay(bart.getObjectId(), ChatType.NPC_GENERAL, bart.getTemplate().getDisplayId(), NpcStringId.I_WILL_GUIDE_YOU_FOLLOW_ME));
				startQuestTimer("MOVE_DELAY", 500, bart, player);
				break;
			}
			case "33448-02.htm":
			{
				if (qs.isStarted())
				{
					showOnScreenMsg(player, NpcStringId.ACCESSORIES_HAVE_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
					giveAdena(player, 25000, true);
					giveItems(player, RING_OF_KNOWLEDGE);
					giveItems(player, NECKLACE_OF_KNOWLEDGE);
					addExpAndSp(player, 16900, 5);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
				break;
			}
			case "CHECK_PLAYER":
			{
				final L2PcInstance owner = npc.getVariables().getObject("OWNER", L2PcInstance.class);
				if (owner != null)
				{
					if (npc.calculateDistance(owner, false, false) < 150)
					{
						npc.getVariables().set("FAIL_COUNT", 0);
						final int loc_index = npc.getVariables().getInt("MOVE_INDEX", -1) + 1;
						if (loc_index > 0)
						{
							if (qs.isMemoState(1))
							{
								if (loc_index == 5)
								{
									npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.TALK_TO_THAT_APPRENTICE_AND_GET_ON_KUKURI));
									startQuestTimer("DELETE_NPC", 2000, npc, owner);
									break;
								}
								npc.getVariables().set("MOVE_INDEX", loc_index);
								addMoveToDesire(npc, BART_LOC_1[loc_index], 0);
							}
							else if (qs.isMemoState(2))
							{
								if (loc_index == 11)
								{
									npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.THIS_IS_IT_FOR_ME));
									startQuestTimer("DELETE_NPC", 2000, npc, owner);
									break;
								}
								npc.getVariables().set("MOVE_INDEX", loc_index);
								addMoveToDesire(npc, BART_LOC_2[loc_index], 0);
							}
						}
					}
					else
					{
						final int failCount = npc.getVariables().getInt("FAIL_COUNT", 0);
						npc.getVariables().set("FAIL_COUNT", failCount + 1);
						
						if (failCount >= 30)
						{
							npc.deleteMe();
							break;
						}
						
						startQuestTimer("CHECK_PLAYER", 1200, npc, owner);
						
						if (getRandom(100) < 10)
						{
							npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.HEY_KID_HURRY_UP_AND_FOLLOW_ME));
						}
					}
				}
				else
				{
					npc.deleteMe();
				}
				break;
			}
			case "MOVE_DELAY":
			{
				if (qs.isMemoState(1))
				{
					npc.getVariables().set("OWNER", player);
					npc.setIsRunning(true);
					npc.broadcastInfo();
					addMoveToDesire(npc, BART_LOC_1[0], 0);
					npc.getVariables().set("MOVE_INDEX", 0);
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.YOU_MUST_BE_THE_ONE_KAKAI_TALKED_ABOUT));
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.YOU_SHOULD_RIDE_KUKURI_TO_GO_TO_YE_SAGIRA));
					break;
				}
				else if (qs.isMemoState(2))
				{
					npc.getVariables().set("OWNER", player);
					npc.setIsRunning(true);
					npc.broadcastInfo();
					addMoveToDesire(npc, BART_LOC_2[0], 0);
					npc.getVariables().set("MOVE_INDEX", 0);
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.OPEN_YOUR_MAP_WHEN_YOU_ARRIVE_AT_YE_SAGIRA));
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.IT_S_HARD_TO_TELL_WHERE_YOU_ARE_AT_WITHOUT_A_MAP_IN_YE_SAGIRA));
					break;
				}
				break;
			}
			case "DELETE_NPC":
			{
				npc.deleteMe();
				break;
			}
			case "RESPAWN_BART":
			{
				qs.setMemoState(2);
				final L2Npc bart = addSpawn(BART, BART_SPAWN_2, false, 300000);
				bart.broadcastPacket(new NpcSay(bart.getObjectId(), ChatType.NPC_GENERAL, bart.getTemplate().getDisplayId(), NpcStringId.I_WILL_GUIDE_YOU_FOLLOW_ME));
				startQuestTimer("MOVE_DELAY", 500, bart, player);
				break;
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_TRANSFORM)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerTransform(OnPlayerTransform event)
	{
		if (event.getTransformId() == 137)
		{
			startQuestTimer("RESPAWN_BART", 500, null, event.getActiveChar());
		}
	}
	
	@Override
	public void onMoveFinished(L2Npc npc)
	{
		final L2PcInstance owner = npc.getVariables().getObject("OWNER", L2PcInstance.class);
		
		if (owner != null)
		{
			npc.setHeading(Util.calculateHeadingFrom(npc, owner));
			npc.broadcastPacket(new ExRotation(npc.getObjectId(), npc.getHeading()));
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.HEY_KID_HURRY_UP_AND_FOLLOW_ME));
			startQuestTimer("CHECK_PLAYER", 1200, npc, owner);
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == KAKAI)
				{
					htmltext = "30565-01.htm";
					break;
				}
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == KAKAI ? "30565-04.htm" : "33448-01.htm";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == KAKAI ? "30565-06.htm" : "33448-03.htm";
				break;
			}
		}
		return htmltext;
	}
}