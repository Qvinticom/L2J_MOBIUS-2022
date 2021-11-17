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
package quests.Q00824_AttackTheCommandPost;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Command Post Raid (824)
 * @URL https://l2wiki.com/Command_Post_Raid
 * @author Dmitri
 */
public class Q00824_AttackTheCommandPost extends Quest
{
	// NPCs
	private static final int DEVIANNE = 34089;
	private static final int ELIKIA = 34057;
	// RaidBosses
	private static final int BURNSTEIN = 26136;
	// Rewards
	private static final int ADEN_VANGUARD_SUPPLY_BOX = 46283;
	// Misc
	private static final int MIN_LEVEL = 100;
	
	public Q00824_AttackTheCommandPost()
	{
		super(824);
		addStartNpc(DEVIANNE);
		addTalkId(DEVIANNE, ELIKIA);
		addKillId(BURNSTEIN);
		addCondMinLevel(MIN_LEVEL, "34089-00.htm");
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
			case "34089-02.htm":
			case "34089-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34089-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34057-07.html":
			{
				// Rewards
				giveItems(player, ADEN_VANGUARD_SUPPLY_BOX, 1);
				addExpAndSp(player, 3954960000L, 9491880);
				qs.exitQuest(QuestType.DAILY, true);
				htmltext = event;
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
				if (npc.getId() == DEVIANNE)
				{
					htmltext = "34089-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case DEVIANNE:
					{
						if (qs.isCond(1))
						{
							htmltext = "34089-05.html";
						}
						break;
					}
					case ELIKIA:
					{
						if (qs.isCond(2))
						{
							htmltext = "34057-06.html";
						}
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
					htmltext = "34089-01.htm";
				}
				else
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			qs.setCond(2, true);
		}
	}
}
