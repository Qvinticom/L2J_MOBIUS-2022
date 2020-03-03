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
package quests.Q00943_FillingTheEnergyOfDestruction;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

/**
 * Filling the Energy of Destruction (943)
 * @author karma12
 */
public class Q00943_FillingTheEnergyOfDestruction extends Quest
{
	// NPC
	private static final int SEED_TALISMAN_MANAGER = 33715;
	// Raids
	private static final int ISTINA_EASY = 29195;
	private static final int ISTINA_HARD = 29196;
	private static final int OCTAVIS_EASY = 29194;
	private static final int OCTAVIS_HARD = 29212;
	private static final int SPEZION_EASY = 25867;
	private static final int SPEZION_HARD = 25868;
	private static final int BAYLOR = 29213;
	private static final int BALOK = 29218;
	private static final int RON = 25825;
	private static final int TAUTI_1 = 29236;
	private static final int TAUTI_2 = 29237;
	private static final int TAUTI_3 = 29238;
	// Item
	private static final int CORE_OF_TWISTED_MAGIC = 35668;
	// Rewards
	private static final int ENERGY_OF_DESTRUCTION = 35562;
	
	public Q00943_FillingTheEnergyOfDestruction()
	{
		super(943);
		addStartNpc(SEED_TALISMAN_MANAGER);
		addTalkId(SEED_TALISMAN_MANAGER);
		addKillId(ISTINA_EASY, ISTINA_HARD, OCTAVIS_EASY, OCTAVIS_HARD, SPEZION_EASY, SPEZION_HARD, BAYLOR, BALOK, RON, TAUTI_1, TAUTI_2, TAUTI_3);
		registerQuestItems(CORE_OF_TWISTED_MAGIC);
	}
	
	@Override
	public void actionForEachPlayer(PlayerInstance player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && Util.checkIfInRange(1500, npc, player, false))
		{
			giveItems(player, CORE_OF_TWISTED_MAGIC, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			qs.setCond(2, true);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "33715-03.htm":
			{
				qs.startQuest();
				break;
			}
			case "33715-06.html":
			{
				takeItems(player, CORE_OF_TWISTED_MAGIC, 1);
				giveItems(player, ENERGY_OF_DESTRUCTION, 1);
				if (player.getLevel() >= 99)
				{
					addExpAndSp(player, 0, 5371901);
				}
				qs.exitQuest(QuestType.DAILY, true);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, true);
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.COMPLETED:
			{
				if (qs.isNowAvailable())
				{
					qs.setState(State.CREATED);
					htmltext = (player.getLevel() >= 90) ? "33715-01.htm" : "33715-00.htm";
				}
				else
				{
					htmltext = "33715-07.html";
				}
				break;
			}
			case State.CREATED:
			{
				htmltext = (player.getLevel() >= 90) ? "33715-01.htm" : "33715-00.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33715-04.html";
				}
				else if (qs.isCond(2))
				{
					if (player.getLevel() < 90)
					{
						htmltext = "33715-00a.html";
					}
					else
					{
						htmltext = "33715-05.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
}
