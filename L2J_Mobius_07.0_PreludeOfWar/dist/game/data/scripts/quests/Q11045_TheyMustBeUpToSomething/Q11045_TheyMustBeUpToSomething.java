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
package quests.Q11045_TheyMustBeUpToSomething;

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

import quests.Q11044_KetraOrcs.Q11044_KetraOrcs;
import quests.Q11046_PrayingForSafety.Q11046_PrayingForSafety;

/**
 * They Must Be Up to Something (11045)
 * @URL https://l2wiki.com/They_Must_Be_Up_to_Something
 * @author Dmitri, Mobius
 */
public class Q11045_TheyMustBeUpToSomething extends Quest
{
	// NPCs
	private static final int RECLOUS = 30648;
	private static final int KETRA_ORC_SCOUT = 24411;
	private static final int KETRA_ORC_SEER = 24412;
	// Items
	private static final int KETRA_ORDER = 80676;
	private static final ItemHolder SOE_RECLOUS = new ItemHolder(80682, 1);
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-94258, 102141, -3472);
	// Misc
	private static final int MIN_LEVEL = 82;
	
	public Q11045_TheyMustBeUpToSomething()
	{
		super(11045);
		addStartNpc(RECLOUS);
		addTalkId(RECLOUS);
		addKillId(KETRA_ORC_SCOUT, KETRA_ORC_SEER);
		registerQuestItems(SOE_RECLOUS.getId(), KETRA_ORDER);
		addCondMinLevel(MIN_LEVEL, "30648-07.html");
		addCondCompletedQuest(Q11044_KetraOrcs.class.getSimpleName(), "30648-07.html");
		setQuestNameNpcStringId(NpcStringId.LV_76_85_THEY_MUST_BE_UP_TO_SOMETHING);
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
			case "30648-03.html":
			case "30648-04.html":
			{
				htmltext = event;
				break;
			}
			case "30648-02.html":
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
			case "30648-06.html":
			{
				if (qs.isCond(2))
				{
					addExpAndSp(player, 514892511, 463403);
					qs.exitQuest(false, true);
					htmltext = event;
					
					// Initialize next quest.
					final Quest nextQuest = QuestManager.getInstance().getQuest(Q11046_PrayingForSafety.class.getSimpleName());
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
				if (npc.getId() == RECLOUS)
				{
					htmltext = "30648-01.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case RECLOUS:
					{
						if (qs.isCond(1))
						{
							htmltext = "30648-02.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "30648-05.html";
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
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, KETRA_ORDER, 1, 15, 0.5, true))
		{
			qs.setCond(2, true);
			giveItems(killer, SOE_RECLOUS);
			showOnScreenMsg(killer, NpcStringId.USE_SCROLL_OF_ESCAPE_RECLOUS_IN_YOUR_INVENTORY_NTALK_TO_RECLOUS_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
		}
		return super.onKill(npc, killer, isSummon);
	}
}
