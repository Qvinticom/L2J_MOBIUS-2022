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
package quests.Q00489_InThisQuietPlace;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * In This Quiet Place (489)
 * @URL https://l2wiki.com/In_This_Quiet_Place
 * @author Gigi
 */
public class Q00489_InThisQuietPlace extends Quest
{
	// NPCs
	private static final int ADVANTURES_GUIDE = 32327;
	private static final int BASTIAN = 31280;
	// MONSTERS
	private static final int GRAVE_SCARAB = 21646;
	private static final int SCAVENGER_SCARAB = 21647;
	private static final int GRAVE_ANT = 21648;
	private static final int SCAVANGER_ANT = 21649;
	private static final int SHRINE_KNIGHT = 21650;
	private static final int SHRINE_ROYAL_GUARD = 21651;
	// Items
	private static final int TRACE_OF_EVIL_SPIRIT = 19501;
	private static final ItemHolder ADENA = new ItemHolder(57, 283800);
	// Misc
	private static final int MIN_LEVEL = 75;
	private static final int MAX_LEVEL = 79;
	// Reward
	private static final int EXP_REWARD = 19890000;
	private static final int SP_REWARD = 4773;
	
	public Q00489_InThisQuietPlace()
	{
		super(489, Q00489_InThisQuietPlace.class.getSimpleName(), "In This Quiet Place");
		addStartNpc(ADVANTURES_GUIDE);
		addTalkId(ADVANTURES_GUIDE, BASTIAN);
		registerQuestItems(TRACE_OF_EVIL_SPIRIT);
		addKillId(GRAVE_SCARAB, SCAVENGER_SCARAB, GRAVE_ANT, SCAVANGER_ANT, SHRINE_KNIGHT, SHRINE_ROYAL_GUARD);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
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
			case "32327-02.htm":
			case "32327-03.htm":
			{
				htmltext = event;
				break;
			}
			case "32327-04.htm":
			{
				qs.startQuest();
				htmltext = event;
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
			case ADVANTURES_GUIDE:
			{
				if (qs.isCreated())
				{
					htmltext = "32327-01.htm";
				}
				else if (qs.getCond() > 0)
				{
					htmltext = "32327-05.html";
				}
				else if (qs.isCompleted() && !qs.isNowAvailable())
				{
					htmltext = "32327-07.html";
				}
				break;
			}
			case BASTIAN:
			{
				if (qs.isCond(2) && (getQuestItemsCount(player, TRACE_OF_EVIL_SPIRIT) >= 77))
				{
					takeItems(player, TRACE_OF_EVIL_SPIRIT, -1);
					giveItems(player, ADENA);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = "31280-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc monster, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, monster, TRACE_OF_EVIL_SPIRIT, 1, 77, 0.2, true))
		{
			qs.setCond(2, true);
		}
		return super.onKill(monster, killer, isSummon);
	}
}