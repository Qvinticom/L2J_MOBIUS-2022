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
package quests.Q10368_RebellionOfMonsters;

import java.util.HashSet;
import java.util.Set;

import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * Rebellion of Monsters (10368)
 * @author St3eT
 */
public final class Q10368_RebellionOfMonsters extends Quest
{
	// NPCs
	private static final int FRED = 33179;
	private static final int WEARY_JAGUAR = 23024;
	private static final int WEARY_JAGUAR_SCOUT = 23025;
	private static final int ANT_SOLDIER = 23099;
	private static final int ANT_WARRIOR_CAPTAIN = 23100;
	// Misc
	private static final int MIN_LEVEL = 34;
	private static final int MAX_LEVEL = 40;
	
	public Q10368_RebellionOfMonsters()
	{
		super(10368);
		addStartNpc(FRED);
		addTalkId(FRED);
		addKillId(WEARY_JAGUAR, WEARY_JAGUAR_SCOUT, ANT_SOLDIER, ANT_WARRIOR_CAPTAIN);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33179-08.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33179-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33179-03.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "33179-06.html":
			{
				if (st.isCond(2))
				{
					giveAdena(player, 990, true);
					addExpAndSp(player, 750000, 180);
					st.exitQuest(false, true);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = "33179-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (st.isCond(1))
				{
					htmltext = "33179-04.html";
				}
				else if (st.isCond(2))
				{
					htmltext = "33179-05.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = "33179-07.html";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState st = getQuestState(killer, false);
		
		if ((st != null) && st.isStarted() && st.isCond(1))
		{
			int killedJaguar = st.getInt("killed_" + WEARY_JAGUAR);
			int killedJaguarScout = st.getInt("killed_" + WEARY_JAGUAR_SCOUT);
			int killedSoldier = st.getInt("killed_" + ANT_SOLDIER);
			int killedCaptain = st.getInt("killed_" + ANT_WARRIOR_CAPTAIN);
			
			switch (npc.getId())
			{
				case WEARY_JAGUAR:
				{
					if (killedJaguar < 10)
					{
						killedJaguar++;
						st.set("killed_" + WEARY_JAGUAR, killedJaguar);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case WEARY_JAGUAR_SCOUT:
				{
					if (killedJaguarScout < 15)
					{
						killedJaguarScout++;
						st.set("killed_" + WEARY_JAGUAR_SCOUT, killedJaguarScout);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case ANT_SOLDIER:
				{
					if (killedSoldier < 15)
					{
						killedSoldier++;
						st.set("killed_" + ANT_SOLDIER, killedSoldier);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case ANT_WARRIOR_CAPTAIN:
				{
					if (killedCaptain < 20)
					{
						killedCaptain++;
						st.set("killed_" + ANT_WARRIOR_CAPTAIN, killedCaptain);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
			}
			
			if ((killedJaguar == 10) && (killedJaguarScout == 15) && (killedSoldier == 15) && (killedCaptain == 20))
			{
				st.setCond(2, true);
			}
			sendNpcLogList(killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(L2PcInstance activeChar)
	{
		final QuestState st = getQuestState(activeChar, false);
		if ((st != null) && st.isStarted() && st.isCond(1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(4);
			npcLogList.add(new NpcLogListHolder(WEARY_JAGUAR, false, st.getInt("killed_" + WEARY_JAGUAR)));
			npcLogList.add(new NpcLogListHolder(WEARY_JAGUAR_SCOUT, false, st.getInt("killed_" + WEARY_JAGUAR_SCOUT)));
			npcLogList.add(new NpcLogListHolder(ANT_SOLDIER, false, st.getInt("killed_" + ANT_SOLDIER)));
			npcLogList.add(new NpcLogListHolder(ANT_WARRIOR_CAPTAIN, false, st.getInt("killed_" + ANT_WARRIOR_CAPTAIN)));
			return npcLogList;
		}
		return super.getNpcLogList(activeChar);
	}
}