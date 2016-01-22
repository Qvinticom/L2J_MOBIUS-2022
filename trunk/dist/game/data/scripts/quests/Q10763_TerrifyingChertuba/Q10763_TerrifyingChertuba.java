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
package quests.Q10763_TerrifyingChertuba;

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

import quests.Q10762_MarionetteSpirit.Q10762_MarionetteSpirit;

/**
 * Terrifying Chertuba (10763)
 * @author Stayway
 */
public class Q10763_TerrifyingChertuba extends Quest
{
	// NPC
	private static final int VORBOS = 33966;
	// Monsters
	private static final int CHERTUBA_MIRAGE = 23421;
	private static final int CHERTUBA_ILLUSION = 23422;
	// Items
	private static final ItemHolder GUILD_COIN = new ItemHolder(37045, 5);
	private static final int MAGIC_CHAIN_KEY_2 = 39489;
	// Rewards
	private static final int EXP_REWARD = 896996;
	private static final int SP_REWARD = 215;
	// Other
	private static final int MIN_LEVEL = 34;
	
	public Q10763_TerrifyingChertuba()
	{
		super(10763, Q10763_TerrifyingChertuba.class.getSimpleName(), "Terrifying Chertuba");
		addStartNpc(VORBOS);
		addTalkId(VORBOS);
		addKillId(CHERTUBA_MIRAGE, CHERTUBA_ILLUSION);
		registerQuestItems(MAGIC_CHAIN_KEY_2);
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondMinLevel(MIN_LEVEL, "no_level.htm");
		addCondCompletedQuest(Q10762_MarionetteSpirit.class.getSimpleName(), "restriction.html");
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
			{
				htmltext = event;
				break;
			}
			case "33966-03.htm": // start the quest
			{
				qs.startQuest();
				qs.set(Integer.toString(CHERTUBA_ILLUSION), 0);
				qs.set(Integer.toString(CHERTUBA_MIRAGE), 0);
				htmltext = event;
				break;
			}
			case "33966-05.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, GUILD_COIN);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.unset(Integer.toString(CHERTUBA_ILLUSION));
					qs.unset(Integer.toString(CHERTUBA_MIRAGE));
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
					htmltext = "33966-06.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "33966-04.html";
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
			giveItemRandomly(killer, npc, MAGIC_CHAIN_KEY_2, 1, 1, 1.0, true);
			int kills = qs.getInt(Integer.toString(CHERTUBA_ILLUSION));
			kills++;
			qs.set(Integer.toString(CHERTUBA_ILLUSION), kills);
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpcString(NpcStringId.CHERTUBA_S_ILLUSION_OR_CHERTUBA_S_MIRAGE, kills);
			killer.sendPacket(log);
			
			if (kills >= 1)
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
