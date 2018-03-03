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
package quests.Q654_JourneyToASettlement;

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q654_JourneyToASettlement extends Quest
{
	private static final String qn = "Q654_JourneyToASettlement";
	
	// Item
	private static final int ANTELOPE_SKIN = 8072;
	
	// Reward
	private static final int FORCE_FIELD_REMOVAL_SCROLL = 8073;
	
	public Q654_JourneyToASettlement()
	{
		super(654, qn, "Journey to a Settlement");
		
		registerQuestItems(ANTELOPE_SKIN);
		
		addStartNpc(31453); // Nameless Spirit
		addTalkId(31453);
		
		addKillId(21294, 21295); // Canyon Antelope, Canyon Antelope Slave
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("31453-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31453-03.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31453-06.htm"))
		{
			st.takeItems(ANTELOPE_SKIN, -1);
			st.giveItems(FORCE_FIELD_REMOVAL_SCROLL, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				QuestState prevSt = player.getQuestState("Q119_LastImperialPrince");
				htmltext = ((prevSt == null) || !prevSt.isCompleted() || (player.getLevel() < 74)) ? "31453-00.htm" : "31453-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "31453-02.htm";
				}
				else if (cond == 2)
				{
					htmltext = "31453-04.htm";
				}
				else if (cond == 3)
				{
					htmltext = "31453-05.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "2");
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(ANTELOPE_SKIN, 1, 1, 50000))
		{
			st.set("cond", "3");
		}
		
		return null;
	}
}