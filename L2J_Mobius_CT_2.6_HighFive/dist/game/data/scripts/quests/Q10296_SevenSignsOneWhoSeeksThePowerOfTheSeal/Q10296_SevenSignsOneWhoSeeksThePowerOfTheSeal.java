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
package quests.Q10296_SevenSignsOneWhoSeeksThePowerOfTheSeal;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;

import quests.Q10295_SevenSignsSolinasTomb.Q10295_SevenSignsSolinasTomb;

/**
 * Seven Signs, One Who Seeks the Power of the Seal (10296)
 * @author Adry_85
 * @since 2.6.0.0
 */
public class Q10296_SevenSignsOneWhoSeeksThePowerOfTheSeal extends Quest
{
	// NPCs
	private static final int HARDIN = 30832;
	private static final int WOOD = 32593;
	private static final int FRANZ = 32597;
	private static final int ELCADIA = 32784;
	private static final int ELCADIA_2 = 32787;
	private static final int ERISS_EVIL_THOUGHTS = 32792;
	private static final int ODD_GLOBE = 32815;
	// Reward
	private static final int CERTIFICATE_OF_DAWN = 17265;
	// Misc
	private static final int MIN_LEVEL = 81;
	
	public Q10296_SevenSignsOneWhoSeeksThePowerOfTheSeal()
	{
		super(10296);
		addStartNpc(ERISS_EVIL_THOUGHTS, ODD_GLOBE);
		addTalkId(ERISS_EVIL_THOUGHTS, ODD_GLOBE, HARDIN, WOOD, FRANZ, ELCADIA, ELCADIA_2);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32792-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32792-03.htm":
			{
				qs.startQuest();
				qs.setMemoState(1);
				htmltext = event;
				break;
			}
			case "30832-03.html":
			{
				if (qs.isMemoState(4))
				{
					htmltext = event;
				}
				break;
			}
			case "30832-04.html":
			{
				if (qs.isMemoState(4))
				{
					qs.setMemoState(5);
					qs.setCond(5, true);
					htmltext = event;
				}
				break;
			}
			case "32593-03.html":
			{
				if (qs.isMemoState(5))
				{
					htmltext = event;
				}
				break;
			}
			case "32597-02.html":
			{
				if (qs.isMemoState(5))
				{
					htmltext = event;
				}
				break;
			}
			case "32597-03.html":
			{
				if (qs.isMemoState(5))
				{
					if (player.isSubClassActive())
					{
						htmltext = event;
					}
					else
					{
						addExpAndSp(player, 125000000, 12500000);
						giveItems(player, CERTIFICATE_OF_DAWN, 1);
						qs.exitQuest(false, true);
						htmltext = "32597-04.html";
					}
				}
				break;
			}
			case "32784-02.html":
			{
				if (qs.isMemoState(3))
				{
					htmltext = event;
				}
				break;
			}
			case "32784-03.html":
			{
				if (qs.isMemoState(3))
				{
					qs.setMemoState(4);
					qs.setCond(4, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCompleted())
		{
			if (npc.getId() == ERISS_EVIL_THOUGHTS)
			{
				htmltext = "32792-04.html";
			}
		}
		else if (qs.isCreated())
		{
			final QuestState st1 = player.getQuestState(Q10295_SevenSignsSolinasTomb.class.getSimpleName());
			if ((st1 != null) && (st1.isCompleted()))
			{
				if ((npc.getId() == ERISS_EVIL_THOUGHTS) && (player.getLevel() >= MIN_LEVEL))
				{
					htmltext = "32792-01.htm";
				}
				else
				{
					htmltext = "32815-01.html";
				}
			}
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case ERISS_EVIL_THOUGHTS:
				{
					if (qs.isMemoState(1))
					{
						qs.setMemoState(2);
						qs.setCond(2, true);
						htmltext = "32792-05.html";
					}
					else if (qs.isMemoState(2))
					{
						htmltext = "32792-06.html";
					}
					break;
				}
				case ODD_GLOBE:
				{
					final int memoState = qs.getMemoState();
					if ((memoState > 0) && (memoState <= 2))
					{
						htmltext = "32815-01.html";
					}
					else if (memoState > 2)
					{
						htmltext = "32815-02.html";
					}
					break;
				}
				case HARDIN:
				{
					final int memoState = qs.getMemoState();
					if (memoState < 4)
					{
						htmltext = "30832-01.html";
					}
					else if (memoState == 4)
					{
						htmltext = "30832-02.html";
					}
					else if (memoState > 4)
					{
						htmltext = "30832-04.html";
					}
					break;
				}
				case WOOD:
				{
					final int memoState = qs.getMemoState();
					if (memoState < 5)
					{
						htmltext = "32593-01.html";
					}
					else if (memoState == 5)
					{
						htmltext = "32593-02.html";
					}
					else if (memoState > 5)
					{
						htmltext = "32593-04.html";
					}
					break;
				}
				case FRANZ:
				{
					if (qs.isMemoState(5))
					{
						htmltext = "32597-01.html";
					}
					break;
				}
				case ELCADIA:
				{
					final int memoState = qs.getMemoState();
					if (memoState == 3)
					{
						htmltext = "32784-01.html";
					}
					else if (memoState > 3)
					{
						htmltext = "32784-04.html";
					}
					break;
				}
				case ELCADIA_2:
				{
					final int memoState = qs.getMemoState();
					if (memoState < 1)
					{
						htmltext = "32787-01.html";
					}
					else if (memoState < 2)
					{
						htmltext = "32787-02.html";
					}
					else if (memoState == 2)
					{
						htmltext = "32787-03.html";
					}
					else
					{
						qs.setCond(3, true);
						htmltext = "32787-04.html";
					}
					break;
				}
			}
		}
		return htmltext;
	}
}
