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
package quests.Q11038_GrowlersTurnedViolent;

import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q11037_WhyAreTheRatelHere.Q11037_WhyAreTheRatelHere;
import quests.Q11039_CommunicationBreakdown.Q11039_CommunicationBreakdown;

/**
 * Growlers Turned Violent (11038)
 * @URL https://l2wiki.com/Growlers_Turned_Violent
 * @author Dmitri
 */
public class Q11038_GrowlersTurnedViolent extends Quest
{
	// NPCs
	private static final int PIO = 33963;
	private static final int AWESOME_SHRIKER = 24396;
	private static final int AWESOME_DEVELOPED_SHRIKER = 24397;
	private static final int AWESOME_STRONG_SHRIKER = 24398;
	// Items
	private static final int CORRUPTED_ENERGY = 80673;
	private static final ItemHolder SOE_PIO = new ItemHolder(80681, 1);
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-84963, 80967, -3144);
	
	public Q11038_GrowlersTurnedViolent()
	{
		super(11038);
		addStartNpc(PIO);
		addTalkId(PIO);
		addKillId(AWESOME_SHRIKER, AWESOME_DEVELOPED_SHRIKER, AWESOME_STRONG_SHRIKER);
		registerQuestItems(SOE_PIO.getId(), CORRUPTED_ENERGY);
		addCondCompletedQuest(Q11037_WhyAreTheRatelHere.class.getSimpleName(), "33963-06.html");
		setQuestNameNpcStringId(NpcStringId.LV_40_76_GROWLERS_TURNED_VIOLENT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "33963-03.html":
			{
				htmltext = event;
				break;
			}
			case "33963-02.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT);
				}
				break;
			}
			case "33963-05.html":
			{
				if (qs.isCond(2))
				{
					addExpAndSp(player, 76142825, 68528);
					qs.exitQuest(false, true);
					htmltext = event;
					
					// Initialize next quest.
					final Quest nextQuest = QuestManager.getInstance().getQuest(Q11039_CommunicationBreakdown.class.getSimpleName());
					if (nextQuest != null)
					{
						nextQuest.newQuestState(player);
					}
				}
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
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == PIO)
				{
					htmltext = "33963-01.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case PIO:
					{
						if (qs.isCond(1))
						{
							htmltext = "33963-02.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "33963-04.html";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, CORRUPTED_ENERGY, 1, 15, 0.5, true))
		{
			qs.setCond(2, true);
			giveItems(killer, SOE_PIO);
			showOnScreenMsg(killer, NpcStringId.USE_SCROLL_OF_ESCAPE_PIO_IN_YOUR_INVENTORY_NTALK_TO_PIO_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
		}
		return super.onKill(npc, killer, isSummon);
	}
}
