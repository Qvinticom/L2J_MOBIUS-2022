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
package quests.Q032_AnObviousLie;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q032_AnObviousLie extends Quest
{
	// NPCs
	private static final int GENTLER = 30094;
	private static final int MAXIMILIAN = 30120;
	private static final int MIKI_THE_CAT = 31706;
	// Items
	private static final int SUEDE = 1866;
	private static final int THREAD = 1868;
	private static final int SPIRIT_ORE = 3031;
	private static final int MAP = 7165;
	private static final int MEDICINAL_HERB = 7166;
	// Rewards
	private static final int CAT_EARS = 6843;
	private static final int RACOON_EARS = 7680;
	private static final int RABBIT_EARS = 7683;
	
	public Q032_AnObviousLie()
	{
		super(32, "An Obvious Lie");
		registerQuestItems(MAP, MEDICINAL_HERB);
		addStartNpc(MAXIMILIAN);
		addTalkId(MAXIMILIAN, GENTLER, MIKI_THE_CAT);
		addKillId(20135); // Alligator
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
			case "30120-1.htm":
			{
				st.startQuest();
				break;
			}
			case "30094-1.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(MAP, 1);
				break;
			}
			case "31706-1.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(MAP, 1);
				break;
			}
			case "30094-4.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(MEDICINAL_HERB, 20);
				break;
			}
			case "30094-7.htm":
			{
				if (st.getQuestItemsCount(SPIRIT_ORE) < 500)
				{
					htmltext = "30094-5.htm";
				}
				else
				{
					st.setCond(6);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(SPIRIT_ORE, 500);
				}
				break;
			}
			case "31706-4.htm":
			{
				st.setCond(7);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30094-10.htm":
			{
				st.setCond(8);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30094-13.htm":
			{
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "cat":
			{
				if ((st.getQuestItemsCount(THREAD) < 1000) || (st.getQuestItemsCount(SUEDE) < 500))
				{
					htmltext = "30094-11.htm";
				}
				else
				{
					htmltext = "30094-14.htm";
					st.takeItems(SUEDE, 500);
					st.takeItems(THREAD, 1000);
					st.giveItems(CAT_EARS, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				break;
			}
			case "racoon":
			{
				if ((st.getQuestItemsCount(THREAD) < 1000) || (st.getQuestItemsCount(SUEDE) < 500))
				{
					htmltext = "30094-11.htm";
				}
				else
				{
					htmltext = "30094-14.htm";
					st.takeItems(SUEDE, 500);
					st.takeItems(THREAD, 1000);
					st.giveItems(RACOON_EARS, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				break;
			}
			case "rabbit":
			{
				if ((st.getQuestItemsCount(THREAD) < 1000) || (st.getQuestItemsCount(SUEDE) < 500))
				{
					htmltext = "30094-11.htm";
				}
				else
				{
					htmltext = "30094-14.htm";
					st.takeItems(SUEDE, 500);
					st.takeItems(THREAD, 1000);
					st.giveItems(RABBIT_EARS, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
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
				htmltext = (player.getLevel() < 45) ? "30120-0a.htm" : "30120-0.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case MAXIMILIAN:
					{
						htmltext = "30120-2.htm";
						break;
					}
					case GENTLER:
					{
						if (cond == 1)
						{
							htmltext = "30094-0.htm";
						}
						else if ((cond == 2) || (cond == 3))
						{
							htmltext = "30094-2.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30094-3.htm";
						}
						else if (cond == 5)
						{
							htmltext = (st.getQuestItemsCount(SPIRIT_ORE) < 500) ? "30094-5.htm" : "30094-6.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30094-8.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30094-9.htm";
						}
						else if (cond == 8)
						{
							htmltext = ((st.getQuestItemsCount(THREAD) < 1000) || (st.getQuestItemsCount(SUEDE) < 500)) ? "30094-11.htm" : "30094-12.htm";
						}
						break;
					}
					case MIKI_THE_CAT:
					{
						if (cond == 2)
						{
							htmltext = "31706-0.htm";
						}
						else if ((cond > 2) && (cond < 6))
						{
							htmltext = "31706-2.htm";
						}
						else if (cond == 6)
						{
							htmltext = "31706-3.htm";
						}
						else if (cond > 6)
						{
							htmltext = "31706-5.htm";
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
		final QuestState st = checkPlayerCondition(player, npc, 3);
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItemsAlways(MEDICINAL_HERB, 1, 20))
		{
			st.setCond(4);
		}
		
		return null;
	}
}