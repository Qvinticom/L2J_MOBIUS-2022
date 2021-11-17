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
package quests.Q00783_VestigeOfTheMagicPower;

import java.util.List;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10455_ElikiasLetter.Q10455_ElikiasLetter;

/**
 * Vestige of the Magic Power (783)
 * @URL https://l2wiki.com/Vestige_of_the_Magic_Power
 * @author Gigi, Iris, CostyKiller
 */
public class Q00783_VestigeOfTheMagicPower extends Quest
{
	// NPCs
	private static final int LEONA_BLACKBIRD = 31595;
	// Monsters
	private static final int[] MONSTERS =
	{
		23384, // Smaug
		23385, // Lunatikan
		23386, // Jabberwok
		23387, // Kanzaroth
		23388, // Kandiloth
		23395, // Garion
		23396, // Garion Neti
		23397, // Desert Wendigo
		23398, // Koraza
		23399 // Bend Beetle
	};
	// Misc
	private static final int MIN_LEVEL = 99;
	private static final int HIGH_GRADE_FRAGMENT_OF_CHAOS = 46557;
	private static final int BASIC_SUPPLY_BOX = 47356;
	private static final int INTERMEDIATE_SUPPLY_BOX = 47357;
	private static final int ADVANCED_SUPPLY_BOX = 47358;
	
