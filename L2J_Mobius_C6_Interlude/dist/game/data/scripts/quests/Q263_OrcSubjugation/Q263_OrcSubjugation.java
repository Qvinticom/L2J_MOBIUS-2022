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

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q263_OrcSubjugation extends Quest
{
	// Items
	private static final int ORC_AMULET = 1116;
	private static final int ORC_NECKLACE = 1117;
	
	public Q263_OrcSubjugation()
	{
		super(263, "Orc Subjugation");
		registerQuestItems(ORC_AMULET, ORC_NECKLACE);
		addStartNpc(30346); // Kayleen
		addTalkId(30346);
		addKillId(20385, 20386, 20387, 20388);
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
		
		if (event.equals("30346-03.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("30346-06.htm"))
		{
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
				if (player.getRace() != Race.DARK_ELF)
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
			}
			case State.STARTED:
			{
				final int amulet = st.getQuestItemsCount(ORC_AMULET);
				final int necklace = st.getQuestItemsCount(ORC_NECKLACE);
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
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		st.dropItems((npc.getNpcId() == 20385) ? ORC_AMULET : ORC_NECKLACE, 1, 0, 500000);
		
		return null;
	}
}