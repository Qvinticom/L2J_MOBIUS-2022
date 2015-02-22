/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q10390_KekropusLetter;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.handler.BypassHandler;
import com.l2jserver.gameserver.handler.IBypassHandler;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.ListenerRegisterType;
import com.l2jserver.gameserver.model.events.annotations.RegisterEvent;
import com.l2jserver.gameserver.model.events.annotations.RegisterType;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Neanrakyr
 */
public class Q10390_KekropusLetter extends Quest implements IBypassHandler
{
	// NPCs
	private static final int DRIKUS = 30505;
	private static final int BATHIS = 30332;
	private static final int GOSTA = 30916;
	private static final int ELI = 33858;
	// Items
	private static final ItemHolder ENCHANT_WEAPON_C = new ItemHolder(951, 3);
	private static final ItemHolder SCROLL_OF_ESCAPE_ALIGATOR_ISLAND = new ItemHolder(37025, 1);
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 21);
	// Level Condition
	private static final int MIN_LEVEL = 40;
	private static final int MAX_LEVEL = 45;
	// Others
	private static final Location TELE_LOCATION = new Location(-12712, 124902, -3133);
	private static final String[] COMMAND =
	{
		"Q10390_Teleport"
	};
	
	public Q10390_KekropusLetter()
	{
		super(10390, Q10390_KekropusLetter.class.getSimpleName(), "Kekropus Letter");
		addStartNpc(DRIKUS);
		addTalkId(DRIKUS, BATHIS, GOSTA, ELI);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "30505-noLevel.html");
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
			case "30505-03.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30332-03.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2);
					htmltext = event;
				}
				break;
			}
			case "30332-05.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3);
					htmltext = event;
				}
				break;
			}
			case "30916-03.html":
			{
				if (qs.isCond(3))
				{
					qs.setCond(4);
					giveItems(player, SCROLL_OF_ESCAPE_ALIGATOR_ISLAND);
					htmltext = event;
				}
				break;
			}
			case "33858-02.html":
			{
				if (qs.isCond(4))
				{
					giveItems(player, ENCHANT_WEAPON_C);
					giveItems(player, STEEL_DOOR_GUILD);
					addExpAndSp(player, 370440, 88);
					showOnScreenMsg(player, NpcStringId.GROW_STRONGER_HERE_UNTIL_YOU_RECEIVE_THE_NEXT_LETTER_FROM_KEKROPUS_AT_LV_46, ExShowScreenMessage.TOP_CENTER, 4500);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "30505-02.htm":
			case "30332-02.html":
			case "30916-02.html":
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCompleted())
		{
			return getAlreadyCompletedMsg(player);
		}
		if (player.getRace() == Race.ERTHEIA)
		{
			return "30505-noErtheia.html";
		}
		
		switch (npc.getId())
		{
			case DRIKUS:
			{
				if (qs.isCreated())
				{
					htmltext = "30505-01.htm";
				}
				else if (qs.isStarted())
				{
					htmltext = "30505-03.html";
				}
				break;
			}
			case BATHIS:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30332-01.html";
						break;
					}
					case 2:
					{
						htmltext = "30332-04.html";
						break;
					}
					case 3:
					{
						htmltext = "30332-05.html";
						break;
					}
				}
				break;
			}
			case GOSTA:
			{
				switch (qs.getCond())
				{
					case 3:
					{
						htmltext = "30916-01.html";
						break;
					}
					case 4:
					{
						htmltext = "30916-03.html";
						break;
					}
				}
				break;
			}
			case ELI:
			{
				if (qs.isCond(4))
				{
					htmltext = "33858-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final L2PcInstance player = event.getActiveChar();
		final int oldLevel = event.getOldLevel();
		final int newLevel = event.getNewLevel();
		
		if (((oldLevel == (newLevel - 1)) && (player.getLevel() >= MIN_LEVEL)) && (player.getLevel() <= MAX_LEVEL) && !(player.getRace() == Race.ERTHEIA))
		{
			final QuestState qs = getQuestState(player, false);
			if (qs == null)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/quests/Q10390_KekropusLetter/Announce.html"));
				player.sendPacket(html);
			}
			return;
		}
	}
	
	@Override
	public boolean useBypass(String command, L2PcInstance player, L2Character bypassOrigin)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) || (player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL) || (player.getRace() == Race.ERTHEIA))
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
			player.teleToLocation(TELE_LOCATION);
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMAND;
	}
}