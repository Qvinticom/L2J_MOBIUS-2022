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
package quests.Q10897_ShowYourAbility;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10896_VisitTheAdventureGuild.Q10896_VisitTheAdventureGuild;

/**
 * Show Your Ability (10897)
 * @URL https://l2wiki.com/Show_Your_Ability
 * @author Dmitri
 */
public class Q10897_ShowYourAbility extends Quest
{
	// NPCs
	private static final int PENNY = 34413;
	// Misc
	private static final int MIN_LEVEL = 100;
	
	public Q10897_ShowYourAbility()
	{
		super(10897);
		addStartNpc(PENNY);
		addTalkId(PENNY);
		addCondMinLevel(MIN_LEVEL, "nolevel.html");
		addCondCompletedQuest(Q10896_VisitTheAdventureGuild.class.getSimpleName(), "34413-00.htm");
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
			case "34413-02.htm":
			case "34413-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34413-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34413-07.html":
			{
				// Rewards
				addExpAndSp(player, 20763540000L, 20763540);
				addFactionPoints(player, Faction.ADVENTURE_GUILD, 100); // add FP points to ADVENTURE_GUILD Faction
				qs.exitQuest(false, true);
				htmltext = event;
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
				if (npc.getId() == PENNY)
				{
					htmltext = "34413-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case PENNY:
					{
						if ((qs.isCond(1)) && (player.getFactionLevel(Faction.ADVENTURE_GUILD) >= 2))
						{
							final QuestState st = player.getQuestState("Q00682_TheStrongInTheClosedSpace");
							if ((st != null) && st.isCompleted())
							{
								qs.setCond(2, true);
								htmltext = "34413-06.html";
							}
							else
							{
								htmltext = "34413-05.html";
							}
						}
						if (qs.isCond(2))
						{
							htmltext = "34413-06.html";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
}