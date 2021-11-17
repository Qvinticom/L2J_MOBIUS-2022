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
package quests.Q00651_RunawayYouth;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Runaway Youth (651)
 * @author malyelfik
 */
public class Q00651_RunawayYouth extends Quest
{
	// NPCs
	private static final int BATIDAE = 31989;
	private static final int IVAN = 32014;
	// Item
	private static final int SOE = 736;
	// Misc
	private static final int MIN_LEVEL = 26;
	
	public Q00651_RunawayYouth()
	{
		super(651);
		addStartNpc(IVAN);
		addTalkId(BATIDAE, IVAN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		if (event.equals("32014-03.html"))
		{
			htmltext = event;
		}
		else if (event.equals("32014-04.htm"))
		{
			if (!hasQuestItems(player, SOE))
			{
				return "32014-05.htm";
			}
			qs.startQuest();
			takeItems(player, SOE, 1);
			npc.deleteMe();
			htmltext = event;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case IVAN:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = (player.getLevel() >= MIN_LEVEL) ? "32014-01.htm" : "32014-01a.htm";
						break;
					}
					case State.STARTED:
					{
						htmltext = "32014-02.html";
						break;
					}
				}
				break;
			}
			case BATIDAE:
			{
				if (qs.isStarted())
				{
					giveAdena(player, 2883, true);
					qs.exitQuest(true, true);
					htmltext = "31989-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
}