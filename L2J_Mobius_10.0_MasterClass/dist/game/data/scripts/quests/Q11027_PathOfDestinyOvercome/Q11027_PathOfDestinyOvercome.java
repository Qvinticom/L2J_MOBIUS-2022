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
package quests.Q11027_PathOfDestinyOvercome;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
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
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

import quests.Q11026_PathOfDestinyConviction.Q11026_PathOfDestinyConviction;

/**
 * Path of Destiny - Overcome (11027)
 * @URL https://l2wiki.com/Path_of_Destiny_-_Overcome
 * @author Liviades
 */
public class Q11027_PathOfDestinyOvercome extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int RAYMOND = 30289;
	private static final int GERETH = 33932;
	private static final int RECLOUS = 30648;
	// Items
	private static final int PROPHECY_MACHINE = 39540;
	private static final int ATELIA = 39542;
	private static final int ORC_EMPOWERING_POTION = 80675;
	private static final int KETRA_ORDER = 80676;
	// Monsters
	private static final int TUREK_WAR_HOUND = 24403;
	private static final int TUREK_ORC_FOOTMAN = 24404;
	private static final int TUREK_ORC_ARCHER = 24405;
	private static final int TUREK_ORC_SKIRMISHER = 24406;
	private static final int TUREK_ORC_PREFECT = 24407;
	private static final int TUREK_ORC_PRIEST = 24408;
	private static final int KETRA_ORC_WARRIOR = 24410;
	private static final int KETRA_ORC_RAIDER = 24409;
	private static final int KETRA_ORC_SCOUT = 24411;
	private static final int KETRA_ORC_PRIEST = 24412;
	private static final int KETRA_ORC_OFFICER = 24413;
	private static final int KETRA_ORC_CAPTAIN = 24414;
	// Reward
	private static final int CHAOS_POMANDER = 37374;
	private static final int VITALITY_MAINTAINING_RUNE = 80712;
	private static final Map<CategoryType, Integer> AWAKE_POWER = new EnumMap<>(CategoryType.class);
	static
	{
		AWAKE_POWER.put(CategoryType.SIXTH_SIGEL_GROUP, 32264);
		AWAKE_POWER.put(CategoryType.SIXTH_TIR_GROUP, 32265);
		AWAKE_POWER.put(CategoryType.SIXTH_OTHEL_GROUP, 32266);
		AWAKE_POWER.put(CategoryType.SIXTH_YR_GROUP, 32267);
		AWAKE_POWER.put(CategoryType.SIXTH_FEOH_GROUP, 32268);
		AWAKE_POWER.put(CategoryType.SIXTH_WYNN_GROUP, 32269);
		AWAKE_POWER.put(CategoryType.SIXTH_IS_GROUP, 32270);
		AWAKE_POWER.put(CategoryType.SIXTH_EOLH_GROUP, 32271);
	}
	// Location
	private static final Location TELEPORT1 = new Location(-89443, 111717, -3336);
	private static final Location TELEPORT2 = new Location(-92290, 116512, -3472);
	private static final Location TELEPORT3 = new Location(-92680, 112394, -3696);
	private static final Location TELEPORT4 = new Location(-93023, 108834, -3856);
	private static final Location TELEPORT5 = new Location(-95920, 102192, -3544);
	private static final Location TELEPORT6 = new Location(-88533, 104054, -3416);
	private static final Location TELEPORT7 = new Location(-78669, 251000, -2971);
	private static final Location TELEPORT8 = new Location(-14180, 123840, -3120);
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final String KILL_COUNT_VAR2 = "KillCount2";
	private static final String KILL_COUNT_VAR3 = "KillCount3";
	private static final String KILL_COUNT_VAR4 = "KillCount3";
	private static final String REWARD_CHECK_VAR1 = "Q11027_REWARD_1";
	private static final String REWARD_CHECK_VAR2 = "Q11027_REWARD_2";
	private static final String REWARD_CHECK_VAR3 = "Q11027_REWARD_3";
	private static final String REWARD_CHECK_VAR4 = "Q11027_REWARD_4";
	private static final String REWARD_CHECK_VAR5 = "Q11027_REWARD_5";
	private static final String REWARD_CHECK_VAR6 = "Q11027_REWARD_6";
	private static final String AWAKE_POWER_REWARDED_VAR = "AWAKE_POWER_REWARDED";
	private static final int LEVEL_76 = 76;
	private static final int LEVEL_85 = 85;
	private static boolean INSTANT_LEVEL_85 = false;
	
	public Q11027_PathOfDestinyOvercome()
	{
		super(11027);
		addStartNpc(TARTI);
		addTalkId(TARTI, RECLOUS, RAYMOND, GERETH);
		addKillId(TUREK_WAR_HOUND, TUREK_ORC_FOOTMAN, TUREK_ORC_ARCHER, TUREK_ORC_SKIRMISHER, TUREK_ORC_PREFECT, TUREK_ORC_PRIEST, KETRA_ORC_WARRIOR, KETRA_ORC_RAIDER, KETRA_ORC_SCOUT, KETRA_ORC_PRIEST, KETRA_ORC_OFFICER, KETRA_ORC_CAPTAIN);
		registerQuestItems(PROPHECY_MACHINE, ATELIA, ORC_EMPOWERING_POTION, KETRA_ORDER);
		addCondMinLevel(LEVEL_76, "34505-14.html"); // Not retail, I do the same as on older quest but updated.
		addCondCompletedQuest(Q11026_PathOfDestinyConviction.class.getSimpleName(), "34505-15.html");
		setQuestNameNpcStringId(NpcStringId.LV_76_PATH_OF_DESTINY_OVERCOME);
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
			case "30648-03.html":
			case "30648-04.html":
			case "30648-09.html":
			case "30648-10.html":
			case "30648-15.html":
			case "30648-16.html":
			case "30648-21.html":
			case "30648-22.html":
			case "30648-27.html":
			case "30648-28.html":
			case "34505-08.html":
			case "34505-09.html":
			case "30289-02.html":
			case "33932-02.html":
			case "33932-03.html":
			case "33932-04.html":
			case "33932-05.html":
			{
				htmltext = event;
				break;
			}
			case "34505-04.htm":
			{
				if (player.getLevel() >= LEVEL_76)
				{
					qs.startQuest();
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				else
				{
					htmltext = "34505-14.html";
				}
				break;
			}
			case "30648-02.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR1, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR1, true);
						addExpAndSp(player, 392513005, 353261);
					}
				}
				break;
			}
			case "30648-05.html":
			{
				if (qs.isCond(3))
				{
					qs.setCond(4, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "30648-08.html":
			{
				if (qs.isCond(5))
				{
					qs.setCond(6, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR2, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR2, true);
						addExpAndSp(player, 581704958, 523534);
					}
				}
				break;
			}
			case "30648-11.html":
			{
				if (qs.isCond(6))
				{
					qs.setCond(7, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "30648-14.html":
			{
				if (qs.isCond(8))
				{
					takeItems(player, ORC_EMPOWERING_POTION, 15);
					qs.setCond(9, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR3, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR3, true);
						addExpAndSp(player, 750392145, 675352);
					}
				}
				break;
			}
			case "30648-17.html":
			{
				if (qs.isCond(9))
				{
					qs.setCond(10, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "30648-20.html":
			{
				if (qs.isCond(11))
				{
					qs.setCond(12, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR4, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR4, true);
						addExpAndSp(player, 452984693, 407686);
					}
				}
				break;
			}
			case "30648-23.html":
			{
				if (qs.isCond(12))
				{
					qs.setCond(13, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "30648-26.html":
			{
				if (qs.isCond(14))
				{
					takeItems(player, KETRA_ORDER, 15);
					qs.setCond(15, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR5, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR5, true);
						addExpAndSp(player, 514892511, 463403);
					}
				}
				break;
			}
			case "30648-29.html":
			{
				if (qs.isCond(15))
				{
					qs.setCond(16, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "34505-07.html":
			{
				if (qs.isCond(17))
				{
					qs.setCond(18, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR6, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR6, true);
						if (INSTANT_LEVEL_85 && (player.getLevel() < LEVEL_85))
						{
							addExpAndSp(player, (ExperienceData.getInstance().getExpForLevel(LEVEL_85) + 100) - player.getExp(), 527586);
						}
						else
						{
							addExpAndSp(player, 1176372111, 527586);
						}
						giveAdena(player, 420000, true);
					}
				}
				break;
			}
			case "34505-10.html":
			{
				if ((player.getLevel() >= LEVEL_85) && qs.isCond(19))
				{
					qs.setCond(20, true);
					htmltext = event;
				}
				break;
			}
			case "30289-03.html":
			{
				if (qs.isCond(19) || qs.isCond(20))
				{
					qs.setCond(21, true);
					giveItems(player, PROPHECY_MACHINE, 1);
					htmltext = event;
				}
				break;
			}
			case "33932-06.html":
			{
				if (qs.isCond(21))
				{
					qs.setCond(22, true);
					htmltext = event;
				}
				break;
			}
			case "33932-08.html":
			{
				if (qs.isCond(23))
				{
					qs.setCond(24, true);
					htmltext = event;
				}
				break;
			}
			case "34505-13.html":
			{
				if (qs.isCond(24))
				{
					giveItems(player, VITALITY_MAINTAINING_RUNE, 1);
					giveItems(player, CHAOS_POMANDER, 2);
					qs.exitQuest(false, true);
					if (CategoryData.getInstance().isInCategory(CategoryType.FOURTH_CLASS_GROUP, player.getClassId().getId()) || (CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, player.getClassId().getId()) && (player.getRace() == Race.ERTHEIA)))
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
					player.teleToLocation(TELEPORT1);
				}
				break;
			}
			case "teleport2":
			{
				if (qs.isCond(4))
				{
					player.teleToLocation(TELEPORT2);
				}
				break;
			}
			case "teleport3":
			{
				if (qs.isCond(7))
				{
					player.teleToLocation(TELEPORT3);
				}
				break;
			}
			case "teleport4":
			{
				if (qs.isCond(10))
				{
					player.teleToLocation(TELEPORT4);
				}
				break;
			}
			case "teleport5":
			{
				if (qs.isCond(13))
				{
					player.teleToLocation(TELEPORT5);
				}
				break;
			}
			case "teleport6":
			{
				if (qs.isCond(16))
				{
					player.teleToLocation(TELEPORT6);
				}
				break;
			}
			case "teleport7":
			{
				if (qs.isCond(21))
				{
					player.teleToLocation(TELEPORT7);
				}
				break;
			}
			case "teleport8":
			{
				if (qs.isCond(24))
				{
					player.teleToLocation(TELEPORT8);
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
				if (npc.getId() == TARTI)
				{
					htmltext = "34505-01.htm";
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
								htmltext = "34505-05.html";
								break;
							}
							case 17:
							{
								htmltext = "34505-06.html";
								break;
							}
							case 18:
							{
								if (player.getLevel() >= LEVEL_85)
								{
									qs.setCond(19, true);
									htmltext = "34505-08.html";
								}
								else
								{
									htmltext = "34505-16.html";
								}
								break;
							}
							case 19:
							{
								htmltext = "34505-09.html";
								break;
							}
							case 20:
							{
								htmltext = "34505-11.html";
								break;
							}
							case 24:
							{
								htmltext = "34505-12.html";
								break;
							}
						}
						break;
					}
					case RECLOUS:
					{
						switch (qs.getCond())
						{
							case 2:
							{
								htmltext = "30648-01.html";
								break;
							}
							case 3:
							{
								htmltext = "30648-02.html";
								break;
							}
							case 4:
							{
								htmltext = "30648-06.html";
								break;
							}
							case 5:
							{
								htmltext = "30648-07.html";
								break;
							}
							case 6:
							{
								htmltext = "30648-08.html";
								break;
							}
							case 7:
							{
								htmltext = "30648-12.html";
								break;
							}
							case 8:
							{
								htmltext = "30648-13.html";
								break;
							}
							case 9:
							{
								htmltext = "30648-14.html";
								break;
							}
							case 10:
							{
								htmltext = "30648-18.html";
								break;
							}
							case 11:
							{
								htmltext = "30648-19.html";
								break;
							}
							case 12:
							{
								htmltext = "30648-20.html";
								break;
							}
							case 13:
							{
								htmltext = "30648-24.html";
								break;
							}
							case 14:
							{
								htmltext = "30648-25.html";
								break;
							}
							case 15:
							{
								htmltext = "30648-26c.html";
								break;
							}
							case 16:
							{
								htmltext = "30648-30.html";
								break;
							}
						}
						break;
					}
					case RAYMOND:
					{
						if (qs.isCond(19) || qs.isCond(20))
						{
							htmltext = "30289-01.html";
						}
						else if (qs.isCond(21))
						{
							htmltext = "30289-04.html";
						}
						break;
					}
					case GERETH:
					{
						if (qs.isCond(21))
						{
							htmltext = "33932-01.html";
						}
						else if (qs.isCond(22))
						{
							htmltext = "33932-06.html";
						}
						else if (qs.isCond(23))
						{
							htmltext = "33932-07.html";
						}
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
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, true);
		if (qs != null)
		{
			switch (npc.getId())
			{
				case TUREK_WAR_HOUND:
				case TUREK_ORC_FOOTMAN:
				{
					if (qs.isCond(1))
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
							qs.setCond(2, true);
							qs.unset(KILL_COUNT_VAR);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_RECLOUS_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case TUREK_ORC_ARCHER:
				case TUREK_ORC_SKIRMISHER:
				{
					if (qs.isCond(4))
					{
						final int killCount = qs.getInt(KILL_COUNT_VAR2) + 1;
						if (killCount < 30)
						{
							qs.set(KILL_COUNT_VAR2, killCount);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
							sendNpcLogList(killer);
						}
						else
						{
							qs.setCond(5, true);
							qs.unset(KILL_COUNT_VAR2);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_RECLOUS_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case TUREK_ORC_PREFECT:
				case TUREK_ORC_PRIEST:
				{
					if (qs.isCond(7) && getRandomBoolean())
					{
						if (getQuestItemsCount(killer, ORC_EMPOWERING_POTION) < 15)
						{
							giveItems(killer, ORC_EMPOWERING_POTION, 1);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
						if (getQuestItemsCount(killer, ORC_EMPOWERING_POTION) >= 15)
						{
							qs.setCond(8, true);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_RECLOUS_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case KETRA_ORC_RAIDER:
				case KETRA_ORC_WARRIOR:
				{
					if (qs.isCond(10))
					{
						final int killCount = qs.getInt(KILL_COUNT_VAR3) + 1;
						if (killCount < 30)
						{
							qs.set(KILL_COUNT_VAR3, killCount);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
							sendNpcLogList(killer);
						}
						else
						{
							qs.setCond(11, true);
							qs.unset(KILL_COUNT_VAR3);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_RECLOUS_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case KETRA_ORC_SCOUT:
				case KETRA_ORC_PRIEST:
				{
					if (qs.isCond(13) && getRandomBoolean())
					{
						if (getQuestItemsCount(killer, KETRA_ORDER) < 15)
						{
							giveItems(killer, KETRA_ORDER, 1);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
						if (getQuestItemsCount(killer, KETRA_ORDER) >= 15)
						{
							qs.setCond(14, true);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_RECLOUS_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case KETRA_ORC_OFFICER:
				case KETRA_ORC_CAPTAIN:
				{
					if (qs.isCond(16))
					{
						final int killCount = qs.getInt(KILL_COUNT_VAR4) + 1;
						if (killCount < 30)
						{
							qs.set(KILL_COUNT_VAR4, killCount);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
							sendNpcLogList(killer);
						}
						else
						{
							qs.setCond(17, true);
							qs.unset(KILL_COUNT_VAR4);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_TARTI_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
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
			switch (qs.getCond())
			{
				case 1:
				{
					final Set<NpcLogListHolder> holder = new HashSet<>();
					holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_TUREK_WAR_HOUNDS_AND_FOOTMEN_2.getId(), true, qs.getInt(KILL_COUNT_VAR)));
					return holder;
				}
				case 4:
				{
					final Set<NpcLogListHolder> holder = new HashSet<>();
					holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_TUREK_ARCHERS_AND_SKIRMISHERS_2.getId(), true, qs.getInt(KILL_COUNT_VAR2)));
					return holder;
				}
				case 10:
				{
					final Set<NpcLogListHolder> holder = new HashSet<>();
					holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_KETRA_RAIDERS_AND_WARRIORS_2.getId(), true, qs.getInt(KILL_COUNT_VAR3)));
					return holder;
				}
				case 16:
				{
					final Set<NpcLogListHolder> holder = new HashSet<>();
					holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_KETRA_OFFICERS_AND_CAPTAIN_2.getId(), true, qs.getInt(KILL_COUNT_VAR4)));
					return holder;
				}
			}
		}
		return super.getNpcLogList(player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + "-01.html";
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
		
		// Avoid reward more than once.
		if (player.getVariables().getBoolean(AWAKE_POWER_REWARDED_VAR, false))
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCompleted())
		{
			if (player.getRace() == Race.ERTHEIA)
			{
				if (player.getClassId() == ClassId.EVISCERATOR)
				{
					player.getVariables().set(AWAKE_POWER_REWARDED_VAR, true);
					giveItems(player, 40268, 1);
				}
				if (player.getClassId() == ClassId.SAYHA_SEER)
				{
					player.getVariables().set(AWAKE_POWER_REWARDED_VAR, true);
					giveItems(player, 40269, 1);
				}
			}
			else
			{
				for (Entry<CategoryType, Integer> ent : AWAKE_POWER.entrySet())
				{
					if (player.isInCategory(ent.getKey()))
					{
						player.getVariables().set(AWAKE_POWER_REWARDED_VAR, true);
						giveItems(player, ent.getValue().intValue(), 1);
						break;
					}
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
		
		if (!CategoryData.getInstance().isInCategory(CategoryType.FOURTH_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if (Config.DISABLE_TUTORIAL || ((qs != null) && qs.isCompleted()))
		{
			player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
		}
	}
}
