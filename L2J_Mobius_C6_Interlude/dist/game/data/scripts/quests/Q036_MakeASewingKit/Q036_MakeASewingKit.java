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
package quests.Q036_MakeASewingKit;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q037_MakeFormalWear.Q037_MakeFormalWear;

public class Q036_MakeASewingKit extends Quest
{
	// Items
	private static final int REINFORCED_STEEL = 7163;
	private static final int ARTISANS_FRAME = 1891;
	private static final int ORIHARUKON = 1893;
	// Reward
	private static final int SEWING_KIT = 7078;
	
	public Q036_MakeASewingKit()
	{
		super(36, "Make a Sewing Kit");
		registerQuestItems(REINFORCED_STEEL);
		addStartNpc(30847); // Ferris
		addTalkId(30847);
		addKillId(20566); // Iron Golem
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30847-1.htm":
			{
				st.startQuest();
				break;
			}
			case "30847-3.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(REINFORCED_STEEL, 5);
				break;
			}
			case "30847-5.htm":
			{
				if ((st.getQuestItemsCount(ORIHARUKON) >= 10) && (st.getQuestItemsCount(ARTISANS_FRAME) >= 10))
				{
					st.takeItems(ARTISANS_FRAME, 10);
					st.takeItems(ORIHARUKON, 10);
					st.giveItems(SEWING_KIT, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
				{
					htmltext = "30847-4a.htm";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() >= 60)
				{
					final QuestState fwear = player.getQuestState(Q037_MakeFormalWear.class.getSimpleName());
					if ((fwear != null) && fwear.isCond(6))
					{
						htmltext = "30847-0.htm";
					}
					else
					{
						htmltext = "30847-0a.htm";
					}
				}
				else
				{
					htmltext = "30847-0b.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				if (cond == 1)
				{
					htmltext = "30847-1a.htm";
				}
				else if (cond == 2)
				{
					htmltext = "30847-2.htm";
				}
				else if (cond == 3)
				{
					htmltext = ((st.getQuestItemsCount(ORIHARUKON) < 10) || (st.getQuestItemsCount(ARTISANS_FRAME) < 10)) ? "30847-4a.htm" : "30847-4.htm";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, 1);
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(REINFORCED_STEEL, 1, 5, 500000))
		{
			st.setCond(2);
		}
		
		return null;
	}
}