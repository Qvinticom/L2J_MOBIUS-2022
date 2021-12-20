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
package quests.Q10885_SaviorsPathDiscovery;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerBypass;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerPressTutorialMark;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;

/**
 * Savior's Path - Discovery (10885)
 * @URL https://l2wiki.com/Savior%27s_Path_-_Discovery
 * @author CostyKiller
 */
public class Q10885_SaviorsPathDiscovery extends Quest
{
	// NPCs
	private static final int LEONA_BLACKBIRD = 34425;
	private static final int ELIKIA = 34057;
	// Items
	private static final int LEONA_BLACKBIRDS_MESSAGE = 48545;
	// Misc
	private static final int MIN_LEVEL = 103;
	private final NpcStringId _startMessage = NpcStringId.YOU_VE_GOT_A_MESSAGE_FROM_LEONA_BLACKBIRD_NCLICK_THE_QUESTION_MARK_ICON_TO_READ;
	
	public Q10885_SaviorsPathDiscovery()
	{
		super(10885);
		addTalkId(LEONA_BLACKBIRD, ELIKIA);
		addCondMinLevel(MIN_LEVEL, "34425-00.html");
		registerQuestItems(LEONA_BLACKBIRDS_MESSAGE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		
		switch (event)
		{
			case "34057-03.html":
			{
				if (qs.isCond(1) && hasQuestItems(player, LEONA_BLACKBIRDS_MESSAGE))
				{
					qs.setCond(2);
				}
				htmltext = event;
				break;
			}
			case "34057-05.html":
			{
				if (qs.isCond(2) && hasQuestItems(player, LEONA_BLACKBIRDS_MESSAGE))
				{
					qs.setCond(3);
				}
				htmltext = event;
				break;
			}
			case "34425-03.html":
			{
				if (qs.isCond(3) && hasQuestItems(player, LEONA_BLACKBIRDS_MESSAGE))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						addExpAndSp(player, 906387492, 906387);
						qs.exitQuest(false, true);
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
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
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case LEONA_BLACKBIRD:
					{
						if (qs.isCond(3) && hasQuestItems(player, LEONA_BLACKBIRDS_MESSAGE))
						{
							htmltext = "34425-01.htm";
						}
						else
						{
							htmltext = "34425-02.html";
						}
						break;
					}
					case ELIKIA:
					{
						if (qs.isCond(1))
						{
							if (hasQuestItems(player, LEONA_BLACKBIRDS_MESSAGE))
							{
								htmltext = "34057-01.htm";
							}
							else
							{
								htmltext = "34057-02.html";
							}
						}
						else if (qs.isCond(2) && hasQuestItems(player, LEONA_BLACKBIRDS_MESSAGE))
						{
							htmltext = "34057-04.htm";
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
	
	@RegisterEvent(EventType.ON_PLAYER_PRESS_TUTORIAL_MARK)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerPressTutorialMark(OnPlayerPressTutorialMark event)
	{
		final Player player = event.getPlayer();
		if ((event.getMarkId() == getId()) && canStartQuest(player))
		{
			final String html = getHtm(player, "Message.html");
			final QuestState qs = getQuestState(player, true);
			qs.startQuest();
			player.sendPacket(new TutorialShowHtml(html));
			giveItems(player, LEONA_BLACKBIRDS_MESSAGE, 1);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_BYPASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerBypass(OnPlayerBypass event)
	{
		if (event.getCommand().startsWith("Quest Q10885_SaviorsPathDiscovery "))
		{
			final String html = onAdvEvent(event.getCommand().substring(34), null, event.getPlayer());
			event.getPlayer().sendPacket(TutorialCloseHtml.STATIC_PACKET);
			showResult(event.getPlayer(), html);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final Player player = event.getPlayer();
		final QuestState qs = getQuestState(player, false);
		if ((qs == null) && (event.getOldLevel() < event.getNewLevel()) && canStartQuest(player))
		{
			player.sendPacket(new TutorialShowQuestionMark(getId(), 0));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
			showOnScreenMsg(player, _startMessage, ExShowScreenMessage.TOP_CENTER, 10000);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		final QuestState qs = getQuestState(player, false);
		if ((qs == null) && (player.getLevel() > MIN_LEVEL))
		{
			player.sendPacket(new TutorialShowQuestionMark(getId(), 0));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
			showOnScreenMsg(player, _startMessage, ExShowScreenMessage.TOP_CENTER, 10000);
		}
	}
	
	@Override
	public void onQuestAborted(Player player)
	{
		final QuestState qs = getQuestState(player, true);
		qs.startQuest();
		giveItems(player, LEONA_BLACKBIRDS_MESSAGE, 1);
		player.sendPacket(SystemMessageId.THIS_QUEST_CANNOT_BE_DELETED);
	}
}
