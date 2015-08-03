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
package quests.Q10336_DividedSakumKanilov;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.util.Util;

/**
 * Divided Sakum, Kanilov (10336)
 * @author spider
 */
public class Q10336_DividedSakumKanilov extends Quest
{
	// NPCs
	private static final int ZENATH = 33509;
	private static final int ADV_GUILDSMAN = 31795;
	// Monster
	private static final int KANILOV = 27451;
	// Items
	private static final int SAKUMS_SKETCH_A = 17584;
	private static final int MIN_LEVEL = 27;
	private static final int MAX_LEVEL = 40;
	// Rewards
	private static final int ADENA_REWARD = 1000;
	private static final int EXP_REWARD = 500000;
	private static final int SP_REWARD = 120;
	private static final ItemHolder SCROLL_EW_D = new ItemHolder(955, 3); // 3 scrolls on retail server
	
	public Q10336_DividedSakumKanilov()
	{
		super(10336, Q10336_DividedSakumKanilov.class.getSimpleName(), "Divided Sakum, Kanilov");
		addStartNpc(ZENATH);
		addTalkId(ZENATH, ADV_GUILDSMAN);
		addKillId(KANILOV);
		registerQuestItems(SAKUMS_SKETCH_A);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String texthtml = null;
		switch (event)
		{
			case "33509-02.htm":
			{
				texthtml = event;
				break;
			}
			case "33509-03.htm": // start the quest
			{
				qs.startQuest();
				texthtml = event;
				break;
			}
			case "31795-02.html":
			{
				texthtml = event;
				break;
			}
			case "31795-03.html": // end quest, take sketch, give rewards
			{
				giveAdena(player, ADENA_REWARD, true);
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				rewardItems(player, SCROLL_EW_D);
				qs.exitQuest(false, true);
				texthtml = event;
				break;
			}
		}
		return texthtml;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String texthtml = null;
		switch (qs.getState())
		{
			case State.CREATED:
			{
				texthtml = npc.getId() == ZENATH ? "33509-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case ZENATH:
					{
						if (qs.isCond(1))
						{
							texthtml = "33509-03.htm";
						}
						else if (qs.isCond(2)) // report defeated kanilov, get the sketch
						{
							giveItems(player, SAKUMS_SKETCH_A, 1);
							qs.setCond(3);
							texthtml = "33509-05.html";
						}
						else
						{
							texthtml = "33509-06.html";
						}
						break;
					}
					case ADV_GUILDSMAN:
					{
						if (qs.isCond(3)) // start end quest dialogs
						{
							texthtml = "31795-01.html";
						}
						else
						{
							texthtml = getNoQuestMsg(player);
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				texthtml = npc.getId() == ADV_GUILDSMAN ? "31795-04.html" : getAlreadyCompletedMsg(player);
				break;
			}
		}
		return texthtml;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false); // kill counts independent on party/no party
		if ((qs != null) && qs.isStarted() && qs.isCond(1) && (Util.checkIfInRange(1500, npc, qs.getPlayer(), false)))
		{
			qs.setCond(2);
		}
		return super.onKill(npc, killer, isSummon);
	}
}