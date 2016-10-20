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
package quests.Q10332_ToughRoad;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

import quests.Q10331_StartOfFate.Q10331_StartOfFate;

/**
 * Tough Road (10332)
 * @author spider
 */
public class Q10332_ToughRoad extends Quest
{
	// Npcs
	private static final int BATHIS = 30332;
	private static final int KAKAI = 30565;
	// Rewards
	private static final int ADENA_REWARD = 700;
	private static final int EXP_REWARD = 90000;
	private static final int SP_REWARD = 21;
	
	public Q10332_ToughRoad()
	{
		super(10332, Q10332_ToughRoad.class.getSimpleName(), "Tough Road");
		addStartNpc(KAKAI);
		addTalkId(KAKAI, BATHIS);
		addCondLevel(20, 40, "no_level.html");
		addCondCompletedQuest(Q10331_StartOfFate.class.getSimpleName(), "no_level.htm");
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
			case "30565-02.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30565-03.html":
			{
				htmltext = event;
				break;
			}
			case "30332-02.html":
			{
				htmltext = event;
				break;
			}
			case "30332-03.html":
			{
				giveAdena(player, ADENA_REWARD, true);
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
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
				htmltext = npc.getId() == KAKAI ? "30565-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == KAKAI ? "30565-03.html" : "30332-01.html";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == KAKAI ? "30565-04.html" : "30332-04.html";
				break;
			}
		}
		return htmltext;
	}
}
