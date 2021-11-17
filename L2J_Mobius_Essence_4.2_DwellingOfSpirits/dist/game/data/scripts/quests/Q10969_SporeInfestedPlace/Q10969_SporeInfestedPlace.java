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
package quests.Q10969_SporeInfestedPlace;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * @author quangnguyen, Mobius
 */
public class Q10969_SporeInfestedPlace extends Quest
{
	// NPC
	private static final int MAXIMILLIAN = 30120;
	private static final int ORVEN = 30857;
	// Monsters
	private static final int GIANT_FUNGUS = 20555;
	private static final int GIANT_MONSTER_EYE = 20556;
	private static final int DIRE_WYRM = 20557;
	private static final int ROTTING_TREE = 20558;
	private static final int ROTTING_GOLEM = 20559;
	private static final int SPIDER_TRISALIM = 20560;
	private static final int TRISALIM_TARANTULA = 20561;
	private static final int SPORE_ZOMBIE = 20562;
	private static final int EARTH_GUARDIAN_WYRM = 20176;
	// Items
	private static final ItemHolder SPIRIT_ORE = new ItemHolder(3031, 500);
	private static final ItemHolder SOULSHOT_TICKET = new ItemHolder(90907, 50);
	private static final ItemHolder HP_POTS = new ItemHolder(91912, 100);
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final int MIN_LEVEL = 40;
	
	public Q10969_SporeInfestedPlace()
	{
		super(10969);
		addStartNpc(MAXIMILLIAN);
		addTalkId(MAXIMILLIAN, ORVEN);
		addKillId(GIANT_FUNGUS, GIANT_MONSTER_EYE, DIRE_WYRM, ROTTING_TREE, ROTTING_GOLEM, SPIDER_TRISALIM, TRISALIM_TARANTULA, SPORE_ZOMBIE, EARTH_GUARDIAN_WYRM);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_40_45_SPORE_INFECTED_PLACE);
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
			case "30120.htm":
			case "30120-01.htm":
			case "30120-02.htm":
			case "30857.html":
			case "30857-01.html":
			case "30857-03.html":
			case "30857-05.html":
			{
				htmltext = event;
				break;
			}
			case "30120-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "TELEPORT_TO_ORVEN":
			{
				player.teleToLocation(147447, 22633, -1984);
				break;
			}
			case "30857-02.html":
			{
				qs.setCond(2);
				htmltext = event;
				break;
			}
			case "reward":
			{
				if (qs.isCond(3))
				{
					addExpAndSp(player, 1000000, 27000);
					giveItems(player, SPIRIT_ORE);
					giveItems(player, SOULSHOT_TICKET);
					giveItems(player, HP_POTS);
					qs.exitQuest(false, true);
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
			htmltext = "30120.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case MAXIMILLIAN:
				{
					htmltext = "30120-01.html";
					break;
				}
				case ORVEN:
				{
					if (qs.isCond(1))
					{
						htmltext = "30857.html";
					}
					else if (qs.isCond(3) && (player.getLevel() >= 45))
					{
						htmltext = "30857-04.html";
					}
					else
					{
						htmltext = "no_enough.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == ORVEN)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
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
			if (killCount < 500)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(3, true);
				qs.unset(KILL_COUNT_VAR);
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
			holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_IN_THE_SEA_OF_SPORES.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			holder.add(new NpcLogListHolder(NpcStringId.REACH_LV_45, player.getLevel() > 44 ? 1 : 0));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
