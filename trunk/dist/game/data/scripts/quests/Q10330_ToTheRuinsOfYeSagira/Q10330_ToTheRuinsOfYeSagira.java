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
package quests.Q10330_ToTheRuinsOfYeSagira;

import quests.Q10329_BackupSeekers.Q10329_BackupSeekers;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author Gladicek
 */
public class Q10330_ToTheRuinsOfYeSagira extends Quest
{
	// Npcs
	private static final int ATRAN = 33448;
	private static final int LAKCIS = 32977;
	
	// Items
	private static final ItemHolder LEATHER_SHIRT = new ItemHolder(22, 1);
	private static final ItemHolder LEATHER_PANTS = new ItemHolder(29, 1);
	
	// Level Condition
	private static final int MIN_LEVEL = 8;
	private static final int MAX_LEVEL = 20;
	
	public Q10330_ToTheRuinsOfYeSagira()
	{
		super(10330, Q10330_ToTheRuinsOfYeSagira.class.getSimpleName(), "To the Ruins of Ye Sagira");
		addStartNpc(ATRAN);
		addTalkId(ATRAN, LAKCIS);
		addCondMinLevel(MIN_LEVEL, "33448-05.htm");
		addCondMaxLevel(MAX_LEVEL, "33448-05.htm");
		addCondCompletedQuest(Q10329_BackupSeekers.class.getSimpleName(), "33448-05.htm");
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
			case "33448-02.htm":
			case "32977-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33448-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32977-03.htm":
			{
				if (qs.isStarted())
				{
					showOnScreenMsg(player, NpcStringId.ARMOR_HAS_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
					giveAdena(player, 620, true);
					giveItems(player, LEATHER_SHIRT);
					giveItems(player, LEATHER_PANTS);
					addExpAndSp(player, 23000, 5);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
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
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = npc.getId() == ATRAN ? "33448-01.htm" : "32977-04.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == ATRAN ? "33448-04.htm" : "32977-01.htm";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == ATRAN ? "33448-06.htm" : "32977-05.htm";
				break;
			}
		}
		return htmltext;
	}
}