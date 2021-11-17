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
package quests.Q00161_FruitOfTheMotherTree;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Fruit of the Mother Tree (161)
 * @author malyelfik
 */
public class Q00161_FruitOfTheMotherTree extends Quest
{
	// NPCs
	private static final int ANDELLIA = 30362;
	private static final int THALIA = 30371;
	// Items
	private static final int ANDELLRIAS_LETTER = 1036;
	private static final int MOTHERTREE_FRUIT = 1037;
	// Misc
	private static final int MIN_LEVEL = 3;
	
	public Q00161_FruitOfTheMotherTree()
	{
		super(161);
		addStartNpc(ANDELLIA);
		addTalkId(ANDELLIA, THALIA);
		registerQuestItems(ANDELLRIAS_LETTER, MOTHERTREE_FRUIT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "30362-04.htm":
			{
				qs.startQuest();
				giveItems(player, ANDELLRIAS_LETTER, 1);
				break;
			}
			case "30371-03.html":
			{
				break;
			}
			default:
			{
				htmltext = null;
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
		switch (npc.getId())
		{
			case ANDELLIA:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = (player.getRace() == Race.ELF) ? (player.getLevel() >= MIN_LEVEL) ? "30362-03.htm" : "30362-02.htm" : "30362-01.htm";
						break;
					}
					case State.STARTED:
					{
						if (qs.isCond(1))
						{
							htmltext = "30362-05.html";
						}
						else if (qs.isCond(2) && hasQuestItems(player, MOTHERTREE_FRUIT))
						{
							giveAdena(player, 100, true);
							qs.exitQuest(false, true);
							htmltext = "30362-06.html";
						}
						break;
					}
					case State.COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg(player);
						break;
					}
				}
				break;
			}
			case THALIA:
			{
				if (qs.isStarted())
				{
					if (qs.isCond(1) && hasQuestItems(player, ANDELLRIAS_LETTER))
					{
						takeItems(player, ANDELLRIAS_LETTER, -1);
						giveItems(player, MOTHERTREE_FRUIT, 1);
						qs.setCond(2, true);
						htmltext = "30371-01.html";
					}
					else if (qs.isCond(2) && hasQuestItems(player, MOTHERTREE_FRUIT))
					{
						htmltext = "30371-02.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
}