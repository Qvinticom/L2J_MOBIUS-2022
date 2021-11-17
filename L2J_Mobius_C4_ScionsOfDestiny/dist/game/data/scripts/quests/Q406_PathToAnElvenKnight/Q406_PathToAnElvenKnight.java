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
package quests.Q406_PathToAnElvenKnight;

import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Q406_PathToAnElvenKnight extends Quest
{
	// NPCs
	private static final int SORIUS = 30327;
	private static final int KLUTO = 30317;
	// Items
	private static final int SORIUS_LETTER = 1202;
	private static final int KLUTO_BOX = 1203;
	private static final int ELVEN_KNIGHT_BROOCH = 1204;
	private static final int TOPAZ_PIECE = 1205;
	private static final int EMERALD_PIECE = 1206;
	private static final int KLUTO_MEMO = 1276;
	
	public Q406_PathToAnElvenKnight()
	{
		super(406, "Path to an Elven Knight");
		registerQuestItems(SORIUS_LETTER, KLUTO_BOX, TOPAZ_PIECE, EMERALD_PIECE, KLUTO_MEMO);
		addStartNpc(SORIUS);
		addTalkId(SORIUS, KLUTO);
		addKillId(20035, 20042, 20045, 20051, 20054, 20060, 20782);
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
			case "30327-05.htm":
			{
				if (player.getClassId() != ClassId.ELVEN_FIGHTER)
				{
					htmltext = (player.getClassId() == ClassId.ELVEN_KNIGHT) ? "30327-02a.htm" : "30327-02.htm";
				}
				else if (player.getLevel() < 19)
				{
					htmltext = "30327-03.htm";
				}
				else if (st.hasQuestItems(ELVEN_KNIGHT_BROOCH))
				{
					htmltext = "30327-04.htm";
				}
				break;
			}
			case "30327-06.htm":
			{
				st.startQuest();
				break;
			}
			case "30317-02.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(SORIUS_LETTER, 1);
				st.giveItems(KLUTO_MEMO, 1);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
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
				htmltext = "30327-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case SORIUS:
					{
						if (cond == 1)
						{
							htmltext = (!st.hasQuestItems(TOPAZ_PIECE)) ? "30327-07.htm" : "30327-08.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30327-09.htm";
							st.setCond(3);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(SORIUS_LETTER, 1);
						}
						else if ((cond > 2) && (cond < 6))
						{
							htmltext = "30327-11.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30327-10.htm";
							st.takeItems(KLUTO_BOX, 1);
							st.takeItems(KLUTO_MEMO, 1);
							st.giveItems(ELVEN_KNIGHT_BROOCH, 1);
							st.rewardExpAndSp(3200, 2280);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					}
					case KLUTO:
					{
						if (cond == 3)
						{
							htmltext = "30317-01.htm";
						}
						else if (cond == 4)
						{
							htmltext = (!st.hasQuestItems(EMERALD_PIECE)) ? "30317-03.htm" : "30317-04.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30317-05.htm";
							st.setCond(6);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(EMERALD_PIECE, -1);
							st.takeItems(TOPAZ_PIECE, -1);
							st.giveItems(KLUTO_BOX, 1);
						}
						else if (cond == 6)
						{
							htmltext = "30317-06.htm";
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
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case 20035:
			case 20042:
			case 20045:
			case 20051:
			case 20054:
			case 20060:
			{
				if (st.isCond(1) && st.dropItems(TOPAZ_PIECE, 1, 20, 700000))
				{
					st.setCond(2);
				}
				break;
			}
			case 20782:
			{
				if (st.isCond(4) && st.dropItems(EMERALD_PIECE, 1, 20, 500000))
				{
					st.setCond(5);
				}
				break;
			}
		}
		
		return null;
	}
}