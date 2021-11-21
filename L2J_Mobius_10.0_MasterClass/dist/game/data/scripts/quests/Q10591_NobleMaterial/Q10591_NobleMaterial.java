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
package quests.Q10591_NobleMaterial;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.util.Util;

import quests.Q10590_ReawakenedFate.Q10590_ReawakenedFate;

/**
 * Noble Material (10591)
 * @author NightBR, Mobius, NasSeKa
 */
public class Q10591_NobleMaterial extends Quest
{
	// NPCs
	private static final int JOACHIM = 34513;
	private static final int HARP_ZU_HESTUI = 34014;
	private static final int EVAN_GRAHAM = 34523;
	private static final int HERPA = 34362;
	private static final int LIONEL_HUNTER = 33907;
	private static final int[] MONSTERS =
	{
		23487, // Magma Ailith
		23489, // Lava Wyrm
		23490, // Lava Drake
		23491, // Lava Wendigo
		23492, // Lavastone Golem
		23493, // Lava Leviah
		23494, // Magma Salamander
		23495, // Magma Dre Vanul
		23499, // Flame Preta
		23500, // Flame Crow
		23501, // Flame Rael
		23502, // Flame Salamander
		23503, // Flame Drake
		23504, // Flame Votis
		24585, // Vanor Silenos Mercenary
		24586, // Vanor Silenos Guardian
	};
	// Items
	private static final int FLAME_ENERGY = 80856; // Flame Energy - monster drop
	private static final ItemHolder TELEPORT_CUBE = new ItemHolder(81875, 1);
	// Rewards
	private static final int ADENA_AMOUNT = 5050;
	private static final int ACHIEVEMENT_BOX_LV_100 = 80910;
	private static final int WARRIOR_CICLET_BOX_LV5 = 80911;
	private static final int WIZARD_CICLET_BOX_LV5 = 80912;
	private static final int KNIGHT_CICLET_BOX_LV5 = 80913;
	private static final int EXALTED_HEAVY_ARMOR_PACK = 81203;
	private static final int EXALTED_LIGHT_ARMOR_PACK = 81204;
	private static final int EXALTED_ROBE_PACK = 81205;
	private static final int EXALTED_SHIELD = 81186;
	private static final int EXALTED_SIGIL = 81197;
	private static final int COMMON_EXALTED_QUEST_REWARD_PHYSICAL = 81207;
	private static final int COMMON_EXALTED_QUEST_REWARD_MAGIC = 81208;
	private static final int SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL = 81209;
	private static final int SPECIAL_EXALTED_QUEST_REWARD_MAGIC = 81210;
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
	// Misc
	private static final int MIN_LEVEL = 100;
	// Location
	private static final Location BURNING_MARSH = new Location(152754, -15142, -4400);
	private static final Location WAR_PLAINS = new Location(159620, 21075, -3688);
	private static final Location HEIN = new Location(111257, 221071, -3550);
	
