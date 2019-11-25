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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.util.Util;

import quests.Q10589_WhereFatesIntersect.Q10589_WhereFatesIntersect;

/**
 * Q10590_ReawakenedFate
 * @URL https://www.youtube.com/watch?v=HCd784Gnguw
 * @author NightBR
 */
public class Q10590_ReawakenedFate extends Quest
{
	// NPCs
	private static final int JOACHIM = 34513;
	private static final int RAFLASIA = 34414;
	private static final int HERPA = 34362;
	private static final int ORWEN = 30857;
	private static final int[] MONSTERS =
	{
		24457, // Swamp Vampire Rogue
		24458, // Swamp Vampire Warrior
		24459, // Swamp Vampire Wizard
		24460 // Swamp Vampire Shooter
	};
	// Item
	private static final int IHOR_VAMPIRE = 80854; // Ihor Vampire - monster drop
	// Rewards
	private static final long EXP = 1;
	private static final int SP = 1;
	private static final int ADENA_AMOUNT = 5050;
	private static final int ACHIEVEMENT_BOX = 80909;
	private static final int RUBIN_LV2 = 38856;
	private static final int SAPPHIRE_LV2 = 38928;
	// Misc
	private static final int MIN_LEVEL = 99;
	// Location
	private static final Location ALTAR_OF_EVIL = new Location(-14467, 44242, -3673);
	
	public Q10590_ReawakenedFate()
	{
		super(10590);
		addStartNpc(JOACHIM);
		addTalkId(JOACHIM, RAFLASIA, HERPA, ORWEN);
		addKillId(MONSTERS);
		registerQuestItems(IHOR_VAMPIRE);
		addCondMinLevel(95, "34513-16.html");
		addCondCompletedQuest(Q10589_WhereFatesIntersect.class.getSimpleName(), "34513-16.html");
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
			case "34513-02.html":
			case "34513-04.html":
			case "34513-07.html":
			case "34362-03.html":
			case "30857-03.html":
			case "34513-09.html":
			case "34513-12.html":
			case "34513-13.html":
			{
				htmltext = event;
				break;
			}
			case "34513-03.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(ALTAR_OF_EVIL);
				}
				break;
			}
			case "34414-02.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34513-06.html":
			{
				qs.setCond(4, true);
				htmltext = event;
				break;
			}
			case "34362-02.html":
			{
				qs.setCond(5, true);
				htmltext = event;
				break;
			}
			case "30857-02.html":
			{
				qs.setCond(6, true);
				htmltext = event;
				break;
			}
			case "30857-04.html":
			{
				if (qs.isCond(6))
				{
					qs.setCond(7, true);
				}
				htmltext = event;
				break;
			}
			case "34513-10.html":
			{
				if (qs.isCond(7))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						// Reward №1
						addExpAndSp(player, EXP, SP);
						giveAdena(player, ADENA_AMOUNT, false);
						giveItems(player, ACHIEVEMENT_BOX, 1);
						giveItems(player, RUBIN_LV2, 1);
						showOnScreenMsg(player, NpcStringId.YOU_ARE_READY_TO_ADD_A_DUAL_CLASS_NTALK_TO_THE_DUAL_CLASS_MASTER, ExShowScreenMessage.TOP_CENTER, 10000);
						qs.exitQuest(false, true);
						htmltext = event;
					}
					break;
				}
			}
			case "34513-11.html":
			{
				if (qs.isCond(7))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						// Reward №2
						addExpAndSp(player, EXP, SP);
						giveAdena(player, ADENA_AMOUNT, false);
						giveItems(player, ACHIEVEMENT_BOX, 1);
						giveItems(player, SAPPHIRE_LV2, 1);
						showOnScreenMsg(player, NpcStringId.YOU_ARE_READY_TO_ADD_A_DUAL_CLASS_NTALK_TO_THE_DUAL_CLASS_MASTER, ExShowScreenMessage.TOP_CENTER, 10000);
						qs.exitQuest(false, true);
						htmltext = event;
					}
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == JOACHIM)
				{
					htmltext = "34513-01.html";
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
							htmltext = "34513-03.html";
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
					case RAFLASIA:
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
					case HERPA:
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
					case ORWEN:
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
	public void actionForEachPlayer(PlayerInstance player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, player, false))
		{
			if ((getQuestItemsCount(player, IHOR_VAMPIRE) < 500) && (getRandom(100) < 90))
			{
				giveItems(player, IHOR_VAMPIRE, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if ((getQuestItemsCount(player, IHOR_VAMPIRE) >= 500) && (player.getLevel() >= MIN_LEVEL))
			{
				qs.setCond(3, true);
			}
		}
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
}
