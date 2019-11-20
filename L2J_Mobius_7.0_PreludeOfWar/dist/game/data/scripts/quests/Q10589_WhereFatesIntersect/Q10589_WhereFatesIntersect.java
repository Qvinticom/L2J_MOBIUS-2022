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
package quests.Q10589_WhereFatesIntersect;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.ExTutorialShowId;
import org.l2jmobius.gameserver.util.Util;

/**
 * Where Fates Intersect (10589)
 * @URL https://l2wiki.com/Where_Fates_Intersect
 * @author Dmitri
 */
public class Q10589_WhereFatesIntersect extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int HERPA = 34362;
	private static final int WALLODOS = 30137;
	private static final int JOACHIM = 34513;
	private static final int[] MONSTERS =
	{
		24452, // Doom Soldier
		24453, // Doom Servant
		24454, // Doom Berserker
		24455, // Doom Seer
	};
	// Item
	private static final int MONSTER_DROP = 80853; // Traces of Evil Spirit
	// Rewards
	private static final long EXP = 1;
	private static final int SP = 1;
	private static final int ADENA_AMOUNT = 5050;
	private static final int ACHIEVEMENT_BOX = 80908;
	// Misc
	private static final int MIN_LEVEL = 95;
	// Location
	private static final Location ALTAR_OF_EVIL = new Location(-13982, 22124, -3611);
	private static final Location TOWN_OF_ADEN = new Location(146632, 26760, -2213);
	
	public Q10589_WhereFatesIntersect()
	{
		super(10589);
		addStartNpc(TARTI);
		addTalkId(TARTI, HERPA, WALLODOS, JOACHIM);
		addKillId(MONSTERS);
		registerQuestItems(MONSTER_DROP);
		addCondMinLevel(85, "34505-06.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "34505-04.html":
			case "34505-02.html":
			case "30137-03.html":
			case "30137-02.html":
			case "34362-03.html":
			case "34362-05.html":
			{
				htmltext = event;
				break;
			}
			case "34505-03.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(TOWN_OF_ADEN);
				}
				break;
			}
			case "34362-02.html":
			{
				qs.setCond(2, true);
				player.sendPacket(new ExTutorialShowId(37)); // Adventurers Guide
				htmltext = event;
				break;
			}
			case "teleport_d":
			{
				if (qs.isCond(2))
				{
					player.teleToLocation(ALTAR_OF_EVIL);
				}
				break;
			}
			case "34513-02.html":
			{
				if (qs.isCond(5))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						// Reward.
						addExpAndSp(player, EXP, SP);
						giveAdena(player, ADENA_AMOUNT, false);
						giveItems(player, ACHIEVEMENT_BOX, 1);
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
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == TARTI)
				{
					htmltext = "34505-01.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case TARTI:
					{
						if (qs.isCond(1))
						{
							htmltext = "34505-03.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "34505-05.html";
						}
						break;
					}
					case HERPA:
					{
						if (qs.isCond(1))
						{
							htmltext = "34362-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34362-03.html";
						}
						else if (qs.isCond(4))
						{
							qs.setCond(5, true);
							htmltext = "34362-04.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "34362-06.html";
						}
						break;
					}
					case WALLODOS:
					{
						if (qs.isCond(2))
						{
							qs.setCond(3, true);
							htmltext = "30137-01.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "30137-04.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "30137-05.html";
						}
						break;
					}
					case JOACHIM:
					{
						if (qs.isCond(2))
						{
							htmltext = "34513-03.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34513-04.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "34513-05.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "34513-01.html";
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
	
	@Override
	public void actionForEachPlayer(PlayerInstance player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(3) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, player, false))
		{
			if ((getQuestItemsCount(player, MONSTER_DROP) < 200) && (getRandom(100) < 90))
			{
				giveItems(player, MONSTER_DROP, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if ((getQuestItemsCount(player, MONSTER_DROP) >= 200) && (player.getLevel() >= MIN_LEVEL))
			{
				qs.setCond(4, true);
			}
		}
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
}
