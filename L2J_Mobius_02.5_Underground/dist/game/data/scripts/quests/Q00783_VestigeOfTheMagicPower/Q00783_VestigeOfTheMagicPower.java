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

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10455_ElikiasLetter.Q10455_ElikiasLetter;

/**
 * Vestige of the Magic Power (783)
 * @URL https://l2wiki.com/Vestige_of_the_Magic_Power
 * @author Gigi
 */
public class Q00783_VestigeOfTheMagicPower extends Quest
{
	// NPC's
	private static final int LEONA_BLACKBIRD = 31595;
	// Monster's
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
	private static final int LEONAS_REWARD_BOX = 46558;
	private static final int BLOODIED_DEMONIC_TOME = 37893;
	
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
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
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
				if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) >= 250) && (getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) < 500))
				{
					addExpAndSp(player, 3876316782L, 9303137);
					giveItems(player, LEONAS_REWARD_BOX, 1);
					takeItems(player, HIGH_GRADE_FRAGMENT_OF_CHAOS, -1);
					giveItems(player, BLOODIED_DEMONIC_TOME, 1);
					qs.exitQuest(QuestType.REPEATABLE, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) >= 500) && (getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) < 750))
				{
					addExpAndSp(player, 7752633564L, 18606274);
					giveItems(player, LEONAS_REWARD_BOX, 2);
					takeItems(player, HIGH_GRADE_FRAGMENT_OF_CHAOS, -1);
					giveItems(player, BLOODIED_DEMONIC_TOME, 1);
					qs.exitQuest(QuestType.REPEATABLE, true);
					htmltext = event;
					break;
				}
				else if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) >= 750) && (getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) < 1000))
				{
					addExpAndSp(player, 11628950346L, 27909411);
					giveItems(player, LEONAS_REWARD_BOX, 3);
					takeItems(player, HIGH_GRADE_FRAGMENT_OF_CHAOS, -1);
					giveItems(player, BLOODIED_DEMONIC_TOME, 1);
					qs.exitQuest(QuestType.REPEATABLE, true);
					htmltext = event;
					break;
				}
				else if (getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) >= 1000)
				{
					addExpAndSp(player, 15505267128L, 37212548);
					giveItems(player, LEONAS_REWARD_BOX, 4);
					takeItems(player, HIGH_GRADE_FRAGMENT_OF_CHAOS, -1);
					giveItems(player, BLOODIED_DEMONIC_TOME, 1);
					qs.exitQuest(QuestType.REPEATABLE, true);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
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
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isStarted() && (getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) < 1000))
		{
			giveItems(player, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1);
			if (getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) >= 250)
			{
				qs.setCond(2, true);
			}
			else
			{
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
}