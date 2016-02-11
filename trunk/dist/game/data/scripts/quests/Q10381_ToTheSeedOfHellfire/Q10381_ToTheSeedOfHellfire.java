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
package quests.Q10381_ToTheSeedOfHellfire;

import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * @author hlwrave
 */
public class Q10381_ToTheSeedOfHellfire extends Quest
{
	// NPCS
	private static final int KEUCEREUS = 32548;
	private static final int KBALDIR = 32733;
	private static final int SIZRAK = 33669;
	// Items
	private static final int KBALDIRS_LETTER = 34957;
	// Misc
	private static final int MIN_LEVEL = 97;
	
	public Q10381_ToTheSeedOfHellfire()
	{
		super(10381, Q10381_ToTheSeedOfHellfire.class.getSimpleName(), "To the Seed of Hellfire");
		addStartNpc(KEUCEREUS);
		addTalkId(KEUCEREUS, KBALDIR, SIZRAK);
		registerQuestItems(KBALDIRS_LETTER);
		addCondMinLevel(MIN_LEVEL, "kserth_q10381_04.html");
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
			case "kserth_q10381_03.html":
			{
				qs.startQuest();
				break;
			}
			case "kbarldire_q10381_03.html":
			{
				qs.setCond(2);
				giveItems(player, KBALDIRS_LETTER, 1);
				break;
			}
			case "sofa_sizraku_q10381_03.html":
			{
				takeItems(player, KBALDIRS_LETTER, -1);
				addExpAndSp(player, 951127800, 435041400);
				giveAdena(player, 3256740, true);
				qs.exitQuest(QuestType.ONE_TIME, true);
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
			case KEUCEREUS:
			{
				if (qs.isCreated())
				{
					htmltext = "kserth_q10381_01.htm";
				}
				else if (qs.isStarted())
				{
					htmltext = "kserth_q10381_06.html";
					
				}
				else if (qs.isCompleted())
				{
					htmltext = "kserth_q10381_05.html";
				}
				break;
			}
			case KBALDIR:
			{
				if (qs.isCond(1))
				{
					htmltext = "kbarldire_q10381_01.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "kbarldire_q10381_04.html";
				}
				break;
			}
			case SIZRAK:
			{
				if (qs.isCond(2))
				{
					htmltext = "sofa_sizraku_q10381_01.html";
				}
				break;
			}
		}
		return htmltext;
	}
}