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
package quests.Q10880_TheLastOneStanding;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10879_ExaltedGuideToPower.Q10879_ExaltedGuideToPower;

/**
 * The Last One Standing (10880)
 * @URL https://l2wiki.com/The_Last_One_Standing
 * @author Dmitri
 */
public class Q10880_TheLastOneStanding extends Quest
{
	// NPCs
	private static final int CYPHONA = 34055;
	private static final int FERIN = 34054;
	// Items
	private static final int MASTER_CYPHONA_CERTIFICATE = 47835;
	private static final int PROOF_OF_STRENGTH = 47843;
	private static final ItemHolder LIONEL_HUNTERS_LIST_PART_5 = new ItemHolder(47834, 1);
	// Monsters
	private static final int[] MONSTERS =
	{
		// The Enchanted Valley
		23566, // Nymph Rose
		23567, // Nymph Rose
		23568, // Nymph Lily
		23569, // Nymph Lily
		23570, // Nymph Tulip
		23571, // Nymph Tulip
		23572, // Nymph Cosmos
		23573, // Nymph Cosmos
		23578, // Nymph Guardian
		// Garden of Spirits
		23541, // Kerberos Lager
		23550, // Kerberos Lager (night)
		23542, // Kerberos Fort
		23551, // Kerberos Fort (night)
		23543, // Kerberos Nero
		23552, // Kerberos Nero (night)
		23544, // Fury Sylph Barrena
		23553, // Fury Sylph Barrena (night)
		23546, // Fury Sylph Temptress
		23555, // Fury Sylph Temptress (night)
		23547, // Fury Sylph Purka
		23556, // Fury Sylph Purka (night)
		23545, // Fury Kerberos Leger
		23557, // Fury Kerberos Leger (night)
		23549, // Fury Kerberos Nero
		23558, // Fury Kerberos Nero (night)
		// Atelia Fortress
		23505, // Fortress Raider 101
		23506, // Fortress Guardian Captain 101
		23537, // Atelia Elite Captain Atelia Infuser 102
		23538, // Atelia High Priest Atelia Infuser 103
		23536, // Atelia High Priest Kelbim's 102
		23535, // Atelia Archon Kelbim's 102
		23532, // Atelia Elite Captain Kelbim's 101
		23530, // Fortress Guardian Captain Kelbim's 101
		23507, // Atelia Passionate Soldier 101
		23508, // Atelia Elite Captain 101
		23509, // Fortress Dark Wizard 102
		23510, // Atelia Flame Master 102
		23511, // Fortress Archon 102
		23512, // Atelia High Priest 102
	};
	
	// Misc
	private static final int MIN_LEVEL = 104;
	
	public Q10880_TheLastOneStanding()
	{
		super(10880);
		addStartNpc(CYPHONA);
		addTalkId(CYPHONA, FERIN);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "34055-00.htm");
		addCondStartedQuest(Q10879_ExaltedGuideToPower.class.getSimpleName(), "34055-00.htm");
		registerQuestItems(PROOF_OF_STRENGTH);
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
			case "34055-02.htm":
			case "34055-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34055-04.htm":
			{
				if (hasItem(player, LIONEL_HUNTERS_LIST_PART_5))
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "34054-07.html":
			{
				if (qs.isCond(2))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						takeItems(player, PROOF_OF_STRENGTH, -1);
						giveItems(player, MASTER_CYPHONA_CERTIFICATE, 1);
						addFactionPoints(player, Faction.MOTHER_TREE_GUARDIANS, 4500);
						qs.exitQuest(false, true);
						
						final Quest mainQ = QuestManager.getInstance().getQuest(Q10879_ExaltedGuideToPower.class.getSimpleName());
						if (mainQ != null)
						{
							mainQ.notifyEvent("SUBQUEST_FINISHED_NOTIFY", npc, player);
						}
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
				break;
			}
			case "34054-07a.html":
			{
				if (qs.isCond(2))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						takeItems(player, PROOF_OF_STRENGTH, -1);
						giveItems(player, MASTER_CYPHONA_CERTIFICATE, 1);
						addFactionPoints(player, Faction.UNWORLDLY_VISITORS, 4500);
						qs.exitQuest(false, true);
						
						final Quest mainQ = QuestManager.getInstance().getQuest(Q10879_ExaltedGuideToPower.class.getSimpleName());
						if (mainQ != null)
						{
							mainQ.notifyEvent("SUBQUEST_FINISHED_NOTIFY", npc, player);
						}
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
				break;
			}
			case "34054-07b.html":
			{
				if (qs.isCond(2))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						takeItems(player, PROOF_OF_STRENGTH, -1);
						giveItems(player, MASTER_CYPHONA_CERTIFICATE, 1);
						addFactionPoints(player, Faction.KINGDOM_ROYAL_GUARDS, 4500);
						qs.exitQuest(false, true);
						
						final Quest mainQ = QuestManager.getInstance().getQuest(Q10879_ExaltedGuideToPower.class.getSimpleName());
						if (mainQ != null)
						{
							mainQ.notifyEvent("SUBQUEST_FINISHED_NOTIFY", npc, player);
						}
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
					break;
				}
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
				if ((npc.getId() == CYPHONA) && (hasItem(player, LIONEL_HUNTERS_LIST_PART_5)))
				{
					htmltext = "34055-01.htm";
				}
				else
				{
					htmltext = "noItem.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case CYPHONA:
					{
						if (qs.isCond(1))
						{
							htmltext = "34055-05.html";
						}
						break;
					}
					case FERIN:
					{
						if (qs.isCond(2))
						{
							htmltext = "34054-06.html";
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
		if ((qs != null) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE) && CommonUtil.contains(MONSTERS, npc.getId()))
		{
			giveItems(player, PROOF_OF_STRENGTH, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			
			if (getQuestItemsCount(player, PROOF_OF_STRENGTH) >= 10000)
			{
				qs.setCond(2, true);
			}
		}
	}
}