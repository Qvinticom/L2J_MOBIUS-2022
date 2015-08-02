/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q10333_DisappearedSakum;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jserver.gameserver.util.Util;

/**
 * Disappeared Sakum (10333)
 * @author spider
 */
public class Q10333_DisappearedSakum extends Quest
{
	// Npcs
	private static final int BATHIS = 30332;
	private static final int VENT = 33176;
	private static final int SCHUNAIN = 33508;
	// Monsters
	private static final int LANGK_LIZARDMAN = 20030; // Langk Lizardman
	private static final int VUKU_ORC_FIGHTER = 20017; // WARRIOR on l2wiki.com
	private static final int LANGK_LIZARDMAN_REQUIRED = 7;
	private static final int VUKU_ORC_FIGHTER_REQUIRED = 5;
	private static final int POISONOUS_SPIDER = 23094;
	private static final int VENOMOUS_SPIDER = 20038;
	private static final int POISON_PREDATOR = 20050; // arachnid predator on l2wiki.com
	private static final int SUSPICIOUS_BADGE = 17583; // suspicious mark on l2wiki.com
	// Rewards
	private static final long ADENA_REWARD = 800;
	private static final int EXP_REWARD = 180000;
	private static final int SP_REWARD = 43;
	// Other
	private static final int SUSPICIOUS_BADGE_REQUIRED = 5;
	
	public Q10333_DisappearedSakum()
	{
		super(10333, Q10333_DisappearedSakum.class.getSimpleName(), "Disappeared Sakum");
		addStartNpc(BATHIS);
		addTalkId(BATHIS, VENT, SCHUNAIN);
		addKillId(LANGK_LIZARDMAN, VUKU_ORC_FIGHTER, POISONOUS_SPIDER, VENOMOUS_SPIDER, POISON_PREDATOR);
		registerQuestItems(SUSPICIOUS_BADGE);
		addCondLevel(18, 40, "no_level.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		String htmltext = null;
		switch (event)
		{
			case "30332-02.htm":
			case "30332-03.htm":
			case "30332-04.htm": // just show the dialogs with rewards
			{
				htmltext = event;
				break;
			}
			case "30332-05.htm": // start the quest
			{
				qs.startQuest();
				qs.setCond(2);
				qs.setCond(1); // arrow hack, required for that quest
				htmltext = event;
				break;
			}
			case "33176-01.html":
			case "33176-02.html":
			{
				htmltext = event;
				break;
			}
			case "33176-03.html":
			{
				qs.setCond(2);
				qs.set(Integer.toString(LANGK_LIZARDMAN), 0);
				qs.set(Integer.toString(VUKU_ORC_FIGHTER), 0);
				htmltext = event;
				break;
			}
			case "33508-01.html":
			case "33508-02.html":
			{
				htmltext = event;
				break;
			}
			case "33508-03.html":
			{
				giveAdena(player, ADENA_REWARD, true);
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				qs.exitQuest(false);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, true);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case BATHIS:
					{
						htmltext = "30332-01.htm";
						break;
					}
					case VENT:
					{
						htmltext = getNoQuestMsg(player);
						break;
					}
					case SCHUNAIN:
					{
						htmltext = getNoQuestMsg(player);
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case BATHIS:
					{
						htmltext = "30332-06.html";
						break;
					}
					case VENT:
						if (qs.isCond(1))
						{
							htmltext = "33176-01.html";
						}
						else
						{
							htmltext = "33176-04.html";
						}
						break;
					case SCHUNAIN:
					{
						if (qs.isCond(1) || qs.isCond(2))
						{
							htmltext = getNoQuestMsg(player);
						}
						else if (qs.isCond(3))
						{
							htmltext = "33508-01.html";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				switch (npc.getId())
				{
					case BATHIS:
					{
						htmltext = "30332-07.html";
						break;
					}
					case VENT:
					{
						htmltext = "33176-05.html";
						break;
					}
					case SCHUNAIN:
					{
						htmltext = "33508-04.html";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && qs.isCond(2) && Util.checkIfInRange(1500, npc, qs.getPlayer(), false))
		{
			switch (npc.getId())
			{
				case LANGK_LIZARDMAN:
				{
					int kills = qs.getInt(Integer.toString(LANGK_LIZARDMAN));
					if (kills < LANGK_LIZARDMAN_REQUIRED)
					{
						kills++;
						qs.set(Integer.toString(LANGK_LIZARDMAN), kills);
					}
					break;
				}
				case VUKU_ORC_FIGHTER:
				{
					int kills = qs.getInt(Integer.toString(VUKU_ORC_FIGHTER));
					if (kills < VUKU_ORC_FIGHTER_REQUIRED)
					{
						kills++;
						qs.set(Integer.toString(VUKU_ORC_FIGHTER), kills);
					}
					break;
				}
				case POISONOUS_SPIDER:
				case VENOMOUS_SPIDER:
				case POISON_PREDATOR:
				{
					if (getQuestItemsCount(qs.getPlayer(), SUSPICIOUS_BADGE) < SUSPICIOUS_BADGE_REQUIRED)
					{
						giveItemRandomly(qs.getPlayer(), npc, SUSPICIOUS_BADGE, 1, SUSPICIOUS_BADGE_REQUIRED, 0.5, true);
					}
					break;
				}
			}
			
			ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(LANGK_LIZARDMAN, qs.getInt(Integer.toString(LANGK_LIZARDMAN)));
			log.addNpc(VUKU_ORC_FIGHTER, qs.getInt(Integer.toString(VUKU_ORC_FIGHTER)));
			killer.sendPacket(log);
			
			if ((qs.getInt(Integer.toString(LANGK_LIZARDMAN)) >= LANGK_LIZARDMAN_REQUIRED) && (qs.getInt(Integer.toString(VUKU_ORC_FIGHTER)) >= VUKU_ORC_FIGHTER_REQUIRED))
			{
				qs.setCond(3);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
