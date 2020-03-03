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
package quests.Q10434_TheSealOfPunishmentSelMahumTrainingGrounds;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

/**
 * The Seal of Punishment: Sel Mahum Training Grounds (10434)
 * @author Stayway
 */
public class Q10434_TheSealOfPunishmentSelMahumTrainingGrounds extends Quest
{
	// NPCs
	private static final int RUA = 33841;
	private static final int[] SEL_MAHUMS =
	{
		18908, // Sel Mahum Chef
		22775, // Sel Mahum Drill Sergeant (Wizard)
		22776, // Sel Mahum Training Officer
		22777, // Sel Mahum Drill Sergeant(Warrior)
		22778, // Sel Mahum Drill Sergeant(Archer)
		22779, // Sel Mahum Warrior
		22780, // Sel Mahum Recruit(Wizard)
		22781, // Sel Mahum Soldier(Wizard)
		22782, // Sel Mahum Recruit(Warrior)
		22783, // Sel Mahum Soldier(Warrior)
		22784, // Sel Mahum Recruit(Archer)
		22785, // Sel Mahum Soldier(Archer)
		22786, // Sel Mahum Squad Leader(Wizard)
		22787, // Sel Mahum Squad Leader(Warrior)
		22788, // Sel Mahum Squad Leader(Archer)
	};
	// Item
	private static final int EVIDENCE_OF_CONSPIRACY = 36692;
	// Misc
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	
	public Q10434_TheSealOfPunishmentSelMahumTrainingGrounds()
	{
		super(10434);
		addStartNpc(RUA);
		addTalkId(RUA);
		addKillId(SEL_MAHUMS);
		registerQuestItems(EVIDENCE_OF_CONSPIRACY);
		addCondMaxLevel(MAX_LEVEL, "noLevel.html");
		addCondMinLevel(MIN_LEVEL, "noLevel.html");
		addCondNotRace(Race.ERTHEIA, "noErtheia.html");
		addCondInCategory(CategoryType.WEAPON_MASTER, "no-class.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		String htmltext = event;
		switch (event)
		{
			case "33841-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33841-03.htm":
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
				final long count = getQuestItemsCount(player, EVIDENCE_OF_CONSPIRACY);
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
				htmltext = "33841-06.html";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == RUA)
				{
					htmltext = "33841-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if ((qs.isCond(1)) && (npc.getId() == RUA))
				{
					htmltext = "33841-04.html";
					break;
				}
				else if (qs.isCond(2))
				{
					htmltext = "33841-05.html";
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
	
	private void giveItem(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs != null)
		{
			giveItems(player, EVIDENCE_OF_CONSPIRACY, 1);
			if (qs.isCond(1))
			{
				if (getQuestItemsCount(player, EVIDENCE_OF_CONSPIRACY) >= 50)
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
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		if (killer.isInParty())
		{
			for (PlayerInstance member : killer.getParty().getMembers())
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