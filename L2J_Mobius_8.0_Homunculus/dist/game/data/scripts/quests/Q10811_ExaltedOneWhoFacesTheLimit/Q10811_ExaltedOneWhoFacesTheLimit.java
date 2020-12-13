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
package quests.Q10811_ExaltedOneWhoFacesTheLimit;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerAbilityPointsChanged;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Exalted, One Who Faces the Limit (10811)
 * @author Gladicek
 */
public class Q10811_ExaltedOneWhoFacesTheLimit extends Quest
{
	// NPC
	private static final int LIONEL = 33907;
	// Items
	private static final int LIONEL_HUNTER_MISSING_LIST = 45627;
	private static final int SLAYERS_PROOF = 45871;
	// Misc
	private static final int MIN_LEVEL = 100;
	private static final int MIN_COMPLETE_LEVEL = 101;
	private static final int SLAYERS_PROOF_NEEDED = 40000;
	// Reward
	private static final int SPELLBOOK_DIGNITY_OF_THE_EXALTED = 45922;
	private static final int EXATLED_HEAVY_ARMOR_PACK = 81203;
	private static final int EXATLED_LIGHT_ARMOR_PACK = 81204;
	private static final int EXATLED_ROBE_PACK = 81205;
	private static final int FIRST_EXALTED_QUEST_REWARD_PHYSICAL = 81207;
	private static final int FIRST_EXALTED_QUEST_REWARD_MAGIC = 81208;
	private static final int EXALTED_CUTTER = 81157;
	private static final int EXALTED_SLASHER = 81158;
	private static final int EXALTED_AVENGER = 81159;
	private static final int EXALTED_FIGHTER = 81160;
	private static final int EXALTED_STROMER = 81161;
	private static final int EXALTED_THROWER = 81162;
	private static final int EXALTED_SHOOTER = 81163;
	private static final int EXALTED_BUSTER = 81164;
	private static final int EXALTED_CASTER = 81165;
	// private static final int EXALTED_RETIBUTER = 81166;
	private static final int EXALTED_DUAL_SWORDS = 81167;
	private static final int EXALTED_DUAL_DAGGERS = 81168;
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
	
	public Q10811_ExaltedOneWhoFacesTheLimit()
	{
		super(10811);
		addStartNpc(LIONEL);
		addTalkId(LIONEL);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "33907-07.html");
		registerQuestItems(LIONEL_HUNTER_MISSING_LIST, SLAYERS_PROOF);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final Race race = player.getRace();
		final ClassId classId = player.getClassId();
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33907-03.html":
			case "33907-04.html":
			{
				htmltext = event;
				break;
			}
			case "movieStart":
			{
				qs.startQuest();
				playMovie(player, Movie.SC_HONORS);
				break;
			}
			case "33907-05.html":
			{
				qs.setCond(2);
				giveItems(player, LIONEL_HUNTER_MISSING_LIST, 1);
				htmltext = event;
				break;
			}
			case "33907-09.html":
			{
				if (qs.isCond(3))
				{
					giveItems(player, SPELLBOOK_DIGNITY_OF_THE_EXALTED, 1);
					switch (race)
					{
						case HUMAN:
						case ELF:
						case DARK_ELF:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId.getId()) || (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_WYNN_GROUP, classId.getId())))
							{
								giveItems(player, EXALTED_BUSTER, 1);
								giveItems(player, EXATLED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_MAGIC, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_EOLH_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_CASTER, 1);
								giveItems(player, EXATLED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_MAGIC, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_DUAL_DAGGERS, 1);
								giveItems(player, EXATLED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_THROWER, 1);
								giveItems(player, EXATLED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId.getId()) || (player.getClassId() == ClassId.TYRR_DUELIST))
							{
								giveItems(player, EXALTED_DUAL_SWORDS, 1);
								giveItems(player, EXATLED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_DREADNOUGHT)
							{
								giveItems(player, EXALTED_STROMER, 1);
								giveItems(player, EXATLED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_SIGEL_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_CUTTER, 1);
								giveItems(player, EXATLED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							break;
						}
						case DWARF:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_DUAL_DAGGERS, 1);
								giveItems(player, EXATLED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else
							{
								giveItems(player, EXALTED_AVENGER, 1);
								giveItems(player, EXATLED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							break;
						}
						case ORC:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_DUAL_SWORDS, 1);
								giveItems(player, EXATLED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_GRAND_KHAVATARI)
							{
								giveItems(player, EXALTED_FIGHTER, 1);
								giveItems(player, EXATLED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_TITAN)
							{
								giveItems(player, EXALTED_SLASHER, 1);
								giveItems(player, EXATLED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							break;
						}
						case KAMAEL:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_BUSTER, 1);
								giveItems(player, EXATLED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_MAGIC, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_SHOOTER, 1);
								giveItems(player, EXATLED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else
							{
								giveItems(player, EXATLED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
								break;
							}
							break;
						}
						case ERTHEIA:
						{
							if (player.isMageClass())
							{
								giveItems(player, EXATLED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_MAGIC, 1);
							}
							else
							{
								giveItems(player, EXALTED_SLASHER, 1);
								giveItems(player, EXATLED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
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
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() >= MIN_LEVEL) && (player.getNobleLevel() > 0) ? "33907-01.htm" : "33907-07.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33907-02.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "33907-06.html";
				}
				else if (qs.isCond(3))
				{
					htmltext = "33907-08.html";
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
		if ((qs != null) && qs.isCond(2) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			if (getQuestItemsCount(player, SLAYERS_PROOF) < SLAYERS_PROOF_NEEDED)
			{
				giveItemRandomly(player, SLAYERS_PROOF, 1, SLAYERS_PROOF_NEEDED, 1, true);
			}
			if ((getQuestItemsCount(player, SLAYERS_PROOF) >= SLAYERS_PROOF_NEEDED) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
			{
				qs.setCond(3, true);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_ABILITY_POINTS_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	private void OnPlayerAbilityPointsChanged(OnPlayerAbilityPointsChanged event)
	{
		notifyEvent("SUBQUEST_FINISHED_NOTIFY", null, event.getPlayer());
	}
}