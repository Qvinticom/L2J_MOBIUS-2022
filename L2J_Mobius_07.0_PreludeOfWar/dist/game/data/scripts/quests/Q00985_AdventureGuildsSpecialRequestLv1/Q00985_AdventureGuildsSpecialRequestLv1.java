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
package quests.Q00985_AdventureGuildsSpecialRequestLv1;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * Adventure Guilds Special Request Lv1 (985)
 * @author Dmitri
 */
public class Q00985_AdventureGuildsSpecialRequestLv1 extends Quest
{
	// NPCs
	private static final int ADVENTURE_GUILDSMAN = 33946;
	// Monsters
	private static final int[] MONSTERS =
	{
		24458, // Vampire Swamp Warrior
		24457, // Swamp Vampire Rogue
		24460, // Swamp Vampire Shooter
		24459, // Swamp Vampire Wizard
		24454, // Berserker of Fate
		24455, // Prophet of Doom
		24453, // Servant of Fate
		24452, // Soldier of Fate
	};
	// Misc
	private static final int MIN_LEVEL = 85;
	private static final int MAX_LEVEL = 89;
	private static final boolean PARTY_QUEST = true;
	// Reward
	private static final int RUNE_WIND_RESISTANCE_RING = 14599;
	
	public Q00985_AdventureGuildsSpecialRequestLv1()
	{
		super(985);
		addStartNpc(ADVENTURE_GUILDSMAN);
		addTalkId(ADVENTURE_GUILDSMAN);
		addKillId(MONSTERS);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33946-00.htm");
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
			case "33946-02.htm":
			case "33946-03.htm":
			case "33946-04.htm":
			case "33946-08.html":
			{
				htmltext = event;
				break;
			}
			case "33946-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33946-09.html":
			{
				giveItems(player, RUNE_WIND_RESISTANCE_RING, 1);
				addExpAndSp(player, 2108117571L, 2529741);
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
				htmltext = "33946-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33946-06.html";
				}
				else
				{
					htmltext = "33946-07.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				if (qs.isNowAvailable())
				{
					qs.setState(State.CREATED);
					htmltext = "33946-01.htm";
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
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = PARTY_QUEST ? getRandomPartyMemberState(killer, -1, 3, npc) : getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			final int killedGhosts = qs.getInt("AncientGhosts") + 1;
			qs.set("AncientGhosts", killedGhosts);
			playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			
			if (killedGhosts >= 200)
			{
				qs.setCond(2, true);
			}
			sendNpcLogList(killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_MONSTER.getId(), true, qs.getInt("AncientGhosts")));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
