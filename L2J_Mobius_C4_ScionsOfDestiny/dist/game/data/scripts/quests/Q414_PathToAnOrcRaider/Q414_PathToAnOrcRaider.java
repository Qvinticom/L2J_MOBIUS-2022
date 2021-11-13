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
package quests.Q414_PathToAnOrcRaider;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Q414_PathToAnOrcRaider extends Quest
{
	// NPCs
	private static final int KARUKIA = 30570;
	private static final int KASMAN = 30501;
	private static final int TAZEER = 31978;
	// Monsters
	private static final int GOBLIN_TOMB_RAIDER_LEADER = 20320;
	private static final int KURUKA_RATMAN_LEADER = 27045;
	private static final int UMBAR_ORC = 27054;
	private static final int TIMORA_ORC = 27320;
	// Items
	private static final int GREEN_BLOOD = 1578;
	private static final int GOBLIN_DWELLING_MAP = 1579;
	private static final int KURUKA_RATMAN_TOOTH = 1580;
	private static final int BETRAYER_REPORT_1 = 1589;
	private static final int BETRAYER_REPORT_2 = 1590;
	private static final int HEAD_OF_BETRAYER = 1591;
	private static final int MARK_OF_RAIDER = 1592;
	private static final int TIMORA_ORC_HEAD = 8544;
	
	public Q414_PathToAnOrcRaider()
	{
		super(414, "Path to an Orc Raider");
		registerQuestItems(GREEN_BLOOD, GOBLIN_DWELLING_MAP, KURUKA_RATMAN_TOOTH, BETRAYER_REPORT_1, BETRAYER_REPORT_2, HEAD_OF_BETRAYER, TIMORA_ORC_HEAD);
		addStartNpc(KARUKIA);
		addTalkId(KARUKIA, KASMAN, TAZEER);
		addKillId(GOBLIN_TOMB_RAIDER_LEADER, KURUKA_RATMAN_LEADER, UMBAR_ORC, TIMORA_ORC);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30570-05.htm":
			{
				if (player.getClassId() != ClassId.ORC_FIGHTER)
				{
					htmltext = (player.getClassId() == ClassId.ORC_RAIDER) ? "30570-02a.htm" : "30570-03.htm";
				}
				else if (player.getLevel() < 19)
				{
					htmltext = "30570-02.htm";
				}
				else if (st.hasQuestItems(MARK_OF_RAIDER))
				{
					htmltext = "30570-04.htm";
				}
				else
				{
					st.startQuest();
					st.giveItems(GOBLIN_DWELLING_MAP, 1);
				}
				break;
			}
			case "30570-07a.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(GOBLIN_DWELLING_MAP, 1);
				st.takeItems(KURUKA_RATMAN_TOOTH, -1);
				st.giveItems(BETRAYER_REPORT_1, 1);
				st.giveItems(BETRAYER_REPORT_2, 1);
				break;
			}
			case "30570-07b.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(GOBLIN_DWELLING_MAP, 1);
				st.takeItems(KURUKA_RATMAN_TOOTH, -1);
				break;
			}
			case "31978-03.htm":
			{
				st.setCond(6);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = "30570-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case KARUKIA:
					{
						if (cond == 1)
						{
							htmltext = "30570-06.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30570-07.htm";
						}
						else if ((cond == 3) || (cond == 4))
						{
							htmltext = "30570-08.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30570-07b.htm";
						}
						break;
					}
					case KASMAN:
					{
						if (cond == 3)
						{
							htmltext = "30501-01.htm";
						}
						else if (cond == 4)
						{
							if (st.getQuestItemsCount(HEAD_OF_BETRAYER) == 1)
							{
								htmltext = "30501-02.htm";
							}
							else
							{
								htmltext = "30501-03.htm";
								st.takeItems(BETRAYER_REPORT_1, 1);
								st.takeItems(BETRAYER_REPORT_2, 1);
								st.takeItems(HEAD_OF_BETRAYER, -1);
								st.giveItems(MARK_OF_RAIDER, 1);
								st.rewardExpAndSp(3200, 2360);
								player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(true);
							}
						}
						break;
					}
					case TAZEER:
					{
						if (cond == 5)
						{
							htmltext = "31978-01.htm";
						}
						else if (cond == 6)
						{
							htmltext = "31978-04.htm";
						}
						else if (cond == 7)
						{
							htmltext = "31978-05.htm";
							st.takeItems(TIMORA_ORC_HEAD, 1);
							st.giveItems(MARK_OF_RAIDER, 1);
							st.rewardExpAndSp(3200, 2360);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					}
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case GOBLIN_TOMB_RAIDER_LEADER:
			{
				if (st.isCond(1))
				{
					if (st.getQuestItemsCount(GREEN_BLOOD) <= Rnd.get(20))
					{
						st.playSound(QuestState.SOUND_ITEMGET);
						st.giveItems(GREEN_BLOOD, 1);
					}
					else
					{
						st.takeItems(GREEN_BLOOD, -1);
						addSpawn(KURUKA_RATMAN_LEADER, npc, false, 300000);
					}
				}
				break;
			}
			case KURUKA_RATMAN_LEADER:
			{
				if (st.isCond(1) && st.dropItemsAlways(KURUKA_RATMAN_TOOTH, 1, 10))
				{
					st.setCond(2);
				}
				break;
			}
			case UMBAR_ORC:
			{
				if ((st.isCond(3) || st.isCond(4)) && (st.getQuestItemsCount(HEAD_OF_BETRAYER) < 2) && (Rnd.get(10) < 2))
				{
					if (st.isCond(3))
					{
						st.setCond(4);
					}
					
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(HEAD_OF_BETRAYER, 1);
				}
				break;
			}
			case TIMORA_ORC:
			{
				if (st.isCond(6) && st.dropItems(TIMORA_ORC_HEAD, 1, 1, 600000))
				{
					st.setCond(7);
				}
				break;
			}
		}
		
		return null;
	}
}