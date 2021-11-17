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
package quests.Q688_DefeatTheElrokianRaiders;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q688_DefeatTheElrokianRaiders extends Quest
{
	// NPC
	private static final int DINN = 32105;
	// Monster
	private static final int ELROKI = 22214;
	// Item
	private static final int DINOSAUR_FANG_NECKLACE = 8785;
	
	public Q688_DefeatTheElrokianRaiders()
	{
		super(688, "Defeat the Elrokian Raiders!");
		registerQuestItems(DINOSAUR_FANG_NECKLACE);
		addStartNpc(DINN);
		addTalkId(DINN);
		addKillId(ELROKI);
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
		
		switch (event)
		{
			case "32105-03.htm":
			{
				st.startQuest();
				break;
			}
			case "32105-08.htm":
			{
				final int count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
				if (count > 0)
				{
					st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
					st.rewardItems(57, count * 3000);
				}
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
				break;
			}
			case "32105-06.htm":
			{
				final int count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
				st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
				st.rewardItems(57, count * 3000);
				break;
			}
			case "32105-07.htm":
			{
				final int count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
				if (count >= 100)
				{
					st.takeItems(DINOSAUR_FANG_NECKLACE, 100);
					st.rewardItems(57, 450000);
				}
				else
				{
					htmltext = "32105-04.htm";
				}
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
				htmltext = (player.getLevel() < 75) ? "32105-00.htm" : "32105-01.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = (!st.hasQuestItems(DINOSAUR_FANG_NECKLACE)) ? "32105-04.htm" : "32105-05.htm";
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final Player partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		st.dropItems(DINOSAUR_FANG_NECKLACE, 1, 0, 500000);
		
		return null;
	}
}