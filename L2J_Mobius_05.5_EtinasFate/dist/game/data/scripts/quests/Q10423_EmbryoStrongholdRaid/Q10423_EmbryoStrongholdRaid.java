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
package quests.Q10423_EmbryoStrongholdRaid;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * Embryo Stronghold Raid (10423)
 * @URL https://l2wiki.com/Embryo_Stronghold_Raid
 * @author Dmitri
 */
public class Q10423_EmbryoStrongholdRaid extends Quest
{
	// NPCs
	private static final int ERDA = 34319;
	// Monsters
	private static final int[] MOBS =
	{
		26199, // Sampson
		26200, // Hanson
		26201, // Grom
		26202, // Medvez
		26203, // Zigatan
		26204, // Hunchback Kwai
		26205, // Cornix
		26206, // Caranix
		26207, // Jonadan
		26208, // Demien
		26209, // Berg
		26210, // Tarku
		26211, // Tarpin
		26212, // Embryo Safe Vault
		26213, // Embryo Secret Vault
		26214, // Sakum
		26215, // Crazy Typhoon
		26216, // Cursed Haren
		26217, // Flynt
		26218, // Harp
		26219, // Maliss
		26220, // Isadora
		26221, // Whitra
		26222, // Bletra
		26223, // Upgraded Siege Tank
		26224, // Vegima
		26225, // Varonia
		26226, // Aronia
		26227, // Odd
		26228, // Even
		26229 // Nemertess
	};
	// Rewards
	private static final int SUPERIOR_GIANTS_CODEX = 46151; // Superior Giant's Codex - Mastery Chapter 1
	// Misc
	private static final int MIN_LEVEL = 100;
	
	public Q10423_EmbryoStrongholdRaid()
	{
		super(10423);
		addStartNpc(ERDA);
		addTalkId(ERDA);
		addKillId(MOBS);
		addCondMinLevel(MIN_LEVEL, "34319-00.htm");
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
			case "34319-02.htm":
			case "34319-03.htm":
			case "34319-07.html":
			{
				htmltext = event;
				break;
			}
			case "34319-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34319-08.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, SUPERIOR_GIANTS_CODEX, 1);
					addExpAndSp(player, 29682570651L, 71108570);
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
				htmltext = "34319-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "34319-05.html";
				}
				else
				{
					htmltext = "34319-06.html";
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
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			int killedEmbryo = qs.getInt("killed_" + MOBS[0]);
			if (CommonUtil.contains(MOBS, npc.getId()))
			{
				if (killedEmbryo < 30)
				{
					killedEmbryo++;
					qs.set("killed_" + MOBS[0], killedEmbryo);
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			if (killedEmbryo == 30)
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isStarted() && qs.isCond(1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_EMBRYO_OFFICER, qs.getInt("killed_" + MOBS[0])));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
