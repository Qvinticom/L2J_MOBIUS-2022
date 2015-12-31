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
package quests.Q10359_SakumsTrace;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.util.Util;

/**
 * Sakum's Trace (10359)
 * @author spider
 */
public class Q10359_SakumsTrace extends Quest
{
	// NPCs
	private static final int ADV_GUILDSMAN = 31795;
	private static final int FRED = 33179;
	private static final int TOBIAS = 30297;
	private static final int RAYMOND = 30289;
	private static final int RAINS = 30288;
	private static final int DRIKUS = 30505;
	private static final int MENDIO = 30504;
	private static final int GERSHWIN = 32196;
	private static final int ELLENIA = 30155;
	private static final int ESRANDELL = 30158;
	// Monsters
	private static final int MONSTER_EYE_WATCHER = 20067;
	private static final int LESSER_BASILISK = 20070;
	private static final int BASILISK = 20072;
	private static final int SKELETON_MARAUDER = 20190;
	private static final int GRANITE_GOLEM = 23098;
	private static final int SAHARA = 23026;
	private static final int TYRANT = 20192;
	// Items
	private static final int SUSPICIOUS_FRAGMENT = 17586;
	private static final int SUSPICIOUS_FRAGMENT_REQUIRED = 20;
	// Rewards
	private static final int ADENA_REWARD = 108000;
	private static final int EXP_REWARD = 900000;
	private static final int SP_REWARD = 216;
	// Others
	private static final int MIN_LEVEL = 34;
	private static final int MAX_LEVEL = 40;
	
	public Q10359_SakumsTrace()
	{
		super(10359, Q10359_SakumsTrace.class.getSimpleName(), "Sakum's Trace");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
		addTalkId(ADV_GUILDSMAN, FRED, TOBIAS, RAYMOND, RAINS, DRIKUS, MENDIO, GERSHWIN, ELLENIA, ESRANDELL);
		addKillId(MONSTER_EYE_WATCHER, LESSER_BASILISK, BASILISK, SKELETON_MARAUDER, GRANITE_GOLEM, SAHARA, TYRANT);
		registerQuestItems(SUSPICIOUS_FRAGMENT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "30297-02.html":
			{
				htmltext = event;
				break;
			}
			case "30297-03.html": // end quest
			{
				if (qs.getCond() > 3)
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
			}
			case "31795-02.htm":
			{
				htmltext = event;
				break;
			}
			case "31795-03.htm": // start quest
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33179-02.html":
			{
				htmltext = event;
				break;
			}
			case "33179-03.html": // go kill mobs
			{
				if (qs.isCond(1))
				{
					qs.setCond(2);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = npc.getId() == ADV_GUILDSMAN ? "31795-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case ADV_GUILDSMAN:
					{
						htmltext = "31795-03.htm";
						break;
					}
					case FRED:
					{
						if (qs.isCond(1))
						{
							htmltext = "33179-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "33179-04.html";
						}
						else if (qs.isCond(3)) // go to race master
						{
							switch (player.getRace())
							{
								case HUMAN:
								{
									if (player.isMageClass())
									{
										qs.setCond(4); // go to High Priest Raymond Einhasad Temple
										htmltext = "33179-05a.html";
									}
									else
									{
										qs.setCond(5); // go to Grand Master Rains
										htmltext = "33179-05b.html";
									}
									break;
								}
								case ELF:
								{
									if (player.isMageClass())
									{
										qs.setCond(11); // Grand Magister Esrandell in Einhasad Temple
										htmltext = "33179-06a.html";
									}
									else
									{
										qs.setCond(10); // Grand Master Ellenia in Warrior Guild
										htmltext = "33179-06b.html";
									}
									break;
								}
								case DARK_ELF:
								{
									qs.setCond(6); // go to Tobias DE guild
									htmltext = "33179-07.html";
									break;
								}
								case ORC:
								{
									qs.setCond(7); // High Prefect Drikus in Orc Guild
									htmltext = "33179-08.html";
									break;
								}
								case DWARF:
								{
									qs.setCond(8); // Head Blacksmith Mendio in the Blacksmith Workshop
									htmltext = "33179-09.html";
									break;
								}
								case KAMAEL:
								{
									qs.setCond(9); // Grand Master Gershwin in Kamael Guild
									htmltext = "33179-10.html";
									break;
								}
							}
						}
						break;
					}
					case RAYMOND: // TODO: get all race-specific texts
					case RAINS:
					case DRIKUS:
					case MENDIO:
					case GERSHWIN:
					case ELLENIA:
					case ESRANDELL:
					case TOBIAS:
					{
						if (qs.getCond() > 3)
						{
							htmltext = "30297-01.html";
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
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isStarted() && qs.isCond(2) && (Util.checkIfInRange(1500, npc, qs.getPlayer(), false)))
		{
			if (getQuestItemsCount(qs.getPlayer(), SUSPICIOUS_FRAGMENT) < SUSPICIOUS_FRAGMENT_REQUIRED)
			{
				giveItemRandomly(qs.getPlayer(), npc, SUSPICIOUS_FRAGMENT, 1, SUSPICIOUS_FRAGMENT_REQUIRED, 0.25, true);
			}
			else
			{
				qs.setCond(3);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
