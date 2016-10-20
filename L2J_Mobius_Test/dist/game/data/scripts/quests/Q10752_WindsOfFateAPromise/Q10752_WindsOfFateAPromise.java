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
package quests.Q10752_WindsOfFateAPromise;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.handler.BypassHandler;
import com.l2jmobius.gameserver.handler.IBypassHandler;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import quests.Q10751_WindsOfFateEncounters.Q10751_WindsOfFateEncounters;

/**
 * Winds of Fate: A Promise (10752)
 * @URL https://l2wiki.com/Winds_of_Fate:_A_Promise
 * @author Gigi
 */
public class Q10752_WindsOfFateAPromise extends Quest implements IBypassHandler
{
	// NPC's
	private static final int MAGISTER_AYANTHE = 33942;
	private static final int MASTER_KATALIN = 33943;
	private static final int KARLA = 33933;
	private static final int GRAND_MASTER_SIEGMUND = 31321;
	private static final int HEAD_BLACKSMITH_LOMBERT = 31317;
	private static final int MYSTERIOUS_WIZARD = 31522;
	private static final int TOMBSTONE = 31523;
	private static final int GHOST_OF_VON_HELLMANN = 31524;
	private static final int BROKEN_BOOKSHELF = 31526;
	// Item's
	private static final int NAVARIS_MARK = 39536; //
	private static final int PROPHECY_MACHINE_FRAGMENT = 39537;
	private static final int KAINS_PROPHECY_MACHINE_FRAGMENT = 39538;
	private static final int MYSTERIOUS_SOULSHOT_LARGE_PACK_S_GRADE = 22576;
	private static final int MYSTERIOUS_BLESSED_SPIRITSHOT_LARGE_PACK_S_GRADE = 22577;
	private static final int STEEL_DOOR_GUILD_COIN = 37045;
	private static final int ADENA = 57;
	// Requirements
	private static final int MIN_LEVEL = 76;
	// Rewards
	private static final long REWARD_EXP = 2050000;
	private static final int REWARD_SP = 0;
	// Other
	private static final int GHOST_DESPAWN_DELAY = 35000; // 35 sec.
	private static final String GHOST_OBJECT_ID_VAR = "ghost_object_id";
	// Teleport
	private static final Location TP_LOC = new Location(-81297, 249787, -3360);
	private static final String[] TP_COMMANDS =
	{
		"Q10752_Teleport"
	};
	
