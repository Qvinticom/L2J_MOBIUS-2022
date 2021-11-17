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
package quests.Q644_GraveRobberAnnihilation;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

public class Q644_GraveRobberAnnihilation extends Quest
{
	// NPC
	private static final int KARUDA = 32017;
	// Item
	private static final int ORC_GRAVE_GOODS = 8088;
	// Rewards
	private static final int[][] REWARDS =
	{
		// @formatter:off
		{1865, 30},
		{1867, 40},
		{1872, 40},
		{1871, 30},
		{1870, 30},
		{1869, 30}
		// @formatter:on
	};
	
	public Q644_GraveRobberAnnihilation()
	{
		super(644, "Grave Robber Annihilation");
		registerQuestItems(ORC_GRAVE_GOODS);
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		addKillId(22003, 22004, 22005, 22006, 22008);
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
		
		if (event.equals("32017-02.htm"))
		{
			st.startQuest();
		}
		else if (Util.isDigit(event))
		{
			htmltext = "32017-04.htm";
			st.takeItems(ORC_GRAVE_GOODS, -1);
			
			final int[] reward = REWARDS[Integer.parseInt(event)];
			st.rewardItems(reward[0], reward[1]);
			
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
				htmltext = (player.getLevel() < 20) ? "32017-06.htm" : "32017-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				if (cond == 1)
				{
					htmltext = "32017-05.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32017-07.htm";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final Player partyMember = getRandomPartyMember(player, npc, 1);
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(ORC_GRAVE_GOODS, 1, 120, 500000))
		{
			st.setCond(2);
		}
		
		return null;
	}
}