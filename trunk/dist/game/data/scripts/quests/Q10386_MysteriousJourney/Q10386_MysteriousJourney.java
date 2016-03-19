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
package quests.Q10386_MysteriousJourney;

import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * @author hlwrave
 * @URL https://l2wiki.com/Mysterious_Journey
 */
public class Q10386_MysteriousJourney extends Quest
{
	// Npcs
	private static final int TOPOI = 30499;
	private static final int HESET = 33780;
	private static final int BERNA = 33796;
	// Misc
	public static final int MIN_LEVEL = 93;
	
	public Q10386_MysteriousJourney()
	{
		super(10386, Q10386_MysteriousJourney.class.getSimpleName(), "Mysterious Journey");
		addStartNpc(TOPOI);
		addTalkId(TOPOI, HESET, BERNA);
		addCondMinLevel(MIN_LEVEL, "nolevel.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "accepted.html":
			{
				qs.startQuest();
				break;
			}
			case "acceptedHeset.html":
			{
				qs.setCond(3);
				break;
			}
			case "acceptedBerma.html":
			{
				qs.setCond(4);
				break;
			}
			case "endquest.html":
			{
				addExpAndSp(player, 27244350, 2724435);
				giveAdena(player, 58707, true);
				giveItems(player, 17526, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				qs.exitQuest(QuestType.ONE_TIME, true);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (npc.getId())
		{
			case TOPOI:
			{
				if (qs.isCreated())
				{
					htmltext = "start.htm";
				}
				else if (qs.isCompleted())
				{
					htmltext = "completed.html";
				}
				break;
			}
			case HESET:
			{
				if (qs.isStarted())
				{
					if (qs.isCond(1))
					{
						htmltext = "hesetCond1.html";
					}
					else if (qs.isCond(4))
					{
						htmltext = "collected.html";
					}
				}
				else if (qs.isCompleted())
				{
					htmltext = "completed.html";
				}
				break;
			}
			case BERNA:
			{
				if (qs.isStarted())
				{
					if (qs.isCond(3))
					{
						htmltext = "berna.html";
					}
				}
				else if (qs.isCompleted())
				{
					htmltext = "completed.html";
				}
				break;
			}
		}
		return htmltext;
	}
}