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
package quests.Q10542_SearchingForNewPower;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q10541_TrainLikeTheRealThing.Q10541_TrainLikeTheRealThing;

/**
 * Searching for New Power (10542)
 * @URL https://l2wiki.com/Searching_for_New_Power
 * @author GIgi
 */
public final class Q10542_SearchingForNewPower extends Quest
{
	// NPCs
	private static final int SHANNON = 32974;
	// Items
	private static final int SOULSHOT = 1835;
	private static final int THE_WAR_OF_GODS_AND_GIANTS = 17575;
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10542_SearchingForNewPower()
	{
		super(10542);
		addStartNpc(SHANNON);
		addTalkId(SHANNON);
		registerQuestItems(THE_WAR_OF_GODS_AND_GIANTS);
		addCondNotRace(Race.ERTHEIA, "noRace.html");
		addCondMaxLevel(MAX_LEVEL, "noLevel.html");
		addCondCompletedQuest(Q10541_TrainLikeTheRealThing.class.getSimpleName(), "noLevel.html");
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
			case "32974-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32974-03.htm":
			{
				qs.startQuest();
				showOnScreenMsg(player, NpcStringId.AUTOMATE_SOULSHOT_AS_SHOWN_IN_THE_TUTORIAL, ExShowScreenMessage.TOP_CENTER, 10000);
				if (!hasQuestItems(player, SOULSHOT))
				{
					giveItems(player, SOULSHOT, 100);
				}
				htmltext = event;
				break;
			}
			case "32974-06.html":
			{
				addExpAndSp(player, 3200, 8);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
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
				if (npc.getId() == SHANNON)
				{
					htmltext = "32974-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if ((qs.getCond() > 0) && (qs.getCond() < 5))
				{
					htmltext = "32974-04.html";
					qs.setCond(2);
				}
				else if (qs.isCond(5))
				{
					htmltext = "32974-05.html";
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
}