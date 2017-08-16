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
package quests.Q10790_AMercenaryHelper;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

/**
 * A Mercenary Helper (10790)
 * @author Stayway
 */
public class Q10790_AMercenaryHelper extends Quest
{
	// NPC
	private static final int DOKARA = 33847;
	// Monsters
	private static final int SPLINTER_STAKATO = 21508;
	private static final int SPLINTER_STAKATO_WORKER = 21509;
	private static final int SPLINTER_STAKATO_SOLDIER = 21510;
	private static final int SPLINTER_STAKATO_DRONE = 21511;
	private static final int NEEDLE_STAKATO = 21513;
	private static final int NEEDLE_STAKATO_WORKER = 21514;
	private static final int NEEDLE_STAKATO_SOLDIER = 21515;
	private static final int NEEDLE_STAKATO_DRONE = 21516;
	private static final Map<Integer, Integer> MOBS_REQUIRED = new HashMap<>();
	{
		MOBS_REQUIRED.put(SPLINTER_STAKATO, 50);
	}
	// Item
	private static final ItemHolder GUILD_COIN = new ItemHolder(37045, 3);
	private static final ItemHolder ENCHANT_ARMOR_A = new ItemHolder(26351, 3);
	// Rewards
	private static final int EXP_REWARD = 942690;
	private static final int SP_REWARD = 226;
	// Other
	private static final int MIN_LEVEL = 65;
	private static final int MAX_LEVEL = 70;
	
	public Q10790_AMercenaryHelper()
	{
		super(10790);
		addStartNpc(DOKARA);
		addTalkId(DOKARA);
		addKillId(SPLINTER_STAKATO, SPLINTER_STAKATO_WORKER, SPLINTER_STAKATO_SOLDIER, SPLINTER_STAKATO_DRONE, NEEDLE_STAKATO, NEEDLE_STAKATO_WORKER, NEEDLE_STAKATO_SOLDIER, NEEDLE_STAKATO_DRONE);
		addCondMinLevel(MIN_LEVEL, "no_level.htm");
		addCondRace(Race.ERTHEIA, "no Ertheia.html");
		addCondClassId(ClassId.MARAUDER, "no_class.html");
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
			case "33847-02.htm":
			case "33847-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33847-04.htm": // start the quest
			{
				qs.startQuest();
				qs.set(Integer.toString(SPLINTER_STAKATO), 0);
				htmltext = event;
				break;
			}
			case "33847-07.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, GUILD_COIN);
					giveItems(player, ENCHANT_ARMOR_A);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
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
				if ((player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL))
				{
					htmltext = "no_level.html";
				}
				else
				{
					htmltext = "33847-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33847-05.html"; // Need find proper html
				}
				else if (qs.isCond(2))
				{
					htmltext = "33847-06.html";
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
			int kills = 0;
			switch (npc.getId())
			{
				case SPLINTER_STAKATO:
				case SPLINTER_STAKATO_WORKER:
				case SPLINTER_STAKATO_SOLDIER:
				case SPLINTER_STAKATO_DRONE:
				case NEEDLE_STAKATO:
				case NEEDLE_STAKATO_WORKER:
				case NEEDLE_STAKATO_SOLDIER:
				case NEEDLE_STAKATO_DRONE:
				{
					kills = qs.getInt(Integer.toString(SPLINTER_STAKATO));
					kills++;
					qs.set(Integer.toString(SPLINTER_STAKATO), kills);
					break;
				}
			}
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(SPLINTER_STAKATO, qs.getInt(Integer.toString(SPLINTER_STAKATO)));
			log.addNpcString(NpcStringId.KILL_STAKATOS, qs.getInt(Integer.toString(SPLINTER_STAKATO)));
			killer.sendPacket(log);
			
			if ((qs.getInt(Integer.toString(SPLINTER_STAKATO)) >= MOBS_REQUIRED.get(SPLINTER_STAKATO)) && (qs.getInt(Integer.toString(SPLINTER_STAKATO)) >= MOBS_REQUIRED.get(SPLINTER_STAKATO)))
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
