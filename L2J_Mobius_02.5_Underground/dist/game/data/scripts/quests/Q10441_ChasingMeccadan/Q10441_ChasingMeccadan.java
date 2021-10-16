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
package quests.Q10441_ChasingMeccadan;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;

import quests.Q10440_TheSealOfPunishmentTheFields.Q10440_TheSealOfPunishmentTheFields;

/**
 * Chasing Meccadan (10441)
 * @URL https://l2wiki.com/Chasing_Meccadan
 * @author Gigi
 */
public class Q10441_ChasingMeccadan extends Quest
{
	// NPCs
	private static final int HELVETICA = 32641;
	private static final int ATHENIA = 32643;
	private static final int DIMENSIONAL_DOOR = 33869;
	private static final int AWAKENING_MECCADEN = 27505;
	// Reward
	private static final int EAS = 960;
	// Misc
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	
	public Q10441_ChasingMeccadan()
	{
		super(10441);
		addStartNpc(HELVETICA, ATHENIA);
		addTalkId(HELVETICA, ATHENIA, DIMENSIONAL_DOOR);
		addKillId(AWAKENING_MECCADEN);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "noCondition.htm");
		addCondNotRace(Race.ERTHEIA, "noErtheia.htm");
		addCondInCategory(CategoryType.MAGE_GROUP, "noCondition.htm");
		addCondCompletedQuest(Q10440_TheSealOfPunishmentTheFields.class.getSimpleName(), "noCondition.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32641-02.htm":
			case "32641-03.htm":
			case "32641-07.html":
			case "32643-02.htm":
			case "32643-03.htm":
			case "32643-07.html":
			{
				htmltext = event;
				break;
			}
			case "32641-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32643-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "spawn":
			{
				final Npc maccaden = addSpawn(AWAKENING_MECCADEN, npc.getX() + 80, npc.getY() + 80, npc.getZ(), npc.getHeading(), false, 120000, true);
				maccaden.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.COVETING_THE_POWER_OF_THE_SEAL_HUH_SUCH_COURAGE_SHOULD_BE_REWARDED_WITH_DEATH);
				addAttackPlayerDesire(maccaden, player);
				break;
			}
			case "reward_9546":
			case "reward_9547":
			case "reward_9548":
			case "reward_9549":
			case "reward_9550":
			case "reward_9551":
			{
				if (qs.isCond(2))
				{
					if (npc.getId() == HELVETICA)
					{
						final int stoneId = Integer.parseInt(event.replaceAll("reward_", ""));
						giveItems(player, stoneId, 15);
						giveItems(player, EAS, 2);
						giveStoryQuestReward(player, 30);
						addExpAndSp(player, 14120400, 3388);
						qs.exitQuest(false, true);
						htmltext = "32641-08.html";
					}
					else if (npc.getId() == ATHENIA)
					{
						final int stoneId = Integer.parseInt(event.replaceAll("reward_", ""));
						giveItems(player, stoneId, 15);
						giveItems(player, EAS, 2);
						giveStoryQuestReward(player, 30);
						addExpAndSp(player, 14120400, 3388);
						qs.exitQuest(false, true);
						htmltext = "32643-08.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case HELVETICA:
			{
				if (qs.isCreated())
				{
					htmltext = "32641-01.htm";
				}
				else if (qs.isCond(1))
				{
					htmltext = "32641-05.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "32641-06.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case ATHENIA:
			{
				if (qs.isCreated())
				{
					htmltext = "32643-01.htm";
				}
				else if (qs.isCond(1))
				{
					htmltext = "32643-05.html";
				}
				else if (qs.isCond(3))
				{
					htmltext = "32643-06.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case DIMENSIONAL_DOOR:
			{
				if (qs.isCond(1))
				{
					htmltext = "33869-01.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.FOOLS_YOU_HAVE_ALSO_BEEN_USED_BY_SHILEN_ARGH_EVEN_IF_I_DIE_THE_SEAL_OF_PUNISHMENT_WILL_ARGHHH);
			qs.setCond(2, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}