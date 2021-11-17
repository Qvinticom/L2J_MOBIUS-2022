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
package quests.Q00930_DisparagingThePhantoms;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10457_KefensisIllusion.Q10457_KefensisIllusion;

/**
 * Disparaging the Phantoms (930)
 * @URL https://l2wiki.com/Disparaging_the_Phantoms
 * @author Dmitri
 */
public class Q00930_DisparagingThePhantoms extends Quest
{
	// NPCs
	private static final int SPORCHA = 34230;
	// Monsters
	private static final int VIPER = 23389;
	private static final int VIPER_CHERKIA = 23796;
	private static final int SMAUG = 23384;
	private static final int LUNATIKAN = 23385;
	private static final int JABBERWOK = 23386;
	private static final int KANZAROTH = 23387;
	private static final int KANDILOTH = 23388;
	private static final int GARION = 23395;
	private static final int GARION_NETI = 23396;
	private static final int DESERT_WENDIGO = 23397;
	private static final int KORAZA = 23398;
	private static final int BEND_BEETLE = 23399;
	private static final int[] VIPE =
	{
		23389, // Viper
		23796, // Viper Cherkia
	};
	// Items Rewards
	private static final int BASIC_SUPPLY_BOX = 47356; // Basic Supply Box Blackbird Clan
	private static final int INTERMEDIATE_SUPPLY_BOX = 47357; // Intermediate Supply Box Blackbird Clan
	private static final int ADVANCED_SUPPLY_BOX = 47358; // Advanced Supply Box Blackbird Clan
	// Misc
	private static final int MIN_LEVEL = 99;
	
	public Q00930_DisparagingThePhantoms()
	{
		super(930);
		addStartNpc(SPORCHA);
		addTalkId(SPORCHA);
		addKillId(VIPE);
		addKillId(VIPER, VIPER_CHERKIA, SMAUG, LUNATIKAN, JABBERWOK, KANZAROTH, KANDILOTH, GARION, GARION_NETI, DESERT_WENDIGO, KORAZA, BEND_BEETLE);
		addCondMinLevel(MIN_LEVEL, "34230-00.htm");
		addCondCompletedQuest(Q10457_KefensisIllusion.class.getSimpleName(), "34230-00.htm");
		addFactionLevel(Faction.BLACKBIRD_CLAN, 4, "34230-00.htm");
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
			case "34230-02.htm":
			case "34230-03.htm":
			case "34230-04.htm":
			case "34230-04a.htm":
			case "34230-06.html":
			case "34230-06a.html":
			{
				htmltext = event;
				break;
			}
			case "select_mission":
			{
				qs.startQuest();
				if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) < 5)
				{
					htmltext = "34230-04a.htm";
					break;
				}
				htmltext = "34230-04.htm";
				break;
			}
			case "return":
			{
				if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) < 5)
				{
					htmltext = "34230-04a.htm";
					break;
				}
				htmltext = "34230-04.htm";
				break;
			}
			case "34230-07.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34230-07a.html":
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "34230-10.html":
			{
				final int chance = getRandom(100);
				switch (qs.getCond())
				{
					case 4:
					{
						if (player.getLevel() >= MIN_LEVEL)
						{
							if (chance < 2)
							{
								giveItems(player, ADVANCED_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, INTERMEDIATE_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, BASIC_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 12113489880L, 12113460);
							addFactionPoints(player, Faction.BLACKBIRD_CLAN, 100);
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
						if (player.getLevel() >= MIN_LEVEL)
						{
							if (chance < 2)
							{
								giveItems(player, ADVANCED_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, BASIC_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, INTERMEDIATE_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 24226979760L, 24226920);
							addFactionPoints(player, Faction.BLACKBIRD_CLAN, 200);
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
				htmltext = "34230-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) < 5)
						{
							htmltext = "34230-04a.htm";
							break;
						}
						htmltext = "34230-04.htm";
						break;
					}
					case 2:
					{
						htmltext = "34230-08.html";
						break;
					}
					case 3:
					{
						htmltext = "34230-08a.html";
						break;
					}
					case 4:
					case 5:
					{
						htmltext = "34230-09.html";
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
					htmltext = "34230-01.htm";
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
				case SMAUG:
				case LUNATIKAN:
				case JABBERWOK:
				case KANZAROTH:
				case KANDILOTH:
				case GARION:
				case GARION_NETI:
				case DESERT_WENDIGO:
				case KORAZA:
				case BEND_BEETLE:
				{
					if (getRandom(100) < 25)
					{
						final Npc mob = addSpawn(VIPER, npc.getX(), npc.getY(), npc.getZ(), 0, true, 120000);
						addAttackPlayerDesire(mob, killer, 5);
					}
					else if (getRandom(100) < 25)
					{
						final Npc mob = addSpawn(VIPER_CHERKIA, npc.getX(), npc.getY(), npc.getZ(), 0, true, 120000);
						addAttackPlayerDesire(mob, killer, 5);
					}
					break;
				}
				case VIPER:
				case VIPER_CHERKIA:
				{
					int killedViper = qs.getInt("killed_" + VIPE[0]);
					switch (qs.getCond())
					{
						case 2:
						{
							if (CommonUtil.contains(VIPE, npc.getId()))
							{
								if ((killedViper < 3) && (getRandom(100) < 25))
								{
									killedViper++;
									qs.set("killed_" + VIPE[0], killedViper);
									playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
								}
								
								if (killedViper == 3)
								{
									qs.setCond(4, true);
								}
							}
							break;
						}
						case 3:
						{
							if (CommonUtil.contains(VIPE, npc.getId()))
							{
								if ((killedViper < 6) && (getRandom(100) < 25))
								{
									killedViper++;
									qs.set("killed_" + VIPE[0], killedViper);
									playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
								}
								
								if (killedViper == 6)
								{
									qs.setCond(5, true);
								}
								break;
							}
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (qs.getCond() > 1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>();
			npcLogList.add(new NpcLogListHolder(VIPE[0], false, qs.getInt("killed_" + VIPE[0])));
			return npcLogList;
		}
		return super.getNpcLogList(player);
	}
}