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
package quests.Q10435_ChasingKeltron;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10434_TheSealOfPunishmentSelMahumTrainingGrounds.Q10434_TheSealOfPunishmentSelMahumTrainingGrounds;

/**
 * Chasing Keltron (10435)
 * @URL https://l2wiki.com/Chasing_Keltron
 * @author Gigi
 */
public class Q10435_ChasingKeltron extends Quest
{
	// NPCs
	private static final int RUA = 33841;
	private static final int SEL_MAHUM_CHIEF_KELTRON = 27498;
	// Reward
	private static final int EAS = 960;
	// Misc
	private static final int MIN_LEVEL = 81;
	
	public Q10435_ChasingKeltron()
	{
		super(10435);
		addStartNpc(RUA);
		addTalkId(RUA);
		addKillId(SEL_MAHUM_CHIEF_KELTRON);
		addCondMinLevel(MIN_LEVEL, "33841-00.htm");
		addCondNotRace(Race.ERTHEIA, "33841-00.htm");
		addCondInCategory(CategoryType.WEAPON_MASTER, "33841-00.htm");
		addCondCompletedQuest(Q10434_TheSealOfPunishmentSelMahumTrainingGrounds.class.getSimpleName(), "33841-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33841-02.htm":
			case "33841-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33841-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "reward_9546":
			case "reward_9547":
			case "reward_9548":
			case "reward_9549":
			case "reward_9550":
			case "reward_9551":
			{
				if (qs.isCond(2))
				{
					final int stoneId = Integer.parseInt(event.replaceAll("reward_", ""));
					giveItems(player, stoneId, 15);
					giveItems(player, EAS, 2);
					giveStoryQuestReward(player, 30);
					addExpAndSp(player, 14120400, 3388);
					qs.exitQuest(false, true);
					htmltext = "33841-07.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == RUA)
				{
					htmltext = "33841-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if ((qs.isCond(1)) && (npc.getId() == RUA))
				{
					htmltext = "33841-05.html";
					break;
				}
				else if (qs.isCond(2))
				{
					htmltext = "33841-06.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getNoQuestMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			qs.setCond(2, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}