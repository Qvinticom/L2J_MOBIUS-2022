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
package quests.Q230_TestOfTheSummoner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Q230_TestOfTheSummoner extends Quest
{
	// NPCs
	private static final int LARA = 30063;
	private static final int GALATEA = 30634;
	private static final int ALMORS = 30635;
	private static final int CAMONIELL = 30636;
	private static final int BELTHUS = 30637;
	private static final int BASILLA = 30638;
	private static final int CELESTIEL = 30639;
	private static final int BRYNTHEA = 30640;
	// Monsters
	private static final int NOBLE_ANT = 20089;
	private static final int NOBLE_ANT_LEADER = 20090;
	private static final int WYRM = 20176;
	private static final int TYRANT = 20192;
	private static final int TYRANT_KINGPIN = 20193;
	private static final int BREKA_ORC = 20267;
	private static final int BREKA_ORC_ARCHER = 20268;
	private static final int BREKA_ORC_SHAMAN = 20269;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int BREKA_ORC_WARRIOR = 20271;
	private static final int FETTERED_SOUL = 20552;
	private static final int WINDSUS = 20553;
	private static final int GIANT_FUNGUS = 20555;
	private static final int MANASHEN_GARGOYLE = 20563;
	private static final int LETO_LIZARDMAN = 20577;
	private static final int LETO_LIZARDMAN_ARCHER = 20578;
	private static final int LETO_LIZARDMAN_SOLDIER = 20579;
	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
	private static final int LETO_LIZARDMAN_SHAMAN = 20581;
	private static final int LETO_LIZARDMAN_OVERLORD = 20582;
	private static final int KARUL_BUGBEAR = 20600;
	// Quest Monsters
	private static final int PAKO_THE_CAT = 27102;
	private static final int UNICORN_RACER = 27103;
	private static final int SHADOW_TUREN = 27104;
	private static final int MIMI_THE_CAT = 27105;
	private static final int UNICORN_PHANTASM = 27106;
	private static final int SILHOUETTE_TILFO = 27107;
	// Items
	private static final int LETO_LIZARDMAN_AMULET = 3337;
	private static final int SAC_OF_REDSPORES = 3338;
	private static final int KARUL_BUGBEAR_TOTEM = 3339;
	private static final int SHARDS_OF_MANASHEN = 3340;
	private static final int BREKA_ORC_TOTEM = 3341;
	private static final int CRIMSON_BLOODSTONE = 3342;
	private static final int TALONS_OF_TYRANT = 3343;
	private static final int WINGS_OF_DRONEANT = 3344;
	private static final int TUSK_OF_WINDSUS = 3345;
	private static final int FANGS_OF_WYRM = 3346;
	private static final int LARA_LIST_1 = 3347;
	private static final int LARA_LIST_2 = 3348;
	private static final int LARA_LIST_3 = 3349;
	private static final int LARA_LIST_4 = 3350;
	private static final int LARA_LIST_5 = 3351;
	private static final int GALATEA_LETTER = 3352;
	private static final int BEGINNER_ARCANA = 3353;
	private static final int ALMORS_ARCANA = 3354;
	private static final int CAMONIELL_ARCANA = 3355;
	private static final int BELTHUS_ARCANA = 3356;
	private static final int BASILLIA_ARCANA = 3357;
	private static final int CELESTIEL_ARCANA = 3358;
	private static final int BRYNTHEA_ARCANA = 3359;
	private static final int CRYSTAL_OF_PROGRESS_1 = 3360;
	private static final int CRYSTAL_OF_INPROGRESS_1 = 3361;
	private static final int CRYSTAL_OF_FOUL_1 = 3362;
	private static final int CRYSTAL_OF_DEFEAT_1 = 3363;
	private static final int CRYSTAL_OF_VICTORY_1 = 3364;
	private static final int CRYSTAL_OF_PROGRESS_2 = 3365;
	private static final int CRYSTAL_OF_INPROGRESS_2 = 3366;
	private static final int CRYSTAL_OF_FOUL_2 = 3367;
	private static final int CRYSTAL_OF_DEFEAT_2 = 3368;
	private static final int CRYSTAL_OF_VICTORY_2 = 3369;
	private static final int CRYSTAL_OF_PROGRESS_3 = 3370;
	private static final int CRYSTAL_OF_INPROGRESS_3 = 3371;
	private static final int CRYSTAL_OF_FOUL_3 = 3372;
	private static final int CRYSTAL_OF_DEFEAT_3 = 3373;
	private static final int CRYSTAL_OF_VICTORY_3 = 3374;
	private static final int CRYSTAL_OF_PROGRESS_4 = 3375;
	private static final int CRYSTAL_OF_INPROGRESS_4 = 3376;
	private static final int CRYSTAL_OF_FOUL_4 = 3377;
	private static final int CRYSTAL_OF_DEFEAT_4 = 3378;
	private static final int CRYSTAL_OF_VICTORY_4 = 3379;
	private static final int CRYSTAL_OF_PROGRESS_5 = 3380;
	private static final int CRYSTAL_OF_INPROGRESS_5 = 3381;
	private static final int CRYSTAL_OF_FOUL_5 = 3382;
	private static final int CRYSTAL_OF_DEFEAT_5 = 3383;
	private static final int CRYSTAL_OF_VICTORY_5 = 3384;
	private static final int CRYSTAL_OF_PROGRESS_6 = 3385;
	private static final int CRYSTAL_OF_INPROGRESS_6 = 3386;
	private static final int CRYSTAL_OF_FOUL_6 = 3387;
	private static final int CRYSTAL_OF_DEFEAT_6 = 3388;
	private static final int CRYSTAL_OF_VICTORY_6 = 3389;
	// Rewards
	private static final int MARK_OF_SUMMONER = 3336;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	// Other
	private static final int[][] LARA_LISTS = new int[][]
	{
		// @formatter:off
		{LARA_LIST_1, SAC_OF_REDSPORES, LETO_LIZARDMAN_AMULET},
		{LARA_LIST_2, KARUL_BUGBEAR_TOTEM, SHARDS_OF_MANASHEN},
		{LARA_LIST_3, CRIMSON_BLOODSTONE, BREKA_ORC_TOTEM},
		{LARA_LIST_4, TUSK_OF_WINDSUS, TALONS_OF_TYRANT},
		{LARA_LIST_5, WINGS_OF_DRONEANT, FANGS_OF_WYRM}
		// @formatter:on
	};
	
	private static final Map<Integer, ProgressDuelMob> _duelsInProgress = new ConcurrentHashMap<>();
	
	public Q230_TestOfTheSummoner()
	{
		super(230, "Test of the Summoner");
		registerQuestItems(LETO_LIZARDMAN_AMULET, SAC_OF_REDSPORES, KARUL_BUGBEAR_TOTEM, SHARDS_OF_MANASHEN, BREKA_ORC_TOTEM, CRIMSON_BLOODSTONE, TALONS_OF_TYRANT, WINGS_OF_DRONEANT, TUSK_OF_WINDSUS, FANGS_OF_WYRM, LARA_LIST_1, LARA_LIST_2, LARA_LIST_3, LARA_LIST_4, LARA_LIST_5, GALATEA_LETTER, BEGINNER_ARCANA, ALMORS_ARCANA, CAMONIELL_ARCANA, BELTHUS_ARCANA, BASILLIA_ARCANA, CELESTIEL_ARCANA, BRYNTHEA_ARCANA, CRYSTAL_OF_PROGRESS_1, CRYSTAL_OF_INPROGRESS_1, CRYSTAL_OF_FOUL_1, CRYSTAL_OF_DEFEAT_1, CRYSTAL_OF_VICTORY_1, CRYSTAL_OF_PROGRESS_2, CRYSTAL_OF_INPROGRESS_2, CRYSTAL_OF_FOUL_2, CRYSTAL_OF_DEFEAT_2, CRYSTAL_OF_VICTORY_2, CRYSTAL_OF_PROGRESS_3, CRYSTAL_OF_INPROGRESS_3, CRYSTAL_OF_FOUL_3, CRYSTAL_OF_DEFEAT_3, CRYSTAL_OF_VICTORY_3, CRYSTAL_OF_PROGRESS_4, CRYSTAL_OF_INPROGRESS_4, CRYSTAL_OF_FOUL_4, CRYSTAL_OF_DEFEAT_4, CRYSTAL_OF_VICTORY_4, CRYSTAL_OF_PROGRESS_5, CRYSTAL_OF_INPROGRESS_5, CRYSTAL_OF_FOUL_5, CRYSTAL_OF_DEFEAT_5, CRYSTAL_OF_VICTORY_5, CRYSTAL_OF_PROGRESS_6, CRYSTAL_OF_INPROGRESS_6, CRYSTAL_OF_FOUL_6, CRYSTAL_OF_DEFEAT_6, CRYSTAL_OF_VICTORY_6);
		addStartNpc(GALATEA);
		addTalkId(GALATEA, ALMORS, CAMONIELL, BELTHUS, BASILLA, CELESTIEL, BRYNTHEA, LARA);
		addKillId(NOBLE_ANT, NOBLE_ANT_LEADER, WYRM, TYRANT, TYRANT_KINGPIN, BREKA_ORC, BREKA_ORC_ARCHER, BREKA_ORC_SHAMAN, BREKA_ORC_OVERLORD, BREKA_ORC_WARRIOR, FETTERED_SOUL, WINDSUS, GIANT_FUNGUS, MANASHEN_GARGOYLE, LETO_LIZARDMAN, LETO_LIZARDMAN_ARCHER, LETO_LIZARDMAN_SOLDIER, LETO_LIZARDMAN_WARRIOR, LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD, KARUL_BUGBEAR, PAKO_THE_CAT, UNICORN_RACER, SHADOW_TUREN, MIMI_THE_CAT, UNICORN_PHANTASM, SILHOUETTE_TILFO);
		addAttackId(PAKO_THE_CAT, UNICORN_RACER, SHADOW_TUREN, MIMI_THE_CAT, UNICORN_PHANTASM, SILHOUETTE_TILFO);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		String htmltext = event;
		
		switch (event)
		{
			case "30634-08.htm":
			{
				st.startQuest();
				st.set("Belthus", "1");
				st.set("Brynthea", "1");
				st.set("Celestiel", "1");
				st.set("Camoniell", "1");
				st.set("Basilla", "1");
				st.set("Almors", "1");
				st.giveItems(GALATEA_LETTER, 1);
				if (!player.getVariables().getBoolean("secondClassChange39", false))
				{
					htmltext = "30634-08a.htm";
					st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
					player.getVariables().set("secondClassChange39", true);
				}
				break;
			}
			case "30063-02.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(GALATEA_LETTER, 1);
				final int random = Rnd.get(5);
				st.giveItems(LARA_LISTS[random][0], 1);
				st.set("Lara", String.valueOf(random + 1)); // avoid 0
				break;
			}
			case "30063-04.htm":
			{
				final int random = Rnd.get(5);
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(LARA_LISTS[random][0], 1);
				st.set("Lara", String.valueOf(random + 1));
				break;
			}
			case "30635-02.htm":
			{
				if (st.hasQuestItems(BEGINNER_ARCANA))
				{
					htmltext = "30635-03.htm";
				}
				break;
			}
			case "30635-04.htm":
			{
				st.set("Almors", "2"); // set state ready to fight
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(CRYSTAL_OF_FOUL_1, -1); // just in case he cheated or lost
				st.takeItems(CRYSTAL_OF_DEFEAT_1, -1);
				st.takeItems(BEGINNER_ARCANA, 1);
				st.giveItems(CRYSTAL_OF_PROGRESS_1, 1); // give Starting Crystal
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getSkill(4126, 1));
				break;
			}
			case "30636-02.htm":
			{
				if (st.hasQuestItems(BEGINNER_ARCANA))
				{
					htmltext = "30636-03.htm";
				}
				break;
			}
			case "30636-04.htm":
			{
				st.set("Camoniell", "2");
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(CRYSTAL_OF_FOUL_2, -1);
				st.takeItems(CRYSTAL_OF_DEFEAT_2, -1);
				st.takeItems(BEGINNER_ARCANA, 1);
				st.giveItems(CRYSTAL_OF_PROGRESS_2, 1);
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getSkill(4126, 1));
				break;
			}
			case "30637-02.htm":
			{
				if (st.hasQuestItems(BEGINNER_ARCANA))
				{
					htmltext = "30637-03.htm";
				}
				break;
			}
			case "30637-04.htm":
			{
				st.set("Belthus", "2");
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(CRYSTAL_OF_FOUL_3, -1);
				st.takeItems(CRYSTAL_OF_DEFEAT_3, -1);
				st.takeItems(BEGINNER_ARCANA, 1);
				st.giveItems(CRYSTAL_OF_PROGRESS_3, 1);
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getSkill(4126, 1));
				break;
			}
			case "30638-02.htm":
			{
				if (st.hasQuestItems(BEGINNER_ARCANA))
				{
					htmltext = "30638-03.htm";
				}
				break;
			}
			case "30638-04.htm":
			{
				st.set("Basilla", "2");
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(CRYSTAL_OF_FOUL_4, -1);
				st.takeItems(CRYSTAL_OF_DEFEAT_4, -1);
				st.takeItems(BEGINNER_ARCANA, 1);
				st.giveItems(CRYSTAL_OF_PROGRESS_4, 1);
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getSkill(4126, 1));
				break;
			}
			case "30639-02.htm":
			{
				if (st.hasQuestItems(BEGINNER_ARCANA))
				{
					htmltext = "30639-03.htm";
				}
				break;
			}
			case "30639-04.htm":
			{
				st.set("Celestiel", "2");
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(CRYSTAL_OF_FOUL_5, -1);
				st.takeItems(CRYSTAL_OF_DEFEAT_5, -1);
				st.takeItems(BEGINNER_ARCANA, 1);
				st.giveItems(CRYSTAL_OF_PROGRESS_5, 1);
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getSkill(4126, 1));
				break;
			}
			case "30640-02.htm":
			{
				if (st.hasQuestItems(BEGINNER_ARCANA))
				{
					htmltext = "30640-03.htm";
				}
				break;
			}
			case "30640-04.htm":
			{
				st.set("Brynthea", "2");
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(CRYSTAL_OF_FOUL_6, -1);
				st.takeItems(CRYSTAL_OF_DEFEAT_6, -1);
				st.takeItems(BEGINNER_ARCANA, 1);
				st.giveItems(CRYSTAL_OF_PROGRESS_6, 1);
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getSkill(4126, 1));
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		final int cond = st.getCond();
		final int npcId = npc.getNpcId();
		switch (st.getState())
		{
			case State.CREATED:
			{
				if ((player.getClassId() != ClassId.WIZARD) && (player.getClassId() != ClassId.ELVEN_WIZARD) && (player.getClassId() != ClassId.DARK_WIZARD))
				{
					htmltext = "30634-01.htm";
				}
				else if (player.getLevel() < 39)
				{
					htmltext = "30634-02.htm";
				}
				else
				{
					htmltext = "30634-03.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npcId)
				{
					case LARA:
					{
						if (cond == 1)
						{
							htmltext = "30063-01.htm";
						}
						else
						{
							if (st.getInt("Lara") == 0)
							{
								htmltext = "30063-03.htm";
							}
							else
							{
								final int[] laraPart = LARA_LISTS[st.getInt("Lara") - 1];
								if ((st.getQuestItemsCount(laraPart[1]) < 30) || (st.getQuestItemsCount(laraPart[2]) < 30))
								{
									htmltext = "30063-05.htm";
								}
								else
								{
									htmltext = "30063-06.htm";
									st.setCond(3);
									st.unset("Lara");
									st.playSound(QuestState.SOUND_MIDDLE);
									st.takeItems(laraPart[0], 1);
									st.takeItems(laraPart[1], -1);
									st.takeItems(laraPart[2], -1);
									st.giveItems(BEGINNER_ARCANA, 2);
								}
							}
						}
						break;
					}
					case GALATEA:
					{
						if (cond == 1)
						{
							htmltext = "30634-09.htm";
						}
						else if ((cond == 2) || (cond == 3))
						{
							htmltext = (!st.hasQuestItems(BEGINNER_ARCANA)) ? "30634-10.htm" : "30634-11.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30634-12.htm";
							st.takeItems(BEGINNER_ARCANA, -1);
							st.takeItems(ALMORS_ARCANA, -1);
							st.takeItems(BASILLIA_ARCANA, -1);
							st.takeItems(BELTHUS_ARCANA, -1);
							st.takeItems(BRYNTHEA_ARCANA, -1);
							st.takeItems(CAMONIELL_ARCANA, -1);
							st.takeItems(CELESTIEL_ARCANA, -1);
							st.takeItems(LARA_LIST_1, -1);
							st.takeItems(LARA_LIST_2, -1);
							st.takeItems(LARA_LIST_3, -1);
							st.takeItems(LARA_LIST_4, -1);
							st.takeItems(LARA_LIST_5, -1);
							st.giveItems(MARK_OF_SUMMONER, 1);
							st.rewardExpAndSp(148409, 30000);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					}
					case ALMORS:
					{
						final int almorsStat = st.getInt("Almors");
						if (almorsStat == 1)
						{
							htmltext = "30635-01.htm";
						}
						else if (almorsStat == 2)
						{
							htmltext = "30635-08.htm";
						}
						else if (almorsStat == 3)
						{
							htmltext = "30635-09.htm";
						}
						else if (almorsStat == 4)
						{
							htmltext = "30635-05.htm";
						}
						else if (almorsStat == 5)
						{
							htmltext = "30635-06.htm";
						}
						else if (almorsStat == 6)
						{
							htmltext = "30635-07.htm";
							st.set("Almors", "7");
							st.takeItems(CRYSTAL_OF_VICTORY_1, -1);
							st.giveItems(ALMORS_ARCANA, 1);
							
							if (st.hasQuestItems(CAMONIELL_ARCANA, BELTHUS_ARCANA, BASILLIA_ARCANA, CELESTIEL_ARCANA, BRYNTHEA_ARCANA))
							{
								st.setCond(4);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (almorsStat == 7)
						{
							htmltext = "30635-10.htm";
						}
						break;
					}
					case CAMONIELL:
					{
						final int camoniellStat = st.getInt("Camoniell");
						if (camoniellStat == 1)
						{
							htmltext = "30636-01.htm";
						}
						else if (camoniellStat == 2)
						{
							htmltext = "30636-08.htm";
						}
						else if (camoniellStat == 3)
						{
							htmltext = "30636-09.htm";
						}
						else if (camoniellStat == 4)
						{
							htmltext = "30636-05.htm";
						}
						else if (camoniellStat == 5)
						{
							htmltext = "30636-06.htm";
						}
						else if (camoniellStat == 6)
						{
							htmltext = "30636-07.htm";
							st.set("Camoniell", "7");
							st.takeItems(CRYSTAL_OF_VICTORY_2, -1);
							st.giveItems(CAMONIELL_ARCANA, 1);
							
							if (st.hasQuestItems(ALMORS_ARCANA, BELTHUS_ARCANA, BASILLIA_ARCANA, CELESTIEL_ARCANA, BRYNTHEA_ARCANA))
							{
								st.setCond(4);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (camoniellStat == 7)
						{
							htmltext = "30636-10.htm";
						}
						break;
					}
					case BELTHUS:
					{
						final int belthusStat = st.getInt("Belthus");
						if (belthusStat == 1)
						{
							htmltext = "30637-01.htm";
						}
						else if (belthusStat == 2)
						{
							htmltext = "30637-08.htm";
						}
						else if (belthusStat == 3)
						{
							htmltext = "30637-09.htm";
						}
						else if (belthusStat == 4)
						{
							htmltext = "30637-05.htm";
						}
						else if (belthusStat == 5)
						{
							htmltext = "30637-06.htm";
						}
						else if (belthusStat == 6)
						{
							htmltext = "30637-07.htm";
							st.set("Belthus", "7");
							st.takeItems(CRYSTAL_OF_VICTORY_3, -1);
							st.giveItems(BELTHUS_ARCANA, 1);
							
							if (st.hasQuestItems(ALMORS_ARCANA, CAMONIELL_ARCANA, BASILLIA_ARCANA, CELESTIEL_ARCANA, BRYNTHEA_ARCANA))
							{
								st.setCond(4);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (belthusStat == 7)
						{
							htmltext = "30637-10.htm";
						}
						break;
					}
					case BASILLA:
					{
						final int basillaStat = st.getInt("Basilla");
						if (basillaStat == 1)
						{
							htmltext = "30638-01.htm";
						}
						else if (basillaStat == 2)
						{
							htmltext = "30638-08.htm";
						}
						else if (basillaStat == 3)
						{
							htmltext = "30638-09.htm";
						}
						else if (basillaStat == 4)
						{
							htmltext = "30638-05.htm";
						}
						else if (basillaStat == 5)
						{
							htmltext = "30638-06.htm";
						}
						else if (basillaStat == 6)
						{
							htmltext = "30638-07.htm";
							st.set("Basilla", "7");
							st.takeItems(CRYSTAL_OF_VICTORY_4, -1);
							st.giveItems(BASILLIA_ARCANA, 1);
							
							if (st.hasQuestItems(ALMORS_ARCANA, CAMONIELL_ARCANA, BELTHUS_ARCANA, CELESTIEL_ARCANA, BRYNTHEA_ARCANA))
							{
								st.setCond(4);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (basillaStat == 7)
						{
							htmltext = "30638-10.htm";
						}
						break;
					}
					case CELESTIEL:
					{
						final int celestielStat = st.getInt("Celestiel");
						if (celestielStat == 1)
						{
							htmltext = "30639-01.htm";
						}
						else if (celestielStat == 2)
						{
							htmltext = "30639-08.htm";
						}
						else if (celestielStat == 3)
						{
							htmltext = "30639-09.htm";
						}
						else if (celestielStat == 4)
						{
							htmltext = "30639-05.htm";
						}
						else if (celestielStat == 5)
						{
							htmltext = "30639-06.htm";
						}
						else if (celestielStat == 6)
						{
							htmltext = "30639-07.htm";
							st.set("Celestiel", "7");
							st.takeItems(CRYSTAL_OF_VICTORY_5, -1);
							st.giveItems(CELESTIEL_ARCANA, 1);
							
							if (st.hasQuestItems(ALMORS_ARCANA, CAMONIELL_ARCANA, BELTHUS_ARCANA, BASILLIA_ARCANA, BRYNTHEA_ARCANA))
							{
								st.setCond(4);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (celestielStat == 7)
						{
							htmltext = "30639-10.htm";
						}
						break;
					}
					case BRYNTHEA:
					{
						final int bryntheaStat = st.getInt("Brynthea");
						if (bryntheaStat == 1)
						{
							htmltext = "30640-01.htm";
						}
						else if (bryntheaStat == 2)
						{
							htmltext = "30640-08.htm";
						}
						else if (bryntheaStat == 3)
						{
							htmltext = "30640-09.htm";
						}
						else if (bryntheaStat == 4)
						{
							htmltext = "30640-05.htm";
						}
						else if (bryntheaStat == 5)
						{
							htmltext = "30640-06.htm";
						}
						else if (bryntheaStat == 6)
						{
							htmltext = "30640-07.htm";
							st.set("Brynthea", "7");
							st.takeItems(CRYSTAL_OF_VICTORY_6, -1);
							st.giveItems(BRYNTHEA_ARCANA, 1);
							
							if (st.hasQuestItems(ALMORS_ARCANA, CAMONIELL_ARCANA, BELTHUS_ARCANA, BASILLIA_ARCANA, CELESTIEL_ARCANA))
							{
								st.setCond(4);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (bryntheaStat == 7)
						{
							htmltext = "30640-10.htm";
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
	public String onDeath(Creature killer, Creature victim, QuestState st)
	{
		if (st == null)
		{
			return null;
		}
		
		if (!(killer instanceof Attackable))
		{
			return super.onDeath(killer, victim, st);
		}
		
		switch (((NpcInstance) killer).getNpcId())
		{
			case PAKO_THE_CAT:
			{
				if (st.getInt("Almors") == 3)
				{
					st.set("Almors", "4");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(CRYSTAL_OF_DEFEAT_1, 1);
				}
				break;
			}
			case UNICORN_RACER:
			{
				if (st.getInt("Camoniell") == 3)
				{
					st.set("Camoniell", "4");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(CRYSTAL_OF_DEFEAT_2, 1);
				}
				break;
			}
			case SHADOW_TUREN:
			{
				if (st.getInt("Belthus") == 3)
				{
					st.set("Belthus", "4");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(CRYSTAL_OF_DEFEAT_3, 1);
				}
				break;
			}
			case MIMI_THE_CAT:
			{
				if (st.getInt("Basilla") == 3)
				{
					st.set("Basilla", "4");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(CRYSTAL_OF_DEFEAT_4, 1);
				}
				break;
			}
			case UNICORN_PHANTASM:
			{
				if (st.getInt("Celestiel") == 3)
				{
					st.set("Celestiel", "4");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(CRYSTAL_OF_DEFEAT_5, 1);
				}
				break;
			}
			case SILHOUETTE_TILFO:
			{
				if (st.getInt("Brynthea") == 3)
				{
					st.set("Brynthea", "4");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(CRYSTAL_OF_DEFEAT_6, 1);
				}
				break;
			}
		}
		
		return null;
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
			case GIANT_FUNGUS:
				if (st.getInt("Lara") == 1)
				{
					st.dropItems(SAC_OF_REDSPORES, 1, 30, 800000);
				}
				break;
			
			case LETO_LIZARDMAN:
			case LETO_LIZARDMAN_ARCHER:
				if (st.getInt("Lara") == 1)
				{
					st.dropItems(LETO_LIZARDMAN_AMULET, 1, 30, 250000);
				}
				break;
			
			case LETO_LIZARDMAN_SOLDIER:
			case LETO_LIZARDMAN_WARRIOR:
				if (st.getInt("Lara") == 1)
				{
					st.dropItems(LETO_LIZARDMAN_AMULET, 1, 30, 500000);
				}
				break;
			
			case LETO_LIZARDMAN_SHAMAN:
			case LETO_LIZARDMAN_OVERLORD:
				if (st.getInt("Lara") == 1)
				{
					st.dropItems(LETO_LIZARDMAN_AMULET, 1, 30, 750000);
				}
				break;
			
			case MANASHEN_GARGOYLE:
				if (st.getInt("Lara") == 2)
				{
					st.dropItems(SHARDS_OF_MANASHEN, 1, 30, 800000);
				}
				break;
			
			case KARUL_BUGBEAR:
				if (st.getInt("Lara") == 2)
				{
					st.dropItems(KARUL_BUGBEAR_TOTEM, 1, 30, 800000);
				}
				break;
			
			case BREKA_ORC:
			case BREKA_ORC_ARCHER:
			case BREKA_ORC_WARRIOR:
				if (st.getInt("Lara") == 3)
				{
					st.dropItems(BREKA_ORC_TOTEM, 1, 30, 250000);
				}
				break;
			
			case BREKA_ORC_SHAMAN:
			case BREKA_ORC_OVERLORD:
				if (st.getInt("Lara") == 3)
				{
					st.dropItems(BREKA_ORC_TOTEM, 1, 30, 500000);
				}
				break;
			
			case FETTERED_SOUL:
				if (st.getInt("Lara") == 3)
				{
					st.dropItems(CRIMSON_BLOODSTONE, 1, 30, 600000);
				}
				break;
			
			case WINDSUS:
				if (st.getInt("Lara") == 4)
				{
					st.dropItems(TUSK_OF_WINDSUS, 1, 30, 700000);
				}
				break;
			
			case TYRANT:
			case TYRANT_KINGPIN:
				if (st.getInt("Lara") == 4)
				{
					st.dropItems(TALONS_OF_TYRANT, 1, 30, 500000);
				}
				break;
			
			case NOBLE_ANT:
			case NOBLE_ANT_LEADER:
				if (st.getInt("Lara") == 5)
				{
					st.dropItems(WINGS_OF_DRONEANT, 1, 30, 600000);
				}
				break;
			
			case WYRM:
				if (st.getInt("Lara") == 5)
				{
					st.dropItems(FANGS_OF_WYRM, 1, 30, 500000);
				}
				break;
			
			case PAKO_THE_CAT:
				if ((st.getInt("Almors") == 3) && _duelsInProgress.containsKey(npcId))
				{
					st.set("Almors", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(CRYSTAL_OF_INPROGRESS_1, -1);
					st.giveItems(CRYSTAL_OF_VICTORY_1, 1);
					npc.broadcastNpcSay("I'm sorry, Lord!");
					st.getPlayer().addNotifyQuestOfDeath(st);
					_duelsInProgress.remove(npcId);
				}
				break;
			
			case UNICORN_RACER:
				if ((st.getInt("Camoniell") == 3) && _duelsInProgress.containsKey(npcId))
				{
					st.set("Camoniell", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(CRYSTAL_OF_INPROGRESS_2, -1);
					st.giveItems(CRYSTAL_OF_VICTORY_2, 1);
					npc.broadcastNpcSay("I LOSE");
					st.getPlayer().addNotifyQuestOfDeath(st);
					_duelsInProgress.remove(npcId);
				}
				break;
			
			case SHADOW_TUREN:
				if ((st.getInt("Belthus") == 3) && _duelsInProgress.containsKey(npcId))
				{
					st.set("Belthus", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(CRYSTAL_OF_INPROGRESS_3, -1);
					st.giveItems(CRYSTAL_OF_VICTORY_3, 1);
					npc.broadcastNpcSay("Ugh! I lost...!");
					st.getPlayer().addNotifyQuestOfDeath(st);
					_duelsInProgress.remove(npcId);
				}
				break;
			
			case MIMI_THE_CAT:
				if ((st.getInt("Basilla") == 3) && _duelsInProgress.containsKey(npcId))
				{
					st.set("Basilla", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(CRYSTAL_OF_INPROGRESS_4, -1);
					st.giveItems(CRYSTAL_OF_VICTORY_4, 1);
					npc.broadcastNpcSay("Lost! Sorry, Lord!");
					st.getPlayer().addNotifyQuestOfDeath(st);
					_duelsInProgress.remove(npcId);
				}
				break;
			
			case UNICORN_PHANTASM:
				if ((st.getInt("Celestiel") == 3) && _duelsInProgress.containsKey(npcId))
				{
					st.set("Celestiel", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(CRYSTAL_OF_INPROGRESS_5, -1);
					st.giveItems(CRYSTAL_OF_VICTORY_5, 1);
					npc.broadcastNpcSay("I LOSE");
					st.getPlayer().addNotifyQuestOfDeath(st);
					_duelsInProgress.remove(npcId);
				}
				break;
			
			case SILHOUETTE_TILFO:
				if ((st.getInt("Brynthea") == 3) && _duelsInProgress.containsKey(npcId))
				{
					st.set("Brynthea", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(CRYSTAL_OF_INPROGRESS_6, -1);
					st.giveItems(CRYSTAL_OF_VICTORY_6, 1);
					npc.broadcastNpcSay("Ugh! Can this be happening?!");
					st.getPlayer().addNotifyQuestOfDeath(st);
					_duelsInProgress.remove(npcId);
				}
				break;
		}
		
		return null;
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		QuestState st = checkPlayerState(attacker, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		st.addNotifyOfDeath(npc);
		
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case PAKO_THE_CAT:
				if ((st.getInt("Almors") == 2) && isPet && (npc.getCurrentHp() == npc.getMaxHp()))
				{
					st.set("Almors", "3");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(CRYSTAL_OF_PROGRESS_1, -1);
					st.giveItems(CRYSTAL_OF_INPROGRESS_1, 1);
					npc.broadcastNpcSay("Whhiisshh!");
					_duelsInProgress.put(npcId, new ProgressDuelMob(attacker, attacker.getPet()));
				}
				else if ((st.getInt("Almors") == 3) && _duelsInProgress.containsKey(npcId))
				{
					final ProgressDuelMob duel = _duelsInProgress.get(npcId);
					// check if the attacker is the same pet as the one that attacked before.
					if (!isPet || (attacker.getPet() != duel.getPet())) // if a foul occured find the player who had the duel in progress and give a foul crystal
					{
						final PlayerInstance foulPlayer = duel.getAttacker();
						if (foulPlayer != null)
						{
							st = foulPlayer.getQuestState(getName());
							if (st != null)
							{
								st.set("Almors", "5");
								st.takeItems(CRYSTAL_OF_PROGRESS_1, -1);
								st.takeItems(CRYSTAL_OF_INPROGRESS_1, -1);
								st.giveItems(CRYSTAL_OF_FOUL_1, 1);
								st.getPlayer().addNotifyQuestOfDeath(st);
								npc.broadcastNpcSay("Rule violation!");
								npc.doDie(npc);
							}
						}
					}
				}
				break;
			
			case UNICORN_RACER:
				if ((st.getInt("Camoniell") == 2) && isPet && (npc.getCurrentHp() == npc.getMaxHp()))
				{
					st.set("Camoniell", "3");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(CRYSTAL_OF_PROGRESS_2, -1);
					st.giveItems(CRYSTAL_OF_INPROGRESS_2, 1);
					npc.broadcastNpcSay("START DUEL");
					_duelsInProgress.put(npcId, new ProgressDuelMob(attacker, attacker.getPet()));
				}
				else if ((st.getInt("Camoniell") == 3) && _duelsInProgress.containsKey(npcId))
				{
					final ProgressDuelMob duel = _duelsInProgress.get(npcId);
					if (!isPet || (attacker.getPet() != duel.getPet()))
					{
						final PlayerInstance foulPlayer = duel.getAttacker();
						if (foulPlayer != null)
						{
							st = foulPlayer.getQuestState(getName());
							if (st != null)
							{
								st.set("Camoniell", "5");
								st.takeItems(CRYSTAL_OF_PROGRESS_2, -1);
								st.takeItems(CRYSTAL_OF_INPROGRESS_2, -1);
								st.giveItems(CRYSTAL_OF_FOUL_2, 1);
								st.getPlayer().addNotifyQuestOfDeath(st);
								npc.broadcastNpcSay("RULE VIOLATION");
								npc.doDie(npc);
							}
						}
					}
				}
				break;
			
			case SHADOW_TUREN:
				if ((st.getInt("Belthus") == 2) && isPet && (npc.getCurrentHp() == npc.getMaxHp()))
				{
					st.set("Belthus", "3");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(CRYSTAL_OF_PROGRESS_3, -1);
					st.giveItems(CRYSTAL_OF_INPROGRESS_3, 1);
					npc.broadcastNpcSay("So shall we start?!");
					_duelsInProgress.put(npcId, new ProgressDuelMob(attacker, attacker.getPet()));
				}
				else if ((st.getInt("Belthus") == 3) && _duelsInProgress.containsKey(npcId))
				{
					final ProgressDuelMob duel = _duelsInProgress.get(npcId);
					if (!isPet || (attacker.getPet() != duel.getPet()))
					{
						final PlayerInstance foulPlayer = duel.getAttacker();
						if (foulPlayer != null)
						{
							st = foulPlayer.getQuestState(getName());
							if (st != null)
							{
								st.set("Belthus", "5");
								st.takeItems(CRYSTAL_OF_PROGRESS_3, -1);
								st.takeItems(CRYSTAL_OF_INPROGRESS_3, -1);
								st.giveItems(CRYSTAL_OF_FOUL_3, 1);
								st.getPlayer().addNotifyQuestOfDeath(st);
								npc.broadcastNpcSay("Rule violation!!!");
								npc.doDie(npc);
							}
						}
					}
				}
				break;
			
			case MIMI_THE_CAT:
				if ((st.getInt("Basilla") == 2) && isPet && (npc.getCurrentHp() == npc.getMaxHp()))
				{
					st.set("Basilla", "3");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(CRYSTAL_OF_PROGRESS_4, -1);
					st.giveItems(CRYSTAL_OF_INPROGRESS_4, 1);
					npc.broadcastNpcSay("Whish! Fight!");
					_duelsInProgress.put(npcId, new ProgressDuelMob(attacker, attacker.getPet()));
				}
				else if ((st.getInt("Basilla") == 3) && _duelsInProgress.containsKey(npcId))
				{
					final ProgressDuelMob duel = _duelsInProgress.get(npcId);
					if (!isPet || (attacker.getPet() != duel.getPet()))
					{
						final PlayerInstance foulPlayer = duel.getAttacker();
						if (foulPlayer != null)
						{
							st = foulPlayer.getQuestState(getName());
							if (st != null)
							{
								st.set("Basilla", "5");
								st.takeItems(CRYSTAL_OF_PROGRESS_4, -1);
								st.takeItems(CRYSTAL_OF_INPROGRESS_4, -1);
								st.giveItems(CRYSTAL_OF_FOUL_4, 1);
								st.getPlayer().addNotifyQuestOfDeath(st);
								npc.broadcastNpcSay("Rule violation!");
								npc.doDie(npc);
							}
						}
					}
				}
				break;
			
			case UNICORN_PHANTASM:
				if ((st.getInt("Celestiel") == 2) && isPet && (npc.getCurrentHp() == npc.getMaxHp()))
				{
					st.set("Celestiel", "3");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(CRYSTAL_OF_PROGRESS_5, -1);
					st.giveItems(CRYSTAL_OF_INPROGRESS_5, 1);
					npc.broadcastNpcSay("START DUEL");
					_duelsInProgress.put(npcId, new ProgressDuelMob(attacker, attacker.getPet()));
				}
				else if ((st.getInt("Celestiel") == 3) && _duelsInProgress.containsKey(npcId))
				{
					final ProgressDuelMob duel = _duelsInProgress.get(npcId);
					if (!isPet || (attacker.getPet() != duel.getPet()))
					{
						final PlayerInstance foulPlayer = duel.getAttacker();
						if (foulPlayer != null)
						{
							st = foulPlayer.getQuestState(getName());
							if (st != null)
							{
								st.set("Celestiel", "5");
								st.takeItems(CRYSTAL_OF_PROGRESS_5, -1);
								st.takeItems(CRYSTAL_OF_INPROGRESS_5, -1);
								st.giveItems(CRYSTAL_OF_FOUL_5, 1);
								st.getPlayer().addNotifyQuestOfDeath(st);
								npc.broadcastNpcSay("RULE VIOLATION");
								npc.doDie(npc);
							}
						}
					}
				}
				break;
			
			case SILHOUETTE_TILFO:
				if ((st.getInt("Brynthea") == 2) && isPet && (npc.getCurrentHp() == npc.getMaxHp()))
				{
					st.set("Brynthea", "3");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(CRYSTAL_OF_PROGRESS_6, -1);
					st.giveItems(CRYSTAL_OF_INPROGRESS_6, 1);
					npc.broadcastNpcSay("I'll walk all over you!");
					_duelsInProgress.put(npcId, new ProgressDuelMob(attacker, attacker.getPet()));
				}
				else if ((st.getInt("Brynthea") == 3) && _duelsInProgress.containsKey(npcId))
				{
					final ProgressDuelMob duel = _duelsInProgress.get(npcId);
					if (!isPet || (attacker.getPet() != duel.getPet()))
					{
						final PlayerInstance foulPlayer = duel.getAttacker();
						if (foulPlayer != null)
						{
							st = foulPlayer.getQuestState(getName());
							if (st != null)
							{
								st.set("Brynthea", "5");
								st.takeItems(CRYSTAL_OF_PROGRESS_6, -1);
								st.takeItems(CRYSTAL_OF_INPROGRESS_6, -1);
								st.giveItems(CRYSTAL_OF_FOUL_6, 1);
								st.getPlayer().addNotifyQuestOfDeath(st);
								npc.broadcastNpcSay("Rule violation!!!");
								npc.doDie(npc);
							}
						}
					}
				}
				break;
		}
		
		return null;
	}
	
	private final class ProgressDuelMob
	{
		private final PlayerInstance _attacker;
		private final Summon _pet;
		
		public ProgressDuelMob(PlayerInstance attacker, Summon pet)
		{
			_attacker = attacker;
			_pet = pet;
		}
		
		public PlayerInstance getAttacker()
		{
			return _attacker;
		}
		
		public Summon getPet()
		{
			return _pet;
		}
	}
}