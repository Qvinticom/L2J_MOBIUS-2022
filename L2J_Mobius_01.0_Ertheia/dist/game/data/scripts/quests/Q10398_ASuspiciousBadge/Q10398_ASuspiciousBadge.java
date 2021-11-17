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
package quests.Q10398_ASuspiciousBadge;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * A Suspicious Badge (10398)
 * @author St3eT
 */
public class Q10398_ASuspiciousBadge extends Quest
{
	// NPCs
	private static final int ANDY = 33845;
	private static final int BACON = 33846;
	private static final int[] MONSTERS =
	{
		20555, // Giant Fungus
		20558, // Rotting Tree
		23305, // Corroded Skeleton
		23306, // Rotten Corpse
		23307, // Corpse Spider
		23308, // Explosive Spider
	};
	// Items
	private static final int BADGE = 36666; // Unidentified Suspicious Badge
	private static final int EAB = 948; // Scroll: Enchant Armor (B-grade)
	// Misc
	private static final int MIN_LEVEL = 52;
	private static final int MAX_LEVEL = 58;
	
	public Q10398_ASuspiciousBadge()
	{
		super(10398);
		addStartNpc(ANDY);
		addTalkId(ANDY, BACON);
		addKillId(MONSTERS);
		registerQuestItems(BADGE);
		addCondNotRace(Race.ERTHEIA, "33845-05.html");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33845-04.htm");
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
			case "33845-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33845-03.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33846-03.html":
			{
				if (qs.isCond(2))
				{
					qs.exitQuest(false, true);
					giveItems(player, EAB, 5);
					giveStoryQuestReward(player, 36);
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 3_811_500, 914);
					}
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
				if (npc.getId() == ANDY)
				{
					htmltext = "33845-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = npc.getId() == ANDY ? "33845-03.html" : "33846-01.html";
				}
				else if (qs.isCond(2) && (npc.getId() == BACON))
				{
					htmltext = "33846-02.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == ANDY)
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isStarted() && qs.isCond(1))
		{
			if (giveItemRandomly(killer, npc, BADGE, 1, 20, 0.75, true))
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}