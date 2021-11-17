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
package quests.Q00935_ExploringTheEastWingOfTheDungeonOfAbyss;

import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * @author QuangNguyen
 */
public class Q00935_ExploringTheEastWingOfTheDungeonOfAbyss extends Quest
{
	// NPCs
	private static final int IRIS = 31776;
	private static final int ROSAMMY = 31777;
	// Monsters
	public int MERTT = 21644;
	public int DUHT = 21645;
	public int PRIZT = 21646;
	public int KOVART = 21647;
	// Items
	public ItemHolder OSKZLA = new ItemHolder(90009, 1);
	public ItemHolder POD = new ItemHolder(90136, 1);
	
	public Q00935_ExploringTheEastWingOfTheDungeonOfAbyss()
	{
		super(935);
		addStartNpc(IRIS, ROSAMMY);
		addTalkId(IRIS, ROSAMMY);
		addKillId(MERTT, DUHT, PRIZT, KOVART);
		registerQuestItems(OSKZLA.getId());
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
			case "31776-01.htm":
			case "31776-02.htm":
			case "31776-03.htm":
			case "31777-01.htm":
			case "31777-02.htm":
			case "31777-03.htm":
			{
				htmltext = event;
				break;
			}
			
			case "31776-04.htm":
			{
				if (player.getLevel() >= 45)
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "31777-04.htm":
			{
				if (player.getLevel() >= 45)
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "end.htm":
			{
				player.addExpAndSp(250000, 7700);
				rewardItems(player, POD);
				qs.exitQuest(QuestType.DAILY, true);
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
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = (talker.getLevel() < 45) ? "nolvl.htm" : "31776-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == IRIS)
				{
					switch (qs.getCond())
					{
						case 0:
						{
							if ((qs.getPlayer().getLevel() >= 45) && (qs.getPlayer().getLevel() <= 49))
							{
								htmltext = "31776-01.htm";
							}
							else
							{
								htmltext = "31776-01a.htm";
							}
							break;
						}
						case 1:
						{
							htmltext = "31776-04.htm";
							break;
						}
						case 2:
						{
							htmltext = "31776-05.htm";
							break;
						}
					}
					break;
				}
				else if (npc.getId() == ROSAMMY)
				{
					switch (qs.getCond())
					{
						case 0:
						{
							if ((qs.getPlayer().getLevel() >= 45) && (qs.getPlayer().getLevel() <= 49))
							{
								htmltext = "31777-01.htm";
								qs.startQuest();
							}
							else
							{
								htmltext = "31777-01a.htm";
							}
							break;
						}
						case 1:
						{
							htmltext = "31777-04.htm";
							break;
						}
						case 2:
						{
							htmltext = "31777-05.htm";
							break;
						}
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (qs.isNowAvailable())
				{
					qs.setState(State.CREATED);
					if ((npc.getId() == IRIS) && (qs.getPlayer().getLevel() < 45))
					{
						htmltext = "31776-01.htm";
					}
					else if ((npc.getId() == ROSAMMY) && (qs.getPlayer().getLevel() < 45))
					{
						htmltext = "31777-01.htm";
					}
					else
					{
						htmltext = "nolvl.htm";
					}
				}
				else
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
		final QuestState qs = getQuestState(killer, false);
		if (qs.getCond() == 1)
		{
			if (getQuestItemsCount(killer, OSKZLA.getId()) < 50)
			{
				giveItems(killer, OSKZLA);
			}
			if (getQuestItemsCount(killer, OSKZLA.getId()) >= 50)
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}