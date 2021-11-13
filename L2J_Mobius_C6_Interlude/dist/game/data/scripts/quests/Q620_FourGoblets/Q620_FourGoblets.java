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
package quests.Q620_FourGoblets;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.instancemanager.FourSepulchersManager;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

public class Q620_FourGoblets extends Quest
{
	// NPCs
	private static final int GHOST_OF_WIGOTH_1 = 31452;
	private static final int NAMELESS_SPIRIT = 31453;
	private static final int GHOST_OF_WIGOTH_2 = 31454;
	private static final int GHOST_CHAMBERLAIN_1 = 31919;
	private static final int GHOST_CHAMBERLAIN_2 = 31920;
	private static final int CONQUERORS_SEPULCHER_MANAGER = 31921;
	private static final int EMPERORS_SEPULCHER_MANAGER = 31922;
	private static final int GREAT_SAGES_SEPULCHER_MANAGER = 31923;
	private static final int JUDGES_SEPULCHER_MANAGER = 31924;
	// Items
	private static final int RELIC = 7254;
	private static final int SEALED_BOX = 7255;
	private static final int GOBLET_OF_ALECTIA = 7256;
	private static final int GOBLET_OF_TISHAS = 7257;
	private static final int GOBLET_OF_MEKARA = 7258;
	private static final int GOBLET_OF_MORIGUL = 7259;
	private static final int USED_GRAVE_PASS = 7261;
	// Rewards
	private static final int ANTIQUE_BROOCH = 7262;
	private static final int[] RCP_REWARDS = new int[]
	{
		6881,
		6883,
		6885,
		6887,
		6891,
		6893,
		6895,
		6897,
		6899,
		7580
	};
	
