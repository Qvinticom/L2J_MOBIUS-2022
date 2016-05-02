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
package quests.Q10798_LettersFromTheQueen_DragonValley;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.enums.QuestSound;
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
 * Letters from the Queen: Dragon Valley (10798)
 * @URL https://l2wiki.com/Letters_from_the_Queen:_Dragon_Valley
 * @author Gigi
 */
public class Q10798_LettersFromTheQueen_DragonValley extends Quest implements IBypassHandler
{
	// NPCs
	private static final int MAXIMILIAN = 30120;
	private static final int NAMO = 33973;
	// Items
	private static final ItemHolder SCROLL_OF_ESCAPE_DRAGON_VALLEY = new ItemHolder(39587, 1);
	private static final ItemHolder SCROLL_OF_ESCAPE_TOWN_OF_GIRAN = new ItemHolder(39586, 1);
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 182);
	private static final ItemHolder EWS = new ItemHolder(959, 1);
	// Reward
	private static final int EXP_REWARD = 1277640;
	private static final int SP_REWARD = 306;
	// Misc
	private static final int MIN_LEVEL = 76;
	private static final int MAX_LEVEL = 84;
	// Teleport
	private static final Location TP_LOC = new Location(86674, 148630, -3401);
	private static final String[] TP_COMMANDS =
	{
		"Q10798_Teleport"
	};
	
	public Q10798_LettersFromTheQueen_DragonValley()
	{
		super(10798, Q10798_LettersFromTheQueen_DragonValley.class.getSimpleName(), "Letters from the Queen: Dragon Valley");
		addStartNpc(MAXIMILIAN);
		addTalkId(MAXIMILIAN, NAMO);
		registerQuestItems(SCROLL_OF_ESCAPE_DRAGON_VALLEY.getId(), SCROLL_OF_ESCAPE_TOWN_OF_GIRAN.getId());
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
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
			case "30120-02.html":
			case "33973-02.html":
			{
				htmltext = event;
				break;
			}
			case "close":
			{
				return null;
			}
			case "30120-03.html":
			{
				qs.startQuest();
				if (getQuestItemsCount(player, SCROLL_OF_ESCAPE_DRAGON_VALLEY.getId()) < 1)
				{
					giveItems(player, SCROLL_OF_ESCAPE_DRAGON_VALLEY);
					player.sendPacket(new ExShowScreenMessage("Try using the teleport scroll Innocentin gave you to go to Dragon Valley.", 10000));
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "33973-03.html":
			{
				if (qs.isCond(2))
				{
					showOnScreenMsg(player, NpcStringId.YOU_HAVE_FINISHED_ALL_OF_QUEEN_NAVARI_S_LETTERS_GROW_STRONGER_HERE_UNTIL_YOU_RECEIVE_LETTERS_FROM_A_MINSTREL_AT_LV_85, ExShowScreenMessage.TOP_CENTER, 8000);
					giveItems(player, STEEL_DOOR_GUILD);
					giveItems(player, EWS);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					playSound(player, QuestSound.ITEMSOUND_QUEST_FINISH);
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
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
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
					case MAXIMILIAN:
					{
						htmltext = "30120-01.html";
						break;
					}
					
					case NAMO:
					{
						if ((player.getRace() != Race.ERTHEIA) || (qs.isCreated()))
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
					case MAXIMILIAN:
					{
						if (qs.isCond(2))
						{
							htmltext = "30120-04.html";
						}
						break;
					}
					case NAMO:
					{
						if (qs.isCond(2))
						{
							htmltext = "33973-01.html";
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
		final QuestState qs = getQuestState(player, false);
		if ((qs == null) && (player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && (player.getRace() == Race.ERTHEIA))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
			html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10798_LettersFromTheQueen_DragonValley/Announce.html"));
			player.sendPacket(html);
			if (getQuestItemsCount(player, SCROLL_OF_ESCAPE_TOWN_OF_GIRAN.getId()) < 1)
			{
				giveItems(player, SCROLL_OF_ESCAPE_TOWN_OF_GIRAN);
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
			if (getQuestItemsCount(player, SCROLL_OF_ESCAPE_TOWN_OF_GIRAN.getId()) > 0)
			{
				takeItem(player, SCROLL_OF_ESCAPE_TOWN_OF_GIRAN);
			}
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return TP_COMMANDS;
	}
}