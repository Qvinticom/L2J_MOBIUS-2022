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
package quests.Q11026_PathOfDestinyConviction;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.CategoryType;
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
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

import quests.Q11025_PathOfDestinyProving.Q11025_PathOfDestinyProving;

/**
 * Path of Destiny - Conviction (11026)
 * @URL https://l2wiki.com/Path_of_Destiny_-_Conviction
 * @author Liviades
 */
public class Q11026_PathOfDestinyConviction extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int RAYMOND = 30289;
	private static final int PIO = 33963;
	// Monsters
	private static final int SOBBING_WINDRA = 24391;
	private static final int WHISPERING_WINDRA = 24392;
	private static final int GIGGLING_WINDRA = 24393;
	private static final int FEAR_RATEL = 24394;
	private static final int FEAR_ROBUST_RATEL = 24395;
	private static final int FEAR_GROWLER = 24396;
	private static final int FEAR_GROWLER_EVOLVED = 24397;
	private static final int FEAR_GROWLER_ROBUST = 24398;
	private static final int FUSSY_LEAF = 24399;
	private static final int FUSSY_ARBOR = 24400;
	private static final int TINY_WINDIMA = 24401;
	private static final int GIANT_WINDIMA = 24402;
	// Quest Item
	private static final int KAIN_PROPHECY_MACHINE_FRAGMENT = 39538;
	private static final int CORRUPTED_ENERGY = 80673;
	private static final int EMBEDDED_SHARD = 80674;
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT1 = new Location(-76775, 92186, -3688);
	private static final Location TRAINING_GROUNDS_TELEPORT2 = new Location(-81155, 89637, -3728);
	private static final Location TRAINING_GROUNDS_TELEPORT3 = new Location(-85476, 80753, -3048);
	private static final Location TRAINING_GROUNDS_TELEPORT4 = new Location(-87952, 87062, -3416);
	private static final Location TRAINING_GROUNDS_TELEPORT5 = new Location(-91374, 92270, -3360);
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final String KILL_COUNT_VAR2 = "KillCount2";
	private static final String KILL_COUNT_VAR3 = "KillCount3";
	private static final String REWARD_CHECK_VAR1 = "Q11026_REWARD_1";
	private static final String REWARD_CHECK_VAR2 = "Q11026_REWARD_2";
	private static final String REWARD_CHECK_VAR3 = "Q11026_REWARD_3";
	private static final String REWARD_CHECK_VAR4 = "Q11026_REWARD_4";
	private static final String REWARD_CHECK_VAR5 = "Q11026_REWARD_5";
	private static final int LEVEL_40 = 40;
	private static final int LEVEL_76 = 76;
	private static boolean INSTANT_LEVEL_76 = false;
	
	public Q11026_PathOfDestinyConviction()
	{
		super(11026);
		addStartNpc(TARTI);
		addTalkId(TARTI, RAYMOND, PIO);
		addKillId(SOBBING_WINDRA, WHISPERING_WINDRA, GIGGLING_WINDRA, FEAR_RATEL, FEAR_ROBUST_RATEL, FEAR_GROWLER, FEAR_GROWLER_EVOLVED, FEAR_GROWLER_ROBUST, FUSSY_LEAF, FUSSY_ARBOR, TINY_WINDIMA, GIANT_WINDIMA);
		registerQuestItems(KAIN_PROPHECY_MACHINE_FRAGMENT, CORRUPTED_ENERGY, EMBEDDED_SHARD);
		addCondMinLevel(LEVEL_40, "34505-011.html"); // Custom.
		addCondCompletedQuest(Q11025_PathOfDestinyProving.class.getSimpleName(), "34505-012.html"); // ADD TEXT
		setQuestNameNpcStringId(NpcStringId.LV_40_PATH_OF_DESTINY_CONVICTION);
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
			case "33963-03.html":
			case "33963-04.html":
			case "33963-09.html":
			case "33963-10.html":
			case "33963-15.html":
			case "33963-16.html":
			case "33963-21.html":
			case "33963-22.html":
			case "34505-08.html":
			case "34505-09.html":
			case "34505-10.html":
			case "30289-02.html":
			case "34505-14.html":
			case "34505-15.html":
			case "34505-16.html":
			case "34505-17.html":
			{
				htmltext = event;
				break;
			}
			case "34505-04.html":
			{
				qs.startQuest();
				qs.setCond(1, true);
				giveStoryBuffReward(npc, player);
				htmltext = event;
				break;
			}
			case "33963-02.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR1, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR1, true);
						addExpAndSp(player, 14281098, 12852);
					}
				}
				break;
			}
			case "33963-05.html":
			{
				if (qs.isCond(3))
				{
					qs.setCond(4, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "33963-08.html":
			{
				if (qs.isCond(5))
				{
					qs.setCond(6, true);
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR2, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR2, true);
						addExpAndSp(player, 30949789, 27854);
					}
				}
				htmltext = event;
				break;
			}
			case "33963-11.html":
			{
				if (qs.isCond(6))
				{
					qs.setCond(7, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "33963-14.html":
			{
				if (qs.isCond(8))
				{
					takeItems(player, CORRUPTED_ENERGY, 15);
					qs.setCond(9, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR3, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR3, true);
						addExpAndSp(player, 76142825, 68528);
					}
				}
				break;
			}
			case "33963-17.html":
			{
				if (qs.isCond(9))
				{
					qs.setCond(10, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "33963-20.html":
			{
				if (qs.isCond(11))
				{
					takeItems(player, EMBEDDED_SHARD, 15);
					qs.setCond(12, true);
					htmltext = event;
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR4, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR4, true);
						addExpAndSp(player, 174520303, 157068);
					}
				}
				break;
			}
			case "33963-23.html":
			{
				if (qs.isCond(12))
				{
					qs.setCond(13, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
			}
			case "34505-07.html":
			{
				if (qs.isCond(14))
				{
					qs.setCond(15, true);
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR5, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR5, true);
						if (INSTANT_LEVEL_76 && (player.getLevel() < LEVEL_76))
						{
							addExpAndSp(player, (ExperienceData.getInstance().getExpForLevel(LEVEL_76) + 100) - player.getExp(), 595042);
						}
						else
						{
							addExpAndSp(player, 834929477, 595042);
						}
						giveAdena(player, 240000, true);
					}
					htmltext = event;
				}
				break;
			}
			case "34505-11.html":
			{
				if (qs.isCond(15) && (player.getLevel() >= LEVEL_76))
				{
					qs.setCond(17, true);
					htmltext = event;
				}
				else if (qs.isCond(16) && (player.getLevel() >= LEVEL_76))
				{
					qs.setCond(17, true);
					htmltext = event;
				}
				else
				{
					qs.setCond(16, true);
					htmltext = event;
				}
				break;
			}
			case "30289-03.html":
			{
				if (qs.isCond(17))
				{
					qs.setCond(18, true);
				}
				htmltext = event;
				break;
			}
			case "34505-18.html":
			{
				if (qs.isCond(19))
				{
					qs.exitQuest(false, true);
					if (CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, player.getClassId().getId()) || (CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()) && (player.getRace() == Race.ERTHEIA)))
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
				htmltext = event;
				break;
			}
			case "teleport2":
			{
				if (qs.isCond(4))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT2);
				}
				htmltext = event;
				break;
			}
			case "teleport3":
			{
				if (qs.isCond(7))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT3);
				}
				htmltext = event;
				break;
			}
			case "teleport4":
			{
				if (qs.isCond(10))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT4);
				}
				htmltext = event;
				break;
			}
			case "teleport5":
			{
				if (qs.isCond(13))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT5);
				}
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
							case 14:
							{
								htmltext = "34505-06.html";
								break;
							}
							case 15:
							{
								htmltext = "34505-07.html";
								break;
							}
							case 16:
							{
								htmltext = "34505-10.html";
								break;
							}
							case 17:
							{
								htmltext = "34505-12.html";
								break;
							}
							case 19:
							{
								htmltext = "34505-13.html";
								break;
							}
						}
						break;
					}
					case PIO:
					{
						switch (qs.getCond())
						{
							case 2:
							{
								htmltext = "33963-01.html";
								break;
							}
							case 3:
							{
								htmltext = "33963-02.html";
								break;
							}
							case 4:
							{
								htmltext = "33963-06.html";
								break;
							}
							case 5:
							{
								htmltext = "33963-07.html";
								break;
							}
							case 6:
							{
								htmltext = "33963-08.html";
								break;
							}
							case 7:
							{
								htmltext = "33963-12.html";
								break;
							}
							case 8:
							{
								htmltext = "33963-13.html";
								break;
							}
							case 9:
							{
								htmltext = "33963-14.html";
								break;
							}
							case 10:
							{
								htmltext = "33963-17.html";
								break;
							}
							case 11:
							{
								htmltext = "33963-19.html";
								break;
							}
							case 12:
							{
								htmltext = "33963-20.html";
								break;
							}
							case 13:
							{
								htmltext = "33963-24.html";
								break;
							}
						}
						break;
					}
					case RAYMOND:
					{
						if (qs.isCond(17))
						{
							htmltext = "30289-01.html";
							break;
						}
						else if (qs.isCond(18))
						{
							htmltext = "30289-04.html";
							break;
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
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, true);
		if (qs != null)
		{
			switch (npc.getId())
			{
				case SOBBING_WINDRA:
				case WHISPERING_WINDRA:
				case GIGGLING_WINDRA:
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
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_PIO_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case FEAR_RATEL:
				case FEAR_ROBUST_RATEL:
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
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_PIO_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case FEAR_GROWLER:
				case FEAR_GROWLER_EVOLVED:
				case FEAR_GROWLER_ROBUST:
				{
					if (qs.isCond(7) && getRandomBoolean())
					{
						if (getQuestItemsCount(killer, CORRUPTED_ENERGY) < 15)
						{
							giveItems(killer, CORRUPTED_ENERGY, 1);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
						if (getQuestItemsCount(killer, CORRUPTED_ENERGY) >= 15)
						{
							qs.setCond(8, true);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_PIO_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case FUSSY_LEAF:
				case FUSSY_ARBOR:
				{
					if (qs.isCond(10) && getRandomBoolean())
					{
						if (getQuestItemsCount(killer, EMBEDDED_SHARD) < 15)
						{
							giveItems(killer, EMBEDDED_SHARD, 1);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
						if (getQuestItemsCount(killer, EMBEDDED_SHARD) >= 15)
						{
							qs.setCond(11, true);
							showOnScreenMsg(killer, NpcStringId.USE_TELEPORTATION_CUBE_IN_YOUR_INVENTORY_TALK_TO_PIO_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case TINY_WINDIMA:
				case GIANT_WINDIMA:
				{
					if (qs.isCond(13))
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
							qs.setCond(14, true);
							qs.unset(KILL_COUNT_VAR3);
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
					holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_THE_PACK_OF_WINDRA_2.getId(), true, qs.getInt(KILL_COUNT_VAR)));
					return holder;
				}
				case 4:
				{
					final Set<NpcLogListHolder> holder = new HashSet<>();
					holder.add(new NpcLogListHolder(NpcStringId.ERADICATE_THE_FEAR_RATEL_2.getId(), true, qs.getInt(KILL_COUNT_VAR2)));
					return holder;
				}
				case 13:
				{
					final Set<NpcLogListHolder> holder = new HashSet<>();
					holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_THE_PACK_OF_WINDIMA_2.getId(), true, qs.getInt(KILL_COUNT_VAR3)));
					return holder;
				}
			}
		}
		return super.getNpcLogList(player);
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
		
		if (!CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, player.getClassId().getId()))
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
