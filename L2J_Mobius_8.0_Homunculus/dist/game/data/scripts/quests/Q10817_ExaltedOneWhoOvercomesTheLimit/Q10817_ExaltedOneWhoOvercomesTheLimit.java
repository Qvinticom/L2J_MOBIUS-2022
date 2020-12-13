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
package quests.Q10817_ExaltedOneWhoOvercomesTheLimit;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10811_ExaltedOneWhoFacesTheLimit.Q10811_ExaltedOneWhoFacesTheLimit;

/**
 * Exalted, One Who Overcomes the Limit (10817)
 * @URL https://l2wiki.com/Exalted,_One_Who_Overcomes_the_Limit
 * @author Mobius
 */
public class Q10817_ExaltedOneWhoOvercomesTheLimit extends Quest
{
	// NPC
	private static final int LIONEL = 33907;
	// Items
	private static final int PROOF_OF_RESISTANCE = 80823;
	private static final int LIONEL_MISSION_LIST_2 = 45632;
	// Rewards
	private static final int SPELLBOOK_DIGNITY_OF_THE_EXALTED = 45923;
	private static final int SPELLBOOK_BELIEF_OF_THE_EXALTED = 45925;
	private static final int SPELLBOOK_FAVOR_OF_THE_EXALTED = 45928;
	private static final int EXALSTED_WEAPON_UPGRADE_STONE = 81200;
	private static final int SECOND_EXALTED_QUEST_REWARD_P = 81209;
	private static final int SECOND_EXALTED_QUEST_REWARD_M = 81210;
	// Misc
	private static final int MIN_LEVEL = 101;
	private static final int MIN_COMPLETE_LEVEL = 102;
	private static final int PROOF_OF_RESISTANCE_NEEDED = 40000;
	// Monsters
	private static final int[] MONSTERS =
	{
		// Hellbound monsters
		23811, // Cantera Tanya
		23812, // Cantera Deathmoz
		23813, // Cantera Floxis
		23814, // Cantera Belika
		23815, // Cantera Bridget
		23354, // Decay Hannibal
		23355, // Armor Beast
		23356, // Klein Soldier
		23357, // Disorder Warrior
		23360, // Bizuard
		23361, // Mutated Fly
		24511, // Lunatikan
		24515, // Kandiloth
		24512, // Garion Neti
		24513, // Desert Wendigo
		24514, // Koraza
	};
	
	public Q10817_ExaltedOneWhoOvercomesTheLimit()
	{
		super(10817);
		addStartNpc(LIONEL);
		addTalkId(LIONEL);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "33907-07.html");
		addCondCompletedQuest(Q10811_ExaltedOneWhoFacesTheLimit.class.getSimpleName(), "33907-02.html");
		registerQuestItems(LIONEL_MISSION_LIST_2, PROOF_OF_RESISTANCE_NEEDED);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		final Race race = player.getRace();
		final ClassId classId = player.getClassId();
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "33907-03.htm":
			case "33907-04.htm":
			{
				htmltext = event;
				break;
			}
			case "33907-05.html":
			{
				if (qs.isCreated())
				{
					giveItems(player, LIONEL_MISSION_LIST_2, 1);
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "33907-09.html":
			{
				if (qs.isCond(2) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
				{
					giveItems(player, SPELLBOOK_DIGNITY_OF_THE_EXALTED, 1);
					giveItems(player, SPELLBOOK_BELIEF_OF_THE_EXALTED, 1);
					giveItems(player, SPELLBOOK_FAVOR_OF_THE_EXALTED, 1);
					giveItems(player, EXALSTED_WEAPON_UPGRADE_STONE, 1);
					
					switch (race)
					{
						case HUMAN:
						case ELF:
						case DARK_ELF:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId.getId()) || (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_WYNN_GROUP, classId.getId())))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_M, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_EOLH_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_M, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId.getId()) || (player.getClassId() == ClassId.TYRR_DUELIST))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_DREADNOUGHT)
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_SIGEL_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_M, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_BOW, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DAGGER, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DANCE, classId.getId()) || (player.getClassId() == ClassId.GLADIATOR))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (player.getClassId() == ClassId.WARLORD)
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (player.getClassId() == ClassId.DUELIST)
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.TANKER_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_WARRIOR_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							break;
						}
						case DWARF:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.DWARF_BOUNTY_CLASS, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							break;
						}
						case ORC:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_GRAND_KHAVATARI)
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_TITAN)
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (player.isMageClass())
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.LIGHT_ARMOR_CLASS, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							break;
						}
						case KAMAEL:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_M, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_M, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_ARCHER, classId.getId()))
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							else
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
								break;
							}
							break;
						}
						case ERTHEIA:
						{
							if (player.isMageClass())
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_M, 1);
							}
							else
							{
								giveItems(player, SECOND_EXALTED_QUEST_REWARD_P, 1);
							}
							break;
						}
					}
					
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "33907-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(2) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
				{
					htmltext = "33907-08.html";
				}
				else
				{
					htmltext = "33907-06.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(PlayerInstance player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			if (getQuestItemsCount(player, PROOF_OF_RESISTANCE) < PROOF_OF_RESISTANCE_NEEDED)
			{
				giveItemRandomly(player, PROOF_OF_RESISTANCE, 1, PROOF_OF_RESISTANCE_NEEDED, 1, true);
			}
			if ((getQuestItemsCount(player, PROOF_OF_RESISTANCE) >= PROOF_OF_RESISTANCE_NEEDED) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
			{
				qs.setCond(2, true);
			}
		}
	}
	
}
