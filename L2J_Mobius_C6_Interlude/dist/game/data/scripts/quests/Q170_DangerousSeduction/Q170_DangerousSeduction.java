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
package quests.Q170_DangerousSeduction;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q170_DangerousSeduction extends Quest
{
	// Item
	private static final int NIGHTMARE_CRYSTAL = 1046;
	
	public Q170_DangerousSeduction()
	{
		super(170, "Dangerous Seduction");
		registerQuestItems(NIGHTMARE_CRYSTAL);
		addStartNpc(30305); // Vellior
		addTalkId(30305);
		addKillId(27022); // Merkenis
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
		
		if (event.equals("30305-04.htm"))
		{
			st.startQuest();
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
				if (player.getRace() != Race.DARK_ELF)
				{
					htmltext = "30305-00.htm";
				}
				else if (player.getLevel() < 21)
				{
					htmltext = "30305-02.htm";
				}
				else
				{
					htmltext = "30305-03.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (st.hasQuestItems(NIGHTMARE_CRYSTAL))
				{
					htmltext = "30305-06.htm";
					st.takeItems(NIGHTMARE_CRYSTAL, -1);
					st.rewardItems(57, 102680);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
				{
					htmltext = "30305-05.htm";
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
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, 1);
		if (st == null)
		{
			return null;
		}
		
		st.setCond(2);
		st.playSound(QuestState.SOUND_MIDDLE);
		st.giveItems(NIGHTMARE_CRYSTAL, 1);
		
		return null;
	}
}