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
package quests.Q00593_BasicMissionPaganTemple;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Basic Mission: Pagan Temple (593)
 * @URL https://l2wiki.com/Basic_Mission:_Pagan_Temple
 * @author Dmitri
 */
public class Q00593_BasicMissionPaganTemple extends Quest
{
	// NPCs
	private static final int PENNY = 34413;
	private static final int RAZEN = 33803;
	private static final int ELIYAH = 31329;
	// Rewards
	private static final int SCROLL_OF_ESCAPE_PAGAN_TEMPLE = 40225;
	private static final int SCROLL_OF_ESCAPE_TOWN_OF_ADEN = 48413;
	// Misc
	private static final int MIN_LEVEL = 97;
	private static final int MAX_LEVEL = 99;
	// Location
	private static final Location TOWN_OF_ADEN = new Location(146632, 26760, -2213);
	
	public Q00593_BasicMissionPaganTemple()
	{
		super(593);
		addStartNpc(PENNY);
		addTalkId(PENNY, RAZEN, ELIYAH);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "nolevel.html");
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
			case "31329-02.html":
			case "33803-02.html":
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
			case "31329-03.html": // ELIYAH
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "33803-03.html": // RAZEN
			{
				qs.setCond(5, true);
				htmltext = event;
				break;
			}
			case "33803-05.html": // RAZEN
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
				// Rewards
				giveItems(player, SCROLL_OF_ESCAPE_PAGAN_TEMPLE, 1);
				addExpAndSp(player, 1346064975L, 1346055);
				addFactionPoints(player, Faction.ADVENTURE_GUILD, 140); // add FP points to ADVENTURE_GUILD Faction
				qs.exitQuest(QuestType.DAILY, true);
				htmltext = event;
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
					case ELIYAH:
					{
						if (qs.getCond() == 2)
						{
							htmltext = "31329-01.html";
						}
						else if (qs.getCond() == 3)
						{
							final QuestState st = player.getQuestState("Q10388_ConspiracyBehindDoor");
							if ((st != null) && st.isCompleted())
							{
								qs.setCond(4, true);
								htmltext = null;
							}
							else
							{
								htmltext = "31329-03.html";
							}
						}
						else if (qs.getCond() == 4)
						{
							htmltext = "31329-04.html";
						}
						break;
					}
					case RAZEN:
					{
						if (qs.getCond() == 4)
						{
							htmltext = "33803-01.html";
						}
						else if (qs.getCond() == 5)
						{
							final QuestState st = player.getQuestState("Q00757_TriolsMovement");
							if ((st != null) && st.isCompleted())
							{
								qs.setCond(6, true);
								htmltext = "33803-04.html";
							}
							else
							{
								htmltext = "33803-03.html";
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
}
