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
package quests.Q00101_SwordOfSolidarity;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Sword of Solidarity (101)
 * @author xban1x
 */
public class Q00101_SwordOfSolidarity extends Quest
{
	// NPCs
	private static final int ROIEN = 30008;
	private static final int ALTRAN = 30283;
	// Items
	private static final int BROKEN_SWORD_HANDLE = 739;
	private static final int BROKEN_BLADE_BOTTOM = 740;
	private static final int BROKEN_BLADE_TOP = 741;
	private static final int ALTRANS_NOTE = 742;
	private static final int ROIENS_LETTER = 796;
	private static final int DIRECTIONS_TO_RUINS = 937;
	// Monsters
	private static final int[] MONSTERS =
	{
		20361, // Tunath Orc Marksman
		20362, // Tunath Orc Warrior
	};
	// Rewards
	private static final int REWARDS = 49043; // Sword of Solidarity
	// Misc
	private static final int MIN_LEVEL = 10;
	private static final int MAX_LEVEL = 16;
	
	public Q00101_SwordOfSolidarity()
	{
		super(101);
		addStartNpc(ROIEN);
		addKillId(MONSTERS);
		addTalkId(ROIEN, ALTRAN);
		registerQuestItems(BROKEN_SWORD_HANDLE, BROKEN_BLADE_BOTTOM, BROKEN_BLADE_TOP, ALTRANS_NOTE, ROIENS_LETTER, DIRECTIONS_TO_RUINS);
		addCondMaxLevel(MAX_LEVEL, "30008-08.htm");
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
				case "30008-03.html":
				case "30008-09.html":
				{
					htmltext = event;
					break;
				}
				case "30008-04.htm":
				{
					qs.startQuest();
					giveItems(player, ROIENS_LETTER, 1);
					htmltext = event;
					break;
				}
				case "30283-02.html":
				{
					if (qs.isCond(1) && hasQuestItems(player, ROIENS_LETTER))
					{
						takeItems(player, ROIENS_LETTER, -1);
						giveItems(player, DIRECTIONS_TO_RUINS, 1);
						qs.setCond(2, true);
						htmltext = event;
					}
					break;
				}
				case "30283-07.html":
				{
					if (qs.isCond(5) && hasQuestItems(player, BROKEN_SWORD_HANDLE))
					{
						// Q00281_HeadForTheHills.giveNewbieReward(player);
						rewardItems(player, REWARDS, 1);
						qs.exitQuest(false, true);
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
		if ((qs != null) && qs.isCond(2) && (getRandom(5) == 0))
		{
			if (!hasQuestItems(killer, BROKEN_BLADE_TOP))
			{
				giveItems(killer, BROKEN_BLADE_TOP, 1);
				if (hasQuestItems(killer, BROKEN_BLADE_BOTTOM))
				{
					qs.setCond(3, true);
				}
				else
				{
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (!hasQuestItems(killer, BROKEN_BLADE_BOTTOM))
			{
				giveItems(killer, BROKEN_BLADE_BOTTOM, 1);
				if (hasQuestItems(killer, BROKEN_BLADE_TOP))
				{
					qs.setCond(3, true);
				}
				else
				{
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
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
			case ROIEN:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = (player.getRace() == Race.HUMAN) ? (player.getLevel() >= MIN_LEVEL) ? "30008-02.htm" : "30008-08.htm" : "30008-01.htm";
						break;
					}
					case State.STARTED:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								if (hasQuestItems(player, ROIENS_LETTER))
								{
									htmltext = "30008-05.html";
								}
								break;
							}
							case 2:
							{
								if (hasAtLeastOneQuestItem(player, BROKEN_BLADE_BOTTOM, BROKEN_BLADE_TOP))
								{
									htmltext = "30008-11.html";
								}
								else if (hasQuestItems(player, DIRECTIONS_TO_RUINS))
								{
									htmltext = "30008-10.html";
								}
								break;
							}
							case 3:
							{
								if (hasQuestItems(player, BROKEN_BLADE_BOTTOM, BROKEN_BLADE_TOP))
								{
									htmltext = "30008-12.html";
								}
								break;
							}
							case 4:
							{
								if (hasQuestItems(player, ALTRANS_NOTE))
								{
									takeItems(player, ALTRANS_NOTE, -1);
									giveItems(player, BROKEN_SWORD_HANDLE, 1);
									qs.setCond(5, true);
									htmltext = "30008-06.html";
								}
								break;
							}
							case 5:
							{
								if (hasQuestItems(player, BROKEN_SWORD_HANDLE))
								{
									htmltext = "30008-07.html";
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
						if (hasQuestItems(player, ROIENS_LETTER))
						{
							htmltext = "30283-01.html";
						}
						break;
					}
					case 2:
					{
						if (hasAtLeastOneQuestItem(player, BROKEN_BLADE_BOTTOM, BROKEN_BLADE_TOP))
						{
							htmltext = "30283-08.html";
						}
						else if (hasQuestItems(player, DIRECTIONS_TO_RUINS))
						{
							htmltext = "30283-03.html";
						}
						break;
					}
					case 3:
					{
						if (hasQuestItems(player, BROKEN_BLADE_BOTTOM, BROKEN_BLADE_TOP))
						{
							takeItems(player, -1, DIRECTIONS_TO_RUINS, BROKEN_BLADE_TOP, BROKEN_BLADE_BOTTOM);
							giveItems(player, ALTRANS_NOTE, 1);
							qs.setCond(4, true);
							htmltext = "30283-04.html";
						}
						break;
					}
					case 4:
					{
						if (hasQuestItems(player, ALTRANS_NOTE))
						{
							htmltext = "30283-05.html";
						}
						break;
					}
					case 5:
					{
						if (hasQuestItems(player, BROKEN_SWORD_HANDLE))
						{
							htmltext = "30283-06.html";
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