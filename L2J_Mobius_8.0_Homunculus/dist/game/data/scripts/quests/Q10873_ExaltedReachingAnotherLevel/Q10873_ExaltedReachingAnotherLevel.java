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
package quests.Q10873_ExaltedReachingAnotherLevel;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10823_ExaltedOneWhoShattersTheLimit.Q10823_ExaltedOneWhoShattersTheLimit;

/**
 * Exalted, Reaching Another Level (10873)
 * @URL https://l2wiki.com/Exalted,_Reaching_Another_Level
 * @author Dmitri
 */
public class Q10873_ExaltedReachingAnotherLevel extends Quest
{
	// NPC
	private static final int LIONEL = 33907;
	// Items
	private static final int PROOF_OF_REPUTATION = 80826;
	private static final int LIONEL_MISSION_LIST_4 = 47829;
	// Rewards
	private static final int DIGNITY_OF_THE_EXALTED = 47852;
	private static final int VITALITY_OF_THE_EXALTED = 47854;
	// Misc
	private static final int MIN_LEVEL = 103;
	private static final int MIN_COMPLETE_LEVEL = 105;
	private static final int PROOF_OF_REPUTATION_NEEDED = 80000;
	// Monsters
	private static final int[] MONSTERS =
	{
		// Hellbound monsters
		23811, // Cantera Tanya
		23812, // Cantera Deathmoz
		23813, // Cantera Floxis
		23814, // Cantera Belika
		23815, // Cantera Bridget
		23354, // Decay Hannibal
		23355, // Armor Beast
		23356, // Klein Soldier
		23357, // Disorder Warrior
		23360, // Bizuard
		23361, // Mutated Fly
		24511, // Lunatikan
		24515, // Kandiloth
		24512, // Garion Neti
		24513, // Desert Wendigo
		24514, // Koraza
	};
	
	public Q10873_ExaltedReachingAnotherLevel()
	{
		super(10873);
		addStartNpc(LIONEL);
		addTalkId(LIONEL);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "33907-00.htm");
		addCondCompletedQuest(Q10823_ExaltedOneWhoShattersTheLimit.class.getSimpleName(), "33907-00.htm");
		registerQuestItems(LIONEL_MISSION_LIST_4, PROOF_OF_REPUTATION);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		switch (event)
		{
			case "33907-03.htm":
			case "33907-04.htm":
			{
				htmltext = event;
				break;
			}
			case "33907-05.html":
			{
				if (qs.isCreated())
				{
					giveItems(player, LIONEL_MISSION_LIST_4, 1);
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = event;
				}
				break;
			}
			case "33907-05a.html":
			{
				qs.setMemoState(2);
				htmltext = event;
				break;
			}
			case "33907-08.html":
			{
				if (qs.isCond(2) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
				{
					giveItems(player, DIGNITY_OF_THE_EXALTED, 1);
					giveItems(player, VITALITY_OF_THE_EXALTED, 1);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "33907-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getMemoState())
				{
					case 1:
					{
						if (qs.isCond(2) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
						{
							htmltext = "33907-07.html";
						}
						else
						{
							htmltext = "33907-06.html";
						}
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
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(PlayerInstance player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			if (getQuestItemsCount(player, PROOF_OF_REPUTATION) < PROOF_OF_REPUTATION_NEEDED)
			{
				giveItemRandomly(player, PROOF_OF_REPUTATION, 1, PROOF_OF_REPUTATION_NEEDED, 1, true);
			}
			if ((getQuestItemsCount(player, PROOF_OF_REPUTATION) >= PROOF_OF_REPUTATION_NEEDED) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
			{
				qs.setCond(2, true);
			}
		}
	}
}
