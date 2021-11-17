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
package quests.Q10540_ThwartingMimirsPlan;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

/**
 * Thwarting Mimir's Plan (10540)
 * @URL https://l2wiki.com/Thwarting_Mimir%27s_Plan
 * @author Dmitri
 */
public class Q10540_ThwartingMimirsPlan extends Quest
{
	// NPCs
	private static final int KRENAHT = 34237;
	// Boss
	private static final int MIMIR = 26148;
	// Misc
	private static final int MIN_LEVEL = 100;
	private static final int GIANTS_SCROLL_R_GRADE_WEAPON = 36386;
	
	public Q10540_ThwartingMimirsPlan()
	{
		super(10540);
		addStartNpc(KRENAHT);
		addTalkId(KRENAHT);
		addKillId(MIMIR);
		addCondMinLevel(MIN_LEVEL, "34237-00.htm");
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
			case "34237-02.htm":
			case "34237-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34237-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34237-06.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, GIANTS_SCROLL_R_GRADE_WEAPON, 1);
					addExpAndSp(player, 3954960000L, 9491880);
					qs.exitQuest(false, true);
					htmltext = event;
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
				htmltext = "34237-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "34237-04.htm";
				}
				else
				{
					htmltext = "34237-05.html";
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
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, player, false))
		{
			qs.setCond(2, true);
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
}