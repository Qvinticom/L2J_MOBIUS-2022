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
package quests.Q00303_CollectArrowheads;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Collect Arrowheads (303)
 * @author ivantotov
 */
public class Q00303_CollectArrowheads extends Quest
{
	// NPC
	private static final int MINIA = 30029;
	// Item
	private static final int ORCISH_ARROWHEAD = 963;
	// Misc
	private static final int MIN_LEVEL = 10;
	private static final int REQUIRED_ITEM_COUNT = 10;
	// Monster
	private static final int TUNATH_ORC_MARKSMAN = 20361;
	
	public Q00303_CollectArrowheads()
	{
		super(303);
		addStartNpc(MINIA);
		addTalkId(MINIA);
		addKillId(TUNATH_ORC_MARKSMAN);
		registerQuestItems(ORCISH_ARROWHEAD);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && event.equals("30029-04.htm"))
		{
			qs.startQuest();
			return event;
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Player partyMember = getRandomPartyMember(player, 1);
		if (partyMember != null)
		{
			final QuestState qs = getQuestState(partyMember, false);
			if (giveItemRandomly(player, npc, ORCISH_ARROWHEAD, 1, REQUIRED_ITEM_COUNT, 0.4, true))
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getLevel() >= MIN_LEVEL ? "30029-03.htm" : "30029-02.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						if (getQuestItemsCount(player, ORCISH_ARROWHEAD) < REQUIRED_ITEM_COUNT)
						{
							htmltext = "30029-05.html";
						}
						break;
					}
					case 2:
					{
						if (getQuestItemsCount(player, ORCISH_ARROWHEAD) >= REQUIRED_ITEM_COUNT)
						{
							giveAdena(player, 1000, true);
							addExpAndSp(player, 2000, 0);
							qs.exitQuest(true, true);
							htmltext = "30029-06.html";
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
}