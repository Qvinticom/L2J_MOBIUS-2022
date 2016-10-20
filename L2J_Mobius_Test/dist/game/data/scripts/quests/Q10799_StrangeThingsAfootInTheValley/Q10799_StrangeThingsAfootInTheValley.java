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
package quests.Q10799_StrangeThingsAfootInTheValley;

import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

/**
 * Strange Things Afoot in the Valley (10799)
 * @URL https://l2wiki.com/Strange_Things_Afoot_in_the_Valley
 * @author Gigi
 */
public class Q10799_StrangeThingsAfootInTheValley extends Quest
{
	// NPCs
	private static final int NAMO = 33973;
	// Monsters
	private static final int MOB_1 = 23423; // Mesmer Dragon
	private static final int MOB_2 = 23424; // Gargoyle Dragon
	private static final int MOB_3 = 23425; // Black Dragon
	private static final int MOB_4 = 23427; // Sand Dragon
	private static final int MOB_5 = 23428; // Captain Dragonblood
	private static final int MOB_6 = 23429; // Minion Dragonblood
	private static final int MOB_7 = 23436; // Cave Servant Archer
	private static final int MOB_8 = 23437; // Cave Servant Warrior
	private static final int MOB_9 = 23438; // Metallic Cave Servant
	private static final int MOB_10 = 23439; // Iron Cave Servant
	private static final int MOB_11 = 23440; // Headless Knight
	// Items
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 196);
	private static final ItemHolder EAS = new ItemHolder(960, 5);
	private static final ItemHolder FIRE_STONE = new ItemHolder(9546, 30);
	private static final ItemHolder WATER_STONE = new ItemHolder(9547, 30);
	private static final ItemHolder EARTH_STONE = new ItemHolder(9548, 30);
	private static final ItemHolder WIND_STONE = new ItemHolder(9549, 30);
	private static final ItemHolder DARK_STONE = new ItemHolder(9550, 30);
	private static final ItemHolder HOLY_STONE = new ItemHolder(9551, 30);
	// Reward
	private static final int EXP_REWARD = 76658400;
	private static final int SP_REWARD = 18398;
	// Misc
	private static final int MIN_LEVEL = 76;
	private static final int MAX_LEVEL = 85;
	
	public Q10799_StrangeThingsAfootInTheValley()
	{
		super(10799, Q10799_StrangeThingsAfootInTheValley.class.getSimpleName(), "Strange Things Afoot in the Valley");
		addStartNpc(NAMO);
		addTalkId(NAMO);
		addKillId(MOB_1, MOB_2, MOB_3, MOB_4, MOB_5, MOB_6, MOB_7, MOB_8, MOB_9, MOB_10, MOB_11);
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "33973-02.htm":
			case "33973-03.htm":
			case "33973-07.html":
			{
				htmltext = event;
				break;
			}
			case "33973-04.htm":
			{
				qs.startQuest();
				qs.set(Integer.toString(MOB_1), 0);
				htmltext = event;
				break;
			}
			case "fire":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAS);
				giveItems(player, FIRE_STONE);
				qs.exitQuest(false, true);
				htmltext = "33973-08.html";
				break;
			}
			case "water":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAS);
				giveItems(player, WATER_STONE);
				qs.exitQuest(false, true);
				htmltext = "33973-08.html";
				break;
			}
			case "earth":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAS);
				giveItems(player, EARTH_STONE);
				qs.exitQuest(false, true);
				htmltext = "33973-08.html";
				break;
			}
			case "wind":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAS);
				giveItems(player, WIND_STONE);
				qs.exitQuest(false, true);
				htmltext = "33973-08.html";
				break;
			}
			case "dark":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAS);
				giveItems(player, DARK_STONE);
				qs.exitQuest(false, true);
				htmltext = "33973-08.html";
				break;
			}
			case "holy":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAS);
				giveItems(player, HOLY_STONE);
				qs.exitQuest(false, true);
				htmltext = "33973-08.html";
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
		if (qs.isCreated())
		{
			htmltext = "33973-01.htm";
		}
		else if ((qs.isStarted()) && (qs.isCond(1)))
		{
			htmltext = "33973-05.html";
		}
		else if (qs.isCond(2))
		{
			htmltext = "33973-06.html";
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isCond(1) && Util.checkIfInRange(1500, npc, qs.getPlayer(), false))
		{
			int kills = qs.getInt(Integer.toString(MOB_1));
			kills++;
			qs.set(Integer.toString(MOB_1), kills);
			playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if (kills >= 100)
			{
				qs.setCond(2, true);
			}
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpcString(NpcStringId.KILL_MONSTERS_IN_THE_DRAGON_VALLEY, kills);
			killer.sendPacket(log);
		}
		return super.onKill(npc, killer, isSummon);
	}
}