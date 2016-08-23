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
package quests.Q10437_TheSealOfPunishmentPlainsOfTheLizardmen;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.enums.CategoryType;
import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.util.Util;

/**
 * The Seal of Punishment: Plains Of The Lizardmen (10437)
 * @author Stayway
 */
public class Q10437_TheSealOfPunishmentPlainsOfTheLizardmen extends Quest
{
	// NPCs
	private static final int LAKI = 32742;
	// Item
	private static final int EMBRYO_FRAGMENT = 36687;
	// Misc
	private static final Map<Integer, Integer> TANTA_LIZARDMAN = new HashMap<>();
	static
	{
		TANTA_LIZARDMAN.put(22768, 888); // Tanta Lizardman Scouts
		TANTA_LIZARDMAN.put(22769, 888); // Tanta Lizardman Warriors
		TANTA_LIZARDMAN.put(22770, 888); // Tanta Lizardman Soldiers
		TANTA_LIZARDMAN.put(22771, 888); // Tanta Lizardman Berserkers
		TANTA_LIZARDMAN.put(22772, 888); // Tanta Lizardman Archers
		TANTA_LIZARDMAN.put(22773, 888); // Tanta Lizardman Wizards
		TANTA_LIZARDMAN.put(22774, 888); // Tanta Lizardman Summoners
	}
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	
	public Q10437_TheSealOfPunishmentPlainsOfTheLizardmen()
	{
		super(10437);
		addStartNpc(LAKI);
		addTalkId(LAKI);
		addKillId(TANTA_LIZARDMAN.keySet());
		registerQuestItems(EMBRYO_FRAGMENT);
		addCondMaxLevel(MAX_LEVEL, "noLevel.html");
		addCondMinLevel(MIN_LEVEL, "noLevel.html");
		addCondNotRace(Race.ERTHEIA, "noErtheia.html");
		addCondInCategory(CategoryType.BOW_MASTER, "nocond.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		String htmltext = event;
		switch (event)
		{
			case "32742-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32742-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "reward_9546":
			case "reward_9547":
			case "reward_9548":
			case "reward_9549":
			case "reward_9550":
			case "reward_9551":
			{
				if (!qs.isCond(2))
				{
					break;
				}
				final int stoneId = Integer.parseInt(event.replaceAll("reward_", ""));
				giveItems(player, stoneId, 15);
				giveStoryQuestReward(player, 60);
				final long count = getQuestItemsCount(player, EMBRYO_FRAGMENT);
				if ((count >= 50) && (count < 100))
				{
					addExpAndSp(player, 28240800, 6777);
				}
				else if ((count >= 100) && (count < 200))
				{
					addExpAndSp(player, 56481600, 13554);
				}
				else if ((count >= 200) && (count < 300))
				{
					addExpAndSp(player, 84722400, 20331);
				}
				else if ((count >= 300) && (count < 400))
				{
					addExpAndSp(player, 112963200, 27108);
				}
				else if ((count >= 400) && (count < 500))
				{
					addExpAndSp(player, 141204000, 33835);
				}
				else if ((count >= 500) && (count < 600))
				{
					addExpAndSp(player, 169444800, 40662);
				}
				else if ((count >= 600) && (count < 700))
				{
					addExpAndSp(player, 197685600, 47439);
				}
				else if ((count >= 700) && (count < 800))
				{
					addExpAndSp(player, 225926400, 54216);
				}
				else if ((count >= 800) && (count < 900))
				{
					addExpAndSp(player, 254167200, 60993);
				}
				else if (count >= 900)
				{
					addExpAndSp(player, 282408000, 67770);
				}
				qs.exitQuest(false, true);
				htmltext = "32742-06.html";
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
		switch (qs.getState())
		{
			
			case State.CREATED:
			{
				if (npc.getId() == LAKI)
				{
					htmltext = "32742-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if ((qs.isCond(1)) && (npc.getId() == LAKI))
				{
					htmltext = "32742-04.html";
					break;
				}
				else if (qs.isCond(2))
				{
					htmltext = "32742-05.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getNoQuestMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	private void giveItem(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs != null)
		{
			giveItems(player, EMBRYO_FRAGMENT, 1);
			if (qs.isCond(1))
			{
				if (getQuestItemsCount(player, EMBRYO_FRAGMENT) >= 50)
				{
					qs.setCond(2, true);
				}
				else
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (killer.isInParty())
		{
			for (L2PcInstance member : killer.getParty().getMembers())
			{
				if (Util.checkIfInRange(1500, npc, member, false))
				{
					giveItem(npc, member);
				}
			}
		}
		else
		{
			giveItem(npc, killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
}