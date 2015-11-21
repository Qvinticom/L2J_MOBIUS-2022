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
package quests.Q10756_AnInterdimensionalDraft;

import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;

/**
 * @author Neanrakyr
 */
public class Q10756_AnInterdimensionalDraft extends Quest
{
	// NPCs
	private static final int PIO = 33963;
	// Items
	private static final int UNWORLDLY_WIND = 39493; // Unworldly Wind
	private static final ItemHolder STEEL_DOOR_GUILD_COIN = new ItemHolder(37045, 8); // Steel Door Guild Coin
	// Mobs
	private static final int[] MOBS =
	{
		20078, // Whispering Wind
		21023, // Sobbing Wind
		21024, // Babbling Wind
		21025, // Giggling Wind
		21026, // Singing Wind
		23414, // Windima
		23415, // Windima Feri
		23416, // Windima Resh
	};
	// Misc
	private static final int MIN_LEVEL = 20;
	private static final int MAX_LEVEL = 107;
	
	public Q10756_AnInterdimensionalDraft()
	{
		super(10756, Q10756_AnInterdimensionalDraft.class.getSimpleName(), "An Interdimensional Draft");
		addStartNpc(PIO);
		addTalkId(PIO);
		registerQuestItems(UNWORLDLY_WIND);
		addKillId(MOBS);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33963-08.html");
		addCondRace(Race.ERTHEIA, "33963-08.html");
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
			case "33963-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33963-02.htm":
			case "33963-03.htm":
			case "33963-04.htm":
			case "33963-06.html":
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
		
		if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		
		switch (npc.getId())
		{
			case PIO:
			{
				if (qs.isCreated())
				{
					htmltext = "33963-01.htm";
				}
				else if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "33963-05.htm";
							break;
						}
						case 2:
						{
							if (qs.isCond(2) && (qs.getQuestItemsCount(UNWORLDLY_WIND) >= 30))
							{
								qs.takeItems(UNWORLDLY_WIND, 30);
								giveItems(player, STEEL_DOOR_GUILD_COIN);
								addExpAndSp(player, 174222, 41);
								qs.exitQuest(false, true);
								htmltext = "33963-07.html";
							}
							break;
						}
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
		final QuestState qs = getQuestState(killer, false);
		
		if ((qs != null) && qs.isCond(1))
		{
			if (giveItemRandomly(killer, npc, UNWORLDLY_WIND, 1, 30, 1.0, true))
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}