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
package quests.Q00122_OminousNews;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Ominous News (122)
 * @author malyelfik
 */
public class Q00122_OminousNews extends Quest
{
	// NPCs
	private static final int MOIRA = 31979;
	private static final int KARUDA = 32017;
	// Misc
	private static final int MIN_LEVEL = 20;
	
	public Q00122_OminousNews()
	{
		super(122);
		addStartNpc(MOIRA);
		addTalkId(MOIRA, KARUDA);
		addCondMinLevel(MIN_LEVEL, "31979-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "31979-02.htm":
			{
				qs.startQuest();
				break;
			}
			case "32017-02.html":
			{
				giveAdena(player, 8923, true);
				addExpAndSp(player, 45151, 10);
				qs.exitQuest(false, true);
				break;
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		
		switch (npc.getId())
		{
			case MOIRA:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = "31979-01.htm";
						break;
					}
					case State.STARTED:
					{
						htmltext = "31979-03.html";
						break;
					}
					case State.COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg(player);
						break;
					}
				}
				break;
			}
			case KARUDA:
			{
				if (qs.isStarted())
				{
					htmltext = "32017-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
}