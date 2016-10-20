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
package quests.Q10770_InSearchOfTheGrail;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * In Search of the Grail (10770)
 * @URL https://l2wiki.com/In_Search_of_the_Grail
 * @author Gigi
 */
public class Q10770_InSearchOfTheGrail extends Quest
{
	// NPCs
	private static final int LORAIN = 30673;
	private static final int JANSSEN = 30484;
	// Monsters
	private static final int[] MONSTERS =
	{
		20213, // Porta
		20214, // Excuro
		20216, // Ricenseo
		20217, // Krator
		21036, // Shindebarn
	};
	// Items
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 30);
	private static final ItemHolder EWC = new ItemHolder(951, 1);
	private static final ItemHolder EAC = new ItemHolder(952, 2);
	private static final int SHINING_MYSTERIOUS = 39711;
	// Reward
	private static final int EXP_REWARD = 2342300;
	private static final int SP_REWARD = 562;
	// Misc
	private static final int MIN_LEVEL = 40;
	
	public Q10770_InSearchOfTheGrail()
	{
		super(10770, Q10770_InSearchOfTheGrail.class.getSimpleName(), "In Search of the Grail");
		addStartNpc(LORAIN);
		addTalkId(LORAIN, JANSSEN);
		registerQuestItems(SHINING_MYSTERIOUS);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "noLevel.html");
		addCondRace(Race.ERTHEIA, "noErtheya.html");
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
			case "30673-02.htm":
			case "30673-03.htm":
			case "30673-04.htm":
			{
				htmltext = event;
				break;
			}
			case "30673-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				qs.set(Integer.toString(SHINING_MYSTERIOUS), 0);
				break;
			}
			case "30484-02.html":
			{
				takeItems(player, SHINING_MYSTERIOUS, 30);
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "30484-04.html":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EWC);
				giveItems(player, EAC);
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
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (npc.getId())
		{
			case LORAIN:
			{
				if (qs.isCreated())
				{
					htmltext = "30673-01.htm";
				}
				else if (qs.isStarted())
				{
					if (qs.isCond(1))
					{
						htmltext = "30673-06.html";
					}
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case JANSSEN:
			{
				if (qs.isCond(2))
				{
					htmltext = "30484-01.html";
				}
				else if (qs.isCond(3))
				{
					htmltext = "30484-03.html";
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
		
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, npc, SHINING_MYSTERIOUS, 1, 30, 0.5, true))
		{
			qs.setCond(2, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}