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
package quests.Q11034_ResurrectedOne;

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
import org.l2jmobius.gameserver.network.serverpackets.ExTutorialShowId;

import quests.Q11035_DeathlyMischief.Q11035_DeathlyMischief;

/**
 * Resurrected One (11034)
 * @URL https://l2wiki.com/Resurrected_One
 * @author Dmitri
 */
public class Q11034_ResurrectedOne extends Quest
{
	// NPCs
	private static final int KALESIN = 33177;
	private static final int ZENATH = 33509;
	private static final int SKELETON_SCOUT = 24386;
	private static final int SKELETON_ARCHER = 24387;
	private static final int SKELETON_WARRIOR = 24388;
	// Items
	private static final ItemHolder SOE_ZENATH = new ItemHolder(80680, 1);
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-46169, 110937, -3808);
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final int MIN_LEVEL = 27;
	
	public Q11034_ResurrectedOne()
	{
		super(11034);
		addStartNpc(KALESIN);
		addTalkId(KALESIN, ZENATH);
		addKillId(SKELETON_SCOUT, SKELETON_ARCHER, SKELETON_WARRIOR);
		registerQuestItems(SOE_ZENATH.getId());
		addCondMinLevel(MIN_LEVEL, "33177-05.html");
		setQuestNameNpcStringId(NpcStringId.LV_20_40_THE_RESURRECTED_ONE);
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
			case "33177-02.html":
			case "33177-03.html":
			{
				htmltext = event;
				break;
			}
			case "33177-04.html":
			{
				qs.startQuest();
				player.sendPacket(new ExTutorialShowId(19)); // Adventurers Guide
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
			case "33509-02.html":
			{
				if (qs.isCond(2))
				{
					addExpAndSp(player, 1640083, 1476);
					qs.exitQuest(false, true);
					htmltext = event;
					
					// Initialize next quest.
					final Quest nextQuest = QuestManager.getInstance().getQuest(Q11035_DeathlyMischief.class.getSimpleName());
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
				if (npc.getId() == KALESIN)
				{
					htmltext = "33177-01.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case KALESIN:
					{
						if (qs.isCond(1))
						{
							htmltext = "33177-04.html";
						}
						break;
					}
					case ZENATH:
					{
						if (qs.isCond(2))
						{
							htmltext = "33509-01.html";
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
				giveItems(killer, SOE_ZENATH);
				showOnScreenMsg(killer, NpcStringId.USE_SCROLL_OF_ESCAPE_ZENATH_IN_YOUR_INVENTORY_NTALK_TO_ZENATH_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
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
			holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_SKELETONS_3.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
