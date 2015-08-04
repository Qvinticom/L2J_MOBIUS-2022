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
package quests.Q10365_SeekerEscort;

import com.l2jserver.gameserver.enums.ChatType;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExRotation;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.NpcSay;
import com.l2jserver.gameserver.util.Util;

/**
 * Seeker Escort (10365)
 * @author Mobius
 */
public final class Q10365_SeekerEscort extends Quest
{
	// NPCs
	private static final int SEBION = 32978;
	private static final int BLOODHOUND = 32988;
	private static final int DEP = 33453;
	// Locations
	private static final Location BLOODHOUND_LOC1_SPAWN = new Location(-110579, 238972, -2920);
	private static final Location BLOODHOUND_LOC2_SPAWN = new Location(-112665, 233944, -3072);
	private final static Location[] BLOODHOUND_PATH1_COORDS =
	{
		new Location(-110579, 238972, -2920),
		new Location(-110706, 239273, -2920),
		new Location(-110962, 239513, -2920),
		new Location(-110988, 239944, -2920),
		new Location(-110759, 240185, -2920),
		new Location(-110794, 240551, -2920),
		new Location(-111028, 240608, -2920),
		new Location(-111295, 240387, -2920),
		new Location(-111639, 239897, -2920),
		new Location(-111948, 239731, -2920),
		new Location(-112281, 239791, -2920),
		new Location(-112622, 239901, -2920),
		new Location(-112705, 240230, -2920),
		new Location(-112473, 240518, -2920),
		new Location(-112138, 240510, -2920),
		new Location(-112022, 240281, -2920),
		new Location(-112212, 240154, -2920),
	};
	private final static Location[] BLOODHOUND_PATH2_COORDS =
	{
		new Location(-112665, 233944, -3072),
		new Location(-112431, 233681, -3096),
		new Location(-112185, 233480, -3120),
		new Location(-112117, 233092, -3136),
		new Location(-112415, 232911, -3096),
		new Location(-112705, 232543, -3072),
		new Location(-112505, 232054, -3096),
		new Location(-112284, 232075, -3104),
		new Location(-112078, 232350, -3136),
		new Location(-111685, 232600, -3168),
		new Location(-111215, 232725, -3224),
		new Location(-110822, 232470, -3256),
		new Location(-110769, 232134, -3256),
		new Location(-111156, 231852, -3224),
		new Location(-111475, 231982, -3200),
		new Location(-111672, 231945, -3168),
	};
	// Others
	private static final int MIN_LEVEL = 16;
	private static final int MAX_LEVEL = 25;
	