	public Q00783_VestigeOfTheMagicPower()
	{
		super(783);
		addStartNpc(LEONA_BLACKBIRD);
		addTalkId(LEONA_BLACKBIRD);
		addKillId(MONSTERS);
		registerQuestItems(HIGH_GRADE_FRAGMENT_OF_CHAOS);
		addCondMinLevel(MIN_LEVEL, "31595-00.html");
		addCondCompletedQuest(Q10455_ElikiasLetter.class.getSimpleName(), "31595-00.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final int chance = getRandom(100);
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		switch (event)
		{
			case "31595-02.htm":
			case "31595-03.htm":
			case "31595-05.htm":
			case "31595-06.htm":
			case "31595-06d.html":
			case "31595-07.htm":
			case "31595-07d.html":
			{
				htmltext = event;
				break;
			}
			case "select_mission":
			{
				qs.startQuest();
				if ((player.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 1) && (player.getFactionLevel(Faction.BLACKBIRD_CLAN) < 2))
				{
					htmltext = "31595-04a.htm";
					break;
				}
				else if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 2)
				{
					htmltext = "31595-04b.htm";
					break;
				}
				htmltext = "31595-04.htm";
				break;
			}
			case "return":
			{
				if ((player.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 1) && (player.getFactionLevel(Faction.BLACKBIRD_CLAN) < 2))
				{
					htmltext = "31595-04a.htm";
					break;
				}
				else if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 2)
				{
					htmltext = "31595-04b.htm";
					break;
				}
				htmltext = "31595-04.htm";
				break;
			}
			case "31595-05a.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "31595-06a.html":
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "31595-07a.html":
			{
				qs.setCond(4, true);
				htmltext = event;
				break;
			}
			case "31595-05c.html":
			{
				if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) == 300) && (player.getLevel() >= MIN_LEVEL))
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
					addFactionPoints(player, Faction.BLACKBIRD_CLAN, 100);
					addExpAndSp(player, 12113489880L, 12113460);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
				}
				else
				{
					htmltext = getNoQuestLevelRewardMsg(player);
				}
				break;
			}
			case "31595-06c.html":
			{
				if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) == 600) && (player.getLevel() >= MIN_LEVEL))
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
					addFactionPoints(player, Faction.BLACKBIRD_CLAN, 200);
					addExpAndSp(player, 24226979760L, 24226920);
					qs.exitQuest(QuestType.DAILY, true);
					htmltext = event;
				}
				else
				{
					htmltext = getNoQuestLevelRewardMsg(player);
				}
				break;
			}
			case "31595-07c.html":
			{
				if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) == 900) && (player.getLevel() >= MIN_LEVEL))
				{
					if (chance < 2)
					{
						giveItems(player, BASIC_SUPPLY_BOX, 1);
					}
					else if (chance < 20)
					{
						giveItems(player, INTERMEDIATE_SUPPLY_BOX, 1);
					}
					else if (chance < 100)
					{
						giveItems(player, ADVANCED_SUPPLY_BOX, 1);
					}
					addFactionPoints(player, Faction.BLACKBIRD_CLAN, 300);
					addExpAndSp(player, 36340469640L, 36340380);
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
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (npc.getId() == LEONA_BLACKBIRD)
		{
			switch (qs.getState())
			{
				case State.CREATED:
				{
					htmltext = "31595-01.htm";
					break;
				}
				case State.STARTED:
				{
					switch (qs.getCond())
					{
						case 1:
						{
							if ((player.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 1) && (player.getFactionLevel(Faction.BLACKBIRD_CLAN) < 2))
							{
								htmltext = "31595-04a.htm";
								break;
							}
							else if (player.getFactionLevel(Faction.BLACKBIRD_CLAN) >= 2)
							{
								htmltext = "31595-04b.htm";
								break;
							}
							htmltext = "31595-04.htm";
							break;
						}
						case 2:
						{
							if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) >= 300) && (player.getLevel() >= MIN_LEVEL))
							{
								htmltext = "31595-05b.htm";
								break;
							}
							htmltext = "31595-08.htm";
							break;
						}
						case 3:
						{
							if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) >= 600) && (player.getLevel() >= MIN_LEVEL))
							{
								htmltext = "31595-06b.htm";
								break;
							}
							htmltext = "31595-08.htm";
							break;
						}
						case 4:
						{
							if ((getQuestItemsCount(player, HIGH_GRADE_FRAGMENT_OF_CHAOS) >= 900) && (player.getLevel() >= MIN_LEVEL))
							{
								htmltext = "31595-07b.htm";
								break;
							}
							htmltext = "31595-08.htm";
							break;
						}
						case 5:
						{
							htmltext = "31595-05b.htm";
							break;
						}
						case 6:
						{
							htmltext = "31595-06b.htm";
							break;
						}
						case 7:
						{
							htmltext = "31595-07b.htm";
							break;
						}
					}
					break;
				}
				case State.COMPLETED:
				{
					if (!qs.isNowAvailable())
					{
						htmltext = "31595-00a.htm";
						break;
					}
					qs.setState(State.CREATED);
					htmltext = "31595-01.htm";
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (killer.isInParty())
		{
			final Party party = killer.getParty();
			final List<Player> partyMember = party.getMembers();
			for (Player singleMember : partyMember)
			{
				final QuestState qsPartyMember = getQuestState(singleMember, false);
				final double distance = npc.calculateDistance3D(singleMember);
				if ((qsPartyMember != null) && (distance <= 1000))
				{
					if (qsPartyMember.isCond(2) && giveItemRandomly(singleMember, npc, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1, 300, 1, true))
					{
						qsPartyMember.setCond(5, true);
					}
					if (qsPartyMember.isCond(3) && giveItemRandomly(singleMember, npc, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1, 600, 1, true))
					{
						qsPartyMember.setCond(6, true);
					}
					if (qsPartyMember.isCond(4) && giveItemRandomly(singleMember, npc, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1, 900, 1, true))
					{
						qsPartyMember.setCond(7, true);
					}
				}
			}
		}
		else
		{
			final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
			if (qs != null)
			{
				if (qs.isCond(2) && giveItemRandomly(killer, npc, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1, 300, 1, true))
				{
					qs.setCond(5, true);
				}
				if (qs.isCond(3) && giveItemRandomly(killer, npc, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1, 600, 1, true))
				{
					qs.setCond(6, true);
				}
				if (qs.isCond(4) && giveItemRandomly(killer, npc, HIGH_GRADE_FRAGMENT_OF_CHAOS, 1, 900, 1, true))
				{
					qs.setCond(7, true);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}