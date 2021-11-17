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
package quests.Q156_MillenniumLove;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q156_MillenniumLove extends Quest
{
	// NPCs
	private static final int LILITH = 30368;
	private static final int BAENEDES = 30369;
	// Items
	private static final int LILITH_LETTER = 1022;
	private static final int THEON_DIARY = 1023;
	
	public Q156_MillenniumLove()
	{
		super(156, "Millennium Love");
		registerQuestItems(LILITH_LETTER, THEON_DIARY);
		addStartNpc(LILITH);
		addTalkId(LILITH, BAENEDES);
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
			case "30368-04.htm":
			{
				st.startQuest();
				st.giveItems(LILITH_LETTER, 1);
				break;
			}
			case "30369-02.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(LILITH_LETTER, 1);
				st.giveItems(THEON_DIARY, 1);
				break;
			}
			case "30369-03.htm":
			{
				st.takeItems(LILITH_LETTER, 1);
				st.rewardExpAndSp(3000, 0);
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
				htmltext = (player.getLevel() < 15) ? "30368-00.htm" : "30368-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (npc.getNpcId())
				{
					case LILITH:
					{
						if (st.hasQuestItems(LILITH_LETTER))
						{
							htmltext = "30368-05.htm";
						}
						else if (st.hasQuestItems(THEON_DIARY))
						{
							htmltext = "30368-06.htm";
							st.takeItems(THEON_DIARY, 1);
							st.giveItems(5250, 1);
							st.rewardExpAndSp(3000, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					}
					case BAENEDES:
					{
						if (st.hasQuestItems(LILITH_LETTER))
						{
							htmltext = "30369-01.htm";
						}
						else if (st.hasQuestItems(THEON_DIARY))
						{
							htmltext = "30369-04.htm";
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