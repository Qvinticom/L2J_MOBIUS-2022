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
package quests.Q213_TrialOfTheSeeker;

import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Q213_TrialOfTheSeeker extends Quest
{
	// NPCs
	private static final int TERRY = 30064;
	private static final int DUFNER = 30106;
	private static final int BRUNON = 30526;
	private static final int VIKTOR = 30684;
	private static final int MARINA = 30715;
	// Monsters
	private static final int NEER_GHOUL_BERSERKER = 20198;
	private static final int ANT_CAPTAIN = 20080;
	private static final int OL_MAHUM_CAPTAIN = 20211;
	private static final int TURAK_BUGBEAR_WARRIOR = 20249;
	private static final int TUREK_ORC_WARLORD = 20495;
	private static final int MEDUSA = 20158;
	private static final int ANT_WARRIOR_CAPTAIN = 20088;
	private static final int MARSH_STAKATO_DRONE = 20234;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
	// Items
	private static final int DUFNER_LETTER = 2647;
	private static final int TERRY_ORDER_1 = 2648;
	private static final int TERRY_ORDER_2 = 2649;
	private static final int TERRY_LETTER = 2650;
	private static final int VIKTOR_LETTER = 2651;
	private static final int HAWKEYE_LETTER = 2652;
	private static final int MYSTERIOUS_RUNESTONE = 2653;
	private static final int OL_MAHUM_RUNESTONE = 2654;
	private static final int TUREK_RUNESTONE = 2655;
	private static final int ANT_RUNESTONE = 2656;
	private static final int TURAK_BUGBEAR_RUNESTONE = 2657;
	private static final int TERRY_BOX = 2658;
	private static final int VIKTOR_REQUEST = 2659;
	private static final int MEDUSA_SCALES = 2660;
	private static final int SHILEN_RUNESTONE = 2661;
	private static final int ANALYSIS_REQUEST = 2662;
	private static final int MARINA_LETTER = 2663;
	private static final int EXPERIMENT_TOOLS = 2664;
	private static final int ANALYSIS_RESULT = 2665;
	private static final int TERRY_ORDER_3 = 2666;
	private static final int LIST_OF_HOST = 2667;
	private static final int ABYSS_RUNESTONE_1 = 2668;
	private static final int ABYSS_RUNESTONE_2 = 2669;
	private static final int ABYSS_RUNESTONE_3 = 2670;
	private static final int ABYSS_RUNESTONE_4 = 2671;
	private static final int TERRY_REPORT = 2672;
	// Rewards
	private static final int MARK_OF_SEEKER = 2673;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	public Q213_TrialOfTheSeeker()
	{
		super(213, "Trial of the Seeker");
		registerQuestItems(DUFNER_LETTER, TERRY_ORDER_1, TERRY_ORDER_2, TERRY_LETTER, VIKTOR_LETTER, HAWKEYE_LETTER, MYSTERIOUS_RUNESTONE, OL_MAHUM_RUNESTONE, TUREK_RUNESTONE, ANT_RUNESTONE, TURAK_BUGBEAR_RUNESTONE, TERRY_BOX, VIKTOR_REQUEST, MEDUSA_SCALES, SHILEN_RUNESTONE, ANALYSIS_REQUEST, MARINA_LETTER, EXPERIMENT_TOOLS, ANALYSIS_RESULT, TERRY_ORDER_3, LIST_OF_HOST, ABYSS_RUNESTONE_1, ABYSS_RUNESTONE_2, ABYSS_RUNESTONE_3, ABYSS_RUNESTONE_4, TERRY_REPORT);
		addStartNpc(DUFNER);
		addTalkId(TERRY, DUFNER, BRUNON, VIKTOR, MARINA);
		addKillId(NEER_GHOUL_BERSERKER, ANT_CAPTAIN, OL_MAHUM_CAPTAIN, TURAK_BUGBEAR_WARRIOR, TUREK_ORC_WARLORD, ANT_WARRIOR_CAPTAIN, MARSH_STAKATO_DRONE, BREKA_ORC_OVERLORD, LETO_LIZARDMAN_WARRIOR, MEDUSA);
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
			case "30106-05.htm":
			{
				st.startQuest();
				st.giveItems(DUFNER_LETTER, 1);
				if (!player.getVariables().getBoolean("secondClassChange35", false))
				{
					htmltext = "30106-05a.htm";
					st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35.get(player.getClassId().getId()));
					player.getVariables().set("secondClassChange35", true);
				}
				break;
			}
			case "30064-03.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(DUFNER_LETTER, 1);
				st.giveItems(TERRY_ORDER_1, 1);
				break;
			}
			case "30064-06.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(MYSTERIOUS_RUNESTONE, 1);
				st.takeItems(TERRY_ORDER_1, 1);
				st.giveItems(TERRY_ORDER_2, 1);
				break;
			}
			case "30064-10.htm":
			{
				st.setCond(6);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(ANT_RUNESTONE, 1);
				st.takeItems(OL_MAHUM_RUNESTONE, 1);
				st.takeItems(TURAK_BUGBEAR_RUNESTONE, 1);
				st.takeItems(TUREK_RUNESTONE, 1);
				st.takeItems(TERRY_ORDER_2, 1);
				st.giveItems(TERRY_BOX, 1);
				st.giveItems(TERRY_LETTER, 1);
				break;
			}
			case "30064-18.htm":
			{
				if (player.getLevel() < 36)
				{
					htmltext = "30064-17.htm";
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(ANALYSIS_RESULT, 1);
					st.giveItems(TERRY_ORDER_3, 1);
				}
				else
				{
					st.setCond(16);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(ANALYSIS_RESULT, 1);
					st.giveItems(LIST_OF_HOST, 1);
				}
				break;
			}
			case "30684-05.htm":
			{
				st.setCond(7);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(TERRY_LETTER, 1);
				st.giveItems(VIKTOR_LETTER, 1);
				break;
			}
			case "30684-11.htm":
			{
				st.setCond(9);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(TERRY_LETTER, 1);
				st.takeItems(TERRY_BOX, 1);
				st.takeItems(HAWKEYE_LETTER, 1);
				st.takeItems(VIKTOR_LETTER, 1);
				st.giveItems(VIKTOR_REQUEST, 1);
				break;
			}
			case "30684-15.htm":
			{
				st.setCond(11);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(VIKTOR_REQUEST, 1);
				st.takeItems(MEDUSA_SCALES, 10);
				st.giveItems(ANALYSIS_REQUEST, 1);
				st.giveItems(SHILEN_RUNESTONE, 1);
				break;
			}
			case "30715-02.htm":
			{
				st.setCond(12);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(SHILEN_RUNESTONE, 1);
				st.takeItems(ANALYSIS_REQUEST, 1);
				st.giveItems(MARINA_LETTER, 1);
				break;
			}
			case "30715-05.htm":
			{
				st.setCond(14);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(EXPERIMENT_TOOLS, 1);
				st.giveItems(ANALYSIS_RESULT, 1);
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
				if ((player.getClassId() == ClassId.ROGUE) || (player.getClassId() == ClassId.ELVEN_SCOUT) || (player.getClassId() == ClassId.ASSASSIN))
				{
					htmltext = (player.getLevel() < 35) ? "30106-02.htm" : "30106-03.htm";
				}
				else
				{
					htmltext = "30106-00.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case DUFNER:
					{
						if (cond == 1)
						{
							htmltext = "30106-06.htm";
						}
						else if (cond > 1)
						{
							if (!st.hasQuestItems(TERRY_REPORT))
							{
								htmltext = "30106-07.htm";
							}
							else
							{
								htmltext = "30106-08.htm";
								st.takeItems(TERRY_REPORT, 1);
								st.giveItems(MARK_OF_SEEKER, 1);
								st.rewardExpAndSp(72126, 11000);
								player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(false);
							}
						}
						break;
					}
					case TERRY:
					{
						if (cond == 1)
						{
							htmltext = "30064-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30064-04.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30064-05.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30064-08.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30064-09.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30064-11.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30064-12.htm";
							st.setCond(8);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(VIKTOR_LETTER, 1);
							st.giveItems(HAWKEYE_LETTER, 1);
						}
						else if (cond == 8)
						{
							htmltext = "30064-13.htm";
						}
						else if ((cond > 8) && (cond < 14))
						{
							htmltext = "30064-14.htm";
						}
						else if (cond == 14)
						{
							if (!st.hasQuestItems(TERRY_ORDER_3))
							{
								htmltext = "30064-15.htm";
							}
							else if (player.getLevel() < 36)
							{
								htmltext = "30064-20.htm";
							}
							else
							{
								htmltext = "30064-21.htm";
								st.setCond(15);
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(TERRY_ORDER_3, 1);
								st.giveItems(LIST_OF_HOST, 1);
							}
						}
						else if ((cond == 15) || (cond == 16))
						{
							htmltext = "30064-22.htm";
						}
						else if (cond == 17)
						{
							if (!st.hasQuestItems(TERRY_REPORT))
							{
								htmltext = "30064-23.htm";
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(LIST_OF_HOST, 1);
								st.takeItems(ABYSS_RUNESTONE_1, 1);
								st.takeItems(ABYSS_RUNESTONE_2, 1);
								st.takeItems(ABYSS_RUNESTONE_3, 1);
								st.takeItems(ABYSS_RUNESTONE_4, 1);
								st.giveItems(TERRY_REPORT, 1);
							}
							else
							{
								htmltext = "30064-24.htm";
							}
						}
						break;
					}
					case VIKTOR:
					{
						if (cond == 6)
						{
							htmltext = "30684-01.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30684-05.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30684-12.htm";
						}
						else if (cond == 9)
						{
							htmltext = "30684-13.htm";
						}
						else if (cond == 10)
						{
							htmltext = "30684-14.htm";
						}
						else if (cond == 11)
						{
							htmltext = "30684-16.htm";
						}
						else if (cond > 11)
						{
							htmltext = "30684-17.htm";
						}
						break;
					}
					case MARINA:
					{
						if (cond == 11)
						{
							htmltext = "30715-01.htm";
						}
						else if (cond == 12)
						{
							htmltext = "30715-03.htm";
						}
						else if (cond == 13)
						{
							htmltext = "30715-04.htm";
						}
						else if (st.hasQuestItems(ANALYSIS_RESULT))
						{
							htmltext = "30715-06.htm";
						}
						break;
					}
					case BRUNON:
					{
						if (cond == 12)
						{
							htmltext = "30526-01.htm";
							st.setCond(13);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MARINA_LETTER, 1);
							st.giveItems(EXPERIMENT_TOOLS, 1);
						}
						else if (cond == 13)
						{
							htmltext = "30526-02.htm";
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
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case NEER_GHOUL_BERSERKER:
			{
				if (st.isCond(2) && st.dropItems(MYSTERIOUS_RUNESTONE, 1, 1, 100000))
				{
					st.setCond(3);
				}
				break;
			}
			case ANT_CAPTAIN:
			{
				if (st.isCond(4) && st.dropItems(ANT_RUNESTONE, 1, 1, 250000) && st.hasQuestItems(OL_MAHUM_RUNESTONE, TURAK_BUGBEAR_RUNESTONE, TUREK_RUNESTONE))
				{
					st.setCond(5);
				}
				break;
			}
			case OL_MAHUM_CAPTAIN:
			{
				if (st.isCond(4) && st.dropItems(OL_MAHUM_RUNESTONE, 1, 1, 250000) && st.hasQuestItems(ANT_RUNESTONE, TURAK_BUGBEAR_RUNESTONE, TUREK_RUNESTONE))
				{
					st.setCond(5);
				}
				break;
			}
			case TURAK_BUGBEAR_WARRIOR:
			{
				if (st.isCond(4) && st.dropItems(TURAK_BUGBEAR_RUNESTONE, 1, 1, 250000) && st.hasQuestItems(ANT_RUNESTONE, OL_MAHUM_RUNESTONE, TUREK_RUNESTONE))
				{
					st.setCond(5);
				}
				break;
			}
			case TUREK_ORC_WARLORD:
			{
				if (st.isCond(4) && st.dropItems(TUREK_RUNESTONE, 1, 1, 250000) && st.hasQuestItems(ANT_RUNESTONE, OL_MAHUM_RUNESTONE, TURAK_BUGBEAR_RUNESTONE))
				{
					st.setCond(5);
				}
				break;
			}
			case MEDUSA:
			{
				if (st.isCond(9) && st.dropItems(MEDUSA_SCALES, 1, 10, 300000))
				{
					st.setCond(10);
				}
				break;
			}
			case MARSH_STAKATO_DRONE:
			{
				if ((st.isCond(15) || st.isCond(16)) && st.dropItems(ABYSS_RUNESTONE_1, 1, 1, 250000) && st.hasQuestItems(ABYSS_RUNESTONE_2, ABYSS_RUNESTONE_3, ABYSS_RUNESTONE_4))
				{
					st.setCond(17);
				}
				break;
			}
			case BREKA_ORC_OVERLORD:
			{
				if ((st.isCond(15) || st.isCond(16)) && st.dropItems(ABYSS_RUNESTONE_2, 1, 1, 250000) && st.hasQuestItems(ABYSS_RUNESTONE_1, ABYSS_RUNESTONE_3, ABYSS_RUNESTONE_4))
				{
					st.setCond(17);
				}
				break;
			}
			case ANT_WARRIOR_CAPTAIN:
			{
				if ((st.isCond(15) || st.isCond(16)) && st.dropItems(ABYSS_RUNESTONE_3, 1, 1, 250000) && st.hasQuestItems(ABYSS_RUNESTONE_1, ABYSS_RUNESTONE_2, ABYSS_RUNESTONE_4))
				{
					st.setCond(17);
				}
				break;
			}
			case LETO_LIZARDMAN_WARRIOR:
			{
				if ((st.isCond(15) || st.isCond(16)) && st.dropItems(ABYSS_RUNESTONE_4, 1, 1, 250000) && st.hasQuestItems(ABYSS_RUNESTONE_1, ABYSS_RUNESTONE_2, ABYSS_RUNESTONE_3))
				{
					st.setCond(17);
				}
				break;
			}
		}
		
		return null;
	}
}