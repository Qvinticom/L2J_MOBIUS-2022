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
package quests.Q348_AnArrogantSearch;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q348_AnArrogantSearch extends Quest
{
	// NPCs
	private static final int HANELLIN = 30864;
	private static final int CLAUDIA_ATHEBALDT = 31001;
	private static final int MARTIEN = 30645;
	private static final int HARNE = 30144;
	private static final int ARK_GUARDIAN_CORPSE = 30980;
	private static final int HOLY_ARK_OF_SECRECY_1 = 30977;
	private static final int HOLY_ARK_OF_SECRECY_2 = 30978;
	private static final int HOLY_ARK_OF_SECRECY_3 = 30979;
	private static final int GUSTAV_ATHEBALDT = 30760;
	private static final int HARDIN = 30832;
	private static final int IASON_HEINE = 30969;
	// Monsters
	private static final int LESSER_GIANT_MAGE = 20657;
	private static final int LESSER_GIANT_ELDER = 20658;
	private static final int PLATINUM_TRIBE_SHAMAN = 20828;
	private static final int PLATINUM_TRIBE_OVERLORD = 20829;
	private static final int GUARDIAN_ANGEL = 20859;
	private static final int SEAL_ANGEL = 20860;
	// Quest Monsters
	private static final int ANGEL_KILLER = 27184;
	private static final int ARK_GUARDIAN_ELBEROTH = 27182;
	private static final int ARK_GUARDIAN_SHADOW_FANG = 27183;
	// Items
	private static final int TITAN_POWERSTONE = 4287;
	private static final int HANELLIN_FIRST_LETTER = 4288;
	private static final int HANELLIN_SECOND_LETTER = 4289;
	private static final int HANELLIN_THIRD_LETTER = 4290;
	private static final int FIRST_KEY_OF_ARK = 4291;
	private static final int SECOND_KEY_OF_ARK = 4292;
	private static final int THIRD_KEY_OF_ARK = 4293;
	private static final int BOOK_OF_SAINT = 4397;
	private static final int BLOOD_OF_SAINT = 4398;
	private static final int BOUGH_OF_SAINT = 4399;
	private static final int WHITE_FABRIC_TRIBE = 4294;
	private static final int WHITE_FABRIC_ANGELS = 5232;
	private static final int BLOODED_FABRIC = 4295;
	private static final int ANTIDOTE = 1831;
	private static final int HEALING_POTION = 1061;
	// NPCs instances, in order to avoid infinite instances creation speaking to chests.
	private NpcInstance _elberoth;
	private NpcInstance _shadowFang;
	private NpcInstance _angelKiller;
	
	public Q348_AnArrogantSearch()
	{
		super(348, "An Arrogant Search");
		registerQuestItems(TITAN_POWERSTONE, HANELLIN_FIRST_LETTER, HANELLIN_SECOND_LETTER, HANELLIN_THIRD_LETTER, FIRST_KEY_OF_ARK, SECOND_KEY_OF_ARK, THIRD_KEY_OF_ARK, BOOK_OF_SAINT, BLOOD_OF_SAINT, BOUGH_OF_SAINT, WHITE_FABRIC_TRIBE, WHITE_FABRIC_ANGELS);
		addStartNpc(HANELLIN);
		addTalkId(HANELLIN, CLAUDIA_ATHEBALDT, MARTIEN, HARNE, HOLY_ARK_OF_SECRECY_1, HOLY_ARK_OF_SECRECY_2, HOLY_ARK_OF_SECRECY_3, ARK_GUARDIAN_CORPSE, GUSTAV_ATHEBALDT, HARDIN, IASON_HEINE);
		addSpawnId(ARK_GUARDIAN_ELBEROTH, ARK_GUARDIAN_SHADOW_FANG, ANGEL_KILLER);
		addAttackId(ARK_GUARDIAN_ELBEROTH, ARK_GUARDIAN_SHADOW_FANG, ANGEL_KILLER, PLATINUM_TRIBE_SHAMAN, PLATINUM_TRIBE_OVERLORD);
		addKillId(LESSER_GIANT_MAGE, LESSER_GIANT_ELDER, ARK_GUARDIAN_ELBEROTH, ARK_GUARDIAN_SHADOW_FANG, ANGEL_KILLER, PLATINUM_TRIBE_SHAMAN, PLATINUM_TRIBE_OVERLORD, GUARDIAN_ANGEL, SEAL_ANGEL);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30864-05.htm":
			{
				st.startQuest();
				st.setCond(2);
				st.set("points", "0");
				break;
			}
			case "30864-09.htm":
			{
				st.setCond(4);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(TITAN_POWERSTONE, 1);
				break;
			}
			case "30864-17.htm":
			{
				st.setCond(5);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(HANELLIN_FIRST_LETTER, 1);
				st.giveItems(HANELLIN_SECOND_LETTER, 1);
				st.giveItems(HANELLIN_THIRD_LETTER, 1);
				break;
			}
			case "30864-36.htm":
			{
				st.setCond(24);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.rewardItems(57, Rnd.get(1, 2) * 12000);
				break;
			}
			case "30864-37.htm":
			{
				st.setCond(25);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30864-51.htm":
			{
				st.setCond(26);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(WHITE_FABRIC_ANGELS, (st.hasQuestItems(BLOODED_FABRIC)) ? 9 : 10);
				break;
			}
			case "30864-58.htm":
			{
				st.setCond(27);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30864-57.htm":
			{
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
				break;
			}
			case "30864-56.htm":
			{
				st.setCond(29);
				st.set("gustav", "0"); // st.unset doesn't work.
				st.set("hardin", "0");
				st.set("iason", "0");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(WHITE_FABRIC_ANGELS, 10);
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
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (st.hasQuestItems(BLOODED_FABRIC))
				{
					htmltext = "30864-00.htm";
				}
				else if (player.getLevel() < 60)
				{
					htmltext = "30864-01.htm";
				}
				else
				{
					htmltext = "30864-02.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case HANELLIN:
					{
						if (cond == 1)
						{
							htmltext = "30864-02.htm";
						}
						else if (cond == 2)
						{
							htmltext = (!st.hasQuestItems(TITAN_POWERSTONE)) ? "30864-06.htm" : "30864-07.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30864-09.htm";
						}
						else if ((cond > 4) && (cond < 21))
						{
							htmltext = (st.hasAtLeastOneQuestItem(BOOK_OF_SAINT, BLOOD_OF_SAINT, BOUGH_OF_SAINT)) ? "30864-28.htm" : "30864-24.htm";
						}
						else if (cond == 21)
						{
							htmltext = "30864-29.htm";
							st.setCond(22);
							st.takeItems(BOOK_OF_SAINT, 1);
							st.takeItems(BLOOD_OF_SAINT, 1);
							st.takeItems(BOUGH_OF_SAINT, 1);
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 22)
						{
							if (st.hasQuestItems(WHITE_FABRIC_TRIBE))
							{
								htmltext = "30864-31.htm";
							}
							else if ((st.getQuestItemsCount(ANTIDOTE) < 5) || !st.hasQuestItems(HEALING_POTION))
							{
								htmltext = "30864-30.htm";
							}
							else
							{
								htmltext = "30864-31.htm";
								st.takeItems(ANTIDOTE, 5);
								st.takeItems(HEALING_POTION, 1);
								st.giveItems(WHITE_FABRIC_TRIBE, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (cond == 24)
						{
							htmltext = "30864-38.htm";
						}
						else if (cond == 25)
						{
							if (st.hasQuestItems(WHITE_FABRIC_TRIBE))
							{
								htmltext = "30864-39.htm";
							}
							else if (st.hasQuestItems(BLOODED_FABRIC))
							{
								htmltext = "30864-49.htm";
								// Use the only fabric on Baium, drop the quest.
							}
							else
							{
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(true);
							}
						}
						else if (cond == 26)
						{
							final int count = st.getQuestItemsCount(BLOODED_FABRIC);
							if ((count + st.getQuestItemsCount(WHITE_FABRIC_ANGELS)) < 10)
							{
								htmltext = "30864-54.htm";
								st.takeItems(BLOODED_FABRIC, -1);
								st.rewardItems(57, (1000 * count) + 4000);
								st.exitQuest(true);
							}
							else if (count < 10)
							{
								htmltext = "30864-52.htm";
							}
							else if (count >= 10)
							{
								htmltext = "30864-53.htm";
							}
						}
						else if (cond == 27)
						{
							if ((st.getInt("gustav") + st.getInt("hardin") + st.getInt("iason")) == 3)
							{
								htmltext = "30864-60.htm";
								st.setCond(28);
								st.rewardItems(57, 49000);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else if (st.hasQuestItems(BLOODED_FABRIC) && (st.getInt("usedonbaium") != 1))
							{
								htmltext = "30864-59.htm";
							}
							else
							{
								htmltext = "30864-61.htm";
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(true);
							}
						}
						else if (cond == 28)
						{
							htmltext = "30864-55.htm";
						}
						else if (cond == 29)
						{
							final int count = st.getQuestItemsCount(BLOODED_FABRIC);
							if ((count + st.getQuestItemsCount(WHITE_FABRIC_ANGELS)) < 10)
							{
								htmltext = "30864-54.htm";
								st.takeItems(BLOODED_FABRIC, -1);
								st.rewardItems(57, 5000 * count);
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(true);
							}
							else if (count < 10)
							{
								htmltext = "30864-52.htm";
							}
							else if (count >= 10)
							{
								htmltext = "30864-53.htm";
							}
						}
						break;
					}
					case GUSTAV_ATHEBALDT:
					{
						if (cond == 27)
						{
							if ((st.getQuestItemsCount(BLOODED_FABRIC) >= 3) && (st.getInt("gustav") == 0))
							{
								st.set("gustav", "1");
								htmltext = "30760-01.htm";
								st.takeItems(BLOODED_FABRIC, 3);
							}
							else if (st.getInt("gustav") == 1)
							{
								htmltext = "30760-02.htm";
							}
							else
							{
								htmltext = "30760-03.htm";
								st.set("usedonbaium", "1");
							}
						}
						break;
					}
					case HARDIN:
					{
						if (cond == 27)
						{
							if (st.hasQuestItems(BLOODED_FABRIC) && (st.getInt("hardin") == 0))
							{
								st.set("hardin", "1");
								htmltext = "30832-01.htm";
								st.takeItems(BLOODED_FABRIC, 1);
							}
							else if (st.getInt("hardin") == 1)
							{
								htmltext = "30832-02.htm";
							}
							else
							{
								htmltext = "30832-03.htm";
								st.set("usedonbaium", "1");
							}
						}
						break;
					}
					case IASON_HEINE:
					{
						if (cond == 27)
						{
							if ((st.getQuestItemsCount(BLOODED_FABRIC) >= 6) && (st.getInt("iason") == 0))
							{
								st.set("iason", "1");
								htmltext = "30969-01.htm";
								st.takeItems(BLOODED_FABRIC, 6);
							}
							else if (st.getInt("iason") == 1)
							{
								htmltext = "30969-02.htm";
							}
							else
							{
								htmltext = "30969-03.htm";
								st.set("usedonbaium", "1");
							}
						}
						break;
					}
					case HARNE:
					{
						if ((cond >= 5) && (cond <= 22))
						{
							if (!st.hasQuestItems(BLOOD_OF_SAINT))
							{
								if (st.hasQuestItems(HANELLIN_FIRST_LETTER))
								{
									htmltext = "30144-01.htm";
									st.setCond(17);
									st.playSound(QuestState.SOUND_MIDDLE);
									st.takeItems(HANELLIN_FIRST_LETTER, 1);
									st.addRadar(-418, 44174, -3568);
								}
								else if (!st.hasQuestItems(FIRST_KEY_OF_ARK))
								{
									htmltext = "30144-03.htm";
									st.addRadar(-418, 44174, -3568);
								}
								else
								{
									htmltext = "30144-04.htm";
								}
							}
							else
							{
								htmltext = "30144-05.htm";
							}
						}
						break;
					}
					case CLAUDIA_ATHEBALDT:
					{
						if ((cond >= 5) && (cond <= 22))
						{
							if (!st.hasQuestItems(BOOK_OF_SAINT))
							{
								if (st.hasQuestItems(HANELLIN_SECOND_LETTER))
								{
									htmltext = "31001-01.htm";
									st.setCond(9);
									st.playSound(QuestState.SOUND_MIDDLE);
									st.takeItems(HANELLIN_SECOND_LETTER, 1);
									st.addRadar(181472, 7158, -2725);
								}
								else if (!st.hasQuestItems(SECOND_KEY_OF_ARK))
								{
									htmltext = "31001-03.htm";
									st.addRadar(181472, 7158, -2725);
								}
								else
								{
									htmltext = "31001-04.htm";
								}
							}
							else
							{
								htmltext = "31001-05.htm";
							}
						}
						break;
					}
					case MARTIEN:
					{
						if ((cond >= 5) && (cond <= 22))
						{
							if (!st.hasQuestItems(BOUGH_OF_SAINT))
							{
								if (st.hasQuestItems(HANELLIN_THIRD_LETTER))
								{
									htmltext = "30645-01.htm";
									st.setCond(13);
									st.playSound(QuestState.SOUND_MIDDLE);
									st.takeItems(HANELLIN_THIRD_LETTER, 1);
									st.addRadar(50693, 158674, 376);
								}
								else if (!st.hasQuestItems(THIRD_KEY_OF_ARK))
								{
									htmltext = "30645-03.htm";
									st.addRadar(50693, 158674, 376);
								}
								else
								{
									htmltext = "30645-04.htm";
								}
							}
							else
							{
								htmltext = "30645-05.htm";
							}
						}
						break;
					}
					case ARK_GUARDIAN_CORPSE:
					{
						if (!st.hasQuestItems(HANELLIN_FIRST_LETTER) && (cond >= 5) && (cond <= 22))
						{
							if (!st.hasQuestItems(FIRST_KEY_OF_ARK) && !st.hasQuestItems(BLOOD_OF_SAINT))
							{
								if (st.getInt("angelkiller") == 0)
								{
									htmltext = "30980-01.htm";
									if (_angelKiller == null)
									{
										_angelKiller = addSpawn(ANGEL_KILLER, npc, false, 0);
									}
									
									if (!st.isCond(18))
									{
										st.setCond(18);
										st.playSound(QuestState.SOUND_MIDDLE);
									}
								}
								else
								{
									htmltext = "30980-02.htm";
									st.giveItems(FIRST_KEY_OF_ARK, 1);
									st.playSound(QuestState.SOUND_ITEMGET);
									
									st.unset("angelkiller");
								}
							}
							else
							{
								htmltext = "30980-03.htm";
							}
						}
						break;
					}
					case HOLY_ARK_OF_SECRECY_1:
					{
						if (!st.hasQuestItems(HANELLIN_FIRST_LETTER) && (cond >= 5) && (cond <= 22))
						{
							if (!st.hasQuestItems(BLOOD_OF_SAINT))
							{
								if (st.hasQuestItems(FIRST_KEY_OF_ARK))
								{
									htmltext = "30977-02.htm";
									st.setCond(20);
									st.playSound(QuestState.SOUND_MIDDLE);
									
									st.takeItems(FIRST_KEY_OF_ARK, 1);
									st.giveItems(BLOOD_OF_SAINT, 1);
									
									if (st.hasQuestItems(BOOK_OF_SAINT, BOUGH_OF_SAINT))
									{
										st.setCond(21);
									}
								}
								else
								{
									htmltext = "30977-04.htm";
								}
							}
							else
							{
								htmltext = "30977-03.htm";
							}
						}
						break;
					}
					case HOLY_ARK_OF_SECRECY_2:
					{
						if (!st.hasQuestItems(HANELLIN_SECOND_LETTER) && (cond >= 5) && (cond <= 22))
						{
							if (!st.hasQuestItems(BOOK_OF_SAINT))
							{
								if (!st.hasQuestItems(SECOND_KEY_OF_ARK))
								{
									htmltext = "30978-01.htm";
									if (_elberoth == null)
									{
										_elberoth = addSpawn(ARK_GUARDIAN_ELBEROTH, npc, false, 0);
									}
								}
								else
								{
									htmltext = "30978-02.htm";
									st.setCond(12);
									st.playSound(QuestState.SOUND_MIDDLE);
									
									st.takeItems(SECOND_KEY_OF_ARK, 1);
									st.giveItems(BOOK_OF_SAINT, 1);
									
									if (st.hasQuestItems(BLOOD_OF_SAINT, BOUGH_OF_SAINT))
									{
										st.setCond(21);
									}
								}
							}
							else
							{
								htmltext = "30978-03.htm";
							}
						}
						break;
					}
					case HOLY_ARK_OF_SECRECY_3:
					{
						if (!st.hasQuestItems(HANELLIN_THIRD_LETTER) && (cond >= 5) && (cond <= 22))
						{
							if (!st.hasQuestItems(BOUGH_OF_SAINT))
							{
								if (!st.hasQuestItems(THIRD_KEY_OF_ARK))
								{
									htmltext = "30979-01.htm";
									if (_shadowFang == null)
									{
										_shadowFang = addSpawn(ARK_GUARDIAN_SHADOW_FANG, npc, false, 0);
									}
								}
								else
								{
									htmltext = "30979-02.htm";
									st.setCond(16);
									st.playSound(QuestState.SOUND_MIDDLE);
									
									st.takeItems(THIRD_KEY_OF_ARK, 1);
									st.giveItems(BOUGH_OF_SAINT, 1);
									
									if (st.hasQuestItems(BLOOD_OF_SAINT, BOOK_OF_SAINT))
									{
										st.setCond(21);
									}
								}
							}
							else
							{
								htmltext = "30979-03.htm";
							}
						}
						break;
					}
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onSpawn(NpcInstance npc)
	{
		switch (npc.getNpcId())
		{
			case ARK_GUARDIAN_ELBEROTH:
			{
				npc.broadcastNpcSay("This does not belong to you. Take your hands out!");
				break;
			}
			case ARK_GUARDIAN_SHADOW_FANG:
			{
				npc.broadcastNpcSay("I don't believe it! Grrr!");
				break;
			}
			case ANGEL_KILLER:
			{
				npc.broadcastNpcSay("I have the key, do you wish to steal it?");
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		final QuestState st = checkPlayerState(attacker, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case ARK_GUARDIAN_ELBEROTH:
			{
				if (npc.isScriptValue(0))
				{
					npc.broadcastNpcSay("...I feel very sorry, but I have taken your life.");
					npc.setScriptValue(1);
				}
				break;
			}
			case ARK_GUARDIAN_SHADOW_FANG:
			{
				if (npc.isScriptValue(0))
				{
					npc.broadcastNpcSay("I will cover this mountain with your blood!");
					npc.setScriptValue(1);
				}
				break;
			}
			case ANGEL_KILLER:
			{
				if (npc.isScriptValue(0))
				{
					npc.broadcastNpcSay("Haha.. Really amusing! As for the key, search the corpse!");
					npc.setScriptValue(1);
				}
				
				if ((npc.getCurrentHp() / npc.getMaxHp()) < 0.50)
				{
					npc.abortAttack();
					npc.broadcastNpcSay("Can't get rid of you... Did you get the key from the corpse?");
					npc.decayMe();
					
					st.setCond(19);
					st.set("angelkiller", "1");
					st.playSound(QuestState.SOUND_MIDDLE);
					
					_angelKiller = null;
				}
				break;
			}
			case PLATINUM_TRIBE_OVERLORD:
			case PLATINUM_TRIBE_SHAMAN:
			{
				final int cond = st.getCond();
				if (((cond == 24) || (cond == 25)) && st.hasQuestItems(WHITE_FABRIC_TRIBE))
				{
					final int points = st.getInt("points") + ((npc.getNpcId() == PLATINUM_TRIBE_SHAMAN) ? 60 : 70);
					if (points > ((cond == 24) ? 80000 : 100000))
					{
						st.set("points", Integer.toString(0));
						
						st.takeItems(WHITE_FABRIC_TRIBE, 1);
						st.giveItems(BLOODED_FABRIC, 1);
						
						if (cond != 24)
						{
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						else
						{
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
					}
					else
					{
						st.set("points", Integer.toString(points));
					}
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
		
		final int cond = st.getCond();
		switch (npc.getNpcId())
		{
			case LESSER_GIANT_ELDER:
			case LESSER_GIANT_MAGE:
			{
				if (cond == 2)
				{
					st.dropItems(TITAN_POWERSTONE, 1, 1, 100000);
				}
				break;
			}
			case ARK_GUARDIAN_ELBEROTH:
			{
				if ((cond >= 5) && (cond <= 22) && !st.hasQuestItems(SECOND_KEY_OF_ARK))
				{
					st.setCond(11);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(SECOND_KEY_OF_ARK, 1);
					npc.broadcastNpcSay("Oh, dull-witted.. God, they...");
				}
				_elberoth = null;
				break;
			}
			case ARK_GUARDIAN_SHADOW_FANG:
			{
				if ((cond >= 5) && (cond <= 22) && !st.hasQuestItems(THIRD_KEY_OF_ARK))
				{
					st.setCond(15);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(THIRD_KEY_OF_ARK, 1);
					npc.broadcastNpcSay("You do not know.. Seven seals are.. coughs");
				}
				_shadowFang = null;
				break;
			}
			case PLATINUM_TRIBE_OVERLORD:
			case PLATINUM_TRIBE_SHAMAN:
			{
				if (((cond == 24) || (cond == 25)) && st.hasQuestItems(WHITE_FABRIC_TRIBE))
				{
					final int points = st.getInt("points") + ((npc.getNpcId() == PLATINUM_TRIBE_SHAMAN) ? 600 : 700);
					if (points > ((cond == 24) ? 80000 : 100000))
					{
						st.set("points", Integer.toString(0));
						
						st.takeItems(WHITE_FABRIC_TRIBE, 1);
						st.giveItems(BLOODED_FABRIC, 1);
						
						if (cond != 24)
						{
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						else
						{
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
					}
					else
					{
						st.set("points", Integer.toString(points));
					}
				}
				break;
			}
			case SEAL_ANGEL:
			case GUARDIAN_ANGEL:
			{
				if (((cond == 26) || (cond == 29)) && (Rnd.get(4) < 1) && st.hasQuestItems(WHITE_FABRIC_ANGELS))
				{
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(WHITE_FABRIC_ANGELS, 1);
					st.giveItems(BLOODED_FABRIC, 1);
				}
				break;
			}
			case ANGEL_KILLER:
			{
				_angelKiller = null;
				break;
			}
		}
		
		return null;
	}
}