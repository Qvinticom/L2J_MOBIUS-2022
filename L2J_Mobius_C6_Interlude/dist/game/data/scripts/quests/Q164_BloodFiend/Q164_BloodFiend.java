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
package quests.Q164_BloodFiend;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q164_BloodFiend extends Quest
{
	// Item
	private static final int KIRUNAK_SKULL = 1044;
	
	public Q164_BloodFiend()
	{
		super(164, "Blood Fiend");
		registerQuestItems(KIRUNAK_SKULL);
		addStartNpc(30149);
		addTalkId(30149);
		addKillId(27021);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("30149-04.htm"))
		{
			st.startQuest();
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
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
				if (player.getRace() == Race.DARK_ELF)
				{
					htmltext = "30149-00.htm";
				}
				else if (player.getLevel() < 21)
				{
					htmltext = "30149-02.htm";
				}
				else
				{
					htmltext = "30149-03.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (st.hasQuestItems(KIRUNAK_SKULL))
				{
					htmltext = "30149-06.htm";
					st.takeItems(KIRUNAK_SKULL, 1);
					st.rewardItems(57, 42130);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
				{
					htmltext = "30149-05.htm";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, 1);
		if (st == null)
		{
			return null;
		}
		
		st.setCond(2);
		st.playSound(QuestState.SOUND_MIDDLE);
		st.giveItems(KIRUNAK_SKULL, 1);
		
		return null;
	}
}