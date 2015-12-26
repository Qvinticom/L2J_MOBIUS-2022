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
package quests.Q10358_DividedSakumPoslof;

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
 * Divided Sakum, Poslof (10358)
 * @author spider
 */
public class Q10358_DividedSakumPoslof extends Quest
{
	// NPCs
	private static final int LEF = 33510;
	private static final int ADV_GUILDSMAN = 31795;
	// Monsters
	private static final int POSLOF = 27452;
	private static final int ZOMBIE_WARRIOR = 20458;
	private static final int VEELAN_BUGBEAR_WARRIOR = 20402;
	private static final Map<Integer, Integer> MOBS_REQUIRED = new HashMap<>();
	{
		MOBS_REQUIRED.put(ZOMBIE_WARRIOR, 20);
		MOBS_REQUIRED.put(VEELAN_BUGBEAR_WARRIOR, 23);
		MOBS_REQUIRED.put(POSLOF, 0);
	}
	// Item
	private static final int SAKUMS_SKETCH_B = 17585;
	// Rewards
	private static final int ADENA_REWARD = 105000;
	private static final int EXP_REWARD = 750000;
	private static final int SP_REWARD = 180;
	// Others
	private static final int MIN_LEVEL = 33;
	private static final int MAX_LEVEL = 40;
	
	public Q10358_DividedSakumPoslof()
	{
		super(10358, Q10358_DividedSakumPoslof.class.getSimpleName(), "Divided Sakum, Poslof");
		addStartNpc(LEF);
		addTalkId(LEF, ADV_GUILDSMAN);
		addKillId(ZOMBIE_WARRIOR, VEELAN_BUGBEAR_WARRIOR, POSLOF);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
		registerQuestItems(SAKUMS_SKETCH_B);
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
			case "31795-02.html":
			{
				htmltext = event;
				break;
			}
			case "31795-03.html":
			{
				if (qs.isCond(4))
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(false, true);
					qs.unset(Integer.toString(ZOMBIE_WARRIOR));
					qs.unset(Integer.toString(VEELAN_BUGBEAR_WARRIOR));
					qs.unset(Integer.toString(POSLOF));
					// htmltext = null; // got nothing on retail, retail-like bug? :D
				}
				break;
			}
			case "33510-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33510-03.htm":
			{
				qs.startQuest();
				qs.set(Integer.toString(ZOMBIE_WARRIOR), 0);
				qs.set(Integer.toString(VEELAN_BUGBEAR_WARRIOR), 0);
				
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
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = npc.getId() == LEF ? "33510-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case LEF:
					{
						if (qs.isCond(1))
						{
							htmltext = "33510-03.htm";
						}
						else if (qs.isCond(2)) // mobs killed
						{
							qs.setCond(3);
							giveItems(player, SAKUMS_SKETCH_B, 1);
							qs.set(Integer.toString(POSLOF), 0);
							htmltext = "33510-04.html";
						}
						else
						{
							htmltext = "33510-05.html";
						}
						break;
					}
					case ADV_GUILDSMAN:
					{
						if (qs.isCond(4)) // poslof defeated
						{
							htmltext = "31795-01.html";
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
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isStarted() && (Util.checkIfInRange(1500, npc, qs.getPlayer(), false)))
		{
			int kills = 0;
			switch (npc.getId())
			{
				case ZOMBIE_WARRIOR:
				{
					kills = qs.getInt(Integer.toString(ZOMBIE_WARRIOR));
					kills++;
					qs.set(Integer.toString(ZOMBIE_WARRIOR), kills);
					break;
				}
				case VEELAN_BUGBEAR_WARRIOR:
				{
					kills = qs.getInt(Integer.toString(VEELAN_BUGBEAR_WARRIOR));
					kills++;
					qs.set(Integer.toString(VEELAN_BUGBEAR_WARRIOR), kills);
					break;
				}
				case POSLOF:
				{
					if (qs.isCond(3))
					{
						qs.setCond(4);
					}
					break;
				}
				
			}
			if (qs.isCond(1))
			{
				final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
				log.addNpc(ZOMBIE_WARRIOR, qs.getInt(Integer.toString(ZOMBIE_WARRIOR)));
				log.addNpc(VEELAN_BUGBEAR_WARRIOR, qs.getInt(Integer.toString(VEELAN_BUGBEAR_WARRIOR)));
				killer.sendPacket(log);
				
				if ((qs.getInt(Integer.toString(ZOMBIE_WARRIOR)) >= MOBS_REQUIRED.get(ZOMBIE_WARRIOR)) && (qs.getInt(Integer.toString(VEELAN_BUGBEAR_WARRIOR)) >= MOBS_REQUIRED.get(VEELAN_BUGBEAR_WARRIOR)))
				{
					qs.setCond(2); // mobs killed
				}
			}
			else if (qs.isCond(3))
			{
				final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
				log.addNpc(POSLOF, qs.getInt(Integer.toString(POSLOF)));
				killer.sendPacket(log);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
