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
package quests.Q161_FruitOfTheMotherTree;

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.Race;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q161_FruitOfTheMotherTree extends Quest
{
	private static final String qn = "Q161_FruitOfTheMotherTree";
	
	// NPCs
	private static final int ANDELLIA = 30362;
	private static final int THALIA = 30371;
	
	// Items
	private static final int ANDELLIA_LETTER = 1036;
	private static final int MOTHERTREE_FRUIT = 1037;
	
	public Q161_FruitOfTheMotherTree()
	{
		super(161, qn, "Fruit of the Mothertree");
		
		registerQuestItems(ANDELLIA_LETTER, MOTHERTREE_FRUIT);
		
		addStartNpc(ANDELLIA);
		addTalkId(ANDELLIA, THALIA);
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
		
		if (event.equals("30362-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ANDELLIA_LETTER, 1);
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
				if (player.getRace() != Race.elf)
				{
					htmltext = "30362-00.htm";
				}
				else if (player.getLevel() < 3)
				{
					htmltext = "30362-02.htm";
				}
				else
				{
					htmltext = "30362-03.htm";
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ANDELLIA:
						if (cond == 1)
						{
							htmltext = "30362-05.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30362-06.htm";
							st.takeItems(MOTHERTREE_FRUIT, 1);
							st.rewardItems(57, 1000);
							st.rewardExpAndSp(1000, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case THALIA:
						if (cond == 1)
						{
							htmltext = "30371-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ANDELLIA_LETTER, 1);
							st.giveItems(MOTHERTREE_FRUIT, 1);
						}
						else if (cond == 2)
						{
							htmltext = "30371-02.htm";
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