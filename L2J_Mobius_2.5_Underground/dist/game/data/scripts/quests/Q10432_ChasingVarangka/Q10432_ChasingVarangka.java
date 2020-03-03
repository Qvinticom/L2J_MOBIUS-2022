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
package quests.Q10432_ChasingVarangka;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10431_TheSealOfPunishmentDenOfEvil.Q10431_TheSealOfPunishmentDenOfEvil;

/**
 * Chasing Varangka (10432)
 * @URL https://l2wiki.com/Chasing_Varangka
 * @author Gigi
 */
public class Q10432_ChasingVarangka extends Quest
{
	// NPCs
	private static final int CHAIREN = 32655;
	private static final int JOKEL = 33868;
	private static final int DARK_SHAMAN_VARANGKA = 18808;
	// Misc
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	// Rewards
	private static final int EAS = 960;
	
	public Q10432_ChasingVarangka()
	{
		super(10432);
		addStartNpc(CHAIREN);
		addTalkId(CHAIREN, JOKEL);
		addKillId(DARK_SHAMAN_VARANGKA);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "32655-00.htm");
		addCondNotRace(Race.ERTHEIA, "noErtheia.html");
		addCondInCategory(CategoryType.FOURTH_CLASS_GROUP, "32655-00.htm");
		addCondCompletedQuest(Q10431_TheSealOfPunishmentDenOfEvil.class.getSimpleName(), "32655-00.htm");
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
			case "32655-02.htm":
			case "32655-03.htm":
			case "33868-02.html":
			{
				htmltext = event;
				break;
			}
			case "32655-04.htm":
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
					htmltext = "33868-03.html";
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
				if (npc.getId() == CHAIREN)
				{
					htmltext = "32655-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case CHAIREN:
					{
						if (qs.isCond(1))
						{
							htmltext = "32655-05.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "32655-06.html";
						}
						break;
					}
					case JOKEL:
					{
						if (qs.isCond(2))
						{
							htmltext = "33868-01.html";
						}
						break;
					}
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