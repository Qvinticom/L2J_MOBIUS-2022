/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q10334_ReportingTheStatusOfWindmillHill;

import quests.Q10333_DisappearedSakum.Q10333_DisappearedSakum;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;

/**
 * Reporting the status of Windmill Hill (10334)
 * @author spider
 */
public class Q10334_ReportingTheStatusOfWindmillHill extends Quest
{
	// Npcs
	private static final int SCHUNAIN = 33508;
	private static final int BATHIS = 30332;
	// Rewards
	private static final int ADENA_REWARD = 850;
	private static final int EXP_REWARD = 200000;
	private static final int SP_REWARD = 48;
	// Other
	private static final int MIN_LEVEL = 22;
	private static final int MAX_LEVEL = 40;
	
	public Q10334_ReportingTheStatusOfWindmillHill()
	{
		super(10334, Q10334_ReportingTheStatusOfWindmillHill.class.getSimpleName(), "Reporting the status of Windmill Hill");
		addStartNpc(SCHUNAIN);
		addTalkId(SCHUNAIN, BATHIS);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
		addCondCompletedQuest(Q10333_DisappearedSakum.class.getSimpleName(), "no_prequest.htm");
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
			case "33508-03.htm":
			{
				qs.startQuest();
				qs.setCond(2); // arrow hack, required for that quest
				qs.setCond(1);
				htmltext = event;
				break;
			}
			case "33508-02.htm":
			case "33508-04.html":
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
				htmltext = npc.getId() == SCHUNAIN ? "33508-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == SCHUNAIN ? "33508-03.htm" : "30332-01.html";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == SCHUNAIN ? "33508-04.html" : "30332-04.html";
				break;
			}
		}
		return htmltext;
	}
}
