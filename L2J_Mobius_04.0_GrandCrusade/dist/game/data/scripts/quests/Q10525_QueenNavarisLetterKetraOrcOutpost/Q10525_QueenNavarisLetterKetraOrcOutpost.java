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
package quests.Q10525_QueenNavarisLetterKetraOrcOutpost;

import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.LetterQuest;

/**
 * Queen Navari's Letter, Ketra Orc Outpost (10525)
 * @URL https://l2wiki.com/Queen_Navari%27s_Letter,_Ketra_Orc_Outpost
 * @author Mobius
 */
public class Q10525_QueenNavarisLetterKetraOrcOutpost extends LetterQuest
{
	// NPCs
	private static final int GREGORY = 31279;
	private static final int LUGONNES = 33852;
	// Items
	private static final int KETRA_ORC_OUTPOST = 46732;
	private static final int SOE_TOWN_OF_GODDARD = 46731;
	// Misc
	private static final int MIN_LEVEL = 76;
	private static final int MAX_LEVEL = 80;
	// Teleport
	private static final Location TELEPORT_LOC = new Location(147711, -52920, -2728);
	
	public Q10525_QueenNavarisLetterKetraOrcOutpost()
	{
		super(10525);
		addTalkId(GREGORY, LUGONNES);
		setIsErtheiaQuest(true);
		setLevel(MIN_LEVEL, MAX_LEVEL);
		setStartLocation(SOE_TOWN_OF_GODDARD, TELEPORT_LOC);
		setStartQuestSound("Npcdialog1.serenia_quest_13");
		registerQuestItems(SOE_TOWN_OF_GODDARD, KETRA_ORC_OUTPOST);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "31279-02.htm":
			{
				htmltext = event;
				break;
			}
			case "31279-03.htm":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
					giveItems(player, KETRA_ORC_OUTPOST, 1);
					htmltext = event;
				}
				break;
			}
			case "33852-02.htm":
			{
				if (qs.isCond(3))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 1277640, 306);
						giveStoryQuestReward(npc, player);
						showOnScreenMsg(player, NpcStringId.YOU_HAVE_FINISHED_ALL_OF_QUEEN_NAVARI_S_LETTERS_GROW_STRONGER_HERE_UNTIL_YOU_RECEIVE_LETTERS_FROM_A_MINSTREL_AT_LV_85, ExShowScreenMessage.TOP_CENTER, 8000);
						qs.exitQuest(QuestType.ONE_TIME, true);
						htmltext = event;
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
		if (qs.isStarted())
		{
			if (npc.getId() == GREGORY)
			{
				htmltext = (qs.isCond(2)) ? "31279-01.htm" : "31279-04.html";
			}
			else if (qs.isCond(3))
			{
				htmltext = "33852-01.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public boolean canShowTutorialMark(Player player)
	{
		return player.getClassId() == ClassId.STRATOMANCER;
	}
}
