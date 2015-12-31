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
package quests.Q10360_CertificationOfFate;

import com.l2jmobius.gameserver.data.xml.impl.MultisellData;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.util.Util;

/**
 * Certification of Fate (10360)
 * @author spider
 */
public class Q10360_CertificationOfFate extends Quest
{
	// NPCs: Town Masters
	private static final int RAYMOND = 30289;
	private static final int RAINS = 30288;
	private static final int ELLENIA = 30155;
	private static final int ESRANDELL = 30158;
	private static final int TOBIAS = 30297;
	private static final int DRIKUS = 30505;
	private static final int MENDIO = 30504;
	private static final int GERSHWIN = 32196;
	// Other Npcs
	private static final int RENFAD = 33524;
	private static final int JOEL = 33516;
	private static final int SCHUAZEN = 33517;
	private static final int SELON = 33518;
	// Monsters
	private static final int RKANILOV = 27459;
	private static final int RPOSLOF = 27460;
	private static final int SAKUM = 27453;
	// Items
	private static final ItemHolder SHINE_STONE = new ItemHolder(17587, 1);
	// Requirements
	private static final int MIN_LEVEL = 38;
	// Rewards
	private static final int ADENA_REWARD = 1100;
	private static final int EXP_REWARD = 2700000;
	private static final int SP_REWARD = 648;
	private static final ItemHolder PROOF_OF_JUSTICE = new ItemHolder(17822, 40);
	// Other
	private static final Location WASTELANDS = new Location(-24697, 188757, -3954);
	
