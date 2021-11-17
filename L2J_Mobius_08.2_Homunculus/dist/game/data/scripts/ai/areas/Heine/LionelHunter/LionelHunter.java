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
package ai.areas.Heine.LionelHunter;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.QuestState;

import ai.AbstractNpcAI;
import quests.Q10507_ObtainingNewPower.Q10507_ObtainingNewPower;
import quests.Q10811_ExaltedOneWhoFacesTheLimit.Q10811_ExaltedOneWhoFacesTheLimit;
import quests.Q10817_ExaltedOneWhoOvercomesTheLimit.Q10817_ExaltedOneWhoOvercomesTheLimit;
import quests.Q10823_ExaltedOneWhoShattersTheLimit.Q10823_ExaltedOneWhoShattersTheLimit;
import quests.Q10873_ExaltedReachingAnotherLevel.Q10873_ExaltedReachingAnotherLevel;
import quests.Q10879_ExaltedGuideToPower.Q10879_ExaltedGuideToPower;

/**
 * Lionel Hunter AI.
 * @author Sero, CostyKiller
 */
public class LionelHunter extends AbstractNpcAI
{
	// NPC
	private static final int LIONEL_HUNTER_HEINE = 33907;
	// Items
	private static final int SPELLBOOK_DIGNITY_OF_THE_EXALTED = 45922;
	private static final int SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV2 = 45923;
	private static final int SPELLBOOK_BELIEF_OF_THE_EXALTED = 45925;
	private static final int SPELLBOOK_FAVOR_OF_THE_EXALTED = 45928;
	private static final int OBTAIN_EXALTED_STATUS = 45638;
	private static final int EXALTED_TIARA = 45644;
	private static final int SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV3 = 45924;
	private static final int BLESSING_OF_THE_EXALTED = 45926;
	private static final int SUMMON_BATTLE_POTION = 45927;
	private static final int FATE_OF_THE_EXALTED = 46036;
	private static final int SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV4 = 47852;
	private static final int VITALITY_OF_THE_EXALTED = 47854;
	private static final int SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV5 = 47853;
	private static final int VITALITY_OF_THE_EXALTED_LV2 = 47855;
	private static final int DIGNITY_OF_THE_EXALTED_LV6 = 80970;
	private static final int FAVOR_OF_THE_EXLATED = 45870;
	
	private LionelHunter()
	{
		addStartNpc(LIONEL_HUNTER_HEINE);
		addTalkId(LIONEL_HUNTER_HEINE);
		addFirstTalkId(LIONEL_HUNTER_HEINE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs1 = player.getQuestState(Q10811_ExaltedOneWhoFacesTheLimit.class.getSimpleName());
		final QuestState qs2 = player.getQuestState(Q10817_ExaltedOneWhoOvercomesTheLimit.class.getSimpleName());
		final QuestState qs3 = player.getQuestState(Q10823_ExaltedOneWhoShattersTheLimit.class.getSimpleName());
		final QuestState qs4 = player.getQuestState(Q10873_ExaltedReachingAnotherLevel.class.getSimpleName());
		final QuestState qs5 = player.getQuestState(Q10879_ExaltedGuideToPower.class.getSimpleName());
		final QuestState qs6 = player.getQuestState(Q10507_ObtainingNewPower.class.getSimpleName());
		switch (event)
		{
			case "33907.htm":
			case "33907-01.htm":
			case "33907-02.htm":
			case "33907-03.htm":
			case "33907-04.htm":
			case "33907-05.htm":
			{
				htmltext = event;
				break;
			}
			case "33907-book1.html":
			{
				if ((qs1 != null) && qs1.isCompleted())
				{
					giveItems(player, SPELLBOOK_DIGNITY_OF_THE_EXALTED, 1);
					htmltext = event;
				}
				else
				{
					htmltext = "33907-not-completed.html";
				}
				break;
			}
			case "33907-book2.html":
			{
				if ((qs2 != null) && qs2.isCompleted())
				{
					giveItems(player, SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV2, 1);
					giveItems(player, SPELLBOOK_BELIEF_OF_THE_EXALTED, 1);
					giveItems(player, SPELLBOOK_FAVOR_OF_THE_EXALTED, 1);
					htmltext = event;
				}
				else
				{
					htmltext = "33907-not-completed.html";
				}
				break;
			}
			case "33907-book3.html":
			{
				if ((qs3 != null) && qs3.isCompleted())
				{
					giveItems(player, OBTAIN_EXALTED_STATUS, 1);
					giveItems(player, EXALTED_TIARA, 1);
					giveItems(player, SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV3, 1);
					giveItems(player, BLESSING_OF_THE_EXALTED, 1);
					giveItems(player, SUMMON_BATTLE_POTION, 1);
					giveItems(player, FATE_OF_THE_EXALTED, 1);
					giveItems(player, FAVOR_OF_THE_EXLATED, 1);
					htmltext = event;
				}
				else
				{
					htmltext = "33907-not-completed.html";
				}
				break;
			}
			case "33907-book4.html":
			{
				if ((qs4 != null) && qs4.isCompleted())
				{
					giveItems(player, SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV4, 1);
					giveItems(player, VITALITY_OF_THE_EXALTED, 1);
					htmltext = event;
				}
				else
				{
					htmltext = "33907-not-completed.html";
				}
				break;
			}
			case "33907-book5.html":
			{
				if ((qs5 != null) && qs5.isCompleted())
				{
					giveItems(player, SPELLBOOK_DIGNITY_OF_THE_EXALTED_LV5, 1);
					giveItems(player, VITALITY_OF_THE_EXALTED_LV2, 1);
					htmltext = event;
				}
				else
				{
					htmltext = "33907-not-completed.html";
				}
				break;
			}
			case "33907-book6.html":
			{
				if ((qs6 != null) && qs6.isCompleted())
				{
					giveItems(player, DIGNITY_OF_THE_EXALTED_LV6, 1);
					htmltext = event;
				}
				else
				{
					htmltext = "33907-not-completed.html";
				}
				break;
				
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "33907.htm";
	}
	
	public static void main(String[] args)
	{
		new LionelHunter();
	}
}