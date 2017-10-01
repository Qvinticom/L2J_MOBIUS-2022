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
package quests.Q00783_VestigeOfTheMagicPower;

import com.l2jmobius.gameserver.enums.Faction;
import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

import quests.Q10455_ElikiasLetter.Q10455_ElikiasLetter;

/**
 * Vestige of the Magic Power (783)
 * @URL https://l2wiki.com/Vestige_of_the_Magic_Power
 * @author Gigi
 */
public class Q00783_VestigeOfTheMagicPower extends Quest
{
	// NPCs
	private static final int LEONA_BLACKBIRD = 31595;
	// Monsters
	private static final int[] MONSTERS =
	{
		23384, // Smaug
		23385, // Lunatikan
		23386, // Jabberwok
		23387, // Kanzaroth
		23388, // Kandiloth
		23395, // Garion
		23396, // Garion Neti
		23397, // Desert Wendigo
		23398, // Koraza
		23399 // Bend Beetle
	};
	// Misc
	private static final int MIN_LEVEL = 99;
	private static final int HIGH_GRADE_FRAGMENT_OF_CHAOS = 46557;
	private static final int BASIC_SUPPLY_BOX = 47356;
	private static final int INTERMEDIATE_SUPPLY_BOX = 47357;
	private static final int ADVANCED_SUPPLY_BOX = 47358;
	
	public Q00783_VestigeOfTheMagicPower()
	{
		super(783);
		addStartNpc(LEONA_BLACKBIRD);
		addTalkId(LEONA_BLACKBIRD);
		addKillId(MONSTERS);
		registerQuestItems(HIGH_GRADE_FRAGMENT_OF_CHAOS);
		addCondMinLevel(MIN_LEVEL, "31595-00.htm");
		addCondCompletedQuest(Q10455_ElikiasLetter.class.getSimpleName(), "31595-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		switch (event)
		{
			case "31595-02.htm":
			case "31595-03.htm":
			{
				htmltext = event;
				break;
			}
			case "31595-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "31595-07.html":
			{
				if (qs.isCond(2))
				{
					if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) == 0)
					{
						player.addFactionPoints(Faction.BLACKBIRD_CLAN, 100);
						giveItems(player, BASIC_SUPPLY_BOX, 1);
						addExpAndSp(player, 4845395970L, 11628900);
					}
					else if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) <= 1)
					{
						player.addFactionPoints(Faction.BLACKBIRD_CLAN, 200);
						giveItems(player, INTERMEDIATE_SUPPLY_BOX, 1);
						addExpAndSp(player, 9690791940L, 23257800);
					}
					else if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 2)
					{
						player.addFactionPoints(Faction.BLACKBIRD_CLAN, 300);
						giveItems(player, ADVANCED_SUPPLY_BOX, 1);
						addExpAndSp(player, 14536187910L, 34886700);
					}
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		if (npc.getId() == LEONA_BLACKBIRD)
		{
			switch (qs.getState())
			{
				case State.CREATED:
				{
					htmltext = "31595-01.htm";
					break;
				}
				case State.STARTED:
				{
					if (qs.isCond(1))
					{
						htmltext = "31595-05.html";
					}
					else if (qs.isCond(2))
					{
						htmltext = "31595-06.html";
					}
					break;
				}
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
			if ((killer.getFactionLevel(Faction.BLACKBIRD_CLAN) == 0) && (getQuestItemsCount(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS) < 300))
			{
				if (getQuestItemsCount(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS) == 300)
				{
					qs.setCond(2, true);
				}
				giveItems(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if ((killer.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 1) && (getQuestItemsCount(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS) < 600))
			{
				if (getQuestItemsCount(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS) == 600)
				{
					qs.setCond(2, true);
				}
				giveItems(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if ((killer.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 2) && (getQuestItemsCount(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS) < 900))
			{
				if (getQuestItemsCount(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS) == 900)
				{
					qs.setCond(2, true);
				}
				giveItems(killer, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}