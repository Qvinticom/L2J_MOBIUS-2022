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
package quests.Q00586_MutatedCreatures;

import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Mutated Creatures (586)
 * @URL https://l2wiki.com/Mutated_Creatures
 * @author Dmitri
 */
public class Q00586_MutatedCreatures extends Quest
{
	// NPC
	private static final int NERUPA = 30370;
	// Monsters
	private static final int COCOON_DESTROYER = 19294; // Cocoon Destroyer (Violent) 93
	// Items
	private static final int COCOON_DESTROYER_SHELL = 48382;
	// Misc
	private static final int MIN_LEVEL = 90;
	private static final int MAX_LEVEL = 100;
	
	public Q00586_MutatedCreatures()
	{
		super(586);
		addStartNpc(NERUPA);
		addTalkId(NERUPA);
		addKillId(COCOON_DESTROYER);
		registerQuestItems(COCOON_DESTROYER_SHELL);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "nolevel.html");
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
			case "30370-02.htm":
			case "30370-03.htm":
			{
				htmltext = event;
				break;
			}
			case "30370-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30370-07.html":
			{
				giveAdena(player, 559020, true);
				addExpAndSp(player, 646727130, 646710);
				qs.exitQuest(QuestType.DAILY, true);
				htmltext = event;
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
				htmltext = "30370-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "30370-05.html";
				}
				else
				{
					htmltext = "30370-06.html";
				}
				break;
			}
			case State.COMPLETED:
			
			{
				if (qs.isNowAvailable())
				{
					qs.setState(State.CREATED);
					htmltext = "30370-01.htm";
				}
				else
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
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
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, npc, COCOON_DESTROYER_SHELL, 1, 25, 1, true))
		{
			qs.setCond(2, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}