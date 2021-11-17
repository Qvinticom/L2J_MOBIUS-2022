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
package quests.Q10362_CertificationOfTheSeeker;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q10361_RolesOfTheSeeker.Q10361_RolesOfTheSeeker;

/**
 * Certification of The Seeker (10362)
 * @author Gladicek
 */
public class Q10362_CertificationOfTheSeeker extends Quest
{
	// NPCs
	private static final int CHESHA = 33449;
	private static final int NAGEL = 33450;
	private static final int STALKER = 22992;
	private static final int CRAWLER = 22991;
	// Items
	private static final int GLOVES = 49;
	private static final int HEALING_POTION = 1060;
	// Misc
	private static final int MIN_LEVEL = 10;
	private static final int MAX_LEVEL = 20;
	
	public Q10362_CertificationOfTheSeeker()
	{
		super(10362);
		addStartNpc(CHESHA);
		addTalkId(CHESHA, NAGEL);
		addKillId(STALKER, CRAWLER);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33449-07.html");
		addCondCompletedQuest(Q10361_RolesOfTheSeeker.class.getSimpleName(), "33449-07.html");
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
			case "33449-02.htm":
			case "33450-02.html":
			{
				htmltext = event;
				break;
			}
			case "33449-03.html":
			{
				qs.startQuest();
				qs.setMemoStateEx(STALKER, 0);
				qs.setMemoStateEx(CRAWLER, 0);
				htmltext = event;
				break;
			}
			case "33450-03.html":
			{
				if (qs.isCond(3))
				{
					giveItems(player, GLOVES, 1);
					giveAdena(player, 430, true);
					giveItems(player, HEALING_POTION, 50);
					addExpAndSp(player, 50000, 12);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
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
		if ((qs != null) && qs.isCond(1))
		{
			int killedStalker = qs.getMemoStateEx(STALKER);
			int killedCrawler = qs.getMemoStateEx(CRAWLER);
			if (npc.getId() == STALKER)
			{
				killedStalker++;
				if (killedStalker <= 10)
				{
					qs.setMemoStateEx(STALKER, killedStalker);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (npc.getId() == CRAWLER)
			{
				killedCrawler++;
				if (killedCrawler <= 5)
				{
					qs.setMemoStateEx(CRAWLER, killedCrawler);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			
			if ((killedStalker == 10) && (killedCrawler == 5))
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
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
				if (npc.getId() == CHESHA)
				{
					htmltext = "33449-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == CHESHA)
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "33449-04.html";
							break;
						}
						case 2:
						{
							showOnScreenMsg(player, NpcStringId.USE_THE_YE_SAGIRA_TELEPORT_DEVICE_TO_GO_TO_EXPLORATION_AREA_2, ExShowScreenMessage.TOP_CENTER, 4500);
							qs.setCond(3, true);
							htmltext = "33449-05.html";
							break;
						}
						case 3:
						{
							htmltext = "33449-06.html";
							break;
						}
					}
				}
				else if (npc.getId() == NAGEL)
				{
					if (qs.isCond(3))
					{
						htmltext = "33450-01.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == CHESHA ? "33449-08.html" : "33450-04.html";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(2);
			npcLogList.add(new NpcLogListHolder(STALKER, false, qs.getMemoStateEx(STALKER)));
			npcLogList.add(new NpcLogListHolder(CRAWLER, false, qs.getMemoStateEx(CRAWLER)));
			return npcLogList;
		}
		return super.getNpcLogList(player);
	}
}