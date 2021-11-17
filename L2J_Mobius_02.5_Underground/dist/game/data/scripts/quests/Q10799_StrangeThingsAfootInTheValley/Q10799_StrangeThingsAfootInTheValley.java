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
package quests.Q10799_StrangeThingsAfootInTheValley;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * Strange Things Afoot in the Valley (10799)
 * @URL https://l2wiki.com/Strange_Things_Afoot_in_the_Valley
 * @author Gigi
 */
public class Q10799_StrangeThingsAfootInTheValley extends Quest
{
	// NPCs
	private static final int NAMO = 33973;
	// Monsters
	private static final int[] MONSTERS =
	{
		23423, // Mesmer Dragon
		23424, // Gargoyle Dragon
		23425, // Black Dragon
		23427, // Sand Dragon
		23428, // Captain Dragonblood
		23429, // Minion Dragonblood
		23436, // Cave Servant Archer
		23437, // Cave Servant Warrior
		23438, // Metallic Cave Servant
		23439, // Iron Cave Servant
		23440, // Headless Knight
		23447 // Gust Spiral
	};
	// Items
	private static final int EAS = 960;
	// Misc
	private static final int MIN_LEVEL = 76;
	private static final int MAX_LEVEL = 85;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10799_StrangeThingsAfootInTheValley()
	{
		super(10799);
		addStartNpc(NAMO);
		addTalkId(NAMO);
		addKillId(MONSTERS);
		addCondRace(Race.ERTHEIA, "33973-00.htm");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33973-09.htm");
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
			case "33973-07.html":
			{
				htmltext = event;
				break;
			}
			case "33973-04.htm":
			{
				qs.startQuest();
				break;
			}
			default:
			{
				if (qs.isCond(2) && event.startsWith("giveReward_") && (player.getLevel() >= MIN_LEVEL))
				{
					final int itemId = Integer.parseInt(event.replace("giveReward_", ""));
					qs.exitQuest(false, true);
					giveStoryQuestReward(player, 196);
					giveItems(player, EAS, 10);
					giveItems(player, itemId, 30);
					addExpAndSp(player, 76658400, 18398);
					htmltext = "33973-08.html";
				}
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
				htmltext = "33973-01.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = (qs.isCond(1)) ? "33973-05.html" : "33973-06.html";
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
			int count = qs.getInt(KILL_COUNT_VAR);
			qs.set(KILL_COUNT_VAR, ++count);
			if (count >= 100)
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
		if ((qs != null) && qs.isCond(1))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR);
			if (killCount > 0)
			{
				final Set<NpcLogListHolder> holder = new HashSet<>();
				holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_IN_THE_DRAGON_VALLEY, killCount));
				return holder;
			}
		}
		return super.getNpcLogList(player);
	}
}