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
package quests.Q00470_DivinityProtector;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * Divinity Protector (470)
 * @URL https://l2wiki.com/Divinity_Protector
 * @author Gigi
 */
public class Q00470_DivinityProtector extends Quest
{
	// NPC
	private static final int ADVANTURES_GUIDE = 32327;
	private static final int AGRIPEL = 31348;
	// MONSTERS
	private static final int[] MOBS =
	{
		21520, // Eye of Splendor
		21521, // Claws of Splendor
		21523, // Flash of Splendor
		21524, // Blade of Splendor
		21526, // Wisdom of Splendor
		21527, // Fury of Splendor
		21529, // Soul of Splendor
		21530, // Victory of Splendor
		21531, // Punishment of Splendor
		21532, // Shout of Splendor
		21533, // Alliance of Splendor
		21535, // Signet of Splendor
		21536, // Crown of Splendor
		21537, // Fang of Splendor
		21539, // Wailing of Splendor
		21541, // Pilgrim of Splendor
		21542, // Pilgrim's Disciple
		21543, // Pilgrim's Servant
		21545, // Judge of Fire
		21546 // Judge of Light
	};
	// Items
	private static final int REMNANT_ASH = 19489; // Remnant Ash
	private static final ItemHolder ADENA = new ItemHolder(57, 194000); // Adena
	// Misc
	private static final int MIN_LEVEL = 60;
	private static final int MAX_LEVEL = 64;
	// Reward
	private static final int EXP_REWARD = 1879400;
	private static final int SP_REWARD = 451;
	
	public Q00470_DivinityProtector()
	{
		super(470, Q00470_DivinityProtector.class.getSimpleName(), "Divinity Protector");
		addStartNpc(ADVANTURES_GUIDE);
		addTalkId(ADVANTURES_GUIDE, AGRIPEL);
		registerQuestItems(REMNANT_ASH);
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
				else if (qs.isCompleted())
				{
					if (!qs.isNowAvailable())
					{
						htmltext = "32327-07.html";
					}
				}
				break;
			}
			case AGRIPEL:
			{
				if ((qs.isCond(2)) && (getQuestItemsCount(player, REMNANT_ASH) >= 20))
				{
					takeItems(player, REMNANT_ASH, 20);
					giveItems(player, ADENA);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = "31348-01.html";
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
		if ((qs != null) && qs.isCond(1))
		{
			if (giveItemRandomly(killer, npc, REMNANT_ASH, 1, 20, 0.1, true))
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}