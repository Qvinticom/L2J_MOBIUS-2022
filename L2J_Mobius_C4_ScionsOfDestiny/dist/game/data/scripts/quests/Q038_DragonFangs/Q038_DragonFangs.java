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
package quests.Q038_DragonFangs;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q038_DragonFangs extends Quest
{
	// NPCs
	private static final int LUIS = 30386;
	private static final int IRIS = 30034;
	private static final int ROHMER = 30344;
	// Items
	private static final int FEATHER_ORNAMENT = 7173;
	private static final int TOOTH_OF_TOTEM = 7174;
	private static final int TOOTH_OF_DRAGON = 7175;
	private static final int LETTER_OF_IRIS = 7176;
	private static final int LETTER_OF_ROHMER = 7177;
	// Reward { item, adena }
	private static final int[][] REWARD =
	{
		// @formatter:off
		{45, 5200},
		{627, 1500},
		{1123, 3200},
		{605, 3200}
		// @formatter:on
	};
	// Droplist
	private static final Map<Integer, int[]> DROPLIST = new HashMap<>();
	static
	{
		// @formatter:off
		DROPLIST.put(21100, new int[]{1, FEATHER_ORNAMENT, 100, 1000000});
		DROPLIST.put(20357, new int[]{1, FEATHER_ORNAMENT, 100, 1000000});
		DROPLIST.put(21101, new int[]{6, TOOTH_OF_DRAGON, 50, 500000});
		DROPLIST.put(20356, new int[]{6, TOOTH_OF_DRAGON, 50, 500000});
		// @formatter:on
	}
	
	public Q038_DragonFangs()
	{
		super(38, "Dragon Fangs");
		registerQuestItems(FEATHER_ORNAMENT, TOOTH_OF_TOTEM, TOOTH_OF_DRAGON, LETTER_OF_IRIS, LETTER_OF_ROHMER);
		addStartNpc(LUIS);
		addTalkId(LUIS, IRIS, ROHMER);
		addKillId(DROPLIST.keySet());
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
			case "30386-02.htm":
			{
				st.startQuest();
				break;
			}
			case "30386-04.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(FEATHER_ORNAMENT, 100);
				st.giveItems(TOOTH_OF_TOTEM, 1);
				break;
			}
			case "30034-02a.htm":
			{
				if (st.hasQuestItems(TOOTH_OF_TOTEM))
				{
					htmltext = "30034-02.htm";
					st.setCond(4);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(TOOTH_OF_TOTEM, 1);
					st.giveItems(LETTER_OF_IRIS, 1);
				}
				break;
			}
			case "30344-02a.htm":
			{
				if (st.hasQuestItems(LETTER_OF_IRIS))
				{
					htmltext = "30344-02.htm";
					st.setCond(5);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(LETTER_OF_IRIS, 1);
					st.giveItems(LETTER_OF_ROHMER, 1);
				}
				break;
			}
			case "30034-04a.htm":
			{
				if (st.hasQuestItems(LETTER_OF_ROHMER))
				{
					htmltext = "30034-04.htm";
					st.setCond(6);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(LETTER_OF_ROHMER, 1);
				}
				break;
			}
			case "30034-06a.htm":
			{
				if (st.getQuestItemsCount(TOOTH_OF_DRAGON) >= 50)
				{
					final int position = Rnd.get(REWARD.length);
					htmltext = "30034-06.htm";
					st.takeItems(TOOTH_OF_DRAGON, 50);
					st.giveItems(REWARD[position][0], 1);
					st.rewardItems(57, REWARD[position][1]);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
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
				htmltext = (player.getLevel() < 19) ? "30386-01a.htm" : "30386-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case LUIS:
						if (cond == 1)
						{
							htmltext = "30386-02a.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30386-03.htm";
						}
						else if (cond > 2)
						{
							htmltext = "30386-03a.htm";
						}
						break;
					
					case IRIS:
						if (cond == 3)
						{
							htmltext = "30034-01.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30034-02b.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30034-03.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30034-05a.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30034-05.htm";
						}
						break;
					
					case ROHMER:
						if (cond == 4)
						{
							htmltext = "30344-01.htm";
						}
						else if (cond > 4)
						{
							htmltext = "30344-03.htm";
						}
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
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
		
		final int[] droplist = DROPLIST.get(npc.getNpcId());
		if (st.isCond(droplist[0]) && st.dropItems(droplist[1], 1, droplist[2], droplist[3]))
		{
			st.setCond(droplist[0] + 1);
		}
		
		return null;
	}
}