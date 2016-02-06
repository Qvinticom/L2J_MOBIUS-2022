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
package quests.Q10388_ConspiracyBehindDoor;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * @author hlwrave
 */
public class Q10388_ConspiracyBehindDoor extends Quest
{
	// NPCs
	private static final int ELIA = 31329;
	private static final int KARGOS = 33821;
	private static final int HICHEN = 33820;
	private static final int RAZDEN = 33803;
	// Item
	private static final int VISITORS_BADGE = 8064;
	
	public Q10388_ConspiracyBehindDoor()
	{
		super(10388, Q10388_ConspiracyBehindDoor.class.getSimpleName(), "Conspiracy Behind Door");
		addStartNpc(ELIA);
		addTalkId(ELIA, KARGOS, HICHEN, RAZDEN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "go.htm":
			{
				qs.setCond(1);
				qs.startQuest();
				break;
			}
			case "toCond2.htm":
			{
				qs.setCond(2);
				break;
			}
			case "toCond3.htm":
			{
				qs.setCond(3);
				qs.giveItems(VISITORS_BADGE, 1);
				break;
			}
			case "final.htm":
			{
				qs.addExpAndSp(29638350, 2963835);
				qs.exitQuest(false, true);
				break;
			}
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
		
		final int npcId = npc.getId();
		switch (qs.getState())
		{
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
			case State.CREATED:
			{
				if (npcId == ELIA)
				{
					if (player.getLevel() >= 97)
					{
						htmltext = "start.htm";
					}
					else
					{
						htmltext = "nolvl.htm";
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npcId)
				{
					case KARGOS:
					{
						if (qs.isCond(1))
						{
							htmltext = "cond1.htm";
						}
						break;
					}
					case HICHEN:
					{
						if (qs.isCond(2))
						{
							htmltext = "cond2.htm";
						}
						break;
					}
					case RAZDEN:
					{
						if (qs.isCond(3))
						{
							htmltext = "cond3.htm";
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
}