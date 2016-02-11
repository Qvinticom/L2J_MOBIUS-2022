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
package quests.Q00757_TriolsMovement;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author hlwrave
 */
public class Q00757_TriolsMovement extends Quest
{
	// NPCs
	private static final int RADZEN = 33803;
	// Items
	private static final int SPIRIT = 36231;
	private static final int TOTEM = 36230;
	private static final int SPIRIT_COUNT = 100;
	private static final int TOTEM_COUNT = 100;
	// Mobs
	private static final int[] MOBS =
	{
		22140,
		22147,
		23278,
		23283,
		22146,
		22155,
		22141,
		22144,
		22139,
		22152,
		22153,
		22154,
	};
	
	public Q00757_TriolsMovement()
	{
		super(757, Q00757_TriolsMovement.class.getSimpleName(), "Triols Movement");
		addStartNpc(RADZEN);
		addTalkId(RADZEN);
		registerQuestItems(SPIRIT, TOTEM);
		addKillId(MOBS);
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
			case "accepted.html":
			{
				qs.setCond(1);
				qs.startQuest();
				break;
			}
			case "endquest.html":
			{
				if (qs.isCond(2))
				{
					qs.takeItems(SPIRIT, -100);
					qs.takeItems(TOTEM, -100);
					qs.giveItems(57, 745929);
					qs.giveItems(36232, 1);
					qs.addExpAndSp(301518549, 7236360);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = "endquest.html";
				}
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
		
		switch (qs.getState())
		{
			case State.COMPLETED:
			{
				htmltext = "You have completed this quest today, come back tomorow at 6:30!";
				break;
			}
			case State.CREATED:
			{
				if (player.getLevel() >= 97)
				{
					htmltext = "start.htm";
				}
				else
				{
					htmltext = "no_level.html";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "notcollected.htmll";
				}
				else if (qs.isCond(2))
				{
					htmltext = "collected.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1) && qs.isStarted())
		{
			
			if (Util.contains(MOBS, npc.getId()))
			{
				giveItemRandomly(killer, npc, TOTEM, 1, 100, 1.0, true);
			}
			if (Util.contains(MOBS, npc.getId()))
			{
				giveItemRandomly(killer, npc, SPIRIT, 1, 100, 1.0, true);
			}
			if ((getQuestItemsCount(killer, SPIRIT) == SPIRIT_COUNT) && (getQuestItemsCount(killer, TOTEM) == TOTEM_COUNT))
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}