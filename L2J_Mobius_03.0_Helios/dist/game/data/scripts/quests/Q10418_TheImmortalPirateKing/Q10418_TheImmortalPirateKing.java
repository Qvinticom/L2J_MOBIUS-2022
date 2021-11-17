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
package quests.Q10418_TheImmortalPirateKing;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * The Immortal Pirate King (10418)
 * @URL https://l2wiki.com/The_Immortal_Pirate_King
 * @author Dmitri
 */
public class Q10418_TheImmortalPirateKing extends Quest
{
	// NPC
	private static final int JERONIN = 30121;
	// Raidboss
	private static final int ZAKEN = 29181;
	// Misc
	private static final int MIN_LEVEL = 83;
	private static final int MAX_LEVEL = 90;
	
	public Q10418_TheImmortalPirateKing()
	{
		super(10418);
		addStartNpc(JERONIN);
		addTalkId(JERONIN);
		addKillId(ZAKEN);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "30121-00.htm");
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
			case "30121-02.htm":
			case "30121-03.htm":
			{
				htmltext = event;
				break;
			}
			case "30121-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30121-07.html":
			{
				// Rewards
				giveAdena(player, 359064, true);
				addExpAndSp(player, 34720560, 41664);
				qs.exitQuest(false, true);
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
				htmltext = "30121-01.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = (qs.isCond(1)) ? "30121-05.html" : "30121-06.html";
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
		if ((qs != null) && qs.isCond(1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			qs.setCond(2, true);
		}
	}
}
