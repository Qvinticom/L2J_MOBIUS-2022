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
package quests.Q10438_ChasingLoygen;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10437_TheSealOfPunishmentPlainsOfTheLizardmen.Q10437_TheSealOfPunishmentPlainsOfTheLizardmen;

/**
 * Chasing Loygen (10438)
 * @URL https://l2wiki.com/Chasing_Loygen
 * @author Gigi
 */
public class Q10438_ChasingLoygen extends Quest
{
	// NPCs
	private static final int LAKI = 32742;
	private static final int TANTA_LIZARDMAN_CHIEF_LOYGEN = 27497;
	// Misc
	private static final int MIN_LEVEL = 81;
	// Reward
	private static final int EAS = 960;
	
	public Q10438_ChasingLoygen()
	{
		super(10438);
		addStartNpc(LAKI);
		addTalkId(LAKI);
		addKillId(TANTA_LIZARDMAN_CHIEF_LOYGEN);
		addCondMinLevel(MIN_LEVEL, "32742-00.htm");
		addCondNotRace(Race.ERTHEIA, "noErtheia.html");
		addCondInCategory(CategoryType.BOW_MASTER, "32742-00.htm");
		addCondCompletedQuest(Q10437_TheSealOfPunishmentPlainsOfTheLizardmen.class.getSimpleName(), "32742-00.htm");
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
			case "32742-02.htm":
			case "32742-03.htm":
			case "32742-07.html":
			{
				htmltext = event;
				break;
			}
			case "32742-04.htm":
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
					giveItems(player, EAS, 5);
					giveStoryQuestReward(player, 30);
					addExpAndSp(player, 14120400, 3388);
					qs.exitQuest(false, true);
					htmltext = "32742-08.html";
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
				htmltext = "32742-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "32742-05.html";
					break;
				}
				else if (qs.isCond(2))
				{
					htmltext = "32742-06.html";
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