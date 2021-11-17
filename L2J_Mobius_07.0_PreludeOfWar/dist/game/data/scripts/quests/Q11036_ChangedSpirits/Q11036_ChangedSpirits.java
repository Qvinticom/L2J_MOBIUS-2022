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
package quests.Q11036_ChangedSpirits;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q11025_PathOfDestinyProving.Q11025_PathOfDestinyProving;
import quests.Q11037_WhyAreTheRatelHere.Q11037_WhyAreTheRatelHere;

/**
 * Changed Spirits (11036)
 * @URL https://l2wiki.com/Changed_Spirits
 * @author Dmitri
 */
public class Q11036_ChangedSpirits extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int PIO = 33963;
	private static final int SOBBING_BREEZE = 24391;
	private static final int WHISPERING_BREEZE = 24392;
	private static final int LAUGHING_BREEZE = 24393;
	// Items
	private static final ItemHolder SOE_PIO = new ItemHolder(80681, 1);
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-74631, 94630, -3736);
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final int MIN_LEVEL = 40;
	
	public Q11036_ChangedSpirits()
	{
		super(11036);
		addStartNpc(TARTI);
		addTalkId(TARTI, PIO);
		addKillId(SOBBING_BREEZE, WHISPERING_BREEZE, LAUGHING_BREEZE);
		registerQuestItems(SOE_PIO.getId());
		addCondMinLevel(MIN_LEVEL, "34505-06.html");
		addCondCompletedQuest(Q11025_PathOfDestinyProving.class.getSimpleName(), "34505-06.html");
		setQuestNameNpcStringId(NpcStringId.LV_40_76_CHANGED_SPIRITS);
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
			case "34505-02.html":
			case "34505-04.html":
			case "33963-02.html":
			{
				htmltext = event;
				break;
			}
			case "34505-03.html":
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
			case "33963-03.html":
			{
				if (qs.isCond(2))
				{
					addExpAndSp(player, 14281098, 12852);
					qs.exitQuest(false, true);
					htmltext = event;
					
					// Initialize next quest.
					final Quest nextQuest = QuestManager.getInstance().getQuest(Q11037_WhyAreTheRatelHere.class.getSimpleName());
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
				if (npc.getId() == TARTI)
				{
					htmltext = "34505-01.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case TARTI:
					{
						if (qs.isCond(1))
						{
							htmltext = "34505-03.html";
						}
						break;
					}
					case PIO:
					{
						if (qs.isCond(2))
						{
							htmltext = "33963-01.html";
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
		if ((qs != null) && qs.isCond(1))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 30)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(2, true);
				qs.unset(KILL_COUNT_VAR);
				giveItems(killer, SOE_PIO);
				showOnScreenMsg(killer, NpcStringId.USE_SCROLL_OF_ESCAPE_PIO_IN_YOUR_INVENTORY_NTALK_TO_PIO_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_THE_PACK_OF_WINDRA_2.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
