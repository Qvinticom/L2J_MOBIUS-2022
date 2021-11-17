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
package quests.Q10875_ForReputation;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10873_ExaltedReachingAnotherLevel.Q10873_ExaltedReachingAnotherLevel;

/**
 * For Reputation (10875)
 * @URL https://l2wiki.com/For_Reputation https://www.youtube.com/watch?v=7i-M4U4qxaA
 * @author Mobius
 */
public class Q10875_ForReputation extends Quest
{
	// NPC
	private static final int KRENAHT = 34237;
	private static final int KEKROPUS = 34222;
	// Items
	private static final int BLACKBIRD_CLAN_CERTIFICATION = 47840;
	private static final int GIANT_TRACKERS_CERTIFICATION = 47841;
	// Rewards
	private static final int KEKROPUS_CERTIFICATE = 47831;
	private static final int SPELLBOOK_VITALITY_OF_THE_EXALTED = 47831;
	// Misc
	private static final int MIN_LEVEL = 103;
	
	public Q10875_ForReputation()
	{
		super(10875);
		addStartNpc(KRENAHT);
		addTalkId(KRENAHT, KEKROPUS);
		addCondMinLevel(MIN_LEVEL, "34237-00.html");
		addCondStartedQuest(Q10873_ExaltedReachingAnotherLevel.class.getSimpleName(), "34237-00.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "34237-02.htm":
			case "34237-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34237-04.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34237-07.html":
			{
				qs.setCond(2);
				htmltext = event;
				break;
			}
			case "34222-02.html":
			{
				if (qs.isCond(2))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						if (!hasQuestItems(player, BLACKBIRD_CLAN_CERTIFICATION, GIANT_TRACKERS_CERTIFICATION))
						{
							htmltext = "34222-00.html";
						}
						else
						{
							htmltext = event;
							giveItems(player, KEKROPUS_CERTIFICATE, 1);
							giveItems(player, SPELLBOOK_VITALITY_OF_THE_EXALTED, 1);
							qs.exitQuest(false, true);
						}
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
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
				htmltext = "34237-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case KRENAHT:
					{
						if (qs.isCond(1) && !hasQuestItems(player, BLACKBIRD_CLAN_CERTIFICATION, GIANT_TRACKERS_CERTIFICATION))
						{
							htmltext = "34237-05.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34237-07.htm";
						}
						else
						{
							htmltext = "34237-06.htm";
						}
						break;
					}
					case KEKROPUS:
					{
						if (qs.isCond(2))
						{
							htmltext = "34222-01.htm";
						}
						else
						{
							htmltext = "34222-00.html";
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
		return htmltext;
	}
}
