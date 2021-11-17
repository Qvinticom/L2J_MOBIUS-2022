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
package quests.Q10328_RequestToSealTheEvilFragment;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10327_IntruderWhoWantsTheBookOfGiants.Q10327_IntruderWhoWantsTheBookOfGiants;

/**
 * Request to Seal the Evil Fragment (10328)
 * @author Gladicek
 */
public class Q10328_RequestToSealTheEvilFragment extends Quest
{
	// NPCs
	private static final int PANTHEON = 32972;
	private static final int KAKAI = 30565;
	// Items
	private static final int EVIL_FRAGMENT = 17577;
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10328_RequestToSealTheEvilFragment()
	{
		super(10328);
		addStartNpc(PANTHEON);
		addTalkId(PANTHEON, KAKAI);
		addCondMaxLevel(MAX_LEVEL, "32972-06.html");
		registerQuestItems(EVIL_FRAGMENT);
		addCondCompletedQuest(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName(), "32972-06.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32972-04.html":
			{
				qs.startQuest();
				giveItems(player, EVIL_FRAGMENT, 1);
				htmltext = event;
				break;
			}
			case "32972-02.htm":
			case "32972-03.htm":
			case "30565-02.html":
			{
				htmltext = event;
				break;
			}
			case "30565-03.html":
			{
				if (qs.isStarted())
				{
					giveAdena(player, 20000, true);
					addExpAndSp(player, 13000, 5);
					qs.exitQuest(false, true);
					htmltext = event;
				}
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
				if (npc.getId() == PANTHEON)
				{
					htmltext = "32972-01.htm";
					break;
				}
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == PANTHEON ? "32972-05.html" : "30565-01.html";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == PANTHEON ? "32972-07.html" : "30565-04.html";
				break;
			}
		}
		return htmltext;
	}
}