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
package quests.Q00490_DutyOfTheSurvivor;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemChanceHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * Duty of the Survivor (10490)
 * @author Stayway
 */
public class Q00490_DutyOfTheSurvivor extends Quest
{
	// NPC
	private static final int VOLLODOS = 30137;
	// Items
	private static final int PUTRIFIED_EXTRACT = 34059;
	private static final int ROTTEN_BLOOD = 34060;
	
	private static final Map<Integer, ItemChanceHolder> MONSTERS = new HashMap<>();
	static
	{
		MONSTERS.put(23162, new ItemChanceHolder(PUTRIFIED_EXTRACT, 0.59, 1)); // Corpse Devourer
		MONSTERS.put(23163, new ItemChanceHolder(PUTRIFIED_EXTRACT, 0.59, 1)); // Corpse Absorber
		MONSTERS.put(23164, new ItemChanceHolder(PUTRIFIED_EXTRACT, 0.59, 1)); // Corpse Shredder
		MONSTERS.put(23165, new ItemChanceHolder(PUTRIFIED_EXTRACT, 0.70, 1)); // Plagueworm
		MONSTERS.put(23166, new ItemChanceHolder(PUTRIFIED_EXTRACT, 0.64, 1)); // Contaminated Rottenroot
		MONSTERS.put(23167, new ItemChanceHolder(PUTRIFIED_EXTRACT, 0.64, 1)); // Decayed Spore
		MONSTERS.put(23168, new ItemChanceHolder(ROTTEN_BLOOD, 0.70, 1)); // Swamp Tracker
		MONSTERS.put(23169, new ItemChanceHolder(ROTTEN_BLOOD, 0.70, 1)); // Swamp Assassin
		MONSTERS.put(23170, new ItemChanceHolder(ROTTEN_BLOOD, 0.70, 1)); // Swamp Watcher
		MONSTERS.put(23171, new ItemChanceHolder(ROTTEN_BLOOD, 0.54, 1)); // Corpse Collector
		MONSTERS.put(23172, new ItemChanceHolder(ROTTEN_BLOOD, 0.54, 1)); // Delegate of Blood
		MONSTERS.put(23173, new ItemChanceHolder(ROTTEN_BLOOD, 0.54, 1)); // Blood Aide
	}
	// Rewards
	private static final int EXP_REWARD = 145557000;
	private static final int SP_REWARD = 34933;
	// Others
	private static final int MIN_LEVEL = 85;
	private static final int MAX_LEVEL = 89;
	
	public Q00490_DutyOfTheSurvivor()
	{
		super(490, Q00490_DutyOfTheSurvivor.class.getSimpleName(), "Duty Of The Survivor");
		addStartNpc(VOLLODOS);
		addTalkId(VOLLODOS);
		addKillId(MONSTERS.keySet());
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
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
			case "30137-02.htm":
			case "30137-03.htm":
			case "30137-04.htm":
			{
				htmltext = event;
				break;
			}
			case "30137-05.htm": // start the quest
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30137-06.html":
			{
				if (qs.isCond(2))
				{
					giveAdena(player, 505062, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(QuestType.DAILY, true);
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
				if ((player.getLevel() < MIN_LEVEL) && (player.getLevel() > MAX_LEVEL))
				{
					htmltext = "no_level.html";
				}
				else
				{
					htmltext = "30137-01.htm";
				}
				break;
			}
			
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "30137-07.html";
				}
				if (qs.isCond(2))
				{
					htmltext = "30137-06.html";
					giveAdena(player, 505062, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(QuestType.DAILY, true);
				}
				break;
			}
			case State.COMPLETED:
			{
				if ((player.getLevel() < MIN_LEVEL) && (player.getLevel() > MAX_LEVEL))
				{
					htmltext = "no_level.html";
				}
				else if (!qs.isNowAvailable())
				{
					htmltext = "30137-08.html";
				}
				else
				{
					qs.setState(State.CREATED);
					htmltext = "30137-01.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if (qs != null)
		{
			final ItemChanceHolder item = MONSTERS.get(npc.getId());
			if (giveItemRandomly(qs.getPlayer(), npc, item.getId(), item.getCount(), 20, item.getChance(), true) //
				&& (getQuestItemsCount(qs.getPlayer(), PUTRIFIED_EXTRACT) >= 20) //
				&& (getQuestItemsCount(qs.getPlayer(), ROTTEN_BLOOD) >= 20))
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
