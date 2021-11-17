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
package quests.Q347_GoGetTheCalculator;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q347_GoGetTheCalculator extends Quest
{
	// NPCs
	private static final int BRUNON = 30526;
	private static final int SILVERA = 30527;
	private static final int SPIRON = 30532;
	private static final int BALANKI = 30533;
	// Items
	private static final int GEMSTONE_BEAST_CRYSTAL = 4286;
	private static final int CALCULATOR_QUEST = 4285;
	private static final int CALCULATOR_REAL = 4393;
	
	public Q347_GoGetTheCalculator()
	{
		super(347, "Go Get the Calculator");
		registerQuestItems(GEMSTONE_BEAST_CRYSTAL, CALCULATOR_QUEST);
		addStartNpc(BRUNON);
		addTalkId(BRUNON, SILVERA, SPIRON, BALANKI);
		addKillId(20540);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30526-05.htm":
			{
				st.startQuest();
				break;
			}
			case "30533-03.htm":
			{
				if (st.getQuestItemsCount(57) >= 100)
				{
					htmltext = "30533-02.htm";
					st.takeItems(57, 100);
					
					if (st.isCond(3))
					{
						st.setCond(4);
					}
					else
					{
						st.setCond(2);
					}
					
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				break;
			}
			case "30532-02.htm":
			{
				if (st.isCond(2))
				{
					st.setCond(4);
				}
				else
				{
					st.setCond(3);
				}
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30526-08.htm":
			{
				st.takeItems(CALCULATOR_QUEST, -1);
				st.giveItems(CALCULATOR_REAL, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
				break;
			}
			case "30526-09.htm":
			{
				st.takeItems(CALCULATOR_QUEST, -1);
				st.rewardItems(57, 1000);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() < 12) ? "30526-00.htm" : "30526-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case BRUNON:
					{
						htmltext = (!st.hasQuestItems(CALCULATOR_QUEST)) ? "30526-06.htm" : "30526-07.htm";
						break;
					}
					case SPIRON:
					{
						htmltext = (cond < 4) ? "30532-01.htm" : "30532-05.htm";
						break;
					}
					case BALANKI:
					{
						htmltext = (cond < 4) ? "30533-01.htm" : "30533-04.htm";
						break;
					}
					case SILVERA:
					{
						if (cond < 4)
						{
							htmltext = "30527-00.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30527-01.htm";
							st.setCond(5);
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 5)
						{
							if (st.getQuestItemsCount(GEMSTONE_BEAST_CRYSTAL) < 10)
							{
								htmltext = "30527-02.htm";
							}
							else
							{
								htmltext = "30527-03.htm";
								st.setCond(6);
								st.takeItems(GEMSTONE_BEAST_CRYSTAL, -1);
								st.giveItems(CALCULATOR_QUEST, 1);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						else if (cond == 6)
						{
							htmltext = "30527-04.htm";
						}
						break;
					}
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, 5);
		if (st == null)
		{
			return null;
		}
		
		st.dropItems(GEMSTONE_BEAST_CRYSTAL, 1, 10, 500000);
		
		return null;
	}
}