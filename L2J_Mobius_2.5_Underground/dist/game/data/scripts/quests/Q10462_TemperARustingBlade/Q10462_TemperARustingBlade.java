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
package quests.Q10462_TemperARustingBlade;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.Id;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerAugment;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Temper a Rusting Blade (10462) TODO Update to Helios cronicle. Quest start Iv 85
 * @URL https://l2wiki.com/Temper_a_Rusting_Blade
 * @author Gigi
 */
public class Q10462_TemperARustingBlade extends Quest
{
	// NPCs
	private static final int FLUTTER = 30677;
	// quest_items
	private static final int PRACTICE_WEAPON = 36717;
	private static final int PRACTICE_LIFE_STONE = 36718;
	private static final int PRACTICE_LIFE_GEMSTONE = 36719;
	// Misc
	private static final int MIN_LEVEL = 46;
	private static final int MAX_LEVEL = 52;
	
	public Q10462_TemperARustingBlade()
	{
		super(10462);
		addStartNpc(FLUTTER);
		addTalkId(FLUTTER);
		registerQuestItems(PRACTICE_WEAPON, PRACTICE_LIFE_STONE, PRACTICE_LIFE_GEMSTONE);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "30677-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30677-02.htm":
			case "30677-03.htm":
			case "30677-04.htm":
			{
				htmltext = event;
				break;
			}
			case "30677-05.htm":
			{
				qs.startQuest();
				giveItems(player, PRACTICE_WEAPON, 1);
				giveItems(player, PRACTICE_LIFE_STONE, 1);
				giveItems(player, PRACTICE_LIFE_GEMSTONE, 25);
				htmltext = event;
				break;
			}
			case "30677-08.html":
			{
				addExpAndSp(player, 504210, 121);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "30677-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "30677-06.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "30677-07.html";
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
	
	@RegisterEvent(EventType.ON_PLAYER_AUGMENT)
	@RegisterType(ListenerRegisterType.ITEM)
	@Id(PRACTICE_WEAPON)
	public void onItemAugment(OnPlayerAugment event)
	{
		final PlayerInstance player = event.getPlayer();
		final QuestState qs = getQuestState(player, false);
		final ItemInstance item = qs.getPlayer().getInventory().getItemByItemId(PRACTICE_WEAPON);
		if ((item != null) && qs.isCond(1) && item.isAugmented())
		{
			qs.setCond(2, true);
		}
	}
}