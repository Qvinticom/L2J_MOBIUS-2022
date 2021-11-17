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
package quests.Q00130_PathToHellbound;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import ai.areas.Hellbound.HellboundEngine;

/**
 * Path To Hellbound (130)
 * @author Zoey76
 */
public class Q00130_PathToHellbound extends Quest
{
	// NPCs
	private static final int CASIAN = 30612;
	private static final int GALATE = 32292;
	// Item
	private static final int CASIANS_BLUE_CRYSTAL = 12823;
	// Misc
	private static final int MIN_LEVEL = 78;
	
	public Q00130_PathToHellbound()
	{
		super(130);
		addStartNpc(CASIAN);
		addTalkId(CASIAN, GALATE);
		registerQuestItems(CASIANS_BLUE_CRYSTAL);
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
			case "30612-04.htm":
			{
				htmltext = event;
				break;
			}
			case "32292-02.html":
			{
				if (qs.isCond(1))
				{
					htmltext = event;
				}
				break;
			}
			case "32292-06.html":
			{
				if (qs.isCond(3))
				{
					htmltext = event;
				}
				break;
			}
			case "30612-05.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30612-08.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, CASIANS_BLUE_CRYSTAL, 1);
					qs.setCond(3, true);
					htmltext = event;
				}
				break;
			}
			case "32292-03.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "32292-07.html":
			{
				if (qs.isCond(3) && hasQuestItems(player, CASIANS_BLUE_CRYSTAL))
				{
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
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
			case State.CREATED:
			{
				if (npc.getId() == CASIAN)
				{
					if (!HellboundEngine.getInstance().isLocked())
					{
						htmltext = (player.getLevel() >= MIN_LEVEL) ? "30612-01.htm" : "30612-02.html";
					}
					else
					{
						htmltext = "30612-03.html";
					}
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == CASIAN)
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "30612-06.html";
							break;
						}
						case 2:
						{
							htmltext = "30612-07.html";
							break;
						}
						case 3:
						{
							htmltext = "30612-09.html";
							break;
						}
					}
				}
				else if (npc.getId() == GALATE)
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "32292-01.html";
							break;
						}
						case 2:
						{
							htmltext = "32292-04.html";
							break;
						}
						case 3:
						{
							htmltext = "32292-05.html";
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
