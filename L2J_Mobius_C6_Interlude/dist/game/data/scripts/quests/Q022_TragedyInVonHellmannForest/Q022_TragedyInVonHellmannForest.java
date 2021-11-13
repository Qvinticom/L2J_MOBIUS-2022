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
package quests.Q022_TragedyInVonHellmannForest;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.QuestTimer;
import org.l2jmobius.gameserver.util.Util;

import quests.Q021_HiddenTruth.Q021_HiddenTruth;

public class Q022_TragedyInVonHellmannForest extends Quest
{
	// NPCs
	private static final int INNOCENTIN = 31328;
	private static final int TIFAREN = 31334;
	private static final int WELL = 31527;
	private static final int GHOST_OF_PRIEST = 31528;
	private static final int GHOST_OF_ADVENTURER = 31529;
	// Monsters
	private static final int[] MOBS =
	{
		21553, // Trampled Man
		21554, // Trampled Man
		21555, // Slaughter Executioner
		21556, // Slaughter Executioner
		21561, // Sacrificed Man
	};
	private static final int SOUL_OF_WELL = 27217;
	// Items
	private static final int CROSS_OF_EINHASAD = 7141;
	private static final int LOST_SKULL_OF_ELF = 7142;
	private static final int LETTER_OF_INNOCENTIN = 7143;
	private static final int JEWEL_OF_ADVENTURER_1 = 7144;
	private static final int JEWEL_OF_ADVENTURER_2 = 7145;
	private static final int SEALED_REPORT_BOX = 7146;
	private static final int REPORT_BOX = 7147;
	// Misc
	private static final int MIN_LEVEL = 63;
	private static final Location PRIEST_LOC = new Location(38354, -49777, -1128);
	private static final Location SOUL_WELL_LOC = new Location(34706, -54590, -2054);
	private static int _tifarenOwner = 0;
	private static NpcInstance _soulWellNpc = null;
	
