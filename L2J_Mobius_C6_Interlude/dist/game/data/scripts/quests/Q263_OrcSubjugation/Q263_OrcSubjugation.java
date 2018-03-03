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
package quests.Q263_OrcSubjugation;

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.Race;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q263_OrcSubjugation extends Quest
{
	private static final String qn = "Q263_OrcSubjugation";
	
	// Items
	private static final int ORC_AMULET = 1116;
	private static final int ORC_NECKLACE = 1117;
	
	public Q263_OrcSubjugation()
	{
		super(263, qn, "Orc Subjugation");
		
		registerQuestItems(ORC_AMULET, ORC_NECKLACE);
		
		addStartNpc(30346); // Kayleen
		addTalkId(30346);
		
		addKillId(20385, 20386, 20387, 20388);
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
		
		if (event.equals("30346-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30346-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace() != Race.darkelf)
				{
					htmltext = "30346-00.htm";
				}
				else if (player.getLevel() < 8)
				{
					htmltext = "30346-01.htm";
				}
				else
				{
					htmltext = "30346-02.htm";
				}
				break;
			
			case State.STARTED:
				int amulet = st.getQuestItemsCount(ORC_AMULET);
				int necklace = st.getQuestItemsCount(ORC_NECKLACE);
				
				if ((amulet == 0) && (necklace == 0))
				{
					htmltext = "30346-04.htm";
				}
				else
				{
					htmltext = "30346-05.htm";
					st.takeItems(ORC_AMULET, -1);
					st.takeItems(ORC_NECKLACE, -1);
					st.rewardItems(57, (amulet * 20) + (necklace * 30));
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		st.dropItems((npc.getNpcId() == 20385) ? ORC_AMULET : ORC_NECKLACE, 1, 0, 500000);
		
		return null;
	}
}