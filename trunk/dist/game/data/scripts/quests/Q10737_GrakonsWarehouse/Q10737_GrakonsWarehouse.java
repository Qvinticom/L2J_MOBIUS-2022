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
package quests.Q10737_GrakonsWarehouse;

import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;

/**
 * @author Krash
 */
public class Q10737_GrakonsWarehouse extends Quest
{
	// Npcs
	private static final int Grakon = 33947;
	private static final int Katalin = 33943;
	private static final int Ayanthe = 33942;
	// Items
	private static final int Apprentice_Support_Box = 39520;
	private static final int Apprentice_Adventurer_Staff = 7816;
	private static final int Apprentice_Adventurer_Fists = 7819;
	
	// Level Check
	private static final int MIN_LEVEL = 5;
	private static final int MAX_LEVEL = 20;
	
	public Q10737_GrakonsWarehouse()
	{
		super(10737, Q10737_GrakonsWarehouse.class.getSimpleName(), "Grakon's Warehouse");
		addStartNpc(Katalin, Ayanthe);
		addTalkId(Katalin, Ayanthe, Grakon);
		registerQuestItems(Apprentice_Support_Box);
		addCondMinLevel(MIN_LEVEL, "");
		addCondMaxLevel(MAX_LEVEL, "");
		addCondRace(Race.ERTHEIA, "");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "33942-03.htm":
			case "33943-03.htm":
			{
				qs.startQuest();
				qs.setCond(1);
				qs.giveItems(Apprentice_Support_Box, 1);
				htmltext = event;
				break;
			}
			
			case "33947-04.htm":
			{
				if (qs.isCond(1))
				{
					giveAdena(player, 11000, true);
					addExpAndSp(player, 2650, 0);
					qs.giveItems(Apprentice_Adventurer_Fists, 1);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			
			case "33947-08.htm":
			{
				if (qs.isCond(1))
				{
					giveAdena(player, 11000, true);
					addExpAndSp(player, 2650, 0);
					qs.giveItems(Apprentice_Adventurer_Staff, 1);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			
			case "33943-02.htm":
			case "33942-02.htm":
			case "33947-02.htm":
			case "33947-03.htm":
			case "33947-06.htm":
			case "33947-07.htm":
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
		
		switch (npc.getId())
		{
			case Katalin:
				if (qs.isCond(0) && (player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && (player.getClassId() == ClassId.ERTHEIA_FIGHTER))
				{
					htmltext = "33943-01.htm";
				}
				else
				{
					htmltext = "33943-noLevel.htm";
				}
				if (qs.isCond(1))
				{
					htmltext = "33943-03.htm";
				}
				if ((player.getLevel() < MIN_LEVEL) && (player.getLevel() > MAX_LEVEL) && (player.getRace() != Race.ERTHEIA))
				{
					htmltext = "33943-noLevel.htm";
				}
				break;
			
			case Ayanthe:
				if (qs.isCond(0) && (player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && (player.getClassId() == ClassId.ERTHEIA_WIZARD))
				{
					htmltext = "33942-01.htm";
				}
				else
				{
					htmltext = "33942-noLevel.htm";
				}
				if (qs.isCond(1))
				{
					htmltext = "33942-03.htm";
				}
				if ((player.getLevel() < MIN_LEVEL) && (player.getLevel() > MAX_LEVEL) && (player.getRace() != Race.ERTHEIA))
				{
					htmltext = "33942-noLevel.htm";
				}
				break;
			
			case Grakon:
				if (qs.isCond(1) && qs.hasQuestItems(Apprentice_Support_Box))
				{
					if (player.getClassId() == ClassId.ERTHEIA_FIGHTER)
					{
						htmltext = "33947-01.htm";
					}
					else if (player.getClassId() == ClassId.ERTHEIA_WIZARD)
					{
						htmltext = "33947-05.htm";
					}
				}
				break;
		}
		
		return htmltext;
	}
}