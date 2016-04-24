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
package quests.Q10331_StartOfFate;

import com.l2jmobius.gameserver.data.xml.impl.MultisellData;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

/**
 * Start of Fate (10331)
 * @author Mobius, gyo
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
	// Reward
	private static final ItemHolder PROOF_OF_COURAGE = new ItemHolder(17821, 40);
	private static final int REWARD_ADENA = 80000;
	private static final int REWARD_EXP = 200000;
	private static final int REWARD_SP = 48;
	// Other
	private static final Location LAKCIS_TELEPORT_LOC = new Location(-111774, 231933, -3160);
	private static final int PROOF_OF_COURAGE_MULTISELL_ID = 717;
	private static final int MIN_LEVEL = 18;
	
	public Q10331_StartOfFate()
	{
		super(10331, Q10331_StartOfFate.class.getSimpleName(), "Start of Fate");
		addStartNpc(FRANCO, VALFAR, RIVIAN, TOOK, MOKA, DEVON);
		addTalkId(FRANCO, VALFAR, RIVIAN, TOOK, MOKA, DEVON, PANTHEON, LAKCIS, SEBION);
		registerQuestItems(SARIL_NECKLACE, BELIS_MARK);
		addCondNotRace(Race.ERTHEIA, "no_race.html");
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
			case "32978-02.html":
			case "32153-05.html":
			case "32146-05.html":
			case "32147-05.html":
			case "32150-05.html":
			case "32157-05.html":
			case "32160-05.html":
			case "32146-06.html":
			case "32147-06.html":
			case "32150-06.html":
			case "32153-06.html":
			case "32160-06.html":
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
				showOnScreenMsg(player, NpcStringId.GO_TO_THE_ENTRANCE_OF_THE_RUINS_OF_YE_SAGIRA_THROUGH_GATEKEEPER_MILIA_IN_TALKING_ISLAND_VILLAGE, ExShowScreenMessage.TOP_CENTER, 10000);
				qs.startQuest();
				qs.setCond(2);
				qs.setCond(1);
				htmltext = event;
				break;
			}
			case "32977-02.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(3);
					qs.setCond(2, true);
				}
				htmltext = event;
				break;
			}
			case "lakcis_teleport":
			{
				player.teleToLocation(LAKCIS_TELEPORT_LOC);
				return null;
			}
			case "32978-03.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
				}
				htmltext = event;
				break;
			}
			case "pantheon_send_to_master":
			{
				if (!qs.isCond(5) || (getQuestItemsCount(player, SARIL_NECKLACE) == 0))
				{
					htmltext = getNoQuestMsg(player);
					break;
				}
				
				switch (player.getRace())
				{
					case HUMAN:
					{
						qs.setCond(6, true);
						htmltext = "32972-02.html";
						break;
					}
					case ELF:
					{
						qs.setCond(7, true);
						htmltext = "32972-03.html";
						break;
					}
					case DARK_ELF:
					{
						qs.setCond(8, true);
						htmltext = "32972-04.html";
						break;
					}
					case ORC:
					{
						qs.setCond(9, true);
						htmltext = "32972-05.html";
						break;
					}
					case DWARF:
					{
						qs.setCond(10, true);
						htmltext = "32972-06.html";
						break;
					}
					case KAMAEL:
					{
						qs.setCond(11, true);
						htmltext = "32972-07.html";
						break;
					}
				}
				takeItems(player, SARIL_NECKLACE, -1);
				break;
			}
		}
		if (event.startsWith("class_preview_"))
		{
			htmltext = event;
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
			giveAdena(player, REWARD_ADENA, true);
			giveItems(player, PROOF_OF_COURAGE);
			addExpAndSp(player, REWARD_EXP, REWARD_SP);
			player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_009_enchant_01.htm", TutorialShowHtml.LARGE_WINDOW));
			MultisellData.getInstance().separateAndSend(PROOF_OF_COURAGE_MULTISELL_ID, player, npc, false);
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
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "32153-04.html";
								break;
							}
							case 6:
							{
								htmltext = player.isMageClass() ? "32153-05.html" : "32153-06.html";
								break;
							}
							case 7:
							case 8:
							case 9:
							case 10:
							case 11:
							{
								htmltext = "32153-14.html";
								break;
							}
						}
						break;
					}
					case RIVIAN:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "32147-04.html";
								break;
							}
							case 7:
							{
								htmltext = player.isMageClass() ? "32147-05.html" : "32147-06.html";
								break;
							}
							case 6:
							case 8:
							case 9:
							case 10:
							case 11:
							{
								htmltext = "32147-13.html";
								break;
							}
						}
						break;
					}
					case DEVON:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "32160-04.html";
								break;
							}
							case 8:
							{
								htmltext = player.isMageClass() ? "32160-05.html" : "32160-06.html";
								break;
							}
							case 6:
							case 7:
							case 9:
							case 10:
							case 11:
							{
								htmltext = "32160-13.html";
								break;
							}
						}
						break;
					}
					case TOOK:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "32150-04.html";
								break;
							}
							case 9:
							{
								htmltext = player.getClassId().getId() == 49 ? "32150-05.html" : "32150-06.html";
								break;
							}
							case 6:
							case 7:
							case 8:
							case 10:
							case 11:
							{
								htmltext = "32150-12.html";
								break;
							}
						}
						break;
					}
					case MOKA:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "32157-04.html";
								break;
							}
							case 10:
							{
								htmltext = "32157-05.html";
								break;
							}
							case 6:
							case 7:
							case 8:
							case 9:
							case 11:
							{
								htmltext = "32157-10.html";
								break;
							}
						}
						break;
					}
					case VALFAR:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "32146-04.html";
								break;
							}
							case 11:
							{
								htmltext = player.getAppearance().getSex() ? "32146-05.html" : "32146-06.html";
								break;
							}
							case 6:
							case 7:
							case 8:
							case 9:
							case 10:
							{
								htmltext = "32146-11.html";
								break;
							}
						}
						break;
					}
					case LAKCIS:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "32977-01.html";
								break;
							}
							case 2:
							{
								htmltext = "32977-03.html";
								break;
							}
						}
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
							{
								htmltext = "32978-04.html";
								break;
							}
							case 4:
							{
								htmltext = "32978-05.html";
								qs.setCond(6);
								qs.setCond(5, true);
								break;
							}
							case 5:
							{
								htmltext = "32978-06.html";
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
								htmltext = "32972-08.html";
								break;
							}
							case 7:
							{
								htmltext = "32972-09.html";
								break;
							}
							case 8:
							{
								htmltext = "32972-10.html";
								break;
							}
							case 9:
							{
								htmltext = "32972-11.html";
								break;
							}
							case 10:
							{
								htmltext = "32972-12.html";
								break;
							}
							case 11:
							{
								htmltext = "32972-13.html";
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
				htmltext = getNoQuestMsg(player);
				switch (npc.getId())
				{
					case SEBION:
					{
						htmltext = "32978-07.html";
						break;
					}
					case LAKCIS:
					{
						htmltext = "32977-04.html";
						break;
					}
					case FRANCO:
					{
						if (player.getRace() == Race.HUMAN)
						{
							htmltext = player.getLevel() >= MIN_LEVEL ? "32153-01.htm" : "32153-12.htm";
						}
						else
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32153-15.htm";
							}
						}
						break;
					}
					case RIVIAN:
					{
						if (player.getRace() == Race.ELF)
						{
							htmltext = player.getLevel() >= MIN_LEVEL ? "32147-01.html" : "32147-11.html";
						}
						else
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32147-14.html";
							}
						}
						break;
					}
					case DEVON:
					{
						if (player.getRace() == Race.DARK_ELF)
						{
							htmltext = player.getLevel() >= MIN_LEVEL ? "32160-01.html" : "32160-11.html";
						}
						else
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32160-14.html";
							}
						}
						break;
					}
					case TOOK:
					{
						if (player.getRace() == Race.ORC)
						{
							htmltext = player.getLevel() >= MIN_LEVEL ? "32150-01.html" : "32150-10.html";
						}
						else
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32150-13.html";
							}
						}
						break;
					}
					case MOKA:
					{
						if (player.getRace() == Race.DWARF)
						{
							htmltext = player.getLevel() >= MIN_LEVEL ? "32157-01.html" : "32157-08.html";
						}
						else
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32157-11.html";
							}
						}
						break;
					}
					case VALFAR:
					{
						if (player.getRace() == Race.KAMAEL)
						{
							htmltext = player.getLevel() >= MIN_LEVEL ? "32146-01.html" : "32146-09.html";
						}
						else
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32146-12.html";
							}
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				switch (npc.getId())
				{
					case FRANCO:
					{
						htmltext = "32153-13.html";
						break;
					}
					case RIVIAN:
					{
						htmltext = "32147-12.html";
						break;
					}
					case DEVON:
					{
						htmltext = "32160-12.html";
						break;
					}
					case TOOK:
					{
						htmltext = "32150-11.html";
						break;
					}
					case MOKA:
					{
						htmltext = "32157-09.html";
						break;
					}
					case VALFAR:
					{
						htmltext = "32146-10.html";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
}
