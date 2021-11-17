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
package quests.Q357_WarehouseKeepersAmbition;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q357_WarehouseKeepersAmbition extends Quest
{
	// Monsters
	private static final int FOREST_RUNNER = 20594;
	private static final int FLINE_ELDER = 20595;
	private static final int LIELE_ELDER = 20596;
	private static final int VALLEY_TREANT_ELDER = 20597;
	// Item
	private static final int JADE_CRYSTAL = 5867;
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(FOREST_RUNNER, 400000);
		CHANCES.put(FLINE_ELDER, 410000);
		CHANCES.put(LIELE_ELDER, 440000);
		CHANCES.put(VALLEY_TREANT_ELDER, 650000);
	}
	
	public Q357_WarehouseKeepersAmbition()
	{
		super(357, "Warehouse Keeper's Ambition");
		registerQuestItems(JADE_CRYSTAL);
		addStartNpc(30686); // Silva
		addTalkId(30686);
		addKillId(FOREST_RUNNER, FLINE_ELDER, LIELE_ELDER, VALLEY_TREANT_ELDER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30686-2.htm":
			{
				st.startQuest();
				break;
			}
			case "30686-7.htm":
			{
				final int count = st.getQuestItemsCount(JADE_CRYSTAL);
				if (count == 0)
				{
					htmltext = "30686-4.htm";
				}
				else
				{
					int reward = (count * 425) + 3500;
					if (count >= 100)
					{
						reward += 7400;
					}
					
					st.takeItems(JADE_CRYSTAL, -1);
					st.rewardItems(57, reward);
				}
				break;
			}
			case "30686-8.htm":
			{
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() < 47) ? "30686-0a.htm" : "30686-0.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = (!st.hasQuestItems(JADE_CRYSTAL)) ? "30686-4.htm" : "30686-6.htm";
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final Player partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		partyMember.getQuestState(getName()).dropItems(JADE_CRYSTAL, 1, 0, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
}