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
package quests.Q386_StolenDignity;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.commons.util.StringUtil;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Adapted for StayOnly from python.
 */
public class Q386_StolenDignity extends Quest
{
	private static final int ROMP = 30843;
	private static final int STOLEN_INFERNIUM_ORE = 6363;
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(20970, 208000);
		CHANCES.put(20971, 299000);
		CHANCES.put(20958, 170000);
		CHANCES.put(20960, 149000);
		CHANCES.put(20963, 199000);
		CHANCES.put(20670, 202000);
		CHANCES.put(21114, 352000);
		CHANCES.put(20959, 273000);
		CHANCES.put(21020, 478000);
		CHANCES.put(21258, 487000);
		CHANCES.put(21003, 173000);
		CHANCES.put(20969, 205000);
		CHANCES.put(21108, 245000);
		CHANCES.put(21005, 211000);
		CHANCES.put(21116, 487000);
		CHANCES.put(21113, 370000);
		CHANCES.put(20954, 184000);
		CHANCES.put(20671, 211000);
		CHANCES.put(21110, 260000);
		CHANCES.put(20967, 257000);
		CHANCES.put(20956, 216000);
		CHANCES.put(21021, 234000);
		CHANCES.put(21259, 487000);
		CHANCES.put(20974, 440000);
		CHANCES.put(20975, 390000);
		CHANCES.put(21001, 214000);
	}
	private static final int[] REWARDS = new int[]
	{
		5529,
		5532,
		5533,
		5534,
		5535,
		5536,
		5537,
		5538,
		5539,
		5541,
		5542,
		5543,
		5544,
		5545,
		5546,
		5547,
		5548,
		8331,
		8341,
		8342,
		8349,
		8346
	};
	public static final int[][] MATRICE_3X3_LINES = new int[][]
	{
		// @formatter:off
		{1, 2, 3},
		{4, 5, 6},
		{7, 8, 9},
		{1, 4, 7},
		{2, 5, 8},
		{3, 6, 9},
		{1, 5, 9},
		{3, 5, 7}
		// @formatter:on
	};
	
	public Q386_StolenDignity()
	{
		super(386, "Stolen Dignity");
		addStartNpc(ROMP);
		addTalkId(ROMP);
		addKillId(CHANCES.keySet());
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState("Q386_StolenDignity");
		if (st == null)
		{
			return htmltext;
		}
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getLevel() < 58 ? "30843-04.htm" : "30843-01.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = st.getQuestItemsCount(6363) < 100 ? "30843-06.htm" : "30843-07.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState("Q386_StolenDignity");
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("30843-05.htm"))
		{
			st.startQuest();
			st.set("state", "1");
		}
		else if (event.equals("30843-08.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equals("30843-12.htm"))
		{
			if (st.getQuestItemsCount(6363) < 100)
			{
				htmltext = "30843-11.htm";
			}
			else
			{
				st.set("board", StringUtil.scrambleString("123456789"));
				st.takeItems(6363, 100);
			}
		}
		else if (event.startsWith("select_1-"))
		{
			st.set("playerArray", event.substring(9));
			htmltext = fillBoard(st, getHtmlText("30843-13.htm"));
		}
		else
		{
			String number;
			String playerArray;
			if (event.startsWith("select_2-"))
			{
				number = event.substring(9);
				playerArray = st.get("playerArray");
				if (playerArray.contains(number))
				{
					htmltext = fillBoard(st, getHtmlText("30843-" + (13 + (2 * playerArray.length())) + ".htm"));
				}
				else
				{
					st.set("playerArray", playerArray.concat(number));
					htmltext = fillBoard(st, getHtmlText("30843-" + (12 + (2 * playerArray.length())) + ".htm"));
				}
			}
			else if (event.startsWith("select_3-"))
			{
				number = event.substring(9);
				playerArray = st.get("playerArray");
				if (playerArray.contains(number))
				{
					htmltext = fillBoard(st, getHtmlText("30843-25.htm"));
				}
				else
				{
					String playerChoice = playerArray.concat(number);
					String[] board = st.get("board").split("");
					int winningLines = 0;
					int[][] var11 = MATRICE_3X3_LINES;
					int var12 = var11.length;
					
					for (int var13 = 0; var13 < var12; ++var13)
					{
						int[] map = var11[var13];
						boolean won = true;
						int[] var16 = map;
						int var17 = map.length;
						
						for (int var18 = 0; var18 < var17; ++var18)
						{
							int index = var16[var18];
							won &= playerChoice.contains(board[index - 1]);
						}
						
						if (won)
						{
							++winningLines;
						}
					}
					
					if (winningLines == 3)
					{
						htmltext = getHtmlText("30843-22.htm");
						st.rewardItems(REWARDS[Rnd.get(REWARDS.length)], 4);
					}
					else if (winningLines == 0)
					{
						htmltext = getHtmlText("30843-24.htm");
						st.rewardItems(REWARDS[Rnd.get(REWARDS.length)], 10);
					}
					else
					{
						htmltext = getHtmlText("30843-23.htm");
					}
					
					for (int i = 1; i < 10; ++i)
					{
						htmltext = htmltext.replace("<?Cell" + i + "?>", board[i - 1]);
						htmltext = htmltext.replace("<?FontColor" + i + "?>", playerChoice.contains(board[i - 1]) ? "ff0000" : "ffffff");
					}
				}
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		partyMember.getQuestState(getName()).dropItems(STOLEN_INFERNIUM_ORE, 1, 0, CHANCES.get(npc.getNpcId()));
		return null;
	}
	
	private static final String fillBoard(QuestState st, String htmltext)
	{
		String result = htmltext;
		String playerArray = st.get("playerArray");
		String[] board = st.get("board").split("");
		for (int i = 1; i < 10; ++i)
		{
			result = result.replace("<?Cell" + i + "?>", playerArray.contains(board[i - 1]) ? board[i - 1] : "?");
		}
		return result;
	}
}
