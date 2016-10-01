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
package quests.Q10296_SevenSignsOneWhoSeeksThePowerOfTheSeal;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * Seven Signs, One Who Seeks the Power of the Seal (10296)
 * @URL https://l2wiki.com/Seven_Signs,_One_Who_Seeks_the_Power_of_the_Seal
 * @author Mobius
 */
public final class Q10296_SevenSignsOneWhoSeeksThePowerOfTheSeal extends Quest
{
	// NPCs
	private static final int ERIS_EVIL_THOUGHTS = 32792;
	// Misc
	// private static final int MIN_LEVEL = 81;
	
	public Q10296_SevenSignsOneWhoSeeksThePowerOfTheSeal()
	{
		super(10296);
		addStartNpc(ERIS_EVIL_THOUGHTS);
		addTalkId(ERIS_EVIL_THOUGHTS);
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
			
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		// QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			
		}
		return htmltext;
	}
}
