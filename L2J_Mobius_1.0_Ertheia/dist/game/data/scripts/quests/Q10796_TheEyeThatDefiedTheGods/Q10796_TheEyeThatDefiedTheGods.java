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
package quests.Q10796_TheEyeThatDefiedTheGods;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;

/**
 * The Eye that Defied the Gods (10796)
 * @URL https://l2wiki.com/The_Eye_that_Defied_the_Gods
 * @author Gigi
 */
public class Q10796_TheEyeThatDefiedTheGods extends Quest
{
	// NPCs
	private static final int HERMIT = 31616;
	private static final int EYE_OF_ARGOS = 31683;
	// Items
	private static final int EAA = 730;
	// Misc
	private static final int MIN_LEVEL = 70;
	private static final int MAX_LEVEL = 75;
	
	public Q10796_TheEyeThatDefiedTheGods()
	{
		super(10796);
		addStartNpc(HERMIT);
		addTalkId(HERMIT, EYE_OF_ARGOS);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
		addCondRace(Race.ERTHEIA, "noErtheya.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		final String htmltext = event;
		switch (event)
		{
			case "31616-02.htm":
			case "31616-03.htm":
			{
				break;
			}
			case "31616-04.htm":
			{
				qs.startQuest();
				break;
			}
			case "31683-02.html":
			{
				addExpAndSp(player, 1088640, 261);
				giveStoryQuestReward(player, 2);
				giveItems(player, EAA, 2);
				qs.exitQuest(false, true);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case HERMIT:
			{
				if (qs.isCreated())
				{
					htmltext = "31616-01.htm";
				}
				else if (qs.isCond(1))
				{
					htmltext = "31616-05.html";
				}
				if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case EYE_OF_ARGOS:
			{
				if (qs.isCond(1))
				{
					htmltext = "31683-01.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
		}
		return htmltext;
	}
}