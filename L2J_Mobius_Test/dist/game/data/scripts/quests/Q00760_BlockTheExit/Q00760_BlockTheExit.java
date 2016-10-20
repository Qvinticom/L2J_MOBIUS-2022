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
package quests.Q00760_BlockTheExit;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * @author NviX
 */
public class Q00760_BlockTheExit extends Quest
{
	// NPC
	private static final int KURTIZ = 30870;
	// Monsters
	private static final int DARK_RIDER = 26102;
	// Items
	private static final int CURTIZ_REWARD_BOX = 46560;
	// Misc
	private static final int MIN_LEVEL = 99;
	private static final int MAX_LEVEL = 105;
	
	public Q00760_BlockTheExit()
	{
		super(760, Q00760_BlockTheExit.class.getSimpleName(), "Block the Exit");
		addStartNpc(KURTIZ);
		addTalkId(KURTIZ);
		addKillId(DARK_RIDER);
		addCondMinLevel(MIN_LEVEL, "no_level.html");
		addCondMaxLevel(MAX_LEVEL, "no_level.html");
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
			case "30870-7.html":
			{
				qs.startQuest();
				break;
			}
			case "30870-8.html":
			{
				giveItems(player, CURTIZ_REWARD_BOX, 1);
				qs.exitQuest(QuestType.DAILY, true);
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
		if (qs == null)
		{
			return htmltext;
		}
		
		if (qs.isCreated())
		{
			htmltext = "30870-1.html";
		}
		else if (qs.isStarted())
		{
			if (qs.isCond(1))
			{
				htmltext = "30870-4.html";
			}
			else if (qs.isCond(2))
			{
				htmltext = "30870-5.html";
			}
		}
		else if (qs.isCompleted())
		{
			if (!qs.isNowAvailable())
			{
				htmltext = "30870-6.html";
			}
			else
			{
				qs.setState(State.CREATED);
				htmltext = "30870-1.html";
			}
		}
		return htmltext;
	}
}
