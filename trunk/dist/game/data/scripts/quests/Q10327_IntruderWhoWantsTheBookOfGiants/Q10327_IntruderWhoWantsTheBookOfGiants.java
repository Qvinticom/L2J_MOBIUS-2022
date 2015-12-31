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
package quests.Q10327_IntruderWhoWantsTheBookOfGiants;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q10326_RespectYourElders.Q10326_RespectYourElders;

/**
 * Intruder Who Wants the Book of Giants (10327)
 * @author Mobius
 */
public class Q10327_IntruderWhoWantsTheBookOfGiants extends Quest
{
	// Npc
	private static final int PANTHEON = 32972;
	// Items
	private static final int THE_WAR_OF_GODS_AND_GIANTS = 17575;
	private static final int APPRENTICE_EARRING = 112;
	// Other
	private static final int MAX_LEVEL = 20;
	
	public Q10327_IntruderWhoWantsTheBookOfGiants()
	{
		super(10327, Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName(), "Intruder Who Wants the Book of Giants");
		addStartNpc(PANTHEON);
		addTalkId(PANTHEON);
		registerQuestItems(THE_WAR_OF_GODS_AND_GIANTS);
		addCondMaxLevel(MAX_LEVEL, "no_level.html");
		addCondCompletedQuest(Q10326_RespectYourElders.class.getSimpleName(), "no_level.html");
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
			case "32972-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32972-03.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32972-06.html":
			{
				if (qs.isCond(3))
				{
					giveAdena(player, 159, true);
					giveItems(player, APPRENTICE_EARRING, 2);
					addExpAndSp(player, 7800, 5);
					showOnScreenMsg(player, NpcStringId.ACCESSORIES_HAVE_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 5000);
					qs.exitQuest(false, true);
				}
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
				htmltext = "32972-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(3))
				{
					htmltext = "32972-05.html";
				}
				else
				{
					htmltext = "32972-04.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = "32972-07.html";
				break;
			}
		}
		
		return htmltext;
	}
}