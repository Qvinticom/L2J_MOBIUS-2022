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
package quests.Q10751_WindsOfFateEncounters;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.handler.BypassHandler;
import com.l2jmobius.gameserver.handler.IBypassHandler;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.util.Util;

/**
 * Winds of Fate: Encounters (10751)
 * @author Stayway
 */
public class Q10751_WindsOfFateEncounters extends Quest implements IBypassHandler
{
	// NPCs
	private static final int NAVARI = 33931;
	private static final int AYANTHE = 33942;
	private static final int KATALIN = 33943;
	private static final int RAYMOND = 30289;
	private static final int TELESHA = 33981;
	private static final int MYSTERIOUS_WIZARD = 33980;
	// Monsters
	private static final int SKELETON_ARCHER = 27529;
	private static final int SKELETON_WARRIOR = 27528;
	// Items
	private static final ItemHolder WIND_SPIRIT_REALM_RELIC = new ItemHolder(39535, 1);
	private static final ItemHolder NAVARIS_SUPPORT_BOX_F = new ItemHolder(40266, 1);
	private static final ItemHolder NAVARIS_SUPPORT_BOX_M = new ItemHolder(40267, 1);
	// Requirements
	private static final int MIN_LEVEL = 38;
	// Teleport
	private static final Location TP_LOC = new Location(-80565, 251763, -3080);
	private static final String[] TP_COMMANDS =
	{
		"Q10751_Teleport"
	};
	
	public Q10751_WindsOfFateEncounters()
	{
		super(10751, Q10751_WindsOfFateEncounters.class.getSimpleName(), "Winds of Fate: Encounters");
		addStartNpc(NAVARI);
		addTalkId(NAVARI, AYANTHE, RAYMOND, KATALIN, TELESHA, MYSTERIOUS_WIZARD);
		addKillId(SKELETON_ARCHER, SKELETON_WARRIOR);
		registerQuestItems(WIND_SPIRIT_REALM_RELIC.getId(), WIND_SPIRIT_REALM_RELIC.getId(), NAVARIS_SUPPORT_BOX_F.getId());
		addCondMinLevel(MIN_LEVEL, "noLevel.html");
		addCondRace(Race.ERTHEIA, "no_quest.html");
		BypassHandler.getInstance().registerHandler(this);
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
			case "33931-03.html":
			case "33931-05.html":
			case "30289-02.html":
			case "30289-05.html":
			case "33942-04.html":
			case "33942-05.html":
			case "33942-06.html":
			case "33942-07.html":
			case "33942-08.html":
			case "33942-08a.html":
			case "33942-08b.html":
			case "33943-04.html":
			case "33943-05.html":
			case "33943-06.html":
			case "33943-07.html":
			case "33943-08a.html":
			case "33943-08b.html":
			{
				htmltext = event;
				break;
			}
			case "33943-02.html":
			{
				if (qs.isCond(2))
				{
					htmltext = event;
					qs.setCond(4);
				}
				break;
			}
			case "33942-02.html":
			{
				if (qs.isCond(3))
				{
					htmltext = event;
					qs.setCond(4);
				}
				break;
			}
			case "30289-03.html":
			{
				if (qs.isCond(4))
				{
					htmltext = event;
					qs.setCond(6);
					qs.set(Integer.toString(SKELETON_ARCHER), 0);
					qs.set(Integer.toString(SKELETON_WARRIOR), 0);
				}
				break;
			}
			case "wizard":
			{
				if (qs.isCond(6))
				{
					addSpawn(MYSTERIOUS_WIZARD, npc.getX() + 20, npc.getY() + 20, npc.getZ(), npc.getHeading(), false, 50000);
				}
				break;
			}
			case "mysterious-01.html":
			{
				if (qs.isCond(6))
				{
					giveItems(player, WIND_SPIRIT_REALM_RELIC);
					showOnScreenMsg(player, NpcStringId.RETURN_TO_RAYMOND_OF_THE_TOWN_OF_GLUDIO, ExShowScreenMessage.TOP_CENTER, 4500);
					qs.setCond(7);
					htmltext = event;
				}
				break;
			}
			case "33931-04.htm":
			{
				if (player.getClassId().isMage())
				{
					qs.startQuest();
					qs.setCond(3);
					giveItems(player, WIND_SPIRIT_REALM_RELIC);
				}
				htmltext = event;
				break;
			}
			case "33931-02.htm":
			{
				if (!player.getClassId().isMage())
				{
					qs.startQuest();
					qs.setCond(2);
					giveItems(player, WIND_SPIRIT_REALM_RELIC);
					htmltext = event;
				}
				break;
			}
			case "30289-04.htm":
			{
				if (qs.isCond(7))
				{
					htmltext = event;
				}
				break;
			}
			case "accept":
			{
				if (qs.isCond(7))
				{
					if (player.getClassId().isMage())
					{
						htmltext = "30289-06.html";
						qs.setCond(9);
					}
					else
					{
						htmltext = "30289-07.html";
						qs.setCond(8);
					}
				}
				break;
			}
		}
		
