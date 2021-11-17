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
package quests.Q00039_RedEyedInvaders;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;

/**
 * Red-eyed Invaders (39)
 * @author janiko
 */
public class Q00039_RedEyedInvaders extends Quest
{
	// NPCs
	private static final int CAPTAIN_BATHIS = 30332;
	private static final int GUARD_BABENCO = 30334;
	// Monsters
	private static final int MALE_LIZARDMAN = 20919;
	private static final int MALE_LIZARDMAN_SCOUT = 20920;
	private static final int MALE_LIZARDMAN_GUARD = 20921;
	private static final int GIANT_ARANE = 20925;
	// Items
	private static final ItemHolder LIZ_NECKLACE_A = new ItemHolder(7178, 100);
	private static final ItemHolder LIZ_NECKLACE_B = new ItemHolder(7179, 100);
	private static final ItemHolder LIZ_PERFUME = new ItemHolder(7180, 30);
	private static final ItemHolder LIZ_GEM = new ItemHolder(7181, 30);
	// Rewards
	private static final ItemHolder CORRODED_GREEN_BAIT = new ItemHolder(6521, 60);
	private static final ItemHolder CORRODED_BABYDUCK_ROD = new ItemHolder(6529, 1);
	private static final ItemHolder CORRODED_FISHING_SHOT = new ItemHolder(6535, 500);
	// Misc
	private static final int MIN_LEVEL = 20;
	
	public Q00039_RedEyedInvaders()
	{
		super(39);
		addStartNpc(GUARD_BABENCO);
		addTalkId(GUARD_BABENCO, CAPTAIN_BATHIS);
		addKillId(MALE_LIZARDMAN_GUARD, MALE_LIZARDMAN_SCOUT, MALE_LIZARDMAN, GIANT_ARANE);
		registerQuestItems(LIZ_NECKLACE_A.getId(), LIZ_NECKLACE_B.getId(), LIZ_PERFUME.getId(), LIZ_GEM.getId());
		addCondMinLevel(MIN_LEVEL, "30334-02.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30334-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30332-02.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "30332-05.html":
			{
				if (qs.isCond(3))
				{
					if (hasAllItems(player, true, LIZ_NECKLACE_A, LIZ_NECKLACE_B))
					{
						qs.setCond(4, true);
						takeAllItems(player, LIZ_NECKLACE_A, LIZ_NECKLACE_B);
						htmltext = event;
					}
					else
					{
						htmltext = "30332-06.html";
					}
				}
				break;
			}
			case "30332-09.html":
			{
				if (qs.isCond(5))
				{
					if (hasAllItems(player, true, LIZ_PERFUME, LIZ_GEM))
					{
						rewardItems(player, CORRODED_GREEN_BAIT);
						rewardItems(player, CORRODED_BABYDUCK_ROD);
						rewardItems(player, CORRODED_FISHING_SHOT);
						addExpAndSp(player, 62366, 14);
						qs.exitQuest(false, true);
						htmltext = event;
					}
					else
					{
						htmltext = "30332-10.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final QuestState qs = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		switch (npc.getId())
		{
			case CAPTAIN_BATHIS:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30332-01.html";
						break;
					}
					case 2:
					{
						htmltext = "30332-03.html";
						break;
					}
					case 3:
					{
						htmltext = "30332-04.html";
						break;
					}
					case 4:
					{
						htmltext = "30332-07.html";
						break;
					}
					case 5:
					{
						htmltext = "30332-08.html";
						break;
					}
				}
				break;
			}
			case GUARD_BABENCO:
			{
				if (qs.isCreated())
				{
					htmltext = "30334-01.htm";
				}
				else if (qs.isStarted() && qs.isCond(1))
				{
					htmltext = "30334-04.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(talker);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		switch (npc.getId())
		{
			case MALE_LIZARDMAN:
			{
				final QuestState qs = getRandomPartyMemberState(killer, 2, 3, npc);
				if ((qs != null) && giveItemRandomly(qs.getPlayer(), npc, LIZ_NECKLACE_A.getId(), 1, LIZ_NECKLACE_A.getCount(), 0.5, true))
				{
					if (hasItem(qs.getPlayer(), LIZ_NECKLACE_B))
					{
						qs.setCond(3);
					}
				}
				break;
			}
			case MALE_LIZARDMAN_SCOUT:
			{
				if (getRandomBoolean())
				{
					final QuestState qs = getRandomPartyMemberState(killer, 2, 3, npc);
					if ((qs != null) && giveItemRandomly(qs.getPlayer(), npc, LIZ_NECKLACE_A.getId(), 1, LIZ_NECKLACE_A.getCount(), 0.5, true))
					{
						if (hasItem(qs.getPlayer(), LIZ_NECKLACE_B))
						{
							qs.setCond(3);
						}
					}
				}
				else
				{
					final QuestState qs = getRandomPartyMemberState(killer, 4, 3, npc);
					if ((qs != null) && giveItemRandomly(qs.getPlayer(), npc, LIZ_PERFUME.getId(), 1, LIZ_PERFUME.getCount(), 0.25, true))
					{
						if (hasItem(qs.getPlayer(), LIZ_GEM))
						{
							qs.setCond(5);
						}
					}
				}
				break;
			}
			case MALE_LIZARDMAN_GUARD:
			{
				if (getRandomBoolean())
				{
					final QuestState qs = getRandomPartyMemberState(killer, 2, 3, npc);
					if ((qs != null) && giveItemRandomly(qs.getPlayer(), npc, LIZ_NECKLACE_B.getId(), 1, LIZ_NECKLACE_B.getCount(), 0.5, true))
					{
						if (hasItem(qs.getPlayer(), LIZ_NECKLACE_A))
						{
							qs.setCond(3);
						}
					}
				}
				else
				{
					final QuestState qs = getRandomPartyMemberState(killer, 4, 3, npc);
					if ((qs != null) && giveItemRandomly(qs.getPlayer(), npc, LIZ_PERFUME.getId(), 1, LIZ_PERFUME.getCount(), 0.3, true))
					{
						if (hasItem(qs.getPlayer(), LIZ_GEM))
						{
							qs.setCond(5);
						}
					}
				}
				break;
			}
			case GIANT_ARANE:
			{
				final QuestState qs = getRandomPartyMemberState(killer, 4, 3, npc);
				if ((qs != null) && giveItemRandomly(qs.getPlayer(), npc, LIZ_GEM.getId(), 1, LIZ_GEM.getCount(), 0.3, true))
				{
					if (hasItem(qs.getPlayer(), LIZ_PERFUME))
					{
						qs.setCond(5);
					}
				}
				break;
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}