	public Q10591_NobleMaterial()
	{
		super(10591);
		addStartNpc(JOACHIM);
		addTalkId(JOACHIM, HARP_ZU_HESTUI, EVAN_GRAHAM, HERPA, LIONEL_HUNTER);
		addKillId(MONSTERS);
		registerQuestItems(FLAME_ENERGY);
		addCondMinLevel(99, "34513-16.html");
		addCondCompletedQuest(Q10590_ReawakenedFate.class.getSimpleName(), "34513-16.html");
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
			case "34513-02.htm":
			case "34513-07.html":
			case "34513-04.htm":
			case "34014-03.html":
			case "34523-03.html":
			case "34362-03.html":
			case "33907-03.html":
			{
				htmltext = event;
				break;
			}
			case "34513-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "teleportWP":
			{
				if (qs.isCond(1) || qs.isCond(2) || qs.isCond(4) || qs.isCond(5) || qs.isCond(6) || qs.isCond(7))
				{
					giveStoryBuffReward(npc, player);
					player.teleToLocation(WAR_PLAINS);
				}
				break;
			}
			case "teleportBS":
			{
				if (qs.isCond(1) || qs.isCond(2) || qs.isCond(4) || qs.isCond(5) || qs.isCond(6) || qs.isCond(7))
				{
					giveStoryBuffReward(npc, player);
					player.teleToLocation(BURNING_MARSH);
				}
				break;
			}
			case "34014-02.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34523-02.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34513-06.html":
			{
				qs.setCond(4, true);
				htmltext = event;
				break;
			}
			case "34362-02.html":
			{
				qs.setCond(5, true);
				htmltext = event;
				break;
			}
			case "teleport_s":
			{
				if (qs.isCond(5))
				{
					player.teleToLocation(HEIN);
				}
				break;
			}
			case "33907-02.html":
			{
				qs.setCond(6, true);
				htmltext = event;
				break;
			}
			case "33907-05.html":
			{
				if (qs.isCond(7) && (player.getLevel() >= MIN_LEVEL))
				{
					// Reward #1
					giveItems(player, WARRIOR_CICLET_BOX_LV5, 1);
					basicRewards(player);
					player.setNobleLevel(1);
					player.broadcastInfo();
					showOnScreenMsg(player, NpcStringId.CONGRATULATIONS_YOU_ARE_NOW_A_NOBLESSE, ExShowScreenMessage.TOP_CENTER, 10000);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "33907-06.html":
			{
				if (qs.isCond(7) && (player.getLevel() >= MIN_LEVEL))
				{
					// Reward #2
					giveItems(player, WIZARD_CICLET_BOX_LV5, 1);
					basicRewards(player);
					player.setNobleLevel(1);
					player.broadcastInfo();
					showOnScreenMsg(player, NpcStringId.CONGRATULATIONS_YOU_ARE_NOW_A_NOBLESSE, ExShowScreenMessage.TOP_CENTER, 10000);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "33907-07.html":
			{
				if (qs.isCond(7) && (player.getLevel() >= MIN_LEVEL))
				{
					// Reward #3
					giveItems(player, KNIGHT_CICLET_BOX_LV5, 1);
					basicRewards(player);
					player.setNobleLevel(1);
					player.broadcastInfo();
					showOnScreenMsg(player, NpcStringId.CONGRATULATIONS_YOU_ARE_NOW_A_NOBLESSE, ExShowScreenMessage.TOP_CENTER, 10000);
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
		final ClassId classId = player.getBaseTemplate().getClassId();
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == JOACHIM)
				{
					htmltext = "34513-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case JOACHIM:
					{
						if ((getQuestItemsCount(player, FLAME_ENERGY) >= 1000) && (player.getLevel() >= MIN_LEVEL))
						{
							qs.setCond(3, true);
						}
						
						if (qs.isCond(1))
						{
							if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.SIXTH_WYNN_GROUP, classId.getId()))
							{
								htmltext = "34513-04b.htm";
							}
							else
							{
								htmltext = "34513-04.htm";
							}
						}
						else if (qs.isCond(2))
						{
							htmltext = "34513-15.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34513-05.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "34513-07.html";
						}
						break;
					}
					case HARP_ZU_HESTUI:
					{
						if (qs.isCond(1))
						{
							htmltext = "34014-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34014-03.html";
						}
						break;
					}
					case EVAN_GRAHAM:
					{
						if (qs.isCond(1))
						{
							htmltext = "34523-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34523-03.html";
						}
						break;
					}
					case HERPA:
					{
						if (qs.isCond(4))
						{
							htmltext = "34362-01.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "34362-03.html";
						}
						break;
					}
					case LIONEL_HUNTER:
					{
						if (qs.isCond(5))
						{
							htmltext = "33907-01.html";
						}
						else if (qs.isCond(6))
						{
							htmltext = "33907-03.html";
						}
						else if (qs.isCond(7))
						{
							htmltext = "33907-04.html";
						}
						break;
					}
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
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, player, false))
		{
			if (getQuestItemsCount(player, FLAME_ENERGY) < 1000)
			{
				giveItems(player, FLAME_ENERGY, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if ((getQuestItemsCount(player, FLAME_ENERGY) >= 1000) && (player.getLevel() >= MIN_LEVEL))
			{
				qs.setCond(3, true);
			}
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
	
	public void basicRewards(Player player)
	{
		final Race race = player.getRace();
		final ClassId classId = player.getBaseTemplate().getClassId();
		
		giveAdena(player, ADENA_AMOUNT, false);
		giveItems(player, ACHIEVEMENT_BOX_LV_100, 1);
		takeItem(player, TELEPORT_CUBE);
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
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_MAGIC, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_MAGIC, 1);
				}
				else if ((CategoryData.getInstance().isInCategory(CategoryType.SIXTH_WYNN_GROUP, classId.getId())))
				{
					giveItems(player, EXALTED_RETRIBUTER, 1);
					giveItems(player, EXALTED_ROBE_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_EOLH_GROUP, classId.getId()))
				{
					giveItems(player, EXALTED_CASTER, 1);
					giveItems(player, EXALTED_ROBE_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_MAGIC, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_MAGIC, 1);
				}
				else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId.getId()))
				{
					giveItems(player, EXALTED_DUAL_DAGGERS, 1);
					giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()))
				{
					giveItems(player, EXALTED_THROWER, 1);
					giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId.getId()) || (player.getClassId() == ClassId.TYRR_DUELIST))
				{
					giveItems(player, EXALTED_DUAL_SWORDS, 1);
					giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else if (player.getClassId() == ClassId.TYRR_DREADNOUGHT)
				{
					giveItems(player, EXALTED_STORMER, 1);
					giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_SIGEL_GROUP, classId.getId()))
				{
					giveItems(player, EXALTED_CUTTER, 1);
					giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
					giveItems(player, player.getClassId() == ClassId.SIGEL_DEATH_KNIGHT ? EXALTED_SIGIL : EXALTED_SHIELD, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				break;
			}
			case DWARF:
			{
				if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId.getId()))
				{
					giveItems(player, EXALTED_DUAL_DAGGERS, 1);
					giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else
				{
					giveItems(player, EXALTED_AVENGER, 1);
					giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SHIELD, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				break;
			}
			case ORC:
			{
				if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId.getId()))
				{
					giveItems(player, player.getClassId() == ClassId.ISS_DOMINATOR ? EXALTED_CUTTER : EXALTED_DUAL_SWORDS, 1);
					giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
					giveItems(player, player.getClassId() == ClassId.ISS_DOMINATOR ? EXALTED_SHIELD : EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else if (player.getClassId() == ClassId.TYRR_GRAND_KHAVATARI)
				{
					giveItems(player, EXALTED_FIGHTER, 1);
					giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else if (player.getClassId() == ClassId.TYRR_TITAN)
				{
					giveItems(player, EXALTED_SLASHER, 1);
					giveItems(player, EXALTED_HEAVY_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				break;
			}
			case KAMAEL:
			{
				if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId.getId()))
				{
					giveItems(player, EXALTED_BUSTER, 1);
					giveItems(player, EXALTED_ROBE_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_MAGIC, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_MAGIC, 1);
				}
				else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId.getId()))
				{
					giveItems(player, EXALTED_SHOOTER, 1);
					giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				else
				{
					giveItems(player, EXALTED_SLASHER, 1);
					giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
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
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_MAGIC, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_MAGIC, 1);
				}
				else
				{
					giveItems(player, EXALTED_FIGHTER, 1);
					giveItems(player, EXALTED_LIGHT_ARMOR_PACK, 1);
					giveItems(player, EXALTED_SIGIL, 1);
					giveItems(player, COMMON_EXALTED_QUEST_REWARD_PHYSICAL, 1);
					giveItems(player, SPECIAL_EXALTED_QUEST_REWARD_PHYSICAL, 1);
				}
				break;
			}
		}
	}
}
