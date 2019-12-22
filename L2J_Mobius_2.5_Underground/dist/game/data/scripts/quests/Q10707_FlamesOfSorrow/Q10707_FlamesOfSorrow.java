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
package quests.Q10707_FlamesOfSorrow;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

import quests.Q10395_NotATraitor.Q10395_NotATraitor;

/**
 * Flames of Sorrow (10707)
 * @author St3eT
 */
public class Q10707_FlamesOfSorrow extends Quest
{
	// NPCs
	private static final int LEO = 33863;
	private static final int WARNING_FIRE = 19545;
	private static final int VENGEFUL_SPIRIT = 27518;
	private static final int SPIRIT = 33959;
	// Items
	private static final int MARK = 39508; // Mark of Gratitude
	private static final int EAC = 952; // Scroll: Enchant Armor (C-grade)
	// Misc
	private static final int MIN_LEVEL = 46;
	private static final int MAX_LEVEL = 51;
	private static final NpcStringId[] RANDOM_MSGS =
	{
		NpcStringId.WE_WILL_NOT_TURN_BACK,
		NpcStringId.THE_WAR_IS_NOT_YET_OVER,
	};
	
	public Q10707_FlamesOfSorrow()
	{
		super(10707);
		addFirstTalkId(WARNING_FIRE);
		addStartNpc(LEO);
		addTalkId(LEO, WARNING_FIRE);
		addKillId(VENGEFUL_SPIRIT);
		registerQuestItems(MARK);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33863-07.htm");
		addCondCompletedQuest(Q10395_NotATraitor.class.getSimpleName(), "33863-07.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33863-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33863-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33863-06.html":
			{
				if (qs.isCond(2))
				{
					qs.exitQuest(false, true);
					giveItems(player, EAC, 2);
					giveStoryQuestReward(player, 10);
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 483_840, 116);
					}
					htmltext = event;
				}
				break;
			}
			case "spawnMonster":
			{
				if (qs.isCond(1))
				{
					npc.deleteMe();
					final Npc spirit = addSpawn(VENGEFUL_SPIRIT, player, true, 60000);
					addAttackPlayerDesire(spirit, player);
					spirit.broadcastSay(ChatType.NPC_GENERAL, RANDOM_MSGS[getRandom(RANDOM_MSGS.length)]);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == LEO)
				{
					htmltext = "33863-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == LEO)
				{
					if (qs.isCond(1))
					{
						htmltext = "33863-04.html";
					}
					else if (qs.isCond(2))
					{
						htmltext = "33863-05.html";
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == LEO)
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		
		if ((qs != null) && qs.isStarted() && qs.isCond(1) && (getRandom(100) < 75))
		{
			giveItems(killer, MARK, 1);
			playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			final Npc spirit = addSpawn(SPIRIT, npc, false, 5000);
			spirit.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.THANK_YOU_DELIVER_THIS_MARK_OF_GRATITUDE_TO_LEO);
			
			if (getQuestItemsCount(killer, MARK) == 5)
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		return (qs != null) && qs.isCond(1) ? "19545.html" : "19545-no.html";
	}
}