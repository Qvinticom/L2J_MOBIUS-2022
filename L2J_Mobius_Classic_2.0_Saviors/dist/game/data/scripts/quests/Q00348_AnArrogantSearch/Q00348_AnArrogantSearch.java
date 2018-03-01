package quests.Q00348_AnArrogantSearch;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.RadarControl;

//SanyaDC

public class Q00348_AnArrogantSearch extends Quest
{
	public final int HANELLIN = 30864;
	public final int CLAUDIA_ATHEBALT = 31001;
	public final int TABLE_OF_VISION = 31646;
	
	// mobs
	public final int CRIMSON_DRAKE = 20670;
	public final int KADIOS = 20671;
	public final int PLATINUM_TRIBE_SHAMAN = 20828;
	public final int PLATINUM_TRIBE_PREFECT = 20829;
	public final int GUARDIAN_ANGEL = 20830;
	public final int SEAL_ANGEL = 20831;
	public final int STONE_WATCHMAN_EZEKIEL = 27296;
	
	// Items
	public final int SHELL_OF_MONSTERS = 14857;
	public final int BOOK_OF_SAINT = 4397;
	public final int HEALING_POTION = 1061;
	public final int WHITE_CLOTH_PLATINUM = 4294;
	public final int WHITE_CLOTH_ANGLE = 4400;
	private static final int BLOODED_FABRIC = 4295;
	
	public Q00348_AnArrogantSearch()
	{
		super(348);
		addStartNpc(HANELLIN);
		addTalkId(HANELLIN, CLAUDIA_ATHEBALT, TABLE_OF_VISION);
		addKillId(CRIMSON_DRAKE, KADIOS, PLATINUM_TRIBE_SHAMAN, PLATINUM_TRIBE_PREFECT, GUARDIAN_ANGEL, SEAL_ANGEL, STONE_WATCHMAN_EZEKIEL);
		registerQuestItems(SHELL_OF_MONSTERS, BOOK_OF_SAINT, HEALING_POTION, WHITE_CLOTH_PLATINUM, WHITE_CLOTH_ANGLE);
		addCondMinLevel(60, "lvl.htm");
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
			case "30864.htm":
			case "30864-01.htm":
			case "30864-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30864-03.htm":
			{
				if (player.getLevel() >= 60)
				{
					st.startQuest();
					st.setCond(2);
				}
				break;
			}
			case "30864-04.htm":
			{
				if (st.getCond() == 3)
				{
					st.setCond(4);
					takeItems(player, SHELL_OF_MONSTERS, -1);
				}
				break;
			}
			case "30864-05.htm":
			{
				if (st.getCond() == 4)
				{
					st.setCond(5);
				}
				break;
			}
			case "31001-01.htm":
			{
				if (st.getCond() == 5)
				{
					addRadar(player, 120112, 30912, -3616);
				}
				break;
			}
			case "31646-01.htm":
			{
				if (st.getCond() == 5)
				{
					addSpawn(STONE_WATCHMAN_EZEKIEL, npc, true, 0, true);
				}
				st.getPlayer().sendPacket(new RadarControl(2, 2, 0, 0, 0));
				break;
			}
			case "30864-06.htm":
			{
				if (st.getCond() == 6)
				{
					st.setCond(7);
				}
				break;
			}
			case "30864-07.htm":
			{
				if (st.getCond() == 7)
				{
					takeItems(player, HEALING_POTION, 1);
				}
				st.setCond(8);
				break;
			}
			case "30864-08.htm":
			{
				if (st.getCond() == 7)
				{
					takeItems(player, HEALING_POTION, 1);
				}
				st.setCond(9);
				break;
			}
			case "end.htm":
			{
				if ((st.getCond() == 10) || (st.getCond() == 11))
				{
					takeItems(player, WHITE_CLOTH_PLATINUM, -1);
					takeItems(player, WHITE_CLOTH_ANGLE, -1);
					rewardItems(player, BLOODED_FABRIC, 1);
					st.exitQuest(true, true);
				}
				break;
			}
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
				if (npc.getId() == HANELLIN)
				{
					htmltext = "30864.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == HANELLIN)
				{
					switch (qs.getCond())
					{
						case 2:
							htmltext = "30864-09.htm";
							break;
						case 3:
							htmltext = "30864-10.htm";
							break;
						case 4:
							htmltext = "30864-04.htm";
							break;
						case 5:
							htmltext = "30864-05.htm";
							break;
						case 6:
							htmltext = "30864-11.htm";
							break;
						case 7:
							if ((qs.getCond() == 7) && (getQuestItemsCount(talker, HEALING_POTION) > 0))
							{
								htmltext = "30864-12.htm";
							}
							else
							{
								htmltext = "noz.htm";
							}
							break;
						case 9:
							htmltext = "30864-07.htm";
							break;
						case 10:
							htmltext = "30864-13.htm";
							break;
						case 11:
							htmltext = "30864-13.htm";
							break;
					}
				}
				if (npc.getId() == CLAUDIA_ATHEBALT)
				{
					if (qs.getCond() == 5)
					{
						htmltext = "31001.htm";
					}
				}
				if (npc.getId() == TABLE_OF_VISION)
				{
					if (qs.getCond() == 5)
					{
						htmltext = "31646.htm";
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		switch (npc.getId())
		{
			case CRIMSON_DRAKE:
			case KADIOS:
				if (qs.getCond() == 2)
				{
					giveItemRandomly(killer, npc, SHELL_OF_MONSTERS, 1, 1, 50, true);
				}
				qs.setCond(3);
				break;
			case PLATINUM_TRIBE_SHAMAN:
			case PLATINUM_TRIBE_PREFECT:
				
				if (qs.getCond() == 8)
				{
					if (getQuestItemsCount(killer, WHITE_CLOTH_PLATINUM) < 100)
					{
						giveItemRandomly(killer, npc, WHITE_CLOTH_PLATINUM, 1, 100, 50, true);
					}
					if (getQuestItemsCount(killer, WHITE_CLOTH_PLATINUM) >= 100)
					{
						qs.setCond(10);
					}
				}
				break;
			case GUARDIAN_ANGEL:
			case SEAL_ANGEL:
				if (qs.getCond() == 9)
				{
					if (getQuestItemsCount(killer, WHITE_CLOTH_ANGLE) < 1000)
					{
						giveItemRandomly(killer, npc, WHITE_CLOTH_ANGLE, 1, 1000, 50, true);
					}
					if (getQuestItemsCount(killer, WHITE_CLOTH_ANGLE) >= 1000)
					{
						qs.setCond(11);
					}
				}
				break;
			case STONE_WATCHMAN_EZEKIEL:
				if (qs.getCond() == 5)
				{
					giveItems(killer, BOOK_OF_SAINT, 1);
				}
				qs.setCond(6);
				break;
		}
		return null;
	}
}