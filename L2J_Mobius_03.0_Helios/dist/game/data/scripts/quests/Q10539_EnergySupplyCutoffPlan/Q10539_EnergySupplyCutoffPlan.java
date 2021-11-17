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
package quests.Q10539_EnergySupplyCutoffPlan;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

import quests.Q10537_KamaelDisarray.Q10537_KamaelDisarray;

/**
 * Energy Supply Cutoff Plan (10539)
 * @URL https://l2wiki.com/Energy_Supply_Cutoff_Plan
 * @author Dmitri
 */
public class Q10539_EnergySupplyCutoffPlan extends Quest
{
	// NPCs
	private static final int KRENAHT = 34237;
	// Monsters
	private static final int MARKA = 23739;
	private static final int SCHLIEN = 23740;
	private static final int BERIMAH = 23741;
	// Reward
	private static final int RUNE_STONE = 39738;
	// Misc
	private static final int MIN_LEVEL = 100;
	
	public Q10539_EnergySupplyCutoffPlan()
	{
		super(10539);
		addStartNpc(KRENAHT);
		addTalkId(KRENAHT);
		addKillId(MARKA, SCHLIEN, BERIMAH);
		addCondMinLevel(MIN_LEVEL, "34237-00.htm");
		addCondCompletedQuest(Q10537_KamaelDisarray.class.getSimpleName(), "34237-00.htm");
		addFactionLevel(Faction.GIANT_TRACKERS, 4, "34237-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "34237-02.htm":
			case "34237-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34237-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34237-07.html":
			{
				giveItems(player, RUNE_STONE, 1); // Rune Stone
				addExpAndSp(player, 11073888000L, 26577180);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "34237-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "34237-05.html";
				}
				else
				{
					htmltext = "34237-06.html";
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
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, player, false))
		{
			int killedCount = qs.getInt(Integer.toString(npc.getId()));
			final int Marka = qs.getInt(Integer.toString(MARKA));
			final int Schlien = qs.getInt(Integer.toString(SCHLIEN));
			final int Berimah = qs.getInt(Integer.toString(BERIMAH));
			switch (qs.getCond())
			{
				case 1:
				{
					qs.set(Integer.toString(npc.getId()), ++killedCount);
					if ((Marka == 1) && (Schlien == 1) && (Berimah == 1))
					{
						qs.setCond(2, true);
					}
					else
					{
						sendNpcLogList(player);
					}
					break;
				}
			}
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(3);
			npcLogList.add(new NpcLogListHolder(MARKA, false, qs.getInt(Integer.toString(MARKA))));
			npcLogList.add(new NpcLogListHolder(SCHLIEN, false, qs.getInt(Integer.toString(SCHLIEN))));
			npcLogList.add(new NpcLogListHolder(BERIMAH, false, qs.getInt(Integer.toString(BERIMAH))));
			return npcLogList;
		}
		return super.getNpcLogList(player);
	}
}
