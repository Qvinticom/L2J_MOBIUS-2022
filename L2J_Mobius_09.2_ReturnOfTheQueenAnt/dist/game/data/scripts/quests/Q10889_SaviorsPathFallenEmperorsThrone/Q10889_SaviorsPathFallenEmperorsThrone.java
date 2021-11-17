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
package quests.Q10889_SaviorsPathFallenEmperorsThrone;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10888_SaviorsPathDefeatTheEmbryo.Q10888_SaviorsPathDefeatTheEmbryo;

/**
 * Savior's Path - Fallen Emperor's Throne
 * @URL https://l2wiki.com/Savior%27s_Path_-_Fallen_Emperor%27s_Throne
 * @author CostyKiller
 */
public class Q10889_SaviorsPathFallenEmperorsThrone extends Quest
{
	// NPC
	private static final int LEONA_BLACKBIRD = 34425;
	// Monsters
	private static final int FE_HELIOS = 26335;
	// Items
	private static final int ORIGIN_OF_GIANTS = 48548;
	// Misc
	private static final int MIN_LEVEL = 103;
	private static final int ORIGIN_OF_GIANTS_NEEDED = 5;
	
	public Q10889_SaviorsPathFallenEmperorsThrone()
	{
		super(10889);
		addStartNpc(LEONA_BLACKBIRD);
		addTalkId(LEONA_BLACKBIRD);
		addKillId(FE_HELIOS);
		addCondMinLevel(MIN_LEVEL, "34425-00.html");
		addCondCompletedQuest(Q10888_SaviorsPathDefeatTheEmbryo.class.getSimpleName(), "34425-00.html");
		registerQuestItems(ORIGIN_OF_GIANTS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		
		switch (event)
		{
			case "34425-02.htm":
			case "34425-03.htm":
			case "34425-06.htm":
			{
				htmltext = event;
				break;
			}
			case "34425-04.html":
			{
				if ((player.getLevel() >= MIN_LEVEL))
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "34425-07.html":
			{
				if (qs.isCond(2))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						addExpAndSp(player, 271916247600L, 271916100);
						giveAdena(player, 30773010, true);
						qs.exitQuest(false, true);
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "34425-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1) && (getQuestItemsCount(player, ORIGIN_OF_GIANTS) < ORIGIN_OF_GIANTS_NEEDED))
				{
					htmltext = "34425-05.html";
				}
				else if (qs.isCond(2) && (getQuestItemsCount(player, ORIGIN_OF_GIANTS) >= ORIGIN_OF_GIANTS_NEEDED))
				{
					htmltext = "34425-06.htm";
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
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, true);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			if (getQuestItemsCount(player, ORIGIN_OF_GIANTS) < ORIGIN_OF_GIANTS_NEEDED)
			{
				giveItems(player, ORIGIN_OF_GIANTS, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if (getQuestItemsCount(player, ORIGIN_OF_GIANTS) >= ORIGIN_OF_GIANTS_NEEDED)
			{
				qs.setCond(2, true);
			}
		}
	}
}