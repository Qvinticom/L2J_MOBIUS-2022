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
package quests.Q10877_BreakThroughCrisis;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.data.xml.MonsterBookData;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.MonsterBookCardHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10873_ExaltedReachingAnotherLevel.Q10873_ExaltedReachingAnotherLevel;

/**
 * Break Through Crisis (10877)
 * @URL https://l2wiki.com/Break_Through_Crisis
 * @author CostyKiller
 */
public class Q10877_BreakThroughCrisis extends Quest
{
	// NPC
	private static final int ARCTURUS = 34267;
	// Items
	private static final ItemHolder LIONEL_HUNTERS_LIST_PART_4 = new ItemHolder(47829, 1);
	// Rewards
	private static final int ARCTURUS_CERTIFICATE = 47833;
	// Misc
	private static final int MIN_LEVEL = 103;
	private static final int BESTIARY_PAGES_NEEDED = 10;
	
	public Q10877_BreakThroughCrisis()
	{
		super(10877);
		addStartNpc(ARCTURUS);
		addTalkId(ARCTURUS);
		addCondMinLevel(MIN_LEVEL, "34267-00.html");
		addCondStartedQuest(Q10873_ExaltedReachingAnotherLevel.class.getSimpleName(), "34267-00.html");
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
			case "34267-02.htm":
			case "34267-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34267-04.html":
			{
				if (hasItem(player, LIONEL_HUNTERS_LIST_PART_4))
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "34267-07.html":
			{
				qs.setCond(2);
				htmltext = event;
				break;
			}
			case "34267-08.html":
			{
				// 64 to 103 card ids of hunters guild mobs
				final List<Integer> killedMobs = new ArrayList<>();
				for (MonsterBookCardHolder card : MonsterBookData.getInstance().getMonsterBookCards())
				{
					if ((card.getFaction() == Faction.HUNTERS_GUILD) && (player.getMonsterBookKillCount(card.getId()) > 0))
					{
						killedMobs.add(card.getId());
					}
				}
				if (killedMobs.size() >= BESTIARY_PAGES_NEEDED)
				{
					addExpAndSp(player, 34471245000L, 634471244);
					giveItems(player, ARCTURUS_CERTIFICATE, 1);
					qs.exitQuest(false, true);
					
					final Quest mainQ = QuestManager.getInstance().getQuest(Q10873_ExaltedReachingAnotherLevel.class.getSimpleName());
					if (mainQ != null)
					{
						mainQ.notifyEvent("SUBQUEST_FINISHED_NOTIFY", npc, player);
					}
					htmltext = event;
				}
				else
				{
					htmltext = "34267-06.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (hasItem(player, LIONEL_HUNTERS_LIST_PART_4))
				{
					htmltext = "34267-01.htm";
				}
				else
				{
					htmltext = "34267-00.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					// 64 to 103 card ids of hunters guild mobs
					final List<Integer> killedMobs = new ArrayList<>();
					for (MonsterBookCardHolder card : MonsterBookData.getInstance().getMonsterBookCards())
					{
						if ((card.getFaction() == Faction.HUNTERS_GUILD) && (player.getMonsterBookKillCount(card.getId()) > 0))
						{
							killedMobs.add(card.getId());
						}
					}
					if (killedMobs.size() >= BESTIARY_PAGES_NEEDED)
					{
						htmltext = "34267-06.html";
					}
				}
				else
				{
					htmltext = "34267-05.html";
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
}
