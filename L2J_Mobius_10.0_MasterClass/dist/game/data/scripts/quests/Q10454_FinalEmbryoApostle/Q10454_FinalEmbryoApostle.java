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
package quests.Q10454_FinalEmbryoApostle;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Final Embryo Apostle (10454)
 * @URL https://l2wiki.com/Final_Embryo_Apostle
 * @author Dmitri
 */
public class Q10454_FinalEmbryoApostle extends Quest
{
	// NPCs
	private static final int ERDA = 34319;
	// Boss
	private static final int CAMILLE = 26236; // Camille - Inner Messiahs Castle
	// Item
	private static final int SCROLL_ENCHANT_R_GRADE_WEAPON = 19447;
	private static final int SCROLL_ENCHANT_R_GRADE_ARMOR = 19448;
	// Misc
	private static final int MIN_LEVEL = 102;
	
	public Q10454_FinalEmbryoApostle()
	{
		super(10454);
		addStartNpc(ERDA);
		addTalkId(ERDA);
		addKillId(CAMILLE);
		addCondMinLevel(MIN_LEVEL, "34319-00.htm");
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
			case "34319-02.htm":
			case "34319-03.htm":
			case "34319-07.html":
			{
				htmltext = event;
				break;
			}
			case "34319-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34319-08.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, SCROLL_ENCHANT_R_GRADE_WEAPON, 1);
					giveItems(player, SCROLL_ENCHANT_R_GRADE_ARMOR, 1);
					addExpAndSp(player, 36255499714L, 87013199);
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
				htmltext = "34319-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "34319-05.html";
				}
				else
				{
					htmltext = "34319-06.html";
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
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			qs.setCond(2, true);
		}
	}
}
