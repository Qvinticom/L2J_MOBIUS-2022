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
package quests.Q00943_FillingTheEnergyOfDestruction;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * Filling the Energy of Destruction (943)
 * @author St3eT
 */
public final class Q00943_FillingTheEnergyOfDestruction extends Quest
{
	// NPCs
	private static final int SEED_TALISMAN = 33715;
	private static final int[] BOSSES =
	{
		29195, // Istina (common)
		29196, // Istina (extreme)
		29194, // Octavis (common)
		29212, // Octavis (extreme)
		25779, // Spezion (normal)
		25867, // Spezion (extreme)
		29213, // Baylor
		29218, // Balok
		25825, // Ron
		29236, // Tauti (common)
		29237, // Tauti (extreme)
	};
	// Items
	private static final int TWISTED_MAGIC = 35668;
	private static final int ENERGY_OF_DESTRUCTION = 35562;
	// Misc
	private static final int MIN_LEVEL = 90;
	
	public Q00943_FillingTheEnergyOfDestruction()
	{
		super(943);
		addStartNpc(SEED_TALISMAN);
		addTalkId(SEED_TALISMAN);
		addKillId(BOSSES);
		registerQuestItems(TWISTED_MAGIC);
		addCondMinLevel(MIN_LEVEL, "33715-08.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		
		if (st == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33715-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33715-03.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "33715-06.html":
			{
				st.exitQuest(QuestType.DAILY, true);
				giveItems(player, ENERGY_OF_DESTRUCTION, 1);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		if (npc.getId() == SEED_TALISMAN)
		{
			switch (st.getState())
			{
				case State.CREATED:
				{
					htmltext = "33715-01.htm";
					break;
				}
				case State.STARTED:
				{
					htmltext = st.isCond(1) ? "33715-04.html" : "33715-05.html";
					break;
				}
				case State.COMPLETED:
				{
					if (st.isNowAvailable())
					{
						st.setState(State.CREATED);
						htmltext = "33715-01.htm";
					}
					else
					{
						htmltext = "33715-07.html";
					}
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, true);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		final QuestState st = getQuestState(player, true);
		if ((st != null) && st.isCond(1) && (npc.calculateDistance(player, false, false) <= 1500))
		{
			st.setCond(2, true);
			giveItems(player, TWISTED_MAGIC, 1);
		}
	}
}