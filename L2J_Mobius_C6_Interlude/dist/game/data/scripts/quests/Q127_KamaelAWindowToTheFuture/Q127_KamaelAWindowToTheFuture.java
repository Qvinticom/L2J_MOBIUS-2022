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
package quests.Q127_KamaelAWindowToTheFuture;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.ExShowSlideshowKamael;

public class Q127_KamaelAWindowToTheFuture extends Quest
{
	// NPCs
	private static final int DOMINIC = 31350;
	private static final int KLAUS = 30187;
	private static final int ALDER = 32092;
	private static final int AKLAN = 31288;
	private static final int OLTLIN = 30862;
	private static final int JURIS = 30113;
	private static final int RODEMAI = 30756;
	// Items
	private static final int MARK_DOMINIC = 8939;
	private static final int MARK_HUMAN = 8940;
	private static final int MARK_DWARF = 8941;
	private static final int MARK_ORC = 8944;
	private static final int MARK_DELF = 8943;
	private static final int MARK_ELF = 8942;
	
	public Q127_KamaelAWindowToTheFuture()
	{
		super(127, "Kamael: A Window to the Future");
		registerQuestItems(MARK_DOMINIC, MARK_HUMAN, MARK_DWARF, MARK_ORC, MARK_DELF, MARK_ELF);
		addStartNpc(DOMINIC);
		addTalkId(DOMINIC, KLAUS, ALDER, AKLAN, OLTLIN, JURIS, RODEMAI);
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
			case "31350-04.htm":
			{
				st.startQuest();
				st.giveItems(MARK_DOMINIC, 1);
				break;
			}
			case "31350-06.htm":
			{
				st.takeItems(MARK_HUMAN, -1);
				st.takeItems(MARK_DWARF, -1);
				st.takeItems(MARK_ELF, -1);
				st.takeItems(MARK_DELF, -1);
				st.takeItems(MARK_ORC, -1);
				st.takeItems(MARK_DOMINIC, -1);
				st.rewardItems(57, 159100);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
				break;
			}
			case "30187-06.htm":
			{
				st.setCond(2);
				break;
			}
			case "30187-08.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(MARK_HUMAN, 1);
				break;
			}
			case "32092-05.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(MARK_DWARF, 1);
				break;
			}
			case "31288-04.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(MARK_ORC, 1);
				break;
			}
			case "30862-04.htm":
			{
				st.setCond(6);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(MARK_DELF, 1);
				break;
			}
			case "30113-04.htm":
			{
				st.setCond(7);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(MARK_ELF, 1);
				break;
			}
			case "kamaelstory":
			{
				st.setCond(8);
				st.playSound(QuestState.SOUND_MIDDLE);
				player.sendPacket(ExShowSlideshowKamael.STATIC_PACKET);
				return null;
			}
			case "30756-05.htm":
			{
				st.setCond(9);
				st.playSound(QuestState.SOUND_MIDDLE);
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
				htmltext = "31350-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (npc.getNpcId())
				{
					case KLAUS:
					{
						if (st.isCond(1))
						{
							htmltext = "30187-01.htm";
						}
						else if (st.isCond(2))
						{
							htmltext = "30187-06.htm";
						}
						break;
					}
					case ALDER:
					{
						if (st.isCond(3))
						{
							htmltext = "32092-01.htm";
						}
						break;
					}
					case AKLAN:
					{
						if (st.isCond(4))
						{
							htmltext = "31288-01.htm";
						}
						break;
					}
					case OLTLIN:
					{
						if (st.isCond(5))
						{
							htmltext = "30862-01.htm";
						}
						break;
					}
					case JURIS:
					{
						if (st.isCond(6))
						{
							htmltext = "30113-01.htm";
						}
						break;
					}
					case RODEMAI:
					{
						if (st.isCond(7))
						{
							htmltext = "30756-01.htm";
						}
						else if (st.isCond(8))
						{
							htmltext = "30756-04.htm";
						}
						break;
					}
					case DOMINIC:
					{
						if (st.isCond(9))
						{
							htmltext = "31350-05.htm";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
				return htmltext;
			}
		}
		
		return htmltext;
	}
}