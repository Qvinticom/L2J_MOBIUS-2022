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
package quests.Q10356_BlacksmithsSoul2;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

import quests.Q10355_BlacksmithsSoul1.Q10355_BlacksmithsSoul1;

/**
 * @author Sero
 */
public class Q10356_BlacksmithsSoul2 extends Quest
{
	// NPCs
	private static final int BLACKSMITH_MAMMON = 31126;
	private static final int SHADAI = 32347;
	private static final int ISHUMA = 32615;
	// Items
	private static final int Improved_SHADOW_Ingot = 47896;
	private static final int RESEARCH_SHADOW_WEAPON = 47890;
	private static final int SHADOW_WEAPON_1 = 46317;
	private static final int SHADOW_WEAPON_2 = 46318;
	private static final int SHADOW_WEAPON_3 = 46319;
	private static final int SHADOW_WEAPON_4 = 46320;
	private static final int SHADOW_WEAPON_5 = 46321;
	private static final int SHADOW_WEAPON_6 = 46322;
	private static final int SHADOW_WEAPON_7 = 46323;
	private static final int SHADOW_WEAPON_8 = 46324;
	private static final int SHADOW_WEAPON_9 = 46325;
	private static final int SHADOW_WEAPON_10 = 46326;
	private static final int SHADOW_CRYSTAL = 47899;
	private static final int MAIN_LEVEL = 99;
	
	public Q10356_BlacksmithsSoul2()
	{
		super(10356);
		addStartNpc(BLACKSMITH_MAMMON);
		addTalkId(BLACKSMITH_MAMMON, SHADAI, ISHUMA);
		addCondCompletedQuest(Q10355_BlacksmithsSoul1.class.getSimpleName(), "31126-02.htm");
		addCondMinLevel(MAIN_LEVEL, getNoQuestMsg(null));
		registerQuestItems(Improved_SHADOW_Ingot, SHADOW_WEAPON_1, SHADOW_WEAPON_2, SHADOW_WEAPON_3, SHADOW_WEAPON_4, SHADOW_WEAPON_5, SHADOW_WEAPON_6, SHADOW_WEAPON_7, SHADOW_WEAPON_8, SHADOW_WEAPON_9, SHADOW_WEAPON_10, RESEARCH_SHADOW_WEAPON);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		switch (event)
		{
			case "31126-01.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "31126-04.htm":
			{
				if (hasQuestItems(player, Improved_SHADOW_Ingot))
				{
					takeItems(player, Improved_SHADOW_Ingot, 1);
					qs.setCond(2);
					htmltext = event;
				}
				else
				{
					htmltext = "31126-03r.htm";
				}
				break;
			}
			case "SHADOW_WEAPON_1":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_1))
				{
					takeItems(player, SHADOW_WEAPON_1, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "SHADOW_WEAPON_2":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_2))
				{
					takeItems(player, SHADOW_WEAPON_2, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "SHADOW_WEAPON_3":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_3))
				{
					takeItems(player, SHADOW_WEAPON_3, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "SHADOW_WEAPON_4":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_4))
				{
					takeItems(player, SHADOW_WEAPON_4, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "SHADOW_WEAPON_5":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_5))
				{
					takeItems(player, SHADOW_WEAPON_5, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "SHADOW_WEAPON_6":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_6))
				{
					takeItems(player, SHADOW_WEAPON_6, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
					htmltext = event;
				}
				break;
			}
			case "SHADOW_WEAPON_7":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_7))
				{
					takeItems(player, SHADOW_WEAPON_7, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "SHADOW_WEAPON_8":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_8))
				{
					takeItems(player, SHADOW_WEAPON_8, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "SHADOW_WEAPON_9":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_9))
				{
					takeItems(player, SHADOW_WEAPON_9, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "SHADOW_WEAPON_10":
			{
				if (hasQuestItems(player, SHADOW_WEAPON_10))
				{
					takeItems(player, SHADOW_WEAPON_10, 1);
					giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
					htmltext = "31126-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "RESEARCH_SHADOW_WEAPON7":
			{
				if (hasQuestItems(player, RESEARCH_SHADOW_WEAPON) && (getEnchantLevel(player, RESEARCH_SHADOW_WEAPON) >= 7))
				{
					takeItems(player, RESEARCH_SHADOW_WEAPON, -1);
					giveItems(player, SHADOW_CRYSTAL, 2);
					qs.setCond(5);
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case BLACKSMITH_MAMMON:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = "31126-00.htm";
						break;
					}
					case State.STARTED:
					{
						if (qs.isCond(1))
						{
							htmltext = "31126-03.htm";
						}
						else if (qs.isCond(2))
						{
							htmltext = "31126-05.htm";
						}
						else if (qs.isCond(3))
						{
							htmltext = "31126-08.htm";
							qs.setCond(4);
						}
						else if (qs.isCond(4))
						{
							if (hasQuestItems(player, RESEARCH_SHADOW_WEAPON))
							{
								htmltext = "31126-07.htm";
							}
							else
							{
								giveItems(player, RESEARCH_SHADOW_WEAPON, 1);
								htmltext = "31126-06.htm";
							}
						}
						else if (qs.isCond(6))
						{
							htmltext = "31126-09.htm";
							addExpAndSp(player, 32958000000L, 29662200);
							qs.exitQuest(false, true);
						}
						break;
					}
					case State.COMPLETED:
					{
						if (!qs.isNowAvailable())
						{
							htmltext = "31126-00a.htm";
							break;
						}
					}
				}
				break;
			}
			case SHADAI:
			{
				switch (qs.getState())
				{
					case State.STARTED:
					{
						if (qs.isCond(5) && hasQuestItems(player, SHADOW_CRYSTAL))
						{
							htmltext = "32347-00.htm";
							takeItems(player, SHADOW_CRYSTAL, -1);
							qs.setCond(6);
						}
						break;
					}
					case State.COMPLETED:
					{
						if (!qs.isNowAvailable())
						{
							htmltext = "31126-00a.htm";
							break;
						}
					}
				}
				break;
			}
			case ISHUMA:
			{
				switch (qs.getState())
				{
					case State.STARTED:
					{
						if (qs.isCond(5) && hasQuestItems(player, SHADOW_CRYSTAL))
						{
							htmltext = "32615-00.htm";
							takeItems(player, SHADOW_CRYSTAL, -1);
							qs.setCond(6);
						}
						break;
					}
					case State.COMPLETED:
					{
						if (!qs.isNowAvailable())
						{
							htmltext = "31126-00a.htm";
							break;
						}
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2))
		{
			if (hasAtLeastOneQuestItem(player, SHADOW_WEAPON_1, SHADOW_WEAPON_2, SHADOW_WEAPON_3, SHADOW_WEAPON_4, SHADOW_WEAPON_5, SHADOW_WEAPON_6, SHADOW_WEAPON_7, SHADOW_WEAPON_8, SHADOW_WEAPON_9, SHADOW_WEAPON_10))
			{
				final Set<NpcLogListHolder> holder = new HashSet<>();
				holder.add(new NpcLogListHolder(NpcStringId.BRING_SHADOW_WEAPON, 1));
				return holder;
			}
			
		}
		if ((qs != null) && qs.isCond(4))
		{
			if ((getEnchantLevel(player, RESEARCH_SHADOW_WEAPON) >= 7))
			{
				final Set<NpcLogListHolder> holder = new HashSet<>();
				holder.add(new NpcLogListHolder(NpcStringId.ENCHANT_THE_RESEARCH_WEAPON, 1));
				return holder;
			}
			
		}
		return super.getNpcLogList(player);
	}
}
