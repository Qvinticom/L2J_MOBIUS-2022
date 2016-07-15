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
package quests.Q00495_IncarnationOfJealousyPellineGroup;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * Incarnation of Jealousy Pelline (Group) (495)
 * @author Mobius
 */
public class Q00495_IncarnationOfJealousyPellineGroup extends Quest
{
	// NPC
	private static final int KARTIA_RESEARCHER = 33647;
	// Item
	private static final int DIMENSION_KEEPER_RED_BOX = 34928;
	// Misc
	private static final int MIN_LEVEL = 90;
	private static final int MAX_LEVEL = 94;
	
	public Q00495_IncarnationOfJealousyPellineGroup()
	{
		super(495);
		addStartNpc(KARTIA_RESEARCHER);
		addTalkId(KARTIA_RESEARCHER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		final String htmltext = event;
		if (event.equals("33647-03.htm"))
		{
			qs.startQuest();
		}
		else if (event.equals("33647-06.html") && qs.isCond(2))
		{
			rewardItems(player, DIMENSION_KEEPER_RED_BOX, 1);
			qs.exitQuest(QuestType.DAILY, true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if ((player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL))
				{
					htmltext = "33647-00.htm";
				}
				else
				{
					htmltext = "33647-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33647-04.html";
				}
				else if (qs.isCond(2))
				{
					if ((player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL))
					{
						htmltext = "33647-00a.htm";
					}
					else
					{
						htmltext = "33647-05.html";
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if ((player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL))
				{
					htmltext = "33647-00.htm";
				}
				else if (!qs.isNowAvailable())
				{
					htmltext = "33647-07.html";
				}
				else
				{
					qs.setState(State.CREATED);
					htmltext = "33647-01.htm";
				}
				break;
			}
		}
		return htmltext;
	}
}