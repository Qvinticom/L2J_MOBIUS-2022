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
package quests.Q00153_DeliverGoods;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Deliver Goods (153)
 * @author Zoey76
 * @version 1.0 Freya (11/16/2010), Based on Naia (EURO)
 */
public class Q00153_DeliverGoods extends Quest
{
	// NPCs
	private static final int JACKSON_ID = 30002;
	private static final int SILVIA_ID = 30003;
	private static final int ARNOLD_ID = 30041;
	private static final int RANT_ID = 30054;
	// Items
	private static final int DELIVERY_LIST_ID = 1012;
	private static final int HEAVY_WOOD_BOX_ID = 1013;
	private static final int CLOTH_BUNDLE_ID = 1014;
	private static final int CLAY_POT_ID = 1015;
	private static final int JACKSONS_RECEIPT_ID = 1016;
	private static final int SILVIAS_RECEIPT_ID = 1017;
	private static final int RANTS_RECEIPT_ID = 1018;
	// Rewards
	private static final int RING_OF_KNOWLEDGE_ID = 49041;
	
	public Q00153_DeliverGoods()
	{
		super(153);
		addStartNpc(ARNOLD_ID);
		addTalkId(JACKSON_ID, SILVIA_ID, ARNOLD_ID, RANT_ID);
		registerQuestItems(DELIVERY_LIST_ID, HEAVY_WOOD_BOX_ID, CLOTH_BUNDLE_ID, CLAY_POT_ID, JACKSONS_RECEIPT_ID, SILVIAS_RECEIPT_ID, RANTS_RECEIPT_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (npc.getId() == ARNOLD_ID) && event.equalsIgnoreCase("30041-02.html"))
		{
			qs.startQuest();
			giveItems(player, DELIVERY_LIST_ID, 1);
			giveItems(player, HEAVY_WOOD_BOX_ID, 1);
			giveItems(player, CLOTH_BUNDLE_ID, 1);
			giveItems(player, CLAY_POT_ID, 1);
		}
		return event;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (npc.getId() == ARNOLD_ID)
		{
			switch (qs.getState())
			{
				case State.CREATED:
				{
					htmltext = (player.getLevel() >= 2) ? "30041-01.htm" : "30041-00.htm";
					break;
				}
				case State.STARTED:
				{
					if (qs.isCond(1))
					{
						htmltext = "30041-03.html";
					}
					else if (qs.isCond(2))
					{
						takeItems(player, DELIVERY_LIST_ID, -1);
						takeItems(player, JACKSONS_RECEIPT_ID, -1);
						takeItems(player, SILVIAS_RECEIPT_ID, -1);
						takeItems(player, RANTS_RECEIPT_ID, -1);
						giveItems(player, RING_OF_KNOWLEDGE_ID, 1);
						qs.exitQuest(false, true);
						htmltext = "30041-04.html";
					}
					break;
				}
				case State.COMPLETED:
				{
					htmltext = getAlreadyCompletedMsg(player);
					break;
				}
			}
		}
		else
		{
			if (npc.getId() == JACKSON_ID)
			{
				if (hasQuestItems(player, HEAVY_WOOD_BOX_ID))
				{
					takeItems(player, HEAVY_WOOD_BOX_ID, -1);
					giveItems(player, JACKSONS_RECEIPT_ID, 1);
					htmltext = "30002-01.html";
				}
				else
				{
					htmltext = "30002-02.html";
				}
			}
			else if (npc.getId() == SILVIA_ID)
			{
				if (hasQuestItems(player, CLOTH_BUNDLE_ID))
				{
					takeItems(player, CLOTH_BUNDLE_ID, -1);
					giveItems(player, SILVIAS_RECEIPT_ID, 1);
					htmltext = "30003-01.html";
				}
				else
				{
					htmltext = "30003-02.html";
				}
			}
			else if (npc.getId() == RANT_ID)
			{
				if (hasQuestItems(player, CLAY_POT_ID))
				{
					takeItems(player, CLAY_POT_ID, -1);
					giveItems(player, RANTS_RECEIPT_ID, 1);
					htmltext = "30054-01.html";
				}
				else
				{
					htmltext = "30054-02.html";
				}
			}
			
			if (qs.isCond(1) && hasQuestItems(player, JACKSONS_RECEIPT_ID) && hasQuestItems(player, SILVIAS_RECEIPT_ID) && hasQuestItems(player, RANTS_RECEIPT_ID))
			{
				qs.setCond(2, true);
			}
		}
		return htmltext;
	}
}
