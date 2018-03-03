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
package quests.Q297_GatekeepersFavor;

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q297_GatekeepersFavor extends Quest
{
	private static final String qn = "Q297_GatekeepersFavor";
	
	// Item
	private static final int STARSTONE = 1573;
	
	// Reward
	private static final int GATEKEEPER_TOKEN = 1659;
	
	public Q297_GatekeepersFavor()
	{
		super(297, qn, "Gatekeeper's Favor");
		
		registerQuestItems(STARSTONE);
		
		addStartNpc(30540); // Wirphy
		addTalkId(30540);
		
		addKillId(20521); // Whinstone Golem
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("30540-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() < 15) ? "30540-01.htm" : "30540-02.htm";
				break;
			
			case State.STARTED:
				if (st.getInt("cond") == 1)
				{
					htmltext = "30540-04.htm";
				}
				else
				{
					htmltext = "30540-05.htm";
					st.takeItems(STARSTONE, -1);
					st.rewardItems(GATEKEEPER_TOKEN, 2);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(STARSTONE, 1, 20, 500000))
		{
			st.set("cond", "2");
		}
		
		return null;
	}
}