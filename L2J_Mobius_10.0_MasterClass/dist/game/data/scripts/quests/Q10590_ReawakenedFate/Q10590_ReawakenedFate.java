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
package quests.Q10590_ReawakenedFate;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.util.Util;

import quests.Q10589_WhereFatesIntersect.Q10589_WhereFatesIntersect;

/**
 * Reawakened Fate (10590)
 * @URL https://l2wiki.com/Reawakened_Fate
 * @author Dmitri
 * @author NightBR (fixes and standardization)
 */
public class Q10590_ReawakenedFate extends Quest
{
	// NPCs
	private static final int JOACHIM = 34513;
	private static final int LAPATHIA = 34414;
	private static final int HERPHAH = 34362;
	private static final int ORVEN = 30857;
	private static final int[] MONSTERS =
	{
		24457, // Marsh Vampire Rogue
		24458, // Marsh Vampire Warrior
		24459, // Marsh Vampire Wizard
		24460 // Marsh Vampire Shooter
	};
	// Items
	private static final int VAMPIRE_ICHOR = 80854; // Vampire Ichor - monster drop
	// Rewards
	private static final int ACHIEVEMENT_BOX = 80909;
	private static final int RUBIN_LV2 = 38856;
	private static final int SAPPHIRE_LV2 = 38928;
	// Misc
	private static final int MIN_LEVEL = 95;
	private static final int REACH_LV_99 = NpcStringId.REACH_LV_99.getId();
	// Location
	private static final Location BLOODY_SWAMPLAND = new Location(-14467, 44242, -3673);
	
	public Q10590_ReawakenedFate()
	{
		super(10590);
		addStartNpc(JOACHIM);
		addTalkId(JOACHIM, LAPATHIA, HERPHAH, ORVEN);
		addKillId(MONSTERS);
		registerQuestItems(VAMPIRE_ICHOR);
		addCondMinLevel(MIN_LEVEL, "34513-16.html");
		addCondCompletedQuest(Q10589_WhereFatesIntersect.class.getSimpleName(), "34513-16.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "34513-02.htm":
			case "34513-03.htm":
			case "34513-06.html":
			case "34414-02.html":
			case "34362-02.html":
			case "30857-02.html":
			case "34513-09.html":
			{
				htmltext = event;
				break;
			}
			case "34513-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(1) || qs.isCond(2) || qs.isCond(4) || qs.isCond(5) || qs.isCond(6) || qs.isCond(7))
				{
					giveStoryBuffReward(npc, player);
					player.teleToLocation(BLOODY_SWAMPLAND); // Teleport to Bloody Swampland near npc Lapathia
				}
				break;
			}
			case "34414-03.html":
			{
				qs.setCond(2, true);
				giveStoryBuffReward(npc, player);
				htmltext = event;
				break;
			}
			case "34513-07.html":
			{
				qs.setCond(4, true);
				giveStoryBuffReward(npc, player);
				htmltext = event;
				break;
			}
			case "34362-03.html":
			{
				qs.setCond(5, true);
				giveStoryBuffReward(npc, player);
				htmltext = event;
				break;
			}
			case "30857-03.html":
			{
				qs.setCond(6, true);
				giveStoryBuffReward(npc, player);
				htmltext = event;
				break;
			}
			case "34513-10.html":
			{
				if (qs.isCond(7) && (player.getLevel() >= 99))
				{
					// Reward №1
					takeItems(player, VAMPIRE_ICHOR, -1);
					giveItems(player, ACHIEVEMENT_BOX, 1);
					giveItems(player, RUBIN_LV2, 1);
					showOnScreenMsg(player, NpcStringId.YOU_ARE_READY_TO_ADD_A_DUAL_CLASS_TALK_TO_THE_DUAL_CLASS_MASTER, ExShowScreenMessage.TOP_CENTER, 10000);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "34513-11.html":
			{
				if (qs.isCond(7) && (player.getLevel() >= 99))
				{
					// Reward №2
					takeItems(player, VAMPIRE_ICHOR, -1);
					giveItems(player, ACHIEVEMENT_BOX, 1);
					giveItems(player, SAPPHIRE_LV2, 1);
					showOnScreenMsg(player, NpcStringId.YOU_ARE_READY_TO_ADD_A_DUAL_CLASS_TALK_TO_THE_DUAL_CLASS_MASTER, ExShowScreenMessage.TOP_CENTER, 10000);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "34513-12.html":
			{
				if (player.hasDualClass())
				{
					htmltext = "34513-13.html";
				}
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
				if (npc.getId() == JOACHIM)
				{
					htmltext = "34513-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case JOACHIM:
					{
						if (qs.isCond(1))
						{
							htmltext = "34513-03.htm";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34513-15.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34513-05.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "34513-07.html";
						}
						else if (qs.isCond(7))
						{
							htmltext = "34513-08.html";
						}
						break;
					}
					case LAPATHIA:
					{
						if (qs.isCond(1))
						{
							htmltext = "34414-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34414-03.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34414-04.html";
						}
						break;
					}
					case HERPHAH:
					{
						if (qs.isCond(4))
						{
							htmltext = "34362-01.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "34362-04.html";
						}
						break;
					}
					case ORVEN:
					{
						if (qs.isCond(5))
						{
							htmltext = "30857-01.html";
						}
						else if (qs.isCond(6))
						{
							qs.setCond(7, true);
							htmltext = "30857-04.html";
						}
						else if (qs.isCond(7))
						{
							htmltext = "30857-05.html";
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
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, player, false))
		{
			if (getQuestItemsCount(player, VAMPIRE_ICHOR) < 500)
			{
				giveItems(player, VAMPIRE_ICHOR, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if ((getQuestItemsCount(player, VAMPIRE_ICHOR) >= 500) && (player.getLevel() >= 99))
			{
				qs.setCond(3, true);
			}
			sendNpcLogList(player);
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			if (player.getLevel() >= 99)
			{
				holder.add(new NpcLogListHolder(REACH_LV_99, true, 1));
			}
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