	public Q620_FourGoblets()
	{
		super(620, "Four Goblets");
		registerQuestItems(SEALED_BOX, USED_GRAVE_PASS, GOBLET_OF_ALECTIA, GOBLET_OF_TISHAS, GOBLET_OF_MEKARA, GOBLET_OF_MORIGUL);
		addStartNpc(NAMELESS_SPIRIT, CONQUERORS_SEPULCHER_MANAGER, EMPERORS_SEPULCHER_MANAGER, GREAT_SAGES_SEPULCHER_MANAGER, JUDGES_SEPULCHER_MANAGER, GHOST_CHAMBERLAIN_1, GHOST_CHAMBERLAIN_2);
		addTalkId(NAMELESS_SPIRIT, CONQUERORS_SEPULCHER_MANAGER, EMPERORS_SEPULCHER_MANAGER, GREAT_SAGES_SEPULCHER_MANAGER, JUDGES_SEPULCHER_MANAGER, GHOST_CHAMBERLAIN_1, GHOST_CHAMBERLAIN_2, GHOST_OF_WIGOTH_1, GHOST_OF_WIGOTH_2);
		for (int id = 18120; id <= 18256; id++)
		{
			addKillId(id);
		}
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("31452-05.htm"))
		{
			if (Rnd.nextBoolean())
			{
				htmltext = (Rnd.nextBoolean()) ? "31452-03.htm" : "31452-04.htm";
			}
		}
		else if (event.equals("31452-06.htm"))
		{
			player.teleToLocation(169590, -90218, -2914); // Wigoth : Teleport back to Pilgrim's Temple
		}
		else if (event.equals("31453-13.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("31453-16.htm"))
		{
			if (st.hasQuestItems(GOBLET_OF_ALECTIA, GOBLET_OF_TISHAS, GOBLET_OF_MEKARA, GOBLET_OF_MORIGUL))
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(GOBLET_OF_ALECTIA, -1);
				st.takeItems(GOBLET_OF_TISHAS, -1);
				st.takeItems(GOBLET_OF_MEKARA, -1);
				st.takeItems(GOBLET_OF_MORIGUL, -1);
				st.giveItems(ANTIQUE_BROOCH, 1);
			}
			else
			{
				htmltext = "31453-14.htm";
			}
		}
		// else if (event.equals("31453-13.htm"))
		// {
		// if (st.getCond()s == 2)
		// {
		// htmltext = "31453-19.htm";
		// }
		// }
		else if (event.equals("31453-18.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equals("boxes"))
		{
			if (st.hasQuestItems(SEALED_BOX))
			{
				st.takeItems(SEALED_BOX, 1);
				if (!calculateBoxReward(st))
				{
					htmltext = (Rnd.nextBoolean()) ? "31454-09.htm" : "31454-10.htm";
				}
				else
				{
					htmltext = "31454-08.htm";
				}
			}
		}
		// Ghost Chamberlain of Elmoreden: Teleport to 4th sepulcher
		else if (event.equals("tele_4sep"))
		{
			if (st.hasQuestItems(ANTIQUE_BROOCH))
			{
				player.teleToLocation(178298, -84574, -7216);
				return null;
			}
			
			if (st.hasQuestItems(USED_GRAVE_PASS))
			{
				st.takeItems(USED_GRAVE_PASS, 1);
				player.teleToLocation(178298, -84574, -7216);
				return null;
			}
			htmltext = npc.getNpcId() + "-00.htm";
		}
		// Ghost Chamberlain of Elmoreden: Teleport to Imperial Tomb entrance
		else if (event.equals("tele_it"))
		{
			if (st.hasQuestItems(ANTIQUE_BROOCH))
			{
				player.teleToLocation(186942, -75602, -2834);
				return null;
			}
			
			if (st.hasQuestItems(USED_GRAVE_PASS))
			{
				st.takeItems(USED_GRAVE_PASS, 1);
				player.teleToLocation(186942, -75602, -2834);
				return null;
			}
			htmltext = npc.getNpcId() + "-00.htm";
		}
		else if (event.equals("31919-06.htm"))
		{
			if (st.hasQuestItems(SEALED_BOX))
			{
				st.takeItems(SEALED_BOX, 1);
				if (!calculateBoxReward(st))
				{
					htmltext = (Rnd.nextBoolean()) ? "31919-04.htm" : "31919-05.htm";
				}
				else
				{
					htmltext = "31919-03.htm";
				}
			}
		}
		// If event is a simple digit, parse it to get an integer form, then test the reward list
		else if (Util.isDigit(event))
		{
			final int id = Integer.parseInt(event);
			if (Util.contains(RCP_REWARDS, id) && (st.getQuestItemsCount(RELIC) >= 1000))
			{
				st.takeItems(RELIC, 1000);
				st.giveItems(id, 1);
			}
			htmltext = "31454-12.htm";
		}
		else if (event.equals("Enter"))
		{
			FourSepulchersManager.getInstance().tryEntry(npc, player);
			return null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		final int npcId = npc.getNpcId();
		final int cond = st.getCond();
		if (npcId == GHOST_OF_WIGOTH_1)
		{
			if (cond == 1)
			{
				htmltext = "31452-01.htm";
			}
			else if (cond == 2)
			{
				htmltext = "31452-02.htm";
			}
		}
		else if (npcId == NAMELESS_SPIRIT)
		{
			if (cond == 0)
			{
				htmltext = (player.getLevel() >= 74) ? "31453-01.htm" : "31453-12.htm";
			}
			else if (cond == 1)
			{
				htmltext = (st.hasQuestItems(GOBLET_OF_ALECTIA, GOBLET_OF_TISHAS, GOBLET_OF_MEKARA, GOBLET_OF_MORIGUL)) ? "31453-15.htm" : "31453-14.htm";
			}
			else if (cond == 2)
			{
				htmltext = "31453-17.htm";
			}
		}
		else if (npcId == GHOST_OF_WIGOTH_2)
		{
			// Possibilities : 0 = nothing, 1 = seal boxes only, 2 = relics only, 3 = both, 4/5/6/7 = "4 goblets" versions of 0/1/2/3.
			int index = 0;
			if (st.hasQuestItems(GOBLET_OF_ALECTIA, GOBLET_OF_TISHAS, GOBLET_OF_MEKARA, GOBLET_OF_MORIGUL))
			{
				index = 4;
			}
			
			final boolean gotSealBoxes = st.hasQuestItems(SEALED_BOX);
			final boolean gotEnoughRelics = st.getQuestItemsCount(RELIC) >= 1000;
			if (gotSealBoxes && gotEnoughRelics)
			{
				index += 3;
			}
			else if (!gotSealBoxes && gotEnoughRelics)
			{
				index += 2;
			}
			else if (gotSealBoxes)
			{
				index += 1;
			}
			
			htmltext = "31454-0" + index + ".htm";
		}
		else
		{
			htmltext = npcId + "-01.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		partyMember.getQuestState(getName()).dropItems(SEALED_BOX, 1, 0, 300000);
		return null;
	}
	
	/**
	 * Calculate boxes rewards, then return if there was a reward.
	 * @param st the QuestState of the player, used to reward him.
	 * @return true if there was a reward, false if not (used to call a "no-reward" html)
	 */
	private static boolean calculateBoxReward(QuestState st)
	{
		boolean reward = false;
		final int rnd = Rnd.get(5);
		if (rnd == 0)
		{
			st.giveItems(57, 10000);
			reward = true;
		}
		else if (rnd == 1)
		{
			if (Rnd.get(1000) < 848)
			{
				reward = true;
				final int i = Rnd.get(1000);
				if (i < 43)
				{
					st.giveItems(1884, 42);
				}
				else if (i < 66)
				{
					st.giveItems(1895, 36);
				}
				else if (i < 184)
				{
					st.giveItems(1876, 4);
				}
				else if (i < 250)
				{
					st.giveItems(1881, 6);
				}
				else if (i < 287)
				{
					st.giveItems(5549, 8);
				}
				else if (i < 484)
				{
					st.giveItems(1874, 1);
				}
				else if (i < 681)
				{
					st.giveItems(1889, 1);
				}
				else if (i < 799)
				{
					st.giveItems(1877, 1);
				}
				else if (i < 902)
				{
					st.giveItems(1894, 1);
				}
				else
				{
					st.giveItems(4043, 1);
				}
			}
			
			if (Rnd.get(1000) < 323)
			{
				reward = true;
				final int i = Rnd.get(1000);
				if (i < 335)
				{
					st.giveItems(1888, 1);
				}
				else if (i < 556)
				{
					st.giveItems(4040, 1);
				}
				else if (i < 725)
				{
					st.giveItems(1890, 1);
				}
				else if (i < 872)
				{
					st.giveItems(5550, 1);
				}
				else if (i < 962)
				{
					st.giveItems(1893, 1);
				}
				else if (i < 986)
				{
					st.giveItems(4046, 1);
				}
				else
				{
					st.giveItems(4048, 1);
				}
			}
		}
		else if (rnd == 2)
		{
			if (Rnd.get(1000) < 847)
			{
				reward = true;
				final int i = Rnd.get(1000);
				if (i < 148)
				{
					st.giveItems(1878, 8);
				}
				else if (i < 175)
				{
					st.giveItems(1882, 24);
				}
				else if (i < 273)
				{
					st.giveItems(1879, 4);
				}
				else if (i < 322)
				{
					st.giveItems(1880, 6);
				}
				else if (i < 357)
				{
					st.giveItems(1885, 6);
				}
				else if (i < 554)
				{
					st.giveItems(1875, 1);
				}
				else if (i < 685)
				{
					st.giveItems(1883, 1);
				}
				else if (i < 803)
				{
					st.giveItems(5220, 1);
				}
				else if (i < 901)
				{
					st.giveItems(4039, 1);
				}
				else
				{
					st.giveItems(4044, 1);
				}
			}
			
			if (Rnd.get(1000) < 251)
			{
				reward = true;
				final int i = Rnd.get(1000);
				if (i < 350)
				{
					st.giveItems(1887, 1);
				}
				else if (i < 587)
				{
					st.giveItems(4042, 1);
				}
				else if (i < 798)
				{
					st.giveItems(1886, 1);
				}
				else if (i < 922)
				{
					st.giveItems(4041, 1);
				}
				else if (i < 966)
				{
					st.giveItems(1892, 1);
				}
				else if (i < 996)
				{
					st.giveItems(1891, 1);
				}
				else
				{
					st.giveItems(4047, 1);
				}
			}
		}
		else if (rnd == 3)
		{
			if (Rnd.get(1000) < 31)
			{
				reward = true;
				final int i = Rnd.get(1000);
				if (i < 223)
				{
					st.giveItems(730, 1);
				}
				else if (i < 893)
				{
					st.giveItems(948, 1);
				}
				else
				{
					st.giveItems(960, 1);
				}
			}
			
			if (Rnd.get(1000) < 5)
			{
				reward = true;
				final int i = Rnd.get(1000);
				if (i < 202)
				{
					st.giveItems(729, 1);
				}
				else if (i < 928)
				{
					st.giveItems(947, 1);
				}
				else
				{
					st.giveItems(959, 1);
				}
			}
		}
		else if (rnd == 4)
		{
			if (Rnd.get(1000) < 329)
			{
				reward = true;
				final int i = Rnd.get(1000);
				if (i < 88)
				{
					st.giveItems(6698, 1);
				}
				else if (i < 185)
				{
					st.giveItems(6699, 1);
				}
				else if (i < 238)
				{
					st.giveItems(6700, 1);
				}
				else if (i < 262)
				{
					st.giveItems(6701, 1);
				}
				else if (i < 292)
				{
					st.giveItems(6702, 1);
				}
				else if (i < 356)
				{
					st.giveItems(6703, 1);
				}
				else if (i < 420)
				{
					st.giveItems(6704, 1);
				}
				else if (i < 482)
				{
					st.giveItems(6705, 1);
				}
				else if (i < 554)
				{
					st.giveItems(6706, 1);
				}
				else if (i < 576)
				{
					st.giveItems(6707, 1);
				}
				else if (i < 640)
				{
					st.giveItems(6708, 1);
				}
				else if (i < 704)
				{
					st.giveItems(6709, 1);
				}
				else if (i < 777)
				{
					st.giveItems(6710, 1);
				}
				else if (i < 799)
				{
					st.giveItems(6711, 1);
				}
				else if (i < 863)
				{
					st.giveItems(6712, 1);
				}
				else if (i < 927)
				{
					st.giveItems(6713, 1);
				}
				else
				{
					st.giveItems(6714, 1);
				}
			}
			
			if (Rnd.get(1000) < 54)
			{
				reward = true;
				final int i = Rnd.get(1000);
				if (i < 100)
				{
					st.giveItems(6688, 1);
				}
				else if (i < 198)
				{
					st.giveItems(6689, 1);
				}
				else if (i < 298)
				{
					st.giveItems(6690, 1);
				}
				else if (i < 398)
				{
					st.giveItems(6691, 1);
				}
				else if (i < 499)
				{
					st.giveItems(7579, 1);
				}
				else if (i < 601)
				{
					st.giveItems(6693, 1);
				}
				else if (i < 703)
				{
					st.giveItems(6694, 1);
				}
				else if (i < 801)
				{
					st.giveItems(6695, 1);
				}
				else if (i < 902)
				{
					st.giveItems(6696, 1);
				}
				else
				{
					st.giveItems(6697, 1);
				}
			}
		}
		
		return reward;
	}
}