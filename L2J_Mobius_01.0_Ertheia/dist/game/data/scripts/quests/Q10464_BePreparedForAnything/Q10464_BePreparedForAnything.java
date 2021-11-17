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
package quests.Q10464_BePreparedForAnything;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

/**
 * Be Prepared for Anything (10464)
 * @author St3eT
 */
public class Q10464_BePreparedForAnything extends Quest
{
	// NPCs
	private static final int PATERSON = 33864;
	private static final int OLF_KANORE = 32610;
	// Items
	private static final int BELT = 13894; // Cloth Belt
	private static final int MAGIC_PIN = 36725; // Practice Magic Pin (C-grade)
	private static final int LEATHER_BELT = 36724; // Practice Leather Belt
	private static final int LEATHER_BELT_FINISHED = 36726; // Flutter's Magic Pin Leather Belt (Low-grade)
	// Misc
	private static final int MIN_LEVEL = 58;
	private static final int MAX_LEVEL = 65;
	
	public Q10464_BePreparedForAnything()
	{
		super(10464);
		addStartNpc(PATERSON);
		addTalkId(PATERSON, OLF_KANORE);
		registerQuestItems(MAGIC_PIN, LEATHER_BELT, LEATHER_BELT_FINISHED);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33864-08.htm");
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
			case "33864-02.htm":
			case "33864-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33864-04.htm":
			{
				qs.startQuest();
				giveItems(player, MAGIC_PIN, 1);
				htmltext = event;
				break;
			}
			case "32610-02.html":
			{
				if (qs.isCond(1))
				{
					if (!hasQuestItems(player, LEATHER_BELT))
					{
						giveItems(player, LEATHER_BELT, 1);
					}
					htmltext = event;
				}
				break;
			}
			case "32610-03.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "32610-04.html":
			{
				if (qs.isCond(2))
				{
					player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2text\\QT_024_belt_01.htm", TutorialShowHtml.LARGE_WINDOW));
					htmltext = event;
				}
				break;
			}
			case "32610-07.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
					htmltext = event;
				}
				break;
			}
			case "33864-07.html":
			{
				if (qs.isCond(3))
				{
					qs.exitQuest(false, true);
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 781_410, 187);
					}
					giveItems(player, BELT, 1);
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
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == PATERSON)
				{
					htmltext = "33864-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = npc.getId() == PATERSON ? "33864-05.html" : "32610-01.html";
				}
				else if (qs.isCond(2) && (npc.getId() == OLF_KANORE))
				{
					htmltext = hasQuestItems(player, LEATHER_BELT_FINISHED) ? "32610-06.html" : "32610-05.html";
				}
				else if (qs.isCond(3))
				{
					htmltext = npc.getId() == PATERSON ? "33864-06.html" : "32610-08.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == PATERSON)
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
		}
		return htmltext;
	}
}