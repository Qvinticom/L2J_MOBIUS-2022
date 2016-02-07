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
package quests.Q10393_KekropusLetter_AClueCompleted;

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
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Neanrakyr
 */
public class Q10393_KekropusLetter_AClueCompleted extends Quest implements IBypassHandler
{
	// NPCs
	private static final int FLUTTER = 30677;
	private static final int KELIOS = 33862;
	// Items
	private static final ItemHolder SCROLL_OF_ESCAPE_OUTLAW_FOREST = new ItemHolder(37026, 1);
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 15);
	private static final ItemHolder ENCHANT_ARMOR_C = new ItemHolder(952, 4);
	// Requirements
	private static final int MIN_LEVEL = 46;
	private static final int MAX_LEVEL = 51;
	// Teleport
	private static final Location TELE_LOCATION = new Location(83697, 55446, -1512);
	private static final String[] COMMAND =
	{
		"Q10393_Teleport"
	};
	
	public Q10393_KekropusLetter_AClueCompleted()
	{
		super(10393, Q10393_KekropusLetter_AClueCompleted.class.getSimpleName(), "Krekopus Letter: A Clue Completed");
		addStartNpc(FLUTTER);
		addTalkId(FLUTTER, KELIOS);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "30677-noLevel.html");
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
			case "30677-02.html":
			{
				qs.startQuest();
				giveItems(player, SCROLL_OF_ESCAPE_OUTLAW_FOREST);
				htmltext = event;
				break;
			}
			case "33862-02.html":
			{
				if (qs.isCond(1))
				{
					giveItems(player, STEEL_DOOR_GUILD);
					giveItems(player, ENCHANT_ARMOR_C);
					addExpAndSp(player, 483840, 116);
					showOnScreenMsg(player, NpcStringId.GROW_STRONGER_HERE_UNTIL_YOU_RECEIVE_THE_NEXT_LETTER_FROM_KEKROPUS_AT_LV_52, ExShowScreenMessage.TOP_CENTER, 4500);
					qs.exitQuest(false, true);
					htmltext = event;
				}
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
			return "30677-noErtheia.html";
		}
		
		switch (npc.getId())
		{
			case FLUTTER:
			{
				if (qs.isCreated())
				{
					htmltext = "30677-01.htm";
				}
				else if (qs.isStarted())
				{
					htmltext = "30677-03.html";
				}
				break;
			}
			case KELIOS:
			{
				if (qs.isCond(1))
				{
					htmltext = "33862-01.html";
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
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		final L2PcInstance player = event.getActiveChar();
		if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && (player.getRace() != Race.ERTHEIA))
		{
			final QuestState qs = getQuestState(player, false);
			if (qs == null)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10393_KekropusLetter_AClueCompleted/Announce.html"));
				player.sendPacket(html);
			}
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