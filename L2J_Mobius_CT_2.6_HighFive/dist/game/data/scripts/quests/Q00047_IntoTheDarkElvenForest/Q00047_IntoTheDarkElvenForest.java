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
package quests.Q00047_IntoTheDarkElvenForest;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q00008_AnAdventureBegins.Q00008_AnAdventureBegins;

/**
 * Into The Dark Elven Forest (47)
 * @author janiko
 */
public class Q00047_IntoTheDarkElvenForest extends Quest
{
	// Npcs
	private static final int GALLADUCCI = 30097;
	private static final int GENTLER = 30094;
	private static final int SANDRA = 30090;
	private static final int DUSTIN = 30116;
	// Items
	private static final int MARK_OF_TRAVELER = 7570;
	private static final int GALLADUCCIS_ORDER_1 = 7563;
	private static final int GALLADUCCIS_ORDER_2 = 7564;
	private static final int GALLADUCCIS_ORDER_3 = 7565;
	private static final int PURIFIED_MAGIC_NECKLACE = 7566;
	private static final int GEMSTONE_POWDER = 7567;
	private static final int MAGIC_SWORD_HILT = 7568;
	// Misc
	private static final int MIN_LEVEL = 3;
	// Reward
	private static final int SCROLL_OF_ESCAPE_DARK_ELF_VILLAGE = 7556;
	// Get condition for each npc
	private static Map<Integer, ItemHolder> NPC_ITEMS = new HashMap<>();
	static
	{
		NPC_ITEMS.put(GENTLER, new ItemHolder(1, GALLADUCCIS_ORDER_1));
		NPC_ITEMS.put(SANDRA, new ItemHolder(3, GALLADUCCIS_ORDER_2));
		NPC_ITEMS.put(DUSTIN, new ItemHolder(5, GALLADUCCIS_ORDER_3));
	}
	
	public Q00047_IntoTheDarkElvenForest()
	{
		super(47);
		addStartNpc(GALLADUCCI);
		addTalkId(GALLADUCCI);
		addTalkId(NPC_ITEMS.keySet());
		registerQuestItems(GALLADUCCIS_ORDER_1, GALLADUCCIS_ORDER_2, GALLADUCCIS_ORDER_3, PURIFIED_MAGIC_NECKLACE, GEMSTONE_POWDER, MAGIC_SWORD_HILT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		switch (event)
		{
			case "30097-04.htm":
			{
				if (qs.isCreated())
				{
					qs.startQuest();
					giveItems(player, GALLADUCCIS_ORDER_1, 1);
					htmltext = event;
				}
				break;
			}
			case "30094-02.html":
			{
				if (qs.isCond(1) && hasQuestItems(player, GALLADUCCIS_ORDER_1))
				{
					takeItems(player, GALLADUCCIS_ORDER_1, 1);
					giveItems(player, MAGIC_SWORD_HILT, 1);
					qs.setCond(2, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30094-03.html";
				}
				break;
			}
			case "30097-07.html":
			{
				if (qs.isCond(2) && hasQuestItems(player, MAGIC_SWORD_HILT))
				{
					takeItems(player, MAGIC_SWORD_HILT, 1);
					giveItems(player, GALLADUCCIS_ORDER_2, 1);
					qs.setCond(3, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30097-08.html";
				}
				break;
			}
			case "30090-02.html":
			{
				if (qs.isCond(3) && hasQuestItems(player, GALLADUCCIS_ORDER_2))
				{
					takeItems(player, GALLADUCCIS_ORDER_2, 1);
					giveItems(player, GEMSTONE_POWDER, 1);
					qs.setCond(4, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30090-03.html";
				}
				break;
			}
			case "30097-11.html":
			{
				if (qs.isCond(4) && hasQuestItems(player, GEMSTONE_POWDER))
				{
					takeItems(player, GEMSTONE_POWDER, 1);
					giveItems(player, GALLADUCCIS_ORDER_3, 1);
					qs.setCond(5, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30097-12.html";
				}
				break;
			}
			case "30116-02.html":
			{
				if (qs.isCond(5) && hasQuestItems(player, GALLADUCCIS_ORDER_3))
				{
					takeItems(player, GALLADUCCIS_ORDER_3, 1);
					giveItems(player, PURIFIED_MAGIC_NECKLACE, 1);
					qs.setCond(6, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30116-03.html";
				}
				break;
			}
			case "30097-15.html":
			{
				if (qs.isCond(6) && hasQuestItems(player, PURIFIED_MAGIC_NECKLACE))
				{
					giveItems(player, SCROLL_OF_ESCAPE_DARK_ELF_VILLAGE, 1);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30097-16.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		QuestState qs = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		
		switch (npc.getId())
		{
			case GALLADUCCI:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						if (talker.getLevel() < MIN_LEVEL)
						{
							htmltext = "30097-03.html";
						}
						else
						{
							qs = talker.getQuestState(Q00008_AnAdventureBegins.class.getSimpleName());
							if ((qs != null) && qs.isCompleted() && hasQuestItems(talker, MARK_OF_TRAVELER))
							{
								htmltext = "30097-01.htm";
							}
							else
							{
								htmltext = "30097-02.html";
							}
						}
						break;
					}
					case State.STARTED:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "30097-05.html";
								break;
							}
							case 2:
							{
								if (hasQuestItems(talker, MAGIC_SWORD_HILT))
								{
									htmltext = "30097-06.html";
								}
								break;
							}
							case 3:
							{
								htmltext = "30097-09.html";
								break;
							}
							case 4:
							{
								if (hasQuestItems(talker, GEMSTONE_POWDER))
								{
									htmltext = "30097-10.html";
								}
								break;
							}
							case 5:
							{
								htmltext = "30097-13.html";
								break;
							}
							case 6:
							{
								if (hasQuestItems(talker, PURIFIED_MAGIC_NECKLACE))
								{
									htmltext = "30097-14.html";
								}
								break;
							}
						}
						break;
					}
					case State.COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg(talker);
						break;
					}
				}
				break;
			}
			case GENTLER:
			case SANDRA:
			case DUSTIN:
			{
				if (qs.isStarted())
				{
					final ItemHolder i = NPC_ITEMS.get(npc.getId());
					final int cond = i.getId();
					if (qs.isCond(cond))
					{
						final int itemId = (int) i.getCount();
						if (hasQuestItems(talker, itemId))
						{
							htmltext = npc.getId() + "-01.html";
						}
					}
					else if (qs.isCond(cond + 1))
					{
						htmltext = npc.getId() + "-04.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
}