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
package quests.Q365_DevilsLegacy;

import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q365_DevilsLegacy extends Quest
{
	private static final String qn = "Q365_DevilsLegacy";
	
	// NPCs
	private static final int RANDOLF = 30095;
	private static final int COLLOB = 30092;
	
	// Item
	private static final int PIRATE_TREASURE_CHEST = 5873;
	
	public Q365_DevilsLegacy()
	{
		super(365, qn, "Devil's Legacy");
		
		registerQuestItems(PIRATE_TREASURE_CHEST);
		
		addStartNpc(RANDOLF);
		addTalkId(RANDOLF, COLLOB);
		
		addKillId(20836, 20845, 21629, 21630); // Pirate Zombie && Pirate Zombie Captain.
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("30095-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30095-06.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equals("30092-05.htm"))
		{
			if (!st.hasQuestItems(PIRATE_TREASURE_CHEST))
			{
				htmltext = "30092-02.htm";
			}
			else if (st.getQuestItemsCount(57) < 600)
			{
				htmltext = "30092-03.htm";
			}
			else
			{
				st.takeItems(PIRATE_TREASURE_CHEST, 1);
				st.takeItems(57, 600);
				
				int i0;
				if (Rnd.get(100) < 80)
				{
					i0 = Rnd.get(100);
					if (i0 < 1)
					{
						st.giveItems(955, 1);
					}
					else if (i0 < 4)
					{
						st.giveItems(956, 1);
					}
					else if (i0 < 36)
					{
						st.giveItems(1868, 1);
					}
					else if (i0 < 68)
					{
						st.giveItems(1884, 1);
					}
					else
					{
						st.giveItems(1872, 1);
					}
					
					htmltext = "30092-05.htm";
				}
				else
				{
					i0 = Rnd.get(1000);
					if (i0 < 10)
					{
						st.giveItems(951, 1);
					}
					else if (i0 < 40)
					{
						st.giveItems(952, 1);
					}
					else if (i0 < 60)
					{
						st.giveItems(955, 1);
					}
					else if (i0 < 260)
					{
						st.giveItems(956, 1);
					}
					else if (i0 < 445)
					{
						st.giveItems(1879, 1);
					}
					else if (i0 < 630)
					{
						st.giveItems(1880, 1);
					}
					else if (i0 < 815)
					{
						st.giveItems(1882, 1);
					}
					else
					{
						st.giveItems(1881, 1);
					}
					
					htmltext = "30092-06.htm";
					
					// Curse effect !
					final L2Skill skill = SkillTable.getInstance().getInfo(4082, 1);
					if ((skill != null) && (player.getFirstEffect(skill) == null))
					{
						skill.getEffects(npc, player);
					}
				}
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() < 39) ? "30095-00.htm" : "30095-01.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case RANDOLF:
						if (!st.hasQuestItems(PIRATE_TREASURE_CHEST))
						{
							htmltext = "30095-03.htm";
						}
						else
						{
							htmltext = "30095-05.htm";
							
							int reward = st.getQuestItemsCount(PIRATE_TREASURE_CHEST) * 400;
							
							st.takeItems(PIRATE_TREASURE_CHEST, -1);
							st.rewardItems(57, reward + 19800);
						}
						break;
					
					case COLLOB:
						htmltext = "30092-01.htm";
						break;
				}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		partyMember.getQuestState(qn).dropItems(PIRATE_TREASURE_CHEST, 1, 0, (npc.getNpcId() == 20836) ? 360000 : 520000);
		
		return null;
	}
}