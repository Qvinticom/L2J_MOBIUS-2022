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
package quests.Q10984_CollectSpiderweb;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExRequestClassChangeUi;

/**
 * Collect Spiderweb (10984)
 * @author RobikBobik, Mobius
 * @Notee: Based on NA server September 2019
 */
public class Q10984_CollectSpiderweb extends Quest
{
	// NPCs
	private static final int HERBIEL = 30150;
	private static final int CAPTAIN_BATHIS = 30332;
	// Monsters
	private static final int HOOK_SPIDER = 20308;
	private static final int CRIMSON_SPIDER = 20460;
	private static final int PINCER_SPIDER = 20466;
	// Items
	private static final int GIANT_COBWEB = 91652;
	private static final ItemHolder SOE_TO_CAPTAIN_BATHIS = new ItemHolder(91651, 1);
	private static final ItemHolder SOE_NOVICE = new ItemHolder(10650, 20);
	private static final ItemHolder SPIRIT_ORE = new ItemHolder(3031, 50);
	private static final ItemHolder HP_POTS = new ItemHolder(91912, 50); // TODO: Finish Item
	private static final ItemHolder RICE_CAKE_OF_FLAMING_FIGHTING_SPIRIT_EVENT = new ItemHolder(91840, 1);
	// HELMET FOR ALL ARMORS
	private static final ItemHolder MOON_HELMET = new ItemHolder(7850, 1);
	// HEAVY
	private static final ItemHolder MOON_ARMOR = new ItemHolder(7851, 1);
	private static final ItemHolder MOON_GAUNTLETS = new ItemHolder(7852, 1);
	private static final ItemHolder MOON_BOOTS = new ItemHolder(7853, 1);
	// LIGHT
	private static final ItemHolder MOON_SHELL = new ItemHolder(7854, 1);
	private static final ItemHolder MOON_LEATHER_GLOVES = new ItemHolder(7855, 1);
	private static final ItemHolder MOON_SHOES = new ItemHolder(7856, 1);
	// ROBE
	private static final ItemHolder MOON_CAPE = new ItemHolder(7857, 1);
	private static final ItemHolder MOON_SILK = new ItemHolder(7858, 1);
	private static final ItemHolder MOON_SANDALS = new ItemHolder(7859, 1);
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10984_CollectSpiderweb()
	{
		super(10984);
		addStartNpc(HERBIEL);
		addTalkId(HERBIEL, CAPTAIN_BATHIS);
		addKillId(HOOK_SPIDER, CRIMSON_SPIDER, PINCER_SPIDER);
		registerQuestItems(GIANT_COBWEB);
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_15_20_SPIDER_WEB);
	}
	
	@Override
	public boolean checkPartyMember(Player member, Npc npc)
	{
		final QuestState qs = getQuestState(member, false);
		return ((qs != null) && qs.isStarted());
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
			case "30150-01.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30332-01.html":
			{
				htmltext = event;
				break;
			}
			case "30332-02.html":
			{
				htmltext = event;
				break;
			}
			case "30332-03.html":
			{
				htmltext = event;
				break;
			}
			case "30332.html":
			{
				htmltext = event;
				break;
			}
			case "TELEPORT_TO_HUNTING_GROUND":
			{
				player.teleToLocation(5135, 68148, -3256);
				break;
			}
			case "NEXT_QUEST":
			{
				htmltext = "30150.htm";
				break;
			}
			case "HeavyArmor.html":
			{
				if (qs.isStarted())
				{
					addExpAndSp(player, 600000, 13500);
					giveItems(player, SOE_NOVICE);
					giveItems(player, SPIRIT_ORE);
					giveItems(player, HP_POTS);
					giveItems(player, RICE_CAKE_OF_FLAMING_FIGHTING_SPIRIT_EVENT);
					giveItems(player, MOON_HELMET);
					giveItems(player, MOON_ARMOR);
					giveItems(player, MOON_GAUNTLETS);
					giveItems(player, MOON_BOOTS);
					if (CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()))
					{
						showOnScreenMsg(player, NpcStringId.YOU_VE_FINISHED_THE_TUTORIAL_NTAKE_YOUR_1ST_CLASS_TRANSFER_AND_COMPLETE_YOUR_TRAINING_WITH_BATHIS_TO_BECOME_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
					}
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "LightArmor.html":
			{
				if (qs.isStarted())
				{
					addExpAndSp(player, 600000, 13500);
					giveItems(player, SOE_NOVICE);
					giveItems(player, SPIRIT_ORE);
					giveItems(player, HP_POTS);
					giveItems(player, RICE_CAKE_OF_FLAMING_FIGHTING_SPIRIT_EVENT);
					giveItems(player, MOON_HELMET);
					giveItems(player, MOON_SHELL);
					giveItems(player, MOON_LEATHER_GLOVES);
					giveItems(player, MOON_SHOES);
					if (CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()))
					{
						showOnScreenMsg(player, NpcStringId.YOU_VE_FINISHED_THE_TUTORIAL_NTAKE_YOUR_1ST_CLASS_TRANSFER_AND_COMPLETE_YOUR_TRAINING_WITH_BATHIS_TO_BECOME_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
					}
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "Robe.html":
			{
				if (qs.isStarted())
				{
					addExpAndSp(player, 600000, 13500);
					giveItems(player, SOE_NOVICE);
					giveItems(player, SPIRIT_ORE);
					giveItems(player, HP_POTS);
					giveItems(player, RICE_CAKE_OF_FLAMING_FIGHTING_SPIRIT_EVENT);
					giveItems(player, MOON_HELMET);
					giveItems(player, MOON_CAPE);
					giveItems(player, MOON_SILK);
					giveItems(player, MOON_SANDALS);
					if (CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()))
					{
						showOnScreenMsg(player, NpcStringId.YOU_VE_FINISHED_THE_TUTORIAL_NTAKE_YOUR_1ST_CLASS_TRANSFER_AND_COMPLETE_YOUR_TRAINING_WITH_BATHIS_TO_BECOME_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
					}
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
		if (qs.isCreated())
		{
			htmltext = "30150.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case HERBIEL:
				{
					if (qs.isCond(1))
					{
						htmltext = "30150-01.html";
					}
					break;
				}
				case CAPTAIN_BATHIS:
				{
					if (qs.isCond(2))
					{
						htmltext = "30332.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == HERBIEL)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			if (getQuestItemsCount(killer, GIANT_COBWEB) < 30)
			{
				giveItems(killer, GIANT_COBWEB, 1, true);
			}
			if (getQuestItemsCount(killer, GIANT_COBWEB) >= 30)
			{
				qs.setCond(2, true);
				killer.sendPacket(new ExShowScreenMessage("You hunted all monsters.#Use the Scroll of Escape in you inventory to go to Captain Bathis in the Town of Gludio.", 5000));
				giveItems(killer, SOE_TO_CAPTAIN_BATHIS);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if (Config.DISABLE_TUTORIAL || ((qs != null) && qs.isCompleted()))
		{
			player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
		}
	}
}