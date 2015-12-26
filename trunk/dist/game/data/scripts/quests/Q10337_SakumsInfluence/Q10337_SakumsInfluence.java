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
package quests.Q10337_SakumsInfluence;

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jserver.gameserver.util.Util;

/**
 * Sakum's Influence (10337)
 * @author spider
 */
public class Q10337_SakumsInfluence extends Quest
{
	// NPCs
	private static final int ADV_GUILDSMAN = 31795;
	private static final int SILVAN = 33178;
	private static final int LEF = 33510;
	// Monsters
	private static final int SKELETON_WARRIOR = 23022;
	private static final int RUIN_IMP = 20506;
	private static final int RUIN_IMP_ELDER = 20507;
	private static final int RUIN_BAT = 23023;
	private static final int SCAVENGER_BAT = 20411;
	private static final int BAT = 27458; // for ruin bat & scavenger bat counter(client counter requires BAT id)
	private static final Map<Integer, Integer> MOBS_REQUIRED = new HashMap<>();
	{
		MOBS_REQUIRED.put(SKELETON_WARRIOR, 10);
		MOBS_REQUIRED.put(RUIN_IMP, 20); // imp elder same counter
		MOBS_REQUIRED.put(BAT, 25); // & scavenger bat same counter
	}
	// Rewards
	private static final int ADENA_REWARD = 1030;
	private static final int EXP_REWARD = 650000;
	private static final int SP_REWARD = 156;
	// Others
	private static final int MIN_LEVEL = 28;
	private static final int MAX_LEVEL = 40;
	
	public Q10337_SakumsInfluence()
	{
		super(10337, Q10337_SakumsInfluence.class.getSimpleName(), "Sakum's Influence");
		addStartNpc(ADV_GUILDSMAN);
		addTalkId(ADV_GUILDSMAN, SILVAN, LEF);
		addKillId(SKELETON_WARRIOR, RUIN_IMP, RUIN_IMP_ELDER, RUIN_BAT, SCAVENGER_BAT);
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
		
		String htmltext = null;
		switch (event)
		{
			case "31795-02.htm":
			{
				htmltext = event;
				break;
			}
			case "31795-03.htm": // start the quest
			{
				qs.startQuest();
				qs.setCond(2);
				qs.setCond(1); // arrow hack
				htmltext = event;
				break;
			}
			case "33178-02.html":
			{
				htmltext = event;
				break;
			}
			case "33178-03.html": // step 2 - go kill mobs
			{
				qs.setCond(2);
				qs.set(Integer.toString(SKELETON_WARRIOR), 0); // db vars for mob counter
				qs.set(Integer.toString(RUIN_IMP), 0);
				qs.set(Integer.toString(BAT), 0);
				htmltext = event;
				break;
			}
			case "33510-02.html": // exit quest, give rewards, remove vars from db
			{
				if (qs.isCond(3))
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.unset(Integer.toString(SKELETON_WARRIOR));
					qs.unset(Integer.toString(RUIN_IMP));
					qs.unset(Integer.toString(BAT));
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
		switch (qs.getState())
		{
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case ADV_GUILDSMAN:
					{
						htmltext = "31795-01.htm";
						break;
					}
					case SILVAN:
					{
						htmltext = getNoQuestMsg(player);
						break;
					}
					case LEF:
					{
						htmltext = getNoQuestMsg(player);
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case ADV_GUILDSMAN:
					{
						htmltext = "31795-04.htm";
						break;
					}
					case SILVAN:
					{
						if (qs.isCond(1))
						{
							htmltext = "33178-01.html";
						}
						else
						{
							htmltext = "33178-03.html";
						}
						break;
					}
					case LEF:
					{
						if (qs.isCond(3)) // mobs killed condition
						{
							htmltext = "33510-01.html";
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
			case State.COMPLETED:
			{
				htmltext = npc.getId() == LEF ? "33510-03.html" : getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isStarted() && qs.isCond(2) && (Util.checkIfInRange(1500, npc, qs.getPlayer(), false)))
		{
			int kills = 0;
			switch (npc.getId())
			{
				case SKELETON_WARRIOR:
				{
					kills = qs.getInt(Integer.toString(SKELETON_WARRIOR));
					kills++;
					qs.set(Integer.toString(SKELETON_WARRIOR), kills);
					break;
				}
				case RUIN_BAT: // ruin bat & scavenger bat - same counter BAT
				case SCAVENGER_BAT:
				{
					kills = qs.getInt(Integer.toString(BAT));
					kills++;
					qs.set(Integer.toString(BAT), kills);
					break;
				}
				case RUIN_IMP: // ruin imp & ruin imp elder - same counter RUIN_IMP
				case RUIN_IMP_ELDER:
				{
					kills = qs.getInt(Integer.toString(RUIN_IMP));
					kills++;
					qs.set(Integer.toString(RUIN_IMP), kills);
					break;
				}
			}
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(SKELETON_WARRIOR, qs.getInt(Integer.toString(SKELETON_WARRIOR)));
			log.addNpc(RUIN_IMP, qs.getInt(Integer.toString(RUIN_IMP)));
			log.addNpc(BAT, qs.getInt(Integer.toString(BAT)));
			killer.sendPacket(log);
			
			if ((qs.getInt(Integer.toString(SKELETON_WARRIOR)) >= MOBS_REQUIRED.get(SKELETON_WARRIOR)) && (qs.getInt(Integer.toString(BAT)) >= MOBS_REQUIRED.get(BAT)) && (qs.getInt(Integer.toString(RUIN_IMP)) >= MOBS_REQUIRED.get(RUIN_IMP)))
			{
				qs.setCond(3);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
