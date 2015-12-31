/*
 * This file is part of the L2J Mobius project.
 * 
 * This file is part of the L2J Mobius Project.
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
package quests.Q10331_StartOfFate;

import com.l2jmobius.gameserver.data.xml.impl.MultisellData;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

import quests.Q10366_RuinsStatusUpdate.Q10366_RuinsStatusUpdate;

/**
 * Start of Fate (10331)
 * @author Mobius
 */
public class Q10331_StartOfFate extends Quest
{
	// NPCs
	private static final int FRANCO = 32153;
	private static final int VALFAR = 32146;
	private static final int RIVIAN = 32147;
	private static final int TOOK = 32150;
	private static final int MOKA = 32157;
	private static final int DEVON = 32160;
	private static final int PANTHEON = 32972;
	private static final int LAKCIS = 32977;
	private static final int SEBION = 32978;
	// Items
	private static final int SARIL_NECKLACE = 17580;
	private static final int BELIS_MARK = 17615;
	private static final int PROOF_OF_COURAGE = 17821;
	// Other
	private static final Location LAKCIS_TELEPORT_LOC = new Location(-111774, 231933, -3160);
	
	public Q10331_StartOfFate()
	{
		super(10331, Q10331_StartOfFate.class.getSimpleName(), "Start of Fate");
		addStartNpc(FRANCO, VALFAR, RIVIAN, TOOK, MOKA, DEVON);
		addTalkId(FRANCO, VALFAR, RIVIAN, TOOK, MOKA, DEVON, PANTHEON, LAKCIS, SEBION);
		registerQuestItems(SARIL_NECKLACE, BELIS_MARK);
		addCondMinLevel(18, "no_level.html");
		addCondCompletedQuest(Q10366_RuinsStatusUpdate.class.getSimpleName(), "no_prequest.html");
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
			case "32153-02.htm":
			case "32147-02.htm":
			case "32160-02.htm":
			case "32150-02.htm":
			case "32157-02.htm":
			case "32146-02.htm":
			case "32977-02.html":
			case "32978-02.html":
			{
				htmltext = event;
				break;
			}
			case "32153-03.htm":
			case "32147-03.htm":
			case "32160-03.htm":
			case "32150-03.htm":
			case "32157-03.htm":
			case "32146-03.htm":
			{
				showOnScreenMsg(player, NpcStringId.GO_TO_THE_ENTRANCE_OF_THE_RUINS_OF_YE_SAGIRA_THROUGH_GATEKEEPER_MILIA_IN_TALKING_ISLAND_VILLAGE, ExShowScreenMessage.TOP_CENTER, 5000);
				qs.startQuest();
				qs.setCond(2);
				qs.setCond(1);
				htmltext = event;
				break;
			}
			case "lakcis_teleport":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2);
				}
				player.teleToLocation(LAKCIS_TELEPORT_LOC);
				return null;
			}
			case "32978-03.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3);
				}
				break;
			}
			case "pantheon_send_to_master":
			{
				if (!qs.isCond(5) || (getQuestItemsCount(player, SARIL_NECKLACE) == 0))
				{
					htmltext = getNoQuestMsg(player);
				}
				
				switch (player.getRace())
				{
					case HUMAN:
					{
						qs.setCond(6);
						htmltext = "32972-02.html";
						break;
					}
					case ELF:
					{
						qs.setCond(7);
						htmltext = "32972-03.html";
						break;
					}
					case DARK_ELF:
					{
						qs.setCond(8);
						htmltext = "32972-04.html";
						break;
					}
					case ORC:
					{
						qs.setCond(9);
						htmltext = "32972-05.html";
						break;
					}
					case DWARF:
					{
						qs.setCond(10);
						htmltext = "32972-06.html";
						break;
					}
					case KAMAEL:
					{
						qs.setCond(11);
						htmltext = "32972-07.html";
						break;
					}
				}
				takeItems(player, SARIL_NECKLACE, -1);
				break;
			}
		}
		
		if (event.startsWith("change_to_"))
		{
			if (qs.getCond() < 6)
			{
				return null;
			}
			final int classId = Integer.parseInt(event.replace("change_to_", ""));
			player.setBaseClassId(classId);
			player.setClassId(classId);
			switch (classId)
			{
				case 1:
				{
					htmltext = "32153-09.html";
					break;
				}
				case 4:
				{
					htmltext = "32153-10.html";
					break;
				}
				case 7:
				{
					htmltext = "32153-11.html";
					break;
				}
				case 11:
				{
					htmltext = "32153-07.html";
					break;
				}
				case 15:
				{
					htmltext = "32153-08.html";
					break;
				}
				case 19:
				{
					htmltext = "32147-09.html";
					break;
				}
				case 22:
				{
					htmltext = "32147-10.html";
					break;
				}
				case 26:
				{
					htmltext = "32147-07.html";
					break;
				}
				case 29:
				{
					htmltext = "32147-08.html";
					break;
				}
				case 32:
				{
					htmltext = "32160-09.html";
					break;
				}
				case 35:
				{
					htmltext = "32160-10.html";
					break;
				}
				case 39:
				{
					htmltext = "32160-07.html";
					break;
				}
				case 42:
				{
					htmltext = "32160-08.html";
					break;
				}
				case 45:
				{
					htmltext = "32150-08.html";
					break;
				}
				case 47:
				{
					htmltext = "32150-09.html";
					break;
				}
				case 50:
				{
					htmltext = "32150-07.html";
					break;
				}
				case 54:
				{
					htmltext = "32157-06.html";
					break;
				}
				case 56:
				{
					htmltext = "32157-07.html";
					break;
				}
				case 125:
				{
					htmltext = "32146-08.html";
					break;
				}
				case 126:
				{
					htmltext = "32146-07.html";
					break;
				}
			}
			giveAdena(player, 80000, true);
			giveItems(player, PROOF_OF_COURAGE, 40);
			addExpAndSp(player, 200000, 48);
			player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_009_enchant_01.htm", TutorialShowHtml.LARGE_WINDOW));
			MultisellData.getInstance().separateAndSend(717, player, npc, false);
			player.broadcastUserInfo();
			qs.exitQuest(false, true);
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
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case FRANCO:
					{
						if (qs.isCond(1))
						{
							htmltext = "32153-04.html";
						}
						else if (qs.isCond(6))
						{
							if (player.isMageClass())
							{
								htmltext = "32153-05.html";
							}
							else
							{
								htmltext = "32153-06.html";
							}
						}
						break;
					}
					case RIVIAN:
					{
						if (qs.isCond(1))
						{
							htmltext = "32147-04.html";
						}
						else if (qs.isCond(7))
						{
							if (player.isMageClass())
							{
								htmltext = "32147-05.html";
							}
							else
							{
								htmltext = "32147-06.html";
							}
						}
						break;
					}
					case DEVON:
					{
						if (qs.isCond(1))
						{
							htmltext = "32160-04.html";
						}
						else if (qs.isCond(8))
						{
							if (player.isMageClass())
							{
								htmltext = "32160-05.html";
							}
							else
							{
								htmltext = "32160-06.html";
							}
						}
						break;
					}
					case TOOK:
					{
						if (qs.isCond(1))
						{
							htmltext = "32150-04.html";
						}
						else if (qs.isCond(9))
						{
							if (player.getClassId().getId() == 49)
							{
								htmltext = "32150-05.html";
							}
							else
							{
								htmltext = "32150-06.html";
							}
						}
						break;
					}
					case MOKA:
					{
						if (qs.isCond(1))
						{
							htmltext = "32157-04.html";
						}
						else if (qs.isCond(10))
						{
							htmltext = "32157-05.html";
						}
						break;
					}
					case VALFAR:
					{
						if (qs.isCond(1))
						{
							htmltext = "32146-04.html";
						}
						else if (qs.isCond(11))
						{
							if (player.getAppearance().getSex())
							{
								htmltext = "32146-05.html";
							}
							else
							{
								htmltext = "32146-06.html";
							}
						}
						break;
					}
					case LAKCIS:
					{
						htmltext = "32977-01.html";
						break;
					}
					case SEBION:
					{
						switch (qs.getCond())
						{
							case 2:
							{
								htmltext = "32978-01.html";
								break;
							}
							case 3:
							case 4:
							{
								htmltext = "32978-04.html";
								break;
							}
							case 5:
							{
								htmltext = "32978-05.html";
								break;
							}
						}
						break;
					}
					case PANTHEON:
					{
						switch (qs.getCond())
						{
							case 5:
							{
								htmltext = "32972-01.html";
								break;
							}
							case 6:
							{
								htmltext = "32972-02.html";
								break;
							}
							case 7:
							{
								htmltext = "32972-03.html";
								break;
							}
							case 8:
							{
								htmltext = "32972-04.html";
								break;
							}
							case 9:
							{
								htmltext = "32972-05.html";
								break;
							}
							case 10:
							{
								htmltext = "32972-06.html";
								break;
							}
							case 11:
							{
								htmltext = "32972-07.html";
								break;
							}
						}
						break;
					}
				}
				break;
			}
			case State.CREATED:
			{
				switch (player.getRace())
				{
					case HUMAN:
					{
						if (npc.getId() == FRANCO)
						{
							htmltext = "32153-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case ELF:
					{
						if (npc.getId() == RIVIAN)
						{
							htmltext = "32147-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case DARK_ELF:
					{
						if (npc.getId() == DEVON)
						{
							htmltext = "32160-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case ORC:
					{
						if (npc.getId() == TOOK)
						{
							htmltext = "32150-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case DWARF:
					{
						if (npc.getId() == MOKA)
						{
							htmltext = "32157-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case KAMAEL:
					{
						if (npc.getId() == VALFAR)
						{
							htmltext = "32146-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
}
