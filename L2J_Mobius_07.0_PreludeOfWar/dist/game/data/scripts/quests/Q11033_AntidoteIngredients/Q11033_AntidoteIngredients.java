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
package quests.Q11033_AntidoteIngredients;

import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.ExTutorialShowId;

import quests.Q11034_ResurrectedOne.Q11034_ResurrectedOne;

/**
 * Antidote Ingredients (11033)
 * @URL https://l2wiki.com/Antidote_Ingredients
 * @author Dmitri, Mobius
 */
public class Q11033_AntidoteIngredients extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int KALESIN = 33177;
	private static final int KRAKOS_BAT = 24384;
	private static final int A_VAMMPIRE = 24385;
	// Items
	private static final int SECRET_MATERIAL = 80671;
	private static final ItemHolder SOE_KALESIN = new ItemHolder(80679, 1);
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-44121, 115926, -3624);
	// Misc
	private static final int MIN_LEVEL = 20;
	
	public Q11033_AntidoteIngredients()
	{
		super(11033);
		addStartNpc(TARTI);
		addTalkId(TARTI, KALESIN);
		addKillId(KRAKOS_BAT, A_VAMMPIRE);
		registerQuestItems(SOE_KALESIN.getId(), SECRET_MATERIAL);
		addCondMinLevel(MIN_LEVEL, "34505-06.html");
		setQuestNameNpcStringId(NpcStringId.LV_20_40_ANTIDOTE_INGREDIENTS);
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
			case "34505-02.html":
			case "34505-05.html":
			{
				htmltext = event;
				break;
			}
			case "34505-03.html":
			{
				player.sendPacket(new ExTutorialShowId(18)); // Quest Progress
				showOnScreenMsg(player, NpcStringId.PRESS_ALT_K_TO_ACQUIRE_THE_SKILL_IN_THE_SKILL_WINDOW, ExShowScreenMessage.TOP_CENTER, 10000);
				htmltext = event;
				break;
			}
			case "34505-04.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT);
				}
				break;
			}
			case "33177-02.html":
			{
				if (qs.isCond(2))
				{
					addExpAndSp(player, 913551, 822);
					qs.exitQuest(false, true);
					htmltext = event;
					
					// Initialize next quest.
					final Quest nextQuest = QuestManager.getInstance().getQuest(Q11034_ResurrectedOne.class.getSimpleName());
					if (nextQuest != null)
					{
						nextQuest.newQuestState(player);
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
				if (npc.getId() == TARTI)
				{
					htmltext = "34505-01.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case TARTI:
					{
						if (qs.isCond(1))
						{
							htmltext = "34505-04.html";
						}
						break;
					}
					case KALESIN:
					{
						if (qs.isCond(2))
						{
							htmltext = "33177-01.html";
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
		if ((qs != null) && qs.isCond(1) && giveItemRandomly(killer, SECRET_MATERIAL, 1, 15, 0.5, true))
		{
			qs.setCond(2, true);
			giveItems(killer, SOE_KALESIN);
			showOnScreenMsg(killer, NpcStringId.USE_SCROLL_OF_ESCAPE_PIO_IN_YOUR_INVENTORY_NTALK_TO_PIO_TO_COMPLETE_THE_QUEST, ExShowScreenMessage.TOP_CENTER, 10000);
		}
		return super.onKill(npc, killer, isSummon);
	}
}
