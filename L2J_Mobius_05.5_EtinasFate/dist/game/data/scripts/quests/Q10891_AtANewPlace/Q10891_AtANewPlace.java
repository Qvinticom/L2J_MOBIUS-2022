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
package quests.Q10891_AtANewPlace;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * At a New Place (10891)
 * @URL https://l2wiki.com/At_a_New_Place
 * @author Dmitri
 */
public class Q10891_AtANewPlace extends Quest
{
	// NPCs
	private static final int ELIKIA = 34057;
	private static final int LOGART = 34235;
	private static final int FERIN = 34054;
	private static final int DEVIANNE = 34427;
	private static final int LEONA = 34425;
	// Reward
	private static final int SCROLL = 46158; // Scroll of Escape: Blackbird Campsite
	// Misc
	private static final int MIN_LEVEL = 103;
	
	public Q10891_AtANewPlace()
	{
		super(10891);
		addStartNpc(ELIKIA);
		addTalkId(ELIKIA, LOGART, FERIN, DEVIANNE, LEONA);
		addCondMinLevel(MIN_LEVEL, "34057-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "34057-02.htm":
			case "34057-03.htm":
			case "34235-03.html":
			case "34054-03.html":
			case "34057-06.html":
			case "34427-02.html":
			case "34427-04.html":
			case "34425-02.html":
			{
				htmltext = event;
				break;
			}
			case "34057-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34235-02.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34054-02.html":
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "34057-07.html":
			{
				qs.setCond(4, true);
				htmltext = event;
				break;
			}
			case "34427-03.html":
			{
				qs.setCond(5, true);
				htmltext = event;
				break;
			}
			case "34425-03.html":
			{
				if (qs.isCond(5))
				{
					addExpAndSp(player, 906_387_492, 906_387);
					giveItems(player, SCROLL, 10); // Scroll of Escape: Blackbird Campsite Ξ²β‚¬β€� 10 pcs.
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
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
				if (npc.getId() == ELIKIA)
				{
					htmltext = "34057-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case ELIKIA:
					{
						if (qs.isCond(1))
						{
							htmltext = "34057-04.htm";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34057-05.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "34057-08.html";
						}
						break;
					}
					case LOGART:
					{
						if (qs.isCond(1))
						{
							htmltext = "34235-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34235-04.html";
						}
						break;
					}
					case FERIN:
					{
						if (qs.isCond(2))
						{
							htmltext = "34054-01.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34054-03.html";
						}
						break;
					}
					case DEVIANNE:
					{
						if (qs.isCond(4))
						{
							htmltext = "34427-01.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "34427-04.html";
						}
						break;
					}
					case LEONA:
					{
						if (qs.isCond(5))
						{
							htmltext = "34425-01.html";
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
