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
package quests.Q00485_HotSpringWater;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * Hot Spring Water (485)
 * @URL https://l2wiki.com/Hot_Spring_Water
 * @author Gigi
 */
public class Q00485_HotSpringWater extends Quest
{
	// NPC
	private static final int ADVANTURES_GUIDE = 32327;
	private static final int WALDERAL = 30844;
	// MONSTERS
	private static final int[] MOBS =
	{
		21314, // Hot Springs Bandersnatchling
		21315, // Hot Springs Buffalo
		21316, // Hot Springs Flava
		21317, // Hot Springs Atroxspawn
		21318, // Hot Springs Antelope
		21319, // Hot Springs Nepenthes
		21320, // Hot Springs Yeti
		21321, // Hot Springs Atrox
		21322, // Hot Springs Bandersnatch
		21323, // Hot Springs Grendel
	};
	// Items
	private static final int HOT_SPRINGS_WATER_SAMPLE = 19497; // Hot Springs Water Sample
	private static final ItemHolder ADENA = new ItemHolder(57, 247410); // Adena
	// Misc
	private static final int MIN_LEVEL = 70;
	private static final int MAX_LEVEL = 74;
	// Reward
	private static final int EXP_REWARD = 9483000;
	private static final int SP_REWARD = 2275;
	
	public Q00485_HotSpringWater()
	{
		super(485, Q00485_HotSpringWater.class.getSimpleName(), "Hot Spring Water");
		addStartNpc(ADVANTURES_GUIDE);
		addTalkId(ADVANTURES_GUIDE, WALDERAL);
		registerQuestItems(HOT_SPRINGS_WATER_SAMPLE);
		addKillId(MOBS);
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
			case WALDERAL:
			{
				if ((qs.isCond(2)) && (getQuestItemsCount(player, HOT_SPRINGS_WATER_SAMPLE) >= 40))
				{
					takeItems(player, HOT_SPRINGS_WATER_SAMPLE, -1);
					giveItems(player, ADENA);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = "30844-01.html";
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
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, npc, HOT_SPRINGS_WATER_SAMPLE, 1, 40, 0.2, true))
		{
			qs.setCond(2, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}