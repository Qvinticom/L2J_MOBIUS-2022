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
package quests.Q10757_QuietingTheStorm;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

/**
 * Quieting The Storm (10757)
 * @author Stayway
 */
public class Q10757_QuietingTheStorm extends Quest
{
	// NPC
	private static final int PIO = 33963;
	// Monsters
	private static final int WIND_VORTEX = 23417;
	private static final int GIANT_WINDIMA = 23419;
	private static final int IMMENSE_WINDIMA = 23420;
	private static final Map<Integer, Integer> MOBS_REQUIRED = new HashMap<>();
	{
		MOBS_REQUIRED.put(WIND_VORTEX, 5);
		MOBS_REQUIRED.put(GIANT_WINDIMA, 1);
	}
	// Item
	private static final ItemHolder GUILD_COIN = new ItemHolder(37045, 7);
	// Rewards
	private static final int EXP_REWARD = 632051;
	private static final int SP_REWARD = 151;
	// Others
	private static final int MIN_LEVEL = 24;
	
	public Q10757_QuietingTheStorm()
	{
		super(10757, Q10757_QuietingTheStorm.class.getSimpleName(), "Quieting The Storm");
		addStartNpc(PIO);
		addTalkId(PIO);
		addKillId(WIND_VORTEX, IMMENSE_WINDIMA, GIANT_WINDIMA);
		addCondMinLevel(MIN_LEVEL, "no_level.htm");
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
			case "33963-02.htm":
			case "33963-03.htm":
			case "33963-04.htm":
			case "33963-06.htm":
			{
				htmltext = event;
				break;
			}
			case "33963-05.htm": // start the quest
			{
				qs.startQuest();
				qs.set(Integer.toString(WIND_VORTEX), 0);
				qs.set(Integer.toString(GIANT_WINDIMA), 0);
				qs.set(Integer.toString(IMMENSE_WINDIMA), 0);
				htmltext = event;
				break;
			}
			case "33963-07.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, GUILD_COIN);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.unset(Integer.toString(WIND_VORTEX));
					qs.unset(Integer.toString(GIANT_WINDIMA));
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
				if (player.getRace() != Race.ERTHEIA)
				{
					htmltext = "noErtheia.html";
				}
				else
				{
					htmltext = "33963-01.htm";
				}
				break;
			}
			
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33963-08.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "33963-06.html";
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
			int kills = 0;
			switch (npc.getId())
			{
				case WIND_VORTEX:
				{
					kills = qs.getInt(Integer.toString(WIND_VORTEX));
					kills++;
					qs.set(Integer.toString(WIND_VORTEX), kills);
					break;
				}
				case IMMENSE_WINDIMA:
				case GIANT_WINDIMA:
				{
					kills = qs.getInt(Integer.toString(GIANT_WINDIMA));
					kills++;
					qs.set(Integer.toString(GIANT_WINDIMA), kills);
					break;
				}
			}
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(WIND_VORTEX, qs.getInt(Integer.toString(WIND_VORTEX)));
			log.addNpcString(NpcStringId.IMMENSE_WINDIMA_OR_GIANT_WINDIMA, qs.getInt(Integer.toString(GIANT_WINDIMA)));
			killer.sendPacket(log);
			
			if ((qs.getInt(Integer.toString(WIND_VORTEX)) >= MOBS_REQUIRED.get(WIND_VORTEX)) && (qs.getInt(Integer.toString(GIANT_WINDIMA)) >= MOBS_REQUIRED.get(GIANT_WINDIMA)))
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
