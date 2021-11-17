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
package quests.Q359_ForSleeplessDeadmen;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q359_ForSleeplessDeadmen extends Quest
{
	// Monsters
	private static final int DOOM_SERVANT = 21006;
	private static final int DOOM_GUARD = 21007;
	private static final int DOOM_ARCHER = 21008;
	// Item
	private static final int REMAINS = 5869;
	// Reward
	private static final int[] REWARD =
	{
		6341,
		6342,
		6343,
		6344,
		6345,
		6346,
		5494,
		5495
	};
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(DOOM_SERVANT, 320000);
		CHANCES.put(DOOM_GUARD, 340000);
		CHANCES.put(DOOM_ARCHER, 420000);
	}
	
	public Q359_ForSleeplessDeadmen()
	{
		super(359, "For Sleepless Deadmen");
		registerQuestItems(REMAINS);
		addStartNpc(30857); // Orven
		addTalkId(30857);
		addKillId(DOOM_SERVANT, DOOM_GUARD, DOOM_ARCHER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("30857-06.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("30857-10.htm"))
		{
			st.giveItems(REWARD[Rnd.get(REWARD.length)], 4);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() < 60) ? "30857-01.htm" : "30857-02.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				if (cond == 1)
				{
					htmltext = "30857-07.htm";
				}
				else if (cond == 2)
				{
					htmltext = "30857-08.htm";
					st.setCond(3);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(REMAINS, -1);
				}
				else if (cond == 3)
				{
					htmltext = "30857-09.htm";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, 1);
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(REMAINS, 1, 60, CHANCES.get(npc.getNpcId())))
		{
			st.setCond(2);
		}
		
		return null;
	}
}