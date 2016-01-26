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
package quests.Q10764_FreeSpirit;

import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.NpcSay;
import com.l2jmobius.gameserver.util.Broadcast;

import quests.Q10763_TerrifyingChertuba.Q10763_TerrifyingChertuba;

/**
 * Free Spirit (10764)
 * @author Gigi
 */
public class Q10764_FreeSpirit extends Quest
{
	// NPCs
	private static final int VORBOS = 33966;
	private static final int CAPTURED_TREE = 33964;
	private static final int CAPTURED_WIND = 33965;
	private static final int LIBERATED_WIND_SPIRIT = 33968;
	private static final int LIBERATED_TREE_SPIRIT = 33969;
	// Items
	private static final int LOOSENED_CHAIN = 39518;
	// Reward
	private static final int STEEL_DOOR_GUILD = 37045;
	// Other
	private static final int MIN_LEVEL = 38;
	
	public Q10764_FreeSpirit()
	{
		super(10764, Q10764_FreeSpirit.class.getSimpleName(), "Free Spirit");
		addStartNpc(VORBOS);
		addTalkId(VORBOS, CAPTURED_TREE, CAPTURED_WIND);
		registerQuestItems(LOOSENED_CHAIN);
		addCondRace(Race.ERTHEIA, "33966-no.html");
		addCondMinLevel(MIN_LEVEL, "33966-noLevel.html");
		addCondCompletedQuest(Q10763_TerrifyingChertuba.class.getSimpleName(), "restriction.html");
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
			case "start":
			{
				if (qs.isStarted() && qs.isCond(1))
				{
					if (getQuestItemsCount(player, LOOSENED_CHAIN) < 10)
					{
						giveItems(player, LOOSENED_CHAIN, 1);
						playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
						npc.deleteMe();
					}
					if (getQuestItemsCount(player, LOOSENED_CHAIN) >= 10)
					{
						qs.setCond(2);
					}
				}
				break;
			}
			case "33966-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33966-03.htm": // start the quest
			{
				qs.startQuest();
				break;
			}
			case "33966-05.htm":
			{
				if (qs.isCond(2))
				{
					takeItems(player, LOOSENED_CHAIN, -1);
					giveItems(player, STEEL_DOOR_GUILD, 10);
					addExpAndSp(player, 1312934, 315);
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
		if (qs == null)
		{
			return htmltext;
		}
		if (player.getRace() != Race.ERTHEIA)
		{
			return "33966-no.html";
		}
		
		switch (npc.getId())
		{
			case VORBOS:
			{
				if (qs.isCreated() && ((player.getLevel() >= MIN_LEVEL)))
				{
					htmltext = "33966-01.htm";
				}
				else if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "33966-06.html";
							break;
						}
						case 2:
						{
							htmltext = "33966-04.htm";
							break;
						}
					}
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case CAPTURED_TREE:
			{
				if (qs.isStarted())
				{
					htmltext = "33964-01.html";
				}
				if (getQuestItemsCount(player, LOOSENED_CHAIN) < 10)
				{
					final L2Npc spirit = addSpawn(LIBERATED_TREE_SPIRIT, npc.getX() + 20, npc.getY() + 20, npc.getZ(), npc.getHeading(), false, 5000);
					Broadcast.toKnownPlayers(spirit, new NpcSay(spirit.getObjectId(), ChatType.NPC_GENERAL, spirit.getId(), NpcStringId.THANK_YOU_THANK_YOU_FOR_HELPING));
					giveItems(player, LOOSENED_CHAIN, 1);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					npc.deleteMe();
				}
				if (getQuestItemsCount(player, LOOSENED_CHAIN) >= 10)
				{
					qs.setCond(2);
				}
				break;
			}
			case CAPTURED_WIND:
			{
				if (qs.isStarted())
				{
					htmltext = "33965-01.html";
				}
				if (getQuestItemsCount(player, LOOSENED_CHAIN) < 10)
				{
					addSpawn(LIBERATED_WIND_SPIRIT, npc.getX() + 20, npc.getY() + 20, npc.getZ(), npc.getHeading(), false, 5000);
					Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.THANK_YOU_THANK_YOU_FOR_HELPING));
					giveItems(player, LOOSENED_CHAIN, 1);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					npc.deleteMe();
				}
				if (getQuestItemsCount(player, LOOSENED_CHAIN) >= 10)
				{
					qs.setCond(2);
				}
				break;
			}
		}
		
		return htmltext;
	}
}