	public Q10360_CertificationOfFate()
	{
		super(10360, Q10360_CertificationOfFate.class.getSimpleName(), "Certification of Fate");
		addCondMinLevel(MIN_LEVEL, "no_level.htm");
		addStartNpc(GERSHWIN, RAYMOND, ELLENIA, ESRANDELL, TOBIAS, DRIKUS, MENDIO, RAINS);
		addTalkId(GERSHWIN, RAYMOND, ELLENIA, ESRANDELL, TOBIAS, DRIKUS, MENDIO, RAINS, RENFAD, JOEL, SCHUAZEN, SELON);
		addKillId(RKANILOV, RPOSLOF, SAKUM);
		registerQuestItems(SHINE_STONE.getId());
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
			case "GO_WASTELANDS":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(WASTELANDS);
				}
				break;
			}
			case "30155-02.htm":
			case "30158-02.htm":
			case "30288-02.htm":
			case "30289-02.htm":
			case "30297-02.htm":
			case "30504-02.htm":
			case "30505-02.htm":
			case "32196-02.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30155-03.html":
			case "30158-03.html":
			case "30288-03.html":
			case "30289-03.html":
			case "30297-03.html":
			case "30504-03.html":
			case "30505-03.html":
			case "32196-03.html":
			case "33524-02.html":
			case "33518-02.html":
			case "33517-02.html":
			case "33516-02.html":
			case "33518-03.html":
			{
				htmltext = event;
				break;
			}
			case "33524-03.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2);
					htmltext = event;
				}
				break;
			}
			case "33516-03.html":
			{
				if (qs.isCond(3))
				{
					qs.setCond(4);
					htmltext = event;
				}
				break;
			}
			case "33517-03.html":
			{
				if (qs.isCond(5))
				{
					qs.setCond(6);
					htmltext = event;
				}
				break;
			}
			
			case "33518-04.html":
			{
				if (qs.isCond(7))
				{
					switch (player.getRace())
					{
						case HUMAN:
						{
							if (player.getClassId().isMage())
							{
								qs.setCond(9);
								
							}
							else
							{
								qs.setCond(8);
							}
							break;
						}
						case ELF:
						{
							if (player.getClassId().isMage())
							{
								qs.setCond(11);
								
							}
							else
							{
								qs.setCond(10);
							}
							break;
						}
						case DARK_ELF:
						{
							qs.setCond(12);
							break;
						}
						case ORC:
						{
							qs.setCond(13);
							break;
						}
						case DWARF:
						{
							qs.setCond(14);
							break;
						}
						case KAMAEL:
						{
							qs.setCond(15);
							break;
						}
					}
					htmltext = event;
				}
				break;
			}
		}
		if (event.startsWith("CC") && (qs.getCond() > 7)) // Change Class
		{
			final ClassId desired = ClassId.valueOf(event.replace("CC_", ""));
			switch (desired)
			{
				case PALADIN:
				case DARK_AVENGER:
				{
					if (player.getClassId() == ClassId.KNIGHT)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case GLADIATOR:
				case WARLORD:
				{
					if (player.getClassId() == ClassId.WARRIOR)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case HAWKEYE:
				case TREASURE_HUNTER:
				{
					if (player.getClassId() == ClassId.ROGUE)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case SORCERER:
				case WARLOCK:
				case NECROMANCER:
				{
					if (player.getClassId() == ClassId.WIZARD)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case BISHOP:
				case PROPHET:
				{
					if (player.getClassId() == ClassId.CLERIC)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case TEMPLE_KNIGHT:
				case SWORDSINGER:
				{
					if (player.getClassId() == ClassId.ELVEN_KNIGHT)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case SILVER_RANGER:
				case PLAINS_WALKER:
				{
					if (player.getClassId() == ClassId.ELVEN_SCOUT)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case ELDER:
				{
					if (player.getClassId() == ClassId.ORACLE)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case SPELLSINGER:
				case ELEMENTAL_SUMMONER:
				{
					if (player.getClassId() == ClassId.ELVEN_WIZARD)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case SHILLIEN_KNIGHT:
				case BLADEDANCER:
				{
					if (player.getClassId() == ClassId.PALUS_KNIGHT)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case PHANTOM_RANGER:
				case ABYSS_WALKER:
				{
					if (player.getClassId() == ClassId.ASSASSIN)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case SPELLHOWLER:
				case PHANTOM_SUMMONER:
				{
					if (player.getClassId() == ClassId.DARK_WIZARD)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case SHILLIEN_ELDER:
				{
					if (player.getClassId() == ClassId.SHILLIEN_ORACLE)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case DESTROYER:
				{
					if (player.getClassId() == ClassId.ORC_RAIDER)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case TYRANT:
				{
					if (player.getClassId() == ClassId.ORC_MONK)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case OVERLORD:
				case WARCRYER:
				{
					if (player.getClassId() == ClassId.ORC_SHAMAN)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case BOUNTY_HUNTER:
				{
					if (player.getClassId() == ClassId.SCAVENGER)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case WARSMITH:
				{
					if (player.getClassId() == ClassId.ARTISAN)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case BERSERKER:
				case MALE_SOULBREAKER:
				{
					if (player.getClassId() == ClassId.TROOPER)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
				case ARBALESTER:
				case FEMALE_SOULBREAKER:
				{
					if (player.getClassId() == ClassId.WARDER)
					{
						player.setBaseClass(desired);
						player.setClassId(desired.getId());
					}
					break;
				}
			}
			giveAdena(player, ADENA_REWARD, true);
			addExpAndSp(player, EXP_REWARD, SP_REWARD);
			giveItems(player, PROOF_OF_JUSTICE);
			MultisellData.getInstance().separateAndSend(718, player, npc, false);
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
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case RAYMOND:
					{
						if (player.getClassId().getParent() == ClassId.MAGE)
						{
							htmltext = "30289-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case RAINS:
					{
						if (player.getClassId().getParent() == ClassId.FIGHTER)
						{
							htmltext = "30288-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case ELLENIA:
					{
						if (player.getClassId().getParent() == ClassId.ELVEN_FIGHTER)
						{
							htmltext = "30155-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case ESRANDELL:
					{
						if (player.getClassId().getParent() == ClassId.ELVEN_MAGE)
						{
							htmltext = "30158-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case TOBIAS:
					{
						if ((player.getRace() == Race.DARK_ELF) && (player.getClassId().level() == 1))
						{
							htmltext = "30297-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case DRIKUS:
					{
						if ((player.getRace() == Race.ORC) && (player.getClassId().level() == 1))
						{
							htmltext = "30505-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case MENDIO:
					{
						if ((player.getRace() == Race.DWARF) && (player.getClassId().level() == 1))
						{
							htmltext = "30504-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case GERSHWIN:
					{
						if ((player.getRace() == Race.KAMAEL) && (player.getClassId().level() == 1))
						{
							htmltext = "32196-01.htm";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					default:
					{
						htmltext = getNoQuestMsg(player);
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case RAYMOND:
					{
						if (qs.isCond(9))
						{
							switch (player.getClassId())
							{
								case CLERIC:
								{
									htmltext = "30289-04b.html";
									break;
								}
								case WIZARD:
								{
									htmltext = "30289-04a.html";
									break;
								}
								default:
								{
									htmltext = getNoQuestMsg(player);
									break;
								}
							}
						}
						else if (qs.isCond(1))
						{
							htmltext = "30289-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case RAINS:
					{
						if (qs.isCond(8))
						{
							switch (player.getClassId())
							{
								case KNIGHT:
								{
									htmltext = "30288-04a.html";
									break;
								}
								case WARRIOR:
								{
									htmltext = "30288-04b.html";
									break;
								}
								case ROGUE:
								{
									htmltext = "30288-04c.html";
									break;
								}
								default:
								{
									htmltext = getNoQuestMsg(player);
									break;
								}
							}
						}
						else if (qs.isCond(1))
						{
							htmltext = "30288-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case ELLENIA:
					{
						if (qs.isCond(10))
						{
							switch (player.getClassId())
							{
								case ELVEN_KNIGHT:
								{
									htmltext = "30155-04a.html";
									break;
								}
								case ELVEN_SCOUT:
								{
									htmltext = "30155-04b.html";
									break;
								}
								default:
								{
									htmltext = getNoQuestMsg(player);
									break;
								}
							}
						}
						else if (qs.isCond(1))
						{
							htmltext = "30155-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case ESRANDELL:
					{
						if (qs.isCond(11))
						{
							switch (player.getClassId())
							{
								case ELVEN_WIZARD:
								{
									htmltext = "30158-04a.html";
									break;
								}
								case ORACLE:
								{
									htmltext = "30158-04b.html";
									break;
								}
								default:
								{
									htmltext = getNoQuestMsg(player);
									break;
								}
							}
						}
						else if (qs.isCond(1))
						{
							htmltext = "30158-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case TOBIAS:
					{
						if (qs.isCond(12))
						{
							switch (player.getClassId())
							{
								case SHILLIEN_ORACLE:
								{
									htmltext = "30297-04d.html";
									break;
								}
								case DARK_WIZARD:
								{
									htmltext = "30297-04c.html";
									break;
								}
								case ASSASSIN:
								{
									htmltext = "30297-04b.html";
									break;
								}
								case PALUS_KNIGHT:
								{
									htmltext = "30297-04a.html";
									break;
								}
								default:
								{
									getNoQuestMsg(player);
									break;
								}
							}
						}
						else if (qs.isCond(1))
						{
							htmltext = "30297-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case DRIKUS:
					{
						if (qs.isCond(13))
						{
							switch (player.getClassId())
							{
								case ORC_SHAMAN:
								{
									htmltext = "30505-04c.html";
									break;
								}
								case ORC_MONK:
								{
									htmltext = "30505-04b.html";
									break;
								}
								case ORC_RAIDER:
								{
									htmltext = "30505-04a.html";
									break;
								}
								default:
								{
									htmltext = getNoQuestMsg(player);
									break;
								}
							}
						}
						else if (qs.isCond(1))
						{
							htmltext = "30505-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case MENDIO:
					{
						if (qs.isCond(14))
						{
							switch (player.getClassId())
							{
								case SCAVENGER:
								{
									htmltext = "30504-04a.html";
									break;
								}
								case ARTISAN:
								{
									htmltext = "30504-04b.html";
									break;
								}
								default:
								{
									htmltext = getNoQuestMsg(player);
									break;
								}
							}
						}
						else if (qs.isCond(1))
						{
							htmltext = "30504-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case GERSHWIN:
					{
						if (qs.isCond(15))
						{
							switch (player.getClassId())
							{
								case TROOPER:
								{
									htmltext = "32196-04a.html";
									break;
								}
								case WARDER:
								{
									htmltext = "32196-04b.html";
									break;
								}
								default:
								{
									htmltext = getNoQuestMsg(player);
									break;
								}
							}
						}
						else if (qs.isCond(1))
						{
							htmltext = "32196-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case RENFAD:
					{
						if (qs.isCond(1))
						{
							htmltext = "33524-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "33524-04.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case JOEL:
					{
						if (qs.isCond(3))
						{
							htmltext = "33516-01.html";
						}
						else if (qs.isCond(4))
						{
							htmltext = "33516-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case SCHUAZEN:
					{
						if (qs.isCond(5))
						{
							htmltext = "33517-01.html";
						}
						else if (qs.isCond(6))
						{
							htmltext = "33517-03.html";
						}
						else
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
					case SELON:
					{
						if (qs.isCond(7))
						{
							htmltext = "33518-01.html";
						}
						else if (qs.getCond() > 7)
						{
							htmltext = "33518-05.html";
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
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isStarted() && (qs.getCond() > 1) && (qs.getCond() < 7) && (Util.checkIfInRange(1500, npc, qs.getPlayer(), false)))
		{
			switch (npc.getId())
			{
				case RKANILOV:
				{
					if (qs.isCond(2))
					{
						qs.setCond(3);
					}
					break;
				}
				case RPOSLOF:
				{
					if (qs.isCond(4))
					{
						qs.setCond(5);
					}
					break;
				}
				case SAKUM:
				{
					if (qs.isCond(6))
					{
						qs.setCond(7);
						giveItems(killer, SHINE_STONE);
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
