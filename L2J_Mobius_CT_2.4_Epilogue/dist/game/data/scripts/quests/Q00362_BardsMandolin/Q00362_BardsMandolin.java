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
package quests.Q00362_BardsMandolin;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Bard's Mandolin (362)
 * @author Adry_85
 */
public class Q00362_BardsMandolin extends Quest
{
	// NPCs
	private static final int WOODROW = 30837;
	private static final int NANARIN = 30956;
	private static final int SWAN = 30957;
	private static final int GALION = 30958;
	// Items
	private static final int SWANS_FLUTE = 4316;
	private static final int SWANS_LETTER = 4317;
	private static final int THEME_OF_JOURNEY = 4410;
	// Misc
	private static final int MIN_LEVEL = 15;
	
	public Q00362_BardsMandolin()
	{
		super(362);
		addStartNpc(SWAN);
		addTalkId(SWAN, GALION, WOODROW, NANARIN);
		registerQuestItems(SWANS_FLUTE, SWANS_LETTER);
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
			case "30957-02.htm":
			{
				qs.startQuest();
				qs.setMemoState(1);
				htmltext = event;
				break;
			}
			case "30957-07.html":
			case "30957-08.html":
			{
				if (qs.isMemoState(5))
				{
					giveAdena(player, 10000, true);
					rewardItems(player, THEME_OF_JOURNEY, 1);
					qs.exitQuest(true, true);
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
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == SWAN)
				{
					htmltext = (player.getLevel() >= MIN_LEVEL) ? "30957-01.htm" : "30957-03.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case SWAN:
					{
						switch (qs.getMemoState())
						{
							case 1:
							case 2:
							{
								htmltext = "30957-04.html";
								break;
							}
							case 3:
							{
								qs.setCond(4, true);
								qs.setMemoState(4);
								giveItems(player, SWANS_LETTER, 1);
								htmltext = "30957-05.html";
								break;
							}
							case 4:
							{
								htmltext = "30957-05.html";
								break;
							}
							case 5:
							{
								htmltext = "30957-06.html";
								break;
							}
						}
						break;
					}
					case GALION:
					{
						if (qs.isMemoState(2))
						{
							qs.setMemoState(3);
							qs.setCond(3, true);
							giveItems(player, SWANS_FLUTE, 1);
							htmltext = "30958-01.html";
						}
						else if (qs.getMemoState() >= 3)
						{
							htmltext = "30958-02.html";
						}
						break;
					}
					case WOODROW:
					{
						if (qs.isMemoState(1))
						{
							qs.setMemoState(2);
							qs.setCond(2, true);
							htmltext = "30837-01.html";
						}
						else if (qs.isMemoState(2))
						{
							htmltext = "30837-02.html";
						}
						else if (qs.getMemoState() >= 3)
						{
							htmltext = "30837-03.html";
						}
						break;
					}
					case NANARIN:
					{
						if (qs.isMemoState(4) && hasQuestItems(player, SWANS_FLUTE, SWANS_LETTER))
						{
							qs.setMemoState(5);
							qs.setCond(5, true);
							takeItems(player, SWANS_FLUTE, -1);
							takeItems(player, SWANS_LETTER, -1);
							htmltext = "30956-01.html";
						}
						else if (qs.getMemoState() >= 5)
						{
							htmltext = "30956-02.html";
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
