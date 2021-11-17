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
package quests.Q296_TarantulasSpiderSilk;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q296_TarantulasSpiderSilk extends Quest
{
	// NPCs
	private static final int MION = 30519;
	private static final int DEFENDER_NATHAN = 30548;
	// Quest Items
	private static final int TARANTULA_SPIDER_SILK = 1493;
	private static final int TARANTULA_SPINNERETTE = 1494;
	// Items
	private static final int RING_OF_RACCOON = 1508;
	private static final int RING_OF_FIREFLY = 1509;
	
	public Q296_TarantulasSpiderSilk()
	{
		super(296, "Tarantula's Spider Silk");
		registerQuestItems(TARANTULA_SPIDER_SILK, TARANTULA_SPINNERETTE);
		addStartNpc(MION);
		addTalkId(MION, DEFENDER_NATHAN);
		addKillId(20394, 20403, 20508); // Crimson Tarantula, Hunter Tarantula, Plunder arantula
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30519-03.htm":
			{
				if (st.hasAtLeastOneQuestItem(RING_OF_RACCOON, RING_OF_FIREFLY))
				{
					st.startQuest();
				}
				else
				{
					htmltext = "30519-03a.htm";
				}
				break;
			}
			case "30519-06.htm":
			{
				st.takeItems(TARANTULA_SPIDER_SILK, -1);
				st.takeItems(TARANTULA_SPINNERETTE, -1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
				break;
			}
			case "30548-02.htm":
			{
				final int count = st.getQuestItemsCount(TARANTULA_SPINNERETTE);
				if (count > 0)
				{
					htmltext = "30548-03.htm";
					st.takeItems(TARANTULA_SPINNERETTE, -1);
					st.giveItems(TARANTULA_SPIDER_SILK, count * (15 + Rnd.get(10)));
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() < 15) ? "30519-01.htm" : "30519-02.htm";
				break;
			}
			case State.STARTED:
			{
				switch (npc.getNpcId())
				{
					case MION:
					{
						final int count = st.getQuestItemsCount(TARANTULA_SPIDER_SILK);
						if (count == 0)
						{
							htmltext = "30519-04.htm";
						}
						else
						{
							htmltext = "30519-05.htm";
							st.takeItems(TARANTULA_SPIDER_SILK, -1);
							st.rewardItems(57, ((count >= 10) ? 2000 : 0) + (count * 30));
						}
						break;
					}
					case DEFENDER_NATHAN:
					{
						htmltext = "30548-01.htm";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		final int rnd = Rnd.get(100);
		if (rnd > 95)
		{
			st.dropItemsAlways(TARANTULA_SPINNERETTE, 1, 0);
		}
		else if (rnd > 45)
		{
			st.dropItemsAlways(TARANTULA_SPIDER_SILK, 1, 0);
		}
		
		return null;
	}
}