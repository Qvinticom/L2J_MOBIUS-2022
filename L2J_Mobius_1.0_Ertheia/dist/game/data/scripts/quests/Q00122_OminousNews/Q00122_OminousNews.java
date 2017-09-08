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
package quests.Q00122_OminousNews;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * Ominous News (122)
 * @author malyelfik
 */
public final class Q00122_OminousNews extends Quest
{
	// NPCs
	private static final int MOIRA = 31979;
	private static final int KARUDA = 32017;
	// Misc
	private static final int MIN_LEVEL = 20;
	
	public Q00122_OminousNews()
	{
		super(122);
		addStartNpc(MOIRA);
		addTalkId(MOIRA, KARUDA);
		addCondMinLevel(MIN_LEVEL, "31979-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "31979-02.htm":
			{
				st.startQuest();
				break;
			}
			case "32017-02.html":
			{
				giveAdena(player, 8923, true);
				addExpAndSp(player, 45151, 10);
				st.exitQuest(false, true);
				break;
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		switch (npc.getId())
		{
			case MOIRA:
			{
				switch (st.getState())
				{
					case State.CREATED:
					{
						htmltext = "31979-01.htm";
						break;
					}
					case State.STARTED:
					{
						htmltext = "31979-03.html";
						break;
					}
					case State.COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg(player);
						break;
					}
				}
				break;
			}
			case KARUDA:
			{
				if (st.isStarted())
				{
					htmltext = "32017-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
}