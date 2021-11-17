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

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q010_IntoTheWorld extends Quest
{
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
		super(10, "Into the World");
		registerQuestItems(VERY_EXPENSIVE_NECKLACE);
		addStartNpc(BALANKI);
		addTalkId(BALANKI, REED, GERALD);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30533-02.htm":
			{
				st.startQuest();
				break;
			}
			case "30520-02.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(VERY_EXPENSIVE_NECKLACE, 1);
				break;
			}
			case "30650-02.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(VERY_EXPENSIVE_NECKLACE, 1);
				break;
			}
			case "30520-04.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30533-05.htm":
			{
				st.giveItems(SOE_GIRAN, 1);
				st.rewardItems(MARK_OF_TRAVELER, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if ((player.getLevel() >= 3) && (player.getRace() == Race.DWARF))
				{
					htmltext = "30533-01.htm";
				}
				else
				{
					htmltext = "30533-01a.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case BALANKI:
					{
						if (cond < 4)
						{
							htmltext = "30533-03.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30533-04.htm";
						}
						break;
					}
					case REED:
					{
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
					}
					case GERALD:
					{
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
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
				break;
			}
		}
		
		return htmltext;
	}
}