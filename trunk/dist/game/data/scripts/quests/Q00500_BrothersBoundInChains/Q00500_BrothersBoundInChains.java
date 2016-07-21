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
package quests.Q00500_BrothersBoundInChains;

import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.CharEffectList;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.npc.OnAttackableKill;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * @author Mathael
 * @URL https://l2wiki.com/Brothers_Bound_in_Chains
 * @version Infinite Odyssey
 */
public class Q00500_BrothersBoundInChains extends Quest
{
	// NPCs
	private static final int DARK_JUDGE = 30981;
	// Items
	private static final int GEMSTONE_B = 2132;
	private static final int PENITENT_MANACLES = 36060; // TODO: check: multiple items with this name
	private static final int CRUMBS_OF_PENITENCE = 36077;
	private static final int DROP_QI_CHANCE = 1; // in % TODO: check
	// Misc
	private static final int HOUR_OF_PENITENCE[] =
	{
		15325,
		15326,
		15327,
		15328,
		15329
	};
	private static final int MIN_LEVEL = 60;
	
	public Q00500_BrothersBoundInChains()
	{
		super(500);
		addStartNpc(DARK_JUDGE);
		addTalkId(DARK_JUDGE);
		registerQuestItems(PENITENT_MANACLES, CRUMBS_OF_PENITENCE);
		addCondMinLevel(MIN_LEVEL, "30981-nopk.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "30981-02.htm":
			case "30981-03.htm":
			{
				break;
			}
			case "30981-04.htm":
			{
				if (takeItems(player, GEMSTONE_B, 200))
				{
					giveItems(player, PENITENT_MANACLES, 1);
				}
				else
				{
					event = "30981-05.html";
				}
				break;
			}
			case "30981-06.htm":
			{
				qs.startQuest();
				break;
			}
			case "30981-09.html": // not retail html.
			{
				if (takeItems(player, CRUMBS_OF_PENITENCE, 10))
				{
					player.setPkKills(Math.max(0, player.getPkKills() - Rnd.get(1, 10)));
					qs.exitQuest(QuestType.DAILY, true);
				}
				else
				{
					// If player delete QuestItems: Need check how it work on retail.
					qs.setCond(1);
					event = "30981-07.html";
				}
				break;
			}
			default:
			{
				event = getNoQuestMsg(player);
			}
		}
		
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);
		final QuestState qs = getQuestState(talker, true);
		
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = (talker.getPkKills() > 0) && (talker.getReputation() >= 0) ? "30981-01.htm" : "30981-nopk.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30981-07.html";
						break;
					}
					case 2:
					{
						htmltext = "30981-08.html"; // not retail html.
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (qs.isNowAvailable())
				{
					qs.setState(State.CREATED);
					htmltext = "30981-01.htm";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_ATTACKABLE_KILL)
	@RegisterType(ListenerRegisterType.GLOBAL_MONSTERS)
	public void onAttackableKill(OnAttackableKill event)
	{
		final QuestState qs = getQuestState(event.getAttacker(), false);
		if (qs == null)
		{
			return;
		}
		
		// Player can drop more than 10 Crumbs of Penitence but there's no point in getting more than 10 (retail)
		boolean isAffectedByHourOfPenitence = false;
		final CharEffectList effects = event.getAttacker().getEffectList();
		for (int i = 0; !isAffectedByHourOfPenitence && (i < HOUR_OF_PENITENCE.length); i++)
		{
			if (effects.isAffectedBySkill(HOUR_OF_PENITENCE[i]))
			{
				isAffectedByHourOfPenitence = true;
			}
		}
		
		if (isAffectedByHourOfPenitence)
		{
			if (Rnd.get(1, 100) <= DROP_QI_CHANCE)
			{
				giveItems(event.getAttacker(), CRUMBS_OF_PENITENCE, 1);
				if (!qs.isCond(2) && (getQuestItemsCount(event.getAttacker(), CRUMBS_OF_PENITENCE) >= 10))
				{
					qs.setCond(2, true);
				}
			}
		}
	}
}
