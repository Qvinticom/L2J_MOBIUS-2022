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
package quests.Q00931_MemoriesOfTheWind;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10831_UnbelievableSight.Q10831_UnbelievableSight;

/**
 * Memories of the Wind (931)
 * @URL https://l2wiki.com/Memories_of_the_Wind
 * @author Dmitri
 */
public class Q00931_MemoriesOfTheWind extends Quest
{
	// NPC
	private static final int CYPHONA = 34055;
	// Monsters
	private static final int EL_FERA = 23797;
	private static final int EL_FLOATO = 23559;
	private static final int KERBEROS_LAGER_N = 23550; // (night)
	private static final int KERBEROS_LAGER = 23541;
	private static final int KERBEROS_FORT_N = 23551; // (night)
	private static final int KERBEROS_FORT = 23542;
	private static final int KERBEROS_NERO_N = 23552; // (night)
	private static final int KERBEROS_NERO = 23543;
	private static final int FURY_SYLPH_BARRENA_N = 23553; // (night)
	private static final int FURY_SYLPH_BARRENA = 23544;
	private static final int FURY_SYLPH_TEMPTRESS_N = 23555; // (night)
	private static final int FURY_SYLPH_TEMPTRESS = 23546;
	private static final int FURY_SYLPH_PURKA_N = 23556; // (night)
	private static final int FURY_SYLPH_PURKA = 23547;
	private static final int FURY_KERBEROS_LEGER_N = 23557; // (night)
	private static final int FURY_KERBEROS_LEGER = 23545;
	private static final int FURY_KERBEROS_NERO_N = 23558; // (night)
	private static final int FURY_KERBEROS_NERO = 23549;
	// Items
	private static final int MEMORIES_OF_THE_WIND = 47188;
	private static final int UNWORLDLY_VISITORS_BASIC_SUPPLY_BOX = 47181;
	private static final int UNWORLDLY_VISITORS_INTERMEDIATE_SUPPLY_BOX = 47182;
	private static final int UNWORLDLY_VISITORS_ADVANCED_SUPPLY_BOX = 47183;
	// Misc
	private static final int MIN_LEVEL = 102;
	
