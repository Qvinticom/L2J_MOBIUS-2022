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
package quests.Q00784_TheQuietKiller;

import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.base.ClassId;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * The Quiet Killer (784)
 * @URL https://l2wiki.com/The_Quiet_Killer
 * @author Gigi
 */
public class Q00784_TheQuietKiller extends Quest
{
	private static final int KAHMAN = 31554;
	// Monsters
	private static final int[] MONSTERS =
	{
		21508, // Splinter Stakato
		21509, // Splinter Stakato Worker
		21510, // Splinter Stakato Soldier
		21511, // Splinter Stakato Drone
		21512, // Splinter Stakato Drone
		21513, // Needle Stakato
		21514, // Needle Stakato Worker
		21515, // Needle Stakato Soldier
		21516, // Needle Stakato Drone
		21517, // Needle Stakato Drone
		// 20518, // Frenzied Stakato Soldier
		20919 // Frenzied Stakato Drone
	};
	// Items
	private static final int STAKATO_CHITIN = 39730;
	private static final int QUALITY_STAKATO_CHITIN = 39731;
	private static final int EMISSARYS_REWARD_BOX = 39726;
	// Misc
	private static final int MIN_LEVEL = 65;
	private static final int MAX_LEVEL = 70;
	
	public Q00784_TheQuietKiller()
	{
		super(784);
		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);
		addKillId(MONSTERS);
		registerQuestItems(STAKATO_CHITIN, QUALITY_STAKATO_CHITIN);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondClassId(ClassId.MARAUDER, "no_quest.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "31554-02.htm":
			case "31554-03.htm":
			case "31554-07.html":
			case "31554-08.html":
			{
				htmltext = event;
				break;
			}
			case "31554-04.htm":
			{
				qs.startQuest();
				qs.set(Integer.toString(STAKATO_CHITIN), 0);
				qs.set(Integer.toString(QUALITY_STAKATO_CHITIN), 0);
				break;
			}
			case "31554-09.html":
			{
				if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) < 100))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 14140350, 3393);
					giveItems(player, EMISSARYS_REWARD_BOX, 1);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				else if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && ((getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 100) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) <= 199)))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 28280700, 6786);
					giveItems(player, EMISSARYS_REWARD_BOX, 2);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				else if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && ((getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 200) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) <= 299)))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 42421050, 10179);
					giveItems(player, EMISSARYS_REWARD_BOX, 3);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				else if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && ((getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 300) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) <= 399)))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 56561400, 13572);
					giveItems(player, EMISSARYS_REWARD_BOX, 4);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				else if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && ((getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 400) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) <= 499)))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 70701750, 16965);
					giveItems(player, EMISSARYS_REWARD_BOX, 5);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				else if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && ((getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 500) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) <= 599)))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 84842100, 20358);
					giveItems(player, EMISSARYS_REWARD_BOX, 6);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				else if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && ((getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 600) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) <= 699)))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 98982450, 23751);
					giveItems(player, EMISSARYS_REWARD_BOX, 7);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				else if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && ((getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 700) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) <= 799)))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 113122800, 27144);
					giveItems(player, EMISSARYS_REWARD_BOX, 8);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				else if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && ((getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 800) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) <= 899)))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 127263150, 30537);
					giveItems(player, EMISSARYS_REWARD_BOX, 9);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
				if ((getQuestItemsCount(player, STAKATO_CHITIN) >= 50) && (getQuestItemsCount(player, QUALITY_STAKATO_CHITIN) >= 900))
				{
					takeItems(player, STAKATO_CHITIN, -1);
					takeItems(player, QUALITY_STAKATO_CHITIN, -1);
					addExpAndSp(player, 141403500, 33930);
					giveItems(player, EMISSARYS_REWARD_BOX, 10);
					qs.exitQuest(QuestType.DAILY, true);
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (npc.getId() == KAHMAN)
		{
			switch (qs.getState())
			{
				case State.COMPLETED:
				{
					if (!qs.isNowAvailable())
					{
						htmltext = "31554-10.html";
						break;
					}
					qs.setState(State.CREATED);
					break;
				}
				case State.CREATED:
				{
					htmltext = "31554-01.htm";
					break;
				}
				case State.STARTED:
				{
					if (qs.isCond(1))
					{
						htmltext = "31554-05.html";
					}
					else if (qs.isStarted() && qs.isCond(2))
					{
						htmltext = "31554-06.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted() && !qs.isNowAvailable())
		{
			htmltext = "31554-10.html";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.isCond(1)))
		{
			if (giveItemRandomly(killer, npc, STAKATO_CHITIN, 1, 50, 0.15, true))
			{
				qs.setCond(2, true);
			}
		}
		if ((qs != null) && (qs.isCond(2)))
		{
			if (giveItemRandomly(killer, npc, QUALITY_STAKATO_CHITIN, 1, 900, 0.25, true))
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}