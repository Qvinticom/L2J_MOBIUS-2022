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
package quests.Q00255_Tutorial;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * Tutorial Quest
 * @author Mobius
 */
public class Q00255_Tutorial extends Quest
{
	public Q00255_Tutorial()
	{
		super(255, Q00255_Tutorial.class.getSimpleName(), "Tutorial");
		setIsCustom(true);
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
			case "tutorial_02.html":
			{
				htmltext = event;
				break;
			}
			case "user_connected":
			{
				// Start Newbie Tutorial
				if ((player.getLevel() < 6) && !qs.isCompleted())
				{
					startQuestTimer("start_newbie_tutorial", 5000, null, player);
				}
				break;
			}
			case "start_newbie_tutorial":
			{
				htmltext = player.getRace() == Race.ERTHEIA ? "tutorial_01_ertheia.html" : "tutorial_01.html";
				qs.exitQuest(false, false); // TODO: Continue with the tutorial.
				break;
			}
		}
		
		return htmltext;
	}
}
