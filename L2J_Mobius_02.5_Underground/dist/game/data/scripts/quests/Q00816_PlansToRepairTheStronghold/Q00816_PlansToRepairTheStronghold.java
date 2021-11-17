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
package quests.Q00816_PlansToRepairTheStronghold;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Plans to Repair the Stronghold (816)
 * @URL https://l2wiki.com/Plans_to_Repair_the_Stronghold
 * @author Dmitri
 */
public class Q00816_PlansToRepairTheStronghold extends Quest
{
	// NPCs
	private static final int ADOLPH = 34058;
	// Monsters
	private static final int[] MONSTERS =
	{
		23505, // Fortress Raider 101
		23506, // Fortress Guardian Captain 101
		23507, // Atelia Passionate Soldier 101
		23508, // Atelia Elite Captain 101
		23509, // Fortress Dark Wizard 102
		23510, // Atelia Flame Master 102
		23511, // Fortress Archon 102
		23512 // Atelia High Priest 102
	};
	// Items
	private static final int MATERIAL_QUEST = 46142; // Stronghold Flag Repair Supplies
	private static final int BASIC_SUPPLY_BOX = 47175;
	private static final int INTERMEDIATE_SUPPLY_BOX = 47176;
	private static final int ADVANCED_SUPPLY_BOX = 47177;
	// Misc
	private static final int MIN_LEVEL = 101;
	
	public Q00816_PlansToRepairTheStronghold()
	{
		super(816);
		addStartNpc(ADOLPH);
		addTalkId(ADOLPH);
		addKillId(MONSTERS);
		registerQuestItems(MATERIAL_QUEST);
		addCondMinLevel(MIN_LEVEL, "34058-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "34058-02.htm":
			case "34058-03.htm":
			case "34058-04.htm":
			case "34058-04a.htm":
			case "34058-04b.htm":
			case "34058-04d.htm":
			case "34058-06.html":
			case "34058-06a.html":
			case "34058-06b.html":
			case "34058-06d.html":
			{
				htmltext = event;
				break;
			}
			case "select_mission":
			{
				qs.startQuest();
				htmltext = "34058-04.htm";
				break;
			}
			case "return":
			{
				htmltext = "34058-04.htm";
				break;
			}
			case "34058-07.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34058-07a.html":
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "34058-07b.html":
			{
				qs.setCond(4, true);
				htmltext = event;
				break;
			}
			case "34058-07d.html":
			{
				qs.setCond(5, true);
				htmltext = event;
				break;
			}
			case "34058-10.html":
			{
				final int chance = getRandom(100);
				switch (qs.getCond())
				{
					case 6:
					{
						if ((getQuestItemsCount(player, MATERIAL_QUEST) == 200) && (player.getLevel() >= MIN_LEVEL))
						{
							if (chance < 2)
							{
								giveItems(player, ADVANCED_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, INTERMEDIATE_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, BASIC_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 18_155_754_360L, 18_155_700);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
						}
						else
						{
							htmltext = getNoQuestLevelRewardMsg(player);
						}
						break;
					}
					case 7:
					{
						if ((getQuestItemsCount(player, MATERIAL_QUEST) == 400) && (player.getLevel() >= MIN_LEVEL))
						{
							if (chance < 2)
							{
								giveItems(player, ADVANCED_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, BASIC_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, INTERMEDIATE_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 36_311_508_720L, 36_311_400);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
						}
						else
						{
							htmltext = getNoQuestLevelRewardMsg(player);
						}
						break;
					}
					case 8:
					{
						if ((getQuestItemsCount(player, MATERIAL_QUEST) == 600) && (player.getLevel() >= MIN_LEVEL))
						{
							if (chance < 2)
							{
								giveItems(player, BASIC_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, INTERMEDIATE_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, ADVANCED_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 54_467_263_080L, 54_467_100);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
						}
						else
						{
							htmltext = getNoQuestLevelRewardMsg(player);
						}
						break;
					}
					case 9:
					{
						if ((getQuestItemsCount(player, MATERIAL_QUEST) == 800) && (player.getLevel() >= MIN_LEVEL))
						{
							if (chance < 50)
							{
								giveItems(player, BASIC_SUPPLY_BOX, 2);
							}
							else if (chance < 50)
							{
								giveItems(player, INTERMEDIATE_SUPPLY_BOX, 2);
							}
							else if (chance < 50)
							{
								giveItems(player, ADVANCED_SUPPLY_BOX, 2);
							}
							addExpAndSp(player, 72_623_017_440L, 72_622_800);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
						}
						else
						{
							htmltext = getNoQuestLevelRewardMsg(player);
						}
						break;
					}
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
				htmltext = "34058-01.htm";
				// fallthrough
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "34058-04.htm";
						break;
					}
					case 2:
					{
						htmltext = "34058-08.html";
						break;
					}
					case 3:
					{
						htmltext = "34058-08a.html";
						break;
					}
					case 4:
					{
						htmltext = "34058-08b.html";
						break;
					}
					case 5:
					{
						htmltext = "34058-08d.html";
						break;
					}
					case 6:
					case 7:
					case 8:
					case 9:
					{
						htmltext = "34058-09.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (!qs.isNowAvailable())
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
				}
				else
				{
					qs.setState(State.CREATED);
					htmltext = "34058-01.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (qs.getCond() > 1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			switch (qs.getCond())
			{
				case 2:
				{
					if (giveItemRandomly(player, npc, MATERIAL_QUEST, 1, 200, 1, true))
					{
						qs.setCond(6, true);
					}
					break;
				}
				case 3:
				{
					if (giveItemRandomly(player, npc, MATERIAL_QUEST, 1, 400, 1, true))
					{
						qs.setCond(7, true);
					}
					break;
				}
				case 4:
				{
					if (giveItemRandomly(player, npc, MATERIAL_QUEST, 1, 600, 1, true))
					{
						qs.setCond(8, true);
					}
					break;
				}
				case 5:
				{
					if (giveItemRandomly(player, npc, MATERIAL_QUEST, 1, 800, 1, true))
					{
						qs.setCond(9, true);
					}
					break;
				}
			}
		}
	}
}
