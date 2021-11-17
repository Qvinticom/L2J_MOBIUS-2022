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
package quests.Q016_TheComingDarkness;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q016_TheComingDarkness extends Quest
{
	// NPCs
	private static final int HIERARCH = 31517;
	private static final int EVIL_ALTAR_1 = 31512;
	private static final int EVIL_ALTAR_2 = 31513;
	private static final int EVIL_ALTAR_3 = 31514;
	private static final int EVIL_ALTAR_4 = 31515;
	private static final int EVIL_ALTAR_5 = 31516;
	
	// Item
	private static final int CRYSTAL_OF_SEAL = 7167;
	
	public Q016_TheComingDarkness()
	{
		super(16, "The Coming Darkness");
		registerQuestItems(CRYSTAL_OF_SEAL);
		addStartNpc(HIERARCH);
		addTalkId(HIERARCH, EVIL_ALTAR_1, EVIL_ALTAR_2, EVIL_ALTAR_3, EVIL_ALTAR_4, EVIL_ALTAR_5);
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
			case "31517-2.htm":
			{
				st.startQuest();
				st.giveItems(CRYSTAL_OF_SEAL, 5);
				break;
			}
			case "31512-1.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(CRYSTAL_OF_SEAL, 1);
				break;
			}
			case "31513-1.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(CRYSTAL_OF_SEAL, 1);
				break;
			}
			case "31514-1.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(CRYSTAL_OF_SEAL, 1);
				break;
			}
			case "31515-1.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(CRYSTAL_OF_SEAL, 1);
				break;
			}
			case "31516-1.htm":
			{
				st.setCond(6);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(CRYSTAL_OF_SEAL, 1);
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
				htmltext = (player.getLevel() < 62) ? "31517-0a.htm" : "31517-0.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				final int npcId = npc.getNpcId();
				
				switch (npcId)
				{
					case HIERARCH:
					{
						if (cond == 6)
						{
							htmltext = "31517-4.htm";
							st.rewardExpAndSp(221958, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						else
						{
							if (st.hasQuestItems(CRYSTAL_OF_SEAL))
							{
								htmltext = "31517-3.htm";
							}
							else
							{
								htmltext = "31517-3a.htm";
								st.exitQuest(true);
							}
						}
						break;
					}
					case EVIL_ALTAR_1:
					case EVIL_ALTAR_2:
					case EVIL_ALTAR_3:
					case EVIL_ALTAR_4:
					case EVIL_ALTAR_5:
					{
						final int condAltar = npcId - 31511;
						if (cond == condAltar)
						{
							if (st.hasQuestItems(CRYSTAL_OF_SEAL))
							{
								htmltext = npcId + "-0.htm";
							}
							else
							{
								htmltext = "altar_nocrystal.htm";
							}
						}
						else if (cond > condAltar)
						{
							htmltext = npcId + "-2.htm";
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