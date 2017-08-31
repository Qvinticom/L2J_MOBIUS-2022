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
package quests.Q10712_TheMinstrelsSongPart1;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.NpcStringId;

import quests.LetterQuest;

/**
 * The Minstrel's Song, Part 1 (10712)
 * @URL https://l2wiki.com/The_Minstrel%27s_Song,_Part_1
 * @author Gigi
 */
public final class Q10712_TheMinstrelsSongPart1 extends LetterQuest
{
	// NPCs
	private static final int LIBERATTO = 33955;
	// Items
	private static final int SOE_TALKING_ISLAND = 39553;
	// Location
	private static final Location TELEPORT_LOC = new Location(-113914, 255797, -1504);
	// Misc
	private static final int MIN_LEVEL = 85;
	private static final int MAX_LEVEL = 87;
	
	public Q10712_TheMinstrelsSongPart1()
	{
		super(10712);
		addTalkId(LIBERATTO);
		setLevel(MIN_LEVEL, MAX_LEVEL);
		setStartLocation(SOE_TALKING_ISLAND, TELEPORT_LOC);
		registerQuestItems(SOE_TALKING_ISLAND);
		setStartMessage(NpcStringId.THIS_IS_THIS_IS_A_GREAT_ACHIEVEMENT_THAT_IS_WORTHY_OF_THE_TRUE_HEROES_OF_LEGEND);
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
			case "33955-02.html":
			{
				htmltext = event;
				break;
			}
			case "33955-03.html":
			{
				giveStoryQuestReward(player, 20);
				addExpAndSp(player, 23297400, 5591);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		String htmltext = getNoQuestMsg(player);
		
		if (qs.isStarted() && (npc.getId() == LIBERATTO))
		{
			htmltext = "33955-01.html";
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
}