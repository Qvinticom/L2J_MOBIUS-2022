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
package quests.Q10472_WindsOfFate_EncroachingShadows;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.network.serverpackets.ExShowUsm;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

/**
 * Winds Of Fate: Encroaching Shadows (10472)
 * @author Joker, Stayway
 */
public class Q10472_WindsOfFate_EncroachingShadows extends Quest
{
	// NPCs
	private static final int NAVARI = 33931;
	private static final int ZEPHYRA = 33978;
	private static final int MOMET = 33998;
	private static final int MAMMON = 31092;
	private static final int BLACKSMITH_MAMMON = 31126;
	private static final int HARDIN = 33870;
	private static final int WYNN = 33403;
	private static final int FEOH = 33401;
	private static final int TYRR = 33398;
	private static final int OTHELL = 33399;
	private static final int ISS = 33402;
	private static final int YUL = 33400;
	private static final int SIGEL = 33397;
	private static final int AEORE = 33404;
	private static final int KARLA = 33933;
	private static final int RAINA = 33491;
	// Items
	private static final int DARK_FRAGMENT = 40060;
	private static final int COUNTERFEIT_ATELIA = 40059;
	private static final int FIRE_STONE = 9546;
	private static final int WATER_STONE = 9547;
	private static final int EARTH_STONE = 9548;
	private static final int WIND_STONE = 9549;
	private static final int DARK_STONE = 9550;
	private static final int HOLY_STONE = 9551;
	private static final int CRYSTAL_R = 17371;
	private static final int RECIPE = 36791;
	// Mobs
	private static final int[] MOBS =
	{
		23174, // Arbitor of Darkness
		23175, // Altar of Evil Offering Boxe
		23176, // Mutated Cerberos
		23177, // Dartanion
		23178, // Insane Phion
		23179, // Dimensional Rifter
		23180, // Hellgate Fighting Dog
	};
	// Misc
	private static final int MIN_LEVEL = 85;
	private static final int RELIQUARY_PRESENTATION_MOVIE_ZONE = 10472;
	private static final String MOVIE_VAR = "reliquary_of_the_giants_movie_zone";
	private static final int ERTHEIA_AWEK_QUEST_USM_ID = 9; // TODO: Find proper Movie
	
