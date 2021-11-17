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
package quests.Q10886_SaviorsPathSearchTheRefinery;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10885_SaviorsPathDiscovery.Q10885_SaviorsPathDiscovery;

/**
 * Savior's Path - Search the Refinery (10886)
 * @URL https://l2wiki.com/Savior%27s_Path_-_Search_the_Refinery
 * @author CostyKiller
 */
public class Q10886_SaviorsPathSearchTheRefinery extends Quest
{
	// NPCs
	private static final int LEONA_BLACKBIRD = 34425;
	private static final int DEVIANNE = 34427;
	// Monsters
	private static final int ATELIA_YUYURINA = 24159;
	private static final int ATELIA_POPOBENA = 24160;
	// Items
	private static final int TOKEN_OF_ETINA = 48546;
	// Misc
	private static final int MIN_LEVEL = 103;
	private static final int TOKEN_OF_ETINA_NEEDED = 20;
	
	public Q10886_SaviorsPathSearchTheRefinery()
	{
		super(10886);
		addStartNpc(LEONA_BLACKBIRD);
		addTalkId(LEONA_BLACKBIRD, DEVIANNE);
		addKillId(ATELIA_YUYURINA, ATELIA_POPOBENA);
		addCondMinLevel(MIN_LEVEL, "34425-00.html");
		addCondCompletedQuest(Q10885_SaviorsPathDiscovery.class.getSimpleName(), "34425-00.html");
		registerQuestItems(TOKEN_OF_ETINA);
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
			case "34425-02.htm":
			case "34425-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34425-04.html":
			{
				if ((player.getLevel() >= MIN_LEVEL))
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = event;
				}
				break;
			}
			case "34427-02.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2);
				}
				htmltext = event;
				break;
			}
			case "34427-05.html":
			{
				if (qs.isCond(3))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						addExpAndSp(player, 27191624760L, 27191610);
						giveAdena(player, 3077301, true);
						qs.exitQuest(false, true);
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
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
				htmltext = "34425-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case LEONA_BLACKBIRD:
					{
						if (qs.isCond(1) && (qs.getMemoState() != 1))
						{
							htmltext = "34425-01.htm";
						}
						else
						{
							htmltext = "34425-05.html";
						}
						break;
					}
					case DEVIANNE:
					{
						if (qs.isCond(1))
						{
							htmltext = "34427-01.htm";
						}
						else if (qs.isCond(2) && (getQuestItemsCount(player, TOKEN_OF_ETINA) < TOKEN_OF_ETINA_NEEDED))
						{
							htmltext = "34427-03.html";
						}
						else if (qs.isCond(3) && (getQuestItemsCount(player, TOKEN_OF_ETINA) >= TOKEN_OF_ETINA_NEEDED))
						{
							htmltext = "34427-04.htm";
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
		if ((qs != null) && qs.isCond(2) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			if (getQuestItemsCount(player, TOKEN_OF_ETINA) < TOKEN_OF_ETINA_NEEDED)
			{
				giveItems(player, TOKEN_OF_ETINA, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if (getQuestItemsCount(player, TOKEN_OF_ETINA) >= TOKEN_OF_ETINA_NEEDED)
			{
				qs.setCond(3, true);
			}
		}
	}
}