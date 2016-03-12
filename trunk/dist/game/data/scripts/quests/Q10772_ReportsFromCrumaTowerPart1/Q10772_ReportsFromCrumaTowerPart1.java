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
package quests.Q10772_ReportsFromCrumaTowerPart1;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

/**
 * Reports from Cruma Tower, Part 1 (10772)
 * @URL https://l2wiki.com/Reports_from_Cruma_Tower,_Part_1
 * @author Gigi
 */
public class Q10772_ReportsFromCrumaTowerPart1 extends Quest
{
	// NPCs
	private static final int JANSSEN = 30484;
	private static final int MAGIC_OWL = 33991;
	// Items
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 4);
	private static final ItemHolder EAC = new ItemHolder(952, 2);
	// Reward
	private static final int EXP_REWARD = 127575;
	private static final int SP_REWARD = 30;
	// Misc
	private static final int MIN_LEVEL = 45;
	
	public Q10772_ReportsFromCrumaTowerPart1()
	{
		super(10772, Q10772_ReportsFromCrumaTowerPart1.class.getSimpleName(), "Reports from Cruma Tower, Part 1");
		addStartNpc(JANSSEN);
		addTalkId(JANSSEN, MAGIC_OWL);
		addCondMinLevel(MIN_LEVEL, "no_level.htm");
		addCondRace(Race.ERTHEIA, "noErtheya.html");
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
			case "30484-02.htm":
			case "30484-03.htm":
			case "30484-04.htm":
			case "30484-05.htm":
			case "33991-02.html":
			{
				htmltext = event;
				break;
			}
			case "30484-06.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30484-08.html":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAC);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
			}
			case "close":
			{
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 2036, 1, 1000, 0));
				qs.setCond(2, true);
				npc.deleteMe();
				break;
			}
			case "summon":
			{
				if (qs.isCond(1))
				{
					addSpawn(MAGIC_OWL, qs.getPlayer().getX(), qs.getPlayer().getY() + getRandom(50, 400), qs.getPlayer().getZ(), getRandom(64000), false, 60000);
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
			case JANSSEN:
			{
				if (qs.isCreated())
				{
					htmltext = "30484-01.htm";
				}
				else if (qs.isStarted())
				{
					if (qs.isCond(1))
					{
						htmltext = "30484-06.htm";
					}
				}
				if (qs.isCond(2))
				{
					htmltext = "30484-07.html";
				}
				if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case MAGIC_OWL:
			{
				if (qs.isCond(1))
				{
					htmltext = "33990-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
}