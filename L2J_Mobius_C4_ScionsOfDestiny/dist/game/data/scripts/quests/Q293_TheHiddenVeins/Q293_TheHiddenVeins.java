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
package quests.Q293_TheHiddenVeins;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q293_TheHiddenVeins extends Quest
{
	// NPCs
	private static final int FILAUR = 30535;
	private static final int CHINCHIRIN = 30539;
	// Monsters
	private static final int UTUKU_ORC = 20446;
	private static final int UTUKU_ARCHER = 20447;
	private static final int UTUKU_GRUNT = 20448;
	// Items
	private static final int CHRYSOLITE_ORE = 1488;
	private static final int TORN_MAP_FRAGMENT = 1489;
	private static final int HIDDEN_VEIN_MAP = 1490;
	// Reward
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public Q293_TheHiddenVeins()
	{
		super(293, "The Hidden Veins");
		registerQuestItems(CHRYSOLITE_ORE, TORN_MAP_FRAGMENT, HIDDEN_VEIN_MAP);
		addStartNpc(FILAUR);
		addTalkId(FILAUR, CHINCHIRIN);
		addKillId(UTUKU_ORC, UTUKU_ARCHER, UTUKU_GRUNT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30535-03.htm":
			{
				st.startQuest();
				break;
			}
			case "30535-06.htm":
			{
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
				break;
			}
			case "30539-02.htm":
			{
				if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) >= 4)
				{
					htmltext = "30539-03.htm";
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(TORN_MAP_FRAGMENT, 4);
					st.giveItems(HIDDEN_VEIN_MAP, 1);
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getRace() != Race.DWARF)
				{
					htmltext = "30535-00.htm";
				}
				else if (player.getLevel() < 6)
				{
					htmltext = "30535-01.htm";
				}
				else
				{
					htmltext = "30535-02.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getNpcId())
				{
					case FILAUR:
					{
						final int chrysoliteOres = st.getQuestItemsCount(CHRYSOLITE_ORE);
						final int hiddenVeinMaps = st.getQuestItemsCount(HIDDEN_VEIN_MAP);
						if ((chrysoliteOres + hiddenVeinMaps) == 0)
						{
							htmltext = "30535-04.htm";
						}
						else
						{
							if (hiddenVeinMaps > 0)
							{
								if (chrysoliteOres > 0)
								{
									htmltext = "30535-09.htm";
								}
								else
								{
									htmltext = "30535-08.htm";
								}
							}
							else
							{
								htmltext = "30535-05.htm";
							}
							
							final int reward = (chrysoliteOres * 5) + (hiddenVeinMaps * 500) + ((chrysoliteOres >= 10) ? 2000 : 0);
							st.takeItems(CHRYSOLITE_ORE, -1);
							st.takeItems(HIDDEN_VEIN_MAP, -1);
							st.rewardItems(57, reward);
							if (player.isNewbie() && (st.getInt("Reward") == 0))
							{
								st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
								st.playTutorialVoice("tutorial_voice_026");
								st.set("Reward", "1");
							}
						}
						break;
					}
					case CHINCHIRIN:
					{
						htmltext = "30539-01.htm";
						break;
					}
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		final int chance = Rnd.get(100);
		if (chance > 50)
		{
			st.dropItemsAlways(CHRYSOLITE_ORE, 1, 0);
		}
		else if (chance < 5)
		{
			st.dropItemsAlways(TORN_MAP_FRAGMENT, 1, 0);
		}
		
		return null;
	}
}