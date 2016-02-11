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
package quests.Q10760_LettersFromTheQueen_OrcBarracs;

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
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Letters From the Queen - Orc Barracs (10760)
 * @author Stayway
 */
public class Q10760_LettersFromTheQueen_OrcBarracs extends Quest implements IBypassHandler
{
	// NPCs
	private static final int LEVIAN = 30037;
	private static final int PIOTUR = 30597;
	// Items
	private static final ItemHolder SCROLL_OF_ESCAPE_ORC_BARRACS = new ItemHolder(39487, 1);
	private static final ItemHolder SCROLL_OF_ESCAPE_GLUDIN_VILLAGE = new ItemHolder(39491, 1);
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 5);
	// Requirements
	private static final int MIN_LEVEL = 30;
	private static final int MAX_LEVEL = 39;
	// Rewards
	private static final int EXP_REWARD = 242760;
	private static final int SP_REWARD = 58;
	// Teleport
	private static final Location TP_LOC = new Location(-79816, 150828, -3040);
	private static final String[] TP_COMMANDS =
	{
		"Q10760_Teleport"
	};
	
	public Q10760_LettersFromTheQueen_OrcBarracs()
	{
		super(10760, Q10760_LettersFromTheQueen_OrcBarracs.class.getSimpleName(), "Letters from the Queen: Orc Barracs");
		addStartNpc(LEVIAN, PIOTUR);
		addTalkId(LEVIAN, PIOTUR);
		registerQuestItems(SCROLL_OF_ESCAPE_GLUDIN_VILLAGE.getId(), SCROLL_OF_ESCAPE_ORC_BARRACS.getId());
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "noLevel.html");
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
			case "30037-03.html":
			{
				qs.startQuest();
				
				if (getQuestItemsCount(player, SCROLL_OF_ESCAPE_ORC_BARRACS.getId()) < 1)
				{
					giveItems(player, SCROLL_OF_ESCAPE_ORC_BARRACS);
					showOnScreenMsg(player, NpcStringId.TRY_USING_THE_TELEPORT_SCROLL_LEVIAN_GAVE_YOU_TO_GO_TO_ORC_BARRACKS, ExShowScreenMessage.TOP_CENTER, 4500);
				}
				htmltext = event;
				break;
			}
			case "30597-03.html":
			{
				if (qs.isCond(1))
				{
					giveItems(player, STEEL_DOOR_GUILD);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					showOnScreenMsg(player, NpcStringId.TRY_TALKING_TO_VORBOS_BY_THE_WELL_NYOU_CAN_RECEIVE_QUEEN_NAVARI_S_NEXT_LETTER_AT_LV_40, ExShowScreenMessage.TOP_CENTER, 4500);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "30037-02.html":
			case "30597-02.html":
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
					case LEVIAN:
					{
						htmltext = "30037-01.htm";
						break;
					}
					case PIOTUR:
					{
						if (player.getRace() != Race.ERTHEIA)
						{
							htmltext = getNoQuestMsg(player);
						}
						else if (qs.isCreated())
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
					case LEVIAN:
					{
						if (qs.isCond(1))
						{
							htmltext = "30037-04.html";
						}
						break;
					}
					case PIOTUR:
					{
						if (qs.isCond(1))
						{
							htmltext = "30597-01.html";
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
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		final L2PcInstance player = event.getActiveChar();
		if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && (player.getRace() == Race.ERTHEIA))
		{
			final QuestState qs = getQuestState(player, false);
			if (qs == null)
			{
				if (player.getInventory().getInventoryItemCount(SCROLL_OF_ESCAPE_GLUDIN_VILLAGE.getId(), -1) == 0)
				{
					giveItems(player, SCROLL_OF_ESCAPE_GLUDIN_VILLAGE);
				}
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10760_LettersFromTheQueen_OrcBarracs/Announce.html"));
				player.sendPacket(html);
			}
		}
	}
	
	@Override
	public boolean useBypass(String command, L2PcInstance player, L2Character bypassOrigin)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) || (player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL) || (player.getRace() != Race.ERTHEIA))
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