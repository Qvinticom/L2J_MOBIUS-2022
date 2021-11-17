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
package quests.Q11010_NewPotionDevelopment2;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q11009_NewPotionDevelopment1.Q11009_NewPotionDevelopment1;

/**
 * New Potion Development (2/3) (11010)
 * @author Stayway
 */
public class Q11010_NewPotionDevelopment2 extends Quest
{
	// NPCs
	private static final int HERBIEL = 30150;
	// Items
	private static final int SPIDER_ICHOR = 90232;
	private static final int MOONSTONE_BEAST_SCALES = 90233;
	private static final int MEDICINE_RESEARCH = 90231;
	// Rewards
	private static final int SCROLL_OF_ESCAPE = 10650;
	private static final int HEALING_POTION = 1073;
	private static final int MP_RECOVERY_POTION = 90310;
	private static final int SOULSHOTS_NO_GRADE = 5789;
	private static final int SPIRITSHOT_NO_GRADE = 5790;
	// Monsters
	private static final int SCAVENGER_SPIDER = 20410;
	private static final int RED_SCAVENGER_SPIDER = 20393;
	private static final int MOONSTONE_BEAST = 20369;
	// Misc
	private static final int MIN_LEVEL = 15;
	private static final int MAX_LEVEL = 20;
	
	public Q11010_NewPotionDevelopment2()
	{
		super(11010);
		addStartNpc(HERBIEL);
		addTalkId(HERBIEL);
		addKillId(SCAVENGER_SPIDER, RED_SCAVENGER_SPIDER, MOONSTONE_BEAST);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no-level.html"); // Custom
		addCondRace(Race.ELF, "no-race.html"); // Custom
		addCondCompletedQuest(Q11009_NewPotionDevelopment1.class.getSimpleName(), "30150-06.html");
		registerQuestItems(MEDICINE_RESEARCH, SPIDER_ICHOR, MOONSTONE_BEAST_SCALES);
		setQuestNameNpcStringId(NpcStringId.LV_15_NEW_POTION_DEVELOPMENT_2_3);
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
			case "30150-02.htm":
			{
				qs.startQuest();
				qs.setCond(2);
				showOnScreenMsg(player, NpcStringId.GO_HUNTING_AND_KILL_SCAVENGER_SPIDERS_AND_RED_SCAVENGER_SPIDERS, ExShowScreenMessage.TOP_CENTER, 10000);
				giveItems(player, MEDICINE_RESEARCH, 1);
				htmltext = event;
				break;
			}
			case "reward1":
			{
				if (qs.isCond(4))
				{
					takeItems(player, MEDICINE_RESEARCH, 1);
					takeItems(player, SPIDER_ICHOR, 20);
					takeItems(player, MOONSTONE_BEAST_SCALES, 20);
					giveItems(player, SCROLL_OF_ESCAPE, 5);
					giveItems(player, HEALING_POTION, 40);
					giveItems(player, MP_RECOVERY_POTION, 40);
					giveItems(player, SOULSHOTS_NO_GRADE, 1000);
					addExpAndSp(player, 70000, 3600);
					qs.exitQuest(false, true);
					htmltext = "30150-04.html";
				}
				break;
			}
			case "reward2":
			{
				if (qs.isCond(4))
				{
					takeItems(player, MEDICINE_RESEARCH, 1);
					takeItems(player, SPIDER_ICHOR, 20);
					takeItems(player, MOONSTONE_BEAST_SCALES, 20);
					giveItems(player, SCROLL_OF_ESCAPE, 5);
					giveItems(player, HEALING_POTION, 40);
					giveItems(player, MP_RECOVERY_POTION, 40);
					giveItems(player, SPIRITSHOT_NO_GRADE, 1000);
					addExpAndSp(player, 70000, 3600);
					qs.exitQuest(false, true);
					htmltext = "30150-05.html";
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
				htmltext = "30150-01.html";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(2))
				{
					htmltext = "30150-02a.html";
				}
				else if (qs.isCond(4))
				{
					htmltext = "30150-03.html";
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
				case SCAVENGER_SPIDER:
				case RED_SCAVENGER_SPIDER:
				{
					if (qs.isCond(2) && (getQuestItemsCount(killer, SPIDER_ICHOR) < 20) && (getRandom(100) < 92))
					{
						giveItems(killer, SPIDER_ICHOR, 1);
						if (getQuestItemsCount(killer, SPIDER_ICHOR) >= 20)
						{
							playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
							showOnScreenMsg(killer, NpcStringId.YOU_HAVE_KILLED_ENOUGH_SCAVENGER_SPIDERS_AND_RED_SCAVENGER_SPIDERS_N_GO_HUNTING_AND_KILL_MOONSTONE_BEASTS, ExShowScreenMessage.TOP_CENTER, 10000);
							qs.setCond(3);
						}
						else
						{
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				}
				case MOONSTONE_BEAST:
				{
					if (qs.isCond(3) && (getQuestItemsCount(killer, MOONSTONE_BEAST_SCALES) < 20) && (getRandom(100) < 92))
					{
						giveItems(killer, MOONSTONE_BEAST_SCALES, 1);
						if (getQuestItemsCount(killer, MOONSTONE_BEAST_SCALES) >= 20)
						{
							playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
							showOnScreenMsg(killer, NpcStringId.RETURN_TO_GROCER_HERBIEL, ExShowScreenMessage.TOP_CENTER, 10000);
							qs.setCond(4);
						}
						else
						{
							playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
