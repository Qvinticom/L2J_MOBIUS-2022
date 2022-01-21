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
package quests.Q00938_TheFishermansOtherHobby;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.FishingEndReason;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerFishing;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * The Fisherman's Other Hobby (938)
 * @author CostyKiller
 */
public class Q00938_TheFishermansOtherHobby extends Quest
{
	// NPCs
	private static final int OFULLE = 31572;
	private static final int LINNAEUS = 31577;
	private static final int PERELIN = 31563;
	private static final int BLEAKER = 31567;
	private static final int CYANO = 31569;
	private static final int PAMFUS = 31568;
	private static final int LANOSCO = 31570;
	private static final int HUFS = 31571;
	private static final int MONAKAN = 31573;
	private static final int BERIX = 31576;
	private static final int LITULON = 31575;
	private static final int WILLIE = 31574;
	private static final int HILGENDORF = 31578;
	private static final int PLATIS = 31696;
	private static final int KLAUS = 31579;
	private static final int BATIDAE = 31989;
	private static final int EINDARKNER = 31697;
	private static final int GALBA = 32007;
	private static final int SANTIAGO = 34138;
	// Reward
	private static final int SANTIAGOS_REEL_FRAGMENT = 47562;
	// Misc
	private static final int MIN_LEVEL = 85;
	private static final int NIMBLE_FISH = 47551;
	private static final int NIMBLE_FISH_NEEDED = 40;
	
	public Q00938_TheFishermansOtherHobby()
	{
		super(938);
		addStartNpc(OFULLE, LINNAEUS, PERELIN, BLEAKER, CYANO, PAMFUS, LANOSCO, HUFS, MONAKAN, BERIX, LITULON, WILLIE, HILGENDORF, PLATIS, KLAUS, BATIDAE, EINDARKNER, GALBA, SANTIAGO);
		addTalkId(OFULLE, LINNAEUS, PERELIN, BLEAKER, CYANO, PAMFUS, LANOSCO, HUFS, MONAKAN, BERIX, LITULON, WILLIE, HILGENDORF, PLATIS, KLAUS, BATIDAE, EINDARKNER, GALBA, SANTIAGO);
		addCondMinLevel(MIN_LEVEL, "noLevel.htm");
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
			case "Guild-02.htm":
			case "Guild-03.htm":
			{
				htmltext = event;
				break;
			}
			case "Guild-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "Guild-07.html":
			{
				if (qs.isCond(2))
				{
					takeItems(player, NIMBLE_FISH, NIMBLE_FISH_NEEDED);
					giveItems(player, SANTIAGOS_REEL_FRAGMENT, 2);
					addFactionPoints(player, Faction.FISHING_GUILD, 100);
					qs.exitQuest(QuestType.REPEATABLE, true);
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
				htmltext = "Guild-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "Guild-05.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "Guild-06.html";
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
	
	@RegisterEvent(EventType.ON_PLAYER_FISHING)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerFishing(OnPlayerFishing event)
	{
		if (event.getReason() == FishingEndReason.WIN)
		{
			final Player player = event.getPlayer();
			final QuestState qs = getQuestState(player, false);
			if ((qs != null) && qs.isCond(1))
			{
				if (getQuestItemsCount(player, NIMBLE_FISH) >= NIMBLE_FISH_NEEDED)
				{
					qs.setCond(2, true);
				}
				else
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
	}
}