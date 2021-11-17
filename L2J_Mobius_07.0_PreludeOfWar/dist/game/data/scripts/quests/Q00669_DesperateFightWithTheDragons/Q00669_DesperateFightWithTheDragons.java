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
package quests.Q00669_DesperateFightWithTheDragons;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Desperate Fight With The Dragons (669)
 * @URL https://l2wiki.com/Intense_Fight_against_Dragon
 * @author Dmitri
 */
public class Q00669_DesperateFightWithTheDragons extends Quest
{
	// NPCs
	private static final int ARCTURUS = 34267;
	private static final int COLIN = 30703;
	// BOSS
	private static final int[] BOSSES =
	{
		29240, // Lindvior 104 Altar of Sacrifice
		29317, // Lindvior 104
		29068, // Antharas 118
		29028, // Valakas 118
		29361, // Fafurion Stage 1 118
		29362, // Fafurion Stage 2 118
		29363, // Fafurion Stage 3 118
		29364, // Fafurion Stage 4 118
		29365, // Fafurion Stage 5 118
		29366, // Fafurion Stage 6 118
		29367, // Fafurion Stage 7 118
	};
	// Misc
	private static final int MIN_LEVEL = 85;
	
	public Q00669_DesperateFightWithTheDragons()
	{
		super(669);
		addStartNpc(ARCTURUS, COLIN);
		addTalkId(ARCTURUS, COLIN);
		addKillId(BOSSES);
		addCondMinLevel(MIN_LEVEL, "34267-00.htm");
		addFactionLevel(Faction.HUNTERS_GUILD, 2, "34267-00.htm");
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
			case "30703-02.htm":
			case "30703-03.htm":
			case "34267-02.htm":
			case "34267-03.htm":
			{
				htmltext = event;
				break;
			}
			case "30703-04.htm":
			case "34267-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30703-07.html":
			case "34267-07.html":
			{
				addFactionPoints(player, Faction.HUNTERS_GUILD, 200);
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
				switch (npc.getId())
				{
					case COLIN:
					{
						htmltext = "30703-01.htm";
						break;
					}
					case ARCTURUS:
					{
						htmltext = "34267-01.htm";
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case COLIN:
					{
						htmltext = (qs.isCond(1)) ? "30703-05.html" : "30703-06.html";
						break;
					}
					case ARCTURUS:
					{
						htmltext = (qs.isCond(1)) ? "34267-05.html" : "34267-06.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (!qs.isNowAvailable())
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
					break;
				}
				qs.setState(State.CREATED);
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
