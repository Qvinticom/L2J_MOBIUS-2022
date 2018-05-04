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
import com.l2jmobius.gameserver.data.xml.impl.MultisellData;
import com.l2jmobius.gameserver.enums.CategoryType;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.Location;
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
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;
import com.l2jmobius.gameserver.util.Util;

/**
 * Start of Fate (10331)
 * @author Gladicek
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
	private static final int LAKCIS = 32977;
	private static final int SEBION = 32978;
	private static final int PANTHEON = 32972;
	// Items
	private static final int SARIL_NECKLACE = 17580;
	private static final int PROOF_OF_COURAGE = 17821;
	// Location
	private static final Location NEAR_SEBION = new Location(-111774, 231933, -3160);
	// Misc
	private static final int MIN_LEVEL = 18;
	
	public Q10331_StartOfFate()
	{
		super(10331);
		addStartNpc(FRANCO, RIVIAN, DEVON, TOOK, MOKA, VALFAR);
		addTalkId(FRANCO, RIVIAN, DEVON, TOOK, MOKA, VALFAR, SEBION, LAKCIS, PANTHEON);
		addCondInCategory(CategoryType.FIRST_CLASS_GROUP, "");
		registerQuestItems(SARIL_NECKLACE);
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
				/**
				 * 1st class transfer htmls for each class
				 */
			}
			case "32146-09.html": // Trooper
			case "32146-10.html": // Warder
			case "32153-09.html": // Warrior
			case "32153-10.html": // Knight
			case "32153-11.html": // Rogue
			case "32153-12.html": // Wizard
			case "32153-13.html": // Cleric
			case "32157-08.html": // Scavenger
			case "32157-09.html": // Artisan
			case "32147-09.html": // Elven Knight
			case "32147-10.html": // Elven Scout
			case "32147-11.html": // Elven Wizard
			case "32147-12.html": // Elven Oracle
			case "32160-09.html": // Palus Knight
			case "32160-10.html": // Assasin
			case "32160-11.html": // Dark Wizard
			case "32160-12.html": // Shilien Oracle
			case "32150-09.html": // Orc Raider
			case "32150-10.html": // Orc Monk
			case "32150-11.html": // Orc Shaman
			{
				if ((qs.getCond() >= 6) && (qs.getCond() <= 11))
				{
					htmltext = event;
				}
				break;
			}
			case "32153-03.htm":
			case "32147-03.htm":
			case "32160-03.htm":
			case "32150-03.htm":
			case "32157-03.htm":
			case "32146-03.htm":
			{
				qs.startQuest();
				qs.setCond(2); // arrow hack
				qs.setCond(1);
				showOnScreenMsg(player, NpcStringId.GO_TO_THE_ENTRANCE_OF_THE_RUINS_OF_YE_SAGIRA_THROUGH_GATEKEEPER_MILIA_IN_TALKING_ISLAND_VILLAGE, ExShowScreenMessage.TOP_CENTER, 4500);
				htmltext = event;
				break;
			}
			case "32977-02.htm":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "32978-03.htm":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
					htmltext = event;
				}
				break;
			}
			case "teleport_sebion":
			{
				if (qs.isCond(2))
				{
					player.teleToLocation(NEAR_SEBION);
				}
				break;
			}
			case "check_race_pantheon":
			{
				if (qs.isCond(5))
				{
					takeItems(player, SARIL_NECKLACE, 1);
					switch (player.getRace())
					{
						case HUMAN:
						{
							qs.setCond(6, true);
							htmltext = "32972-02.htm";
							break;
						}
						case ELF:
						{
							qs.setCond(7, true);
							htmltext = "32972-03.htm";
							break;
						}
						case DARK_ELF:
						{
							qs.setCond(8, true);
							htmltext = "32972-04.htm";
							break;
						}
						case ORC:
						{
							qs.setCond(9, true);
							htmltext = "32972-05.htm";
							break;
						}
						case DWARF:
						{
							qs.setCond(10, true);
							htmltext = "32972-06.htm";
							break;
						}
						case KAMAEL:
						{
							qs.setCond(11, true);
							htmltext = "32972-07.htm";
							break;
						}
					}
					break;
				}
			}
			default:
			{
				if (event.startsWith("classChange;"))
				{
					final ClassId newClassId = ClassId.getClassId(Integer.parseInt(event.replace("classChange;", "")));
					final ClassId currentClassId = player.getClassId();
					if (!newClassId.childOf(currentClassId) || ((qs.getCond() < 6) && (qs.getCond() > 11)))
					{
						Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to cheat the 1st class transfer!", Config.DEFAULT_PUNISH);
						return null;
					}
					switch (newClassId)
					{
						case WARRIOR:
						{
							htmltext = "32153-15.htm";
							break;
						}
						case KNIGHT:
						{
							htmltext = "32153-16.htm";
							break;
						}
						case ROGUE:
						{
							htmltext = "32153-17.htm";
							break;
						}
						case WIZARD:
						{
							htmltext = "32153-18.htm";
							break;
						}
						case CLERIC:
						{
							htmltext = "32153-19.htm";
							break;
						}
						case ELVEN_KNIGHT:
						{
							htmltext = "32147-14.htm";
							break;
						}
						case ELVEN_SCOUT:
						{
							htmltext = "32147-15.htm";
							break;
						}
						case ELVEN_WIZARD:
						{
							htmltext = "32147-16.htm";
							break;
						}
						case ORACLE:
						{
							htmltext = "32147-17.htm";
							break;
						}
						case PALUS_KNIGHT:
						{
							htmltext = "32160-14.htm";
							break;
						}
						case ASSASSIN:
						{
							htmltext = "32160-15.htm";
							break;
						}
						case DARK_WIZARD:
						{
							htmltext = "32160-16.htm";
							break;
						}
						case SHILLIEN_ORACLE:
						{
							htmltext = "32160-17.htm";
							break;
						}
						case ORC_RAIDER:
						{
							htmltext = "32150-14.htm";
							break;
						}
						case ORC_MONK:
						{
							htmltext = "32150-15.htm";
							break;
						}
						case ORC_SHAMAN:
						{
							htmltext = "32150-16.htm";
							break;
						}
						case SCAVENGER:
						{
							htmltext = "32157-11.htm";
							break;
						}
						case ARTISAN:
						{
							htmltext = "32157-12.htm";
							break;
						}
						case TROOPER:
						{
							htmltext = "32146-12.htm";
							break;
						}
						case WARDER:
						{
							htmltext = "32146-13.htm";
							break;
						}
					}
					player.setBaseClass(newClassId);
					player.setClassId(newClassId.getId());
					player.store(false);
					player.broadcastUserInfo();
					player.sendSkillList();
					giveAdena(player, 80000, true);
					giveItems(player, PROOF_OF_COURAGE, 40);
					addExpAndSp(player, 200000, 48);
					MultisellData.getInstance().separateAndSend(717, player, npc, false);
					player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_009_enchant_01.htm", TutorialShowHtml.LARGE_WINDOW));
					qs.exitQuest(false, true);
				}
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
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case FRANCO:
					{
						if (player.getRace() == Race.HUMAN)
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32153-01.htm";
								break;
							}
							htmltext = "32153-14.htm";
							break;
						}
						htmltext = "32153-04.htm";
						break;
					}
					case RIVIAN:
					{
						if (player.getRace() == Race.ELF)
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32147-01.htm";
								break;
							}
							htmltext = "32147-13.htm";
							break;
						}
						htmltext = "32147-04.htm";
						break;
					}
					case DEVON:
					{
						if (player.getRace() == Race.DARK_ELF)
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32160-01.htm";
								break;
							}
							htmltext = "32160-13.htm";
							break;
						}
						htmltext = "32160-04.htm";
						break;
					}
					case TOOK:
					{
						if (player.getRace() == Race.ORC)
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32150-01.htm";
								break;
							}
							htmltext = "32150-12.htm";
							break;
						}
						htmltext = "32150-04.htm";
						break;
					}
					case MOKA:
					{
						if (player.getRace() == Race.DWARF)
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32157-01.htm";
								break;
							}
							htmltext = "32157-10.htm";
							break;
						}
						htmltext = "32157-04.htm";
						break;
					}
					case VALFAR:
					{
						if (player.getRace() == Race.KAMAEL)
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								htmltext = "32146-01.htm";
								break;
							}
							htmltext = "32146-11.htm";
							break;
						}
						htmltext = "32146-04.htm";
						break;
					}
					case LAKCIS:
					{
						htmltext = "32977-03.htm";
						break;
					}
					case SEBION:
					{
						htmltext = "32978-07.htm";
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case FRANCO:
					{
						if (qs.isCond(1))
						{
							htmltext = "32153-03.htm";
							break;
						}
						else if ((qs.getCond() >= 6) && (qs.getCond() <= 11))
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
							htmltext = "32153-06.htm";
							break;
						}
						break;
					}
					case RIVIAN:
					{
						if (qs.isCond(1))
						{
							htmltext = "32147-03.htm";
							break;
						}
						else if ((qs.getCond() >= 6) && (qs.getCond() <= 11))
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
							htmltext = "32147-06.htm";
							break;
						}
						break;
					}
					case DEVON:
					{
						if (qs.isCond(1))
						{
							htmltext = "32160-03.htm";
							break;
						}
						else if ((qs.getCond() >= 6) && (qs.getCond() <= 11))
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
							htmltext = "32160-06.htm";
							break;
						}
						break;
					}
					case TOOK:
					{
						if (qs.isCond(1))
						{
							htmltext = "32150-03.htm";
							break;
						}
						else if ((qs.getCond() >= 6) && (qs.getCond() <= 11))
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
							htmltext = "32150-06.htm";
							break;
						}
						break;
					}
					case MOKA:
					{
						if (qs.isCond(1))
						{
							htmltext = "32157-03.htm";
							break;
						}
						else if ((qs.getCond() >= 6) && (qs.getCond() <= 11))
						{
							if (player.getRace() == Race.DWARF)
							{
								htmltext = "32157-07.html";
								break;
							}
							htmltext = "32157-06.htm";
							break;
						}
						break;
					}
					case VALFAR:
					{
						if (qs.isCond(1))
						{
							htmltext = "32146-03.htm";
							break;
						}
						else if ((qs.getCond() >= 6) && (qs.getCond() <= 11))
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
							htmltext = "32146-06.htm";
							break;
						}
						break;
					}
					case LAKCIS:
					{
						if (qs.isCond(1))
						{
							htmltext = "32977-01.htm";
							break;
						}
						else if (qs.isCond(2))
						{
							htmltext = "32977-02.htm";
							break;
						}
						break;
					}
					case SEBION:
					{
						switch (qs.getCond())
						{
							case 2:
							{
								htmltext = "32978-01.htm";
								break;
							}
							case 3:
							{
								htmltext = "32978-04.htm";
								break;
							}
							case 4:
							{
								htmltext = "32978-05.htm";
								qs.setCond(5, true);
								break;
							}
							case 5:
							{
								htmltext = "32978-06.htm";
								break;
							}
						}
						break;
					}
					case PANTHEON:
					{
						if (qs.isCond(5))
						{
							htmltext = "32972-01.htm";
							break;
						}
						else if ((qs.getCond() >= 6) && (qs.getCond() <= 11))
						{
							switch (player.getRace())
							{
								case HUMAN:
								{
									htmltext = "32972-08.htm";
									break;
								}
								case ELF:
								{
									htmltext = "32972-09.htm";
									break;
								}
								case DARK_ELF:
								{
									htmltext = "32972-10.htm";
									break;
								}
								case ORC:
								{
									htmltext = "32972-11.htm";
									break;
								}
								case DWARF:
								{
									htmltext = "32972-12.htm";
									break;
								}
								case KAMAEL:
								{
									htmltext = "32972-13.htm";
									break;
								}
							}
							break;
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
					case LAKCIS:
					{
						htmltext = "32980-05.htm";
						break;
					}
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
			player.sendPacket(new TutorialShowHtml(getHtm(player, filename)));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		
		final L2PcInstance player = event.getActiveChar();
		final QuestState qs = getQuestState(player, false);
		final int oldLevel = event.getOldLevel();
		final int newLevel = event.getNewLevel();
		
		if ((qs == null) && (oldLevel < newLevel) && (newLevel == MIN_LEVEL) && (player.getRace() != Race.ERTHEIA) && (player.isInCategory(CategoryType.FIRST_CLASS_GROUP)))
		{
			player.sendPacket(new TutorialShowQuestionMark(getId()));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		
		final L2PcInstance player = event.getActiveChar();
		final QuestState qs = getQuestState(player, false);
		
		if ((qs == null) && (player.getRace() != Race.ERTHEIA) && (player.getLevel() >= MIN_LEVEL) && (player.isInCategory(CategoryType.FIRST_CLASS_GROUP)))
		{
			player.sendPacket(new TutorialShowQuestionMark(getId()));
		}
	}
}