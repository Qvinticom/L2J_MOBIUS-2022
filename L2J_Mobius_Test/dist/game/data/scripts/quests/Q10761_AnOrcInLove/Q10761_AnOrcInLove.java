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
package quests.Q10761_AnOrcInLove;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

/**
 * An Orc in Love (10761)
 * @author Stayway
 */
public class Q10761_AnOrcInLove extends Quest
{
	// NPC
	private static final int VORBOS = 33966;
	// Monsters
	private static final int TUREK_WAR_HOUND = 20494;
	private static final int TUREK_ORC_FOOTMAN = 20499;
	private static final int TUREK_ORC_SENTINEL = 20500;
	private static final int TUREK_ORC_SUPPLIER = 20498;
	private static final int TUREK_ORC_ARCHER = 20496;
	private static final int TUREK_ORC_SKIRMISHER = 20497;
	private static final int TUREK_ORC_PRIEST = 20501;
	private static final int TUREK_ORC_PREFECT = 20495;
	private static final int TUREK_ORC_ELDER = 20546;
	// Item
	private static final ItemHolder GUILD_COIN = new ItemHolder(37045, 20);
	// Rewards
	private static final int EXP_REWARD = 354546;
	private static final int SP_REWARD = 85;
	// Other
	private static final int MIN_LEVEL = 30;
	
	public Q10761_AnOrcInLove()
	{
		super(10761, Q10761_AnOrcInLove.class.getSimpleName(), "An Orc in Love");
		addStartNpc(VORBOS);
		addTalkId(VORBOS);
		addKillId(TUREK_WAR_HOUND, TUREK_ORC_FOOTMAN, TUREK_ORC_SENTINEL, TUREK_ORC_SUPPLIER, TUREK_ORC_ARCHER, TUREK_ORC_SKIRMISHER, TUREK_ORC_PRIEST, TUREK_ORC_PREFECT, TUREK_ORC_ELDER);
		addCondMinLevel(MIN_LEVEL, "no_level.htm");
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
			case "33966-02.htm":
			case "33966-03.htm":
			case "33966-04.htm":
			{
				htmltext = event;
				break;
			}
			case "33966-05.html": // start the quest
			{
				qs.startQuest();
				qs.set(Integer.toString(TUREK_ORC_ELDER), 0);
				htmltext = event;
				break;
			}
			case "33966-07.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, GUILD_COIN);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.unset(Integer.toString(TUREK_ORC_ELDER));
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
		String htmltext = null;
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (player.getRace() != Race.ERTHEIA)
				{
					htmltext = "noErtheia.html";
				}
				else
				{
					htmltext = "33966-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33966-08.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "33966-06.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
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
			int kills = qs.getInt(Integer.toString(TUREK_ORC_ELDER));
			kills++;
			qs.set(Integer.toString(TUREK_ORC_ELDER), kills);
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpcString(NpcStringId.KILL_TUREK_ORCS, kills);
			killer.sendPacket(log);
			
			if (kills >= 30)
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
