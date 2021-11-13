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
package quests.Q033_MakeAPairOfDressShoes;

import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q037_MakeFormalWear.Q037_MakeFormalWear;

public class Q033_MakeAPairOfDressShoes extends Quest
{
	// NPCs
	private static final int WOODLEY = 30838;
	private static final int IAN = 30164;
	private static final int LEIKAR = 31520;
	// Items
	private static final int LEATHER = 1882;
	private static final int THREAD = 1868;
	private static final int ADENA = 57;
	// Rewards
	public static final int DRESS_SHOES_BOX = 7113;
	
	public Q033_MakeAPairOfDressShoes()
	{
		super(33, "Make a Pair of Dress Shoes");
		addStartNpc(WOODLEY);
		addTalkId(WOODLEY, IAN, LEIKAR);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30838-1.htm":
			{
				st.startQuest();
				break;
			}
			case "31520-1.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30838-3.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30838-5.htm":
			{
				if ((st.getQuestItemsCount(LEATHER) >= 200) && (st.getQuestItemsCount(THREAD) >= 600) && (st.getQuestItemsCount(ADENA) >= 200000))
				{
					st.setCond(4);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(ADENA, 200000);
					st.takeItems(LEATHER, 200);
					st.takeItems(THREAD, 600);
				}
				else
				{
					htmltext = "30838-4a.htm";
				}
				break;
			}
			case "30164-1.htm":
			{
				if (st.getQuestItemsCount(ADENA) >= 300000)
				{
					st.setCond(5);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(ADENA, 300000);
				}
				else
				{
					htmltext = "30164-1a.htm";
				}
				break;
			}
			case "30838-7.htm":
			{
				st.giveItems(DRESS_SHOES_BOX, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
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
				if (player.getLevel() >= 60)
				{
					final QuestState fwear = player.getQuestState(Q037_MakeFormalWear.class.getSimpleName());
					if ((fwear != null) && fwear.isCond(7))
					{
						htmltext = "30838-0.htm";
					}
					else
					{
						htmltext = "30838-0a.htm";
					}
				}
				else
				{
					htmltext = "30838-0b.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case WOODLEY:
					{
						if (cond == 1)
						{
							htmltext = "30838-1.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30838-2.htm";
						}
						else if (cond == 3)
						{
							if ((st.getQuestItemsCount(LEATHER) >= 200) && (st.getQuestItemsCount(THREAD) >= 600) && (st.getQuestItemsCount(ADENA) >= 200000))
							{
								htmltext = "30838-4.htm";
							}
							else
							{
								htmltext = "30838-4a.htm";
							}
						}
						else if (cond == 4)
						{
							htmltext = "30838-5a.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30838-6.htm";
						}
						break;
					}
					case LEIKAR:
					{
						if (cond == 1)
						{
							htmltext = "31520-0.htm";
						}
						else if (cond > 1)
						{
							htmltext = "31520-1a.htm";
						}
						break;
					}
					case IAN:
					{
						if (cond == 4)
						{
							htmltext = "30164-0.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30164-2.htm";
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