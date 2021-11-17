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
package quests.Q10966_ATripBegins;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;

/**
 * A Trip Begins (10966)
 * @author RobikBobik, Mobius
 * @Note: Updated based on 4game server October 2020
 * @TODO: Update gatekeeper dialogs.
 */
public class Q10966_ATripBegins extends Quest
{
	// NPCs
	private static final int CAPTAIN_BATHIS = 30332;
	private static final int MATHORN = 34139;
	private static final int BELLA = 30256;
	// Items
	private static final ItemHolder SOE_TO_CAPTAIN_BATHIS = new ItemHolder(91651, 1);
	private static final ItemHolder SOE_NOVICE = new ItemHolder(10650, 10);
	private static final ItemHolder TALISMAN_OF_ADEN = new ItemHolder(91745, 1);
	private static final ItemHolder SCROLL_OF_ENCHANT_TALISMAN_OF_ADEN = new ItemHolder(91756, 1);
	private static final ItemHolder ADVENTURERS_BRACELET = new ItemHolder(91934, 1);
	private static final ItemHolder SCROLL_OF_ENCHANT_ADEN_WEAPON = new ItemHolder(93038, 2);
	// Monsters
	private static final int ARACHNID_PREDATOR = 20926;
	private static final int SKELETON_BOWMAN = 20051;
	private static final int RUIN_SPARTOI = 20054;
	private static final int RAGING_SPARTOI = 20060;
	private static final int TUMRAN_BUGBEAR = 20062;
	private static final int TUMRAN_BUGBEAR_WARRIOR = 20064;
	// Location
	private static final Location TELEPORT_LOCATION = new Location(-14443, 123984, -3120);
	// Misc
	private static final int MIN_LEVEL = 20;
	private static final int MAX_LEVEL = 25;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10966_ATripBegins()
	{
		super(10966);
		addStartNpc(CAPTAIN_BATHIS, MATHORN);
		addTalkId(CAPTAIN_BATHIS, MATHORN, BELLA);
		addKillId(ARACHNID_PREDATOR, SKELETON_BOWMAN, RUIN_SPARTOI, RAGING_SPARTOI, RAGING_SPARTOI, TUMRAN_BUGBEAR, TUMRAN_BUGBEAR_WARRIOR);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_20_25_A_TRIP_BEGINS);
	}
	
	@Override
	public boolean checkPartyMember(Player member, Npc npc)
	{
		final QuestState qs = getQuestState(member, false);
		return ((qs != null) && qs.isStarted());
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
			case "30332-01.htm":
			case "30332-07.htm":
			case "30332-08.htm":
			case "34139-01.htm":
			case "34139-02.html":
			case "34139-03.html":
			case "34139-04.html":
			case "34139-05.html":
			case "34139-06.htm":
			{
				htmltext = event;
				break;
			}
			case "34139-00.htm":
			{
				showOnScreenMsg(player, NpcStringId.CHECK_YOUR_INVENTORY_AND_EQUIP_YOUR_WEAPON, ExShowScreenMessage.TOP_CENTER, 10000, player.getName());
				htmltext = event;
				break;
			}
			case "30332-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30332-03.htm":
			{
				qs.startQuest();
				npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, NpcStringId.USING_THE_GATEKEEPER));
				htmltext = event;
				break;
			}
			case "30256-01.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "30332-06.html":
			{
				htmltext = event;
				break;
			}
			case "30332-05.html":
			{
				if (qs.isCond(3))
				{
					showOnScreenMsg(player, NpcStringId.YOU_VE_GOT_ADVENTURER_S_BRACELET_AND_TALISMAN_OF_ADEN_NCOMPLETE_THE_TUTORIAL_AND_TRY_TO_USE_THE_TALISMAN, ExShowScreenMessage.TOP_CENTER, 10000);
					addExpAndSp(player, 1000000, 27000);
					giveItems(player, SOE_NOVICE);
					giveItems(player, TALISMAN_OF_ADEN);
					giveItems(player, SCROLL_OF_ENCHANT_TALISMAN_OF_ADEN);
					giveItems(player, ADVENTURERS_BRACELET);
					giveItems(player, SCROLL_OF_ENCHANT_ADEN_WEAPON);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "34139-07.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(TELEPORT_LOCATION);
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
		if (qs.isCreated())
		{
			switch (npc.getId())
			{
				case CAPTAIN_BATHIS:
				{
					// Death Knights.
					if (player.isDeathKnight())
					{
						return htmltext;
					}
					
					htmltext = "30332.htm";
					break;
				}
				case MATHORN:
				{
					// Death Knights.
					if (!player.isDeathKnight())
					{
						return htmltext;
					}
					
					htmltext = "34139-01.htm";
					break;
				}
			}
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case CAPTAIN_BATHIS:
				{
					if (qs.isCond(1))
					{
						// Death Knights.
						if (player.isDeathKnight())
						{
							return htmltext;
						}
						
						htmltext = "30332-03.htm";
					}
					else if (qs.isCond(3))
					{
						htmltext = "30332-04.html";
					}
					break;
				}
				case MATHORN:
				{
					// Death Knights.
					if (!player.isDeathKnight())
					{
						return htmltext;
					}
					
					if (qs.isCond(1))
					{
						htmltext = "34139-07.htm";
					}
					break;
				}
				case BELLA:
				{
					if (qs.isCond(1))
					{
						htmltext = "30256.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(2))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 70)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(3, true);
				qs.unset(KILL_COUNT_VAR);
				showOnScreenMsg(killer, NpcStringId.YOU_VE_KILLED_ALL_THE_MONSTERS_NUSE_THE_SCROLL_OF_ESCAPE_IN_YOUR_INVENTORY_TO_RETURN_TO_CAPTAIN_BATHIS_IN_GLUDIO, ExShowScreenMessage.TOP_CENTER, 10000, killer.getName());
				giveItems(killer, SOE_TO_CAPTAIN_BATHIS);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_IN_THE_RUINS_OF_AGONY.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}