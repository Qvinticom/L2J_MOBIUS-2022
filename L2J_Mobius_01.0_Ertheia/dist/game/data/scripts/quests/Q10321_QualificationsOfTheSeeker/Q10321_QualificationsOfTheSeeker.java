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
package quests.Q10321_QualificationsOfTheSeeker;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

import quests.Q10320_LetsGoToTheCentralSquare.Q10320_LetsGoToTheCentralSquare;

/**
 * Qualifications Of The Seeker (10321)
 * @author ivantotov, Gladicek
 */
public class Q10321_QualificationsOfTheSeeker extends Quest
{
	// NPCs
	private static final int SHANNON = 32974;
	private static final int THEODORE = 32975;
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10321_QualificationsOfTheSeeker()
	{
		super(10321);
		addStartNpc(THEODORE);
		addTalkId(THEODORE, SHANNON);
		addCondMaxLevel(MAX_LEVEL, "32975-01a.html");
		addCondCompletedQuest(Q10320_LetsGoToTheCentralSquare.class.getSimpleName(), "32975-01a.html");
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
			case "32975-03.html":
			{
				qs.startQuest();
				qs.setCond(2); // arrow hack
				qs.setCond(1);
				player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_027_Quest_01.htm", TutorialShowHtml.LARGE_WINDOW));
				htmltext = event;
				break;
			}
			case "32975-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32974-02.html":
			{
				if (qs.isStarted())
				{
					giveAdena(player, 50, true);
					addExpAndSp(player, 40, 5);
					qs.exitQuest(false, true);
					npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.HM_DON_T_JUST_GO_I_STILL_HAVE_TONS_TO_TEACH_YOU);
					htmltext = event;
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
		String htmltext = null;
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = npc.getId() == THEODORE ? "32975-01.htm" : "32974-04.html";
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == THEODORE ? "32975-04.html" : "32974-01.html";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == THEODORE ? "32975-05.html" : "32974-03.html";
				break;
			}
		}
		return htmltext;
	}
}