		if (event.startsWith("change_to_"))
		{
			final int classId = Integer.parseInt(event.replace("change_to_", ""));
			player.setBaseClassId(classId);
			player.setClassId(classId);
			giveAdena(player, 110000, true);
			addExpAndSp(player, 2700000, 648);
			if (classId == 184)
			{
				htmltext = "33943-ccf.html";
				giveItems(player, NAVARIS_SUPPORT_BOX_F, 1);
			}
			else if (classId == 185)
			{
				htmltext = "33942-ccm.html";
				giveItems(player, NAVARIS_SUPPORT_BOX_M, 1);
			}
			player.broadcastUserInfo();
			qs.exitQuest(false, true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		if (player.getRace() != Race.ERTHEIA)
		{
			return "noErtheia.html";
		}
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case NAVARI:
					{
						if (player.getClassId().isMage())
						{
							htmltext = "33931-m.htm";
						}
						else
						{
							htmltext = "33931-f.htm";
						}
						break;
					}
					case AYANTHE:
					case RAYMOND:
					{
						if (player.getRace() != Race.ERTHEIA)
						{
							htmltext = getNoQuestMsg(player);
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
					case NAVARI:
					{
						if (qs.isCond(2))
						{
							htmltext = "33931-03.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "33931-05.html";
						}
						break;
					}
					case AYANTHE:
					{
						if (qs.isCond(3))
						{
							htmltext = "33942-01.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "33942-10.html";
						}
						else if (qs.isCond(9))
						{
							htmltext = "33942-03.html";
						}
						break;
					}
					case KATALIN:
					{
						if (qs.isCond(2))
						{
							htmltext = "33943-01.html";
						}
						else if (qs.isCond(4) && (!player.getClassId().isMage()))
						{
							htmltext = "33943-09.html";
						}
						else if (qs.isCond(8))
						{
							htmltext = "33943-03.html";
						}
						break;
					}
					case RAYMOND:
					{
						if (qs.isCond(4))
						{
							htmltext = "30289-01.html";
						}
						else if (qs.isCond(7))
						{
							htmltext = "30289-04.html";
						}
						break;
					}
					case TELESHA:
					{
						if (qs.isCond(6))
						{
							htmltext = "telesha.html";
						}
						break;
					}
					case MYSTERIOUS_WIZARD:
					{
						if (qs.isCond(6))
						{
							htmltext = "mysterious.html";
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
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isStarted() && qs.isCond(6) && Util.checkIfInRange(1500, npc, qs.getPlayer(), false))
		{
			int kills = qs.getInt(Integer.toString(SKELETON_ARCHER));
			kills++;
			qs.set(Integer.toString(SKELETON_ARCHER), kills);
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpcString(NpcStringId.KILL_SKELETONS, kills);
			killer.sendPacket(log);
			
			if (kills >= 5)
			{
				addSpawn(TELESHA, npc.getX() + 20, npc.getY() + 20, npc.getZ(), npc.getHeading(), false, 50000);
				showOnScreenMsg(killer, NpcStringId.CHECK_ON_TELESHA, ExShowScreenMessage.TOP_CENTER, 4500);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		final L2PcInstance player = event.getActiveChar();
		if ((player.getLevel() >= MIN_LEVEL) && (player.getRace() == Race.ERTHEIA))
		{
			final QuestState qs = getQuestState(player, false);
			if (qs == null)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10751_WindsOfFateEncounters/Announce.html"));
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
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in combat.", 5000));
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