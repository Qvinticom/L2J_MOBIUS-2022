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
package quests.Q00159_ProtectTheWaterSource;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Protect the Water Source (159)
 * @author xban1x
 */
public class Q00159_ProtectTheWaterSource extends Quest
{
	// NPC
	private static final int ASTERIOS = 30154;
	// Monster
	private static final int PLAGUE_ZOMBIE = 27017;
	// Items
	private static final int PLAGUE_DUST = 1035;
	private static final int HYACINTH_CHARM = 1071;
	private static final int HYACINTH_CHARM2 = 1072;
	// Misc
	private static final int MIN_LEVEL = 12;
	
	public Q00159_ProtectTheWaterSource()
	{
		super(159);
		addStartNpc(ASTERIOS);
		addTalkId(ASTERIOS);
		addKillId(PLAGUE_ZOMBIE);
		registerQuestItems(PLAGUE_DUST, HYACINTH_CHARM, HYACINTH_CHARM2);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && event.equals("30154-04.htm"))
		{
			qs.startQuest();
			giveItems(player, HYACINTH_CHARM, 1);
			return event;
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null))
		{
			switch (qs.getCond())
			{
				case 1:
				{
					if ((getRandom(100) < 40) && hasQuestItems(killer, HYACINTH_CHARM) && !hasQuestItems(killer, PLAGUE_DUST))
					{
						giveItems(killer, PLAGUE_DUST, 1);
						qs.setCond(2, true);
					}
					break;
				}
				case 3:
				{
					long dust = getQuestItemsCount(killer, PLAGUE_DUST);
					if ((getRandom(100) < 40) && (dust < 5) && hasQuestItems(killer, HYACINTH_CHARM2))
					{
						giveItems(killer, PLAGUE_DUST, 1);
						if ((++dust) >= 5)
						{
							qs.setCond(4, true);
						}
						else
						{
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				}
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
				htmltext = (player.getRace() == Race.ELF) ? (player.getLevel() >= MIN_LEVEL ? "30154-03.htm" : "30154-02.htm") : "30154-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						if (hasQuestItems(player, HYACINTH_CHARM) && !hasQuestItems(player, PLAGUE_DUST))
						{
							htmltext = "30154-05.html";
						}
						break;
					}
					case 2:
					{
						if (hasQuestItems(player, HYACINTH_CHARM, PLAGUE_DUST))
						{
							takeItems(player, HYACINTH_CHARM, -1);
							takeItems(player, PLAGUE_DUST, -1);
							giveItems(player, HYACINTH_CHARM2, 1);
							qs.setCond(3, true);
							htmltext = "30154-06.html";
						}
						break;
					}
					case 3:
					{
						if (hasQuestItems(player, HYACINTH_CHARM2))
						{
							htmltext = "30154-07.html";
						}
						break;
					}
					case 4:
					{
						if (hasQuestItems(player, HYACINTH_CHARM2) && (getQuestItemsCount(player, PLAGUE_DUST) >= 5))
						{
							giveAdena(player, 18250, true);
							qs.exitQuest(false, true);
							htmltext = "30154-08.html";
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
}
