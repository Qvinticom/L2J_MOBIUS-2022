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
package quests.Q00932_SayhasEnergy;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

import quests.Q10831_UnbelievableSight.Q10831_UnbelievableSight;

/**
 * Sayha's Energy (932)
 * @URL https://l2wiki.com/Sayha%27s_Energy
 * @author Dmitri
 */
public class Q00932_SayhasEnergy extends Quest
{
	// NPC
	private static final int BELAS = 34056;
	// Monsters
	private static final int[] MONSTERS =
	{
		23545, // Fury Kiku
		23554, // Fury Kiku N
	};
	// Item's
	private static final int UNWORLDLY_VISITORS_BASIC_SUPPLY_BOX = 47181;
	private static final int UNWORLDLY_VISITORS_INTERMEDIATE_SUPPLY_BOX = 47182;
	private static final int UNWORLDLY_VISITORS_ADVANCED_SUPPLY_BOX = 47183;
	// Misc
	private static final int KILLING_NPCSTRING_ID = NpcStringId.SELECT_QUEST_STAGE_15.getId();
	private static final boolean PARTY_QUEST = true;
	private static final int MIN_LEVEL = 102;
	
	public Q00932_SayhasEnergy()
	{
		super(932);
		addStartNpc(BELAS);
		addTalkId(BELAS);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "34056-00.htm");
		addCondCompletedQuest(Q10831_UnbelievableSight.class.getSimpleName(), "34056-00.htm");
		addFactionLevel(Faction.UNWORLDLY_VISITORS, 4, "34056-00.htm");
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
			case "34056-02.htm":
			case "34056-03.htm":
			case "34056-04.htm":
			case "34056-04a.htm":
			case "34056-06.html":
			case "34056-06a.html":
			{
				htmltext = event;
				break;
			}
			case "select_mission":
			{
				qs.startQuest();
				if (player.getFactionLevel(Faction.UNWORLDLY_VISITORS) >= 5)
				{
					htmltext = "34056-04a.htm";
					break;
				}
				htmltext = "34056-04.htm";
				break;
			}
			case "return":
			{
				if (player.getFactionLevel(Faction.UNWORLDLY_VISITORS) >= 5)
				{
					htmltext = "34056-04a.htm";
					break;
				}
				htmltext = "34056-04.htm";
				break;
			}
			case "34056-07.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34056-07a.html":
			{
				qs.setCond(3, true);
				htmltext = event;
				break;
			}
			case "34056-10.html":
			{
				final int chance = getRandom(100);
				switch (qs.getCond())
				{
					case 4:
					{
						if (player.getLevel() >= MIN_LEVEL)
						{
							if (chance < 2)
							{
								giveItems(player, UNWORLDLY_VISITORS_ADVANCED_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, UNWORLDLY_VISITORS_INTERMEDIATE_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, UNWORLDLY_VISITORS_BASIC_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 22_221_427_950L, 22_221_360);
							addFactionPoints(player, Faction.UNWORLDLY_VISITORS, 100);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
						}
						else
						{
							htmltext = getNoQuestLevelRewardMsg(player);
						}
						break;
					}
					case 5:
					{
						if (player.getLevel() >= MIN_LEVEL)
						{
							if (chance < 2)
							{
								giveItems(player, UNWORLDLY_VISITORS_ADVANCED_SUPPLY_BOX, 1);
							}
							else if (chance < 20)
							{
								giveItems(player, UNWORLDLY_VISITORS_BASIC_SUPPLY_BOX, 1);
							}
							else if (chance < 100)
							{
								giveItems(player, UNWORLDLY_VISITORS_INTERMEDIATE_SUPPLY_BOX, 1);
							}
							addExpAndSp(player, 44_442_855_900L, 44_442_720);
							addFactionPoints(player, Faction.UNWORLDLY_VISITORS, 200);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
						}
						else
						{
							htmltext = getNoQuestLevelRewardMsg(player);
						}
						break;
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
				htmltext = "34056-01.htm";
				// fallthrough?
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						if (player.getFactionLevel(Faction.UNWORLDLY_VISITORS) >= 5)
						{
							htmltext = "34056-04a.htm";
							break;
						}
						htmltext = "34056-04.htm";
						break;
					}
					case 2:
					{
						htmltext = "34056-08.html";
						break;
					}
					case 3:
					{
						htmltext = "34056-08a.html";
						break;
					}
					case 4:
					case 5:
					{
						htmltext = "34056-09.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (!qs.isNowAvailable())
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
				}
				else
				{
					qs.setState(State.CREATED);
					htmltext = "34056-01.htm";
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
		if ((qs != null) && (qs.getCond() > 1))
		{
			switch (qs.getCond())
			{
				case 2:
				{
					final int killedGhosts = qs.getInt("AncientGhosts") + 1;
					qs.set("AncientGhosts", killedGhosts);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if (killedGhosts >= 200)
					{
						qs.setCond(4, true);
					}
					break;
				}
				case 3:
				{
					final int killedGhosts = qs.getInt("AncientGhosts") + 1;
					qs.set("AncientGhosts", killedGhosts);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if (killedGhosts >= 400)
					{
						qs.setCond(5, true);
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (qs.getCond() > 1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(KILLING_NPCSTRING_ID, true, qs.getInt("AncientGhosts")));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
