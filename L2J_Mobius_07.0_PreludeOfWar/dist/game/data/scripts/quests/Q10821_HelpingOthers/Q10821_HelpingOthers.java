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
package quests.Q10821_HelpingOthers;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.npc.OnAttackableKill;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

import quests.Q10817_ExaltedOneWhoOvercomesTheLimit.Q10817_ExaltedOneWhoOvercomesTheLimit;

/**
 * Helping Others (10821)
 * @URL https://l2wiki.com/Helping_Others
 * @author Mobius
 */
public class Q10821_HelpingOthers extends Quest
{
	// NPC
	private static final int SIR_KRISTOF_RODEMAI = 30756;
	// Items
	private static final int MENTEE_MARK = 33804;
	private static final int DAICHIR_CERTIFICATE = 45628;
	private static final int OLYMPIAD_MANAGER_CERTIFICATE = 45629;
	private static final int ISHUMA_CERTIFICATE = 45630;
	// Rewards
	private static final int SIR_KRISTOF_RODEMAI_CERTIFICATE = 45631;
	private static final int SPELLBOOK_FAVOR_OF_THE_EXALTED = 45928;
	// Misc
	private static final int MIN_LEVEL = 99;
	private static final int MENTEE_MARKS_NEEDED = 45000;
	private static final int KILLING_NPCSTRING_ID = NpcStringId.BE_PARTY_LEADER.getId();
	
	public Q10821_HelpingOthers()
	{
		super(10821);
		addStartNpc(SIR_KRISTOF_RODEMAI);
		addTalkId(SIR_KRISTOF_RODEMAI);
		addCondMinLevel(MIN_LEVEL, "30756-02.html");
		addCondStartedQuest(Q10817_ExaltedOneWhoOvercomesTheLimit.class.getSimpleName(), "30756-03.html");
		// registerQuestItems(MENTEE_MARK); Should they be removed when abandoning quest?
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30756-04.htm":
			case "30756-05.htm":
			{
				htmltext = event;
				break;
			}
			case "30756-06.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30756-06a.html":
			{
				qs.setCond(2);
				qs.setMemoState(0);
				htmltext = event;
				break;
			}
			case "30756-09.html":
			{
				if ((qs.isCond(1) && (getQuestItemsCount(player, MENTEE_MARK) >= MENTEE_MARKS_NEEDED)) || qs.isCond(3))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						if (hasQuestItems(player, DAICHIR_CERTIFICATE, ISHUMA_CERTIFICATE, OLYMPIAD_MANAGER_CERTIFICATE))
						{
							htmltext = "30756-10.html";
						}
						else
						{
							htmltext = event;
						}
						if (qs.isCond(1))
						{
							takeItems(player, MENTEE_MARK, MENTEE_MARKS_NEEDED);
						}
						giveItems(player, SIR_KRISTOF_RODEMAI_CERTIFICATE, 1);
						giveItems(player, SPELLBOOK_FAVOR_OF_THE_EXALTED, 1);
						qs.exitQuest(false, true);
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "30756-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						if (getQuestItemsCount(player, MENTEE_MARK) >= MENTEE_MARKS_NEEDED)
						{
							htmltext = "30756-08.html";
						}
						else
						{
							htmltext = "30756-07.html";
						}
						break;
					}
					case 3:
					{
						if (qs.isMemoState(2))
						{
							htmltext = "30756-08a.html";
						}
						else
						{
							htmltext = "30756-07a.html";
						}
						break;
					}
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
	
	@RegisterEvent(EventType.ON_ATTACKABLE_KILL)
	@RegisterType(ListenerRegisterType.GLOBAL_MONSTERS)
	public void onAttackableKill(OnAttackableKill event)
	{
		final Player player = event.getAttacker();
		if (player == null)
		{
			return;
		}
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return;
		}
		if (player.getParty() == null)
		{
			return;
		}
		if (player.getParty().getLeader() != player)
		{
			return;
		}
		if (!event.getTarget().isRaid())
		{
			return;
		}
		if (event.getTarget().isRaidMinion())
		{
			return;
		}
		
		if (qs.isCond(2))
		{
			final int memo = qs.getMemoState() + 1;
			qs.setMemoState(memo);
			sendNpcLogList(player);
			if (memo >= 2)
			{
				qs.setCond(3, true);
			}
		}
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(KILLING_NPCSTRING_ID, true, qs.getMemoState()));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}