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
package quests.Q10341_DayOfDestinyHumansFate;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Day of Destiny: Human's Fate (10341)
 * @author Mobius
 */
public class Q10341_DayOfDestinyHumansFate extends Quest
{
	// NPCs
	private static final int ORVEN = 30857;
	private static final int QUARTERMASTER = 33407;
	private static final int DEAD_SOLDIER_1 = 33166;
	private static final int DEAD_SOLDIER_2 = 33167;
	private static final int DEAD_SOLDIER_3 = 33168;
	private static final int DEAD_SOLDIER_4 = 33169;
	private static final int VANGUARD_MEMBER = 33165;
	// Items
	private static final int DEAD_SOLDIER_TAGS = 17748;
	// Rewards
	private static final int STEEL_DOOR_GUILD_COIN = 37045;
	private static final int M_SOULSHOT_S = 22576;
	private static final int M_SPIRITSHOT_S = 22607;
	// Requirement
	private static final int MIN_LEVEL = 76;
	
	public Q10341_DayOfDestinyHumansFate()
	{
		super(10341, Q10341_DayOfDestinyHumansFate.class.getSimpleName(), "Day of Destiny: Human's Fate");
		addStartNpc(ORVEN);
		addTalkId(ORVEN, QUARTERMASTER, DEAD_SOLDIER_1, DEAD_SOLDIER_2, DEAD_SOLDIER_3, DEAD_SOLDIER_4, VANGUARD_MEMBER);
		registerQuestItems(DEAD_SOLDIER_TAGS);
		addCondMinLevel(MIN_LEVEL, "no_level.html");
		addCondRace(Race.HUMAN, "no_race.html");
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
			case "30857-02.htm":
			case "30857-03.htm":
			case "30857-04.htm":
			case "30857-06.html":
			case "30857-07.html":
			{
				htmltext = event;
				break;
			}
			case "30857-05.html":
			{
				qs.startQuest();
				qs.setCond(2); // arrow hack
				qs.setCond(1);
				htmltext = event;
				break;
			}
			case "33407-02.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "dead_collect.html":
			{
				if (qs.isCond(2))
				{
					if (qs.get("" + npc.getId()) == null)
					{
						qs.set("" + npc.getId(), 1);
						giveItems(player, DEAD_SOLDIER_TAGS, 1);
						playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						
					}
					final int count = qs.getMemoState() + 1;
					if (count >= 4)
					{
						qs.setCond(3, true);
						qs.unset("" + DEAD_SOLDIER_1);
						qs.unset("" + DEAD_SOLDIER_2);
						qs.unset("" + DEAD_SOLDIER_3);
						qs.unset("" + DEAD_SOLDIER_4);
						htmltext = "dead_complete.html";
					}
					else
					{
						qs.setMemoState(count);
						final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
						log.addItem(DEAD_SOLDIER_TAGS, count);
						player.sendPacket(log);
						htmltext = event;
					}
				}
				break;
			}
			case "33407-05.html":
			{
				if (qs.isCond(3))
				{
					takeItems(player, DEAD_SOLDIER_TAGS, -1);
					qs.setCond(4, true);
					htmltext = event;
				}
				break;
			}
			case "transfer_info":
			{
				if (qs.isCond(13))
				{
					switch (player.getClassId().getId())
					{
						// TODO: Return detail htmls.
					}
				}
				break;
			}
			case "30857-08.html":
			{
				if (qs.isCond(13))
				{
					switch (player.getClassId().getId())
					{
						// Gladiator -> Duelist
						case 2:
						{
							player.setBaseClassId(88);
							player.setClassId(88);
							break;
						}
						// Warlord -> Dreadnought
						case 3:
						{
							player.setBaseClassId(89);
							player.setClassId(89);
							break;
						}
						// Paladin -> Phoenix Knight
						case 5:
						{
							player.setBaseClassId(90);
							player.setClassId(90);
							break;
						}
						// Dark Avenger -> Hell Knight
						case 6:
						{
							player.setBaseClassId(91);
							player.setClassId(91);
							break;
						}
						// Treasure Hunter -> Adventurer
						case 8:
						{
							player.setBaseClassId(93);
							player.setClassId(93);
							break;
						}
						// Hawkeye -> Sagitarius
						case 9:
						{
							player.setBaseClassId(92);
							player.setClassId(92);
							break;
						}
						// Sorcerer -> Archmage
						case 12:
						{
							player.setBaseClassId(94);
							player.setClassId(94);
							break;
						}
						// Necromancer -> Soultaker
						case 13:
						{
							player.setBaseClassId(95);
							player.setClassId(95);
							break;
						}
						// Warlock -> Arcana Lord
						case 14:
						{
							player.setBaseClassId(96);
							player.setClassId(96);
							break;
						}
						// Bishop -> Cardinal
						case 16:
						{
							player.setBaseClassId(97);
							player.setClassId(97);
							break;
						}
						// Prophet -> Hierophant
						case 17:
						{
							player.setBaseClassId(98);
							player.setClassId(98);
							break;
						}
					}
					rewardItems(player, STEEL_DOOR_GUILD_COIN, 87);
					rewardItems(player, M_SOULSHOT_S, 1);
					rewardItems(player, M_SPIRITSHOT_S, 1);
					addExpAndSp(player, 2050000, 0);
					player.broadcastUserInfo();
					qs.exitQuest(false, true);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (qs.getState())
		{
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case ORVEN:
					{
						if (qs.isCond(1))
						{
							htmltext = "30857-05.html";
						}
						else if (qs.isCond(13))
						{
							htmltext = "30857-06.html";
						}
						break;
					}
					case QUARTERMASTER:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "33407-01.html";
								break;
							}
							case 2:
							{
								htmltext = "33407-03.html";
								break;
							}
							case 3:
							{
								htmltext = "33407-04.html";
								break;
							}
							case 4:
							{
								htmltext = "33407-05.html";
								break;
							}
						}
						break;
					}
					case DEAD_SOLDIER_1:
					case DEAD_SOLDIER_2:
					case DEAD_SOLDIER_3:
					case DEAD_SOLDIER_4:
					{
						if (qs.isCond(2))
						{
							if (qs.get("" + npc.getId()) != null)
							{
								htmltext = "dead_collect.html";
							}
							else
							{
								htmltext = "dead_soldier.html";
							}
						}
						else if (qs.isCond(3))
						{
							htmltext = "dead_complete.html";
						}
						break;
					}
					case VANGUARD_MEMBER:
					{
						if ((qs.getCond() > 3) && (qs.getCond() < 13))
						{
							htmltext = "33165-01.html";
						}
						break;
					}
				}
				break;
			}
			case State.CREATED:
			{
				if ((npc.getId() == ORVEN) && !player.isSubClassActive() && !player.isDualClassActive() && (player.getClassId().level() == 2))
				{
					htmltext = "30857-01.htm";
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
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		final L2PcInstance player = event.getActiveChar();
		if ((player.getLevel() >= MIN_LEVEL) && (player.getRace() == Race.HUMAN))
		{
			final QuestState qs = getQuestState(player, false);
			if (qs == null)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage();
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10341_DayOfDestinyHumansFate/announce.html"));
				player.sendPacket(html);
			}
		}
	}
}