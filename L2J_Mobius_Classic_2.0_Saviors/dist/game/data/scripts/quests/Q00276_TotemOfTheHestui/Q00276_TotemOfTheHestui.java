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
package quests.Q00276_TotemOfTheHestui;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

import quests.Q00261_CollectorsDream.Q00261_CollectorsDream;

/**
 * Totem of the Hestui (276)
 * @author xban1x
 */
public class Q00276_TotemOfTheHestui extends Quest
{
	// Npc
	private static final int TANAPI = 30571;
	// Items
	private static final int KASHA_PARASITE = 1480;
	private static final int KASHA_CRYSTAL = 1481;
	// Monsters
	private static final int KASHA_BEAR = 20479;
	private static final int KASHA_BEAR_TOTEM = 27044;
	// Rewards
	private static final int[] REWARDS = new int[]
	{
		29,
		1500,
	};
	// Misc
	private static final List<ItemHolder> SPAWN_CHANCES = new ArrayList<>();
	private static final int MIN_LEVEL = 15;
	static
	{
		SPAWN_CHANCES.add(new ItemHolder(79, 100));
		SPAWN_CHANCES.add(new ItemHolder(69, 20));
		SPAWN_CHANCES.add(new ItemHolder(59, 15));
		SPAWN_CHANCES.add(new ItemHolder(49, 10));
		SPAWN_CHANCES.add(new ItemHolder(39, 2));
	}
	
	public Q00276_TotemOfTheHestui()
	{
		super(276);
		addStartNpc(TANAPI);
		addTalkId(TANAPI);
		addKillId(KASHA_BEAR, KASHA_BEAR_TOTEM);
		registerQuestItems(KASHA_PARASITE, KASHA_CRYSTAL);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && event.equals("30571-03.htm"))
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
		if ((qs != null) && qs.isCond(1) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, killer, npc, true))
		{
			switch (npc.getId())
			{
				case KASHA_BEAR:
				{
					final long chance1 = getQuestItemsCount(killer, KASHA_PARASITE);
					final int chance2 = getRandom(100);
					boolean chance3 = true;
					for (ItemHolder spawnChance : SPAWN_CHANCES)
					{
						if ((chance1 >= spawnChance.getId()) && (chance2 <= spawnChance.getCount()))
						{
							addSpawn(KASHA_BEAR_TOTEM, npc);
							takeItems(killer, KASHA_PARASITE, -1);
							chance3 = false;
							break;
						}
					}
					if (chance3)
					{
						giveItemRandomly(killer, KASHA_PARASITE, 1, 0, 1, true);
					}
					break;
				}
				case KASHA_BEAR_TOTEM:
				{
					if (giveItemRandomly(killer, KASHA_CRYSTAL, 1, 1, 1, true))
					{
						qs.setCond(2);
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
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
				htmltext = (player.getRace() == Race.ORC) ? (player.getLevel() >= MIN_LEVEL) ? "30571-02.htm" : "30571-01.htm" : "30571-00.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30571-04.html";
						break;
					}
					case 2:
					{
						if (hasQuestItems(player, KASHA_CRYSTAL))
						{
							Q00261_CollectorsDream.giveNewbieReward(player);
							for (int reward : REWARDS)
							{
								rewardItems(player, reward, 1);
							}
							qs.exitQuest(true, true);
							htmltext = "30571-05.html";
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
}
