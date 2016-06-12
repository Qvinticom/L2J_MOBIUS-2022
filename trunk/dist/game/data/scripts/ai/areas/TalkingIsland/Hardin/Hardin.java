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
package ai.areas.TalkingIsland.Hardin;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.QuestState;

import ai.AbstractNpcAI;
import quests.Q10472_WindsOfFateEncroachingShadows.Q10472_WindsOfFateEncroachingShadows;

/**
 * Hardin AI.
 * @author malyelfik
 */
public final class Hardin extends AbstractNpcAI
{
	// NPC
	private static final int HARDIN = 33870;
	
	private Hardin()
	{
		addStartNpc(HARDIN);
		addFirstTalkId(HARDIN);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "33870-01.html"; // Anybody except Ertheia race TODO: Find HTML
		if (player.getRace().equals(Race.ERTHEIA))
		{
			final QuestState st = player.getQuestState(Q10472_WindsOfFateEncroachingShadows.class.getSimpleName());
			htmltext = ((st != null) && (st.getCond() >= 7) && (st.getCond() <= 17)) ? "33870-03.html" : "33870-02.html";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Hardin();
	}
}