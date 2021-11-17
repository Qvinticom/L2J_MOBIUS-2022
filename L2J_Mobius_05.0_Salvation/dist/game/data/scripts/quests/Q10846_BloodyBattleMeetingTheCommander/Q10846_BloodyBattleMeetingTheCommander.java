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
package quests.Q10846_BloodyBattleMeetingTheCommander;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Bloody Battle - Meeting the Commander (10846)
 * @URL https://l2wiki.com/Bloody_Battle_-_Meeting_the_Commander
 * @author Dmitri
 */
public class Q10846_BloodyBattleMeetingTheCommander extends Quest
{
	// NPCs
	private static final int HURAK = 34064;
	private static final int DEVIANNE = 34323;
	// MiniBoss
	private static final int BURNSTEIN = 26136; // Burnstein
	// Item
	private static final int RUNE_STONE = 39738;
	private static final int SCROLL_OF_ESCAPE_BLACKBIRD_CAMPSITE = 46158;
	// Misc
	private static final int MIN_LEVEL = 101;
	
	public Q10846_BloodyBattleMeetingTheCommander()
	{
		super(10846);
		addStartNpc(HURAK);
		addTalkId(HURAK, DEVIANNE);
		addKillId(BURNSTEIN);
		addCondMinLevel(MIN_LEVEL, "34064-00.htm");
		// addCondCompletedQuest(Q10845_BloodyBattleRescueTheSmiths.class.getSimpleName(), "34064-00.htm");
		addFactionLevel(Faction.KINGDOM_ROYAL_GUARDS, 4, "34064-00.htm");
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
			case "34064-04.htm":
			case "34064-03.htm":
			case "34064-02.htm":
			case "34323-02.html":
			{
				htmltext = event;
				break;
			}
			case "34064-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34323-03.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, RUNE_STONE, 1);
					giveItems(player, SCROLL_OF_ESCAPE_BLACKBIRD_CAMPSITE, 1);
					addExpAndSp(player, 7262301690L, 17429400);
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
				if (npc.getId() == HURAK)
				{
					htmltext = "34064-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case HURAK:
					{
						if (qs.isCond(1))
						{
							htmltext = "34064-06.html";
						}
						break;
					}
					case DEVIANNE:
					{
						if (qs.isCond(2))
						{
							htmltext = "34323-01.html";
						}
						break;
					}
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
