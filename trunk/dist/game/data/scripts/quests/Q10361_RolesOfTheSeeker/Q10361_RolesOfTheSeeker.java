/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q10361_RolesOfTheSeeker;

import quests.Q10330_ToTheRuinsOfYeSagira.Q10330_ToTheRuinsOfYeSagira;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.ListenerRegisterType;
import com.l2jserver.gameserver.model.events.annotations.RegisterEvent;
import com.l2jserver.gameserver.model.events.annotations.RegisterType;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerCreate;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.model.zone.L2ZoneType;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Roles of the Seeker (10361)
 * @author spider
 */
public class Q10361_RolesOfTheSeeker extends Quest
{
	// NPCs
	private static final int LAKCIS = 32977;
	private static final int CHESHA = 33449;
	// Rewards
	private static final int ADENA_REWARD = 34000;
	private static final int EXP_REWARD = 35000;
	private static final int SP_REWARD = 5;
	// Others
	private static final int YE_SAGIRA_RUINS_PRESENTATION_MOVIE_ZONE = 10361;
	private static final String MOVIE_VAR = "Ye_Sagira_Ruins_movie";
	private static final int SI_ILLUSION_03_QUE = 103; // movie id
	private static final int MIN_LEVEL = 10;
	private static final int MAX_LEVEL = 20;
	
	public Q10361_RolesOfTheSeeker()
	{
		super(10361, Q10361_RolesOfTheSeeker.class.getSimpleName(), "Roles of the Seeker");
		addStartNpc(LAKCIS);
		addTalkId(LAKCIS, CHESHA);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
		addEnterZoneId(YE_SAGIRA_RUINS_PRESENTATION_MOVIE_ZONE);
		addCondCompletedQuest(Q10330_ToTheRuinsOfYeSagira.class.getSimpleName(), "no_prequest.html");
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
			case "32977-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32977-03.htm":
			{
				qs.startQuest();
				showOnScreenMsg(player, NpcStringId.ENTER_THE_RUINS_OF_YE_SAGIRA_THROUGH_THE_YE_SAGIRA_TELEPORT_DEVICE, ExShowScreenMessage.TOP_CENTER, 5000);
				htmltext = event;
				break;
			}
			case "33449-02.html":
			{
				htmltext = event;
				break;
			}
			case "33449-03.html":
			{
				if (qs.isCond(1))
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(false, true);
				}
				htmltext = event;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = npc.getId() == LAKCIS ? "32977-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == LAKCIS ? "32977-04.htm" : "33449-01.html";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.isPlayer())
		{
			final L2PcInstance player = character.getActingPlayer();
			
			if (player.getVariables().getBoolean(MOVIE_VAR, false))
			{
				if (player.getLevel() <= MAX_LEVEL)
				{
					final QuestState qs = getQuestState(player, false);
					if ((qs != null) && qs.isStarted())
					{
						player.showQuestMovie(SI_ILLUSION_03_QUE);
					}
				}
				player.getVariables().remove(MOVIE_VAR);
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_CREATE)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerCreate(OnPlayerCreate event)
	{
		event.getActiveChar().getVariables().set(MOVIE_VAR, true);
	}
}
