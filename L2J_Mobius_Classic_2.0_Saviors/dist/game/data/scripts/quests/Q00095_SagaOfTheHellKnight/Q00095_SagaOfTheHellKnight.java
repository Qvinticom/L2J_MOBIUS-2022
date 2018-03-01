package quests.Q00095_SagaOfTheHellKnight;

import java.util.HashSet;
import java.util.Set;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.util.Util;

//QuangNguyen

public class Q00095_SagaOfTheHellKnight extends Quest
{
	public final int MORDRED = 31582;
	public final int ROO_ROO = 34271;
	public final int LANCER = 30477;
	public final int LANCER1 = 34271;
	public final int TABLET_OF_VISION_1 = 31646;
	public final int TABLET_OF_VISION_2 = 31648;
	public final int TABLET_OF_VISION_3 = 31653;
	public final int TABLET_OF_VISION_4 = 31654;
	public final int WALDSTEIN = 31599;
	// mobs
	public final int ICE_MONSTER = 27316;
	public final int SPIRIT_OF_A_DROWNED = 27317;
	public final int SOUL_OF_COLD = 27318;
	public final int GHOST_OF_SOLITUDE = 27319;
	public final int FIEND_OF_COLD = 27320;
	public final int SPIRIT_OF_COLD = 27321;
	public final int SPAMPLAND_WATCHMAN = 21650;
	public final int FLAME_DRAKE = 21651;
	public final int FIERY_IFRIT = 21652;
	public final int IKEDIT = 21653;
	public final int KEEPER_OF_THE_HOLY_EDICT = 27215;
	public final int ARHANGEL_ICONOCLASSIS = 27257;
	public final int HALISHA_ARCHON = 27219;
	public final int DEATH_LORD_HALLATE = 27262;
	// items
	public final int ICE_CRYSTAL_FRAGMENT = 49829;
	public final int HALISHA_BADGE = 7510;
	public final int AMULET_REZONANSA_PERVIY = 7293;
	public final int AMULET_REZONANSA_VTOROI = 7324;
	public final int AMULET_REZONANSA_TRETIY = 7355;
	public final int AMULET_REZONANSA_CHETVERTIY = 7386;
	public final int INVESTIGATIVE_REPORT = 7532;
	// reward
	public final int BOOKGOLDLION = 90038;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q00095_SagaOfTheHellKnight()
	{
		super(95);
		addStartNpc(MORDRED);
		addTalkId(MORDRED, ROO_ROO, LANCER, LANCER1, WALDSTEIN, TABLET_OF_VISION_1, TABLET_OF_VISION_2, TABLET_OF_VISION_3, TABLET_OF_VISION_4);
		addKillId(ICE_MONSTER, SPIRIT_OF_A_DROWNED, SOUL_OF_COLD, GHOST_OF_SOLITUDE, FIEND_OF_COLD);
		addKillId(SPIRIT_OF_COLD, SPAMPLAND_WATCHMAN, FLAME_DRAKE, FIERY_IFRIT, IKEDIT);
		addKillId(KEEPER_OF_THE_HOLY_EDICT, ARHANGEL_ICONOCLASSIS, HALISHA_ARCHON, DEATH_LORD_HALLATE);
		registerQuestItems(INVESTIGATIVE_REPORT, ICE_CRYSTAL_FRAGMENT, HALISHA_BADGE);
		addCondMinLevel(76, "mordred_q95_02.htm");
		addCondClassId(ClassId.DARK_AVENGER, "mordred_q95_03.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		String htmltext = event;
		switch (event)
		{
			case "mordred_q95_02a.htm":
				htmltext = "mordred_q95_5.htm";
				break;
			case "mordred_q95_001.htm":
				if (st.getCond() == 0)
				{
					st.startQuest();
					st.setCond(1);
				}
				break;
			case "ruru2.htm":
				if (st.getCond() == 1)
				{
					st.setCond(2);
				}
				break;
			case "ruru4.htm":
				if (st.getCond() == 2)
				{
					st.setCond(3);
				}
				break;
			case "ruru6.htm":
				if (st.getCond() == 4)
				{
					st.setCond(5);
				}
				takeItems(player, ICE_CRYSTAL_FRAGMENT, -1);
				giveItems(player, INVESTIGATIVE_REPORT, 1);
				break;
			case "lancer6.htm":
				if (st.getCond() == 5)
				{
					st.setCond(6);
				}
				giveItems(player, AMULET_REZONANSA_PERVIY, 1);
				takeItems(player, INVESTIGATIVE_REPORT, -1);
				break;
			case "stone12.htm":
				if (st.getCond() == 6)
				{
					st.setCond(7);
				}
				break;
			case "stone22.htm":
				if (st.getCond() == 8)
				{
					addSpawn(ARHANGEL_ICONOCLASSIS, npc, true, 0, true);
					st.setCond(9);
				}
				break;
			case "stone25.htm":
				if (st.getCond() == 10)
				{
					st.setCond(11);
				}
				break;
			case "lancer12.htm":
				if (st.getCond() == 11)
				{
					st.setCond(12);
				}
				break;
			case "stone32.htm":
				if (st.getCond() == 14)
				{
					st.setCond(15);
				}
				break;
			case "valdwtein2.htm":
				if (st.getCond() == 16)
				{
					st.setCond(17);
				}
				giveItems(player, AMULET_REZONANSA_CHETVERTIY, 1);
				break;
			case "stone42.htm":
				if (st.getCond() == 17)
				{
					st.setCond(18);
				}
				break;
			case "mordred_q95_22.htm":
				if (st.getCond() == 18)
				{
					if ((player.getLevel() < 76) && (player.getBaseClass() != 6))
					{
						htmltext = "30849-nolvl.htm";
					}
					addExpAndSp(player, 3100000, 103000);
					rewardItems(player, BOOKGOLDLION, 1);
					takeItems(player, AMULET_REZONANSA_PERVIY, -1);
					takeItems(player, AMULET_REZONANSA_VTOROI, -1);
					takeItems(player, AMULET_REZONANSA_TRETIY, -1);
					takeItems(player, AMULET_REZONANSA_CHETVERTIY, -1);
					takeItems(player, HALISHA_BADGE, -1);
					st.exitQuest(false, true);
					player.setClassId(91);
					player.setBaseClass(91);
					player.broadcastUserInfo();
					npc.broadcastPacket(new MagicSkillUse(npc, player, 5103, 1, 1000, 0));
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		final QuestState qs = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == MORDRED)
				{
					htmltext = "mordred_q95_01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == MORDRED)
				{
					if (qs.getCond() == 1)
					{
						htmltext = "mordred_q95_001.htm";
					}
					if (qs.getCond() == 18)
					{
						htmltext = "mordred_q95_011.htm";
					}
					if (qs.getCond() == 19)
					{
						htmltext = "mordred_q95_012.htm";
					}
					
				}
				if (npc.getId() == LANCER)
				{
					if (qs.getCond() == 1)
					{
						htmltext = "ruru.htm";
					}
					if (qs.getCond() == 2)
					{
						htmltext = "ruru2.htm";
					}
					if (qs.getCond() == 5)
					{
						htmltext = "lancer5.htm";
					}
					if (qs.getCond() == 6)
					{
						htmltext = "lancer6.htm";
					}
					if (qs.getCond() == 11)
					{
						htmltext = "lancer11.htm";
					}
					if (qs.getCond() == 12)
					{
						htmltext = "lancer12.htm";
					}
				}
				if (npc.getId() == ROO_ROO)
				{
					if (qs.getCond() == 2)
					{
						htmltext = "ruru3.htm";
					}
					if (qs.getCond() == 3)
					{
						htmltext = "ruru4.htm";
					}
					if (qs.getCond() == 4)
					{
						htmltext = "ruru5.htm";
					}
					if (qs.getCond() == 5)
					{
						htmltext = "ruru6.htm";
					}
				}
				if (npc.getId() == TABLET_OF_VISION_1)
				{
					if (qs.getCond() == 6)
					{
						htmltext = "stone11.htm";
					}
					if (qs.getCond() == 7)
					{
						htmltext = "stone12.htm";
					}
				}
				if (npc.getId() == TABLET_OF_VISION_2)
				{
					if (qs.getCond() == 8)
					{
						htmltext = "stone21.htm";
					}
					if (qs.getCond() == 9)
					{
						htmltext = "stone23.htm";
					}
					if (qs.getCond() == 10)
					{
						htmltext = "stone24.htm";
					}
				}
				if (npc.getId() == TABLET_OF_VISION_3)
				{
					if (qs.getCond() == 14)
					{
						htmltext = "stone31.htm";
					}
					if (qs.getCond() == 15)
					{
						htmltext = "stone33.htm";
					}
				}
				if (npc.getId() == TABLET_OF_VISION_4)
				{
					if (qs.getCond() == 15)
					{
						htmltext = "stone40.htm";
					}
					if (qs.getCond() == 17)
					{
						htmltext = "stone41.htm";
					}
					if (qs.getCond() == 18)
					{
						htmltext = "stone43.htm";
					}
					
				}
				if (npc.getId() == WALDSTEIN)
				{
					if (qs.getCond() == 16)
					{
						htmltext = "valdwtein1.htm";
					}
					if (qs.getCond() == 17)
					{
						htmltext = "valdwtein2.htm";
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
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isStarted() && Util.checkIfInRange(1500, npc, killer, true))
		{
			switch (npc.getId())
			{
				case ICE_MONSTER:
				case SPIRIT_OF_A_DROWNED:
				case SOUL_OF_COLD:
				case FIEND_OF_COLD:
				case GHOST_OF_SOLITUDE:
				case SPIRIT_OF_COLD:
					if (qs.getCond() == 3)
					{
						if (getQuestItemsCount(killer, ICE_CRYSTAL_FRAGMENT) < 50)
						{
							giveItemRandomly(killer, npc, ICE_CRYSTAL_FRAGMENT, 1, 50, 50, true);
						}
						if (getQuestItemsCount(killer, ICE_CRYSTAL_FRAGMENT) >= 50)
						{
							qs.setCond(4);
						}
					}
					break;
				case ARHANGEL_ICONOCLASSIS:
					if (qs.getCond() == 9)
					{
						qs.setCond(10);
					}
					break;
				case SPAMPLAND_WATCHMAN:
				case FLAME_DRAKE:
				case FIERY_IFRIT:
				case IKEDIT:
					if (qs.getCond() == 12)
					{
						if (getQuestItemsCount(killer, HALISHA_BADGE) < 701)
						{
							giveItemRandomly(killer, npc, HALISHA_BADGE, 1, 701, 50, true);
							if (getQuestItemsCount(killer, HALISHA_BADGE) >= 701)
							{
								addSpawn(HALISHA_ARCHON, npc, true, 0, true);
								qs.setCond(13);
							}
						}
					}
					break;
				case HALISHA_ARCHON:
					if (qs.getCond() == 13)
					{
						if (!hasQuestItems(killer, AMULET_REZONANSA_TRETIY))
						{
							giveItemRandomly(killer, npc, AMULET_REZONANSA_TRETIY, 1, 1, 50, true);
						}
						qs.setCond(14);
					}
					break;
				case DEATH_LORD_HALLATE:
					if (qs.getCond() == 15)
					{
						qs.setCond(16);
					}
					break;
			}
			
			if (qs.isCond(7))
			{
				int count = qs.getInt(KILL_COUNT_VAR);
				if (npc.getId() == KEEPER_OF_THE_HOLY_EDICT)
				{
					if (count < 20)
					{
						qs.set(KILL_COUNT_VAR, ++count);
						sendNpcLogList(killer);
					}
				}
				if (count >= 20)
				{
					qs.setCond(8, true);
					giveItems(killer, AMULET_REZONANSA_VTOROI, 1);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(7))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>(1);
			
			// guardian of forbidden knowledge
			final int guardiancount = qs.getInt(KILL_COUNT_VAR);
			if (guardiancount > 0)
			{
				holder.add(new NpcLogListHolder(KEEPER_OF_THE_HOLY_EDICT, false, guardiancount));
			}
			return holder;
		}
		return super.getNpcLogList(player);
	}
}