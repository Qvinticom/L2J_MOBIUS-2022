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
package quests.Q010_IntoTheWorld;

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.Race;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q010_IntoTheWorld extends Quest
{
	private static final String qn = "Q010_IntoTheWorld";
	
	// Items
	private static final int VERY_EXPENSIVE_NECKLACE = 7574;
	
	// Rewards
	private static final int SOE_GIRAN = 7559;
	private static final int MARK_OF_TRAVELER = 7570;
	
	// NPCs
	private static final int REED = 30520;
	private static final int BALANKI = 30533;
	private static final int GERALD = 30650;
	
	public Q010_IntoTheWorld()
	{
		super(10, qn, "Into the World");
		
		registerQuestItems(VERY_EXPENSIVE_NECKLACE);
		
		addStartNpc(BALANKI);
		addTalkId(BALANKI, REED, GERALD);
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
		
		if (event.equals("30533-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30520-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(VERY_EXPENSIVE_NECKLACE, 1);
		}
		else if (event.equals("30650-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(VERY_EXPENSIVE_NECKLACE, 1);
		}
		else if (event.equals("30520-04.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30533-05.htm"))
		{
			st.giveItems(SOE_GIRAN, 1);
			st.rewardItems(MARK_OF_TRAVELER, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				if ((player.getLevel() >= 3) && (player.getRace() == Race.dwarf))
				{
					htmltext = "30533-01.htm";
				}
				else
				{
					htmltext = "30533-01a.htm";
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case BALANKI:
						if (cond < 4)
						{
							htmltext = "30533-03.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30533-04.htm";
						}
						break;
					
					case REED:
						if (cond == 1)
						{
							htmltext = "30520-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30520-02a.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30520-03.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30520-04a.htm";
						}
						break;
					
					case GERALD:
						if (cond == 2)
						{
							htmltext = "30650-01.htm";
						}
						else if (cond > 2)
						{
							htmltext = "30650-04.htm";
						}
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}