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
package quests.Q10534_HatchlingResearch;

import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Hatchling Research (10534)
 * @URL https://l2wiki.com/Hatchling_Research
 * @author Gigi
 * @date 2017-09-04 - [12:49:15]
 */
public class Q10534_HatchlingResearch extends Quest
{
	// NPC
	private static final int STENA = 34221;
	private static final int DRAGON_HATCHING = 23434;
	private static final int LEOPARD_DRAGON = 23435;
	// Item
	private static final int HATCHLING_FLASH = 46735;
	// Misc
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	
	public Q10534_HatchlingResearch()
	{
		super(10534);
		addStartNpc(STENA);
		addTalkId(STENA);
		addKillId(DRAGON_HATCHING, LEOPARD_DRAGON);
		registerQuestItems(HATCHLING_FLASH);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "34221-08.htm");
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
			case "34221-02.htm":
			case "34221-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34221-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34221-07.html":
			{
				if (qs.isCond(2))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 362_053_391, 19_840);
						qs.exitQuest(QuestType.ONE_TIME, true);
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "34221-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "34221-05.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "34221-06.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.isCond(1)))
		{
			if (giveItemRandomly(killer, npc, HATCHLING_FLASH, 1, 50, 0.7, true))
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
