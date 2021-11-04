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
package quests.Q11024_PathOfDestinyBeginning;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
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
import org.l2jmobius.gameserver.network.serverpackets.ExTutorialShowId;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

import quests.Q11025_PathOfDestinyProving.Q11025_PathOfDestinyProving;

/**
 * Path of Destiny - Beginning (11024)
 * @URL https://l2wiki.com/Path_of_Destiny_-_Beginning
 * @author Liviades
 */
public class Q11024_PathOfDestinyBeginning extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int SILVAN = 33178;
	private static final int NASTY_EYE = 24380;
	private static final int NASTY_BUGGLE = 24381;
	private static final int NASTY_ZOMBIE = 24382;
	private static final int NASTY_ZOMBIE_LORD = 24383;
	// Items
	private static final int SOE_SILVAN = 80678;
	private static final int SOE_TARTI = 80677;
	private static final int SS_NG_NOVICE = 5789;
	private static final int BSS_NG_NOVICE = 5790;
	// Locations
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-17916, 143630, -3904);
	private static final Location TRAINING_GROUNDS_TELEPORT2 = new Location(-16744, 140209, -3872);
	// Misc
	private static final String SHOTS_REWARD_CHECK_VAR = "SHOTS_REWARD_CHECK";
	private static final String REWARD_CHECK_VAR1 = "Q11024_REWARD_1";
	private static final String REWARD_CHECK_VAR2 = "Q11024_REWARD_2";
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final String KILL_COUNT_VAR2 = "KillCount2";
	private static boolean INSTANT_LEVEL_20 = false;
	
	public Q11024_PathOfDestinyBeginning()
	{
		super(11024);
		addStartNpc(TARTI);
		addTalkId(TARTI, SILVAN);
		addKillId(NASTY_EYE, NASTY_BUGGLE, NASTY_ZOMBIE, NASTY_ZOMBIE_LORD);
		setQuestNameNpcStringId(NpcStringId.LV_1_PATH_OF_DESTINY_BEGINNING);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
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
			{
				htmltext = event;
				break;
			}
			case "34505-03.html":
			{
				qs.startQuest();
				qs.setCond(1, true);
				htmltext = event;
				player.sendPacket(new ExTutorialShowId(9)); // Quest
				break;
			}
			case "34505-05.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
					htmltext = event;
					giveStoryBuffReward(npc, player);
					player.sendPacket(new ExTutorialShowId(25)); // Adventurer Guide
				}
				break;
			}
			case "34505-10.html":
			{
				if (qs.isCond(3))
				{
					htmltext = event;
				}
				break;
			}
			case "34505-07.html":
			{
				if (qs.isCond(7))
				{
					qs.setCond(8, true);
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR2, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR2, true);
						if (INSTANT_LEVEL_20 && (player.getLevel() < 20))
						{
							addExpAndSp(player, (ExperienceData.getInstance().getExpForLevel(20) + 100) - player.getExp(), 708);
						}
						else
						{
							addExpAndSp(player, 787633, 708);
						}
					}
					htmltext = event;
				}
				break;
			}
			case "34505-08.html":
			{
				if (qs.isCond(8))
				{
					htmltext = event;
					player.sendPacket(new ExTutorialShowId(102)); // Class Transfer
				}
				break;
			}
			case "34505-09.html":
			{
				if (qs.isCond(8))
				{
					qs.exitQuest(false, true);
					htmltext = player.getRace() == Race.ERTHEIA ? null : event; // TODO: Ertheia html?
					if (CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()) && (player.getRace() != Race.ERTHEIA))
					{
						showOnScreenMsg(player, NpcStringId.CLASS_TRANSFER_IS_AVAILABLE_NCLICK_THE_CLASS_TRANSFER_ICON_IN_THE_NOTIFICATION_WINDOW_TO_TRANSFER_YOUR_CLASS, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
					}
					final Quest nextQuest = QuestManager.getInstance().getQuest(Q11025_PathOfDestinyProving.class.getSimpleName());
					if (nextQuest != null)
					{
						nextQuest.newQuestState(player);
					}
				}
				break;
			}
			case "33178-02.html":
			{
				if (qs.isCond(4))
				{
					qs.setCond(5, true);
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR1, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR1, true);
						addExpAndSp(player, 48229, 43);
					}
					htmltext = event;
				}
				break;
			}
			case "33178-03.html":
			{
				if (qs.isCond(5))
				{
					htmltext = event;
				}
				break;
			}
			case "33178-04.html":
			{
				if (qs.isCond(5))
				{
					qs.setCond(6, true);
					giveStoryBuffReward(npc, player);
					htmltext = event;
					player.sendPacket(new ExTutorialShowId(16)); // TODO: Proper Tutorial ID AUTO-USE SUPPLIES
				}
				break;
			}
			case "teleport1":
			{
				if (qs.isCond(3))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT);
				}
				break;
			}
			case "teleport2":
			{
				if (qs.isCond(6))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT2);
				}
				break;
			}
			case "shotsreward":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					if (!player.getVariables().getBoolean(SHOTS_REWARD_CHECK_VAR, false))
					{
						player.getVariables().set(SHOTS_REWARD_CHECK_VAR, true);
						
						if (player.getRace() == Race.KAMAEL)
						{
							giveItems(player, SS_NG_NOVICE, 3000);
							giveItems(player, BSS_NG_NOVICE, 3000);
						}
						else if (player.isMageClass())
						{
							giveItems(player, BSS_NG_NOVICE, 3000);
						}
						else
						{
							giveItems(player, SS_NG_NOVICE, 3000);
						}
					}
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
						htmltext = "34505-04.html";
						if (qs.isCond(1))
						{
							if (!player.isSimulatingTalking())
							{
								startQuestTimer("shotsreward", 100, npc, player);
								player.sendPacket(new ExTutorialShowId(14)); // Soulshots and Spiritshots
							}
						}
						else if (qs.isCond(3))
						{
							htmltext = "34505-10.html";
						}
						else if (qs.isCond(7))
						{
							htmltext = "34505-06.html";
						}
						else if (qs.isCond(8))
						{
							htmltext = "34505-08.html";
						}
						break;
					}
					case SILVAN:
					{
						if (qs.isCond(4))
						{
							htmltext = "33178-01.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "33178-03.html";
						}
						else if (qs.isCond(6))
						{
							htmltext = "33178-05.html";
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
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if (qs != null)
		{
			switch (npc.getId())
			{
				case NASTY_EYE:
				case NASTY_BUGGLE:
				{
					if (qs.isCond(3))
					{
						final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
						if (killCount < 15)
						{
							qs.set(KILL_COUNT_VAR, killCount);
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
							sendNpcLogList(killer);
						}
						else
						{
							qs.setCond(4, true);
							qs.unset(KILL_COUNT_VAR);
							giveItems(killer, SOE_SILVAN, 1);
							showOnScreenMsg(killer, NpcStringId.USE_SCROLL_OF_ESCAPE_SILVAN_IN_YOUR_INVENTORY_NTALK_TO_SILVAN_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
				case NASTY_ZOMBIE:
				case NASTY_ZOMBIE_LORD:
				{
					if (qs.isCond(6))
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
							qs.setCond(7, true);
							qs.unset(KILL_COUNT_VAR2);
							giveItems(killer, SOE_TARTI, 1);
							showOnScreenMsg(killer, NpcStringId.USE_SCROLL_OF_ESCAPE_TARTI_IN_YOUR_INVENTORY_NTALK_TO_TARTI_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs != null)
		{
			if (qs.isCond(3))
			{
				final Set<NpcLogListHolder> holder = new HashSet<>();
				holder.add(new NpcLogListHolder(NpcStringId.COMBAT_TRAINING_AT_THE_RUINS_OF_DESPAIR.getId(), true, qs.getInt(KILL_COUNT_VAR)));
				return holder;
			}
			else if (qs.isCond(6))
			{
				final Set<NpcLogListHolder> holder = new HashSet<>();
				holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_THE_SWARM_OF_ZOMBIES.getId(), true, qs.getInt(KILL_COUNT_VAR2)));
				return holder;
			}
		}
		return super.getNpcLogList(player);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final PlayerInstance player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if (!Config.DISABLE_TUTORIAL && (qs == null) && (player.getLevel() < 20))
		{
			showOnScreenMsg(player, NpcStringId.TARTI_IS_WORRIED_ABOUT_S1, ExShowScreenMessage.TOP_CENTER, 10000, player.getName());
			return;
		}
		
		if (!CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()) || (player.getRace() == Race.ERTHEIA))
		{
			return;
		}
		
		if (Config.DISABLE_TUTORIAL || ((qs != null) && qs.isCompleted()))
		{
			player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
		}
	}
}
