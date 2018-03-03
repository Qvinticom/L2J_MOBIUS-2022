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
package quests.Q643_RiseAndFallOfTheElrokiTribe;

import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q643_RiseAndFallOfTheElrokiTribe extends Quest
{
	private static final String qn = "Q643_RiseAndFallOfTheElrokiTribe";
	
	// NPCs
	private static final int SINGSING = 32106;
	private static final int KARAKAWEI = 32117;
	
	// Items
	private static final int BONES = 8776;
	
	public Q643_RiseAndFallOfTheElrokiTribe()
	{
		super(643, qn, "Rise and Fall of the Elroki Tribe");
		
		registerQuestItems(BONES);
		
		addStartNpc(SINGSING);
		addTalkId(SINGSING, KARAKAWEI);
		
		addKillId(22208, 22209, 22210, 22211, 22212, 22213, 22221, 22222, 22226, 22227);
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
		
		if (event.equals("32106-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("32106-07.htm"))
		{
			final int count = st.getQuestItemsCount(BONES);
			
			st.takeItems(BONES, count);
			st.rewardItems(57, count * 1374);
		}
		else if (event.equals("32106-09.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equals("32117-03.htm"))
		{
			final int count = st.getQuestItemsCount(BONES);
			if (count >= 300)
			{
				st.takeItems(BONES, 300);
				st.rewardItems(Rnd.get(8712, 8722), 5);
			}
			else
			{
				htmltext = "32117-04.htm";
			}
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
				htmltext = (player.getLevel() < 75) ? "32106-00.htm" : "32106-01.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case SINGSING:
						htmltext = (st.hasQuestItems(BONES)) ? "32106-06.htm" : "32106-05.htm";
						break;
					
					case KARAKAWEI:
						htmltext = "32117-01.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		partyMember.getQuestState(qn).dropItems(BONES, 1, 0, 750000);
		
		return null;
	}
}