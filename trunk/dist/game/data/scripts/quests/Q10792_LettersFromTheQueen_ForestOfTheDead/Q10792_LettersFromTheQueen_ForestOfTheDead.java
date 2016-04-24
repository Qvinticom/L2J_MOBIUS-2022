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
package quests.Q10792_LettersFromTheQueen_ForestOfTheDead;

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
import com.l2jmobius.gameserver.model.base.ClassId;
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
 * Letters from the Queen: Forest of the Dead (10792)
 * @URL https://l2wiki.com/Letters_from_the_Queen:_Forest_of_the_Dead
 * @author Gigi
 */
public class Q10792_LettersFromTheQueen_ForestOfTheDead extends Quest implements IBypassHandler
{
	// NPCs
	private static final int INNOCENTIN = 31328;
	private static final int HATUBA = 33849;
	// Items
	private static final ItemHolder SCROLL_OF_ESCAPE_FOREST_OF_THE_DEAD = new ItemHolder(37031, 1);
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 91);
	private static final ItemHolder EWA = new ItemHolder(729, 1);
	// Reward
	private static final int EXP_REWARD = 942690;
	private static final int SP_REWARD = 226;
	// Misc
	private static final int MIN_LEVEL = 65;
	private static final int MAX_LEVEL = 69;
	// Teleport
	private static final Location TP_LOC = new Location(36599, -49238, -1133);
	private static final String[] TP_COMMANDS =
	{
		"Q10792_Teleport"
	};
	
	public Q10792_LettersFromTheQueen_ForestOfTheDead()
	{
		super(10792, Q10792_LettersFromTheQueen_ForestOfTheDead.class.getSimpleName(), "Letters from the Queen: ForestOfTheDead");
		addStartNpc(INNOCENTIN);
		addTalkId(INNOCENTIN, HATUBA);
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
		addCondClassId(ClassId.CLOUD_BREAKER, "no_quest.html");
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
			case "31328-02.htm":
			case "31328-04.html":
			case "33849-02.htm":
			{
				htmltext = event;
				break;
			}
			case "31328-03.htm":
			{
				qs.startQuest();
				if (getQuestItemsCount(player, SCROLL_OF_ESCAPE_FOREST_OF_THE_DEAD.getId()) < 1)
				{
					giveItems(player, SCROLL_OF_ESCAPE_FOREST_OF_THE_DEAD);
					player.sendPacket(new ExShowScreenMessage("Try using the teleport scroll Innocentin gave you to go to Forest of the Dead.", 10000));
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "33849-03.html":
			{
				if (qs.isCond(2))
				{
					showOnScreenMsg(player, NpcStringId.GROW_STRONGER_HERE_UNTIL_YOU_RECEIVE_THE_NEXT_LETTER_FROM_QUEEN_NAVARI_AT_LV_70, ExShowScreenMessage.TOP_CENTER, 5000);
					giveItems(player, STEEL_DOOR_GUILD);
					giveItems(player, EWA);
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
					case INNOCENTIN:
					{
						htmltext = "31328-01.html";
						break;
					}
					
					case HATUBA:
					{
						if ((player.getRace() != Race.ERTHEIA))
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
					case INNOCENTIN:
					{
						if (qs.isCond(1))
						{
							playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
							htmltext = "31328-03.htm";
						}
						else if (qs.isCond(2))
						{
							htmltext = "31328-04.html";
						}
						break;
					}
					case HATUBA:
					{
						if (qs.isCond(2))
						{
							htmltext = "33849-01.html";
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
		if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && (player.getRace() == Race.ERTHEIA) && (player.getClassId() == ClassId.CLOUD_BREAKER))
		{
			final QuestState qs = getQuestState(player, false);
			if (qs == null)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10792_LettersFromTheQueen_ForestOfTheDead/Announce.html"));
				player.sendPacket(html);
			}
		}
	}
	
	@Override
	public boolean useBypass(String command, L2PcInstance player, L2Character bypassOrigin)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) || (player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL) || ((player.getRace() != Race.ERTHEIA) && (player.getClassId() != ClassId.CLOUD_BREAKER)))
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