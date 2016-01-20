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
package quests.Q10758_TheOathOfTheWind;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

import quests.Q10757_QuietingTheStorm.Q10757_QuietingTheStorm;

/**
 * The Oath of the Wind (10758)
 * @author Stayway
 */
public class Q10758_TheOathOfTheWind extends Quest
{
	// NPC
	private static final int PIO = 33963;
	// Monster
	private static final int WINDIMA_CLONE = 27522;
	// Items
	private static final ItemHolder GUILD_COIN = new ItemHolder(37045, 7);
	// Rewards
	private static final int EXP_REWARD = 561645;
	private static final int SP_REWARD = 134;
	// Others
	private static final int MIN_LEVEL = 28;
	
	public Q10758_TheOathOfTheWind()
	{
		super(10758, Q10758_TheOathOfTheWind.class.getSimpleName(), "The Oath of the Wind");
		addStartNpc(PIO);
		addTalkId(PIO);
		addKillId(WINDIMA_CLONE);
		addCondMinLevel(MIN_LEVEL, "no_level.htm");
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondCompletedQuest(Q10757_QuietingTheStorm.class.getSimpleName(), "restriction.html");
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
			case "33963-02.htm":
			case "33963-03.htm":
			case "33963-04.htm":
			{
				htmltext = event;
				break;
			}
			case "accept": // start the quest
			{
				qs.startQuest();
				qs.set(Integer.toString(WINDIMA_CLONE), 0);
				npc = addSpawn(WINDIMA_CLONE, -93534, 89674, -3216, 0, false, 0, false, player.getInstanceId());
				startQuestTimer("windima_despawn", 100000, null, player);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				htmltext = event;
				break;
			}
			case "33963-05.htm":
			{
				if (qs.isCond(2))
				{
					giveItems(player, GUILD_COIN);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.unset(Integer.toString(WINDIMA_CLONE));
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
					htmltext = "33963-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33963-06.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "33963-04.html";
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
		if ((qs != null) && qs.isStarted() && qs.isCond(1) && (Util.checkIfInRange(1500, npc, qs.getPlayer(), false)))
		{
			int kills = qs.getInt(Integer.toString(WINDIMA_CLONE));
			kills++;
			qs.set(Integer.toString(WINDIMA_CLONE), kills);
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(WINDIMA_CLONE, qs.getInt(Integer.toString(WINDIMA_CLONE)));
			killer.sendPacket(log);
			
			if (qs.getInt(Integer.toString(WINDIMA_CLONE)) >= 1)
			{
				qs.setCond(2);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
