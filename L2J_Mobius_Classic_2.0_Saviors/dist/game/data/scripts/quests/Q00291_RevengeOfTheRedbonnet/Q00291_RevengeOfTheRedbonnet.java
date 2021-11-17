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
package quests.Q00291_RevengeOfTheRedbonnet;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.util.Util;

/**
 * Revenge of the Redbonnet (291).
 * @author xban1x
 */
public class Q00291_RevengeOfTheRedbonnet extends Quest
{
	// NPC
	private static final int MARYSE_REDBONNET = 30553;
	// Item
	private static final ItemHolder BLACK_WOLF_PELT = new ItemHolder(1482, 40);
	// Monster
	private static final int BLACK_WOLF = 20317;
	private static final int BLACK_TIMBER_WOLF = 20318;
	// Rewards
	private static final int ADENA = 57;
	private static final int GRANDMAS_PEARL = 1502;
	private static final int GRANDMAS_MIRROR = 1503;
	private static final int GRANDMAS_NECKLACE = 1504;
	private static final int GRANDMAS_HAIRPIN = 1505;
	// Misc
	private static final int MIN_LEVEL = 4;
	
	public Q00291_RevengeOfTheRedbonnet()
	{
		super(291);
		addStartNpc(MARYSE_REDBONNET);
		addTalkId(MARYSE_REDBONNET);
		addKillId(BLACK_WOLF, BLACK_TIMBER_WOLF);
		registerQuestItems(BLACK_WOLF_PELT.getId());
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && event.equals("30553-03.htm"))
		{
			qs.startQuest();
			return event;
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, killer, true) && giveItemRandomly(qs.getPlayer(), npc, BLACK_WOLF_PELT.getId(), 1, BLACK_WOLF_PELT.getCount(), 1.0, true))
		{
			qs.setCond(2);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String html = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			html = ((player.getLevel() >= MIN_LEVEL) ? "30553-02.htm" : "30553-01.htm");
		}
		else if (qs.isStarted())
		{
			if (qs.isCond(2) && hasItem(player, BLACK_WOLF_PELT))
			{
				takeItem(player, BLACK_WOLF_PELT);
				final int chance = getRandom(100);
				if (chance <= 2)
				{
					giveItems(player, GRANDMAS_PEARL, 1);
				}
				else if (chance <= 20)
				{
					giveItems(player, GRANDMAS_MIRROR, 1);
				}
				else if (chance <= 30)
				{
					giveItems(player, GRANDMAS_HAIRPIN, 1);
				}
				else if (chance <= 45)
				{
					giveItems(player, GRANDMAS_NECKLACE, 1);
				}
				else
				{
					giveItems(player, ADENA, 20);
				}
				qs.exitQuest(true, true);
				html = "30553-05.html";
			}
			else
			{
				html = "30553-04.html";
			}
		}
		return html;
	}
}
