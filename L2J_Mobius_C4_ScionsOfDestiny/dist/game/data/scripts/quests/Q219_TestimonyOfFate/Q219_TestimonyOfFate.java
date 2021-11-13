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
package quests.Q219_TestimonyOfFate;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Q219_TestimonyOfFate extends Quest
{
	// NPCs
	private static final int KAIRA = 30476;
	private static final int METHEUS = 30614;
	private static final int IXIA = 30463;
	private static final int ALDER_SPIRIT = 30613;
	private static final int ROA = 30114;
	private static final int NORMAN = 30210;
	private static final int THIFIELL = 30358;
	private static final int ARKENIA = 30419;
	private static final int BLOODY_PIXY = 31845;
	private static final int BLIGHT_TREANT = 31850;
	// Monsters
	private static final int HANGMAN_TREE = 20144;
	private static final int MARSH_STAKATO = 20157;
	private static final int MEDUSA = 20158;
	private static final int TYRANT = 20192;
	private static final int TYRANT_KINGPIN = 20193;
	private static final int DEAD_SEEKER = 20202;
	private static final int MARSH_STAKATO_WORKER = 20230;
	private static final int MARSH_STAKATO_SOLDIER = 20232;
	private static final int MARSH_SPIDER = 20233;
	private static final int MARSH_STAKATO_DRONE = 20234;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int GRANDIS = 20554;
	private static final int LETO_LIZARDMAN_OVERLORD = 20582;
	private static final int KARUL_BUGBEAR = 20600;
	private static final int BLACK_WILLOW_LURKER = 27079;
	// Items
	private static final int KAIRA_LETTER = 3173;
	private static final int METHEUS_FUNERAL_JAR = 3174;
	private static final int KASANDRA_REMAINS = 3175;
	private static final int HERBALISM_TEXTBOOK = 3176;
	private static final int IXIA_LIST = 3177;
	private static final int MEDUSA_ICHOR = 3178;
	private static final int MARSH_SPIDER_FLUIDS = 3179;
	private static final int DEAD_SEEKER_DUNG = 3180;
	private static final int TYRANT_BLOOD = 3181;
	private static final int NIGHTSHADE_ROOT = 3182;
	private static final int BELLADONNA = 3183;
	private static final int ALDER_SKULL_1 = 3184;
	private static final int ALDER_SKULL_2 = 3185;
	private static final int ALDER_RECEIPT = 3186;
	private static final int REVELATIONS_MANUSCRIPT = 3187;
	private static final int KAIRA_RECOMMENDATION = 3189;
	private static final int KAIRA_INSTRUCTIONS = 3188;
	private static final int PALUS_CHARM = 3190;
	private static final int THIFIELL_LETTER = 3191;
	private static final int ARKENIA_NOTE = 3192;
	private static final int PIXY_GARNET = 3193;
	private static final int GRANDIS_SKULL = 3194;
	private static final int KARUL_BUGBEAR_SKULL = 3195;
	private static final int BREKA_OVERLORD_SKULL = 3196;
	private static final int LETO_OVERLORD_SKULL = 3197;
	private static final int RED_FAIRY_DUST = 3198;
	private static final int BLIGHT_TREANT_SEED = 3199;
	private static final int BLACK_WILLOW_LEAF = 3200;
	private static final int BLIGHT_TREANT_SAP = 3201;
	private static final int ARKENIA_LETTER = 3202;
	// Rewards
	private static final int MARK_OF_FATE = 3172;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	// Cond 6 drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(DEAD_SEEKER, 500000);
		CHANCES.put(TYRANT, 500000);
		CHANCES.put(TYRANT_KINGPIN, 600000);
		CHANCES.put(MEDUSA, 500000);
		CHANCES.put(MARSH_STAKATO, 400000);
		CHANCES.put(MARSH_STAKATO_WORKER, 300000);
		CHANCES.put(MARSH_STAKATO_SOLDIER, 500000);
		CHANCES.put(MARSH_STAKATO_DRONE, 600000);
		CHANCES.put(MARSH_SPIDER, 500000);
	}
	
	public Q219_TestimonyOfFate()
	{
		super(219, "Testimony of Fate");
		registerQuestItems(KAIRA_LETTER, METHEUS_FUNERAL_JAR, KASANDRA_REMAINS, HERBALISM_TEXTBOOK, IXIA_LIST, MEDUSA_ICHOR, MARSH_SPIDER_FLUIDS, DEAD_SEEKER_DUNG, TYRANT_BLOOD, NIGHTSHADE_ROOT, BELLADONNA, ALDER_SKULL_1, ALDER_SKULL_2, ALDER_RECEIPT, REVELATIONS_MANUSCRIPT, KAIRA_RECOMMENDATION, KAIRA_INSTRUCTIONS, PALUS_CHARM, THIFIELL_LETTER, ARKENIA_NOTE, PIXY_GARNET, GRANDIS_SKULL, KARUL_BUGBEAR_SKULL, BREKA_OVERLORD_SKULL, LETO_OVERLORD_SKULL, RED_FAIRY_DUST, BLIGHT_TREANT_SEED, BLACK_WILLOW_LEAF, BLIGHT_TREANT_SAP, ARKENIA_LETTER);
		addStartNpc(KAIRA);
		addTalkId(KAIRA, METHEUS, IXIA, ALDER_SPIRIT, ROA, NORMAN, THIFIELL, ARKENIA, BLOODY_PIXY, BLIGHT_TREANT);
		addKillId(HANGMAN_TREE, MARSH_STAKATO, MEDUSA, TYRANT, TYRANT_KINGPIN, DEAD_SEEKER, MARSH_STAKATO_WORKER, MARSH_STAKATO_SOLDIER, MARSH_SPIDER, MARSH_STAKATO_DRONE, BREKA_ORC_OVERLORD, GRANDIS, LETO_LIZARDMAN_OVERLORD, KARUL_BUGBEAR, BLACK_WILLOW_LURKER);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30476-05.htm":
			{
				st.startQuest();
				st.giveItems(KAIRA_LETTER, 1);
				if (!player.getVariables().getBoolean("secondClassChange37", false))
				{
					htmltext = "30476-05a.htm";
					st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37.get(player.getRace().ordinal()));
					player.getVariables().set("secondClassChange37", true);
				}
				break;
			}
			case "30114-04.htm":
			{
				st.setCond(12);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(ALDER_SKULL_2, 1);
				st.giveItems(ALDER_RECEIPT, 1);
				break;
			}
			case "30476-12.htm":
			{
				st.playSound(QuestState.SOUND_MIDDLE);
				if (player.getLevel() < 38)
				{
					htmltext = "30476-13.htm";
					st.setCond(14);
					st.giveItems(KAIRA_INSTRUCTIONS, 1);
				}
				else
				{
					st.setCond(15);
					st.takeItems(REVELATIONS_MANUSCRIPT, 1);
					st.giveItems(KAIRA_RECOMMENDATION, 1);
				}
				break;
			}
			case "30419-02.htm":
			{
				st.setCond(17);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(THIFIELL_LETTER, 1);
				st.giveItems(ARKENIA_NOTE, 1);
				break;
			}
			case "31845-02.htm":
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(PIXY_GARNET, 1);
				break;
			}
			case "31850-02.htm":
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(BLIGHT_TREANT_SEED, 1);
				break;
			}
			case "30419-05.htm":
			{
				st.setCond(18);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(ARKENIA_NOTE, 1);
				st.takeItems(BLIGHT_TREANT_SAP, 1);
				st.takeItems(RED_FAIRY_DUST, 1);
				st.giveItems(ARKENIA_LETTER, 1);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getRace() != Race.DARK_ELF)
				{
					htmltext = "30476-02.htm";
				}
				else if ((player.getLevel() < 37) || (player.getClassId().level() != 1))
				{
					htmltext = "30476-01.htm";
				}
				else
				{
					htmltext = "30476-03.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case KAIRA:
					{
						if (cond == 1)
						{
							htmltext = "30476-06.htm";
						}
						else if ((cond == 2) || (cond == 3))
						{
							htmltext = "30476-07.htm";
						}
						else if ((cond > 3) && (cond < 9))
						{
							htmltext = "30476-08.htm";
						}
						else if (cond == 9)
						{
							htmltext = "30476-09.htm";
							st.setCond(10);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ALDER_SKULL_1, 1);
							addSpawn(ALDER_SPIRIT, player, false, 0);
						}
						else if ((cond > 9) && (cond < 13))
						{
							htmltext = "30476-10.htm";
						}
						else if (cond == 13)
						{
							htmltext = "30476-11.htm";
						}
						else if (cond == 14)
						{
							if (player.getLevel() < 38)
							{
								htmltext = "30476-14.htm";
							}
							else
							{
								htmltext = "30476-12.htm";
								st.setCond(15);
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(KAIRA_INSTRUCTIONS, 1);
								st.takeItems(REVELATIONS_MANUSCRIPT, 1);
								st.giveItems(KAIRA_RECOMMENDATION, 1);
							}
						}
						else if (cond == 15)
						{
							htmltext = "30476-16.htm";
						}
						else if (cond > 15)
						{
							htmltext = "30476-17.htm";
						}
						break;
					}
					case METHEUS:
					{
						if (cond == 1)
						{
							htmltext = "30614-01.htm";
							st.setCond(2);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(KAIRA_LETTER, 1);
							st.giveItems(METHEUS_FUNERAL_JAR, 1);
						}
						else if (cond == 2)
						{
							htmltext = "30614-02.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30614-03.htm";
							st.setCond(4);
							st.setCond(5);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(KASANDRA_REMAINS, 1);
							st.giveItems(HERBALISM_TEXTBOOK, 1);
						}
						else if ((cond > 3) && (cond < 8))
						{
							htmltext = "30614-04.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30614-05.htm";
							st.setCond(9);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BELLADONNA, 1);
							st.giveItems(ALDER_SKULL_1, 1);
						}
						else if (cond > 8)
						{
							htmltext = "30614-06.htm";
						}
						break;
					}
					case IXIA:
					{
						if (cond == 5)
						{
							htmltext = "30463-01.htm";
							st.setCond(6);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(HERBALISM_TEXTBOOK, 1);
							st.giveItems(IXIA_LIST, 1);
						}
						else if (cond == 6)
						{
							htmltext = "30463-02.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30463-03.htm";
							st.setCond(8);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(IXIA_LIST, 1);
							st.takeItems(DEAD_SEEKER_DUNG, -1);
							st.takeItems(MARSH_SPIDER_FLUIDS, -1);
							st.takeItems(MEDUSA_ICHOR, -1);
							st.takeItems(NIGHTSHADE_ROOT, -1);
							st.takeItems(TYRANT_BLOOD, -1);
							st.giveItems(BELLADONNA, 1);
						}
						else if (cond == 8)
						{
							htmltext = "30463-04.htm";
						}
						else if (cond > 8)
						{
							htmltext = "30463-05.htm";
						}
						break;
					}
					case ALDER_SPIRIT:
					{
						if (cond == 10)
						{
							htmltext = "30613-01.htm";
							st.setCond(11);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(ALDER_SKULL_2, 1);
							npc.deleteMe();
						}
						break;
					}
					case ROA:
					{
						if (cond == 11)
						{
							htmltext = "30114-01.htm";
						}
						else if (cond == 12)
						{
							htmltext = "30114-05.htm";
						}
						else if (cond > 12)
						{
							htmltext = "30114-06.htm";
						}
						break;
					}
					case NORMAN:
					{
						if (cond == 12)
						{
							htmltext = "30210-01.htm";
							st.setCond(13);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ALDER_RECEIPT, 1);
							st.giveItems(REVELATIONS_MANUSCRIPT, 1);
						}
						else if (cond > 12)
						{
							htmltext = "30210-02.htm";
						}
						break;
					}
					case THIFIELL:
					{
						if (cond == 15)
						{
							htmltext = "30358-01.htm";
							st.setCond(16);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(KAIRA_RECOMMENDATION, 1);
							st.giveItems(PALUS_CHARM, 1);
							st.giveItems(THIFIELL_LETTER, 1);
						}
						else if (cond == 16)
						{
							htmltext = "30358-02.htm";
						}
						else if (cond == 17)
						{
							htmltext = "30358-03.htm";
						}
						else if (cond == 18)
						{
							htmltext = "30358-04.htm";
							st.takeItems(PALUS_CHARM, 1);
							st.takeItems(ARKENIA_LETTER, 1);
							st.giveItems(MARK_OF_FATE, 1);
							st.rewardExpAndSp(68183, 1750);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					}
					case ARKENIA:
					{
						if (cond == 16)
						{
							htmltext = "30419-01.htm";
						}
						else if (cond == 17)
						{
							htmltext = (st.hasQuestItems(BLIGHT_TREANT_SAP) && st.hasQuestItems(RED_FAIRY_DUST)) ? "30419-04.htm" : "30419-03.htm";
						}
						else if (cond == 18)
						{
							htmltext = "30419-06.htm";
						}
						break;
					}
					case BLOODY_PIXY:
					{
						if (cond == 17)
						{
							if (st.hasQuestItems(PIXY_GARNET))
							{
								if ((st.getQuestItemsCount(GRANDIS_SKULL) >= 10) && (st.getQuestItemsCount(KARUL_BUGBEAR_SKULL) >= 10) && (st.getQuestItemsCount(BREKA_OVERLORD_SKULL) >= 10) && (st.getQuestItemsCount(LETO_OVERLORD_SKULL) >= 10))
								{
									htmltext = "31845-04.htm";
									st.playSound(QuestState.SOUND_ITEMGET);
									st.takeItems(BREKA_OVERLORD_SKULL, -1);
									st.takeItems(GRANDIS_SKULL, -1);
									st.takeItems(KARUL_BUGBEAR_SKULL, -1);
									st.takeItems(LETO_OVERLORD_SKULL, -1);
									st.takeItems(PIXY_GARNET, 1);
									st.giveItems(RED_FAIRY_DUST, 1);
								}
								else
								{
									htmltext = "31845-03.htm";
								}
							}
							else if (st.hasQuestItems(RED_FAIRY_DUST))
							{
								htmltext = "31845-05.htm";
							}
							else
							{
								htmltext = "31845-01.htm";
							}
						}
						else if (cond == 18)
						{
							htmltext = "31845-05.htm";
						}
						break;
					}
					case BLIGHT_TREANT:
					{
						if (cond == 17)
						{
							if (st.hasQuestItems(BLIGHT_TREANT_SEED))
							{
								if (st.hasQuestItems(BLACK_WILLOW_LEAF))
								{
									htmltext = "31850-04.htm";
									st.playSound(QuestState.SOUND_ITEMGET);
									st.takeItems(BLACK_WILLOW_LEAF, 1);
									st.takeItems(BLIGHT_TREANT_SEED, 1);
									st.giveItems(BLIGHT_TREANT_SAP, 1);
								}
								else
								{
									htmltext = "31850-03.htm";
								}
							}
							else if (st.hasQuestItems(BLIGHT_TREANT_SAP))
							{
								htmltext = "31850-05.htm";
							}
							else
							{
								htmltext = "31850-01.htm";
							}
						}
						else if (cond == 18)
						{
							htmltext = "31850-05.htm";
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
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case HANGMAN_TREE:
			{
				if (st.isCond(2))
				{
					st.setCond(3);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(METHEUS_FUNERAL_JAR, 1);
					st.giveItems(KASANDRA_REMAINS, 1);
				}
				break;
			}
			case DEAD_SEEKER:
			{
				if (st.isCond(6) && st.dropItems(DEAD_SEEKER_DUNG, 1, 10, CHANCES.get(npcId)) && (st.getQuestItemsCount(TYRANT_BLOOD) >= 10) && (st.getQuestItemsCount(MEDUSA_ICHOR) >= 10) && (st.getQuestItemsCount(NIGHTSHADE_ROOT) >= 10) && (st.getQuestItemsCount(MARSH_SPIDER_FLUIDS) >= 10))
				{
					st.setCond(7);
				}
				break;
			}
			case TYRANT:
			case TYRANT_KINGPIN:
			{
				if (st.isCond(6) && st.dropItems(TYRANT_BLOOD, 1, 10, CHANCES.get(npcId)) && (st.getQuestItemsCount(DEAD_SEEKER_DUNG) >= 10) && (st.getQuestItemsCount(MEDUSA_ICHOR) >= 10) && (st.getQuestItemsCount(NIGHTSHADE_ROOT) >= 10) && (st.getQuestItemsCount(MARSH_SPIDER_FLUIDS) >= 10))
				{
					st.setCond(7);
				}
				break;
			}
			case MEDUSA:
			{
				if (st.isCond(6) && st.dropItems(MEDUSA_ICHOR, 1, 10, CHANCES.get(npcId)) && (st.getQuestItemsCount(DEAD_SEEKER_DUNG) >= 10) && (st.getQuestItemsCount(TYRANT_BLOOD) >= 10) && (st.getQuestItemsCount(NIGHTSHADE_ROOT) >= 10) && (st.getQuestItemsCount(MARSH_SPIDER_FLUIDS) >= 10))
				{
					st.setCond(7);
				}
				break;
			}
			case MARSH_STAKATO:
			case MARSH_STAKATO_WORKER:
			case MARSH_STAKATO_SOLDIER:
			case MARSH_STAKATO_DRONE:
			{
				if (st.isCond(6) && st.dropItems(NIGHTSHADE_ROOT, 1, 10, CHANCES.get(npcId)) && (st.getQuestItemsCount(DEAD_SEEKER_DUNG) >= 10) && (st.getQuestItemsCount(TYRANT_BLOOD) >= 10) && (st.getQuestItemsCount(MEDUSA_ICHOR) >= 10) && (st.getQuestItemsCount(MARSH_SPIDER_FLUIDS) >= 10))
				{
					st.setCond(7);
				}
				break;
			}
			case MARSH_SPIDER:
			{
				if (st.isCond(6) && st.dropItems(MARSH_SPIDER_FLUIDS, 1, 10, CHANCES.get(npcId)) && (st.getQuestItemsCount(DEAD_SEEKER_DUNG) >= 10) && (st.getQuestItemsCount(TYRANT_BLOOD) >= 10) && (st.getQuestItemsCount(MEDUSA_ICHOR) >= 10) && (st.getQuestItemsCount(NIGHTSHADE_ROOT) >= 10))
				{
					st.setCond(7);
				}
				break;
			}
			case GRANDIS:
			{
				if (st.hasQuestItems(PIXY_GARNET))
				{
					st.dropItemsAlways(GRANDIS_SKULL, 1, 10);
				}
				break;
			}
			case LETO_LIZARDMAN_OVERLORD:
			{
				if (st.hasQuestItems(PIXY_GARNET))
				{
					st.dropItemsAlways(LETO_OVERLORD_SKULL, 1, 10);
				}
				break;
			}
			case BREKA_ORC_OVERLORD:
			{
				if (st.hasQuestItems(PIXY_GARNET))
				{
					st.dropItemsAlways(BREKA_OVERLORD_SKULL, 1, 10);
				}
				break;
			}
			case KARUL_BUGBEAR:
			{
				if (st.hasQuestItems(PIXY_GARNET))
				{
					st.dropItemsAlways(KARUL_BUGBEAR_SKULL, 1, 10);
				}
				break;
			}
			case BLACK_WILLOW_LURKER:
			{
				if (st.hasQuestItems(BLIGHT_TREANT_SEED))
				{
					st.dropItemsAlways(BLACK_WILLOW_LEAF, 1, 1);
				}
				break;
			}
		}
		
		return null;
	}
}