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
package quests.Q11025_PathOfDestinyProving;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerProfessionChange;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.ExTutorialShowId;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

import quests.Q11024_PathOfDestinyBeginning.Q11024_PathOfDestinyBeginning;

/**
 * Path of Destiny - Proving (11025)
 * @URL https://l2wiki.com/Path_of_Destiny_-_Proving
 * @author Liviades
 */
public class Q11025_PathOfDestinyProving extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int RAYMOND = 30289;
	private static final int TELESHA = 33981;
	private static final int KALLESIN = 33177;
	private static final int ZENATH = 33509;
	private static final int MYSTERIOUS_WIZARD = 1033980;
	// Monsters
	private static final int VAMPIRE = 24385;
	private static final int CARCASS_BAT = 24384;
	private static final int SKELETON_WARRIOR = 24388;
	private static final int SKELETON_SCOUT = 24386;
	private static final int SKELETON_ARCHER = 24387;
	private static final int SPARTOI_SOLDIER = 24389;
	private static final int RAGING_SPARTOI = 24390;
	private static final int SKELETON_WARRIOR_2 = 27528;
	private static final int SKELETON_SCOUT_2 = 27529;
	// Quest Item
	private static final int WIND_SPIRIT_REALM_RELIC = 39535;
	private static final int SECRET_MATERIAL = 80671;
	private static final int BREATH_OF_DEATH = 80672;
	// Class change rewards
	private static final int SS_R = 33780;
	private static final int BSS_R = 33794;
	private static final int BOX_R_HEAVY = 46924;
	private static final int BOX_R_LIGHT = 46925;
	private static final int BOX_R_ROBE = 46926;
	private static final int WEAPON_SWORD_R = 47008;
	private static final int WEAPON_SHIELD_R = 47026;
	private static final int WEAPON_GSWORD_R = 47009;
	private static final int WEAPON_BLUNT_R = 47010;
	private static final int WEAPON_FIST_R = 47011;
	private static final int WEAPON_SPEAR_R = 47012;
	private static final int WEAPON_BOW_R = 47013;
	private static final int WEAPON_DUALDAGGER_R = 47019;
	private static final int WEAPON_STAFF_R = 47017;
	private static final int WEAPON_DUALSWORD_R = 47018;
	private static final int WEAPON_CROSSBOW_R = 47014;
	private static final int WEAPON_BUSTER_R = 47015;
	private static final int WEAPON_CASTER_R = 47016;
	private static final int WEAPON_SIGIL_R = 47037;
	private static final int ORICHALCUM_BOLT_R = 19443;
	private static final int ORICHALCUM_ARROW_R = 18550;
	// Locations
	private static final Location TRAINING_GROUNDS_TELEPORT1 = new Location(-43688, 117592, -3560);
	private static final Location TRAINING_GROUNDS_TELEPORT2 = new Location(-46450, 110273, -3808);
	private static final Location TRAINING_GROUNDS_TELEPORT3 = new Location(-51637, 108721, -3720);
	private static final Location TRAINING_GROUNDS_TELEPORT4 = new Location(-4983, 116607, -3344);
	private static final Location TRAINING_GROUNDS_TELEPORT5 = new Location(-12877, 121710, -2960);
	// Misc
	private static final String R_GRADE_ITEMS_REWARDED_VAR = "R_GRADE_ITEMS_REWARDED";
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final String KILL_COUNT_VAR2 = "KillCount2";
	private static final String REWARD_CHECK_VAR1 = "Q11025_REWARD_1";
	private static final String REWARD_CHECK_VAR2 = "Q11025_REWARD_2";
	private static final String REWARD_CHECK_VAR3 = "Q11025_REWARD_3";
	private static final int LEVEL_20 = 20;
	private static final int LEVEL_40 = 40;
	private static boolean INSTANT_LEVEL_40 = false;
	
	public Q11025_PathOfDestinyProving()
	{
		super(11025);
		addStartNpc(TARTI);
		addFirstTalkId(TELESHA, MYSTERIOUS_WIZARD);
		addTalkId(TARTI, RAYMOND, TELESHA, MYSTERIOUS_WIZARD, KALLESIN, ZENATH);
		addKillId(VAMPIRE, CARCASS_BAT, SKELETON_SCOUT, SKELETON_ARCHER, SKELETON_WARRIOR, SPARTOI_SOLDIER, RAGING_SPARTOI, SKELETON_WARRIOR_2, SKELETON_SCOUT_2);
		registerQuestItems(WIND_SPIRIT_REALM_RELIC, SECRET_MATERIAL, BREATH_OF_DEATH);
		// addCondMinLevel(LEVEL_20, "34505-16.html");
		addCondCompletedQuest(Q11024_PathOfDestinyBeginning.class.getSimpleName(), "34505-16.html");
		setQuestNameNpcStringId(NpcStringId.LV_20_PATH_OF_DESTINY_PROVING);
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
			case "34505-02.htm":
			case "34505-03.htm":
			case "33177-03.html":
			case "33177-04.html":
			case "33509-03.html":
			case "33509-04.html":
			case "30289-02.html":
			case "33980-02.html":
			case "30289-05.html":
			case "34505-10.html":
			case "34505-11.html":
			case "34505-12.html":
			case "34505-13.html":
			case "34505-14.html":
			{
				htmltext = event;
				break;
			}
			case "34505-04.htm":
			{
				htmltext = event;
				player.sendPacket(new ExTutorialShowId(15)); // Skill
				showOnScreenMsg(player, NpcStringId.PRESS_ALT_K_TO_OPEN_THE_LEARN_SKILL_TAB_AND_LEARN_NEW_SKILLS_THE_SKILLS_IN_THE_ACTIVE_TAB_CAN_BE_ADDED_TO_THE_SHORTCUTS, ExShowScreenMessage.TOP_CENTER, 10000);
				break;
			}
			case "34505-05.html":
			{
				qs.startQuest();
				qs.setCond(1, true);
				giveStoryBuffReward(npc, player);
				htmltext = event;
				break;
			}
			case "33177-02.html":
			{
				if (qs.isCond(2))
				{
					takeItems(player, SECRET_MATERIAL, 15);
					qs.setCond(3, true);
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR1, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR1, true);
						addExpAndSp(player, 1640083, 1476);
					}
					htmltext = event;
				}
				break;
			}
			case "33177-05.html":
			{
				if (qs.isCond(3))
				{
					qs.setCond(4, true);
					giveStoryBuffReward(npc, player);
					player.sendPacket(new ExTutorialShowId(15)); // Auto-use Next Target //TODO: ADD PROPER ID
					htmltext = event;
				}
				break;
			}
			case "33509-02.html":
			{
				if (qs.isCond(5))
				{
					qs.setCond(6, true);
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR2, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR2, true);
						addExpAndSp(player, 913551, 822);
					}
					htmltext = event;
				}
				break;
			}
			case "33509-05.html":
			{
				if (qs.isCond(6))
				{
					qs.setCond(7, true);
					giveStoryBuffReward(npc, player);
					player.sendPacket(new ExTutorialShowId(15)); // Auto-use Potions //TODO: ADD PROPER ID
					htmltext = event;
				}
				break;
			}
			case "34505-07.html":
			{
				if (qs.isCond(8))
				{
					takeItems(player, BREATH_OF_DEATH, 15);
					qs.setCond(9, true);
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR3, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR3, true);
						if (INSTANT_LEVEL_40 && (player.getLevel() < LEVEL_40))
						{
							addExpAndSp(player, (ExperienceData.getInstance().getExpForLevel(LEVEL_40) + 100) - player.getExp(), 4457);
						}
						else
						{
							addExpAndSp(player, 4952686, 4457);
						}
						giveAdena(player, 165000, true);
					}
					htmltext = event;
					showOnScreenMsg(player, NpcStringId.SECOND_CLASS_TRANSFER_IS_AVAILABLE_GO_SEE_TARTI_IN_THE_TOWN_OF_GLUDIO_TO_START_THE_CLASS_TRANSFER, ExShowScreenMessage.TOP_CENTER, 10000);
				}
				break;
			}
			case "34505-08.html":
			{
				if (qs.isCond(9))
				{
					if (qs.isCond(9) && (player.getLevel() >= LEVEL_40))
					{
						qs.setCond(11, true);
						htmltext = event;
						break;
					}
					qs.setCond(10, true);
					htmltext = event;
				}
				break;
			}
			case "30289-03.html":
			{
				if (qs.isCond(11))
				{
					qs.setCond(12, true);
					giveItems(player, WIND_SPIRIT_REALM_RELIC, 1);
					htmltext = event;
				}
				break;
			}
			case "30289-06.html":
			{
				if (qs.isCond(14))
				{
					qs.setCond(15, true);
					htmltext = event;
				}
				break;
			}
			case "34505-15.html":
			{
				if (qs.isCond(15))
				{
					takeItems(player, WIND_SPIRIT_REALM_RELIC, 2);
					qs.exitQuest(false, true);
					if (CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()))
					{
						showOnScreenMsg(player, NpcStringId.CLASS_TRANSFER_IS_AVAILABLE_CLICK_THE_CLASS_TRANSFER_ICON_IN_THE_NOTIFICATION_WINDOW_TO_TRANSFER_YOUR_CLASS, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
					}
					htmltext = event;
				}
				break;
			}
			case "teleport1":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT1);
				}
				break;
			}
			case "teleport2":
			{
				if (qs.isCond(4))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT2);
				}
				break;
			}
			case "teleport3":
			{
				if (qs.isCond(7))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT3);
				}
				break;
			}
			case "teleport4":
			{
				if (qs.isCond(12))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT4);
				}
				break;
			}
			case "MageSpawn":
			{
				if (qs.isCond(13) && (npc != null))
				{
					addSpawn(MYSTERIOUS_WIZARD, npc, true, 300000);
					showOnScreenMsg(player, NpcStringId.TALK_TO_THE_MYSTERIOUS_WIZARD, ExShowScreenMessage.TOP_CENTER, 10000);
					npc.deleteMe();
				}
				break;
			}
			case "BacktoRaymond":
			{
				if (qs.isCond(13) && (npc != null))
				{
					qs.setCond(14, true);
					showOnScreenMsg(player, NpcStringId.RETURN_TO_HIGH_PRIEST_RAYMOND_IN_GLUDIO, ExShowScreenMessage.TOP_CENTER, 10000);
					giveItems(player, WIND_SPIRIT_REALM_RELIC, 1);
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT5);
					npc.deleteMe();
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
				if ((npc.getId() == TARTI) && (player.getLevel() >= LEVEL_20))
				{
					htmltext = "34505-01.htm";
				}
				else
				{
					htmltext = "34505-16.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case TARTI:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "34505-01.htm";
								break;
							}
							case 8:
							{
								htmltext = "34505-06.html";
								break;
							}
							case 9:
							{
								htmltext = "34505-07.html";
								break;
							}
							case 10:
							{
								if (player.getLevel() >= 40)
								{
									qs.setCond(11, true);
									htmltext = "34505-08.html";
								}
								else
								{
									htmltext = "34505-19.html";
								}
								break;
							}
							case 11:
							{
								htmltext = "34505-18.html";
								break;
							}
							case 15:
							{
								htmltext = "34505-09.html";
								break;
							}
						}
						break;
					}
					case KALLESIN:
					{
						if (qs.isCond(2))
						{
							htmltext = "33177-01.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "33177-02.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "33177-06.html";
						}
						break;
					}
					case ZENATH:
					{
						if (qs.isCond(5))
						{
							htmltext = "33509-01.html";
						}
						else if (qs.isCond(6))
						{
							htmltext = "33509-03.html";
						}
						else if (qs.isCond(7))
						{
							htmltext = "33509-06.html";
						}
						break;
					}
					case RAYMOND:
					{
						switch (qs.getCond())
						{
							case 11:
							{
								htmltext = "30289-01.html";
								break;
							}
							case 12:
							{
								htmltext = "30289-07.html";
								break;
							}
							case 14:
							{
								htmltext = "30289-04.html";
								break;
							}
							case 15:
							{
								htmltext = "30289-08.html";
								break;
							}
						}
						break;
					}
					case MYSTERIOUS_WIZARD:
					{
						if (qs.isCond(14))
						{
							htmltext = "33980-03.html";
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
	public String onFirstTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(12))
		{
			qs.setCond(13, true);
		}
		return npc.getId() + "-01.html";
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, true);
		if (qs != null)
		{
			switch (npc.getId())
			{
				case CARCASS_BAT:
				case VAMPIRE:
				{
					if (qs.isCond(1))
					{
						if (getQuestItemsCount(killer, SECRET_MATERIAL) < 14)
						{
							giveItems(killer, SECRET_MATERIAL, 1);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
						else if (getQuestItemsCount(killer, SECRET_MATERIAL) == 14)
						{
							giveItems(killer, SECRET_MATERIAL, 1);
							qs.setCond(2, true);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_KALLESIN_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case SKELETON_SCOUT:
				case SKELETON_ARCHER:
				case SKELETON_WARRIOR:
				{
					if (qs.isCond(4))
					{
						final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
						if (killCount < 30)
						{
							qs.set(KILL_COUNT_VAR, killCount);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
							sendNpcLogList(killer);
						}
						else
						{
							qs.setCond(5, true);
							qs.unset(KILL_COUNT_VAR);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_ZENATH_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case SPARTOI_SOLDIER:
				case RAGING_SPARTOI:
				{
					if (qs.isCond(7))
					{
						if (getQuestItemsCount(killer, BREATH_OF_DEATH) < 14)
						{
							giveItems(killer, BREATH_OF_DEATH, 1);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
						else if (getQuestItemsCount(killer, BREATH_OF_DEATH) == 14)
						{
							qs.setCond(8, true);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_TARTI_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case SKELETON_WARRIOR_2:
				case SKELETON_SCOUT_2:
				{
					if (qs.isCond(12))
					{
						final int killCount = qs.getInt(KILL_COUNT_VAR2) + 1;
						if (killCount < 1)
						{
							qs.set(KILL_COUNT_VAR2, killCount);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
							sendNpcLogList(killer);
						}
						else
						{
							qs.unset(KILL_COUNT_VAR2);
							addSpawn(TELESHA, npc, true, 300000);
							showOnScreenMsg(killer, NpcStringId.CHECK_ON_TELESHA, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs != null)
		{
			if (qs.isCond(4))
			{
				final Set<NpcLogListHolder> holder = new HashSet<>();
				holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_SKELETONS_3.getId(), true, qs.getInt(KILL_COUNT_VAR)));
				return holder;
			}
			else if (qs.isCond(11))
			{
				final Set<NpcLogListHolder> holder = new HashSet<>();
				holder.add(new NpcLogListHolder(NpcStringId.INVESTIGATE_THE_SURROUNDINGS.getId(), true, qs.getInt(KILL_COUNT_VAR2)));
				return holder;
			}
		}
		return super.getNpcLogList(player);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PROFESSION_CHANGE)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onProfessionChange(OnPlayerProfessionChange event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		// Avoid reward more than once.
		if (player.getVariables().getBoolean(R_GRADE_ITEMS_REWARDED_VAR, false))
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCompleted())
		{
			player.getVariables().set(R_GRADE_ITEMS_REWARDED_VAR, true);
			giveItems(player, SS_R, 5000);
			giveItems(player, BSS_R, 5000);
			
			switch (player.getClassId())
			{
				case WARLORD:
				{
					giveItems(player, BOX_R_HEAVY, 1);
					giveItems(player, WEAPON_SPEAR_R, 1);
					break;
				}
				case GLADIATOR:
				case WARCRYER:
				case PROPHET:
				case BLADEDANCER:
				{
					giveItems(player, BOX_R_HEAVY, 1);
					giveItems(player, WEAPON_DUALSWORD_R, 1);
					break;
				}
				case PALADIN:
				case DARK_AVENGER:
				case DEATH_BERSERKER:
				case TEMPLE_KNIGHT:
				case SWORDSINGER:
				case SHILLIEN_KNIGHT:
				case OVERLORD:
				{
					giveItems(player, BOX_R_HEAVY, 1);
					giveItems(player, WEAPON_SWORD_R, 1);
					giveItems(player, WEAPON_SHIELD_R, 1);
					break;
				}
				case TREASURE_HUNTER:
				case PLAINS_WALKER:
				case ABYSS_WALKER:
				case BOUNTY_HUNTER:
				{
					giveItems(player, BOX_R_LIGHT, 1);
					giveItems(player, WEAPON_DUALDAGGER_R, 1);
					break;
				}
				case HAWKEYE:
				case SILVER_RANGER:
				case PHANTOM_RANGER:
				{
					giveItems(player, BOX_R_LIGHT, 1);
					giveItems(player, WEAPON_BOW_R, 1);
					giveItems(player, ORICHALCUM_ARROW_R, 20000);
					break;
				}
				case SORCERER:
				case NECROMANCER:
				case SPELLSINGER:
				case SPELLHOWLER:
				case MALE_SOULBREAKER:
				case FEMALE_SOULBREAKER:
				{
					giveItems(player, BOX_R_ROBE, 1);
					giveItems(player, WEAPON_BUSTER_R, 1);
					giveItems(player, WEAPON_SIGIL_R, 1);
					break;
				}
				case WARLOCK:
				case ELEMENTAL_SUMMONER:
				case PHANTOM_SUMMONER:
				{
					giveItems(player, BOX_R_LIGHT, 1);
					giveItems(player, WEAPON_STAFF_R, 1);
					break;
				}
				case BISHOP:
				case ELDER:
				case SHILLIEN_ELDER:
				{
					giveItems(player, BOX_R_ROBE, 1);
					giveItems(player, WEAPON_CASTER_R, 1);
					giveItems(player, WEAPON_SIGIL_R, 1);
					break;
				}
				case WARSMITH:
				{
					giveItems(player, BOX_R_HEAVY, 1);
					giveItems(player, WEAPON_BLUNT_R, 1);
					giveItems(player, WEAPON_SHIELD_R, 1);
					break;
				}
				case DESTROYER:
				case BERSERKER:
				{
					giveItems(player, BOX_R_HEAVY, 1);
					giveItems(player, WEAPON_GSWORD_R, 1);
					break;
				}
				case TYRANT:
				{
					giveItems(player, BOX_R_HEAVY, 1);
					giveItems(player, WEAPON_FIST_R, 1);
					break;
				}
				case ARBALESTER:
				{
					giveItems(player, BOX_R_LIGHT, 1);
					giveItems(player, ORICHALCUM_BOLT_R, 20000);
					giveItems(player, WEAPON_CROSSBOW_R, 1);
					break;
				}
				case MARAUDER:
				{
					giveItems(player, BOX_R_LIGHT, 1);
					giveItems(player, WEAPON_FIST_R, 1);
					break;
				}
				case CLOUD_BREAKER:
				{
					giveItems(player, BOX_R_ROBE, 1);
					giveItems(player, WEAPON_STAFF_R, 1);
					break;
				}
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		// Fix for player killed skeleton and Telesha disappears.
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(13))
		{
			qs.setCond(12, false);
		}
		
		if (Config.DISABLE_TUTORIAL || ((qs != null) && qs.isCompleted()))
		{
			player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
		}
	}
}
