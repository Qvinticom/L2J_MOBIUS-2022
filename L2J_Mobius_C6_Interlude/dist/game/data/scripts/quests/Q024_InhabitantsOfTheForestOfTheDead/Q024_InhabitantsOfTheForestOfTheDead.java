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
package quests.Q024_InhabitantsOfTheForestOfTheDead;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

import quests.Q023_LidiasHeart.Q023_LidiasHeart;

public class Q024_InhabitantsOfTheForestOfTheDead extends Quest
{
	// NPCs
	private static final int DORIAN = 31389;
	private static final int WIZARD = 31522;
	private static final int TOMBSTONE = 31531;
	private static final int MAID_OF_LIDIA = 31532;
	private static final int[] MOBS =
	{
		21557, // BONE_SNATCHER
		21558, // BONE_SNATCHER_A
		21560, // BONE_SHAPER
		21563, // BONE_COLLECTOR
		21564, // SKULL_COLLECTOR
		21565, // BONE_ANIMATOR
		21566, // SKULL_ANIMATOR
		21567, // BONE_SLAYER
	};
	// Items
	private static final int LETTER = 7065;
	private static final int HAIRPIN = 7148;
	private static final int TOTEM = 7151;
	private static final int FLOWER = 7152;
	private static final int SILVER_CROSS = 7153;
	private static final int BROKEN_SILVER_CROSS = 7154;
	private static final int SUSPICIOUS_TOTEM = 7156;
	
