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
package quests.Q649_ALooterAndARailroadMan;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q649_ALooterAndARailroadMan extends Quest
{
	// NPC
	private static final int OBI = 32052;
	// Item
	private static final int THIEF_GUILD_MARK = 8099;
	
	public Q649_ALooterAndARailroadMan()
	{
		super(649, "A Looter and a Railroad Man");
		registerQuestItems(THIEF_GUILD_MARK);
		addStartNpc(OBI);
		addTalkId(OBI);
		addKillId(22017, 22018, 22019, 22021, 22022, 22023, 22024, 22026);
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
		
		if (event.equals("32052-1.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("32052-3.htm"))
		{
			if (st.getQuestItemsCount(THIEF_GUILD_MARK) < 200)
			{
				htmltext = "32052-3a.htm";
			}
			else
			{
				st.takeItems(THIEF_GUILD_MARK, -1);
				st.rewardItems(57, 21698);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
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
				htmltext = (player.getLevel() < 30) ? "32052-0a.htm" : "32052-0.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				if (cond == 1)
				{
					htmltext = "32052-2a.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32052-2.htm";
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
		
		if (st.dropItems(THIEF_GUILD_MARK, 1, 200, 800000))
		{
			st.setCond(2);
		}
		
		return null;
	}
}