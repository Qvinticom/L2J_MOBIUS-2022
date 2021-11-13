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
package quests.Q242_PossessorOfAPreciousSoul;

import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Q242_PossessorOfAPreciousSoul extends Quest
{
	// NPCs
	private static final int VIRGIL = 31742;
	private static final int KASSANDRA = 31743;
	private static final int OGMAR = 31744;
	private static final int MYSTERIOUS_KNIGHT = 31751;
	private static final int ANGEL_CORPSE = 31752;
	private static final int KALIS = 30759;
	private static final int MATILD = 30738;
	private static final int CORNERSTONE = 31748;
	private static final int FALLEN_UNICORN = 31746;
	private static final int PURE_UNICORN = 31747;
	// Monsters
	private static final int RESTRAINER_OF_GLORY = 27317;
	// Items
	private static final int VIRGIL_LETTER = 7677;
	private static final int GOLDEN_HAIR = 7590;
	private static final int SORCERY_INGREDIENT = 7596;
	private static final int ORB_OF_BINDING = 7595;
	private static final int CARADINE_LETTER = 7678;
	// Misc
	private static boolean _unicorn = false;
	
	public Q242_PossessorOfAPreciousSoul()
	{
		super(242, "Possessor of a Precious Soul - 2");
		registerQuestItems(GOLDEN_HAIR, SORCERY_INGREDIENT, ORB_OF_BINDING);
		addStartNpc(VIRGIL);
		addTalkId(VIRGIL, KASSANDRA, OGMAR, MYSTERIOUS_KNIGHT, ANGEL_CORPSE, KALIS, MATILD, CORNERSTONE, FALLEN_UNICORN, PURE_UNICORN);
		addKillId(RESTRAINER_OF_GLORY);
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
			case "31743-05.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "31744-02.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "31751-02.htm":
			{
				st.setCond(4);
				st.set("angel", "0");
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30759-02.htm":
			{
				st.setCond(7);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "30759-05.htm":
			{
				if (st.hasQuestItems(SORCERY_INGREDIENT))
				{
					st.set("orb", "0");
					st.set("cornerstone", "0");
					st.setCond(9);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(GOLDEN_HAIR, 1);
					st.takeItems(SORCERY_INGREDIENT, 1);
				}
				else
				{
					st.setCond(7);
					htmltext = "30759-02.htm";
				}
				break;
			}
			case "30738-02.htm":
			{
				st.setCond(8);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(SORCERY_INGREDIENT, 1);
				break;
			}
			case "31748-03.htm":
			{
				if (st.hasQuestItems(ORB_OF_BINDING))
				{
					npc.doDie(npc);
					st.takeItems(ORB_OF_BINDING, 1);
					
					int cornerstones = st.getInt("cornerstone");
					cornerstones++;
					if (cornerstones == 4)
					{
						st.unset("orb");
						st.unset("cornerstone");
						st.setCond(10);
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
					{
						st.set("cornerstone", Integer.toString(cornerstones));
					}
				}
				else
				{
					htmltext = null;
				}
				break;
			}
			case "spu":
			{
				addSpawn(PURE_UNICORN, 85884, -76588, -3470, 0, false, 0);
				return null;
			}
			case "dspu":
			{
				npc.getSpawn().stopRespawn();
				npc.deleteMe();
				startQuestTimer("sfu", 2000, null, player, false);
				return null;
			}
			case "sfu":
			{
				final NpcInstance unicorn = addSpawn(FALLEN_UNICORN, 85884, -76588, -3470, 0, false, 0);
				unicorn.getSpawn().startRespawn();
				return null;
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
				if (st.hasQuestItems(VIRGIL_LETTER))
				{
					if (!player.isSubClassActive() || (player.getLevel() < 60))
					{
						htmltext = "31742-02.htm";
					}
					else
					{
						st.startQuest();
						st.takeItems(VIRGIL_LETTER, 1);
						htmltext = "31742-03.htm";
					}
				}
				break;
			}
			case State.STARTED:
			{
				if (!player.isSubClassActive())
				{
					break;
				}
				
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case VIRGIL:
					{
						if (cond == 1)
						{
							htmltext = "31742-04.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31742-05.htm";
						}
						break;
					}
					case KASSANDRA:
					{
						if (cond == 1)
						{
							htmltext = "31743-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31743-06.htm";
						}
						else if (cond == 11)
						{
							htmltext = "31743-07.htm";
							st.giveItems(CARADINE_LETTER, 1);
							st.rewardExpAndSp(455764, 0);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					}
					case OGMAR:
					{
						if (cond == 2)
						{
							htmltext = "31744-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "31744-03.htm";
						}
						break;
					}
					case MYSTERIOUS_KNIGHT:
					{
						if (cond == 3)
						{
							htmltext = "31751-01.htm";
						}
						else if (cond == 4)
						{
							htmltext = "31751-03.htm";
						}
						else if (cond == 5)
						{
							if (st.hasQuestItems(GOLDEN_HAIR))
							{
								htmltext = "31751-04.htm";
								st.setCond(6);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								htmltext = "31751-03.htm";
								st.setCond(4);
							}
						}
						else if (cond == 6)
						{
							htmltext = "31751-05.htm";
						}
						break;
					}
					case ANGEL_CORPSE:
					{
						if (cond == 4)
						{
							npc.doDie(npc);
							int hair = st.getInt("angel");
							hair++;
							
							if (hair == 4)
							{
								htmltext = "31752-02.htm";
								st.unset("angel");
								st.setCond(5);
								st.playSound(QuestState.SOUND_MIDDLE);
								st.giveItems(GOLDEN_HAIR, 1);
							}
							else
							{
								st.set("angel", Integer.toString(hair));
								htmltext = "31752-01.htm";
							}
						}
						else if (cond == 5)
						{
							htmltext = "31752-01.htm";
						}
						break;
					}
					case KALIS:
					{
						if (cond == 6)
						{
							htmltext = "30759-01.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30759-03.htm";
						}
						else if (cond == 8)
						{
							if (st.hasQuestItems(SORCERY_INGREDIENT))
							{
								htmltext = "30759-04.htm";
							}
							else
							{
								htmltext = "30759-03.htm";
								st.setCond(7);
							}
						}
						else if (cond == 9)
						{
							htmltext = "30759-06.htm";
						}
						break;
					}
					case MATILD:
					{
						if (cond == 7)
						{
							htmltext = "30738-01.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30738-03.htm";
						}
						break;
					}
					case CORNERSTONE:
					{
						if (cond == 9)
						{
							if (st.hasQuestItems(ORB_OF_BINDING))
							{
								htmltext = "31748-02.htm";
							}
							else
							{
								htmltext = "31748-01.htm";
							}
						}
						break;
					}
					case FALLEN_UNICORN:
					{
						if (cond == 9)
						{
							htmltext = "31746-01.htm";
						}
						else if (cond == 10)
						{
							if (!_unicorn) // Global variable check to prevent multiple spawns
							{
								_unicorn = true;
								npc.getSpawn().stopRespawn(); // Despawn fallen unicorn
								npc.deleteMe();
								startQuestTimer("spu", 3000, npc, player, false);
							}
							htmltext = "31746-02.htm";
						}
						break;
					}
					case PURE_UNICORN:
					{
						if (cond == 10)
						{
							st.setCond(11);
							st.playSound(QuestState.SOUND_MIDDLE);
							if (_unicorn) // Global variable check to prevent multiple spawns
							{
								_unicorn = false;
								startQuestTimer("dspu", 3000, npc, player, false);
							}
							htmltext = "31747-01.htm";
						}
						else if (cond == 11)
						{
							htmltext = "31747-02.htm";
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
		final QuestState st = checkPlayerCondition(player, npc, 9);
		if ((st == null) || !player.isSubClassActive())
		{
			return null;
		}
		
		int orbs = st.getInt("orb"); // check orbs internally, because player can use them before he gets them all
		if (orbs < 4)
		{
			orbs++;
			st.set("orb", Integer.toString(orbs));
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(ORB_OF_BINDING, 1);
		}
		
		return null;
	}
}