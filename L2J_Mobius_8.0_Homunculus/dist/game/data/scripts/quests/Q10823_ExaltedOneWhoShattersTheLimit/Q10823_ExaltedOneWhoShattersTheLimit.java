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
package quests.Q10823_ExaltedOneWhoShattersTheLimit;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10817_ExaltedOneWhoOvercomesTheLimit.Q10817_ExaltedOneWhoOvercomesTheLimit;

/**
 * Exalted, One Who Shatters the Limit (10823)
 * @URL https://l2wiki.com/Exalted,_One_Who_Shatters_the_Limit
 * @author Mobius
 */
public class Q10823_ExaltedOneWhoShattersTheLimit extends Quest
{
	// NPC
	private static final int LIONEL = 33907;
	// Items
	private static final int PROOF_OF_PREPARATION = 80824;
	private static final int LIONEL_MISSION_LIST_3 = 45637;
	// Rewards
	private static final int EXALTED_CLOAK = 37763;
	private static final int OBTAIN_EXALTED_STATUS = 45638;
	private static final int EXALTED_TIARA = 45644;
	private static final int DIGNITY_OF_THE_EXALTED = 45924;
	private static final int BLESSING_OF_THE_EXALTED = 45926;
	private static final int SUMMON_BATTLE_POTION = 45927;
	private static final int FATE_OF_THE_EXALTED = 46036;
	// Misc
	private static final int MIN_LEVEL = 102;
	private static final int MIN_COMPLETE_LEVEL = 103;
	private static final int PROOF_OF_PREPARATION_NEEDED = 40000;
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
	
	public Q10823_ExaltedOneWhoShattersTheLimit()
	{
		super(10823);
		addStartNpc(LIONEL);
		addTalkId(LIONEL);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "33907-07.html");
		addCondCompletedQuest(Q10817_ExaltedOneWhoOvercomesTheLimit.class.getSimpleName(), "33907-02.html");
		registerQuestItems(LIONEL_MISSION_LIST_3, PROOF_OF_PREPARATION);
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
					giveItems(player, LIONEL_MISSION_LIST_3, 1);
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "33907-09.html":
			{
				if (qs.isCond(2) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
				{
					giveItems(player, EXALTED_CLOAK, 1);
					giveItems(player, OBTAIN_EXALTED_STATUS, 1);
					giveItems(player, EXALTED_TIARA, 1);
					giveItems(player, DIGNITY_OF_THE_EXALTED, 1);
					giveItems(player, BLESSING_OF_THE_EXALTED, 1);
					giveItems(player, SUMMON_BATTLE_POTION, 1);
					giveItems(player, FATE_OF_THE_EXALTED, 1);
					// Give Exalted status here?
					// https://l2wiki.com/Noblesse
					player.setNobleLevel(2);
					player.broadcastUserInfo();
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
				if (qs.isCond(2) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
				{
					htmltext = "33907-08.html";
				}
				else
				{
					htmltext = "33907-06.html";
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
			if (getQuestItemsCount(player, PROOF_OF_PREPARATION) < PROOF_OF_PREPARATION_NEEDED)
			{
				giveItemRandomly(player, PROOF_OF_PREPARATION, 1, PROOF_OF_PREPARATION_NEEDED, 1, true);
			}
			if ((getQuestItemsCount(player, PROOF_OF_PREPARATION) >= PROOF_OF_PREPARATION_NEEDED) && (player.getLevel() >= MIN_COMPLETE_LEVEL))
			{
				qs.setCond(2, true);
			}
		}
	}
}
