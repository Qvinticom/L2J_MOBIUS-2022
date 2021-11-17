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
package quests.Q00366_SilverHairedShaman;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;

/**
 * Silver Haired Shaman (366)
 * @author Adry_85, jurchiks
 */
public class Q00366_SilverHairedShaman extends Quest
{
	// NPC
	private static final int DIETER = 30111;
	// Item
	private static final int SAIRONS_SILVER_HAIR = 5874;
	// Misc
	private static final int MIN_LEVEL = 48;
	// Mobs
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	static
	{
		MOBS.put(20986, 80); // saitnn
		MOBS.put(20987, 73); // saitnn_doll
		MOBS.put(20988, 80); // saitnn_puppet
	}
	
	public Q00366_SilverHairedShaman()
	{
		super(366);
		addStartNpc(DIETER);
		addTalkId(DIETER);
		addKillId(MOBS.keySet());
		registerQuestItems(SAIRONS_SILVER_HAIR);
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
			case "30111-02.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30111-05.html":
			{
				qs.exitQuest(true, true);
				htmltext = event;
				break;
			}
			case "30111-06.html":
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		if (getRandom(100) < MOBS.get(npc.getId()))
		{
			final Player luckyPlayer = getRandomPartyMember(player, npc);
			if (luckyPlayer != null)
			{
				giveItemRandomly(luckyPlayer, npc, SAIRONS_SILVER_HAIR, 1, 0, 1.0, true);
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
			htmltext = (player.getLevel() >= MIN_LEVEL) ? "30111-01.htm" : "30111-03.html";
		}
		else if (qs.isStarted())
		{
			if (hasQuestItems(player, SAIRONS_SILVER_HAIR))
			{
				final long itemCount = getQuestItemsCount(player, SAIRONS_SILVER_HAIR);
				giveAdena(player, (itemCount * 500) + 29000, true);
				takeItems(player, SAIRONS_SILVER_HAIR, -1);
				htmltext = "30111-04.html";
			}
			else
			{
				htmltext = "30111-07.html";
			}
		}
		return htmltext;
	}
}
