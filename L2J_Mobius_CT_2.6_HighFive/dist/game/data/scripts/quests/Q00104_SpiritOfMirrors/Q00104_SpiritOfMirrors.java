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
package quests.Q00104_SpiritOfMirrors;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q00281_HeadForTheHills.Q00281_HeadForTheHills;

/**
 * Spirit of Mirrors (104)
 * @author xban1x
 */
public class Q00104_SpiritOfMirrors extends Quest
{
	// NPCs
	private static final int GALLINT = 30017;
	private static final int ARNOLD = 30041;
	private static final int JOHNSTONE = 30043;
	private static final int KENYOS = 30045;
	// Items
	private static final int GALLINTS_OAK_WAND = 748;
	private static final int SPIRITBOUND_WAND1 = 1135;
	private static final int SPIRITBOUND_WAND2 = 1136;
	private static final int SPIRITBOUND_WAND3 = 1137;
	// Monsters
	private static final Map<Integer, Integer> MONSTERS = new HashMap<>();
	static
	{
		MONSTERS.put(27003, SPIRITBOUND_WAND1); // Spirit Of Mirrors
		MONSTERS.put(27004, SPIRITBOUND_WAND2); // Spirit Of Mirrors
		MONSTERS.put(27005, SPIRITBOUND_WAND3); // Spirit Of Mirrors
	}
	// Rewards
	private static final ItemHolder[] REWARDS =
	{
		new ItemHolder(1060, 100), // Lesser Healing Potion
		new ItemHolder(4412, 10), // Echo Crystal - Theme of Battle
		new ItemHolder(4413, 10), // Echo Crystal - Theme of Love
		new ItemHolder(4414, 10), // Echo Crystal - Theme of Solitude
		new ItemHolder(4415, 10), // Echo Crystal - Theme of Feast
		new ItemHolder(4416, 10), // Echo Crystal - Theme of Celebration
		new ItemHolder(747, 1), // Wand of Adept
	};
	// Misc
	private static final int MIN_LEVEL = 10;
	
	public Q00104_SpiritOfMirrors()
	{
		super(104);
		addStartNpc(GALLINT);
		addTalkId(ARNOLD, GALLINT, JOHNSTONE, KENYOS);
		addKillId(MONSTERS.keySet());
		registerQuestItems(GALLINTS_OAK_WAND, SPIRITBOUND_WAND1, SPIRITBOUND_WAND2, SPIRITBOUND_WAND3);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && event.equalsIgnoreCase("30017-04.htm"))
		{
			qs.startQuest();
			giveItems(player, GALLINTS_OAK_WAND, 3);
			return event;
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.isCond(1) || qs.isCond(2)) && (getItemEquipped(killer, Inventory.PAPERDOLL_RHAND) == GALLINTS_OAK_WAND) && !hasQuestItems(killer, MONSTERS.get(npc.getId())))
		{
			takeItems(killer, GALLINTS_OAK_WAND, 1);
			giveItems(killer, MONSTERS.get(npc.getId()), 1);
			if (hasQuestItems(killer, SPIRITBOUND_WAND1, SPIRITBOUND_WAND2, SPIRITBOUND_WAND3))
			{
				qs.setCond(3, true);
			}
			else
			{
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case GALLINT:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = (player.getRace() == Race.HUMAN) ? (player.getLevel() >= MIN_LEVEL) ? "30017-03.htm" : "30017-02.htm" : "30017-01.htm";
						break;
					}
					case State.STARTED:
					{
						if (qs.isCond(3) && hasQuestItems(player, SPIRITBOUND_WAND1, SPIRITBOUND_WAND2, SPIRITBOUND_WAND3))
						{
							Q00281_HeadForTheHills.giveNewbieReward(player);
							for (ItemHolder reward : REWARDS)
							{
								giveItems(player, reward);
							}
							addExpAndSp(player, 39750, 3407);
							giveAdena(player, 16866, true);
							qs.exitQuest(false, true);
							htmltext = "30017-06.html";
						}
						else
						{
							htmltext = "30017-05.html";
						}
						break;
					}
					case State.COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg(player);
						break;
					}
				}
				break;
			}
			case ARNOLD:
			case JOHNSTONE:
			case KENYOS:
			{
				if (qs.isCond(1))
				{
					if (!qs.isSet(npc.getName()))
					{
						qs.set(npc.getName(), "1");
					}
					if (qs.isSet("Arnold") && qs.isSet("Johnstone") && qs.isSet("Kenyos"))
					{
						qs.setCond(2, true);
					}
				}
				htmltext = npc.getId() + "-01.html";
				break;
			}
		}
		return htmltext;
	}
}