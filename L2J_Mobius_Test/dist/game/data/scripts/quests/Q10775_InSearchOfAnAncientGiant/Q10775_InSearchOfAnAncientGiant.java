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
package quests.Q10775_InSearchOfAnAncientGiant;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;

/**
 * In Search of an Ancient Giant (10775)
 * @URL https://l2wiki.com/In_Search_of_an_Ancient_Giant
 * @author Gigi
 */
public class Q10775_InSearchOfAnAncientGiant extends Quest
{
	// NPCs
	private static final int RAMBEL = 30487;
	private static final int BELKADHI = 30485;
	// Monsters
	private static final int[] MONSTERS =
	{
		20753, // Dark Lord
		20754, // Dark Knight
		21040, // Soldier of Darkness
		21037, // Ossiud
		20221, // Perum
		21038, // Liangma
		23153, // Achelando
		23154, // Styrindo
		23155, // Ashende
	};
	// Items
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 46);
	private static final ItemHolder EAC = new ItemHolder(952, 9);
	private static final int ENERGY_OF_REGENERATION = 39715;
	// Reward
	private static final int EXP_REWARD = 2342300;
	private static final int SP_REWARD = 562;
	// Misc
	private static final int MIN_LEVEL = 46;
	
	public Q10775_InSearchOfAnAncientGiant()
	{
		super(10775, Q10775_InSearchOfAnAncientGiant.class.getSimpleName(), "In Search of an Ancient Giant");
		addStartNpc(RAMBEL);
		addTalkId(RAMBEL, BELKADHI);
		registerQuestItems(ENERGY_OF_REGENERATION);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "noLevel.html");
		addCondRace(Race.ERTHEIA, "noErtheia.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		switch (event)
		{
			case "30487-02.htm":
			case "30487-03.htm":
			case "30487-04.htm":
			case "30485-02.htm":
			case "30485-03.htm":
			{
				htmltext = event;
				break;
			}
			case "30487-05.html":
			{
				qs.startQuest();
				htmltext = event;
				qs.set(Integer.toString(ENERGY_OF_REGENERATION), 0);
				break;
			}
			case "30485-04.html":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAC);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (npc.getId())
		{
			case RAMBEL:
			{
				if (qs.isCreated())
				{
					htmltext = "30487-01.htm";
				}
				else if (qs.isStarted())
				{
					if (qs.getCond() > 0)
					{
						htmltext = "30487-06.html";
					}
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case BELKADHI:
			{
				if (qs.isCond(2))
				{
					htmltext = "30485-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, npc, ENERGY_OF_REGENERATION, 1, 20, 0.2, true))
		{
			qs.setCond(2, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}