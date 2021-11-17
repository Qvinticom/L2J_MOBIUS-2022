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
package quests.Q00763_ADauntingTask;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * A Daunting Task (763)
 * @author St3eT
 */
public class Q00763_ADauntingTask extends Quest
{
	// NPCs
	private static final int JANITT = 33851;
	private static final int[] MONSTERS =
	{
		21294, // Canyon Antelope
		21295, // Canyon Antelope Slave
		21296, // Canyon Bandersnatch
		21297, // Canyon Bandersnatch Slave
		21299, // Valley Buffalo Slave
		21300, // Eye of Guide
		21301, // Gaze of Nightmares
		21302, // Eye of Watchman
		21303, // Homunculus
		21304, // Valley Grendel Slave
		21305, // Eye of Pilgrim
		21306, // Disciple of Protection
		21307, // Elder Homunculus
		21308, // Disciples of Punishment
		21310, // Disciples of Authority
		21312, // Eye of Ruler
		23311, // Valley Buffalo
		23312, // Valley Grendel
		23313, // Disciple of Protection
	};
	// Items
	private static final int EYE = 36672; // Evil Eye of Darkness
	private static final int MALICE = 36673; // Powerful Dark Malice
	private static final int STEEL_DOOR_BOX = 37392; // Steel Door Guild Reward Box (Mid-grade)
	// Rewards
	//@formatter:off
	// Format: min item count, exp reward, sp reward, item count reward
	private static final int[][] REWARD = 
	{
		{900, 163_296_000, 1_632_960, 10}, //TODO: Custom, SP reward should be decreated since Ertheia
		{800, 146_966_400, 1_469_664, 9}, //TODO: Custom, SP reward should be decreated since Ertheia
		{700, 130_636_800, 1_306_368, 8}, //TODO: Custom, SP reward should be decreated since Ertheia
		{600, 114_307_200, 1_143_072, 7}, //TODO: Custom, SP reward should be decreated since Ertheia
		{500, 97_977_600, 979_776, 6}, //TODO: Custom, SP reward should be decreated since Ertheia
		{400, 81_648_000, 816_480, 5}, //TODO: Custom, SP reward should be decreated since Ertheia
		{300, 65_318_400, 653_184, 4}, //TODO: Custom, SP reward should be decreated since Ertheia
		{200, 48_988_800, 489_888, 3}, //TODO: Custom, SP reward should be decreated since Ertheia
		{100, 32_659_200, 7_838, 2},
		{0, 16_329_600, 3_919, 1},
	};
	//@formatter:on
	// Misc
	private static final int MIN_LEVEL = 70;
	
	public Q00763_ADauntingTask()
	{
		super(763);
		addStartNpc(JANITT);
		addTalkId(JANITT);
		addKillId(MONSTERS);
		registerQuestItems(EYE, MALICE);
		addCondNotRace(Race.ERTHEIA, "33851-10.html");
		addCondMinLevel(MIN_LEVEL, "33851-11.htm");
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
			case "33851-02.htm":
			case "33851-03.htm":
			case "33851-07.html":
			case "33851-08.html":
			{
				htmltext = event;
				break;
			}
			case "33851-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33851-09.html":
			{
				if (qs.isCond(2))
				{
					final long itemCount = getQuestItemsCount(player, MALICE);
					for (int[] data : REWARD)
					{
						if (itemCount >= data[0])
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								addExpAndSp(player, data[1], data[2]);
							}
							giveItems(player, STEEL_DOOR_BOX, data[3]);
							qs.exitQuest(QuestType.DAILY, true);
							htmltext = event;
							break;
						}
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
				htmltext = "33851-01.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = qs.isCond(1) ? "33851-05.html" : "33851-06.html";
				break;
			}
			case State.COMPLETED:
			{
				if (!qs.isNowAvailable())
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
				}
				else
				{
					qs.setState(State.CREATED);
					htmltext = "33851-01.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.isCond(1) || qs.isCond(2)) && (getRandom(100) < 15))
		{
			if (getQuestItemsCount(killer, EYE) < 50)
			{
				giveItems(killer, EYE, 1);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if (getQuestItemsCount(killer, EYE) >= 50)
				{
					qs.setCond(2, true);
					showOnScreenMsg(killer, NpcStringId.YOU_CAN_GATHER_MORE_POWERFUL_DARK_MALICE, ExShowScreenMessage.TOP_CENTER, 6000);
				}
			}
			else
			{
				giveItems(killer, MALICE, 1);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}