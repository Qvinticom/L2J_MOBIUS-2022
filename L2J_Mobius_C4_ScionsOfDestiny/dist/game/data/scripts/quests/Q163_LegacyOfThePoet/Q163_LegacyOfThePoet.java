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
package quests.Q163_LegacyOfThePoet;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q163_LegacyOfThePoet extends Quest
{
	// NPC
	private static final int STARDEN = 30220;
	// Items
	private static final int[] RUMIELS_POEMS =
	{
		1038,
		1039,
		1040,
		1041
	};
	// Droplist
	private static final int[][] DROPLIST =
	{
		// @formatter:off
		{RUMIELS_POEMS[0], 1, 1, 100000},
		{RUMIELS_POEMS[1], 1, 1, 200000},
		{RUMIELS_POEMS[2], 1, 1, 200000},
		{RUMIELS_POEMS[3], 1, 1, 400000}
		// @formatter:on
	};
	
	public Q163_LegacyOfThePoet()
	{
		super(163, "Legacy of the Poet");
		registerQuestItems(RUMIELS_POEMS);
		addStartNpc(STARDEN);
		addTalkId(STARDEN);
		addKillId(20372, 20373);
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
		
		if (event.equals("30220-07.htm"))
		{
			st.startQuest();
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
				if (player.getRace() == Race.DARK_ELF)
				{
					htmltext = "30220-00.htm";
				}
				else if (player.getLevel() < 11)
				{
					htmltext = "30220-02.htm";
				}
				else
				{
					htmltext = "30220-03.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (st.isCond(2))
				{
					htmltext = "30220-09.htm";
					for (int poem : RUMIELS_POEMS)
					{
						st.takeItems(poem, -1);
					}
					st.rewardItems(57, 13890);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
				{
					htmltext = "30220-08.htm";
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
		
		if (st.dropMultipleItems(DROPLIST))
		{
			st.setCond(2);
		}
		
		return null;
	}
}