	public Q10472_WindsOfFate_EncroachingShadows()
	{
		super(10472, Q10472_WindsOfFate_EncroachingShadows.class.getSimpleName(), "Winds Of Fate: Encroaching Shadows");
		addStartNpc(NAVARI);
		addTalkId(NAVARI, ZEPHYRA, MOMET, MAMMON, BLACKSMITH_MAMMON, HARDIN, WYNN, FEOH, TYRR, OTHELL, ISS, YUL, SIGEL, AEORE, KARLA, RAINA);
		registerQuestItems(DARK_FRAGMENT, COUNTERFEIT_ATELIA, FIRE_STONE, WATER_STONE, EARTH_STONE, WIND_STONE, DARK_STONE, HOLY_STONE, CRYSTAL_R, RECIPE);
		addKillId(MOBS);
		// addCondCompletedQuest(Q10471_WindsOfFate_Choices.class.getSimpleName(), "no_cond.html"); // Need be Done!
		addEnterZoneId(RELIQUARY_PRESENTATION_MOVIE_ZONE);
		addCondMinLevel(MIN_LEVEL, "no_level.html");
		addCondRace(Race.ERTHEIA, "no_race.html"); // TODO: Find proper HTML
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "33931-02.htm":
			case "33931-03.htm":
			case "33931-04.htm":
			case "33978-02.html":
			case "33978-06.html":
			case "33998-02.html":
			case "33998-06.html":
			case "33998-03.html":
			case "31092-02.html":
			case "31092-06.html":
			case "31092-07.html":
			case "31126-02.html":
			case "31126-03.html":
			case "31126-04.html":
			case "31126-05.html":
			case "31126-06.html":
			case "31126-07.html":
			case "33870-02.html":
			case "33870-03.html":
			case "33870-04.html":
			case "33870-08.html":
			case "33933-02.html":
			case "33491-02.html":
			case "33491-03.html":
			case "33491-04.html":
			{
				htmltext = event;
				break;
			}
			case "33931-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33978-03.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
				}
				htmltext = event;
				break;
			}
			case "33998-04.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
				}
				htmltext = event;
				break;
			}
			case "31092-03.html":
			{
				if (qs.isCond(3))
				{
					qs.setCond(4, true);
				}
				htmltext = event;
				break;
			}
			case "31092-08.html":
			{
				if (qs.isCond(5))
				{
					player.setCurrentHp(player.getMaxHp() / 3);
					player.setCurrentCp(player.getMaxCp() / 3);
					player.setCurrentMp(player.getMaxMp() / 3);
					takeItems(player, DARK_FRAGMENT, -1);
					qs.setCond(6, true);
				}
				htmltext = event;
				break;
			}
			case "31126-08.html":
			{
				if (qs.isCond(6))
				{
					giveItems(player, COUNTERFEIT_ATELIA, 1);
					qs.set(MOVIE_VAR, 1);
					qs.setCond(7, true);
				}
				htmltext = event;
				break;
			}
			case "33870-05.html":
			{
				if (qs.isCond(7))
				{
					qs.setCond(8, true);
				}
				htmltext = event;
				break;
			}
			case "33870-09.html":
			{
				if (qs.isCond(16))
				{
					npc.setTarget(player);
					npc.broadcastPacket(new MagicSkillUse(npc, player, 16398, 1, 1000, 0));
					takeItems(player, COUNTERFEIT_ATELIA, -1);
					qs.setCond(17, true);
				}
				htmltext = event;
				break;
			}
			case "33978-07.html":
			{
				if (qs.isCond(17))
				{
					qs.setCond(18, true);
				}
				htmltext = event;
				break;
			}
			case "33933-03.html":
			{
				if (qs.isCond(18))
				{
					qs.setCond(19, true);
				}
				htmltext = event;
				break;
			}
			case "fire":
			{
				if (qs.isCond(19))
				{
					giveItems(player, FIRE_STONE, 15);
					giveItems(player, CRYSTAL_R, 5);
					giveItems(player, RECIPE, 1);
					addExpAndSp(player, 175739575, 47177);
					qs.exitQuest(false, true);
					return "33491-05.html";
				}
				htmltext = event;
				break;
			}
			case "water":
			{
				if (qs.isCond(19))
				{
					giveItems(player, WATER_STONE, 15);
					giveItems(player, CRYSTAL_R, 5);
					giveItems(player, RECIPE, 1);
					addExpAndSp(player, 175739575, 47177);
					qs.exitQuest(false, true);
					return "33491-05.html";
				}
				htmltext = event;
				break;
			}
			case "earth":
			{
				if (qs.isCond(19))
				{
					giveItems(player, EARTH_STONE, 15);
					giveItems(player, CRYSTAL_R, 5);
					giveItems(player, RECIPE, 1);
					addExpAndSp(player, 175739575, 47177);
					qs.exitQuest(false, true);
					return "33491-05.html";
				}
				htmltext = event;
				break;
			}
			case "wind":
			{
				if (qs.isCond(19))
				{
					giveItems(player, WIND_STONE, 15);
					giveItems(player, CRYSTAL_R, 5);
					giveItems(player, RECIPE, 1);
					addExpAndSp(player, 175739575, 47177);
					qs.exitQuest(false, true);
					return "33491-05.html";
				}
				htmltext = event;
				break;
			}
			case "dark":
			{
				if (qs.isCond(19))
				{
					giveItems(player, DARK_STONE, 15);
					giveItems(player, CRYSTAL_R, 5);
					giveItems(player, RECIPE, 1);
					addExpAndSp(player, 175739575, 47177);
					qs.exitQuest(false, true);
					return "33491-05.html";
				}
				htmltext = event;
				break;
			}
			case "holy":
			{
				if (qs.isCond(19))
				{
					giveItems(player, HOLY_STONE, 15);
					giveItems(player, CRYSTAL_R, 5);
					giveItems(player, RECIPE, 1);
					addExpAndSp(player, 175739575, 47177);
					qs.exitQuest(false, true);
					return "33491-05.html";
				}
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (npc.getId())
		{
			case NAVARI:
			{
				if (qs.isCreated())
				{
					htmltext = "33931-01.htm";
				}
				else if (qs.isStarted())
				{
					htmltext = "33931-06.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case ZEPHYRA:
			{
				if (qs.isCond(1))
				{
					htmltext = "33978-01.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "33978-04.html";
				}
				else if (qs.isCond(17))
				{
					htmltext = "33978-05.html";
				}
				else if (qs.isCond(18))
				{
					htmltext = "33978-08.html";
				}
				break;
			}
			case MOMET:
			{
				if (qs.isCond(2))
				{
					htmltext = "33998-01.html";
				}
				else if (qs.isCond(3))
				{
					htmltext = "33998-05.html";
				}
				break;
			}
			case MAMMON:
			{
				if (qs.isCond(5) && (getQuestItemsCount(player, DARK_FRAGMENT) >= 50))
				{
					htmltext = "31092-05.html";
				}
				else if (qs.isCond(3))
				{
					htmltext = "31092-01.html";
				}
				else if (qs.isCond(4))
				{
					htmltext = "31092-04.html";
				}
				else if (qs.isCond(6))
				{
					htmltext = "31092-09.html";
				}
				break;
			}
			case BLACKSMITH_MAMMON:
			{
				if (qs.isCond(6))
				{
					htmltext = "31126-01.html";
				}
				else if (qs.isCond(7))
				{
					htmltext = "31126-09.html";
				}
				break;
			}
			case HARDIN:
			{
				if (qs.isCond(7))
				{
					htmltext = "33870-01.html";
				}
				else if (qs.isCond(8))
				{
					htmltext = "33870-06.html";
				}
				else if (qs.isCond(16))
				{
					htmltext = "33870-07.html";
				}
				else if (qs.isCond(17))
				{
					htmltext = "33870-10.html";
				}
				break;
			}
			case RAINA:
			{
				if (qs.isCond(19))
				{
					return getHtm(player.getHtmlPrefix(), "33491-01.html").replace("%name%", player.getName());
					
				}
			}
			case KARLA:
			{
				if (qs.isCond(18))
				{
					return getHtm(player.getHtmlPrefix(), "33933-01.html").replace("%name%", player.getName());
				}
				if (qs.isCond(19))
				{
					htmltext = "33933-04.html";
				}
				break;
			}
			
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		
		if ((qs != null) && qs.isCond(4) && giveItemRandomly(killer, npc, DARK_FRAGMENT, 1, 50, 1.0, true))
		{
			qs.setCond(5);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.isPlayer())
		{
			final L2PcInstance player = character.getActingPlayer();
			final QuestState qs = getQuestState(player, false);
			if ((qs != null) && (qs.getInt(MOVIE_VAR) > 0))
			{
				player.sendPacket(new ExShowUsm(ERTHEIA_AWEK_QUEST_USM_ID));
				qs.unset(MOVIE_VAR);
			}
		}
		return super.onEnterZone(character, zone);
	}
}