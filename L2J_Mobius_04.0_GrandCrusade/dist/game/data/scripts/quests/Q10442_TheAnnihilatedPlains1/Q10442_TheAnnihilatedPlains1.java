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
package quests.Q10442_TheAnnihilatedPlains1;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * The Annihilated Plains - 1 (10442)
 * @author St3eT
 */
public class Q10442_TheAnnihilatedPlains1 extends Quest
{
	// NPCs
	private static final int MATHIAS = 31340;
	private static final int TUSKA = 33839;
	// Misc
	private static final int MIN_LEVEL = 99;
	
	public Q10442_TheAnnihilatedPlains1()
	{
		super(10442);
		addStartNpc(MATHIAS);
		addTalkId(MATHIAS, TUSKA);
		addCondMinLevel(MIN_LEVEL, "31340-06.htm");
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
		switch (event)
		{
			case "31340-02.htm":
			case "31340-03.htm":
			{
				htmltext = event;
				break;
			}
			case "31340-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33839-02.html":
			{
				if (qs.isCond(1))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 15_436_575, 3_704);
					}
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == MATHIAS)
				{
					htmltext = "31340-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = npc.getId() == MATHIAS ? "31340-05.html" : "33839-01.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == MATHIAS)
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
		}
		return htmltext;
	}
}