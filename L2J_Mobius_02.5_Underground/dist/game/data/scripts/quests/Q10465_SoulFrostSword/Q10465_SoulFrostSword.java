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
package quests.Q10465_SoulFrostSword;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

/**
 * Soul Frost Sword (10465)
 * @URL http://l2on.net/?c=quests&id=10465
 * @author Gigi
 */
public class Q10465_SoulFrostSword extends Quest
{
	// NPC
	private static final int RUPIO = 30471;
	// Items
	private static final ItemHolder ADENA = new ItemHolder(57, 700000);
	private static final int PRACTICE_STORMBRINGER = 46629;
	private static final int PRACTICE_SOUL_CRYSTAL = 46526;
	// Misc
	private static final int MIN_LEVEL = 40;
	// Reward
	private static final int EXP_REWARD = 336000;
	private static final int SP_REWARD = 403;
	
	public Q10465_SoulFrostSword()
	{
		super(10465);
		addStartNpc(RUPIO);
		addTalkId(RUPIO);
		registerQuestItems(PRACTICE_STORMBRINGER, PRACTICE_SOUL_CRYSTAL);
		addCondMinLevel(MIN_LEVEL, "noLevel.html");
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
			case "30471-02.htm":
			case "30471-03.htm":
			{
				htmltext = event;
				break;
			}
			case "30471-04.htm":
			{
				qs.startQuest();
				giveItems(player, PRACTICE_STORMBRINGER, 1);
				giveItems(player, PRACTICE_SOUL_CRYSTAL, 1);
				player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_028_ensoul_01.htm", TutorialShowHtml.LARGE_WINDOW));
				htmltext = event;
				break;
			}
			case "30471-06.html":
			{
				player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_028_ensoul_01.htm", TutorialShowHtml.LARGE_WINDOW));
				htmltext = event;
				break;
			}
			case "30471-08.html":
			{
				takeItems(player, PRACTICE_STORMBRINGER, -1);
				giveItems(player, ADENA);
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
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
		final ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			htmltext = "30471-01.htm";
		}
		else if ((qs.getPlayer().getActiveWeaponItem() == null) || (wpn.getId() != PRACTICE_STORMBRINGER))
		{
			htmltext = "Weapon.html";
		}
		else if (qs.isCond(1))
		{
			if ((getQuestItemsCount(player, PRACTICE_SOUL_CRYSTAL) < 1) && (wpn.getId() == PRACTICE_STORMBRINGER))
			{
				htmltext = "30471-07.html";
			}
			else
			{
				htmltext = "30471-05.html";
			}
		}
		else if (qs.isNowAvailable())
		{
			if (getQuestItemsCount(player, PRACTICE_STORMBRINGER) > 0)
			{
				takeItems(player, PRACTICE_STORMBRINGER, -1);
			}
			else if (getQuestItemsCount(player, PRACTICE_SOUL_CRYSTAL) > 0)
			{
				takeItems(player, PRACTICE_SOUL_CRYSTAL, -1);
			}
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
}