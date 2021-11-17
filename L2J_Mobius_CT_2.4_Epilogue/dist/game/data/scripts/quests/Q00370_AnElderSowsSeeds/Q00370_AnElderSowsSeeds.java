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
package quests.Q00370_AnElderSowsSeeds;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.util.Util;

/**
 * An Elder Sows Seeds (370)
 * @author Adry_85
 */
public class Q00370_AnElderSowsSeeds extends Quest
{
	// NPC
	private static final int CASIAN = 30612;
	// Items
	private static final int SPELLBOOK_PAGE = 5916;
	private static final int CHAPTER_OF_FIRE = 5917;
	private static final int CHAPTER_OF_WATER = 5918;
	private static final int CHAPTER_OF_WIND = 5919;
	private static final int CHAPTER_OF_EARTH = 5920;
	// Misc
	private static final int MIN_LEVEL = 28;
	// Mobs
	private static final Map<Integer, Integer> MOBS1 = new HashMap<>();
	private static final Map<Integer, Double> MOBS2 = new HashMap<>();
	static
	{
		MOBS1.put(20082, 9); // ant_recruit
		MOBS1.put(20086, 9); // ant_guard
		MOBS1.put(20090, 22); // noble_ant_leader
		MOBS2.put(20084, 0.101); // ant_patrol
		MOBS2.put(20089, 0.100); // noble_ant
	}
	
	public Q00370_AnElderSowsSeeds()
	{
		super(370);
		addStartNpc(CASIAN);
		addTalkId(CASIAN);
		addKillId(MOBS1.keySet());
		addKillId(MOBS2.keySet());
	}
	
	@Override
	public boolean checkPartyMember(Player member, Npc npc)
	{
		final QuestState qs = getQuestState(member, false);
		return ((qs != null) && qs.isStarted());
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
			case "30612-02.htm":
			case "30612-03.htm":
			case "30612-06.html":
			case "30612-07.html":
			case "30612-09.html":
			{
				htmltext = event;
				break;
			}
			case "30612-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "REWARD":
			{
				if (qs.isStarted())
				{
					if (exchangeChapters(player, false))
					{
						htmltext = "30612-08.html";
					}
					else
					{
						htmltext = "30612-11.html";
					}
				}
				break;
			}
			case "30612-10.html":
			{
				if (qs.isStarted())
				{
					exchangeChapters(player, true);
					qs.exitQuest(true, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final int npcId = npc.getId();
		if (MOBS1.containsKey(npcId))
		{
			if (getRandom(100) < MOBS1.get(npcId))
			{
				final Player luckyPlayer = getRandomPartyMember(player, npc);
				if (luckyPlayer != null)
				{
					giveItemRandomly(luckyPlayer, npc, SPELLBOOK_PAGE, 1, 0, 1.0, true);
				}
			}
		}
		else
		{
			final QuestState qs = getRandomPartyMemberState(player, -1, 3, npc);
			if (qs != null)
			{
				giveItemRandomly(qs.getPlayer(), npc, SPELLBOOK_PAGE, 1, 0, MOBS2.get(npcId), true);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			htmltext = (player.getLevel() >= MIN_LEVEL) ? "30612-01.htm" : "30612-05.html";
		}
		else if (qs.isStarted())
		{
			htmltext = "30612-06.html";
		}
		return htmltext;
	}
	
	private final boolean exchangeChapters(Player player, boolean takeAllItems)
	{
		final long waterChapters = getQuestItemsCount(player, CHAPTER_OF_WATER);
		final long earthChapters = getQuestItemsCount(player, CHAPTER_OF_EARTH);
		final long windChapters = getQuestItemsCount(player, CHAPTER_OF_WIND);
		final long fireChapters = getQuestItemsCount(player, CHAPTER_OF_FIRE);
		final long minCount = Util.min(waterChapters, earthChapters, windChapters, fireChapters);
		if (minCount > 0)
		{
			giveAdena(player, minCount * 3600, true);
		}
		final long countToTake = (takeAllItems ? -1 : minCount);
		takeItems(player, (int) countToTake, CHAPTER_OF_WATER, CHAPTER_OF_EARTH, CHAPTER_OF_WIND, CHAPTER_OF_FIRE);
		return (minCount > 0);
	}
}
