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
package quests.Q00933_TombRaiders;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Creature; // Imports belov needed for onEnterZone
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.model.zone.type.ScriptZone;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * Tomb Raiders (933)
 * @URL https://l2wiki.com/Tomb_Raiders
 * @author Sero, retail like modifications by Neith and CostyKiller
 */
public class Q00933_TombRaiders extends Quest
{
	// NPCs
	private static final int SEARCH_TEAM_TELEPORTER = 34552;
	private static final int LEOPARD = 32594;
	// Monsters
	private static final int TOMB_GUARDIAN = 24580;
	private static final int TOMB_RAIDER = 24581;
	private static final int TOMB_WATCHER = 24584;
	private static final int TOMB_SOULTAKER = 24583;
	private static final int TOMB_PATROL = 24582;
	// Item
	private static final int BENUSTA_REWARD_BOX = 81151;
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	// Zone
	private static final ScriptZone QUEST_ZONE = ZoneManager.getInstance().getZoneById(93300, ScriptZone.class); // Session Zone - Imperial Tomb
	
	public Q00933_TombRaiders()
	{
		super(933);
		addEnterZoneId(QUEST_ZONE.getId());
		addStartNpc(SEARCH_TEAM_TELEPORTER);
		addTalkId(SEARCH_TEAM_TELEPORTER, LEOPARD);
		addKillId(TOMB_GUARDIAN, TOMB_RAIDER, TOMB_WATCHER, TOMB_SOULTAKER, TOMB_PATROL);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		switch (event)
		{
			case "34552-02.htm":
			{
				qs.startQuest();
				htmltext = event;
				
				break;
			}
			case "32594-04.htm":
			{
				if (qs.isCond(2))
				{
					addExpAndSp(player, 20700253956096L, 1450359376);
					giveItems(player, BENUSTA_REWARD_BOX, 1);
					qs.exitQuest(QuestType.DAILY, true);
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
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == SEARCH_TEAM_TELEPORTER)
				{
					htmltext = "34552-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == LEOPARD)
				{
					if (qs.isCond(2))
					{
						htmltext = "32594-03.htm";
					}
					else
					{
						htmltext = "32594-06.htm";
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == SEARCH_TEAM_TELEPORTER)
				{
					if (qs.isNowAvailable())
					{
						qs.setState(State.CREATED);
						htmltext = "34552-01.htm";
					}
					else
					{
						htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onEnterZone(Creature creature, ZoneType zone)
	{
		if (creature.isPlayer())
		{
			final QuestState qs = getQuestState(creature.getActingPlayer(), true);
			if (qs != null)
			{
				if (qs.isCreated())
				{
					qs.startQuest();
				}
				else if (qs.isNowAvailable() && qs.isCond(0))
				{
					qs.setState(State.CREATED);
					qs.startQuest();
				}
			}
		}
		return super.onEnterZone(creature, zone);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Party party = killer.getParty();
		if (party != null)
		{
			party.getMembers().forEach(p -> processKill(npc, p));
		}
		else
		{
			processKill(npc, killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private void processKill(Npc npc, Player killer)
	{
		final QuestState qs = getRandomPartyMemberState(killer, 1, 3, npc);
		if (qs != null)
		{
			final Player player = qs.getPlayer();
			int killCount = qs.getInt(KILL_COUNT_VAR);
			if (killCount < 150)
			{
				qs.set(KILL_COUNT_VAR, killCount + 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if (killCount >= 150)
			{
				qs.setCond(2, true);
			}
			sendNpcLogList(player);
		}
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_MONSTERS_IN_THE_IMPERIAL_TOMB.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
