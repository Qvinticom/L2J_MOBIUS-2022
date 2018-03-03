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
package quests.Q637_ThroughTheGateOnceMore;

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q637_ThroughTheGateOnceMore extends Quest
{
	private static final String qn = "Q637_ThroughTheGateOnceMore";
	
	// NPC
	private static final int FLAURON = 32010;
	
	// Items
	private static final int FADED_VISITOR_MARK = 8065;
	private static final int NECROMANCER_HEART = 8066;
	
	// Reward
	private static final int PAGAN_MARK = 8067;
	
	public Q637_ThroughTheGateOnceMore()
	{
		super(637, qn, "Through the Gate Once More");
		
		registerQuestItems(NECROMANCER_HEART);
		
		addStartNpc(FLAURON);
		addTalkId(FLAURON);
		
		addKillId(21565, 21566, 21567);
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
		
		if (event.equals("32010-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("32010-10.htm"))
		{
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if ((player.getLevel() < 73) || !st.hasQuestItems(FADED_VISITOR_MARK))
				{
					htmltext = "32010-01a.htm";
				}
				else if (st.hasQuestItems(PAGAN_MARK))
				{
					htmltext = "32010-00.htm";
				}
				else
				{
					htmltext = "32010-01.htm";
				}
				break;
			
			case State.STARTED:
				if (st.getInt("cond") == 2)
				{
					if (st.getQuestItemsCount(NECROMANCER_HEART) == 10)
					{
						htmltext = "32010-06.htm";
						st.takeItems(FADED_VISITOR_MARK, 1);
						st.takeItems(NECROMANCER_HEART, -1);
						st.giveItems(PAGAN_MARK, 1);
						st.giveItems(8273, 10);
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(true);
					}
					else
					{
						st.set("cond", "1");
					}
				}
				else
				{
					htmltext = "32010-05.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, npc, "1");
		if (partyMember == null)
		{
			return null;
		}
		
		QuestState st = partyMember.getQuestState(qn);
		
		if ((st != null) && st.dropItems(NECROMANCER_HEART, 1, 10, 400000))
		{
			st.set("cond", "2");
		}
		
		return null;
	}
}