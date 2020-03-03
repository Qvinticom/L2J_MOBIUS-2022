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
package quests.Q00823_DisappearedRaceNewFairy;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Disappeared Race, New Fairy (00823)
 * @URL https://l2wiki.com/Disappeared_Race,_New_Fairy
 * @author Gigi
 */
public class Q00823_DisappearedRaceNewFairy extends Quest
{
	// NPCs
	private static final int MIMYU = 30747;
	// Monsters
	private static final int[] MONSTERS =
	{
		23566, // Nymph Rose
		23567, // Nymph Rose
		23568, // Nymph Lily
		23569, // Nymph Lily
		23570, // Nymph Tulip
		23571, // Nymph Tulip
		23572, // Nymph Cosmos
		23573, // Nymph Cosmos
		23578 // Nymph Guardian
	};
	// Item's
	private static final int NYMPH_STAMEN = 46258;
	private static final int MIMIUS_REWARD_BOX = 46259;
	// Misc
	private static final int MIN_LEVEL = 100;
	
	public Q00823_DisappearedRaceNewFairy()
	{
		super(823);
		addStartNpc(MIMYU);
		addTalkId(MIMYU);
		addKillId(MONSTERS);
		registerQuestItems(NYMPH_STAMEN);
		addCondMinLevel(MIN_LEVEL, "30747-00.htm");
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
			case "30747-02.htm":
			case "30747-03.htm":
			case "30747-04.htm":
			case "30747-09.html":
			{
				htmltext = event;
				break;
			}
			case "30747-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30747-10.html":
			{
				if ((getQuestItemsCount(player, NYMPH_STAMEN) >= 300) && (getQuestItemsCount(player, NYMPH_STAMEN) < 600))
				{
					addExpAndSp(player, 3045319200L, 7308474);
					giveItems(player, MIMIUS_REWARD_BOX, 1);
				}
				else if ((getQuestItemsCount(player, NYMPH_STAMEN) >= 600) && (getQuestItemsCount(player, NYMPH_STAMEN) < 900))
				{
					addExpAndSp(player, 6090638400L, 14617495);
					giveItems(player, MIMIUS_REWARD_BOX, 2);
				}
				else if ((getQuestItemsCount(player, NYMPH_STAMEN) >= 900) && (getQuestItemsCount(player, NYMPH_STAMEN) < 1200))
				{
					addExpAndSp(player, 9135957600L, 21926243);
					giveItems(player, MIMIUS_REWARD_BOX, 3);
				}
				else if ((getQuestItemsCount(player, NYMPH_STAMEN) >= 1200) && (getQuestItemsCount(player, NYMPH_STAMEN) < 1500))
				{
					addExpAndSp(player, 12181276800L, 29233986);
					giveItems(player, MIMIUS_REWARD_BOX, 4);
				}
				else if ((getQuestItemsCount(player, NYMPH_STAMEN) >= 1500) && (getQuestItemsCount(player, NYMPH_STAMEN) < 1800))
				{
					addExpAndSp(player, 15226596000L, 36542370);
					giveItems(player, MIMIUS_REWARD_BOX, 5);
				}
				else if (getQuestItemsCount(player, NYMPH_STAMEN) >= 1800)
				{
					addExpAndSp(player, 18271915200L, 43852486);
					giveItems(player, MIMIUS_REWARD_BOX, 6);
				}
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
		switch (qs.getState())
		{
			case State.COMPLETED:
			{
				if (!qs.isNowAvailable())
				{
					htmltext = "30747-11.html";
					break;
				}
				qs.setState(State.CREATED);
			}
			case State.CREATED:
			{
				htmltext = "30747-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "30747-06.html";
				}
				else if (qs.isCond(2) && (getQuestItemsCount(player, NYMPH_STAMEN) < 1800))
				{
					htmltext = "30747-07.html";
				}
				else
				{
					htmltext = "30747-08.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (qs.getCond() > 0) && (getQuestItemsCount(player, NYMPH_STAMEN) < 1800))
		{
			giveItems(player, NYMPH_STAMEN, 1);
			if (getQuestItemsCount(player, NYMPH_STAMEN) == 300)
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