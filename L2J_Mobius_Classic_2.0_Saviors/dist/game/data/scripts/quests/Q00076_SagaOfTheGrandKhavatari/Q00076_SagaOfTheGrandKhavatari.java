package quests.Q00076_SagaOfTheGrandKhavatari;

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

public class Q00076_SagaOfTheGrandKhavatari extends Quest
{
	public final int MARESTELLA = 30866;
	public final int DUDA_MARA_TOTEM_SPIRIT = 30593;
	public final int RIFKEN = 34268;
	public final int LAZENBY = 30706;
	public final int TABLET_OF_VISION_1 = 31646;
	public final int TABLET_OF_VISION_2 = 31649;
	public final int TABLET_OF_VISION_3 = 31651;
	public final int TABLET_OF_VISION_4 = 31657;
	public final int KHAVATARI_KASHU = 31637;
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
	public final int WATCHER_OF_THE_ANCIENT_PLEDGE = 27216;
	public final int KHAVATARI_URUZ = 27293;
	public final int HALISHA_ARCHON = 27219;
	public final int FLAME_EVIL_SPIRIT_AZIRA = 27284;
	// items
	public final int ICE_CRYSTAL_FRAGMENT = 49810;
	public final int HALISHA_BADGE = 7491;
	public final int AMULET_REZONANSA_PERVIY = 7274;
	public final int AMULET_REZONANSA_VTOROI = 7305;
	public final int AMULET_REZONANSA_TRETIY = 7336;
	public final int AMULET_REZONANSA_CHETVERTIY = 7367;
	public final int BITTER_COLD_VODKA = 49841;
	// reward
	public final int BOOKBLACKBEAR = 90042;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q00076_SagaOfTheGrandKhavatari()
	{
		super(76);
		addStartNpc(MARESTELLA);
		addTalkId(MARESTELLA, DUDA_MARA_TOTEM_SPIRIT, RIFKEN, LAZENBY, KHAVATARI_KASHU, TABLET_OF_VISION_1, TABLET_OF_VISION_2, TABLET_OF_VISION_3, TABLET_OF_VISION_4);
		addKillId(ICE_MONSTER, SPIRIT_OF_A_DROWNED, SOUL_OF_COLD, GHOST_OF_SOLITUDE, FIEND_OF_COLD);
		addKillId(SPIRIT_OF_COLD, SPAMPLAND_WATCHMAN, FLAME_DRAKE, FIERY_IFRIT, IKEDIT);
		addKillId(WATCHER_OF_THE_ANCIENT_PLEDGE, KHAVATARI_URUZ, HALISHA_ARCHON, FLAME_EVIL_SPIRIT_AZIRA);
		registerQuestItems(BITTER_COLD_VODKA, ICE_CRYSTAL_FRAGMENT, HALISHA_BADGE);
		addCondMinLevel(76, "aiken02.htm");
		addCondClassId(ClassId.TYRANT, "aiken03.htm");
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
			case "aiken02a.htm":
				htmltext = "aiken5.htm";
				break;
			case "aiken01s.htm":
				if (st.getCond() == 0)
				{
					st.startQuest();
					st.setCond(1);
				}
				break;
			case "jer2.htm":
				if (st.getCond() == 1)
				{
					st.setCond(2);
				}
				break;
			case "rifken2.htm":
				if (st.getCond() == 2)
				{
					st.setCond(3);
				}
				break;
			case "rifken4.htm":
				if (st.getCond() == 4)
				{
					st.setCond(5);
				}
				takeItems(player, ICE_CRYSTAL_FRAGMENT, -1);
				giveItems(player, BITTER_COLD_VODKA, 1);
				break;
			case "jer4.htm":
				if (st.getCond() == 5)
				{
					st.setCond(6);
				}
				giveItems(player, AMULET_REZONANSA_PERVIY, 1);
				takeItems(player, BITTER_COLD_VODKA, -1);
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
					addSpawn(KHAVATARI_URUZ, npc, true, 0, true);
					st.setCond(9);
				}
				break;
			case "stone25.htm":
				if (st.getCond() == 10)
				{
					st.setCond(11);
				}
				break;
			case "jer6.htm":
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
			case "stone41.htm":
				if (st.getCond() == 15)
				{
					st.setCond(16);
				}
				addSpawn(FLAME_EVIL_SPIRIT_AZIRA, npc, true, 0, true);
				break;
			case "erikrams2.htm":
				if (st.getCond() == 16)
				{
					st.setCond(17);
				}
				giveItems(player, AMULET_REZONANSA_CHETVERTIY, 1);
				break;
			case "stone43.htm":
				if (st.getCond() == 17)
				{
					st.setCond(18);
				}
				break;
			case "aiken7.htm":
				if (st.getCond() == 18)
				{
					if ((player.getLevel() < 76) && (player.getBaseClass() != 48))
					{
						htmltext = "30849-nolvl.htm";
					}
					addExpAndSp(player, 3100000, 103000);
					rewardItems(player, BOOKBLACKBEAR, 1);
					takeItems(player, AMULET_REZONANSA_PERVIY, -1);
					takeItems(player, AMULET_REZONANSA_VTOROI, -1);
					takeItems(player, AMULET_REZONANSA_TRETIY, -1);
					takeItems(player, AMULET_REZONANSA_CHETVERTIY, -1);
					takeItems(player, HALISHA_BADGE, -1);
					st.exitQuest(false, true);
					player.setClassId(114);
					player.setBaseClass(114);
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
				if (npc.getId() == MARESTELLA)
				{
					htmltext = "aiken01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == MARESTELLA)
				{
					if (qs.getCond() == 1)
					{
						htmltext = "aiken01s.htm";
					}
					if (qs.getCond() == 18)
					{
						htmltext = "aiken6.htm";
					}
					
				}
				if (npc.getId() == DUDA_MARA_TOTEM_SPIRIT)
				{
					if (qs.getCond() == 1)
					{
						htmltext = "jer1.htm";
					}
					if (qs.getCond() == 2)
					{
						htmltext = "jer2.htm";
					}
					if (qs.getCond() == 5)
					{
						htmltext = "jer3.htm";
					}
					if (qs.getCond() == 6)
					{
						htmltext = "jer4.htm";
					}
				}
				if (npc.getId() == LAZENBY)
				{
					if (qs.getCond() == 11)
					{
						htmltext = "jer5.htm";
					}
					if (qs.getCond() == 12)
					{
						htmltext = "jer6.htm";
					}
				}
				if (npc.getId() == RIFKEN)
				{
					if (qs.getCond() == 2)
					{
						htmltext = "rifken1.htm";
					}
					if (qs.getCond() == 3)
					{
						htmltext = "rifken2.htm";
					}
					if (qs.getCond() == 4)
					{
						htmltext = "rifken3.htm";
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
					if (qs.getCond() == 11)
					{
						htmltext = "stone25.htm";
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
						htmltext = "stone32.htm";
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
						htmltext = "stone42.htm";
					}
					if (qs.getCond() == 18)
					{
						htmltext = "stone43.htm";
					}
					
				}
				if (npc.getId() == KHAVATARI_KASHU)
				{
					if (qs.getCond() == 16)
					{
						htmltext = "erikrams1.htm";
					}
					if (qs.getCond() == 17)
					{
						htmltext = "erikrams2.htm";
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
				case KHAVATARI_URUZ:
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
				case FLAME_EVIL_SPIRIT_AZIRA:
					if (qs.getCond() == 16)
					{
						addSpawn(KHAVATARI_KASHU, npc, true, 20000, true);
					}
					break;
			}
			
			if (qs.isCond(7))
			{
				int count = qs.getInt(KILL_COUNT_VAR);
				if (npc.getId() == WATCHER_OF_THE_ANCIENT_PLEDGE)
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
				holder.add(new NpcLogListHolder(WATCHER_OF_THE_ANCIENT_PLEDGE, false, guardiancount));
			}
			return holder;
		}
		return super.getNpcLogList(player);
	}
}