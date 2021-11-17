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
package quests.Q108_JumbleTumbleDiamondFuss;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Q108_JumbleTumbleDiamondFuss extends Quest
{
	// NPCs
	private static final int GOUPH = 30523;
	private static final int REEP = 30516;
	private static final int MURDOC = 30521;
	private static final int AIRY = 30522;
	private static final int BRUNON = 30526;
	private static final int MARON = 30529;
	private static final int TOROCCO = 30555;
	// Monsters
	private static final int GOBLIN_BRIGAND_LEADER = 20323;
	private static final int GOBLIN_BRIGAND_LIEUTENANT = 20324;
	private static final int BLADE_BAT = 20480;
	// Items
	private static final int GOUPH_CONTRACT = 1559;
	private static final int REEP_CONTRACT = 1560;
	private static final int ELVEN_WINE = 1561;
	private static final int BRUNON_DICE = 1562;
	private static final int BRUNON_CONTRACT = 1563;
	private static final int AQUAMARINE = 1564;
	private static final int CHRYSOBERYL = 1565;
	private static final int GEM_BOX = 1566;
	private static final int COAL_PIECE = 1567;
	private static final int BRUNON_LETTER = 1568;
	private static final int BERRY_TART = 1569;
	private static final int BAT_DIAGRAM = 1570;
	private static final int STAR_DIAMOND = 1571;
	// Rewards
	private static final int SILVERSMITH_HAMMER = 1511;
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	private static final int ECHO_BATTLE = 4412;
	private static final int ECHO_LOVE = 4413;
	private static final int ECHO_SOLITUDE = 4414;
	private static final int ECHO_FEAST = 4415;
	private static final int ECHO_CELEBRATION = 4416;
	private static final int LESSER_HEALING_POTION = 1060;
	// @formatter:off
	private static final int[][] LEADER_DROPLIST =
	{
		{AQUAMARINE, 1, 10, 800000},
		{CHRYSOBERYL, 1, 10, 800000}
	};
	private static final int[][] LIEUTENANT_DROPLIST =
	{
		{AQUAMARINE, 1, 10, 600000},
		{CHRYSOBERYL, 1, 10, 600000}
	};
	// @formatter:on
	
	public Q108_JumbleTumbleDiamondFuss()
	{
		super(108, "Jumble, Tumble, Diamond Fuss");
		registerQuestItems(GOUPH_CONTRACT, REEP_CONTRACT, ELVEN_WINE, BRUNON_DICE, BRUNON_CONTRACT, AQUAMARINE, CHRYSOBERYL, GEM_BOX, COAL_PIECE, BRUNON_LETTER, BERRY_TART, BAT_DIAGRAM, STAR_DIAMOND);
		addStartNpc(GOUPH);
		addTalkId(GOUPH, REEP, MURDOC, AIRY, BRUNON, MARON, TOROCCO);
		addKillId(GOBLIN_BRIGAND_LEADER, GOBLIN_BRIGAND_LIEUTENANT, BLADE_BAT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		final String htmltext = event;
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30523-03.htm":
			{
				st.startQuest();
				st.giveItems(GOUPH_CONTRACT, 1);
				break;
			}
			case "30555-02.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(REEP_CONTRACT, 1);
				st.giveItems(ELVEN_WINE, 1);
				break;
			}
			case "30526-02.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BRUNON_DICE, 1);
				st.giveItems(BRUNON_CONTRACT, 1);
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
				if (player.getRace() != Race.DWARF)
				{
					htmltext = "30523-00.htm";
				}
				else if (player.getLevel() < 10)
				{
					htmltext = "30523-01.htm";
				}
				else
				{
					htmltext = "30523-02.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case GOUPH:
					{
						if (cond == 1)
						{
							htmltext = "30523-04.htm";
						}
						else if ((cond > 1) && (cond < 7))
						{
							htmltext = "30523-05.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30523-06.htm";
							st.setCond(8);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GEM_BOX, 1);
							st.giveItems(COAL_PIECE, 1);
						}
						else if ((cond > 7) && (cond < 12))
						{
							htmltext = "30523-07.htm";
						}
						else if (cond == 12)
						{
							htmltext = "30523-08.htm";
							st.takeItems(STAR_DIAMOND, -1);
							st.giveItems(SILVERSMITH_HAMMER, 1);
							st.giveItems(LESSER_HEALING_POTION, 100);
							
							if (player.isNewbie())
							{
								st.showQuestionMark(26);
								if (player.isMageClass())
								{
									st.playTutorialVoice("tutorial_voice_027");
									st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000);
								}
								else
								{
									st.playTutorialVoice("tutorial_voice_026");
									st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
								}
							}
							
							st.giveItems(ECHO_BATTLE, 10);
							st.giveItems(ECHO_LOVE, 10);
							st.giveItems(ECHO_SOLITUDE, 10);
							st.giveItems(ECHO_FEAST, 10);
							st.giveItems(ECHO_CELEBRATION, 10);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					}
					case REEP:
					{
						if (cond == 1)
						{
							htmltext = "30516-01.htm";
							st.setCond(2);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GOUPH_CONTRACT, 1);
							st.giveItems(REEP_CONTRACT, 1);
						}
						else if (cond > 1)
						{
							htmltext = "30516-02.htm";
						}
						break;
					}
					case TOROCCO:
					{
						if (cond == 2)
						{
							htmltext = "30555-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30555-03.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30555-04.htm";
						}
						else if (cond > 7)
						{
							htmltext = "30555-05.htm";
						}
						break;
					}
					case MARON:
					{
						if (cond == 3)
						{
							htmltext = "30529-01.htm";
							st.setCond(4);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ELVEN_WINE, 1);
							st.giveItems(BRUNON_DICE, 1);
						}
						else if (cond == 4)
						{
							htmltext = "30529-02.htm";
						}
						else if (cond > 4)
						{
							htmltext = "30529-03.htm";
						}
						break;
					}
					case BRUNON:
					{
						if (cond == 4)
						{
							htmltext = "30526-01.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30526-03.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30526-04.htm";
							st.setCond(7);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BRUNON_CONTRACT, 1);
							st.takeItems(AQUAMARINE, -1);
							st.takeItems(CHRYSOBERYL, -1);
							st.giveItems(GEM_BOX, 1);
						}
						else if (cond == 7)
						{
							htmltext = "30526-05.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30526-06.htm";
							st.setCond(9);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(COAL_PIECE, 1);
							st.giveItems(BRUNON_LETTER, 1);
						}
						else if (cond == 9)
						{
							htmltext = "30526-07.htm";
						}
						else if (cond > 9)
						{
							htmltext = "30526-08.htm";
						}
						break;
					}
					case MURDOC:
					{
						if (cond == 9)
						{
							htmltext = "30521-01.htm";
							st.setCond(10);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BRUNON_LETTER, 1);
							st.giveItems(BERRY_TART, 1);
						}
						else if (cond == 10)
						{
							htmltext = "30521-02.htm";
						}
						else if (cond > 10)
						{
							htmltext = "30521-03.htm";
						}
						break;
					}
					case AIRY:
					{
						if (cond == 10)
						{
							htmltext = "30522-01.htm";
							st.setCond(11);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BERRY_TART, 1);
							st.giveItems(BAT_DIAGRAM, 1);
						}
						else if (cond == 11)
						{
							htmltext = (Rnd.nextBoolean()) ? "30522-02.htm" : "30522-04.htm";
						}
						else if (cond == 12)
						{
							htmltext = "30522-03.htm";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
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
			case GOBLIN_BRIGAND_LEADER:
			{
				if (st.isCond(5) && st.dropMultipleItems(LEADER_DROPLIST))
				{
					st.setCond(6);
				}
				break;
			}
			case GOBLIN_BRIGAND_LIEUTENANT:
			{
				if (st.isCond(5) && st.dropMultipleItems(LIEUTENANT_DROPLIST))
				{
					st.setCond(6);
				}
				break;
			}
			case BLADE_BAT:
			{
				if (st.isCond(11) && st.dropItems(STAR_DIAMOND, 1, 1, 200000))
				{
					st.takeItems(BAT_DIAGRAM, 1);
					st.setCond(12);
				}
				break;
			}
		}
		return null;
	}
}