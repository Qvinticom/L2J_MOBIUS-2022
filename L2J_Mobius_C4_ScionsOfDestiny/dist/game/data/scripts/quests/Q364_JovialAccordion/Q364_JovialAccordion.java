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
package quests.Q364_JovialAccordion;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q364_JovialAccordion extends Quest
{
	// NPCs
	private static final int BARBADO = 30959;
	private static final int SWAN = 30957;
	private static final int SABRIN = 30060;
	private static final int XABER = 30075;
	private static final int CLOTH_CHEST = 30961;
	private static final int BEER_CHEST = 30960;
	// Items
	private static final int KEY_1 = 4323;
	private static final int KEY_2 = 4324;
	private static final int STOLEN_BEER = 4321;
	private static final int STOLEN_CLOTHES = 4322;
	private static final int ECHO = 4421;
	
	public Q364_JovialAccordion()
	{
		super(364, "Jovial Accordion");
		registerQuestItems(KEY_1, KEY_2, STOLEN_BEER, STOLEN_CLOTHES);
		addStartNpc(BARBADO);
		addTalkId(BARBADO, SWAN, SABRIN, XABER, CLOTH_CHEST, BEER_CHEST);
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
			case "30959-02.htm":
			{
				st.startQuest();
				st.set("items", "0");
				break;
			}
			case "30957-02.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(KEY_1, 1);
				st.giveItems(KEY_2, 1);
				break;
			}
			case "30960-04.htm":
			{
				if (st.hasQuestItems(KEY_2))
				{
					st.takeItems(KEY_2, 1);
					if (Rnd.nextBoolean())
					{
						htmltext = "30960-02.htm";
						st.giveItems(STOLEN_BEER, 1);
						st.playSound(QuestState.SOUND_ITEMGET);
					}
				}
				break;
			}
			case "30961-04.htm":
			{
				if (st.hasQuestItems(KEY_1))
				{
					st.takeItems(KEY_1, 1);
					if (Rnd.nextBoolean())
					{
						htmltext = "30961-02.htm";
						st.giveItems(STOLEN_CLOTHES, 1);
						st.playSound(QuestState.SOUND_ITEMGET);
					}
				}
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
				htmltext = (player.getLevel() < 15) ? "30959-00.htm" : "30959-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				final int stolenItems = st.getInt("items");
				switch (npc.getNpcId())
				{
					case BARBADO:
					{
						if ((cond == 1) || (cond == 2))
						{
							htmltext = "30959-03.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30959-04.htm";
							st.giveItems(ECHO, 1);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					}
					case SWAN:
					{
						if (cond == 1)
						{
							htmltext = "30957-01.htm";
						}
						else if (cond == 2)
						{
							if (stolenItems > 0)
							{
								st.setCond(3);
								st.playSound(QuestState.SOUND_MIDDLE);
								
								if (stolenItems == 2)
								{
									htmltext = "30957-04.htm";
									st.rewardItems(57, 100);
								}
								else
								{
									htmltext = "30957-05.htm";
								}
							}
							else
							{
								if (!st.hasQuestItems(KEY_1) && !st.hasQuestItems(KEY_2))
								{
									htmltext = "30957-06.htm";
									st.playSound(QuestState.SOUND_FINISH);
									st.exitQuest(true);
								}
								else
								{
									htmltext = "30957-03.htm";
								}
							}
						}
						else if (cond == 3)
						{
							htmltext = "30957-07.htm";
						}
						break;
					}
					case BEER_CHEST:
					{
						htmltext = "30960-03.htm";
						if ((cond == 2) && st.hasQuestItems(KEY_2))
						{
							htmltext = "30960-01.htm";
						}
						break;
					}
					case CLOTH_CHEST:
					{
						htmltext = "30961-03.htm";
						if ((cond == 2) && st.hasQuestItems(KEY_1))
						{
							htmltext = "30961-01.htm";
						}
						break;
					}
					case SABRIN:
					{
						if (st.hasQuestItems(STOLEN_BEER))
						{
							htmltext = "30060-01.htm";
							st.set("items", String.valueOf(stolenItems + 1));
							st.playSound(QuestState.SOUND_ITEMGET);
							st.takeItems(STOLEN_BEER, 1);
						}
						else
						{
							htmltext = "30060-02.htm";
						}
						break;
					}
					case XABER:
					{
						if (st.hasQuestItems(STOLEN_CLOTHES))
						{
							htmltext = "30075-01.htm";
							st.set("items", String.valueOf(stolenItems + 1));
							st.playSound(QuestState.SOUND_ITEMGET);
							st.takeItems(STOLEN_CLOTHES, 1);
						}
						else
						{
							htmltext = "30075-02.htm";
						}
						break;
					}
				}
				break;
			}
		}
		
		return htmltext;
	}
}