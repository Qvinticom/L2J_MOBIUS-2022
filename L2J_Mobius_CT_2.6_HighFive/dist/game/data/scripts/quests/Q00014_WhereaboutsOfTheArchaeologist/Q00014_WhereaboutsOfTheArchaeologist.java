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
package quests.Q00014_WhereaboutsOfTheArchaeologist;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Whereabouts of the Archaeologist (14)<br>
 * Original Jython script by disKret.
 * @author nonom
 */
public class Q00014_WhereaboutsOfTheArchaeologist extends Quest
{
	// NPCs
	private static final int LIESEL = 31263;
	private static final int GHOST_OF_ADVENTURER = 31538;
	// Item
	private static final int LETTER = 7253;
	
	public Q00014_WhereaboutsOfTheArchaeologist()
	{
		super(14);
		addStartNpc(LIESEL);
		addTalkId(LIESEL, GHOST_OF_ADVENTURER);
		registerQuestItems(LETTER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "31263-02.html":
			{
				qs.startQuest();
				giveItems(player, LETTER, 1);
				break;
			}
			case "31538-01.html":
			{
				if (qs.isCond(1) && hasQuestItems(player, LETTER))
				{
					giveAdena(player, 136928, true);
					addExpAndSp(player, 325881, 32524);
					qs.exitQuest(false, true);
				}
				else
				{
					htmltext = "31538-02.html";
				}
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
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
			case State.CREATED:
			{
				if (npcId == LIESEL)
				{
					htmltext = (player.getLevel() < 74) ? "31263-01.html" : "31263-00.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					switch (npcId)
					{
						case LIESEL:
						{
							htmltext = "31263-02.html";
							break;
						}
						case GHOST_OF_ADVENTURER:
						{
							htmltext = "31538-00.html";
							break;
						}
					}
				}
				break;
			}
		}
		return htmltext;
	}
}
