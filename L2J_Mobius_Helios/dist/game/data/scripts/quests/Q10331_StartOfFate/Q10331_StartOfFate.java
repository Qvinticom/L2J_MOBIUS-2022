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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.enums.CategoryType;
import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLogin;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerPressTutorialMark;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;
import com.l2jmobius.gameserver.util.Util;

/**
 * Start of Fate (10331)
 * @URL https://l2wiki.com/Start_of_Fate
 * @author Gladicek, Gigi, Stayway
 */
public final class Q10331_StartOfFate extends Quest
{
	// NPCs
	private static final int FRANCO = 32153;
	private static final int RIVIAN = 32147;
	private static final int DEVON = 32160;
	private static final int TOOK = 32150;
	private static final int MOKA = 32157;
	private static final int VALFAR = 32146;
	private static final int SEBION = 32978;
	// Items
	private static final int SARIL_NECKLACE = 17580;
	private static final int SOE = 736;
	private static final int SOULSHOT = 1463;
	private static final int BLESSED_SPIRITSHOT = 3948;
	private static final int PAULINAS_SET_D_GRADE = 46849;
	private static final int PROOF_OF_COURAGE = 17821;
	// Misc
	private static final int MIN_LEVEL = 18;
	
	public Q10331_StartOfFate()
	{
		super(10331);
		addStartNpc(SEBION);
		addTalkId(FRANCO, RIVIAN, DEVON, TOOK, MOKA, VALFAR, SEBION);
		addCondInCategory(CategoryType.FIRST_CLASS_GROUP, "");
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
			/**
			 * 1st class transfer htmls menu with classes
			 */
			case "32146-07.html": // Kamael Male
			case "32146-08.html": // Kamael Female
			case "32153-07.html": // Human Fighter
			case "32153-08.html": // Human Mage
			case "32157-07.html": // Dwarven Fighter
			case "32147-07.html": // Elven Fighter
			case "32147-08.html": // Elven Mage
			case "32160-07.html": // Dark Elven Fighter
			case "32160-08.html": // Dark Elven Mage
			case "32150-07.html": // Orc Fighter
			case "32150-08.html": // Orc Mage
			{
				if ((qs.getCond() >= 3) && (qs.getCond() <= 8))
				{
					htmltext = event;
				}
				break;
			}
			case "32978-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "HTML":
			{
				if (qs.isCond(2))
				{
					qs.setCond(0);
					giveItems(player, SARIL_NECKLACE, 1);
					switch (player.getRace())
					{
						case HUMAN:
						{
							qs.setCond(3, true);
							htmltext = "32978-04.html";
							break;
						}
						case ELF:
						{
							qs.setCond(4, true);
							htmltext = "32978-06.html";
							break;
						}
						case DARK_ELF:
						{
							qs.setCond(5, true);
							htmltext = "32978-07.html";
							break;
						}
						case ORC:
						{
							qs.setCond(6, true);
							htmltext = "32978-08.html";
							break;
						}
						case DWARF:
						{
							qs.setCond(7, true);
							htmltext = "32978-09.html";
							break;
						}
						case KAMAEL:
						{
							qs.setCond(8, true);
							htmltext = "32978-10.html";
							break;
						}
					}
					break;
				}
				break;
			}
			default:
			{
				if (event.startsWith("classChange;") && (getQuestItemsCount(player, SARIL_NECKLACE) >= 1))
				{
					final ClassId newClassId = ClassId.getClassId(Integer.parseInt(event.replace("classChange;", "")));
					final ClassId currentClassId = player.getClassId();
					
					if (!newClassId.childOf(currentClassId) || ((qs.getCond() < 3) && (qs.getCond() > 8)))
					{
						Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to cheat the 1st class transfer!", Config.DEFAULT_PUNISH);
						return null;
					}
					switch (newClassId)
					{
						case WARRIOR:
							htmltext = "32153-15.htm";
							qs.exitQuest(false, true);
							break;
						case KNIGHT:
							htmltext = "32153-16.htm";
							qs.exitQuest(false, true);
							break;
						case ROGUE:
							htmltext = "32153-17.htm";
							qs.exitQuest(false, true);
							break;
						case WIZARD:
							htmltext = "32153-18.htm";
							qs.exitQuest(false, true);
							break;
						case CLERIC:
							htmltext = "32153-19.htm";
							qs.exitQuest(false, true);
							break;
						case ELVEN_KNIGHT:
							htmltext = "32147-14.htm";
							qs.exitQuest(false, true);
							break;
						case ELVEN_SCOUT:
							htmltext = "32147-15.htm";
							qs.exitQuest(false, true);
							break;
						case ELVEN_WIZARD:
							htmltext = "32147-16.htm";
							qs.exitQuest(false, true);
							break;
						case ORACLE:
							htmltext = "32147-17.htm";
							qs.exitQuest(false, true);
							break;
						case PALUS_KNIGHT:
							htmltext = "32160-14.htm";
							qs.exitQuest(false, true);
							break;
						case ASSASSIN:
							htmltext = "32160-15.htm";
							qs.exitQuest(false, true);
							break;
						case DARK_WIZARD:
							htmltext = "32160-16.htm";
							qs.exitQuest(false, true);
							break;
						case SHILLIEN_ORACLE:
							htmltext = "32160-17.htm";
							qs.exitQuest(false, true);
							break;
						case ORC_RAIDER:
							htmltext = "32150-14.htm";
							qs.exitQuest(false, true);
							break;
						case ORC_MONK:
							htmltext = "32150-15.htm";
							qs.exitQuest(false, true);
							break;
						case ORC_SHAMAN:
							htmltext = "32150-16.htm";
							qs.exitQuest(false, true);
							break;
						case SCAVENGER:
							htmltext = "32157-11.htm";
							qs.exitQuest(false, true);
							break;
						case ARTISAN:
							htmltext = "32157-12.htm";
							qs.exitQuest(false, true);
							break;
						case TROOPER:
							htmltext = "32146-12.htm";
							qs.exitQuest(false, true);
							break;
						case WARDER:
							htmltext = "32146-13.htm";
							qs.exitQuest(false, true);
							break;
					}
					player.setBaseClass(newClassId);
					player.setClassId(newClassId.getId());
					player.store(false);
					player.broadcastUserInfo();
					player.sendSkillList();
					giveItems(player, SOE, 10);
					giveItems(player, SOULSHOT, 1500);
					giveItems(player, BLESSED_SPIRITSHOT, 1500);
					takeItems(player, SARIL_NECKLACE, -1);
					giveItems(player, PAULINAS_SET_D_GRADE, 1);
					giveItems(player, PROOF_OF_COURAGE, 40);
					addExpAndSp(player, 296000, 15);
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
				if ((npc.getId() == SEBION) && (player.getLevel() >= MIN_LEVEL))
				{
					htmltext = "32978-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case FRANCO:
					{
						if ((qs.getCond() >= 3) && (qs.getCond() <= 8))
						{
							if (player.getRace() == Race.HUMAN)
							{
								switch (player.getClassId())
								{
									case FIGHTER:
									{
										htmltext = "32153-07.html";
										break;
									}
									case MAGE:
									{
										htmltext = "32153-08.html";
										break;
									}
								}
								break;
							}
						}
						break;
					}
					case RIVIAN:
					{
						if ((qs.getCond() >= 3) && (qs.getCond() <= 8))
						{
							if (player.getRace() == Race.ELF)
							{
								switch (player.getClassId())
								{
									case ELVEN_FIGHTER:
									{
										htmltext = "32147-07.html";
										break;
									}
									case ELVEN_MAGE:
									{
										htmltext = "32147-08.html";
										break;
									}
								}
								break;
							}
						}
						break;
					}
					case DEVON:
					{
						if ((qs.getCond() >= 3) && (qs.getCond() <= 8))
						{
							if (player.getRace() == Race.DARK_ELF)
							{
								switch (player.getClassId())
								{
									case DARK_FIGHTER:
									{
										htmltext = "32160-07.html";
										break;
									}
									case DARK_MAGE:
									{
										htmltext = "32160-08.html";
										break;
									}
								}
								break;
							}
						}
						break;
					}
					case TOOK:
					{
						if ((qs.getCond() >= 3) && (qs.getCond() <= 8))
						{
							if (player.getRace() == Race.ORC)
							{
								switch (player.getClassId())
								{
									case ORC_FIGHTER:
									{
										htmltext = "32150-07.html";
										break;
									}
									case ORC_MAGE:
									{
										htmltext = "32150-08.html";
										break;
									}
								}
								break;
							}
						}
						break;
					}
					case MOKA:
					{
						if ((qs.getCond() >= 3) && (qs.getCond() <= 8))
						{
							if (player.getRace() == Race.DWARF)
							{
								htmltext = "32157-07.html";
								break;
							}
						}
						break;
					}
					case VALFAR:
					{
						if ((qs.getCond() >= 3) && (qs.getCond() <= 8))
						{
							if (player.getRace() == Race.KAMAEL)
							{
								switch (player.getClassId())
								{
									case MALE_SOLDIER:
									{
										htmltext = "32146-07.html";
										break;
									}
									case FEMALE_SOLDIER:
									{
										htmltext = "32146-08.html";
										break;
									}
								}
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
								startQuestTimer("HTML", 300, npc, player);
								break;
							}
							case 3:
							{
								htmltext = "32978-05.html";
								break;
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
					case SEBION:
					case FRANCO:
					case RIVIAN:
					case DEVON:
					case TOOK:
					case MOKA:
					case VALFAR:
					{
						htmltext = npc.getId() + "-05.htm";
						break;
					}
				}
				
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PRESS_TUTORIAL_MARK)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerPressTutorialMark(OnPlayerPressTutorialMark event)
	{
		if (event.getMarkId() == getId())
		{
			final L2PcInstance player = event.getActiveChar();
			final String filename = "popup-" + player.getRace().toString().toLowerCase() + ".htm";
			player.sendPacket(new TutorialShowHtml(getHtm(player.getHtmlPrefix(), filename)));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final L2PcInstance player = event.getActiveChar();
		final QuestState qs = getQuestState(player, false);
		final int oldLevel = event.getOldLevel();
		final int newLevel = event.getNewLevel();
		
		if ((qs == null) && (oldLevel < newLevel) && (newLevel == MIN_LEVEL) && (player.getRace() != Race.ERTHEIA) && (player.isInCategory(CategoryType.FIRST_CLASS_GROUP)))
		{
			player.sendPacket(new TutorialShowQuestionMark(getId(), 1));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		final L2PcInstance player = event.getActiveChar();
		final QuestState qs = getQuestState(player, false);
		
		if ((qs == null) && (player.getRace() != Race.ERTHEIA) && (player.getLevel() >= MIN_LEVEL) && (player.isInCategory(CategoryType.FIRST_CLASS_GROUP)))
		{
			player.sendPacket(new TutorialShowQuestionMark(getId(), 1));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
		}
	}
}