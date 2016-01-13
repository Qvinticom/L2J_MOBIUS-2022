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
package quests.Q10335_RequestToFindSakum;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

/**
 * Request to find Sakum (10335)
 * @author spider
 */
public class Q10335_RequestToFindSakum extends Quest
{
	// NPCs
	private static final int BATHIS = 30332;
	private static final int KALLESIN = 33177;
	private static final int ZENATH = 33509;
	// Monsters
	private static final int SKELETON_TRACKER = 20035;
	private static final int SKELETON_BOWMAN = 20051;
	private static final int RUIN_ZOMBIE = 20026;
	private static final int RUIN_SPARTOI = 20054;
	private static final Map<Integer, Integer> MOBS_REQUIRED = new HashMap<>();
	{
		MOBS_REQUIRED.put(SKELETON_TRACKER, 10);
		MOBS_REQUIRED.put(SKELETON_BOWMAN, 10);
		MOBS_REQUIRED.put(RUIN_ZOMBIE, 15);
		MOBS_REQUIRED.put(RUIN_SPARTOI, 15);
	}
	// Rewards
	private static final int ADENA_REWARD = 90000;
	private static final int EXP_REWARD = 350000;
	private static final int SP_REWARD = 84;
	// Others
	private static final int MIN_LEVEL = 23;
	private static final int MAX_LEVEL = 40;
	
	public Q10335_RequestToFindSakum()
	{
		super(10335, Q10335_RequestToFindSakum.class.getSimpleName(), "Request to find Sakum");
		addStartNpc(BATHIS);
		addTalkId(BATHIS, KALLESIN, ZENATH);
		addKillId(SKELETON_TRACKER, SKELETON_BOWMAN, RUIN_ZOMBIE, RUIN_SPARTOI);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
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
			case "30332-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30332-03.htm": // start the quest
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33177-02.html": // next step, killing mobs
			{
				qs.setCond(2);
				qs.set(Integer.toString(SKELETON_TRACKER), 0);
				qs.set(Integer.toString(SKELETON_BOWMAN), 0);
				qs.set(Integer.toString(RUIN_ZOMBIE), 0);
				qs.set(Integer.toString(RUIN_SPARTOI), 0);
				htmltext = event;
				break;
			}
			case "33509-02.html":
			{
				htmltext = event;
				break;
			}
			case "33509-03.html":
			{
				if (qs.isCond(3))
				{ // exit quest.
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
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
				switch (npc.getId())
				{
					case BATHIS:
					{
						htmltext = "30332-01.htm";
						break;
					}
					case KALLESIN:
					case ZENATH:
					{
						htmltext = getNoQuestMsg(player);
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case BATHIS:
					{
						htmltext = "30332-04.html";
						break;
					}
					case KALLESIN:
					{
						if (qs.isCond(1))
						{
							htmltext = "33177-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "33177-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case ZENATH:
					{
						if (qs.isCond(3))
						{
							htmltext = "33509-01.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				switch (npc.getId())
				{
					case BATHIS:
					case KALLESIN:
					{
						htmltext = getAlreadyCompletedMsg(player);
						break;
					}
					case ZENATH:
					{
						htmltext = "33509-04.html";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isStarted() && qs.isCond(2) && (Util.checkIfInRange(1500, npc, qs.getPlayer(), false)))
		{
			int kills = qs.getInt(Integer.toString(npc.getId()));
			if (kills < MOBS_REQUIRED.get(npc.getId())) // check if killed required number of monsters
			{
				kills++;
				qs.set(Integer.toString(npc.getId()), kills);
			}
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(SKELETON_TRACKER, qs.getInt(Integer.toString(SKELETON_TRACKER)));
			log.addNpc(SKELETON_BOWMAN, qs.getInt(Integer.toString(SKELETON_BOWMAN)));
			log.addNpc(RUIN_SPARTOI, qs.getInt(Integer.toString(RUIN_SPARTOI)));
			log.addNpc(RUIN_ZOMBIE, qs.getInt(Integer.toString(RUIN_ZOMBIE)));
			killer.sendPacket(log);
			
			if ((qs.getInt(Integer.toString(SKELETON_TRACKER)) >= MOBS_REQUIRED.get(SKELETON_TRACKER)) && (qs.getInt(Integer.toString(SKELETON_BOWMAN)) >= MOBS_REQUIRED.get(SKELETON_BOWMAN)) && (qs.getInt(Integer.toString(RUIN_SPARTOI)) >= MOBS_REQUIRED.get(RUIN_SPARTOI)) && (qs.getInt(Integer.toString(RUIN_ZOMBIE)) >= MOBS_REQUIRED.get(RUIN_ZOMBIE)))
			{
				qs.setCond(3);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