	public Q024_InhabitantsOfTheForestOfTheDead()
	{
		super(24, "Inhabitants of the Forest of the Dead");
		
		addStartNpc(DORIAN);
		addTalkId(DORIAN, TOMBSTONE, MAID_OF_LIDIA, WIZARD);
		registerQuestItems(FLOWER, SILVER_CROSS, BROKEN_SILVER_CROSS, LETTER, HAIRPIN, TOTEM);
		addKillId(MOBS);
		addAggroRangeEnterId(25332);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return event;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "31389-03.htm":
			{
				st.startQuest();
				st.set("state", "1");
				st.giveItems(FLOWER, 1);
				break;
			}
			case "31389-08.htm":
			{
				st.set("state", "3");
				break;
			}
			case "31389-13.htm":
			{
				st.setCond(3);
				st.set("state", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(SILVER_CROSS, 1);
				break;
			}
			case "31389-18.htm":
			{
				st.playSound("InterfaceSound.charstat_open_01");
				break;
			}
			case "31389-19.htm":
			{
				st.setCond(5);
				st.set("state", "5");
				st.takeItems(BROKEN_SILVER_CROSS, -1);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "31522-03.htm":
			{
				st.set("state", "12");
				st.takeItems(TOTEM, -1);
				break;
			}
			case "31522-08.htm":
			{
				st.setCond(11);
				st.set("state", "13");
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "31522-17.htm":
			{
				st.set("state", "14");
				break;
			}
			case "31522-21.htm":
			{
				st.giveItems(SUSPICIOUS_TOTEM, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
				break;
			}
			case "31532-04.htm":
			{
				st.setCond(6);
				st.set("state", "6");
				st.giveItems(LETTER, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "31532-06.htm":
			{
				if (st.hasQuestItems(HAIRPIN))
				{
					st.set("state", "8");
					st.takeItems(LETTER, -1);
					st.takeItems(HAIRPIN, -1);
				}
				else
				{
					st.setCond(7);
					st.set("state", "7");
					htmltext = "31532-07.htm";
				}
				break;
			}
			case "31532-10.htm":
			{
				st.set("state", "9");
				break;
			}
			case "31532-14.htm":
			{
				st.set("state", "10");
				break;
			}
			case "31532-19.htm":
			{
				st.setCond(9);
				st.set("state", "11");
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
			case "31531-02.htm":
			{
				st.setCond(2);
				st.set("state", "2");
				st.takeItems(FLOWER, -1);
				st.playSound(QuestState.SOUND_MIDDLE);
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
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
				final QuestState st2 = player.getQuestState(Q023_LidiasHeart.class.getSimpleName());
				if ((st2 != null) && st2.isCompleted() && (player.getLevel() >= 65))
				{
					htmltext = "31389-01.htm";
				}
				else
				{
					htmltext = "31389-02.htm";
				}
				break;
			}
			case State.STARTED:
			{
				int state = st.getInt("state");
				switch (npc.getNpcId())
				{
					case 31389:
					{
						if (state == 1)
						{
							htmltext = "31389-04.htm";
							return htmltext;
						}
						else if (state == 2)
						{
							htmltext = "31389-05.htm";
							return htmltext;
						}
						else if (state == 3)
						{
							htmltext = "31389-09.htm";
							return htmltext;
						}
						else if (state == 4)
						{
							if (st.hasQuestItems(SILVER_CROSS))
							{
								htmltext = "31389-14.htm";
							}
							else if (st.hasQuestItems(BROKEN_SILVER_CROSS))
							{
								htmltext = "31389-15.htm";
								return htmltext;
							}
							
							return htmltext;
						}
						else if (state == 5)
						{
							htmltext = "31389-20.htm";
							return htmltext;
						}
						else
						{
							if ((state == 7) && !st.hasQuestItems(HAIRPIN))
							{
								htmltext = "31389-21.htm";
								st.setCond(8);
								st.giveItems(HAIRPIN, 1);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else if (((state == 7) && st.hasQuestItems(HAIRPIN)) || (state == 6))
							{
								htmltext = "31389-22.htm";
								return htmltext;
							}
							
							return htmltext;
						}
					}
					case 31522:
					{
						if ((state == 11) && st.hasQuestItems(TOTEM))
						{
							htmltext = "31522-01.htm";
							return htmltext;
						}
						else if (state == 12)
						{
							htmltext = "31522-04.htm";
							return htmltext;
						}
						else
						{
							if (state == 13)
							{
								htmltext = "31522-09.htm";
							}
							else if (state == 14)
							{
								htmltext = "31522-18.htm";
								return htmltext;
							}
							
							return htmltext;
						}
					}
					case 31531:
					{
						if ((state == 1) && st.hasQuestItems(FLOWER))
						{
							htmltext = "31531-01.htm";
							st.playSound("AmdSound.d_wind_loot_02");
						}
						else if (state == 2)
						{
							htmltext = "31531-03.htm";
							return htmltext;
						}
						
						return htmltext;
					}
					case 31532:
					{
						if (state == 5)
						{
							htmltext = "31532-01.htm";
							return htmltext;
						}
						else if ((state == 6) && st.hasQuestItems(LETTER))
						{
							htmltext = "31532-05.htm";
							return htmltext;
						}
						else if (state == 7)
						{
							htmltext = "31532-07a.htm";
							return htmltext;
						}
						else if (state == 8)
						{
							htmltext = "31532-08.htm";
							return htmltext;
						}
						else if (state == 9)
						{
							htmltext = "31532-11.htm";
							return htmltext;
						}
						else
						{
							if (state == 10)
							{
								htmltext = "31532-15.htm";
							}
							else if (state == 11)
							{
								htmltext = "31532-20.htm";
								return htmltext;
							}
							
							return htmltext;
						}
					}
					default:
					{
						return htmltext;
					}
				}
			}
			case State.COMPLETED:
			{
				if (npc.getNpcId() == 31522)
				{
					htmltext = "31522-22.htm";
				}
				else
				{
					htmltext = getAlreadyCompletedMsg();
				}
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet)
	{
		if (isPet)
		{
			npc.getAttackByList().remove(player.getPet());
		}
		else
		{
			npc.getAttackByList().remove(player);
			final QuestState qs = player.getQuestState(getName());
			if ((qs != null) && (qs.getQuestItemsCount(SILVER_CROSS) > 0))
			{
				qs.takeItems(SILVER_CROSS, -1);
				qs.giveItems(BROKEN_SILVER_CROSS, 1);
				qs.setCond(4);
				for (Player nearby : npc.getKnownList().getKnownPlayers().values())
				{
					nearby.sendPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "That sign!"));
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Player partyMember = getRandomPartyMember(player, npc, 9);
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		if (st.dropItems(TOTEM, 1, 1, 100000))
		{
			st.setCond(10);
		}
		
		return null;
	}
	
}