	public Q10752_WindsOfFateAPromise()
	{
		super(10752, Q10752_WindsOfFateAPromise.class.getSimpleName(), "Winds of Fate: A Promise");
		addStartNpc(MAGISTER_AYANTHE, MASTER_KATALIN);
		addTalkId(MAGISTER_AYANTHE, MASTER_KATALIN, KARLA, GRAND_MASTER_SIEGMUND, HEAD_BLACKSMITH_LOMBERT, MYSTERIOUS_WIZARD, TOMBSTONE, GHOST_OF_VON_HELLMANN, BROKEN_BOOKSHELF);
		registerQuestItems(NAVARIS_MARK, PROPHECY_MACHINE_FRAGMENT, KAINS_PROPHECY_MACHINE_FRAGMENT);
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondMinLevel(MIN_LEVEL, "no_level.html");
		BypassHandler.getInstance().registerHandler(this);
		addCondCompletedQuest(Q10751_WindsOfFateEncounters.class.getSimpleName(), "restriction.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		String htmltext = null;
		switch (event)
		{
			case "33942-02.htm":
			case "33942-03.htm":
			case "33942-04.htm":
			case "33942-08.html":
			case "33942-09.html":
			case "33943-02.htm":
			case "33943-03.htm":
			case "33943-04.htm":
			case "33943-08.html":
			case "33943-09.html":
			case "33933-02.html":
			case "33933-03.html":
			case "33933-07.html":
			case "33933-08.html":
			case "31321-02.html":
			case "31317-02.html":
			case "31317-03.html":
			case "31317-04.html":
			case "31522-02.html":
			case "31522-03.html":
			case "31522-04.html":
			case "31523-02.html":
			case "31524-02.html":
			case "31526-02.html":
			case "33979-02.html":
			case "33979-03.html":
			case "33979-04.html":
			case "33979-05.html":
			case "33979-06.html":
			case "33979-07.html":
			case "33979-08.html":
			case "33979-09.html":
			case "33979-10.html":
			{
				htmltext = event;
				break;
			}
			case "close":
			{
				return null;
			}
			case "33942-05.htm":
			case "33943-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33933-04.html":
			{
				giveItems(player, NAVARIS_MARK, 1);
				giveItems(player, PROPHECY_MACHINE_FRAGMENT, 1);
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "31321-03.html":
			{
				qs.setCond(1); // StateFlags
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "31317-05.html":
			{
				qs.setCond(4, true);
				htmltext = event;
				break;
			}
			case "31522-05.html":
			{
				qs.setCond(5, true);
				htmltext = event;
				break;
			}
			case "31523-03.html":
			{
				qs.setCond(6, true);
				L2Npc ghost = addSpawn(GHOST_OF_VON_HELLMANN, 51358, -54612, -3128, npc.getHeading(), true, GHOST_DESPAWN_DELAY, false);
				ghost.setTitle(player.getName());
				ghost.setIsRunning(false);
				ghost.broadcastInfo();
				qs.set(GHOST_OBJECT_ID_VAR, ghost.getObjectId());
				showOnScreenMsg(player, NpcStringId.TALK_TO_THE_GHOST_OF_VON_HELLMANN, ExShowScreenMessage.TOP_CENTER, 10000);
				htmltext = event;
				break;
			}
			case "31524-03.html":
			{
				qs.setCond(7, true);
				showOnScreenMsg(player, NpcStringId.TIME_TO_MOVE_ONTO_THE_NEXT_PLACE, ExShowScreenMessage.TOP_CENTER, 15000);
				htmltext = event;
				break;
			}
			case "31526-03.html":
			{
				qs.setCond(8, true);
				htmltext = event;
				break;
			}
			case "33933-09.html":
			{
				if (qs.getPlayer().isMageClass())
				{
					qs.setCond(10, true);
					htmltext = "33933-09a.html";
				}
				else
				{
					qs.setCond(11, true);
					htmltext = event;
				}
				break;
			}
		}
		if (event.startsWith("change_tof_"))
		{
			if (qs.isCond(11))
			{
				final int classId = Integer.parseInt(event.replace("change_tof_", ""));
				player.setBaseClassId(classId);
				player.setClassId(classId);
				// player.broadcastSocialAction(SocialAction.REAWAKENING); //TODO need core support
				takeItems(player, NAVARIS_MARK, -1);
				takeItems(player, PROPHECY_MACHINE_FRAGMENT, -1);
				takeItems(player, KAINS_PROPHECY_MACHINE_FRAGMENT, -1);
				if (classId == 186)
				{
					htmltext = "33943-10.html";
					giveItems(player, ADENA, 5000000);
					giveItems(player, MYSTERIOUS_SOULSHOT_LARGE_PACK_S_GRADE, 1);
					giveItems(player, MYSTERIOUS_BLESSED_SPIRITSHOT_LARGE_PACK_S_GRADE, 1);
					giveItems(player, STEEL_DOOR_GUILD_COIN, 87);
					addExpAndSp(player, REWARD_EXP, REWARD_SP);
				}
				player.broadcastUserInfo();
				qs.exitQuest(false, true);
			}
		}
		if (event.startsWith("change_tom_"))
		{
			if (qs.isCond(10))
			{
				final int classId = Integer.parseInt(event.replace("change_tom_", ""));
				player.setBaseClassId(classId);
				player.setClassId(classId);
				// player.broadcastSocialAction(SocialAction.REAWAKENING); //TODO need core support
				takeItems(player, NAVARIS_MARK, -1);
				takeItems(player, PROPHECY_MACHINE_FRAGMENT, -1);
				takeItems(player, KAINS_PROPHECY_MACHINE_FRAGMENT, -1);
				if (classId == 187)
				{
					htmltext = "33942-10.html";
					giveItems(player, ADENA, 5000000);
					giveItems(player, MYSTERIOUS_SOULSHOT_LARGE_PACK_S_GRADE, 1);
					giveItems(player, MYSTERIOUS_BLESSED_SPIRITSHOT_LARGE_PACK_S_GRADE, 1);
					giveItems(player, STEEL_DOOR_GUILD_COIN, 87);
					addExpAndSp(player, REWARD_EXP, REWARD_SP);
				}
				player.broadcastUserInfo();
				qs.exitQuest(false, true);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case MAGISTER_AYANTHE:
					{
						if (player.getClassId() == ClassId.CLOUD_BREAKER)
						{
							htmltext = "33942-01.htm";
						}
						else
						{
							htmltext = "restriction.html";
						}
						break;
					}
					case MASTER_KATALIN:
					{
						if (player.getClassId() == ClassId.MARAUDER)
						{
							htmltext = "33943-01.htm";
						}
						else
						{
							htmltext = "restriction.html";
						}
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case MAGISTER_AYANTHE:
					{
						if ((qs.isCond(1)) && (qs.getCond() < 10))
						{
							htmltext = "33942-06.html";
						}
						else if (qs.isCond(10))
						{
							htmltext = "33942-07.html";
						}
					}
						break;
					case MASTER_KATALIN:
					{
						if ((qs.getCond() > 0) && (qs.getCond() < 11))
						{
							htmltext = "33943-06.html";
						}
						else if (qs.isCond(11))
						{
							htmltext = "33943-07.html";
						}
					}
						break;
					case KARLA:
					{
						if (qs.isCond(1))
						{
							htmltext = "33933-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "33933-05.html";
						}
						else if (qs.isCond(9))
						{
							htmltext = "33933-06.html";
						}
						else if (qs.isCond(10))
						{
							htmltext = "33933-10a.html";
						}
						else if (qs.isCond(11))
						{
							htmltext = "33933-10.html";
						}
					}
						break;
					case GRAND_MASTER_SIEGMUND:
					{
						if (qs.isCond(2))
						{
							htmltext = "31321-01.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "31321-04.html";
						}
					}
						break;
					case HEAD_BLACKSMITH_LOMBERT:
					{
						if (qs.isCond(3))
						{
							htmltext = "31317-01.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "31317-06.html";
						}
					}
						break;
					case MYSTERIOUS_WIZARD:
					{
						if (qs.isCond(4))
						{
							htmltext = "31522-01.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "31522-06.html";
						}
					}
						break;
					case TOMBSTONE:
					{
						if (qs.isCond(5))
						{
							htmltext = "31523-01.html";
						}
						else if (qs.isCond(6))
						{
							htmltext = null;
							showOnScreenMsg(player, NpcStringId.TALK_TO_THE_GHOST_OF_VON_HELLMANN, ExShowScreenMessage.TOP_CENTER, 5000);
						}
					}
						break;
					case GHOST_OF_VON_HELLMANN:
					{
						if (qs.isCond(6))
						{
							if (npc.getObjectId() == qs.getInt(GHOST_OBJECT_ID_VAR))
							{
								htmltext = "31524-01.html";
							}
						}
					}
						break;
					case BROKEN_BOOKSHELF:
					{
						if (qs.isCond(7))
						{
							htmltext = "31526-01.html";
						}
						if (qs.isCond(8))
						{
							htmltext = "31526-04.html";
						}
						if (qs.isCond(9))
						{
							htmltext = "31526-05.html";
						}
					}
						break;
				}
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final L2PcInstance player = event.getActiveChar();
		final QuestState qs = getQuestState(player, false);
		if ((qs == null) && (player.getLevel() >= MIN_LEVEL) && (player.getRace() == Race.ERTHEIA))
		{
			if (player.getClassId() == ClassId.CLOUD_BREAKER)
			{
				showOnScreenMsg(player, NpcStringId.MAGISTER_AYANTHE_HAS_SENT_A_LETTER_NCLICK_THE_QUESTION_MARK_ICON_TO_READ, ExShowScreenMessage.TOP_CENTER, 10000);
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10752_WindsOfFateAPromise/AnnounceM.html"));
				player.sendPacket(html);
			}
			if (player.getClassId() == ClassId.MARAUDER)
			{
				showOnScreenMsg(player, NpcStringId.MASTER_KATALIN_HAS_SENT_A_LETTER_NCLICK_THE_QUESTION_MARK_ICON_TO_READ, ExShowScreenMessage.TOP_CENTER, 10000);
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10752_WindsOfFateAPromise/AnnounceF.html"));
				player.sendPacket(html);
			}
		}
	}
	
	@Override
	public boolean useBypass(String command, L2PcInstance player, L2Character bypassOrigin)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) || (player.getLevel() < MIN_LEVEL) || (player.getRace() != Race.ERTHEIA))
		{
			return false;
		}
		if (player.isInParty())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in party.", 5000));
		}
		else if (player.isInCombat())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you in combat status.", 5000));
		}
		else if (player.isInDuel())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in a duel.", 5000));
		}
		else if (player.isInOlympiadMode())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in Olympiad.", 5000));
		}
		else if (player.isInVehicle())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in any vehicle or mount.", 5000));
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.teleToLocation(TP_LOC);
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return TP_COMMANDS;
	}
}