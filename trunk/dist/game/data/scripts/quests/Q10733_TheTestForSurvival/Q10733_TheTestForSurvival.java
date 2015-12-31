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
package quests.Q10733_TheTestForSurvival;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

import quests.Q10732_AForeignLand.Q10732_AForeignLand;

/**
 * @author Sdw
 */
public class Q10733_TheTestForSurvival extends Quest
{
	// NPC's
	private static final int GERETH = 33932;
	private static final int DIA = 34005;
	private static final int KATALIN = 33943;
	private static final int AYANTHE = 33942;
	// Items
	private static final int GERETH_RECOMMENDATION = 39519;
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10733_TheTestForSurvival()
	{
		super(10733, Q10733_TheTestForSurvival.class.getSimpleName(), "The Test for Survival");
		addStartNpc(GERETH);
		addTalkId(GERETH, DIA, KATALIN, AYANTHE);
		registerQuestItems(GERETH_RECOMMENDATION);
		addCondMaxLevel(MAX_LEVEL, "33932-04.html");
		addCondRace(Race.ERTHEIA, "33932-04.html");
		addCondCompletedQuest(Q10732_AForeignLand.class.getSimpleName(), "33932-04.html");
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
			case "33932-02.html":
			{
				qs.startQuest();
				player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_027_Quest_01.htm", TutorialShowHtml.LARGE_WINDOW));
				qs.giveItems(GERETH_RECOMMENDATION, 1);
				htmltext = event;
				break;
			}
			case "34005-03.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34005-06.html":
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "33942-01.html":
			case "33943-01.html":
			case "34005-02.html":
			case "34005-05.html":
			{
				htmltext = event;
				break;
			}
			case "33942-02.html":
			case "33943-02.html":
			{
				if (qs.isCond(2) || qs.isCond(3))
				{
					giveAdena(player, 5000, true);
					addExpAndSp(player, 295, 2);
					qs.exitQuest(false, true);
					htmltext = event;
				}
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
			case GERETH:
			{
				if (qs.isCreated())
				{
					htmltext = "33932-01.htm";
				}
				else if (qs.isStarted())
				{
					htmltext = "33932-03.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case DIA:
			{
				if (qs.isStarted() && qs.hasQuestItems(GERETH_RECOMMENDATION))
				{
					if (player.getClassId() == ClassId.ERTHEIA_FIGHTER)
					{
						htmltext = "34005-01.html";
					}
					else if (player.getClassId() == ClassId.ERTHEIA_WIZARD)
					{
						htmltext = "34005-04.html";
					}
				}
				break;
			}
			case KATALIN:
			{
				if (qs.isCond(2) && qs.hasQuestItems(GERETH_RECOMMENDATION))
				{
					htmltext = "33943-01.html";
				}
				break;
			}
			case AYANTHE:
			{
				if (qs.isCond(3) && qs.hasQuestItems(GERETH_RECOMMENDATION))
				{
					htmltext = "33942-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
}