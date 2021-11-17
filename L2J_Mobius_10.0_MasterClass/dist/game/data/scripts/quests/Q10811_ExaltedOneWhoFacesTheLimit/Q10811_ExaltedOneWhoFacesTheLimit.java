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

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

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
	private static final int REACH_LV_101 = NpcStringId.REACH_LV_101.getId();
	private static final int SLAYERS_PROOF_NEEDED = 40000;
	// Reward
	private static final int SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV1 = 45922;
	private static final int EXALTED_HEAVY_ARMOR_PACK = 81203;
	private static final int EXALTED_LIGHT_ARMOR_PACK = 81204;
	private static final int EXALTED_ROBE_PACK = 81205;
	private static final int FIRST_EXALTED_QUEST_REWARD_PHYSICAL = 81207;
	private static final int FIRST_EXALTED_QUEST_REWARD_MAGIC = 81208;
	private static final int EXALTED_CUTTER = 81157;
	private static final int EXALTED_SLASHER = 81158;
	private static final int EXALTED_AVENGER = 81159;
	private static final int EXALTED_FIGHTER = 81160;
	private static final int EXALTED_STORMER = 81161;
	private static final int EXALTED_THROWER = 81162;
	private static final int EXALTED_SHOOTER = 81163;
	private static final int EXALTED_BUSTER = 81164;
	private static final int EXALTED_CASTER = 81165;
	private static final int EXALTED_RETRIBUTER = 81166;
	private static final int EXALTED_DUAL_SWORDS = 81167;
	private static final int EXALTED_DUAL_DAGGERS = 81168;
	// Monsters
	private static final int[] MONSTERS =
	{
		// Blazing Swamp
		23487, // Magma Ailith
		23488, // Magma Apophis
		23491, // Lava Wendigo
		23494, // Magma Salamander
		23495, // Magma Dre Vanul
		23496, // Magma Ifrit
		23500, // Flame Crow
		23501, // Flame Rael
		23502, // Flame Salamander
		23503, // Flame Drake
		23504, // Flame Votis
		// War-Torn
		24585, // Vanor Silenos Mercenary
		24586, // Vanor Silenos Guardian
		// Abandoned Coal Mines
		24577, // Black Hammer Artisan
		24578, // Black Hammer Collector
		24579, // Black Hammer Protector
		// Beleth's Magic Circle
		23361, // Mutated Fly
		23360, // Bizuard
		23357, // Disorder Warrior
		23356, // Klein Soldier
		23355, // Armor Beast
		23354, // Decay Hannibal
		// Desert Quarry
		23811, // Cantera Tanya
		23812, // Cantera Deathmoz
		23813, // Cantera Floxis
		23814, // Cantera Belika
		23815, // Cantera Bridget
		// Phantasmal Ridge
		24511, // Lunatikan
		24515, // Kandiloth
		24512, // Garion Neti
		24513, // Desert Wendigo
		24514, // Koraza
		// Enchanted Valley
		23581, // Apherus
		23568, // Nymph Lily
		23569, // Nymph Lily
		23570, // Nymph Tulip
		23571, // Nymph Tulip
		23572, // Nymph Cosmos
		23573, // Nymph Cosmos
		19600, // Flower Bud
		23566, // Nymph Rose
		23567, // Nymph Rose
		23578, // Nymph Guardian
		// Ivory Tower
		24422, // Stone Golem
		24425, // Steel Golem
		24421, // Stone Gargoyle
		24424, // Gargoyle Hunter
		24426, // Stone Cube
		24423, // Monster Eye
		// Silent Valley
		24506, // Silence Witch
		24508, // Silence Warrior
		24510, // Silence Hannibal
		24509, // Silence Slave
		24507, // Silence Preacle
		// Alligator Island
		24377, // Swamp Tribe
		24378, // Swamp Alligator
		24379, // Swamp Warrior
		24373, // Dailaon Lad
		24376, // Nos Lad
		// Tanor Canyon
		20941, // Tanor Silenos Chieftain
		20939, // Tanor Silenos Warrior
		20937, // Tanor Silenos Soldier
		20942, // Nightmare Guide
		20938, // Tanor Silenos Scout
		20943, // Nightmare Watchman
		24587, // Tanor Silenos
		// The Forest of Mirrors
		24466, // Demonic Mirror
		24465, // Forest Evil Spirit
		24461, // Forest Ghost
		24464, // Bewildered Dwarf Adventurer
		24463, // Bewildered Patrol
		24462, // Bewildered Expedition Member
		// Field of Silence
		24523, // Krotany
		24520, // Krotania
		24521, // Krophy
		24522, // Spiz Krophy
		// Isle of Prayer
		24451, // Lizardman Defender
		24449, // Lizardman Warrior
		24448, // Lizardman Archer
		24450, // Lizardmen Wizard
		24447, // Niasis
		24445, // Lizardman Rogue
		24446, // Island Guard
		// Ketra Orc Outpost
		24631, // Ketra Orc Shaman
		24632, // Ketra Orc Prophet
		24633, // Ketra Orc Warrior
		24634, // Ketra Orc Lieutenant
		24635, // Ketra Orc Battalion Commander
		// Varka Silenos Barracks
		24636, // Varka Silenos Magus
		24637, // Varka Silenos Shaman
		24638, // Varka Silenos Footman
		24639, // Varka Silenos Sergeant
		24640, // Varka Silenos Officer
		// Brekas Stronghold
		24420, // Breka Orc Prefect
		24416, // Breka Orc Scout Captain
		24419, // Breka Orc Slaughterer
		24415, // Breka Orc Scout
		24417, // Breka Orc Archer
		24418, // Breka Orc Shaman
		// Sel Mahum Training Grounds
		24492, // Sel Mahum Soldier
		24494, // Sel Mahum Warrior
		24493, // Sel Mahum Squad Leader
		24495, // Keltron
		// Plains of the Lizardmen
		24496, // Tanta Lizardman Warrior
		24498, // Tanta Lizardman Wizard
		24499, // Priest Ugoros
		24497, // Tanta Lizardman Archer
		// Fields of Massacre
		24486, // Dismal Pole
		24487, // Graveyard Predator
		24489, // Doom Scout
		24491, // Doom Knight
		24490, // Doom Soldier
		24488, // Doom Archer
		// Wall of Argos
		24606, // Captive Antelope
		24607, // Captive Bandersnatch
		24608, // Captive Buffalo
		24609, // Captive Grendel
		24610, // Eye of Watchman
		24611, // Elder Homunculus
		// Sea Of Spores
		24226, // Aranea
		24227, // Keros
		24228, // Falena
		24229, // Atrofa
		24230, // Nuba
		24231, // Torfedo
		24234, // Lesatanas
		24235, // Arbor
		24236, // Tergus
		24237, // Skeletus
		24238, // Atrofine
		// Wasteland
		24501, // Centaur Fighter
		24504, // Centaur Warlord
		24505, // Earth Elemental Lord
		24503, // Centaur Wizard
		24500, // Sand Golem
		24502, // Centaur Marksman
		// Cemetery
		19455, // Aden Raider
		19456, // Te Ochdumann
		19457, // Travis
		20668, // Grave Guard
		23290, // Royal Knight
		23291, // Personal Magician
		23292, // Royal Guard
		23293, // Royal Guard Captain
		23294, // Chief Magician
		23295, // Operations Manager
		23296, // Chief Quartermaster
		23297, // Escort
		23298, // Royal Quartermaster
		23299, // Operations Chief of the 7th Division
		23300, // Commander of Operations
		// Dragon Valley
		24480, // Dragon Legionnaire
		24482, // Dragon Officer
		24481, // Dragon Peltast
		24483, // Dragon Centurion
		24484, // Dragon Elite Guard
		24485, // Behemoth Dragon
		// Fafurion Temple
		24329, // Starving Water Dragon
		24318, // Temple Guard Captain
		24325, // Temple Wizard
		24324, // Temple Guardian Warrior
		24326, // Temple Guardian Wizard
		24323, // Temple Guard
		24321, // Temple Patrol Guard
		24322, // Temple Knight Recruit
		// Neutral Zone
		24641, // Tel Mahum Wizard
		24642, // Tel Mahum Legionary
		24643, // Tel Mahum Footman
		24644, // Tel Mahum Lieutenant
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
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final Race race = player.getRace();
		final ClassId classId = player.getBaseTemplate().getClassId();
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
					giveItems(player, SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV1, 1);
					switch (race)
					{
						case HUMAN:
						case ELF:
						case DARK_ELF:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_BUSTER, 1);
								giveItems(player, EXALTED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_MAGIC, 1);
							}
							else if ((CategoryData.getInstance().isInCategory(CategoryType.SIXTH_WYNN_GROUP, classId.getId())))
							{
								giveItems(player, EXALTED_RETRIBUTER, 1);
								giveItems(player, EXALTED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_EOLH_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_CASTER, 1);
								giveItems(player, EXALTED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_MAGIC, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_DUAL_DAGGERS, 1);
								giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_THROWER, 1);
								giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId.getId()) || (player.getClassId() == ClassId.TYRR_DUELIST))
							{
								giveItems(player, EXALTED_DUAL_SWORDS, 1);
								giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_DREADNOUGHT)
							{
								giveItems(player, EXALTED_STORMER, 1);
								giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_SIGEL_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_CUTTER, 1);
								giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							break;
						}
						case DWARF:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_DUAL_DAGGERS, 1);
								giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else
							{
								giveItems(player, EXALTED_AVENGER, 1);
								giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							break;
						}
						case ORC:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_DUAL_SWORDS, 1);
								giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_GRAND_KHAVATARI)
							{
								giveItems(player, EXALTED_FIGHTER, 1);
								giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else if (player.getClassId() == ClassId.TYRR_TITAN)
							{
								giveItems(player, EXALTED_SLASHER, 1);
								giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							break;
						}
						case KAMAEL:
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_BUSTER, 1);
								giveItems(player, EXALTED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_MAGIC, 1);
							}
							else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()))
							{
								giveItems(player, EXALTED_SHOOTER, 1);
								giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
							}
							else
							{
								giveItems(player, EXALTED_SLASHER, 1);
								giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_PHYSICAL, 1);
								break;
							}
							break;
						}
						case ERTHEIA:
						{
							if (player.isMageClass())
							{
								giveItems(player, EXALTED_RETRIBUTER, 1);
								giveItems(player, EXALTED_ROBE_PACK, 1);
								giveItems(player, FIRST_EXALTED_QUEST_REWARD_MAGIC, 1);
							}
							else
							{
								giveItems(player, EXALTED_FIGHTER, 1);
								giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
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
	public String onTalk(Npc npc, Player player)
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
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
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
			sendNpcLogList(player);
		}
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			if (player.getLevel() >= MIN_COMPLETE_LEVEL)
			{
				holder.add(new NpcLogListHolder(REACH_LV_101, true, 1));
			}
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
