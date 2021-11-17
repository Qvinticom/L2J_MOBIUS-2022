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
package quests.Q00567_BasicMissionIsleOfSouls;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Basic Mission: Isle of Souls (567)
 * @URL https://l2wiki.com/Basic_Mission:_Isle_of_Souls
 * @author Dmitri
 */
public class Q00567_BasicMissionIsleOfSouls extends Quest
{
	// NPCs
	private static final int PENNY = 34413;
	private static final int HESET = 33780;
	private static final int TAPOY = 30499;
	// Rewards
	private static final int SCROLL_OF_ESCAPE_SOUL_ISLAND = 47059;
	private static final int SCROLL_OF_ESCAPE_TOWN_OF_ADEN = 48413;
	// Misc
	private static final int MIN_LEVEL = 93;
	private static final int MAX_LEVEL = 94;
	// Location
	private static final Location TOWN_OF_ADEN = new Location(146632, 26760, -2213);
	
	public Q00567_BasicMissionIsleOfSouls()
	{
		super(567);
		addStartNpc(PENNY);
		addTalkId(PENNY, HESET, TAPOY);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "nolevel.html");
		addFactionLevel(Faction.ADVENTURE_GUILD, 5, "34413-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "34413-02.htm":
			case "34413-03.htm":
			case "34413-06.html":
			case "34413-07.html":
			case "30499-02.html":
			case "33780-02.html":
			{
				htmltext = event;
				break;
			}
			case "34413-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34413-08.html": // PENNY
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "30499-03.html": // TAPOY
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "33780-03.html": // HESET
			{
				qs.setCond(5, true);
				htmltext = event;
				break;
			}
			case "33780-05.html": // HESET
			{
				giveItems(player, SCROLL_OF_ESCAPE_TOWN_OF_ADEN, 1);
				htmltext = event;
				break;
			}
			case "usescroll":
			{
				// TODO: force player to use item SCROLL_OF_ESCAPE_TOWN_OF_ADEN
				player.teleToLocation(TOWN_OF_ADEN); // Town of Aden near Npc Penny - temp solution
				takeItems(player, SCROLL_OF_ESCAPE_TOWN_OF_ADEN, -1); // remove SOE - temp solution
				break;
			}
			case "34413-10.html":
			{
				final StringBuilder str = new StringBuilder("00");
				checkQuestCompleted(player, str); // Initialize the array with all quests completed
				if (str.indexOf("11") != -1) // verify if all quests completed
				{
					giveItems(player, SCROLL_OF_ESCAPE_SOUL_ISLAND, 1);
					addExpAndSp(player, 973202790, 973200);
					addFactionPoints(player, Faction.ADVENTURE_GUILD, 260); // add FP points to ADVENTURE_GUILD Faction
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
				}
				else
				{
					htmltext = "34413-08.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == PENNY)
				{
					htmltext = "34413-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case PENNY:
					{
						if (qs.getCond() == 1)
						{
							htmltext = "34413-05.html";
						}
						else if (qs.getCond() == 2)
						{
							htmltext = "34413-08.html";
						}
						else if (qs.getCond() == 6)
						{
							htmltext = "34413-09.html";
						}
						break;
					}
					case TAPOY:
					{
						if (qs.getCond() == 2)
						{
							htmltext = "30499-01.html";
						}
						else if (qs.getCond() == 3)
						{
							final QuestState st = player.getQuestState("Q10386_MysteriousJourney");
							if ((st != null) && st.isCompleted())
							{
								qs.setCond(4, true);
								htmltext = null;
							}
							else
							{
								htmltext = "30499-03.html";
							}
						}
						else if (qs.getCond() == 4)
						{
							htmltext = "30499-04.html";
						}
						break;
					}
					case HESET:
					{
						if (qs.getCond() == 4)
						{
							htmltext = "33780-01.html";
						}
						else if (qs.getCond() == 5)
						{
							final StringBuilder str = new StringBuilder("00");
							checkQuestCompleted(player, str); // Initialize the array with all quests completed
							if (str.indexOf("11") != -1) // verify if all quests completed
							{
								qs.setCond(6, true);
								htmltext = "33780-04.html";
							}
							else
							{
								htmltext = "33780-03.html";
							}
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (qs.isNowAvailable())
				{
					qs.setState(State.CREATED);
					htmltext = "34413-01.htm";
				}
				else
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
				}
				break;
			}
		}
		return htmltext;
	}
	
	private StringBuilder checkQuestCompleted(Player player, StringBuilder string)
	{
		int index = 0;
		final char ch = '1';
		final QuestState st1 = player.getQuestState("Q00752_UncoverTheSecret");
		if ((st1 != null) && st1.isCompleted())
		{
			index = 0;
			string.setCharAt(index, ch);
		}
		final QuestState st2 = player.getQuestState("Q00587_MoreAggressiveOperation");
		if ((st2 != null) && st2.isCompleted())
		{
			index = 1;
			string.setCharAt(index, ch);
		}
		return string;
	}
}
