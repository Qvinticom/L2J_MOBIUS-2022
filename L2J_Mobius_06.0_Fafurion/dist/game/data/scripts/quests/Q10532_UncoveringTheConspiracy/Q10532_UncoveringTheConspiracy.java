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
package quests.Q10532_UncoveringTheConspiracy;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * Uncovering the Conspiracy (10532)
 * @URL https://youtu.be/y8OmynL8LVA https://l2wiki.com/Uncovering_the_Conspiracy
 * @author Darkloud
 * @date 2020-01-15 - [01:00:1009]
 */
public class Q10532_UncoveringTheConspiracy extends Quest
{
	// NPCs
	private static final int NAMO = 33973;
	private static final int[] MONSTERS =
	{
		23430, // Prey Drake
		23431, // Best Drake
		23432, // Dust Drake
		23426, // Vampiric Drake
		23441, // Bloody Grave Warrior
		23442, // Dark Grave Warrior
		23443, // Dark Grave Wizard
		23444, // Dark Grave Knight
	};
	
	// Rewards
	private static final int XP = 651696104;
	private static final int SP = 30466;
	// Misc
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	
	public Q10532_UncoveringTheConspiracy()
	{
		super(10532);
		addStartNpc(NAMO);
		addTalkId(NAMO);
		addKillId(MONSTERS);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, getNoQuestMsg(null));
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
			case "33973-02.htm":
			case "33973-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33973-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33973-05.htm":
			{
				htmltext = event;
				break;
			}
			case "33973-06.htm":
			{
				if (qs.isCond(2))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, XP, SP);
						qs.exitQuest(QuestType.ONE_TIME, true);
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
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
		String htmltext = null;
		final QuestState qs = getQuestState(player, true);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "33973-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(2))
				{
					htmltext = "33973-05.htm";
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
		if ((qs != null) && qs.isStarted() && qs.isCond(1))
		{
			int killCount = qs.getInt("KILLED_COUNT");
			killCount++;
			qs.set("KILLED_COUNT", killCount);
			if (killCount >= 200)
			{
				qs.setCond(2, true);
			}
			else
			{
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isStarted() && qs.isCond(1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(1);
			npcLogList.add(new NpcLogListHolder(NpcStringId.SUBJUGATION_IN_THE_SOUTHERN_DRAGON_VALLEY, qs.getInt("KILLED_COUNT")));
			return npcLogList;
		}
		return super.getNpcLogList(player);
	}
}