	public Q10365_SeekerEscort()
	{
		super(10365, Q10365_SeekerEscort.class.getSimpleName(), "Seeker Escort");
		addStartNpc(DEP);
		addTalkId(DEP, SEBION);
		addSpawnId(BLOODHOUND);
		addMoveFinishedId(BLOODHOUND);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
		// addCondCompletedQuest(Q10364_ObligationsOfTheSeeker.class.getSimpleName(), "no_level.html");
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
			case "33453-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33453-03.html":
			{
				qs.startQuest();
				qs.setMemoState(2);
				final L2Npc bloodhound = addSpawn(BLOODHOUND, BLOODHOUND_LOC1_SPAWN, false, 300000);
				bloodhound.setTitle(player.getName());
				startQuestTimer("MOVE_DELAY", 500, bloodhound, player);
				htmltext = event;
				break;
			}
			case "32978-02.html":
			{
				if (qs.isCond(2))
				{
					giveAdena(player, 65000, true);
					addExpAndSp(player, 120000, 28);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "SPAWN_BLOODHOUND":
			{
				qs.setMemoState(2);
				final L2Npc bloodhound = addSpawn(BLOODHOUND, BLOODHOUND_LOC1_SPAWN, false, 300000);
				bloodhound.setTitle(player.getName());
				startQuestTimer("MOVE_DELAY", 500, bloodhound, player);
				break;
			}
			case "CHECK_PLAYER":
			{
				final L2PcInstance owner = npc.getSummoner().getActingPlayer();
				if (owner != null)
				{
					if (npc.calculateDistance(owner, false, false) < 180)
					{
						npc.getVariables().set("FAIL_COUNT", 0);
						final int loc_index = npc.getVariables().getInt("MOVE_INDEX", -1) + 1;
						if (loc_index > 0)
						{
							if (qs.isMemoState(2))
							{
								if (loc_index == 16)
								{
									showOnScreenMsg(player, NpcStringId.YOU_MUST_MOVE_TO_EXPLORATION_AREA_5_IN_ORDER_TO_CONTINUE, ExShowScreenMessage.TOP_CENTER, 5000);
									startQuestTimer("DELETE_NPC", 3000, npc, owner);
									startQuestTimer("NEXT_AREA", 7000, npc, owner);
									break;
								}
								npc.getVariables().set("MOVE_INDEX", loc_index);
								addMoveToDesire(npc, BLOODHOUND_PATH1_COORDS[loc_index], 0);
							}
							else if (qs.isMemoState(3))
							{
								if (loc_index == 16)
								{
									qs.setCond(2);
									startQuestTimer("DELETE_NPC", 3000, npc, owner);
									break;
								}
								npc.getVariables().set("MOVE_INDEX", loc_index);
								addMoveToDesire(npc, BLOODHOUND_PATH2_COORDS[loc_index], 0);
							}
						}
					}
					else
					{
						final int failCount = npc.getVariables().getInt("FAIL_COUNT", 0);
						npc.getVariables().set("FAIL_COUNT", failCount + 1);
						
						if (failCount >= 30)
						{
							qs.setMemoState(1);
							showOnScreenMsg(player, NpcStringId.KING_HAS_RETURNED_TO_DEF_RETURN_TO_DEF_AND_START_AGAIN, ExShowScreenMessage.TOP_CENTER, 5000);
							npc.deleteMe();
							break;
						}
						startQuestTimer("CHECK_PLAYER", 1000, npc, owner);
						
						if (getRandom(100) < 10)
						{
							npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.RUFF_RUFF_RRRRRR));
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
				if (qs.isMemoState(2))
				{
					npc.setSummoner(player);
					npc.setIsRunning(true);
					npc.broadcastInfo();
					addMoveToDesire(npc, BLOODHOUND_PATH1_COORDS[0], 0);
					npc.getVariables().set("MOVE_INDEX", 0);
					break;
				}
				else if (qs.isMemoState(3))
				{
					npc.setSummoner(player);
					npc.setIsRunning(true);
					npc.broadcastInfo();
					addMoveToDesire(npc, BLOODHOUND_PATH2_COORDS[0], 0);
					npc.getVariables().set("MOVE_INDEX", 0);
					break;
				}
				break;
			}
			case "DELETE_NPC":
			{
				npc.deleteMe();
				break;
			}
			case "NEXT_AREA":
			{
				qs.setMemoState(3);
				final L2Npc bloodhound = addSpawn(BLOODHOUND, BLOODHOUND_LOC2_SPAWN, false, 300000);
				bloodhound.setTitle(player.getName());
				player.teleToLocation(BLOODHOUND_LOC2_SPAWN);
				startQuestTimer("MOVE_DELAY", 1000, bloodhound, player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public void onMoveFinished(L2Npc npc)
	{
		final L2PcInstance owner = npc.getSummoner().getActingPlayer();
		
		if (owner != null)
		{
			showOnScreenMsg(owner, NpcStringId.CATCH_UP_TO_KING_HE_S_WAITING, ExShowScreenMessage.TOP_CENTER, 5000);
			npc.setHeading(Util.calculateHeadingFrom(npc, owner));
			npc.broadcastPacket(new ExRotation(npc.getObjectId(), npc.getHeading()));
			startQuestTimer("CHECK_PLAYER", 1000, npc, owner);
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
				if (npc.getId() == DEP)
				{
					htmltext = "33453-01.htm";
					break;
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == DEP)
				{
					if (qs.isCond(1))
					{
						if (qs.isMemoState(1))
						{
							htmltext = "33453-04.html";
						}
						else
						{
							htmltext = "33453-05.html";
						}
						break;
					}
					break;
				}
				else if (npc.getId() == SEBION)
				{
					if (qs.isCond(2))
					{
						htmltext = "32978-01.html";
					}
					break;
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == SEBION)
				{
					htmltext = "32978-03.html";
				}
				else
				{
					htmltext = "33453-06.html";
				}
				break;
			}
		}
		return htmltext;
	}
}