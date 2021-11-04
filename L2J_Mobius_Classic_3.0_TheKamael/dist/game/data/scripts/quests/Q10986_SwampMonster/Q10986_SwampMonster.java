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
package quests.Q10986_SwampMonster;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExRequestClassChangeUi;

/**
 * Swamp Monsters (10986)
 * @author RobikBobik
 * @Notee: Based on NA server September 2019
 */
public class Q10986_SwampMonster extends Quest
{
	// NPCs
	private static final int VOLODOS = 30137;
	private static final int CAPTAIN_BATHIS = 30332;
	// Monsters
	private static final int MARSH_ZOMBIE = 20015; // NOTE: Zombie Forest Elf in old client
	private static final int MARSH_ZOMBIE_SCOUT = 20020; // NOTE: Zombie Forest Elf Resercher in old client
	private static final int DARK_HORROR = 20105;
	private static final int LESSER_DARK_HORROR = 20025;
	// Items
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
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10986_SwampMonster()
	{
		super(10986);
		addStartNpc(VOLODOS);
		addTalkId(VOLODOS, CAPTAIN_BATHIS);
		addKillId(MARSH_ZOMBIE, MARSH_ZOMBIE_SCOUT, DARK_HORROR, LESSER_DARK_HORROR);
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_15_20_TERRIBLE_SWAMP_MONSTERS);
	}
	
	@Override
	public boolean checkPartyMember(PlayerInstance member, Npc npc)
	{
		final QuestState qs = getQuestState(member, false);
		return ((qs != null) && qs.isStarted());
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "30137-01.html":
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
				player.teleToLocation(-19004, 47388, -3608);
				break;
			}
			case "NEXT_QUEST":
			{
				htmltext = "30137.htm";
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
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 30)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(2, true);
				qs.unset(KILL_COUNT_VAR);
				killer.sendPacket(new ExShowScreenMessage("You hunted all monsters.#Use the Scroll of Escape in you inventory to go to Captain Bathis in the Town of Gludio.", 5000));
				giveItems(killer, SOE_TO_CAPTAIN_BATHIS);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.KILL_ZOMBIES_AND_DARK_HORRORS.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			htmltext = "30137.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case VOLODOS:
				{
					if (qs.isCond(1))
					{
						htmltext = "30137-01.html";
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
			if (npc.getId() == VOLODOS)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final PlayerInstance player = event.getPlayer();
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