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
package quests.Q00775_RetrievingTheChaosFragment;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10455_ElikiasLetter.Q10455_ElikiasLetter;

/**
 * Retrieving the Fragment of Chaos (775)
 * @URL https://l2wiki.com/Retrieving_the_Fragment_of_Chaos
 * @author Gigi
 */
public class Q00775_RetrievingTheChaosFragment extends Quest
{
	// NPC's
	private static final int LEONA_BLACKBIRD = 31595;
	// Monster's
	private static final int[] MONSTERS =
	{
		23388, // Kandiloth
		23387, // Kanzaroth
		23385, // Lunatikan
		23384, // Smaug
		23386, // Jabberwok
		23395, // Garion
		23397, // Desert Wendigo
		23399, // Bend Beetle
		23398, // Koraza
		23395, // Garion
		23396, // Garion Neti
		23357, // Disorder Warrior
		23356, // Klien Soldier
		23361, // Mutated Fly
		23358, // Blow Archer
		23355, // Armor Beast
		23360, // Bizuard
		23354, // Dacey Hannibal
		23357, // Disorder Warrior
		23363, // Amos Officer
		23364, // Amos Master
		23362, // Amos Soldier
		23365, // Ailith Hunter
	};
	// Misc
	private static final int MIN_LEVEL = 99;
	// Item
	private static final int CHAOS_FRAGMENT = 37766;
	private static final int BLOODIED_DEMONIC_TOME = 37893;
	private static final int LEONAS_REWARD_BOX = 46559;
	
	public Q00775_RetrievingTheChaosFragment()
	{
		super(775);
		addStartNpc(LEONA_BLACKBIRD);
		addTalkId(LEONA_BLACKBIRD);
		addKillId(MONSTERS);
		registerQuestItems(CHAOS_FRAGMENT);
		addCondMinLevel(MIN_LEVEL, "31595-00.htm");
		addCondCompletedQuest(Q10455_ElikiasLetter.class.getSimpleName(), "31595-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "31595-05.html":
			{
				htmltext = event;
				break;
			}
			case "31595-06.htm":
			{
				qs.startQuest();
				break;
			}
			case "31595-03.html":
			{
				if ((getQuestItemsCount(player, CHAOS_FRAGMENT) >= 200) && (getQuestItemsCount(player, CHAOS_FRAGMENT) < 300))
				{
					giveItems(player, LEONAS_REWARD_BOX, 1);
				}
				else if ((getQuestItemsCount(player, CHAOS_FRAGMENT) >= 300) && (getQuestItemsCount(player, CHAOS_FRAGMENT) < 400))
				{
					giveItems(player, LEONAS_REWARD_BOX, 2);
				}
				else if ((getQuestItemsCount(player, CHAOS_FRAGMENT) >= 400) && (getQuestItemsCount(player, CHAOS_FRAGMENT) < 500))
				{
					giveItems(player, LEONAS_REWARD_BOX, 3);
				}
				else if ((getQuestItemsCount(player, CHAOS_FRAGMENT) >= 500) && (getQuestItemsCount(player, CHAOS_FRAGMENT) < 600))
				{
					giveItems(player, LEONAS_REWARD_BOX, 4);
				}
				else if ((getQuestItemsCount(player, CHAOS_FRAGMENT) >= 600) && (getQuestItemsCount(player, CHAOS_FRAGMENT) < 700))
				{
					giveItems(player, LEONAS_REWARD_BOX, 5);
				}
				else if ((getQuestItemsCount(player, CHAOS_FRAGMENT) >= 700) && (getQuestItemsCount(player, CHAOS_FRAGMENT) < 800))
				{
					giveItems(player, LEONAS_REWARD_BOX, 6);
				}
				else if ((getQuestItemsCount(player, CHAOS_FRAGMENT) >= 800) && (getQuestItemsCount(player, CHAOS_FRAGMENT) < 900))
				{
					giveItems(player, LEONAS_REWARD_BOX, 7);
				}
				else if ((getQuestItemsCount(player, CHAOS_FRAGMENT) >= 900) && (getQuestItemsCount(player, CHAOS_FRAGMENT) < 1000))
				{
					giveItems(player, LEONAS_REWARD_BOX, 8);
				}
				else if (getQuestItemsCount(player, CHAOS_FRAGMENT) >= 1000)
				{
					giveItems(player, LEONAS_REWARD_BOX, 9);
				}
				if (getRandom(100) < 50)
				{
					giveItems(player, BLOODIED_DEMONIC_TOME, 1);
				}
				addExpAndSp(player, 463097250, 111143);
				qs.exitQuest(QuestType.DAILY, true);
				htmltext = event;
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
		if (npc.getId() == LEONA_BLACKBIRD)
		{
			switch (qs.getState())
			{
				case State.COMPLETED:
				{
					if (!qs.isNowAvailable())
					{
						htmltext = "31595-08.html";
						break;
					}
					qs.setState(State.CREATED);
				}
				case State.CREATED:
				{
					htmltext = "31595-01.htm";
					break;
				}
				case State.STARTED:
				{
					if (qs.isCond(1))
					{
						htmltext = "31595-07.html";
					}
					else if (qs.isCond(2))
					{
						if (getQuestItemsCount(player, CHAOS_FRAGMENT) < 200)
						{
							htmltext = "31595-02.html";
						}
						else
						{
							htmltext = "31595-09.html";
						}
					}
					break;
				}
			}
		}
		else if (qs.isCompleted() && !qs.isNowAvailable())
		{
			htmltext = "31595-08.html";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isStarted() && (getQuestItemsCount(killer, CHAOS_FRAGMENT) < 1000))
		{
			if (getQuestItemsCount(killer, CHAOS_FRAGMENT) == 100)
			{
				qs.setCond(2, true);
			}
			giveItems(killer, CHAOS_FRAGMENT, 1);
			playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, killer, isSummon);
	}
}