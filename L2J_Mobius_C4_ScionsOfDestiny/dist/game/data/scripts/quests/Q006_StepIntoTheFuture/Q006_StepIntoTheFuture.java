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
package quests.Q006_StepIntoTheFuture;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q006_StepIntoTheFuture extends Quest
{
	// NPCs
	private static final int ROXXY = 30006;
	private static final int BAULRO = 30033;
	private static final int SIR_COLLIN = 30311;
	// Items
	private static final int BAULRO_LETTER = 7571;
	// Rewards
	private static final int MARK_TRAVELER = 7570;
	private static final int SOE_GIRAN = 7559;
	
	public Q006_StepIntoTheFuture()
	{
		super(6, "Step into the Future");
		registerQuestItems(BAULRO_LETTER);
		addStartNpc(ROXXY);
		addTalkId(ROXXY, BAULRO, SIR_COLLIN);
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
			case "30006-03.htm":
			{
				st.startQuest();
				break;
			}
			case "30033-02.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(BAULRO_LETTER, 1);
				break;
			}
			case "30311-02.htm":
			{
				if (st.hasQuestItems(BAULRO_LETTER))
				{
					st.setCond(3);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(BAULRO_LETTER, 1);
				}
				else
				{
					htmltext = "30311-03.htm";
				}
				break;
			}
			case "30006-06.htm":
			{
				st.giveItems(MARK_TRAVELER, 1);
				st.rewardItems(SOE_GIRAN, 1);
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
				if ((player.getRace() != Race.HUMAN) || (player.getLevel() < 3))
				{
					htmltext = "30006-01.htm";
				}
				else
				{
					htmltext = "30006-02.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case ROXXY:
					{
						if ((cond == 1) || (cond == 2))
						{
							htmltext = "30006-04.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30006-05.htm";
						}
						break;
					}
					case BAULRO:
					{
						if (cond == 1)
						{
							htmltext = "30033-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30033-03.htm";
						}
						else
						{
							htmltext = "30033-04.htm";
						}
						break;
					}
					case SIR_COLLIN:
					{
						if (cond == 2)
						{
							htmltext = "30311-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30311-03a.htm";
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