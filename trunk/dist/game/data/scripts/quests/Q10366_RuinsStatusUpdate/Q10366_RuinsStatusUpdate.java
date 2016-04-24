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
package quests.Q10366_RuinsStatusUpdate;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q10365_SeekerEscort.Q10365_SeekerEscort;

/**
 * Ruins status update (10366)
 * @author spider, gyo
 */
public class Q10366_RuinsStatusUpdate extends Quest
{
	// NPCs
	private static final int SEBION = 32978;
	private static final int FRANCO = 32153;
	private static final int VALFAR = 32146;
	private static final int RIVIAN = 32147;
	private static final int TOOK = 32150;
	private static final int MOKA = 32157;
	private static final int DEVON = 32160;
	// Rewards
	private static final int ADENA_REWARD = 750;
	private static final int EXP_REWARD = 150000;
	private static final int SP_REWARD = 36;
	// Requirements
	private static final int MIN_LEVEL = 16;
	private static final int MAX_LEVEL = 25;
	// NPCs Name
	private static final String GRAND_MAGISTER_DEVON = "Grand Magister Devon";
	private static final String GRAND_MASTER_RIVIAN = "Grand Master Rivian";
	private static final String GRAND_MASTER_VALFAR = "Grand Master Valfar";
	private static final String HEAD_BLACKSMITH_MOKA = "Head Blacksmith Moka";
	private static final String HIGH_PREFECT_TOOK = "High Prefect Took";
	private static final String HIGH_PRIEST_FRANCO = "High Priest Franco";
	
	public Q10366_RuinsStatusUpdate()
	{
		super(10366, Q10366_RuinsStatusUpdate.class.getSimpleName(), "Ruins status update");
		addStartNpc(SEBION);
		addTalkId(FRANCO, VALFAR, RIVIAN, TOOK, MOKA, DEVON, SEBION);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "32978-05.htm");
		addCondCompletedQuest(Q10365_SeekerEscort.class.getSimpleName(), "32978-05.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32978-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32978-03.html":
			{
				qs.startQuest();
				showOnScreenMsg(qs.getPlayer(), NpcStringId.USE_THE_ESCAPE_SCROLL_IN_YOUR_INVENTORY_TO_GO_TO_THE_ADMINISTRATIVE_OFFICE_IN_TALKING_ISLAND, ExShowScreenMessage.TOP_CENTER, 10000);
				htmltext = getHtm(player.getHtmlPrefix(), event);
				switch (player.getRace())
				{
					case HUMAN:
					{
						qs.setCond(2);
						htmltext = htmltext.replace("%MASTER%", HIGH_PRIEST_FRANCO);
						break;
					}
					case ELF:
					{
						qs.setCond(3);
						htmltext = htmltext.replace("%MASTER%", GRAND_MASTER_RIVIAN);
						break;
					}
					case DARK_ELF:
					{
						qs.setCond(4);
						htmltext = htmltext.replace("%MASTER%", GRAND_MAGISTER_DEVON);
						break;
					}
					case ORC:
					{
						qs.setCond(5);
						htmltext = htmltext.replace("%MASTER%", HIGH_PREFECT_TOOK);
						break;
					}
					case DWARF:
					{
						qs.setCond(6);
						htmltext = htmltext.replace("%MASTER%", HEAD_BLACKSMITH_MOKA);
						break;
					}
					case KAMAEL:
					{
						qs.setCond(7);
						htmltext = htmltext.replace("%MASTER%", GRAND_MASTER_VALFAR);
						break;
					}
				}
				break;
			}
			case "32146-02.html":
			case "32147-02.html":
			case "32150-02.html":
			case "32153-02.html":
			case "32157-02.html":
			case "32160-02.html":
			{
				htmltext = event;
				break;
			}
			case "32146-03.html":
			case "32147-03.html":
			case "32150-03.html":
			case "32153-03.html":
			case "32157-03.html":
			case "32160-03.html":
			{
				if (qs.getCond() > 1)
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = npc.getId() == SEBION ? "32978-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case SEBION:
					{
						htmltext = getHtm(player.getHtmlPrefix(), "32978-04.html");
						switch (player.getRace())
						{
							case HUMAN:
							{
								htmltext = htmltext.replace("%MASTER%", HIGH_PRIEST_FRANCO);
								break;
							}
							case ELF:
							{
								htmltext = htmltext.replace("%MASTER%", GRAND_MASTER_RIVIAN);
								break;
							}
							case DARK_ELF:
							{
								htmltext = htmltext.replace("%MASTER%", GRAND_MAGISTER_DEVON);
								break;
							}
							case ORC:
							{
								htmltext = htmltext.replace("%MASTER%", HIGH_PREFECT_TOOK);
								break;
							}
							case DWARF:
							{
								htmltext = htmltext.replace("%MASTER%", HEAD_BLACKSMITH_MOKA);
								break;
							}
							case KAMAEL:
							{
								htmltext = htmltext.replace("%MASTER%", GRAND_MASTER_VALFAR);
								break;
							}
						}
						break;
					}
					case FRANCO:
					{
						htmltext = player.getRace() == Race.HUMAN ? "32153-01.html" : "32153-04.html";
						break;
					}
					case RIVIAN:
					{
						htmltext = player.getRace() == Race.ELF ? "32147-01.html" : "32147-04.html";
						break;
					}
					case DEVON:
					{
						htmltext = player.getRace() == Race.DARK_ELF ? "32160-01.html" : "32160-04.html";
						break;
					}
					case TOOK:
					{
						htmltext = player.getRace() == Race.ORC ? "32150-01.html" : "32150-04.html";
						break;
					}
					case MOKA:
					{
						htmltext = player.getRace() == Race.DWARF ? "32157-01.html" : "32157-04.html";
						break;
					}
					case VALFAR:
					{
						htmltext = player.getRace() == Race.KAMAEL ? "32146-01.html" : "32146-04.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				switch (npc.getId())
				{
					case SEBION:
					{
						htmltext = "32978-06.html";
						break;
					}
					case FRANCO:
					{
						htmltext = "32153-05.html";
						break;
					}
					case RIVIAN:
					{
						htmltext = "32147-05.html";
						break;
					}
					case DEVON:
					{
						htmltext = "32160-05.html";
						break;
					}
					case TOOK:
					{
						htmltext = "32150-05.html";
						break;
					}
					case MOKA:
					{
						htmltext = "32157-05.html";
						break;
					}
					case VALFAR:
					{
						htmltext = "32146-05.html";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
}
