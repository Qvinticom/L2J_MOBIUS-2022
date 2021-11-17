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
package quests.Q043_HelpTheSister;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q043_HelpTheSister extends Quest
{
	// NPCs
	private static final int COOPER = 30829;
	private static final int GALLADUCCI = 30097;
	// Monsters
	private static final int SPECTER = 20171;
	private static final int SORROW_MAIDEN = 20197;
	// Items
	private static final int CRAFTED_DAGGER = 220;
	private static final int MAP_PIECE = 7550;
	private static final int MAP = 7551;
	private static final int PET_TICKET = 7584;
	
	public Q043_HelpTheSister()
	{
		super(43, "Help the Sister!");
		registerQuestItems(MAP_PIECE, MAP);
		addStartNpc(COOPER);
		addTalkId(COOPER, GALLADUCCI);
		addKillId(SPECTER, SORROW_MAIDEN);
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
			case "30829-01.htm":
			{
				st.startQuest();
				break;
			}
			case "30829-03.htm":
			{
				if (st.hasQuestItems(CRAFTED_DAGGER))
				{
					st.setCond(2);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(CRAFTED_DAGGER, 1);
				}
				break;
			}
			case "30829-05.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(MAP_PIECE, 30);
				st.giveItems(MAP, 1);
				break;
			}
			case "30097-06.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(MAP, 1);
				break;
			}
			case "30829-07.htm":
			{
				st.giveItems(PET_TICKET, 1);
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
				htmltext = (player.getLevel() < 26) ? "30829-00a.htm" : "30829-00.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case COOPER:
					{
						if (cond == 1)
						{
							htmltext = (!st.hasQuestItems(CRAFTED_DAGGER)) ? "30829-01a.htm" : "30829-02.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30829-03a.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30829-04.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30829-05a.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30829-06.htm";
						}
						break;
					}
					case GALLADUCCI:
					{
						if (cond == 4)
						{
							htmltext = "30097-05.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30097-06a.htm";
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
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, 2);
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItemsAlways(MAP_PIECE, 1, 30))
		{
			st.setCond(3);
		}
		
		return null;
	}
}