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
package quests.Q10852_TheMotherTreeRevivalProject;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * The Mother Tree Revival Project (10852)
 * @URL https://l2wiki.com/The_Mother_Tree_Revival_Project
 * @author Dmitri
 */
public class Q10852_TheMotherTreeRevivalProject extends Quest
{
	// NPCs
	private static final int IRENE = 34233;
	// Monsters
	private static final int NYMPH_SENTINEL = 23578;
	private static final int[] ROSE =
	{
		23566, // Nymph Rose
		23567, // Nymph Rose
	};
	private static final int[] LILY =
	{
		23568, // Nymph Lily
		23569, // Nymph Lily
	};
	private static final int[] TULIP =
	{
		23570, // Nymph Tulip
		23571, // Nymph Tulip
	};
	private static final int[] COSMOS =
	{
		23572, // Nymph Cosmos
		23573, // Nymph Cosmos
	};
	// Items
	private static final int RUNE_STONE = 39738;
	private static final int SPELLBOOK_PEGASUS = 47150;
	// Misc
	private static final int MIN_LEVEL = 102;
	
	public Q10852_TheMotherTreeRevivalProject()
	{
		super(10852);
		addStartNpc(IRENE);
		addTalkId(IRENE);
		addKillId(COSMOS);
		addKillId(TULIP);
		addKillId(LILY);
		addKillId(ROSE);
		addKillId(NYMPH_SENTINEL);
		addCondMinLevel(MIN_LEVEL, "34233-00.htm");
		addFactionLevel(Faction.MOTHER_TREE_GUARDIANS, 6, "34233-00.htm");
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
			case "34233-02.htm":
			case "34233-03.htm":
			case "34233-04.htm":
			case "34233-08.html":
			{
				htmltext = event;
				break;
			}
			case "34233-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34233-09.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, RUNE_STONE, 1);
					giveItems(player, SPELLBOOK_PEGASUS, 1);
					addExpAndSp(player, 444428559000L, 444427200);
					qs.exitQuest(QuestType.ONE_TIME, true);
					htmltext = event;
				}
				else
				{
					htmltext = getNoQuestLevelRewardMsg(player);
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
				if (npc.getId() == IRENE)
				{
					htmltext = "34233-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case IRENE:
					{
						if (qs.isCond(1))
						{
							htmltext = "34233-06.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34233-07.html";
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
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			int killedTulip = qs.getInt("killed_" + TULIP[0]);
			int killedCosmos = qs.getInt("killed_" + COSMOS[0]);
			int killedLily = qs.getInt("killed_" + LILY[0]);
			int killedRose = qs.getInt("killed_" + ROSE[0]);
			int killedSentinel = qs.getInt("killed_" + NYMPH_SENTINEL);
			if (CommonUtil.contains(TULIP, npc.getId()))
			{
				if (killedTulip < 300)
				{
					killedTulip++;
					qs.set("killed_" + TULIP[0], killedTulip);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (CommonUtil.contains(COSMOS, npc.getId()))
			{
				if (killedCosmos < 300)
				{
					killedCosmos++;
					qs.set("killed_" + COSMOS[0], killedCosmos);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (CommonUtil.contains(LILY, npc.getId()))
			{
				if (killedLily < 300)
				{
					killedLily++;
					qs.set("killed_" + LILY[0], killedLily);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (CommonUtil.contains(ROSE, npc.getId()))
			{
				if (killedRose < 300)
				{
					killedRose++;
					qs.set("killed_" + ROSE[0], killedRose);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (killedSentinel < 100)
			{
				qs.set("killed_" + NYMPH_SENTINEL, ++killedSentinel);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			
			if ((killedTulip == 300) && (killedCosmos == 300) && (killedLily == 300) && (killedRose == 300) && (killedSentinel >= 100))
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isStarted() && qs.isCond(1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(5);
			npcLogList.add(new NpcLogListHolder(TULIP[0], false, qs.getInt("killed_" + TULIP[0])));
			npcLogList.add(new NpcLogListHolder(COSMOS[0], false, qs.getInt("killed_" + COSMOS[0])));
			npcLogList.add(new NpcLogListHolder(LILY[0], false, qs.getInt("killed_" + LILY[0])));
			npcLogList.add(new NpcLogListHolder(ROSE[0], false, qs.getInt("killed_" + ROSE[0])));
			npcLogList.add(new NpcLogListHolder(NYMPH_SENTINEL, false, qs.getInt("killed_" + NYMPH_SENTINEL)));
			return npcLogList;
		}
		return super.getNpcLogList(player);
	}
}
