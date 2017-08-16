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
package quests.Q10361_RolesOfTheSeeker;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Roles of the Seeker (10361)
 * @author Gladicek
 */
public final class Q10361_RolesOfTheSeeker extends Quest
{
	// NPCs
	private static final int LAKCIS = 32977;
	private static final int CHESHA = 33449;
	// Misc
	private static final int MIN_LEVEL = 10;
	private static final int MAX_LEVEL = 20;
	
	public Q10361_RolesOfTheSeeker()
	{
		super(10361);
		addStartNpc(LAKCIS);
		addTalkId(LAKCIS, CHESHA);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "32977-05.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32977-02.htm":
			case "33449-02.html":
			{
				htmltext = event;
				break;
			}
			case "32977-03.html":
			{
				qs.startQuest();
				showOnScreenMsg(player, NpcStringId.ENTER_THE_RUINS_OF_YE_SAGIRA_THROUGH_THE_YE_SAGIRA_TELEPORT_DEVICE, ExShowScreenMessage.TOP_CENTER, 4500);
				htmltext = event;
				break;
			}
			case "33449-03.html":
			{
				if (qs.isStarted())
				{
					giveAdena(player, 340, true);
					addExpAndSp(player, 35000, 5);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == LAKCIS)
				{
					htmltext = "32977-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == LAKCIS ? "32977-04.html" : "33449-01.html";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == LAKCIS ? "32977-06.html" : "33449-04.html";
				break;
			}
		}
		return htmltext;
	}
}