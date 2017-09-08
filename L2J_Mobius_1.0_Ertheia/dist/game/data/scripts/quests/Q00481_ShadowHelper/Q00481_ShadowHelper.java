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
package quests.Q00481_ShadowHelper;

import java.util.HashSet;
import java.util.Set;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * Shadow Helper (481)
 * @author malyelfik
 */
public final class Q00481_ShadowHelper extends Quest
{
	// NPC
	private static final int RIDENBAG = 33302;
	private static final int DUMMY = 33348;
	// Monsters
	private static final int[] MONSTERS =
	{
		20136, // Death Knight
		20213, // Porta
		20214, // Excuro
		20215, // Mordeo
		20216, // Ricenseo
		20217, // Krator
		20218, // Premo
		20219, // Validus
		20220, // Dicor
		20221, // Perum
		20222, // Torfe
		20751, // Snipe
		20752, // Snipe Cohort
		20753, // Dark Lord
		20754, // Dark Knight
		21035, // Catherok
		21036, // Shindebarn
		21037, // Ossiud
		21038, // Liangma
		21040, // Soldier of Darkness
	
	};
	// Misc
	private static final int MIN_LEVEL = 38;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q00481_ShadowHelper()
	{
		super(481);
		addStartNpc(RIDENBAG);
		addTalkId(RIDENBAG);
		addKillId(MONSTERS);
		
		addCondMinLevel(MIN_LEVEL, "33302-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "33302-02.htm":
			case "33302-03.htm":
			case "33302-04.htm":
			case "33302-07.html":
			{
				break;
			}
			case "33302-05.htm":
			{
				qs.startQuest();
				break;
			}
			default:
			{
				htmltext = null;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (qs.getState())
		{
			case State.COMPLETED:
			{
				if (!qs.isNowAvailable())
				{
					htmltext = "33302-09.html";
					break;
				}
				qs.setState(State.CREATED);
			}
			case State.CREATED:
			{
				htmltext = "33302-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33302-06.html";
				}
				else
				{
					addExpAndSp(player, 240000, 57);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = "33302-08.html";
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
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			qs.set(KILL_COUNT_VAR, killCount);
			if (killCount >= 20)
			{
				qs.setCond(2, true);
			}
			else
			{
				sendNpcLogList(killer);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR);
			if (killCount > 0)
			{
				final Set<NpcLogListHolder> holder = new HashSet<>(1);
				holder.add(new NpcLogListHolder(DUMMY, false, killCount));
				return holder;
			}
		}
		return super.getNpcLogList(player);
	}
}