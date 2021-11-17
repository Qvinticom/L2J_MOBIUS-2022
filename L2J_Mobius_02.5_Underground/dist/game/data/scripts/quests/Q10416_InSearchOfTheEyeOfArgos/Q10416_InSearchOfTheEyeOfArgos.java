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
package quests.Q10416_InSearchOfTheEyeOfArgos;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * In Search of the Eye of Argos (10416)
 * @author St3eT
 */
public class Q10416_InSearchOfTheEyeOfArgos extends Quest
{
	// NPCs
	private static final int JANITT = 33851;
	private static final int EYE_OF_ARGOS = 31683;
	// Items
	private static final int EAA = 730; // Scroll: Enchant Armor (A-grade)
	// Misc
	private static final int MIN_LEVEL = 70;
	private static final int MAX_LEVEL = 75;
	
	public Q10416_InSearchOfTheEyeOfArgos()
	{
		super(10416);
		addStartNpc(JANITT);
		addTalkId(JANITT, EYE_OF_ARGOS);
		addCondNotRace(Race.ERTHEIA, "33851-06.html");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33851-07.htm");
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
			case "33851-02.htm":
			case "33851-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33851-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "31683-02.html":
			{
				if (qs.isCond(1))
				{
					qs.exitQuest(false, true);
					giveItems(player, EAA, 2);
					giveStoryQuestReward(player, 2);
					if (player.getLevel() > MIN_LEVEL)
					{
						addExpAndSp(player, 1_088_640, 261);
					}
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
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == JANITT)
				{
					htmltext = "33851-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = npc.getId() == JANITT ? "33851-05.html" : "31683-01.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == JANITT)
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
		}
		return htmltext;
	}
}