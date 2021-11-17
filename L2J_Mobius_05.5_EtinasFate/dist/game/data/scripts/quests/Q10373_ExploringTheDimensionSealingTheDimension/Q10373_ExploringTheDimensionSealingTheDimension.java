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
package quests.Q10373_ExploringTheDimensionSealingTheDimension;

import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * @author Sero
 */
public class Q10373_ExploringTheDimensionSealingTheDimension extends Quest
{
	private static final int BELOA = 34227;
	private static final int RUNE_STONE = 39738;
	private static final int COMMANDO_BELT = 47044;
	private static final int REMNANT_OF_THE_RIFT = 46787;
	private static final int ZODIAC_AGATHION = 45577;
	private static final int MIN_LEVEL = 95;
	
	public Q10373_ExploringTheDimensionSealingTheDimension()
	{
		super(10373);
		addStartNpc(BELOA);
		addTalkId(BELOA);
		addCondMinLevel(MIN_LEVEL, getNoQuestMsg(null));
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
		if (event.equals("34227-04.htm"))
		{
			qs.startQuest();
			htmltext = event;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (npc.getId() == BELOA)
		{
			switch (qs.getState())
			{
				case State.CREATED:
				{
					htmltext = "34227-05.htm";
					break;
				}
				case State.STARTED:
				{
					if (getQuestItemsCount(player, REMNANT_OF_THE_RIFT) >= 30)
					{
						takeItems(player, REMNANT_OF_THE_RIFT, -1);
						giveItems(player, COMMANDO_BELT, 1);
						giveItems(player, ZODIAC_AGATHION, 1);
						giveItems(player, RUNE_STONE, 1);
						addExpAndSp(player, 12113489880L, 12113460);
						qs.exitQuest(QuestType.ONE_TIME);
						htmltext = "30756-09.html";
					}
					else
					{
						htmltext = "34227-05a.htm";
					}
					break;
				}
				case State.COMPLETED:
				{
					if (!qs.isNowAvailable())
					{
						htmltext = "34227-00a.htm";
						break;
					}
				}
			}
		}
		return htmltext;
	}
}
