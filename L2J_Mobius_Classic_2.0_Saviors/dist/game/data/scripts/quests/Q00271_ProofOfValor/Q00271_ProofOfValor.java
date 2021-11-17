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
package quests.Q00271_ProofOfValor;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Proof of Valor (271)
 * @author xban1x
 */
public class Q00271_ProofOfValor extends Quest
{
	// NPC
	private static final int RUKAIN = 30577;
	// Items
	private static final int KASHA_WOLF_FANG = 1473;
	// Monsters
	private static final int KASHA_WOLF = 20475;
	// Rewards
	private static final int HEALING_POTION = 1539;
	private static final int NECKLACE_OF_VALOR = 1507;
	// Misc
	private static final int MIN_LEVEL = 4;
	private static final int MAX_LEVEL = 8;
	
	public Q00271_ProofOfValor()
	{
		super(271);
		addStartNpc(RUKAIN);
		addTalkId(RUKAIN);
		addKillId(KASHA_WOLF);
		registerQuestItems(KASHA_WOLF_FANG);
		addCondMaxLevel(MAX_LEVEL, "30577-02.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && event.equalsIgnoreCase("30577-04.htm"))
		{
			qs.startQuest();
			return hasAtLeastOneQuestItem(player, NECKLACE_OF_VALOR) ? "30577-08.html" : event;
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			final long count = getQuestItemsCount(killer, KASHA_WOLF_FANG);
			final int amount = ((getRandom(100) < 25) && (count < 49)) ? 2 : 1;
			giveItems(killer, KASHA_WOLF_FANG, amount);
			if ((count + amount) >= 50)
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
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getRace() == Race.ORC) ? (player.getLevel() >= MIN_LEVEL) ? (hasAtLeastOneQuestItem(player, NECKLACE_OF_VALOR)) ? "30577-07.htm" : "30577-03.htm" : "30577-02.htm" : "30577-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30577-05.html";
						break;
					}
					case 2:
					{
						if (getQuestItemsCount(player, KASHA_WOLF_FANG) >= 50)
						{
							if (getRandom(100) <= 13)
							{
								rewardItems(player, NECKLACE_OF_VALOR, 1);
								rewardItems(player, HEALING_POTION, 1);
							}
							else
							{
								rewardItems(player, NECKLACE_OF_VALOR, 1);
							}
							takeItems(player, KASHA_WOLF_FANG, -1);
							qs.exitQuest(true, true);
							htmltext = "30577-06.html";
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
