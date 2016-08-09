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
package quests.Q00144_PailakaInjuredDragon;

import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.instancezone.Instance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

/**
 * @author Mathael
 */
public class Q00144_PailakaInjuredDragon extends Quest
{
	// NPCs
	private static final int KETRA_ORC_SHAMAN = 32499;
	private static final int KETRA_ORC_SUPPORTER = 32502;
	private static final int KETRA_ORC_SUPPORTER_END = 32512;
	private static final int KETRA_ORC_INTELLIGENCE_OFFICIER = 32509;
	// Monsters
	private static final int LATANA = 18660;
	private static final int[] MONSTERS =
	{
		18635,
		18636,
		18642,
		18646,
		18649,
		18650,
		18653,
		18654,
		18655,
		18657,
		18659
	};
	// Buffs
	private static final SkillHolder[] BUFFS =
	{
		new SkillHolder(1086, 2),
		new SkillHolder(1204, 2),
		new SkillHolder(1059, 3),
		new SkillHolder(1085, 3),
		new SkillHolder(1078, 6),
		new SkillHolder(1068, 3),
		new SkillHolder(1240, 3),
		new SkillHolder(1077, 3),
		new SkillHolder(1242, 3),
		new SkillHolder(1062, 2),
		new SkillHolder(1268, 4),
		new SkillHolder(1045, 6),
	};
	// Quest Items
	private static final int SPEAR_OF_SILENOS = 13052;
	private static final int SPEAR_OF_SILENOS_REINFORCED = 13053;
	private static final int SPEAR_OF_SILENOS_COMPLETED = 13054;
	private static final int WEAPON_UPGRADE_STAGE_1 = 13056;
	private static final int WEAPON_UPGRADE_STAGE_2 = 13057;
	// Usable Quest Items
	private static final int SHIELD_POTION = 13032;
	private static final int HEAL_POTION = 13033;
	// Rewards
	private static final long REWARD_EXP = 24570000;
	private static final int REWARD_SP = 5896;
	private static final int REWARD_PAILAKA_SHIRT = 13296;
	private static final int REWARD_ADENA = 798840;
	private static final int SCROLL_OF_ESCAPE = 736;
	// Misc
	private static final int MIN_LEVEL = 73;
	private static final int MAX_LEVEL = 77;
	private boolean WEAPON_UPGRADE_STAGE_1_DROPED = false;
	private boolean WEAPON_UPGRADE_STAGE_2_DROPED = false;
	private int BUFF_COUNT = 0;
	
	public Q00144_PailakaInjuredDragon()
	{
		super(144);
		addStartNpc(KETRA_ORC_SHAMAN);
		addFirstTalkId(KETRA_ORC_SUPPORTER_END);
		addTalkId(KETRA_ORC_SHAMAN, KETRA_ORC_SUPPORTER, KETRA_ORC_INTELLIGENCE_OFFICIER, KETRA_ORC_SUPPORTER_END);
		addKillId(LATANA);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "32499-03.html");
		addCondMaxLevel(MAX_LEVEL, "32499-04z.html");
		registerQuestItems(SPEAR_OF_SILENOS, SPEAR_OF_SILENOS_REINFORCED, SPEAR_OF_SILENOS_COMPLETED, WEAPON_UPGRADE_STAGE_1, WEAPON_UPGRADE_STAGE_2, HEAL_POTION, SHIELD_POTION);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		if (qs == null)
		{
			return htmltext;
		}
		
