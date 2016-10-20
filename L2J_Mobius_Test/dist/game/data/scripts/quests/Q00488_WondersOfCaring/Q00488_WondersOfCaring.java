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
package quests.Q00488_WondersOfCaring;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * Wonders of Caring (488)
 * @URL https://l2wiki.com/Wonders_of_Caring
 * @author Gigi
 */
public class Q00488_WondersOfCaring extends Quest
{
	// NPCs
	private static final int ADVANTURES_GUIDE = 32327;
	private static final int DOLPHREN = 32880;
	// MONSTERS
	private static final int CNIMERA_PRICE = 20965;
	private static final int MUTATED_CREATION = 20966;
	private static final int CREATURE_OF_THE_PAST = 20967;
	private static final int FORGOTTEN_FACE = 20968;
	private static final int GIANTS_SHADOW = 20969;
	private static final int SOLDIER_OS_ANCIENT_TIMESS = 20970;
	private static final int WARRIOR_OS_ANCIENT_TIME = 20971;
	private static final int SHAMAN_ON_ANCIENT_TIMES = 20972;
	private static final int FORGOTTEN_ANCIENT_PEOPLE = 20973;
	// Items
	private static final int RELIC_BOX = 19500;
	private static final ItemHolder ADENA = new ItemHolder(57, 283800);
	// Misc
	private static final int MIN_LEVEL = 75;
	private static final int MAX_LEVEL = 79;
	// Reward
	private static final int EXP_REWARD = 22901550;
	private static final int SP_REWARD = 5496;
	
	public Q00488_WondersOfCaring()
	{
		super(488, Q00488_WondersOfCaring.class.getSimpleName(), "Wonders of Caring");
		addStartNpc(ADVANTURES_GUIDE);
		addTalkId(ADVANTURES_GUIDE, DOLPHREN);
		registerQuestItems(RELIC_BOX);
		addKillId(CNIMERA_PRICE, MUTATED_CREATION, CREATURE_OF_THE_PAST, FORGOTTEN_FACE, GIANTS_SHADOW, SOLDIER_OS_ANCIENT_TIMESS, WARRIOR_OS_ANCIENT_TIME, SHAMAN_ON_ANCIENT_TIMES, FORGOTTEN_ANCIENT_PEOPLE);
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
			case DOLPHREN:
			{
				if (qs.isCond(2) && (getQuestItemsCount(player, RELIC_BOX) >= 50))
				{
					takeItems(player, RELIC_BOX, -1);
					giveItems(player, ADENA);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = "32880-01.html";
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
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, monster, RELIC_BOX, 1, 50, 0.2, true))
		{
			qs.setCond(2, true);
		}
		return super.onKill(monster, killer, isSummon);
	}
}