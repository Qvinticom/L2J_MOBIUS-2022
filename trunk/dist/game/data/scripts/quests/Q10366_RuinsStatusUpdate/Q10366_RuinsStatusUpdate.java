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
package quests.Q10366_RuinsStatusUpdate;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

import quests.Q10365_SeekerEscort.Q10365_SeekerEscort;

/**
 * Ruins status update (10366)
 * @author spider
 */
public class Q10366_RuinsStatusUpdate extends Quest
{
	// NPCs
	private static final int SEBION = 32978;
	private static final int FRANCO = 32153;
	private static final int VALFAR = 32146;
	private static final int RIVIAN = 32147;
	private static final int TOOK = 32150;
	private static final int MOKA = 32157;
	private static final int DEVON = 32160;
	// Rewards
	private static final int ADENA_REWARD = 750;
	private static final int EXP_REWARD = 150000;
	private static final int SP_REWARD = 36;
	// Requirements
	private static final int MIN_LEVEL = 16;
	private static final int MAX_LEVEL = 25;
	
	public Q10366_RuinsStatusUpdate()
	{
		super(10366, Q10366_RuinsStatusUpdate.class.getSimpleName(), "Ruins status update");
		addStartNpc(SEBION);
		addTalkId(FRANCO, VALFAR, RIVIAN, TOOK, MOKA, DEVON, SEBION);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
		addCondCompletedQuest(Q10365_SeekerEscort.class.getSimpleName(), "no_prequest.html");
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
			case "32978-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32978-03.htm": // FIXME: add race-specified texts
			{
				qs.startQuest();
				qs.setCond(2);
				htmltext = event;
				break;
			}
			case "33750-02.html":
			{
				htmltext = event;
				break;
			}
			case "33750-03.html":
			{
				if (qs.getCond() > 1)
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
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
				htmltext = npc.getId() == SEBION ? "32978-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case SEBION:
					{
						htmltext = "32978-04.html";
						break;
					}
					case VALFAR:// FIXME: add all races texts
					case RIVIAN:
					case TOOK:
					case MOKA:
					case DEVON:
					case FRANCO:
					{
						htmltext = "33750-01.html";
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
