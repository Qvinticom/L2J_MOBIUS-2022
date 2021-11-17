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
package quests.Q00313_CollectSpores;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

/**
 * Collect Spores (313)
 * @author ivantotov
 */
public class Q00313_CollectSpores extends Quest
{
	// NPC
	private static final int HERBIEL = 30150;
	// Item
	private static final int SPORE_SAC = 1118;
	// Misc
	private static final int MIN_LEVEL = 8;
	private static final int REQUIRED_SAC_COUNT = 10;
	// Monster
	private static final int SPORE_FUNGUS = 20509;
	
	public Q00313_CollectSpores()
	{
		super(313);
		addStartNpc(HERBIEL);
		addTalkId(HERBIEL);
		addKillId(SPORE_FUNGUS);
		registerQuestItems(SPORE_SAC);
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
			case "30150-05.htm":
			{
				if (qs.isCreated())
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "30150-04.htm":
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, killer, false) && giveItemRandomly(killer, npc, SPORE_SAC, 1, REQUIRED_SAC_COUNT, 0.4, true))
		{
			qs.setCond(2);
		}
		return super.onKill(npc, killer, isSummon);
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
				htmltext = player.getLevel() >= MIN_LEVEL ? "30150-03.htm" : "30150-02.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						if (getQuestItemsCount(player, SPORE_SAC) < REQUIRED_SAC_COUNT)
						{
							htmltext = "30150-06.html";
						}
						break;
					}
					case 2:
					{
						if (getQuestItemsCount(player, SPORE_SAC) >= REQUIRED_SAC_COUNT)
						{
							giveAdena(player, 3500, true);
							qs.exitQuest(true, true);
							htmltext = "30150-07.html";
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
}