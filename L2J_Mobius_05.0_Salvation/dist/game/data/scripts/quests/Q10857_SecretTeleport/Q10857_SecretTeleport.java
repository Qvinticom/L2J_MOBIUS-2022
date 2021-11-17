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
package quests.Q10857_SecretTeleport;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10856_SuperionAppears.Q10856_SuperionAppears;

/**
 * Secret Teleport (10857)
 * @URL https://l2wiki.com/Secret_Teleport
 * @author Dmitri
 */
public class Q10857_SecretTeleport extends Quest
{
	// NPCs
	private static final int KEKROPUS = 34222;
	private static final int HISTIE = 34243;
	// Monsters
	private static final int[] MONSTERS =
	{
		23774, // Delta Bathus
		23775, // Delta Carcass
		23776, // Delta Kshana
		23777, // Royal Templar
		23778, // Royal Shooter
		23779, // Royal Wizard
		23780, // Royal Templar Colonel
		23781, // Royal Sharpshooter
		23782, // Royal Archmage
		23783 // Royal Gatekeeper
	};
	// Misc
	private static final int MIN_LEVEL = 102;
	// Items
	private static final int SUPERION_MAP_PIECE = 47191; // Quest item: Old Box
	// Reward
	private static final int GIANTS_ENERGY = 35563;
	
	public Q10857_SecretTeleport()
	{
		super(10857);
		addStartNpc(KEKROPUS);
		addTalkId(KEKROPUS, HISTIE);
		addKillId(MONSTERS);
		registerQuestItems(SUPERION_MAP_PIECE);
		addCondMinLevel(MIN_LEVEL, "34222-00.htm");
		addFactionLevel(Faction.GIANT_TRACKERS, 3, "34222-00.htm");
		addCondCompletedQuest(Q10856_SuperionAppears.class.getSimpleName(), "34222-00.htm");
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
			case "34222-03.htm":
			case "34222-02.htm":
			case "34222-05.htm":
			case "34243-02.htm":
			{
				htmltext = event;
				break;
			}
			case "34222-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34243-03.htm":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "teleport":
			{
				player.teleToLocation(79827, 152588, 2309);
				break;
			}
			case "34243-05.html":
			{
				if (qs.isCond(3))
				{
					addExpAndSp(player, 17777142360L, 42664860);
					giveItems(player, GIANTS_ENERGY, 1);
					qs.exitQuest(false, true);
					htmltext = event;
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
				if (npc.getId() == KEKROPUS)
				{
					htmltext = "34222-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case KEKROPUS:
					{
						if (qs.getCond() == 1)
						{
							htmltext = "34222-04.htm";
						}
						break;
					}
					case HISTIE:
					{
						if (qs.getCond() == 1)
						{
							htmltext = "34243-01.htm";
						}
						else if (qs.getCond() == 2)
						{
							htmltext = "34243-03.htm";
						}
						else if (qs.getCond() == 3)
						{
							htmltext = "34243-04.html";
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
					htmltext = "34222-01.htm";
					break;
				}
				qs.setState(State.CREATED);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isCond(2) && giveItemRandomly(killer, SUPERION_MAP_PIECE, 1, 20, 0.9, true))
		{
			qs.setCond(3, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}
