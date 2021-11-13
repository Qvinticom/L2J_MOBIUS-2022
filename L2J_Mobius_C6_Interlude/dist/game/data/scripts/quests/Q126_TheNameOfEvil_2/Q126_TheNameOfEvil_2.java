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
package quests.Q126_TheNameOfEvil_2;

import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q125_TheNameOfEvil_1.Q125_TheNameOfEvil_1;

public class Q126_TheNameOfEvil_2 extends Quest
{
	private static final int MUSHIKA = 32114;
	private static final int ASAMANAH = 32115;
	private static final int ULU_KAIMU = 32119;
	private static final int BALU_KAIMU = 32120;
	private static final int CHUTA_KAIMU = 32121;
	private static final int WARRIOR_GRAVE = 32122;
	private static final int SHILEN_STONE_STATUE = 32109;
	private static final int BONEPOWDER = 8783;
	private static final int EWA = 729;
	
	public Q126_TheNameOfEvil_2()
	{
		super(126, "The Name of Evil - 2");
		addStartNpc(ASAMANAH);
		addTalkId(ASAMANAH, MUSHIKA, ULU_KAIMU, BALU_KAIMU, CHUTA_KAIMU, WARRIOR_GRAVE, SHILEN_STONE_STATUE);
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
			case "32115-05.htm":
			{
				st.startQuest();
				break;
			}
			case "32115-10.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32119-02.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32119-09.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32119-11.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32120-07.htm":
			{
				st.setCond(6);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32120-09.htm":
			{
				st.setCond(7);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32120-11.htm":
			{
				st.setCond(8);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32121-07.htm":
			{
				st.setCond(9);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32121-10.htm":
			{
				st.setCond(10);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32121-15.htm":
			{
				st.setCond(11);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32122-03.htm":
			{
				st.setCond(12);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32122-15.htm":
			{
				st.setCond(13);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32122-18.htm":
			{
				st.setCond(14);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32122-87.htm":
			{
				st.giveItems(BONEPOWDER, 1);
				break;
			}
			case "32122-90.htm":
			{
				st.setCond(18);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32109-02.htm":
			{
				st.setCond(19);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32109-19.htm":
			{
				st.setCond(20);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BONEPOWDER, 1);
				break;
			}
			case "32115-21.htm":
			{
				st.setCond(21);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32115-28.htm":
			{
				st.setCond(22);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32114-08.htm":
			{
				st.setCond(23);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "32114-09.htm":
			{
				st.giveItems(EWA, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
				break;
			}
			case "DOOne":
			{
				htmltext = "32122-26.htm";
				if (st.getInt("DO") < 1)
				{
					st.set("DO", "1");
				}
				break;
			}
			case "MIOne":
			{
				htmltext = "32122-30.htm";
				if (st.getInt("MI") < 1)
				{
					st.set("MI", "1");
				}
				break;
			}
			case "FAOne":
			{
				htmltext = "32122-34.htm";
				if (st.getInt("FA") < 1)
				{
					st.set("FA", "1");
				}
				break;
			}
			case "SOLOne":
			{
				htmltext = "32122-38.htm";
				if (st.getInt("SOL") < 1)
				{
					st.set("SOL", "1");
				}
				break;
			}
			case "FA_2One":
			{
				if (st.getInt("FA_2") < 1)
				{
					st.set("FA_2", "1");
				}
				htmltext = getSongOne(st);
				break;
			}
			case "FATwo":
			{
				htmltext = "32122-47.htm";
				if (st.getInt("FA") < 1)
				{
					st.set("FA", "1");
				}
				break;
			}
			case "SOLTwo":
			{
				htmltext = "32122-51.htm";
				if (st.getInt("SOL") < 1)
				{
					st.set("SOL", "1");
				}
				break;
			}
			case "TITwo":
			{
				htmltext = "32122-55.htm";
				if (st.getInt("TI") < 1)
				{
					st.set("TI", "1");
				}
				break;
			}
			case "SOL_2Two":
			{
				htmltext = "32122-59.htm";
				if (st.getInt("SOL_2") < 1)
				{
					st.set("SOL_2", "1");
				}
				break;
			}
			case "FA_2Two":
			{
				if (st.getInt("FA_2") < 1)
				{
					st.set("FA_2", "1");
				}
				htmltext = getSongTwo(st);
				break;
			}
			case "SOLTri":
			{
				htmltext = "32122-68.htm";
				if (st.getInt("SOL") < 1)
				{
					st.set("SOL", "1");
				}
				break;
			}
			case "FATri":
			{
				htmltext = "32122-72.htm";
				if (st.getInt("FA") < 1)
				{
					st.set("FA", "1");
				}
				break;
			}
			case "MITri":
			{
				htmltext = "32122-76.htm";
				if (st.getInt("MI") < 1)
				{
					st.set("MI", "1");
				}
				break;
			}
			case "FA_2Tri":
			{
				htmltext = "32122-80.htm";
				if (st.getInt("FA_2") < 1)
				{
					st.set("FA_2", "1");
				}
				break;
			}
			case "MI_2Tri":
			{
				if (st.getInt("MI_2") < 1)
				{
					st.set("MI_2", "1");
				}
				htmltext = getSongTri(st);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
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
				if (player.getLevel() < 77)
				{
					htmltext = "32115-02.htm";
				}
				else
				{
					final QuestState st2 = player.getQuestState(Q125_TheNameOfEvil_1.class.getSimpleName());
					if ((st2 != null) && st2.isCompleted())
					{
						htmltext = "32115-01.htm";
					}
					else
					{
						htmltext = "32115-04.htm";
					}
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case ASAMANAH:
					{
						if (cond == 1)
						{
							htmltext = "32115-11.htm";
							st.setCond(2);
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if ((cond > 1) && (cond < 20))
						{
							htmltext = "32115-12.htm";
						}
						else if (cond == 20)
						{
							htmltext = "32115-13.htm";
						}
						else if (cond == 21)
						{
							htmltext = "32115-22.htm";
						}
						else if (cond == 22)
						{
							htmltext = "32115-29.htm";
						}
						break;
					}
					case ULU_KAIMU:
					{
						if (cond == 1)
						{
							htmltext = "32119-01a.htm";
						}
						else if (cond == 2)
						{
							htmltext = "32119-02.htm";
						}
						else if (cond == 3)
						{
							htmltext = "32119-08.htm";
						}
						else if (cond == 4)
						{
							htmltext = "32119-09.htm";
						}
						else if (cond > 4)
						{
							htmltext = "32119-12.htm";
						}
						break;
					}
					case BALU_KAIMU:
					{
						if (cond < 5)
						{
							htmltext = "32120-02.htm";
						}
						else if (cond == 5)
						{
							htmltext = "32120-01.htm";
						}
						else if (cond == 6)
						{
							htmltext = "32120-03.htm";
						}
						else if (cond == 7)
						{
							htmltext = "32120-08.htm";
						}
						else if (cond > 7)
						{
							htmltext = "32120-12.htm";
						}
						break;
					}
					case CHUTA_KAIMU:
					{
						if (cond < 8)
						{
							htmltext = "32121-02.htm";
						}
						else if (cond == 8)
						{
							htmltext = "32121-01.htm";
						}
						else if (cond == 9)
						{
							htmltext = "32121-03.htm";
						}
						else if (cond == 10)
						{
							htmltext = "32121-10.htm";
						}
						else if (cond > 10)
						{
							htmltext = "32121-16.htm";
						}
						break;
					}
					case WARRIOR_GRAVE:
					{
						if (cond < 11)
						{
							htmltext = "32122-02.htm";
						}
						else if (cond == 11)
						{
							htmltext = "32122-01.htm";
						}
						else if (cond == 12)
						{
							htmltext = "32122-15.htm";
						}
						else if (cond == 13)
						{
							htmltext = "32122-18.htm";
						}
						else if (cond == 14)
						{
							htmltext = "32122-24.htm";
						}
						else if (cond == 15)
						{
							htmltext = "32122-45.htm";
						}
						else if (cond == 16)
						{
							htmltext = "32122-66.htm";
						}
						else if (cond == 17)
						{
							htmltext = "32122-84.htm";
						}
						else if (cond == 18)
						{
							htmltext = "32122-91.htm";
						}
						break;
					}
					case SHILEN_STONE_STATUE:
					{
						if (cond < 18)
						{
							htmltext = "32109-03.htm";
						}
						else if (cond == 18)
						{
							htmltext = "32109-02.htm";
						}
						else if (cond == 19)
						{
							htmltext = "32109-05.htm";
						}
						else if (cond > 19)
						{
							htmltext = "32109-04.htm";
						}
						break;
					}
					case MUSHIKA:
					{
						if (cond < 22)
						{
							htmltext = "32114-02.htm";
						}
						else if (cond == 22)
						{
							htmltext = "32114-01.htm";
						}
						else if (cond == 23)
						{
							htmltext = "32114-04.htm";
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
	
	private static String getSongOne(QuestState st)
	{
		String htmltext = "32122-24.htm";
		if (st.isCond(14) && (st.getInt("DO") > 0) && (st.getInt("MI") > 0) && (st.getInt("FA") > 0) && (st.getInt("SOL") > 0) && (st.getInt("FA_2") > 0))
		{
			htmltext = "32122-42.htm";
			st.setCond(15);
			st.playSound(QuestState.SOUND_MIDDLE);
			st.unset("DO");
			st.unset("MI");
			st.unset("FA");
			st.unset("SOL");
			st.unset("FA_2");
		}
		return htmltext;
	}
	
	private static String getSongTwo(QuestState st)
	{
		String htmltext = "32122-45.htm";
		if (st.isCond(15) && (st.getInt("FA") > 0) && (st.getInt("SOL") > 0) && (st.getInt("TI") > 0) && (st.getInt("SOL_2") > 0) && (st.getInt("FA_2") > 0))
		{
			htmltext = "32122-63.htm";
			st.setCond(16);
			st.playSound(QuestState.SOUND_MIDDLE);
			st.unset("FA");
			st.unset("SOL");
			st.unset("TI");
			st.unset("SOL_2");
			st.unset("FA3_2");
		}
		return htmltext;
	}
	
	private static String getSongTri(QuestState st)
	{
		String htmltext = "32122-66.htm";
		if (st.isCond(16) && (st.getInt("SOL") > 0) && (st.getInt("FA") > 0) && (st.getInt("MI") > 0) && (st.getInt("FA_2") > 0) && (st.getInt("MI_2") > 0))
		{
			htmltext = "32122-84.htm";
			st.setCond(17);
			st.playSound(QuestState.SOUND_MIDDLE);
			st.unset("SOL");
			st.unset("FA");
			st.unset("MI");
			st.unset("FA_2");
			st.unset("MI_2");
		}
		return htmltext;
	}
}