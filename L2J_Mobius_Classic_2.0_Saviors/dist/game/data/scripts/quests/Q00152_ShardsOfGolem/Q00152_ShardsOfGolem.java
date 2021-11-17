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
package quests.Q00152_ShardsOfGolem;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Shards of Golem (152)
 * @author xban1x
 */
public class Q00152_ShardsOfGolem extends Quest
{
	// NPCs
	private static final int HARRYS = 30035;
	private static final int ALTRAN = 30283;
	// Monster
	private static final int STONE_GOLEM = 20016;
	// Items
	private static final int WOODEN_BREASTPLATE = 1100;
	private static final int HARRYS_1ST_RECIEPT = 1008;
	private static final int HARRYS_2ND_RECIEPT = 1009;
	private static final int GOLEM_SHARD = 1010;
	private static final int TOOL_BOX = 1011;
	// Misc
	private static final int MIN_LEVEL = 10;
	
	public Q00152_ShardsOfGolem()
	{
		super(152);
		addStartNpc(HARRYS);
		addTalkId(HARRYS, ALTRAN);
		addKillId(STONE_GOLEM);
		registerQuestItems(HARRYS_1ST_RECIEPT, HARRYS_2ND_RECIEPT, GOLEM_SHARD, TOOL_BOX);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		String htmltext = null;
		if (qs != null)
		{
			switch (event)
			{
				case "30035-03.htm":
				{
					qs.startQuest();
					giveItems(player, HARRYS_1ST_RECIEPT, 1);
					htmltext = event;
					break;
				}
				case "30283-02.html":
				{
					if (qs.isCond(1) && hasQuestItems(player, HARRYS_1ST_RECIEPT))
					{
						takeItems(player, HARRYS_1ST_RECIEPT, -1);
						giveItems(player, HARRYS_2ND_RECIEPT, 1);
						qs.setCond(2, true);
						htmltext = event;
					}
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(2) && (getRandom(100) < 30) && (getQuestItemsCount(killer, GOLEM_SHARD) < 5))
		{
			giveItems(killer, GOLEM_SHARD, 1);
			if (getQuestItemsCount(killer, GOLEM_SHARD) >= 5)
			{
				qs.setCond(3, true);
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
		switch (npc.getId())
		{
			case HARRYS:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = player.getLevel() >= MIN_LEVEL ? "30035-02.htm" : "30035-01.htm";
						break;
					}
					case State.STARTED:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								if (hasQuestItems(player, HARRYS_1ST_RECIEPT))
								{
									htmltext = "30035-04a.html";
								}
								break;
							}
							case 2:
							case 3:
							{
								if (hasQuestItems(player, HARRYS_2ND_RECIEPT))
								{
									htmltext = "30035-04.html";
								}
								break;
							}
							case 4:
							{
								if (hasQuestItems(player, HARRYS_2ND_RECIEPT, TOOL_BOX))
								{
									giveItems(player, WOODEN_BREASTPLATE, 1);
									qs.exitQuest(false, true);
									htmltext = "30035-05.html";
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
				break;
			}
			case ALTRAN:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						if (hasQuestItems(player, HARRYS_1ST_RECIEPT))
						{
							htmltext = "30283-01.html";
						}
						break;
					}
					case 2:
					{
						if (hasQuestItems(player, HARRYS_2ND_RECIEPT) && (getQuestItemsCount(player, GOLEM_SHARD) < 5))
						{
							htmltext = "30283-03.html";
						}
						break;
					}
					case 3:
					{
						if (hasQuestItems(player, HARRYS_2ND_RECIEPT) && (getQuestItemsCount(player, GOLEM_SHARD) >= 5))
						{
							takeItems(player, GOLEM_SHARD, -1);
							giveItems(player, TOOL_BOX, 1);
							qs.setCond(4, true);
							htmltext = "30283-04.html";
						}
						break;
					}
					case 4:
					{
						if (hasQuestItems(player, HARRYS_2ND_RECIEPT, TOOL_BOX))
						{
							htmltext = "30283-05.html";
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
