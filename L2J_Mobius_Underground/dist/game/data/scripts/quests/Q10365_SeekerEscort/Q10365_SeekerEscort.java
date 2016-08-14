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
package quests.Q10365_SeekerEscort;

import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExRotation;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.util.Util;

import quests.Q10364_ObligationsOfTheSeeker.Q10364_ObligationsOfTheSeeker;

/**
 * Seeker Escort (10365)
 * @author Gladicek
 */
public final class Q10365_SeekerEscort extends Quest
{
	// NPCs
	private static final int DEP = 33453;
	private static final int BLOODHOUND = 32988;
	private static final int SEBION = 32978;
	// Misc
	private static final int MIN_LEVEL = 16;
	private static final int MAX_LEVEL = 25;
	// Locations
	private static final Location BLOODHOUND_SPAWN_1 = new Location(-110624, 238369, -2920);
	private static final Location BLOODHOUND_SPAWN_2 = new Location(-112660, 233942, -3072);
	private final static Location[] BLOODHOUND_LOC_1 =
	{
		new Location(-110574, 238972, -2920),
		new Location(-110723, 239275, -2920),
		new Location(-110981, 239500, -2920),
		new Location(-111077, 239726, -2920),
		new Location(-111000, 239978, -2920),
		new Location(-111766, 240211, -2920),
		new Location(-111774, 240549, -2920),
		new Location(-111018, 240584, -2920),
		new Location(-111281, 240378, -2920),
		new Location(-111630, 239878, -2920),
		new Location(-111921, 239728, -2920),
		new Location(-112279, 239804, -2920),
		new Location(-112623, 239904, -2920),
		new Location(-112711, 240227, -2920),
		new Location(-112470, 240536, -2920),
		new Location(-112119, 240511, -2920),
		new Location(-112042, 240290, -2920),
		new Location(-112199, 240141, -2920),
		new Location(-112291, 240201, -2920),
	};
	private final static Location[] BLOODHOUND_LOC_2 =
	{
		new Location(-112403, 233676, -3096),
		new Location(-112174, 233487, -3120),
		new Location(-112135, 233107, -3120),
		new Location(-112424, 233910, -3096),
		new Location(-112739, 232540, -3072),
		new Location(-112558, 232070, -3080),
		new Location(-112236, 232098, -3120),
		new Location(-112054, 232356, -3136),
		new Location(-111729, 232600, -3158),
		new Location(-111194, 232727, -3224),
		new Location(-110776, 232459, -3256),
		new Location(-111194, 231841, -3224),
		new Location(-111487, 232013, -3200),
		new Location(-111681, 231967, -3168),
		new Location(-111707, 231828, -3168),
	};
	
	public Q10365_SeekerEscort()
	{
		super(10365);
		addStartNpc(DEP);
		addTalkId(DEP, SEBION);
		addSpawnId(BLOODHOUND);
		addMoveFinishedId(BLOODHOUND);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33453-06.html");
		addCondCompletedQuest(Q10364_ObligationsOfTheSeeker.class.getSimpleName(), "33453-06.html");
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
				qs.setMemoState(1);
				final L2Npc bloodhound = addSpawn(BLOODHOUND, BLOODHOUND_SPAWN_1, false, 300000);
				bloodhound.setSummoner(player);
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
							if (qs.isMemoState(1))
							{
								if (loc_index == 19)
								{
									showOnScreenMsg(player, NpcStringId.YOU_MUST_MOVE_TO_EXPLORATION_AREA_5_IN_ORDER_TO_CONTINUE, ExShowScreenMessage.TOP_CENTER, 4500);
									startQuestTimer("DELETE_NPC", 8000, npc, owner);
									break;
								}
								npc.getVariables().set("MOVE_INDEX", loc_index);
								addMoveToDesire(npc, BLOODHOUND_LOC_1[loc_index], 0);
							}
							else if (qs.isMemoState(2))
							{
								if (loc_index == 15)
								{
									qs.setCond(2);
									startQuestTimer("DELETE_NPC", 2000, npc, owner);
									break;
								}
								npc.getVariables().set("MOVE_INDEX", loc_index);
								addMoveToDesire(npc, BLOODHOUND_LOC_2[loc_index], 0);
							}
						}
					}
					else
					{
						final int failCount = npc.getVariables().getInt("FAIL_COUNT", 0) + 1;
						npc.getVariables().set("FAIL_COUNT", failCount);
						
						if (failCount >= 50)
						{
							// Use maybe something else than memostate to check if npc is spawned?!
							qs.setMemoState(0);
							showOnScreenMsg(player, NpcStringId.KING_HAS_RETURNED_TO_DEF_RETURN_TO_DEF_AND_START_AGAIN, ExShowScreenMessage.TOP_CENTER, 4500);
							npc.deleteMe();
							break;
						}
						startQuestTimer("CHECK_PLAYER", 1000, npc, owner);
						
						if (getRandom(100) < 10)
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.RUFF_RUFF_RRRRRR);
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
					npc.setIsRunning(true);
					npc.broadcastInfo();
					addMoveToDesire(npc, BLOODHOUND_LOC_1[0], 0);
					npc.getVariables().set("MOVE_INDEX", 0);
					break;
				}
				else if (qs.isMemoState(2))
				{
					npc.setIsRunning(true);
					npc.broadcastInfo();
					addMoveToDesire(npc, BLOODHOUND_LOC_2[0], 0);
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
			case "TELEPORT_TO_NEXT_STAGE":
			{
				qs.setMemoState(2);
				player.teleToLocation(BLOODHOUND_SPAWN_2);
				final L2Npc bloodhound = addSpawn(BLOODHOUND, BLOODHOUND_SPAWN_2, false, 300000);
				bloodhound.setTitle(player.getName());
				bloodhound.setSummoner(player);
				bloodhound.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.RUFF_RUFF_RRRRRR);
				startQuestTimer("MOVE_DELAY", 500, bloodhound, player);
				break;
			}
			case "SPAWN_KING":
			{
				qs.setMemoState(1);
				final L2Npc bloodhound = addSpawn(BLOODHOUND, BLOODHOUND_SPAWN_1, false, 300000);
				bloodhound.setTitle(player.getName());
				bloodhound.setSummoner(player);
				startQuestTimer("MOVE_DELAY", 500, bloodhound, player);
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
			showOnScreenMsg(owner, NpcStringId.CATCH_UP_TO_KING_HE_S_WAITING, ExShowScreenMessage.TOP_CENTER, 4500);
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
						// Use maybe something else than memostate to check if npc is spawned?!
						htmltext = qs.isMemoState(0) ? "33453-04.html" : "33453-05.html";
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
				htmltext = npc.getId() == DEP ? "33453-04.html" : "32978-03.html";
				break;
			}
		}
		return htmltext;
	}
}