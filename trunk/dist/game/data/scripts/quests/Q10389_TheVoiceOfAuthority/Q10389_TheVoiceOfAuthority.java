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
package quests.Q10389_TheVoiceOfAuthority;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

import quests.Q10388_ConspiracyBehindDoor.Q10388_ConspiracyBehindDoor;

/**
 * @author hlwrave
 */
public class Q10389_TheVoiceOfAuthority extends Quest
{
	// NPCs
	private static final int RADZEN = 33803;
	// Monsters
	private static final int MOB_1 = 22139;
	private static final int MOB_2 = 22140;
	private static final int MOB_3 = 22141;
	private static final int MOB_4 = 22147;
	private static final int MOB_5 = 22154;
	private static final int MOB_6 = 22144;
	private static final int MOB_7 = 22145;
	private static final int MOB_8 = 22148;
	private static final int MOB_9 = 22142;
	private static final int MOB_10 = 22155;
	
	public Q10389_TheVoiceOfAuthority()
	{
		super(10389, Q10389_TheVoiceOfAuthority.class.getSimpleName(), "The Voice Of Authority");
		addCondCompletedQuest(Q10388_ConspiracyBehindDoor.class.getSimpleName(), "no_quest.html");
		addStartNpc(RADZEN);
		addTalkId(RADZEN);
		addKillId(MOB_1, MOB_2, MOB_3, MOB_4, MOB_5, MOB_6, MOB_7, MOB_8, MOB_9, MOB_10);
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
			case "accepted.html":
			{
				qs.startQuest();
				qs.set(Integer.toString(MOB_1), 0);
				qs.set(Integer.toString(MOB_2), 0);
				qs.set(Integer.toString(MOB_3), 0);
				qs.set(Integer.toString(MOB_4), 0);
				qs.set(Integer.toString(MOB_5), 0);
				qs.set(Integer.toString(MOB_6), 0);
				qs.set(Integer.toString(MOB_7), 0);
				qs.set(Integer.toString(MOB_8), 0);
				qs.set(Integer.toString(MOB_9), 0);
				qs.set(Integer.toString(MOB_10), 0);
				break;
			}
			case "endquest.html":
			{
				giveAdena(player, 1302720, true);
				giveItems(player, 8067, 1);
				addExpAndSp(player, 592767000, 142264);
				qs.exitQuest(false, true);
				htmltext = "endquest.html";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (qs.getState())
		{
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
			case State.CREATED:
			{
				if (player.getLevel() >= 97)
				{
					htmltext = "start.htm";
				}
				else
				{
					htmltext = "no_level.html";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "notcollected.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "collected.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isStarted() && qs.isCond(1) && Util.checkIfInRange(1500, npc, qs.getPlayer(), false))
		{
			int kills = qs.getInt(Integer.toString(MOB_1));
			kills++;
			qs.set(Integer.toString(MOB_1), kills);
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpcString(NpcStringId.ELIMINATE_THE_PAGANS_IN_THE_ANTEROOM, kills);
			killer.sendPacket(log);
			
			if (kills >= 30)
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}