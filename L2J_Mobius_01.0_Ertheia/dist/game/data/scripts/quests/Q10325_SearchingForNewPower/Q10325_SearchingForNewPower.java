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
package quests.Q10325_SearchingForNewPower;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10324_FindingMagisterGallint.Q10324_FindingMagisterGallint;

/**
 * Searching For New Power (10325)
 * @author Gladicek, Neanrakyr
 */
public class Q10325_SearchingForNewPower extends Quest
{
	// NPCs
	private static final int GALLINT = 32980;
	private static final int TALBOT = 32156;
	private static final int CIDNET = 32148;
	private static final int BLACK = 32161;
	private static final int HERTZ = 32151;
	private static final int KINCAID = 32159;
	private static final int XONIA = 32144;
	// Items
	private static final ItemHolder SPIRITSHOTS = new ItemHolder(2509, 1000);
	private static final ItemHolder SOULSHOTS = new ItemHolder(1835, 1000);
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10325_SearchingForNewPower()
	{
		super(10325);
		addStartNpc(GALLINT);
		addTalkId(GALLINT, TALBOT, CIDNET, BLACK, HERTZ, KINCAID, XONIA);
		addCondMaxLevel(MAX_LEVEL, "32980-12.html");
		addCondCompletedQuest(Q10324_FindingMagisterGallint.class.getSimpleName(), "32980-12.html");
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
		if (event.equals("check_race"))
		{
			switch (player.getRace())
			{
				case HUMAN:
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = "32980-06.html";
					qs.setCond(2);
					break;
				}
				case ELF:
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = "32980-07.html";
					qs.setCond(3);
					break;
				}
				case DARK_ELF:
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = "32980-08.html";
					qs.setCond(4);
					break;
				}
				case ORC:
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = "32980-09.html";
					qs.setCond(5);
					break;
				}
				case DWARF:
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = "32980-10.html";
					qs.setCond(6);
					break;
				}
				case KAMAEL:
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = "32980-11.html";
					qs.setCond(7);
					break;
				}
			}
		}
		else if (event.equals("32980-02.htm"))
		{
			htmltext = event;
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
				if (npc.getId() == GALLINT)
				{
					htmltext = "32980-01.htm";
					break;
				}
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case GALLINT:
					{
						if (qs.isCond(8))
						{
							if (player.isMageClass())
							{
								giveItems(player, SPIRITSHOTS);
							}
							else
							{
								giveItems(player, SOULSHOTS);
							}
							addExpAndSp(player, 4654, 5);
							giveAdena(player, 120, true);
							qs.exitQuest(false, true);
							htmltext = "32980-04.html";
							break;
						}
						else if (qs.isMemoState(1))
						{
							htmltext = "32980-03.html";
						}
						break;
					}
					case TALBOT:
					{
						if (player.getRace() == Race.HUMAN)
						{
							if ((qs.isCond(2)))
							{
								qs.setCond(8);
								htmltext = "32156-01.html";
								break;
							}
							htmltext = "32156-02.html";
							break;
						}
						htmltext = "32156-04.html";
						break;
					}
					case CIDNET:
					{
						if (player.getRace() == Race.ELF)
						{
							if ((qs.isCond(3)))
							{
								qs.setCond(8);
								htmltext = "32148-01.html";
								break;
							}
							htmltext = "32148-02.html";
							break;
						}
						htmltext = "32148-04.html";
						break;
					}
					case BLACK:
					{
						if (player.getRace() == Race.DARK_ELF)
						{
							if ((qs.isCond(4)))
							{
								qs.setCond(8);
								htmltext = "32161-01.html";
								break;
							}
							htmltext = "32161-02.html";
							break;
						}
						htmltext = "32161-04.html";
						break;
					}
					case HERTZ:
					{
						if (player.getRace() == Race.ORC)
						{
							if ((qs.isCond(5)))
							{
								qs.setCond(8);
								htmltext = "32151-01.html";
								break;
							}
							htmltext = "32151-02.html";
							break;
						}
						htmltext = "32151-04.html";
						break;
					}
					case KINCAID:
					{
						if (player.getRace() == Race.DWARF)
						{
							if ((qs.isCond(6)))
							{
								qs.setCond(8);
								htmltext = "32159-01.html";
								break;
							}
							htmltext = "32159-02.html";
							break;
						}
						htmltext = "32159-04.html";
						break;
					}
					case XONIA:
					{
						if (player.getRace() == Race.KAMAEL)
						{
							if ((qs.isCond(7)))
							{
								qs.setCond(8);
								htmltext = "32144-01.html";
								break;
							}
							htmltext = "32144-02.html";
							break;
						}
						htmltext = "32144-04.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				switch (npc.getId())
				{
					case GALLINT:
					{
						htmltext = "32980-05.html";
						break;
					}
					case TALBOT:
					case CIDNET:
					case BLACK:
					case HERTZ:
					case KINCAID:
					case XONIA:
					{
						htmltext = npc.getId() + "-03.html";
						break;
					}
				}
			}
		}
		return htmltext;
	}
}