	public Q022_TragedyInVonHellmannForest()
	{
		super(22, "Tragedy in von Hellmann Forest");
		addKillId(MOBS);
		addKillId(SOUL_OF_WELL);
		addAttackId(SOUL_OF_WELL);
		addStartNpc(TIFAREN);
		addTalkId(INNOCENTIN, TIFAREN, WELL, GHOST_OF_PRIEST, GHOST_OF_ADVENTURER);
		registerQuestItems(LOST_SKULL_OF_ELF, CROSS_OF_EINHASAD, REPORT_BOX, JEWEL_OF_ADVENTURER_1, JEWEL_OF_ADVENTURER_2, SEALED_REPORT_BOX);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final QuestState qs = player.getQuestState(getName());
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "31529-02.htm":
			case "31529-04.htm":
			case "31529-05.htm":
			case "31529-06.htm":
			case "31529-07.htm":
			case "31529-09.htm":
			case "31529-13.htm":
			case "31529-13a.htm":
			case "31528-02.htm":
			case "31528-05.htm":
			case "31528-06.htm":
			case "31528-07.htm":
			case "31328-13.htm":
			case "31328-06.htm":
			case "31328-05.htm":
			case "31328-02.htm":
			case "31328-07.htm":
			case "31328-08.htm":
			case "31328-14.htm":
			case "31328-15.htm":
			case "31328-16.htm":
			case "31328-17.htm":
			case "31328-18.htm":
			case "31334-12.htm":
			{
				htmltext = event;
				break;
			}
			case "31334-02.htm":
			{
				if (qs.isCreated())
				{
					final QuestState qs2 = player.getQuestState(Q021_HiddenTruth.class.getSimpleName());
					if ((player.getLevel() >= MIN_LEVEL) && (qs2 != null) && qs2.isCompleted())
					{
						htmltext = event;
					}
					else
					{
						htmltext = "31334-03.htm";
					}
				}
				break;
			}
			case "31334-04.htm":
			{
				if (qs.isCreated())
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "31334-07.htm":
			{
				if (!qs.hasQuestItems(CROSS_OF_EINHASAD))
				{
					qs.setCond(2);
					htmltext = event;
				}
				else
				{
					htmltext = "31334-06.htm";
					qs.setCond(3);
				}
				break;
			}
			case "31334-08.htm":
			{
				if (qs.isCond(3))
				{
					qs.setCond(4);
					qs.playSound(QuestState.SOUND_MIDDLE);
					htmltext = event;
				}
				break;
			}
			case "31334-13.htm":
			{
				final int cond = qs.getCond();
				if (((5 <= cond) && (cond <= 7)) && qs.hasQuestItems(CROSS_OF_EINHASAD))
				{
					if (_tifarenOwner == 0)
					{
						_tifarenOwner = player.getObjectId();
						final NpcInstance ghost2 = addSpawn(GHOST_OF_PRIEST, PRIEST_LOC, true, 0);
						ghost2.setScriptValue(player.getObjectId());
						startQuestTimer("DESPAWN_GHOST2", 1000 * 120, ghost2, player);
						ghost2.broadcastNpcSay("Did you call me, " + player.getName() + "?");
						if (((cond == 5) || (cond == 6)) && qs.hasQuestItems(LOST_SKULL_OF_ELF))
						{
							qs.takeItems(LOST_SKULL_OF_ELF, -1);
							qs.setCond(7);
							qs.playSound(QuestState.SOUND_MIDDLE);
						}
						htmltext = event;
					}
					else
					{
						qs.setCond(6);
						htmltext = "31334-14.htm";
					}
				}
				break;
			}
			case "31528-04.htm":
			{
				if (npc.getScriptValue() == player.getObjectId())
				{
					qs.playSound(QuestState.SOUND_ACCEPT);
					htmltext = event;
				}
				break;
			}
			case "31528-08.htm":
			{
				final QuestTimer qt = getQuestTimer("DESPAWN_GHOST2", npc, player);
				if ((qt != null) && (npc.getScriptValue() == player.getObjectId()))
				{
					qt.cancel();
					npc.setScriptValue(0);
					startQuestTimer("DESPAWN_GHOST2", 1000 * 3, npc, player);
					qs.setCond(8);
					qs.playSound(QuestState.SOUND_MIDDLE);
					htmltext = event;
				}
				break;
			}
			case "DESPAWN_GHOST2":
			{
				_tifarenOwner = 0;
				if (npc.getScriptValue() != 0)
				{
					npc.broadcastNpcSay("I_M_CONFUSED_MAYBE_IT_S_TIME_TO_GO_BACK");
				}
				npc.deleteMe();
				break;
			}
			case "31328-03.htm":
			{
				if (qs.isCond(8))
				{
					
					qs.takeItems(CROSS_OF_EINHASAD, -1);
					htmltext = event;
				}
				break;
			}
			case "31328-09.htm":
			{
				if (qs.isCond(8))
				{
					qs.giveItems(LETTER_OF_INNOCENTIN, 1);
					qs.setCond(9);
					qs.playSound(QuestState.SOUND_MIDDLE);
					htmltext = event;
				}
				break;
			}
			case "31328-11.htm":
			{
				if (qs.isCond(14) && qs.hasQuestItems(REPORT_BOX))
				{
					qs.takeItems(REPORT_BOX, -1);
					qs.setCond(15);
					qs.playSound(QuestState.SOUND_MIDDLE);
					htmltext = event;
				}
				break;
			}
			case "31328-19.htm":
			{
				if (qs.isCond(15))
				{
					qs.setCond(16);
					qs.playSound(QuestState.SOUND_MIDDLE);
					htmltext = event;
				}
				break;
			}
			case "31527-02.htm":
			{
				if (qs.isCond(10) && (_soulWellNpc == null))
				{
					_soulWellNpc = addSpawn(SOUL_OF_WELL, SOUL_WELL_LOC, true, 0);
					startQuestTimer("activateSoulOfWell", 90000, _soulWellNpc, player);
					startQuestTimer("despawnSoulOfWell", 120000, _soulWellNpc, player);
					_soulWellNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
					
					htmltext = event;
				}
				else
				{
					htmltext = "31527-03.htm";
				}
				break;
			}
			case "activateSoulOfWell":
			{
				// this enables onAttack ELSE IF block which allows the player to proceed the quest
				npc.setScriptValue(1);
				break;
			}
			case "despawnSoulOfWell":
			{
				// if the player fails to proceed the quest in 2 minutes, the soul is unspawned
				if (!npc.isDead())
				{
					_soulWellNpc = null;
				}
				npc.deleteMe();
				break;
			}
			case "31529-03.htm":
			{
				if (qs.isCond(9) && qs.hasQuestItems(LETTER_OF_INNOCENTIN))
				{
					qs.set("memoState", "8");
					htmltext = event;
				}
				break;
			}
			case "31529-08.htm":
			{
				if (qs.getInt("memoState") == 8)
				{
					qs.set("memoState", "9");
					htmltext = event;
				}
				break;
			}
			case "31529-11.htm":
			{
				if (qs.getInt("memoState") == 9)
				{
					qs.giveItems(JEWEL_OF_ADVENTURER_1, 1);
					qs.setCond(10);
					qs.playSound(QuestState.SOUND_MIDDLE);
					qs.set("memoState", "10");
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState qs = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		switch (npc.getNpcId())
		{
			case TIFAREN:
			{
				switch (qs.getCond())
				{
					case 0:
					{
						if (qs.isCreated())
						{
							htmltext = "31334-01.htm";
						}
						else if (qs.isCompleted())
						{
							htmltext = getAlreadyCompletedMsg();
						}
						break;
					}
					case 1:
					case 3:
					{
						htmltext = "31334-05.htm";
						break;
					}
					case 4:
					case 5:
					{
						if (qs.hasQuestItems(CROSS_OF_EINHASAD))
						{
							if (!qs.hasQuestItems(LOST_SKULL_OF_ELF))
							{
								htmltext = "31334-09.htm";
							}
							else if (_tifarenOwner == 0)
							{
								htmltext = "31334-10.htm";
							}
							else
							{
								htmltext = "31334-11.htm";
							}
						}
						break;
					}
					case 6:
					case 7:
					{
						if (qs.hasQuestItems(CROSS_OF_EINHASAD))
						{
							if (_tifarenOwner == 0)
							{
								htmltext = "31334-17.htm";
							}
							else if (_tifarenOwner == player.getObjectId())
							{
								htmltext = "31334-15.htm";
							}
							else
							{
								htmltext = "31334-16.htm";
								qs.setCond(6);
							}
						}
						break;
					}
					case 8:
					{
						if (qs.hasQuestItems(CROSS_OF_EINHASAD))
						{
							htmltext = "31334-18.htm";
						}
						break;
					}
				}
				break;
			}
			case GHOST_OF_PRIEST:
			{
				if (npc.getScriptValue() == player.getObjectId())
				{
					htmltext = "31528-01.htm";
				}
				else
				{
					htmltext = "31528-03.htm";
				}
				break;
			}
			case INNOCENTIN:
			{
				switch (qs.getCond())
				{
					case 2:
					{
						if (!qs.hasQuestItems(CROSS_OF_EINHASAD))
						{
							qs.giveItems(CROSS_OF_EINHASAD, 1);
							qs.setCond(3);
							htmltext = "31328-01.htm";
						}
						break;
					}
					case 3:
					{
						if (qs.hasQuestItems(CROSS_OF_EINHASAD))
						{
							htmltext = "31328-01b.htm";
						}
						break;
					}
					case 8:
					{
						if (qs.hasQuestItems(CROSS_OF_EINHASAD))
						{
							htmltext = "31328-02.htm";
						}
						else
						{
							htmltext = "31328-04.htm";
						}
						break;
					}
					case 9:
					{
						htmltext = "31328-09a.htm";
						break;
					}
					case 14:
					{
						if (qs.hasQuestItems(REPORT_BOX))
						{
							htmltext = "31328-10.htm";
						}
						break;
					}
					case 15:
					{
						htmltext = "31328-12.htm";
						break;
					}
					case 16:
					{
						qs.rewardExpAndSp(345966, 31578);
						qs.exitQuest(false);
						if (player.getLevel() >= MIN_LEVEL)
						{
							htmltext = "31328-20.htm";
						}
						else
						{
							htmltext = "31328-21.htm";
						}
						break;
					}
				}
				break;
			}
			case WELL:
			{
				switch (qs.getCond())
				{
					case 10:
					{
						if (qs.hasQuestItems(JEWEL_OF_ADVENTURER_1))
						{
							htmltext = "31527-01.htm";
							
						}
						break;
					}
					case 12:
					{
						if (qs.hasQuestItems(JEWEL_OF_ADVENTURER_2) && !qs.hasQuestItems(SEALED_REPORT_BOX))
						{
							qs.giveItems(SEALED_REPORT_BOX, 1);
							qs.setCond(13);
							htmltext = "31527-04.htm";
						}
						break;
					}
					case 13:
					case 14:
					case 15:
					case 16:
					{
						htmltext = "31527-05.htm";
						break;
					}
				}
				break;
			}
			case GHOST_OF_ADVENTURER:
			{
				switch (qs.getCond())
				{
					case 9:
					{
						if (qs.hasQuestItems(LETTER_OF_INNOCENTIN))
						{
							switch (qs.getInt("memoState"))
							{
								case 0:
								{
									htmltext = "31529-01.htm";
									break;
								}
								case 8:
								{
									htmltext = "31529-03a.htm";
									break;
								}
								case 9:
								{
									htmltext = "31529-10.htm";
									break;
								}
								default:
								{
									break;
								}
							}
						}
						break;
					}
					case 10:
					{
						if (qs.hasQuestItems(JEWEL_OF_ADVENTURER_1))
						{
							final int id = qs.getInt("memoState");
							if (id == 10)
							{
								htmltext = "31529-12.htm";
							}
							else if (id == 11)
							{
								htmltext = "31529-14.htm";
							}
						}
						break;
					}
					case 11:
					{
						if (qs.hasQuestItems(JEWEL_OF_ADVENTURER_2) && !qs.hasQuestItems(SEALED_REPORT_BOX))
						{
							htmltext = "31529-15.htm";
							qs.setCond(12);
						}
						break;
					}
					case 13:
					{
						if (qs.hasQuestItems(JEWEL_OF_ADVENTURER_2) && qs.hasQuestItems(SEALED_REPORT_BOX))
						{
							qs.giveItems(REPORT_BOX, 1);
							qs.takeItems(SEALED_REPORT_BOX, -1);
							qs.takeItems(JEWEL_OF_ADVENTURER_2, -1);
							qs.setCond(14);
							htmltext = "31529-16.htm";
						}
						break;
					}
					case 14:
					{
						if (qs.hasQuestItems(REPORT_BOX))
						{
							htmltext = "31529-17.htm";
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
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isSummon)
	{
		final QuestState qs = attacker.getQuestState(getName());
		if ((qs != null) && qs.isCond(10) && qs.hasQuestItems(JEWEL_OF_ADVENTURER_1))
		{
			if (qs.getInt("memoState") == 10)
			{
				qs.set("memoState", "11");
			}
			else if (npc.isScriptValue(1))
			{
				qs.takeItems(JEWEL_OF_ADVENTURER_1, -1);
				qs.giveItems(JEWEL_OF_ADVENTURER_2, 1);
				qs.setCond(11);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isSummon)
	{
		if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, killer, npc, true))
		{
			if (npc.getNpcId() == SOUL_OF_WELL)
			{
				_soulWellNpc = null;
			}
			else
			{
				final QuestState qs = killer.getQuestState(getName());
				if ((qs != null) && qs.isCond(4) && qs.hasQuestItems(CROSS_OF_EINHASAD) && !qs.hasQuestItems(LOST_SKULL_OF_ELF) && (Rnd.get(100) < 10))
				{
					qs.giveItems(LOST_SKULL_OF_ELF, 1);
					qs.setCond(5);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}