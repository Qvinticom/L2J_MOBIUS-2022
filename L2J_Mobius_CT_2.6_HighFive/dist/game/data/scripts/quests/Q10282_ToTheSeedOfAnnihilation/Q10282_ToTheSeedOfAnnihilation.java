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
package quests.Q10282_ToTheSeedOfAnnihilation;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * To the Seed of Destruction (10269)<br>
 * Original Jython script by Gnacik 2010-08-13 Based on Freya PTS.
 * @author nonom
 */
public class Q10282_ToTheSeedOfAnnihilation extends Quest
{
	// NPCs
	private static final int KBALDIR = 32733;
	private static final int KLEMIS = 32734;
	// Item
	private static final int SOA_ORDERS = 15512;
	
	public Q10282_ToTheSeedOfAnnihilation()
	{
		super(10282);
		addStartNpc(KBALDIR);
		addTalkId(KBALDIR, KLEMIS);
		registerQuestItems(SOA_ORDERS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "32733-07.htm":
			{
				qs.startQuest();
				giveItems(player, SOA_ORDERS, 1);
				break;
			}
			case "32734-02.htm":
			{
				giveAdena(player, 212182, true);
				addExpAndSp(player, 1148480, 99110);
				qs.exitQuest(false);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		final int npcId = npc.getId();
		switch (qs.getState())
		{
			case State.COMPLETED:
			{
				if (npcId == KBALDIR)
				{
					htmltext = "32733-09.htm";
				}
				else if (npcId == KLEMIS)
				{
					htmltext = "32734-03.htm";
				}
				break;
			}
			case State.CREATED:
			{
				htmltext = (player.getLevel() < 84) ? "32733-00.htm" : "32733-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					if (npcId == KBALDIR)
					{
						htmltext = "32733-08.htm";
					}
					else if (npcId == KLEMIS)
					{
						htmltext = "32734-01.htm";
					}
				}
				break;
			}
		}
		return htmltext;
	}
}
