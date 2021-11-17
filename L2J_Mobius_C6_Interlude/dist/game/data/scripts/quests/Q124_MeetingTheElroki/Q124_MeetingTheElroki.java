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
package quests.Q124_MeetingTheElroki;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q124_MeetingTheElroki extends Quest
{
	// NPCs
	private static final int MARQUEZ = 32113;
	private static final int MUSHIKA = 32114;
	private static final int ASAMAH = 32115;
	private static final int KARAKAWEI = 32117;
	private static final int MANTARASA = 32118;
	
	public Q124_MeetingTheElroki()
	{
		super(124, "Meeting the Elroki");
		addStartNpc(MARQUEZ);
		addTalkId(MARQUEZ, MUSHIKA, ASAMAH, KARAKAWEI, MANTARASA);
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
			case "32113-03.htm":
			{
				st.startQuest();
				break;
			}
			case "32113-04.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32114-02.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32115-04.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32117-02.htm":
			{
				if (st.isCond(4))
				{
					st.set("progress", "1");
				}
				break;
			}
			case "32117-03.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32118-02.htm":
			{
				st.setCond(6);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(8778, 1); // Egg
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
				htmltext = (player.getLevel() < 75) ? "32113-01a.htm" : "32113-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case MARQUEZ:
					{
						if (cond == 1)
						{
							htmltext = "32113-03.htm";
						}
						else if (cond > 1)
						{
							htmltext = "32113-04a.htm";
						}
						break;
					}
					case MUSHIKA:
					{
						if (cond == 2)
						{
							htmltext = "32114-01.htm";
						}
						else if (cond > 2)
						{
							htmltext = "32114-03.htm";
						}
						break;
					}
					case ASAMAH:
					{
						if (cond == 3)
						{
							htmltext = "32115-01.htm";
						}
						else if (cond == 6)
						{
							htmltext = "32115-05.htm";
							st.takeItems(8778, -1);
							st.rewardItems(57, 71318);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					}
					case KARAKAWEI:
					{
						if (cond == 4)
						{
							htmltext = "32117-01.htm";
							if (st.getInt("progress") == 1)
							{
								htmltext = "32117-02.htm";
							}
						}
						else if (cond > 4)
						{
							htmltext = "32117-04.htm";
						}
						break;
					}
					case MANTARASA:
					{
						if (cond == 5)
						{
							htmltext = "32118-01.htm";
						}
						else if (cond > 5)
						{
							htmltext = "32118-03.htm";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getNpcId() == ASAMAH)
				{
					htmltext = "32115-06.htm";
				}
				else
				{
					htmltext = getAlreadyCompletedMsg();
				}
				break;
			}
		}
		
		return htmltext;
	}
}