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
package quests.Q012_SecretMeetingWithVarkaSilenos;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q012_SecretMeetingWithVarkaSilenos extends Quest
{
	// NPCs
	private static final int CADMON = 31296;
	private static final int HELMUT = 31258;
	private static final int NARAN_ASHANUK = 31378;
	// Items
	private static final int MUNITIONS_BOX = 7232;
	
	public Q012_SecretMeetingWithVarkaSilenos()
	{
		super(12, "Secret Meeting With Varka Silenos");
		registerQuestItems(MUNITIONS_BOX);
		addStartNpc(CADMON);
		addTalkId(CADMON, HELMUT, NARAN_ASHANUK);
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
		
		switch (event)
		{
			case "31296-03.htm":
			{
				st.startQuest();
				break;
			}
			case "31258-02.htm":
			{
				st.giveItems(MUNITIONS_BOX, 1);
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "31378-02.htm":
			{
				st.takeItems(MUNITIONS_BOX, 1);
				st.rewardExpAndSp(79761, 0);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
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
				htmltext = (player.getLevel() < 74) ? "31296-02.htm" : "31296-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case CADMON:
					{
						if (cond == 1)
						{
							htmltext = "31296-04.htm";
						}
						break;
					}
					case HELMUT:
					{
						if (cond == 1)
						{
							htmltext = "31258-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31258-03.htm";
						}
						break;
					}
					case NARAN_ASHANUK:
					{
						if (cond == 2)
						{
							htmltext = "31378-01.htm";
						}
						break;
					}
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
}