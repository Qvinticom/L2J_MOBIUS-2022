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
package quests.Q10368_RebellionOfMonsters;

import java.util.HashMap;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

/**
 * Rebellion of Monsters (10368)
 * @author spider
 */
public class Q10368_RebellionOfMonsters extends Quest
{
	// NPCs
	private static final int FRED = 33179;
	// Monsters
	private static final int WEARY_JAGUAR = 23024;
	private static final int WEARY_JAGUAR_SCOUT = 23025;
	private static final int ANT_SOLDIER = 23099;
	private static final int ANT_WARRIOR_CAPTAIN = 23100;
	private static final HashMap<Integer, Integer> MOBS_REQUIRED = new HashMap<>();
	
	{
		MOBS_REQUIRED.put(WEARY_JAGUAR, 10);
		MOBS_REQUIRED.put(WEARY_JAGUAR_SCOUT, 15);
		MOBS_REQUIRED.put(ANT_SOLDIER, 15);
		MOBS_REQUIRED.put(ANT_WARRIOR_CAPTAIN, 20);
	}
	
	// Rewards
	private static final int ADENA_REWARD = 99000;
	private static final int EXP_REWARD = 750000;
	private static final int SP_REWARD = 180;
	// Others
	private static final int MIN_LEVEL = 34;
	private static final int MAX_LEVEL = 40;
	
	public Q10368_RebellionOfMonsters()
	{
		super(10368, Q10368_RebellionOfMonsters.class.getSimpleName(), "Rebellion of Monsters");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
		addStartNpc(FRED);
		addTalkId(FRED);
		addKillId(MOBS_REQUIRED.keySet());
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
			case "33179-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33179-03.htm": // start quest
			{
				qs.startQuest();
				qs.set(Integer.toString(WEARY_JAGUAR), 0);
				qs.set(Integer.toString(WEARY_JAGUAR_SCOUT), 0);
				qs.set(Integer.toString(ANT_SOLDIER), 0);
				qs.set(Integer.toString(ANT_WARRIOR_CAPTAIN), 0);
				qs.setCond(2);
				qs.setCond(1); // arrow hack
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
				htmltext = "33179-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33179-04.html";
				}
				else if (qs.isCond(2)) // end quest
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.unset(Integer.toString(WEARY_JAGUAR));
					qs.unset(Integer.toString(WEARY_JAGUAR_SCOUT));
					qs.unset(Integer.toString(ANT_SOLDIER));
					qs.unset(Integer.toString(ANT_WARRIOR_CAPTAIN));
					qs.exitQuest(false, true);
					htmltext = "33179-05.html";
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
		if ((qs != null) && qs.isStarted() && qs.isCond(1) && (Util.checkIfInRange(1500, npc, qs.getPlayer(), false)))
		{
			if (qs.getInt(Integer.toString(npc.getId())) < MOBS_REQUIRED.get(npc.getId()))
			{
				int kills = qs.getInt(Integer.toString(npc.getId()));
				kills++;
				qs.set(Integer.toString(npc.getId()), kills);
			}
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(WEARY_JAGUAR, qs.getInt(Integer.toString(WEARY_JAGUAR)));
			log.addNpc(WEARY_JAGUAR_SCOUT, qs.getInt(Integer.toString(WEARY_JAGUAR_SCOUT)));
			log.addNpc(ANT_SOLDIER, qs.getInt(Integer.toString(ANT_SOLDIER)));
			log.addNpc(ANT_WARRIOR_CAPTAIN, qs.getInt(Integer.toString(ANT_WARRIOR_CAPTAIN)));
			killer.sendPacket(log);
			
			if ((qs.getInt(Integer.toString(WEARY_JAGUAR)) >= MOBS_REQUIRED.get(WEARY_JAGUAR)) && (qs.getInt(Integer.toString(WEARY_JAGUAR_SCOUT)) >= MOBS_REQUIRED.get(WEARY_JAGUAR_SCOUT)) && (qs.getInt(Integer.toString(ANT_SOLDIER)) >= MOBS_REQUIRED.get(ANT_SOLDIER)) && (qs.getInt(Integer.toString(ANT_WARRIOR_CAPTAIN)) >= MOBS_REQUIRED.get(ANT_WARRIOR_CAPTAIN)))
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
