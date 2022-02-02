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
package events.L2Day;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemChanceHolder;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;

/**
 * L2 Day event AI.
 * @author Pandragon
 */
public class L2Day extends LongTimeEvent
{
	// NPCs
	private static final Map<Integer, Integer> MANAGERS = new HashMap<>();
	static
	{
		MANAGERS.put(31854, 7117); // Talking Island Village
		MANAGERS.put(31855, 7118); // Elven Village
		MANAGERS.put(31856, 7119); // Dark Elven Village
		MANAGERS.put(31857, 7121); // Dwarven Village
		MANAGERS.put(31858, 7120); // Orc Village
	}
	// Items
	private static final int A = 3875;
	private static final int C = 3876;
	private static final int E = 3877;
	private static final int F = 3878;
	private static final int G = 3879;
	private static final int H = 3880;
	private static final int I = 3881;
	private static final int L = 3882;
	private static final int N = 3883;
	private static final int O = 3884;
	private static final int R = 3885;
	private static final int S = 3886;
	private static final int T = 3887;
	private static final int II = 3888;
	// Rewards
	private static final ItemChanceHolder[] L2_REWARDS =
	{
		new ItemChanceHolder(3959, 10, 2), // Blessed Scroll of Resurrection (Event)
		new ItemChanceHolder(3958, 7, 2), // Blessed Scroll of Escape (Event)
		new ItemChanceHolder(6660, 0, 1), // Ring of Queen Ant
	};
	private static final ItemChanceHolder[] NC_REWARDS =
	{
		new ItemChanceHolder(3959, 10, 1), // Blessed Scroll of Resurrection (Event)
		new ItemChanceHolder(3958, 7, 1), // Blessed Scroll of Escape (Event)
		new ItemChanceHolder(6661, 0, 1), // Earring of Orfen
	};
	private static final ItemChanceHolder[] CH_REWARDS =
	{
		new ItemChanceHolder(3959, 10, 1), // Blessed Scroll of Resurrection (Event)
		new ItemChanceHolder(3958, 7, 1), // Blessed Scroll of Escape (Event)
		new ItemChanceHolder(6662, 0, 1), // Ring of Core
	};
	
	private L2Day()
	{
		for (int id : MANAGERS.keySet())
		{
			addStartNpc(id);
			addFirstTalkId(id);
			addTalkId(id);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		switch (event)
		{
			case "collect_l2":
			{
				if (hasQuestItems(player, L, I, N, E, A, G, II) && (getQuestItemsCount(player, E) > 1))
				{
					takeItems(player, 1, L, I, N, E, A, G, E, II);
					final int random = getRandom(100);
					if (random >= 95)
					{
						giveItems(player, MANAGERS.get(npc.getNpcId()), 2);
					}
					else
					{
						for (ItemChanceHolder holder : L2_REWARDS)
						{
							if (random >= holder.getChance())
							{
								giveItems(player, holder);
								break;
							}
						}
					}
					htmltext = "manager-1.htm";
				}
				else
				{
					htmltext = "manager-no.htm";
				}
				break;
			}
			case "collect_nc":
			{
				if (hasQuestItems(player, N, C, S, O, F, T))
				{
					takeItems(player, 1, N, C, S, O, F, T);
					final int random = getRandom(100);
					if (random >= 95)
					{
						giveItems(player, MANAGERS.get(npc.getNpcId()), 1);
					}
					else
					{
						for (ItemChanceHolder holder : NC_REWARDS)
						{
							if (random >= holder.getChance())
							{
								giveItems(player, holder);
								break;
							}
						}
					}
					htmltext = "manager-1.htm";
				}
				else
				{
					htmltext = "manager-no.htm";
				}
				break;
			}
			case "collect_ch":
			{
				if (hasQuestItems(player, C, H, R, O, N, I, L, E) && (getQuestItemsCount(player, C) > 1))
				{
					takeItems(player, 1, C, H, R, O, N, I, C, L, E);
					final int random = getRandom(100);
					if (random >= 95)
					{
						giveItems(player, MANAGERS.get(npc.getNpcId()), 1);
					}
					else
					{
						for (ItemChanceHolder holder : CH_REWARDS)
						{
							if (random >= holder.getChance())
							{
								giveItems(player, holder);
								break;
							}
						}
					}
					htmltext = "manager-1.htm";
				}
				else
				{
					htmltext = "manager-no.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "manager-1.htm";
	}
	
	public static void main(String[] args)
	{
		new L2Day();
	}
}
