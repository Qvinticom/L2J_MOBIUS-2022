package quests.Q00073_SagaOfTheDuelist;

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

public class Q00073_SagaOfTheDuelist extends Quest
{
	public final int SEDRICK = 30849;
	public final int ATHEBALDT = 30691;
	public final int FELIX = 31277;
	public final int RIFKEN = 34268;
	public final int KAIN_VAN_HALTER = 31639;
	public final int TABLET_OF_VISION_1 = 31646;
	public final int TABLET_OF_VISION_2 = 31649;
	public final int TABLET_OF_VISION_3 = 31652;
	public final int TABLET_OF_VISION_4 = 31654;
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
	public final int WATCHER_OF_ANCIENT_PLEDGE = 27216;
	public final int ANCIENT_SWORD_MASTER = 27289;
	public final int HALISHA_ARCHON = 27219;
	public final int FALLEN_ANGEL_METELLUS = 27281;
	// items
	public final int ICE_CRYSTAL_FRAGMENT = 49807;
	public final int HALISHA_BADGE = 7488;
	public final int AMULET_REZONANSA_PERVIY = 7271;
	public final int AMULET_REZONANSA_VTOROI = 7302;
	public final int AMULET_REZONANSA_TRETIY = 7333;
	public final int AMULET_REZONANSA_CHETVERTIY = 7364;
	public final int MULTIPLE_COURSE_MEAL = 7537;
	// reward
	public final int BOOKGOLDLION = 90038;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q00073_SagaOfTheDuelist()
	{
		super(73);
		addStartNpc(SEDRICK);
		addTalkId(SEDRICK, ATHEBALDT, FELIX, RIFKEN, KAIN_VAN_HALTER, TABLET_OF_VISION_1, TABLET_OF_VISION_2, TABLET_OF_VISION_3, TABLET_OF_VISION_4);
		addKillId(ICE_MONSTER, SPIRIT_OF_A_DROWNED, SOUL_OF_COLD, GHOST_OF_SOLITUDE, FIEND_OF_COLD);
		addKillId(SPIRIT_OF_COLD, SPAMPLAND_WATCHMAN, FLAME_DRAKE, FIERY_IFRIT, IKEDIT);
		addKillId(WATCHER_OF_ANCIENT_PLEDGE, ANCIENT_SWORD_MASTER, HALISHA_ARCHON, FALLEN_ANGEL_METELLUS);
		registerQuestItems(MULTIPLE_COURSE_MEAL, ICE_CRYSTAL_FRAGMENT, HALISHA_BADGE);
		addCondMinLevel(76, "30849-nolvl.htm");
		addCondClassId(ClassId.GLADIATOR, "30849-checkclass.htm");
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
			case "30849-02a.htm":
				htmltext = "30849-03.htm";
				break;
			case "30849-01a.htm":
				if (st.getCond() == 0)
				{
					st.startQuest();
					st.setCond(1);
				}
				break;
			case "30691-01.htm":
				if (st.getCond() == 1)
				{
					st.setCond(2);
				}
				break;
			case "34268-01.htm":
				if (st.getCond() == 2)
				{
					st.setCond(3);
				}
				break;
			case "34268-03.htm":
				if (st.getCond() == 4)
				{
					st.setCond(5);
				}
				takeItems(player, ICE_CRYSTAL_FRAGMENT, -1);
				giveItems(player, MULTIPLE_COURSE_MEAL, 1);
				break;
			case "30691-04.htm":
				if (st.getCond() == 5)
				{
					st.setCond(6);
				}
				giveItems(player, AMULET_REZONANSA_PERVIY, 1);
				takeItems(player, MULTIPLE_COURSE_MEAL, -1);
				break;
			case "31646-01.htm":
				if (st.getCond() == 6)
				{
					st.setCond(7);
				}
				break;
			case "31649-01.htm":
				if (st.getCond() == 8)
				{
					addSpawn(ANCIENT_SWORD_MASTER, npc, true, 0, true);
					st.setCond(9);
				}
				break;
			case "31649-04.htm":
				if (st.getCond() == 10)
				{
					st.setCond(11);
				}
				break;
			case "31277-01.htm":
				if (st.getCond() == 11)
				{
					st.setCond(12);
				}
				break;
			case "31652-01.htm":
				if (st.getCond() == 14)
				{
					st.setCond(15);
				}
				break;
			case "31654-01.htm":
				if (st.getCond() == 15)
				{
					st.setCond(16);
				}
				addSpawn(FALLEN_ANGEL_METELLUS, npc, true, 0, true);
				break;
			case "31639-01.htm":
				if (st.getCond() == 16)
				{
					st.setCond(17);
				}
				giveItems(player, AMULET_REZONANSA_CHETVERTIY, 1);
				break;
			case "31654-03.htm":
				if (st.getCond() == 17)
				{
					st.setCond(18);
				}
				break;
			case "30849-05.htm":
				if (st.getCond() == 18)
				{
					if ((player.getLevel() < 76) && (player.getBaseClass() != 2))
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
					player.setClassId(88);
					player.setBaseClass(88);
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
				if (npc.getId() == SEDRICK)
				{
					htmltext = "30849.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == SEDRICK)
				{
					if (qs.getCond() == 1)
					{
						htmltext = "30849-01a.htm";
					}
					if (qs.getCond() == 18)
					{
						htmltext = "30849-04.htm";
					}
					
				}
				if (npc.getId() == ATHEBALDT)
				{
					if (qs.getCond() == 1)
					{
						htmltext = "30691.htm";
					}
					if (qs.getCond() == 2)
					{
						htmltext = "30691-01.htm";
					}
					if (qs.getCond() == 5)
					{
						htmltext = "30691-03.htm";
					}
					if (qs.getCond() == 6)
					{
						htmltext = "30691-04.htm";
					}
				}
				if (npc.getId() == FELIX)
				{
					if (qs.getCond() == 11)
					{
						htmltext = "31277.htm";
					}
					if (qs.getCond() == 12)
					{
						htmltext = "31277-01.htm";
					}
				}
				if (npc.getId() == RIFKEN)
				{
					if (qs.getCond() == 2)
					{
						htmltext = "34268.htm";
					}
					if (qs.getCond() == 3)
					{
						htmltext = "34268-01.htm";
					}
					if (qs.getCond() == 4)
					{
						htmltext = "34268-02.htm";
					}
				}
				if (npc.getId() == TABLET_OF_VISION_1)
				{
					if (qs.getCond() == 6)
					{
						htmltext = "31646.htm";
					}
					if (qs.getCond() == 7)
					{
						htmltext = "31646-01.htm";
					}
				}
				if (npc.getId() == TABLET_OF_VISION_2)
				{
					if (qs.getCond() == 8)
					{
						htmltext = "31649.htm";
					}
					if (qs.getCond() == 9)
					{
						htmltext = "31649-02.htm";
					}
					if (qs.getCond() == 10)
					{
						htmltext = "31649-03.htm";
					}
					if (qs.getCond() == 11)
					{
						htmltext = "31649-04.htm";
					}
				}
				if (npc.getId() == TABLET_OF_VISION_3)
				{
					if (qs.getCond() == 14)
					{
						htmltext = "31652.htm";
					}
					if (qs.getCond() == 15)
					{
						htmltext = "31652-01.htm";
					}
				}
				if (npc.getId() == TABLET_OF_VISION_4)
				{
					if (qs.getCond() == 15)
					{
						htmltext = "31654.htm";
					}
					if (qs.getCond() == 17)
					{
						htmltext = "31654-02.htm";
					}
					if (qs.getCond() == 18)
					{
						htmltext = "31654-03.htm";
					}
					
				}
				if (npc.getId() == KAIN_VAN_HALTER)
				{
					if (qs.getCond() == 16)
					{
						htmltext = "31639.htm";
					}
					if (qs.getCond() == 17)
					{
						htmltext = "31639-01.htm";
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
				case ANCIENT_SWORD_MASTER:
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
				case FALLEN_ANGEL_METELLUS:
					if (qs.getCond() == 16)
					{
						addSpawn(KAIN_VAN_HALTER, npc, true, 20000, true);
					}
					break;
			}
			
			if (qs.isCond(7))
			{
				int count = qs.getInt(KILL_COUNT_VAR);
				if (npc.getId() == WATCHER_OF_ANCIENT_PLEDGE)
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
				holder.add(new NpcLogListHolder(WATCHER_OF_ANCIENT_PLEDGE, false, guardiancount));
			}
			return holder;
		}
		return super.getNpcLogList(player);
	}
}