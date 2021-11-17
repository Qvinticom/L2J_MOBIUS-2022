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
package quests.Q10844_BloodyBattleSeizingSupplies;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * Bloody Battle - Seizing Supplies (10844)
 * @URL https://l2wiki.com/Bloody_Battle_-_Seizing_Supplies
 * @author Dmitri
 */
public class Q10844_BloodyBattleSeizingSupplies extends Quest
{
	// NPC
	private static final int ELIKIA = 34057;
	private static final int GLENKINCHIE = 34063;
	private static final int EMBRYO_SUPPLY_BOX = 34137;
	// Monsters
	private static final int FORTRESS_GUARDIAN_CAPTAIN = 23506;
	private static final int FORTRESS_RAIDER = 23505;
	// Items
	private static final int EMBRYO_SUPPLIES = 46282;
	// Misc
	private static final int MIN_LEVEL = 101;
	
	public Q10844_BloodyBattleSeizingSupplies()
	{
		super(10844);
		addStartNpc(ELIKIA);
		addTalkId(ELIKIA, GLENKINCHIE);
		addFirstTalkId(EMBRYO_SUPPLY_BOX);
		registerQuestItems(EMBRYO_SUPPLIES);
		addCondMinLevel(MIN_LEVEL, "34057-00.htm");
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
			case "34057-04.htm":
			case "34057-03.htm":
			case "34057-02.htm":
			case "34063-02.html":
			{
				htmltext = event;
				break;
			}
			case "34057-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34063-03.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "open_box":
			{
				if (qs.isCond(2))
				{
					npc.deleteMe();
					if (getRandom(10) < 5)
					{
						if (qs.isCond(2) && (getQuestItemsCount(qs.getPlayer(), EMBRYO_SUPPLIES) < 19))
						{
							giveItems(player, EMBRYO_SUPPLIES, 1);
							playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
						else
						{
							giveItems(player, EMBRYO_SUPPLIES, 1);
							qs.setCond(3, true);
						}
						break;
					}
					final Npc captain = addSpawn(FORTRESS_GUARDIAN_CAPTAIN, npc, true, 120000, false);
					captain.setTitleString(NpcStringId.SUPPLY_GUARDS);
					addAttackPlayerDesire(captain, player);
					for (int i = 0; i < 2; i++)
					{
						final Npc raider = addSpawn(FORTRESS_RAIDER, npc, true, 120000, false);
						raider.setTitleString(NpcStringId.SUPPLY_GUARDS);
						addAttackPlayerDesire(raider, player);
					}
				}
				break;
			}
			case "34063-06.html":
			{
				if (qs.isCond(3))
				{
					addExpAndSp(player, 7262301690L, 17429400);
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
				if (npc.getId() == ELIKIA)
				{
					htmltext = "34057-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case ELIKIA:
					{
						if (qs.getCond() > 1)
						{
							htmltext = "34057-06.html";
						}
						break;
					}
					case GLENKINCHIE:
					{
						if (qs.isCond(1))
						{
							htmltext = "34063-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34063-04.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34063-05.html";
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
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34137.html";
	}
}
