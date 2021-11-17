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
package quests.Q167_DwarvenKinship;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q167_DwarvenKinship extends Quest
{
	// NPCs
	private static final int CARLON = 30350;
	private static final int NORMAN = 30210;
	private static final int HAPROCK = 30255;
	// Items
	private static final int CARLON_LETTER = 1076;
	private static final int NORMAN_LETTER = 1106;
	
	public Q167_DwarvenKinship()
	{
		super(167, "Dwarven Kinship");
		registerQuestItems(CARLON_LETTER, NORMAN_LETTER);
		addStartNpc(CARLON);
		addTalkId(CARLON, HAPROCK, NORMAN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30350-04.htm":
			{
				st.startQuest();
				st.giveItems(CARLON_LETTER, 1);
				break;
			}
			case "30255-03.htm":
			{
				st.setCond(2);
				st.takeItems(CARLON_LETTER, 1);
				st.giveItems(NORMAN_LETTER, 1);
				st.rewardItems(57, 2000);
				break;
			}
			case "30255-04.htm":
			{
				st.takeItems(CARLON_LETTER, 1);
				st.rewardItems(57, 3000);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
				break;
			}
			case "30210-02.htm":
			{
				st.takeItems(NORMAN_LETTER, 1);
				st.rewardItems(57, 20000);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
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
				htmltext = (player.getLevel() < 15) ? "30350-02.htm" : "30350-03.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case CARLON:
					{
						if (cond == 1)
						{
							htmltext = "30350-05.htm";
						}
						break;
					}
					case HAPROCK:
					{
						if (cond == 1)
						{
							htmltext = "30255-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30255-05.htm";
						}
						break;
					}
					case NORMAN:
					{
						if (cond == 2)
						{
							htmltext = "30210-01.htm";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
				break;
			}
		}
		
		return htmltext;
	}
}