		final String request = event.contains(" ") ? event.substring(0, event.indexOf(" ")) : event;
		switch (request)
		{
			case "32499-04.htm":
			case "32499-05.htm":
			case "32499-06.htm":
			case "32499-08a.html":
			case "32499-08.htm":
			case "32499-09.htm":
			case "32502-01.html":
			case "32502-02.html":
			case "32502-03.html":
			case "32502-04.html":
			case "32502-07.html":
			{
				htmltext = event;
				break;
			}
			case "32499-07.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32502-05.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
					giveItems(player, SPEAR_OF_SILENOS, 1, true);
					htmltext = event;
				}
				break;
			}
			case "32512-02.html":
			{
				final Instance inst = InstanceManager.getInstance().getPlayerInstance(player, true);
				if ((inst != null) && qs.isCond(4))
				{
					takeItems(player, SPEAR_OF_SILENOS_COMPLETED, -1);
					rewardItems(player, 57, REWARD_ADENA);
					rewardItems(player, REWARD_PAILAKA_SHIRT, 1);
					addExpAndSp(player, REWARD_EXP, REWARD_SP);
					giveItems(player, SCROLL_OF_ESCAPE, 1); // Not a reward.
					qs.exitQuest(false, true);
					inst.finishInstance();
					htmltext = event;
				}
				break;
			}
			case "upgrade_weapon":
			{
				if (qs.isCond(3) || qs.isCond(4))
				{
					if (hasQuestItems(player, SPEAR_OF_SILENOS_COMPLETED))
					{
						htmltext = "32509-06.html";
					}
					else if (hasQuestItems(player, SPEAR_OF_SILENOS))
					{
						if (hasQuestItems(player, WEAPON_UPGRADE_STAGE_1))
						{
							takeItems(player, SPEAR_OF_SILENOS, -1);
							takeItems(player, WEAPON_UPGRADE_STAGE_1, -1);
							giveItems(player, SPEAR_OF_SILENOS_REINFORCED, 1, true);
							htmltext = "32509-02.html";
						}
						else
						{
							htmltext = "32509-04.html";
						}
					}
					else if (hasQuestItems(player, SPEAR_OF_SILENOS_REINFORCED))
					{
						if (hasQuestItems(player, WEAPON_UPGRADE_STAGE_2))
						{
							takeItems(player, SPEAR_OF_SILENOS_REINFORCED, -1);
							takeItems(player, WEAPON_UPGRADE_STAGE_2, -1);
							giveItems(player, SPEAR_OF_SILENOS_COMPLETED, 1, true);
							htmltext = "32509-08.html";
						}
						else
						{
							htmltext = "32509-04.html";
						}
					}
				}
				else
				{
					htmltext = "32509-01a.html";
				}
				break;
			}
			case "enhancement_page":
			{
				htmltext = BUFF_COUNT < 5 ? "32509-10.html" : "32509-07.html";
				break;
			}
			case "enhancement":
			{
				if (BUFF_COUNT < 5)
				{
					final int key = Integer.parseInt(event.substring(request.length() + 1)) - 1;
					BUFFS[key].getSkill().applyEffects(npc, player);
					BUFF_COUNT++;
					if (BUFF_COUNT < 5)
					{
						htmltext = "32509-09.html";
					}
					else
					{
						htmltext = "32509-08.html";
					}
				}
				else
				{
					htmltext = "32509-07.html";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);
		final QuestState qs = getQuestState(talker, true);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (npc.getId())
		{
			case KETRA_ORC_SHAMAN:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = "32499-01.htm";
						break;
					}
					case State.COMPLETED:
					{
						htmltext = "32499-02.html";
						break;
					}
					case State.STARTED:
					{
						htmltext = "32499-10.html";
						break;
					}
				}
				break;
			}
			case KETRA_ORC_SUPPORTER:
			{
				switch (qs.getCond())
				{
					case 3:
					{
						htmltext = "32502-07.html";
						break;
					}
					case 4:
					{
						htmltext = "32502-06.html";
						break;
					}
					default:
					{
						htmltext = "32502-01.html";
						break;
					}
				}
				break;
			}
			case KETRA_ORC_INTELLIGENCE_OFFICIER:
			{
				htmltext = !qs.isCond(3) && !qs.isCond(4) ? "32509-01a.html" : "32509-01.html";
				break;
			}
			case KETRA_ORC_SUPPORTER_END:
			{
				htmltext = hasQuestItems(talker, SPEAR_OF_SILENOS_COMPLETED) ? "32512-01.html" : "32512-03.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		return qs.getState() == State.COMPLETED ? "32512-03.html" : "32512-01.html";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final int npcId = npc.getId();
		if (npcId != LATANA)
		{
			if (!WEAPON_UPGRADE_STAGE_1_DROPED && !hasQuestItems(killer, WEAPON_UPGRADE_STAGE_1) && hasQuestItems(killer, SPEAR_OF_SILENOS))
			{
				if (getRandom(1, 6) > 2)
				{
					giveItems(killer, WEAPON_UPGRADE_STAGE_1, 1, true);
					WEAPON_UPGRADE_STAGE_1_DROPED = true;
				}
			}
			if (!WEAPON_UPGRADE_STAGE_2_DROPED && !hasQuestItems(killer, WEAPON_UPGRADE_STAGE_2) && hasQuestItems(killer, SPEAR_OF_SILENOS_REINFORCED))
			{
				if (getRandom(1, 6) > 4)
				{
					giveItems(killer, WEAPON_UPGRADE_STAGE_2, 1, true);
					WEAPON_UPGRADE_STAGE_2_DROPED = true;
				}
			}
		}
		else
		{
			final QuestState qs = getQuestState(killer, false);
			if (qs != null)
			{
				qs.setCond(4, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
