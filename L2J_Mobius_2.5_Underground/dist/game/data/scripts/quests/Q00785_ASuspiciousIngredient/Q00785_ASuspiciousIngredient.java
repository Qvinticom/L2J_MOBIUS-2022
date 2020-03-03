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
package quests.Q00785_ASuspiciousIngredient;

import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.base.ClassId;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * A Suspicious Ingredient (785)
 * @URL https://l2wiki.com/A_Suspicious_Ingredient
 * @author Gigi
 */
public class Q00785_ASuspiciousIngredient extends Quest
{
	// NPC
	private static final int MYSTERIUS_WIZARD = 31522;
	// Monsters
	private static final int[] MONSTERS =
	{
		21547, // Corrupted Knight
		21549, // Corrupted Royal Guard
		21553, // Trampled Man
		21555, // Slaughter Executioner
		21581, // Bone Puppeteer
		21548, // Resurrected Knight
		21551, // Resurrected Royal Guard
		21557, // Bone Snatcher
		21559, // Bone Maker
		21560, // Bone Shaper
		21561, // Sacrificed Man
		21596, // Requiem Lord
		21598, // Requiem Behemot
		21565, // Bone Animator
		21563, // Bone Collector
		21567, // Bone Slayer
		21570, // Ghost of Batrayer
		21580, // Bone Caster
		21572, // Bone Sweeper
		21577, // Bone Grinder
		21578, // Behemot Zombie
		21599, // Requeem priest
		21600 // Requeem Behemot
	};
	// Items
	private static final int MONSTER_FLESH = 39732;
	private static final int MONSTER_BLOOD = 39733;
	private static final int STEEL_DOOR_GUILD_REWARD_BOX = 37391;
	// Misc
	private static final int MIN_LEVEL = 65;
	private static final int MAX_LEVEL = 70;
	
	public Q00785_ASuspiciousIngredient()
	{
		super(785);
		addStartNpc(MYSTERIUS_WIZARD);
		addTalkId(MYSTERIUS_WIZARD);
		addKillId(MONSTERS);
		registerQuestItems(MONSTER_FLESH, MONSTER_BLOOD);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondClassId(ClassId.CLOUD_BREAKER, "no_quest.html");
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
			case "31522-02.htm":
			case "31522-03.htm":
			case "31522-07.html":
			case "31522-08.html":
			{
				htmltext = event;
				break;
			}
			case "31522-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "31522-09.html":
			{
				if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && (getQuestItemsCount(player, MONSTER_BLOOD) < 100))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 14140350, 3393);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 1);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && ((getQuestItemsCount(player, MONSTER_BLOOD) >= 100) && (getQuestItemsCount(player, MONSTER_BLOOD) <= 199)))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 28280700, 6786);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 2);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && ((getQuestItemsCount(player, MONSTER_BLOOD) >= 200) && (getQuestItemsCount(player, MONSTER_BLOOD) <= 299)))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 42421050, 10179);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 3);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && ((getQuestItemsCount(player, MONSTER_BLOOD) >= 300) && (getQuestItemsCount(player, MONSTER_BLOOD) <= 399)))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 56561400, 13572);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 4);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && ((getQuestItemsCount(player, MONSTER_BLOOD) >= 400) && (getQuestItemsCount(player, MONSTER_BLOOD) <= 499)))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 70701750, 16965);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 5);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && ((getQuestItemsCount(player, MONSTER_BLOOD) >= 500) && (getQuestItemsCount(player, MONSTER_BLOOD) <= 599)))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 84842100, 20358);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 6);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && ((getQuestItemsCount(player, MONSTER_BLOOD) >= 600) && (getQuestItemsCount(player, MONSTER_BLOOD) <= 699)))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 98982450, 23751);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 7);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && ((getQuestItemsCount(player, MONSTER_BLOOD) >= 700) && (getQuestItemsCount(player, MONSTER_BLOOD) <= 799)))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 113122800, 27144);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 8);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && ((getQuestItemsCount(player, MONSTER_BLOOD) >= 800) && (getQuestItemsCount(player, MONSTER_BLOOD) <= 899)))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 127263150, 30537);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 9);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
				if ((getQuestItemsCount(player, MONSTER_FLESH) >= 50) && (getQuestItemsCount(player, MONSTER_BLOOD) >= 900))
				{
					takeItems(player, MONSTER_FLESH, -1);
					takeItems(player, MONSTER_BLOOD, -1);
					addExpAndSp(player, 141403500, 33930);
					giveItems(player, STEEL_DOOR_GUILD_REWARD_BOX, 10);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
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
		if (npc.getId() == MYSTERIUS_WIZARD)
		{
			switch (qs.getState())
			{
				case State.COMPLETED:
				{
					if (!qs.isNowAvailable())
					{
						htmltext = "31522-10.html";
						break;
					}
					qs.setState(State.CREATED);
					break;
				}
				case State.CREATED:
				{
					htmltext = "31522-01.htm";
					break;
				}
				case State.STARTED:
				{
					if (qs.isCond(1))
					{
						htmltext = "31522-05.html";
					}
					else if (qs.isStarted() && qs.isCond(2))
					{
						htmltext = "31522-06.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted() && !qs.isNowAvailable())
		{
			htmltext = "31522-10.html";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.isCond(1)))
		{
			if (giveItemRandomly(killer, npc, MONSTER_FLESH, 1, 50, 0.15, true))
			{
				qs.setCond(2, true);
			}
		}
		if ((qs != null) && (qs.isCond(2)))
		{
			if (giveItemRandomly(killer, npc, MONSTER_BLOOD, 1, 900, 0.25, true))
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}