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
package quests.Q10890_SaviorsPathHallOfEtina;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10888_SaviorsPathDefeatTheEmbryo.Q10888_SaviorsPathDefeatTheEmbryo;
import quests.Q10889_SaviorsPathFallenEmperorsThrone.Q10889_SaviorsPathFallenEmperorsThrone;

/**
 * Savior's Path - Hall of Etina (10890)
 * @URL https://l2wiki.com/Savior%27s_Path_-_Fall_of_Etina
 * @author CostyKiller
 */
public class Q10890_SaviorsPathHallOfEtina extends Quest
{
	// NPCs
	private static final int LEONA_BLACKBIRD = 34425;
	private static final int LEONA_BLACKBIRD_OUTLET = 34426;
	// Monsters
	private static final int ETIS_VAN_ETINA_SOLO = 26322;
	// Rewards
	private static final int SAVIORS_MASK = 48584;
	private static final int SAVIORS_ENCHANT_SCROLL = 48583;
	// Misc
	private static final int MIN_LEVEL = 104;
	private static final Location OUTLET_TELEPORT = new Location(-251728, 178576, -8928);
	private static final String ETIS_VAN_ETINA_SOLO_VAR = "26322";
	
	public Q10890_SaviorsPathHallOfEtina()
	{
		super(10890);
		addStartNpc(LEONA_BLACKBIRD);
		addTalkId(LEONA_BLACKBIRD, LEONA_BLACKBIRD_OUTLET);
		addKillId(ETIS_VAN_ETINA_SOLO);
		addCondMinLevel(MIN_LEVEL, "34425-00.html");
		addCondCompletedQuest(Q10888_SaviorsPathDefeatTheEmbryo.class.getSimpleName(), "34425-00.html");
		addCondCompletedQuest(Q10889_SaviorsPathFallenEmperorsThrone.class.getSimpleName(), "34425-00.html");
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
			case "34425-02.htm":
			case "34425-03.htm":
			case "34425-06.htm":
			case "34426-01.htm":
			{
				htmltext = event;
				break;
			}
			case "34425-04.html":
			{
				if ((player.getLevel() >= MIN_LEVEL))
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = event;
				}
				break;
			}
			case "outletTeleport":
			{
				player.teleToLocation(OUTLET_TELEPORT);
				break;
			}
			case "34426-02.html":
			{
				if ((player.getLevel() >= MIN_LEVEL))
				{
					qs.setCond(2);
					htmltext = event;
				}
				break;
			}
			case "34426-04.html":
			{
				if (qs.isCond(3))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						addExpAndSp(player, 376172418240L, 345208219);
						giveItems(player, SAVIORS_MASK, 1);
						giveItems(player, SAVIORS_ENCHANT_SCROLL, 1);
						qs.exitQuest(false, true);
						htmltext = event;
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
				htmltext = "34425-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case LEONA_BLACKBIRD:
					{
						if (qs.isCond(1) && qs.isMemoState(1))
						{
							htmltext = "34425-05.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34425-06.html";
						}
						else
						{
							htmltext = "34425-01.htm";
						}
						break;
					}
					case LEONA_BLACKBIRD_OUTLET:
					{
						if (qs.isCond(1))
						{
							htmltext = "34426-01.htm";
						}
						if (qs.isCond(2))
						{
							htmltext = "34426-02.html";
						}
						else if (qs.isCond(3))
						{
							final String status = qs.get(ETIS_VAN_ETINA_SOLO_VAR);
							if ((status != null) && status.equals("true"))
							{
								htmltext = "34426-03.htm";
							}
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
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			qs.setCond(3, true);
			qs.set(ETIS_VAN_ETINA_SOLO_VAR, "true");
			notifyKill(npc, player, isSummon);
			sendNpcLogList(player);
		}
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			final String status = qs.get(ETIS_VAN_ETINA_SOLO_VAR);
			holder.add(new NpcLogListHolder(1026322, true, (status != null) && status.equals("true") ? 1 : 0));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}