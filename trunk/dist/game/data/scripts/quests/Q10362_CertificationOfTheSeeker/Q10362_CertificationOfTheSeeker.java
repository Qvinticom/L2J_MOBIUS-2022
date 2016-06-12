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

import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q10361_RolesOfTheSeeker.Q10361_RolesOfTheSeeker;

/**
 * Certification of The Seeker (10362)
 * @author Gladicek
 */
public final class Q10362_CertificationOfTheSeeker extends Quest
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
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33449-07.htm");
		addCondCompletedQuest(Q10361_RolesOfTheSeeker.class.getSimpleName(), "33449-07.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
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
			case "33450-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33449-03.htm":
			{
				qs.startQuest();
				qs.setMemoStateEx(STALKER, 0);
				qs.setMemoStateEx(CRAWLER, 0);
				htmltext = event;
				break;
			}
			case "33450-03.htm":
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
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
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
					sendNpcLogList(killer);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (npc.getId() == CRAWLER)
			{
				killedCrawler++;
				if (killedCrawler <= 5)
				{
					qs.setMemoStateEx(CRAWLER, killedCrawler);
					sendNpcLogList(killer);
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
	public String onTalk(L2Npc npc, L2PcInstance player, boolean isSimulated)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		
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
							htmltext = "33449-04.htm";
							break;
						}
						case 2:
						{
							if (!isSimulated)
							{
								showOnScreenMsg(player, NpcStringId.USE_THE_YE_SAGIRA_TELEPORT_DEVICE_TO_GO_TO_EXPLORATION_AREA_2, ExShowScreenMessage.TOP_CENTER, 4500);
								qs.setCond(3, true);
							}
							htmltext = "33449-05.htm";
							break;
						}
						case 3:
						{
							htmltext = "33449-06.htm";
							break;
						}
					}
				}
				else if (npc.getId() == NAGEL)
				{
					if (qs.isCond(3))
					{
						htmltext = "33450-01.htm";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == CHESHA ? "33449-08.htm" : "33450-04.htm";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(L2PcInstance activeChar)
	{
		final QuestState qs = getQuestState(activeChar, false);
		
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(2);
			npcLogList.add(new NpcLogListHolder(STALKER, false, qs.getMemoStateEx(STALKER)));
			npcLogList.add(new NpcLogListHolder(CRAWLER, false, qs.getMemoStateEx(CRAWLER)));
			return npcLogList;
		}
		return super.getNpcLogList(activeChar);
	}
}