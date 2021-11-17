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
package quests.Q00176_StepsForHonor;

import org.l2jmobius.gameserver.instancemanager.TerritoryWarManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Steps for Honor (176)
 * @author malyelfik
 */
public class Q00176_StepsForHonor extends Quest
{
	// NPC
	private static final int RAPIDUS = 36479;
	// Item
	private static final int CLOAK = 14603;
	// Misc
	private static final int MIN_LEVEL = 80;
	
	public Q00176_StepsForHonor()
	{
		super(176);
		addStartNpc(RAPIDUS);
		addTalkId(RAPIDUS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && event.equalsIgnoreCase("36479-04.html"))
		{
			qs.startQuest();
			return event;
		}
		return null;
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
				htmltext = (player.getLevel() >= MIN_LEVEL) ? "36479-03.html" : "36479-02.html";
				break;
			}
			case State.STARTED:
			{
				if (TerritoryWarManager.getInstance().isTWInProgress())
				{
					return "36479-05.html";
				}
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "36479-06.html";
						break;
					}
					case 2:
					{
						qs.setCond(3, true);
						htmltext = "36479-07.html";
						break;
					}
					case 3:
					{
						htmltext = "36479-08.html";
						break;
					}
					case 4:
					{
						qs.setCond(5, true);
						htmltext = "36479-09.html";
						break;
					}
					case 5:
					{
						htmltext = "36479-10.html";
						break;
					}
					case 6:
					{
						qs.setCond(7, true);
						htmltext = "36479-11.html";
						break;
					}
					case 7:
					{
						htmltext = "36479-12.html";
						break;
					}
					case 8:
					{
						giveItems(player, CLOAK, 1);
						qs.exitQuest(false, true);
						htmltext = "36479-13.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = "36479-01.html";
				break;
			}
		}
		return htmltext;
	}
}