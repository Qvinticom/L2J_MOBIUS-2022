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
package quests.Q00092_SagaOfTheElementalMaster;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author QuangNguyen
 */
public class Q00092_SagaOfTheElementalMaster extends Quest
{
	// NPCs
	private static final int AKENIAS = 30174;
	private static final int RADYSS = 31614;
	private static final int CERENAS = 31281;
	private static final int TABLET_OF_VISION_1 = 31646;
	private static final int TABLET_OF_VISION_2 = 31647;
	private static final int TABLET_OF_VISION_3 = 31650;
	private static final int TABLET_OF_VISION_4 = 31655;
	private static final int KALEIDOS_THE_UNICORN = 31629;
	// Monsters
	private static final int ICE_MONSTER = 27316;
	private static final int SPIRIT_OF_A_DROWNED = 27317;
	private static final int SOUL_OF_COLD = 27318;
	private static final int GHOST_OF_SOLITUDE = 27319;
	private static final int FIEND_OF_COLD = 27320;
	private static final int SPIRIT_OF_COLD = 27321;
	private static final int SPAMPLAND_WATCHMAN = 21650;
	private static final int FLAME_DRAKE = 21651;
	private static final int FIERY_IFRIT = 21652;
	private static final int IKEDIT = 21653;
	private static final int GUARDIAN_OF_FORBIDDEN_KNOWLEDGE = 27214;
	private static final int VISION_GUARDIAN_SHAKIEL = 27313;
	private static final int HALISHA_ARCHON = 27219;
	private static final int DOG_KELVAS = 27311;
	// Items
	private static final int ICE_CRYSTAL_FRAGMENT = 49826;
	private static final int HALISHA_BADGE = 7507;
	private static final int RESONANCE_AMULET = 7290;
	private static final int RESONANCE_AMULET_2 = 7321;
	private static final int RESONANCE_AMULET_3 = 7352;
	private static final int RESONANCE_AMULET_4 = 7383;
	private static final int MIRACLE_POTION = 7605;
	// Reward
	private static final int BOOK_PEGASUS = 90039;
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q00092_SagaOfTheElementalMaster()
	{
		super(92);
		addStartNpc(AKENIAS);
		addTalkId(AKENIAS, RADYSS, CERENAS, KALEIDOS_THE_UNICORN, TABLET_OF_VISION_1, TABLET_OF_VISION_2, TABLET_OF_VISION_3, TABLET_OF_VISION_4);
		addKillId(ICE_MONSTER, SPIRIT_OF_A_DROWNED, SOUL_OF_COLD, GHOST_OF_SOLITUDE, FIEND_OF_COLD, SPIRIT_OF_COLD, SPAMPLAND_WATCHMAN, FLAME_DRAKE, FIERY_IFRIT, IKEDIT, GUARDIAN_OF_FORBIDDEN_KNOWLEDGE, VISION_GUARDIAN_SHAKIEL, HALISHA_ARCHON, DOG_KELVAS);
		registerQuestItems(MIRACLE_POTION, ICE_CRYSTAL_FRAGMENT, HALISHA_BADGE);
		addCondMinLevel(76, "aiken02.htm");
		addCondClassId(ClassId.ELEMENTAL_SUMMONER, "aiken03.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		String htmltext = event;
		switch (event)
		{
			case "aiken02a.htm":
			{
				htmltext = "aiken5.htm";
				break;
			}
			case "aiken01s.htm":
			{
				if (qs.isCond(0))
				{
					qs.startQuest();
					qs.setCond(1);
				}
				break;
			}
			case "jer2.htm":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2);
				}
				break;
			}
			case "rifken2.htm":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3);
				}
				break;
			}
			case "rifken4.htm":
			{
				if (qs.isCond(4))
				{
					qs.setCond(5);
					takeItems(player, ICE_CRYSTAL_FRAGMENT, -1);
					giveItems(player, MIRACLE_POTION, 1);
				}
				break;
			}
			case "jer4.htm":
			{
				if (qs.isCond(5))
				{
					qs.setCond(6);
					giveItems(player, RESONANCE_AMULET, 1);
					takeItems(player, MIRACLE_POTION, -1);
				}
				break;
			}
			case "stone12.htm":
			{
				if (qs.isCond(6))
				{
					qs.setCond(7);
				}
				break;
			}
			case "stone22.htm":
			{
				if (qs.isCond(8))
				{
					addSpawn(VISION_GUARDIAN_SHAKIEL, npc, true, 0, true);
					qs.setCond(9);
				}
				break;
			}
			case "stone25.htm":
			{
				if (qs.isCond(10))
				{
					qs.setCond(11);
				}
				break;
			}
			case "jer6.htm":
			{
				if (qs.isCond(11))
				{
					qs.setCond(12);
				}
				break;
			}
			case "stone32.htm":
			{
				if (qs.isCond(14))
				{
					qs.setCond(15);
				}
				break;
			}
			case "stone41.htm":
			{
				if (qs.isCond(15))
				{
					qs.setCond(16);
					addSpawn(DOG_KELVAS, npc, true, 0, true);
				}
				break;
			}
			case "erikrams2.htm":
			{
				if (qs.isCond(16))
				{
					qs.setCond(17);
					giveItems(player, RESONANCE_AMULET_4, 1);
				}
				break;
			}
			case "stone43.htm":
			{
				if (qs.isCond(17))
				{
					qs.setCond(18);
				}
				break;
			}
			case "aiken7.htm":
			{
				if (qs.isCond(18))
				{
					if ((player.getLevel() < 76) || (player.getBaseClass() != 28))
					{
						htmltext = "30849-nolvl.htm";
					}
					else
					{
						addExpAndSp(player, 3100000, 103000);
						rewardItems(player, BOOK_PEGASUS, 1);
						takeItems(player, RESONANCE_AMULET, -1);
						takeItems(player, RESONANCE_AMULET_2, -1);
						takeItems(player, RESONANCE_AMULET_3, -1);
						takeItems(player, RESONANCE_AMULET_4, -1);
						takeItems(player, HALISHA_BADGE, -1);
						qs.exitQuest(false, true);
						player.setClassId(104);
						player.setBaseClass(104);
						player.broadcastUserInfo();
						npc.broadcastPacket(new MagicSkillUse(npc, player, 5103, 1, 1000, 0));
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final QuestState qs = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == AKENIAS)
				{
					htmltext = "aiken01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case AKENIAS:
					{
						if (qs.isCond(1))
						{
							htmltext = "aiken01s.htm";
						}
						else if (qs.isCond(18))
						{
							htmltext = "aiken6.htm";
						}
						break;
					}
					case RADYSS:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "jer1.htm";
								break;
							}
							case 2:
							{
								htmltext = "jer2.htm";
								break;
							}
							case 5:
							{
								htmltext = "jer3.htm";
								break;
							}
							case 6:
							{
								htmltext = "jer4.htm";
								break;
							}
							case 11:
							{
								htmltext = "jer5.htm";
								break;
							}
							case 12:
							{
								htmltext = "jer6.htm";
								break;
							}
						}
						break;
					}
					case CERENAS:
					{
						switch (qs.getCond())
						{
							case 2:
							{
								htmltext = "rifken1.htm";
								break;
							}
							case 3:
							{
								htmltext = "rifken2.htm";
								break;
							}
							case 4:
							{
								htmltext = "rifken3.htm";
								break;
							}
						}
						break;
					}
					case TABLET_OF_VISION_1:
					{
						if (qs.isCond(6))
						{
							htmltext = "stone11.htm";
						}
						else if (qs.isCond(7))
						{
							htmltext = "stone12.htm";
						}
						break;
					}
					case TABLET_OF_VISION_2:
					{
						switch (qs.getCond())
						{
							case 8:
							{
								htmltext = "stone21.htm";
								break;
							}
							case 9:
							{
								htmltext = "stone23.htm";
								break;
							}
							case 10:
							{
								htmltext = "stone24.htm";
								break;
							}
							case 11:
							{
								htmltext = "stone25.htm";
								break;
							}
						}
						break;
					}
					case TABLET_OF_VISION_3:
					{
						if (qs.isCond(14))
						{
							htmltext = "stone31.htm";
						}
						else if (qs.isCond(15))
						{
							htmltext = "stone32.htm";
						}
						break;
					}
					case TABLET_OF_VISION_4:
					{
						switch (qs.getCond())
						{
							case 15:
							{
								htmltext = "stone40.htm";
								break;
							}
							case 17:
							{
								htmltext = "stone42.htm";
								break;
							}
							case 18:
							{
								htmltext = "stone43.htm";
								break;
							}
						}
						break;
					}
					case KALEIDOS_THE_UNICORN:
					{
						if (qs.isCond(16))
						{
							htmltext = "erikrams1.htm";
						}
						else if (qs.isCond(17))
						{
							htmltext = "erikrams2.htm";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(talker);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isStarted() && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, killer, true))
		{
			switch (npc.getId())
			{
				case ICE_MONSTER:
				case SPIRIT_OF_A_DROWNED:
				case SOUL_OF_COLD:
				case FIEND_OF_COLD:
				case GHOST_OF_SOLITUDE:
				case SPIRIT_OF_COLD:
				{
					if (qs.isCond(3) && giveItemRandomly(killer, npc, ICE_CRYSTAL_FRAGMENT, 1, 50, 0.5, true))
					{
						qs.setCond(4);
					}
					break;
				}
				case VISION_GUARDIAN_SHAKIEL:
				{
					if (qs.isCond(9))
					{
						qs.setCond(10);
					}
					break;
				}
				case SPAMPLAND_WATCHMAN:
				case FLAME_DRAKE:
				case FIERY_IFRIT:
				case IKEDIT:
				{
					if (qs.isCond(12) && giveItemRandomly(killer, npc, HALISHA_BADGE, 1, 700, 0.5, true))
					{
						addSpawn(HALISHA_ARCHON, npc, true, 0, true);
						qs.setCond(13);
					}
					break;
				}
				case HALISHA_ARCHON:
				{
					if (qs.isCond(13))
					{
						giveItems(killer, RESONANCE_AMULET_3, 1, true);
						qs.setCond(14);
					}
					break;
				}
				case DOG_KELVAS:
				{
					if (qs.isCond(16))
					{
						addSpawn(KALEIDOS_THE_UNICORN, npc, true, 20000, true);
					}
					break;
				}
				case GUARDIAN_OF_FORBIDDEN_KNOWLEDGE:
				{
					if (qs.isCond(7))
					{
						final int count = qs.getInt(KILL_COUNT_VAR);
						if (count < 20)
						{
							qs.set(KILL_COUNT_VAR, count + 1);
						}
						if (count >= 20)
						{
							qs.setCond(8, true);
							giveItems(killer, RESONANCE_AMULET_2, 1);
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(7))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>(1);
			
			// guardian of forbidden knowledge
			final int guardiancount = qs.getInt(KILL_COUNT_VAR);
			if (guardiancount > 0)
			{
				holder.add(new NpcLogListHolder(GUARDIAN_OF_FORBIDDEN_KNOWLEDGE, false, guardiancount));
			}
			return holder;
		}
		return super.getNpcLogList(player);
	}
}