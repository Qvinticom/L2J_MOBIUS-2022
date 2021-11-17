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
package quests.Q00585_CantGoAgainstTheTime;

import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Can't Go Against the Time (585)
 * @URL https://l2wiki.com/Can%27t_Go_Against_the_Time
 * @author Dmitri
 */
public class Q00585_CantGoAgainstTheTime extends Quest
{
	// NPC
	private static final int FAIRY_CITIZEN = 32921;
	// Monsters
	private static final int[] MONSTERS =
	{
		22866, // Fairy Warrior (Imperfect)
		22874, // Fairy Rogue (Imperfect)
		22882, // Fairy Knight (Imperfect)
		22890, // Satyr Wizard (Imperfect)
		22898, // Satyr Summoner (Imperfect)
		22906, // Satyr Witch (Imperfect)
		22865, // Fairy Warrior (Mature)
		22873, // Fairy Rogue (Mature)
		22881, // Fairy Knight (Mature)
		22889, // Satyr Wizard (Mature)
		22897, // Satyr Summoner (Mature)
		22905, // Satyr Witch (Mature)
		22864, // Fairy Warrior (Wicked)
		22872, // Fairy Rogue (Wicked)
		22880, // Fairy Knight (Wicked)
		22888, // Satyr Wizard (Wicked)
		22896, // Satyr Summoner (Wicked)
		22904, // Satyr Witch (Wicked)
		19400 // Cocoon Destroyer
	};
	// Items
	private static final int TRACES_OF_MUTATION = 48381;
	// Misc
	private static final int MIN_LEVEL = 88;
	private static final int MAX_LEVEL = 98;
	
	public Q00585_CantGoAgainstTheTime()
	{
		super(585);
		addStartNpc(FAIRY_CITIZEN);
		addTalkId(FAIRY_CITIZEN);
		addKillId(MONSTERS);
		registerQuestItems(TRACES_OF_MUTATION);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "nolevel.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		switch (event)
		{
			case "32921-02.htm":
			case "32921-03.htm":
			{
				htmltext = event;
				break;
			}
			case "32921-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32921-07.html":
			{
				giveAdena(player, 536520, true);
				addExpAndSp(player, 429526470, 429510);
				qs.exitQuest(QuestType.DAILY, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "32921-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "32921-05.html";
				}
				else
				{
					htmltext = "32921-06.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				if (qs.isNowAvailable())
				{
					qs.setState(State.CREATED);
					htmltext = "32921-01.htm";
				}
				else
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, npc, TRACES_OF_MUTATION, 1, 100, 1, true))
		{
			qs.setCond(2, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}