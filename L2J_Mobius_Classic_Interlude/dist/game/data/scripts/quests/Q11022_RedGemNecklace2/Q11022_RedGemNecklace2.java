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
package quests.Q11022_RedGemNecklace2;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q11021_RedGemNecklace1.Q11021_RedGemNecklace1;

/**
 * Red Gem Necklace (2/3) (11022)
 * @author Stayway
 */
public class Q11022_RedGemNecklace2 extends Quest
{
	// NPCs
	private static final int USKA = 30560;
	// Items
	private static final int BEARS_SHIN_BONE = 90278;
	private static final int SHARP_SPIDER_LEG = 90279;
	private static final int LIST_OF_MATERIALS = 90277;
	// Rewards
	private static final int SCROLL_OF_ESCAPE = 10650;
	private static final int HEALING_POTION = 1073;
	private static final int MP_RECOVERY_POTION = 90310;
	private static final int SOULSHOTS_NO_GRADE = 5789;
	private static final int SPIRITSHOT_NO_GRADE = 5790;
	// Monsters
	private static final int KASHA_BEAR = 20479;
	private static final int KASHA_SPIDER = 20474;
	private static final int KASHA_FANG_SPIDER = 20476;
	private static final int KASHA_BLADE_SPIDER = 20478;
	// Misc
	private static final int MIN_LEVEL = 15;
	private static final int MAX_LEVEL = 20;
	
	public Q11022_RedGemNecklace2()
	{
		super(11022);
		addStartNpc(USKA);
		addTalkId(USKA);
		addKillId(KASHA_BEAR, KASHA_SPIDER, KASHA_FANG_SPIDER, KASHA_BLADE_SPIDER);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no-level.html"); // Custom
		addCondRace(Race.ORC, "no-race.html"); // Custom
		addCondCompletedQuest(Q11021_RedGemNecklace1.class.getSimpleName(), "30560-06.html");
		registerQuestItems(LIST_OF_MATERIALS, BEARS_SHIN_BONE, SHARP_SPIDER_LEG);
		setQuestNameNpcStringId(NpcStringId.LV_15_RED_GEM_NECKLACE_2_3);
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
			case "abort.html":
			{
				htmltext = event;
				break;
			}
			case "30560-02.htm":
			{
				qs.startQuest();
				qs.setCond(2);
				showOnScreenMsg(player, NpcStringId.GO_HUNTING_AND_KILL_KASHA_BEAR_2, ExShowScreenMessage.TOP_CENTER, 10000);
				giveItems(player, LIST_OF_MATERIALS, 1);
				htmltext = event;
				break;
			}
			case "reward1":
			{
				if (qs.isCond(4))
				{
					takeItems(player, LIST_OF_MATERIALS, 1);
					takeItems(player, BEARS_SHIN_BONE, 20);
					takeItems(player, SHARP_SPIDER_LEG, 30);
					giveItems(player, SCROLL_OF_ESCAPE, 5);
					giveItems(player, HEALING_POTION, 40);
					giveItems(player, MP_RECOVERY_POTION, 40);
					giveItems(player, SOULSHOTS_NO_GRADE, 1000);
					addExpAndSp(player, 70000, 3600);
					qs.exitQuest(false, true);
					htmltext = "30560-04.html";
				}
				break;
			}
			case "reward2":
			{
				if (qs.isCond(4))
				{
					takeItems(player, LIST_OF_MATERIALS, 1);
					takeItems(player, BEARS_SHIN_BONE, 20);
					takeItems(player, SHARP_SPIDER_LEG, 30);
					giveItems(player, SCROLL_OF_ESCAPE, 5);
					giveItems(player, HEALING_POTION, 40);
					giveItems(player, MP_RECOVERY_POTION, 40);
					giveItems(player, SPIRITSHOT_NO_GRADE, 1000);
					addExpAndSp(player, 70000, 3600);
					qs.exitQuest(false, true);
					htmltext = "30560-05.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final QuestState qs = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "30560-01.html";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(2))
				{
					htmltext = "30560-02a.html";
				}
				else if (qs.isCond(4))
				{
					htmltext = "30560-03.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(talker);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if (qs != null)
		{
			switch (npc.getId())
			{
				case KASHA_BEAR:
				{
					if (qs.isCond(2) && (getQuestItemsCount(killer, BEARS_SHIN_BONE) < 20) && (getRandom(100) < 92))
					{
						giveItems(killer, BEARS_SHIN_BONE, 1);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if (getQuestItemsCount(killer, BEARS_SHIN_BONE) >= 20)
						{
							showOnScreenMsg(killer, NpcStringId.YOU_HAVE_KILLED_ENOUGH_KASHA_BEARS_N_GO_HUNTING_AND_KILL_KASHA_SPIDERS, ExShowScreenMessage.TOP_CENTER, 10000);
							qs.setCond(3);
						}
					}
					break;
				}
				case KASHA_SPIDER:
				case KASHA_FANG_SPIDER:
				case KASHA_BLADE_SPIDER:
				{
					if (qs.isCond(3) && (getQuestItemsCount(killer, SHARP_SPIDER_LEG) < 30) && (getRandom(100) < 89))
					{
						giveItems(killer, SHARP_SPIDER_LEG, 1);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if (getQuestItemsCount(killer, SHARP_SPIDER_LEG) >= 30)
						{
							showOnScreenMsg(killer, NpcStringId.YOU_HAVE_KILLED_ENOUGH_KASHA_SPIDERS_NRETURN_TO_ACCESSORY_MERCHANT_USKA, ExShowScreenMessage.TOP_CENTER, 10000);
							qs.setCond(4);
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}