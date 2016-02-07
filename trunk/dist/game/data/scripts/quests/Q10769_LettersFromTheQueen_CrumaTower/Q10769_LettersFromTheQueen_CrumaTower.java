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
package quests.Q10769_LettersFromTheQueen_CrumaTower;

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
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Letters From The Queen: Cruma Tower part1
 * @author Gigi
 */
public class Q10769_LettersFromTheQueen_CrumaTower extends Quest implements IBypassHandler
{
	// NPCs
	private static final int SYLVAIN = 30070;
	private static final int LORAIN = 30673;
	// Items
	private static final ItemHolder SCROLL_OF_ESCAPE_CRUMA_TOWER = new ItemHolder(39594, 1);
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 11);
	private static final ItemHolder EAC = new ItemHolder(952, 1);
	// Reward
	private static final int EXP_REWARD = 370440;
	private static final int SP_REWARD = 88;
	// Misc
	private static final int MIN_LEVEL = 40;
	private static final int MAX_LEVEL = 45;
	// Teleport
	private static final Location TP_LOC = new Location(16218, 142300, -2700);
	private static final String[] TP_COMMANDS =
	{
		"Q10769_Teleport"
	};
	
	public Q10769_LettersFromTheQueen_CrumaTower()
	{
		super(10769, Q10769_LettersFromTheQueen_CrumaTower.class.getSimpleName(), "Letters from the Queen: CrumaTower");
		addStartNpc(SYLVAIN);
		addTalkId(SYLVAIN, LORAIN);
		addCondRace(Race.ERTHEIA, "no.html");
		addCondMinLevel(MIN_LEVEL, "noLevel.html");
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
			case "30070-03.html": // start the quest
			{
				qs.startQuest();
				if (qs.getQuestItemsCount(SCROLL_OF_ESCAPE_CRUMA_TOWER.getId()) < 1)
				{
					qs.giveItems(SCROLL_OF_ESCAPE_CRUMA_TOWER);
					player.sendPacket(new ExShowScreenMessage("Try using the teleport scroll Sylvain gave you to go to Cruma Tower.", 10000));
				}
				htmltext = event;
				break;
			}
			case "30673-03.html":
			{
				if (qs.isCond(2))
				{
					player.sendPacket(new ExShowScreenMessage("Grow stronger here until you receive the next letter from Queen Navari at Lv. 46!", 10000));
					qs.giveItems(STEEL_DOOR_GUILD);
					qs.giveItems(EAC);
					qs.addExpAndSp(EXP_REWARD, SP_REWARD);
					qs.exitQuest(false, true);
				}
				htmltext = event;
				break;
			}
			case "30070-02.html":
			case "30673-02.htm":
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
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
		if (player.getRace() != Race.ERTHEIA)
		{
			return "30070-no.html";
		}
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case SYLVAIN:
					{
						htmltext = "30070-01.html";
						break;
					}
					case LORAIN:
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
					case SYLVAIN:
					{
						if (qs.isCond(1))
						{
							qs.setCond(2, true);
							htmltext = "30070-03.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "30070-04.html";
						}
						break;
					}
					case LORAIN:
					{
						if (qs.isCond(2))
						{
							htmltext = "30673-01.html";
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
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10769_LettersFromTheQueen_CrumaTower/Announce.html"));
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