	public Q00931_MemoriesOfTheWind()
	{
		super(931);
		addStartNpc(CYPHONA);
		addTalkId(CYPHONA);
		addKillId(EL_FERA, EL_FLOATO);
		addKillId(KERBEROS_LAGER, KERBEROS_LAGER_N, KERBEROS_FORT, KERBEROS_FORT_N, KERBEROS_NERO, KERBEROS_NERO_N, FURY_SYLPH_BARRENA, FURY_SYLPH_BARRENA_N, FURY_SYLPH_TEMPTRESS, FURY_SYLPH_TEMPTRESS_N, FURY_SYLPH_PURKA, FURY_SYLPH_PURKA_N, FURY_KERBEROS_LEGER, FURY_KERBEROS_LEGER_N, FURY_KERBEROS_NERO, FURY_KERBEROS_NERO_N);
		registerQuestItems(MEMORIES_OF_THE_WIND);
		addCondMinLevel(MIN_LEVEL, "34055-00.htm");
		addCondCompletedQuest(Q10831_UnbelievableSight.class.getSimpleName(), "34055-00.htm");
		addFactionLevel(Faction.UNWORLDLY_VISITORS, 2, "34055-00.htm");
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
			case "34055-02.htm":
			case "34055-03.htm":
			case "34055-04.htm":
			case "34055-04a.htm":
			case "34055-06.html":
			case "34055-06a.html":
			{
				htmltext = event;
				break;
			}
			case "select_mission":
			{
				qs.startQuest();
				if (player.getFactionLevel(Faction.UNWORLDLY_VISITORS) >= 3)
				{
					htmltext = "34055-04a.htm";
					break;
				}
				htmltext = "34055-04.htm";
				break;
			}
			case "return":
			{
				if (player.getFactionLevel(Faction.UNWORLDLY_VISITORS) >= 3)
				{
					htmltext = "34055-04a.htm";
					break;
				}
				htmltext = "34055-04.htm";
				break;
			}
			case "34055-07.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34055-07a.html":
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "34055-10.html":
			{
				final int chance = getRandom(100);
				switch (qs.getCond())
				{
					case 4:
					{
						if ((getQuestItemsCount(player, MEMORIES_OF_THE_WIND) == 12) && (player.getLevel() >= MIN_LEVEL))
						{
							if (chance < 2)
							{
								giveItems(player, UNWORLDLY_VISITORS_ADVANCED_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, UNWORLDLY_VISITORS_INTERMEDIATE_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, UNWORLDLY_VISITORS_BASIC_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 22_221_427_950L, 22_221_360);
							addFactionPoints(player, Faction.UNWORLDLY_VISITORS, 100);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
						}
						else
						{
							htmltext = getNoQuestLevelRewardMsg(player);
						}
						break;
					}
					case 5:
					{
						if ((getQuestItemsCount(player, MEMORIES_OF_THE_WIND) == 24) && (player.getLevel() >= MIN_LEVEL))
						{
							if (chance < 2)
							{
								giveItems(player, UNWORLDLY_VISITORS_ADVANCED_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, UNWORLDLY_VISITORS_BASIC_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, UNWORLDLY_VISITORS_INTERMEDIATE_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 44_442_855_900L, 44_442_720);
							addFactionPoints(player, Faction.UNWORLDLY_VISITORS, 200);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
						}
						else
						{
							htmltext = getNoQuestLevelRewardMsg(player);
						}
						break;
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
				htmltext = "34055-01.htm";
				// fallthrough?
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						if (player.getFactionLevel(Faction.UNWORLDLY_VISITORS) >= 3)
						{
							htmltext = "34055-04a.htm";
							break;
						}
						htmltext = "34055-04.htm";
						break;
					}
					case 2:
					{
						htmltext = "34055-08.html";
						break;
					}
					case 3:
					{
						htmltext = "34055-08a.html";
						break;
					}
					case 4:
					case 5:
					{
						htmltext = "34055-09.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (!qs.isNowAvailable())
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
				}
				else
				{
					qs.setState(State.CREATED);
					htmltext = "34055-01.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.getCond() > 1) && killer.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			switch (npc.getId())
			{
				case KERBEROS_LAGER:
				case KERBEROS_FORT:
				case KERBEROS_NERO:
				case FURY_SYLPH_BARRENA:
				case FURY_SYLPH_TEMPTRESS:
				case FURY_SYLPH_PURKA:
				case FURY_KERBEROS_LEGER:
				case FURY_KERBEROS_NERO:
				case KERBEROS_LAGER_N:
				case KERBEROS_FORT_N:
				case KERBEROS_NERO_N:
				case FURY_SYLPH_BARRENA_N:
				case FURY_SYLPH_TEMPTRESS_N:
				case FURY_SYLPH_PURKA_N:
				case FURY_KERBEROS_LEGER_N:
				case FURY_KERBEROS_NERO_N:
				{
					if (getRandom(100) < 25)
					{
						final Npc mob = addSpawn(EL_FERA, npc.getX(), npc.getY(), npc.getZ(), 0, true, 120000);
						addAttackPlayerDesire(mob, killer, 5);
					}
					break;
				}
				case EL_FERA:
				case EL_FLOATO:
				{
					switch (qs.getCond())
					{
						case 2:
						{
							if (giveItemRandomly(killer, npc, MEMORIES_OF_THE_WIND, 1, 12, 0.5, true))
							{
								qs.setCond(4, true);
							}
							break;
						}
						case 3:
						{
							if (giveItemRandomly(killer, npc, MEMORIES_OF_THE_WIND, 1, 24, 0.5, true))
							{
								qs.setCond(5, true);
							}
							break;
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
