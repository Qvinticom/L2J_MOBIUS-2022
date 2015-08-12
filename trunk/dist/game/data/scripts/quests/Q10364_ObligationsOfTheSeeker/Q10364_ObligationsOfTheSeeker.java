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
package quests.Q10364_ObligationsOfTheSeeker;

import quests.Q10363_RequestOfTheSeeker.Q10363_RequestOfTheSeeker;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.util.Util;

/**
 * Obligations of the Seeker (10364)
 * @author spider
 */
public class Q10364_ObligationsOfTheSeeker extends Quest
{
	// NPCs
	private static final int CELIN = 33451;
	private static final int WALTER = 33452;
	private static final int DEP = 33453;
	// Monsters
	private static final int KRAPHER = 22996;
	private static final int AVIAN = 22994;
	// Items
	private static final int DIRTY_PIECE_OF_PAPER = 17578;
	private static final int DPP_REQUIRED = 5;
	// Rewards
	private static final int ADENA_REWARD = 550;
	private static final int EXP_REWARD = 95000;
	private static final int SP_REWARD = 22;
	private static final ItemHolder LEATHER_SHOES = new ItemHolder(37, 1);
	private static final ItemHolder HEALING_POTIONS = new ItemHolder(1060, 50);
	// Requirements
	private static final int MIN_LEVEL = 14;
	private static final int MAX_LEVEL = 25;
	
	public Q10364_ObligationsOfTheSeeker()
	{
		super(10364, Q10364_ObligationsOfTheSeeker.class.getSimpleName(), "Obligations of the Seeker");
		addStartNpc(CELIN);
		addTalkId(CELIN, WALTER, DEP);
		addKillId(KRAPHER, AVIAN);
		registerQuestItems(DIRTY_PIECE_OF_PAPER);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
		addCondCompletedQuest(Q10363_RequestOfTheSeeker.class.getSimpleName(), "no_prequest.html");
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
			case "33451-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33451-03.htm": // start quest
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33452-02.html":
			case "33452-03.html":
			{
				htmltext = event;
				break;
			}
			case "33452-04.html":
			{
				if (qs.isCond(1))
				{
					htmltext = event;
					qs.setCond(2);
				}
				break;
			}
			case "33453-02.html":
			case "33453-03.html":
			{
				htmltext = event;
				break;
			}
			case "33453-04.html":
			{
				if (qs.isCond(3))
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					giveItems(player, LEATHER_SHOES);
					giveItems(player, HEALING_POTIONS);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
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
				htmltext = npc.getId() == CELIN ? "33451-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case CELIN:
					{
						htmltext = "33451-04.htm";
						break;
					}
					case WALTER:
					{
						if (qs.isCond(1))
						{
							htmltext = "33452-01.html";
						}
						else
						{
							htmltext = "33452-05.html";
						}
						break;
					}
					case DEP:
					{
						if (qs.isCond(3))
						{
							htmltext = "33453-01.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isCond(2) && Util.checkIfInRange(1500, npc, qs.getPlayer(), false))
		{
			if (qs.getQuestItemsCount(DIRTY_PIECE_OF_PAPER) < DPP_REQUIRED)
			{
				giveItems(qs.getPlayer(), DIRTY_PIECE_OF_PAPER, 1);
			}
			else
			{
				qs.setCond(3);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
