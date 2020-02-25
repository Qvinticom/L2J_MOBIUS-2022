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
package quests.Q10440_TheSealOfPunishmentTheFields;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * The Seal of Punishment: The Fields (10440)
 * @author Stayway
 */
public class Q10440_TheSealOfPunishmentTheFields extends Quest
{
	// NPCs
	private static final int HELVETICA = 32641;
	private static final int ATHENIA = 32643;
	// Monsters
	private static final int MUCROKIAN_FANATIC = 22650;
	private static final int MUCROKIAN_ASCETIC = 22651;
	private static final int MUCROKIAN_SAVIOR = 22652;
	private static final int MUCROKIAN_PROPHET = 22653;
	private static final int CONTAMINATED_MUCROKIAN = 22654;
	private static final int AWAKENED_MUCROKIAN = 22655;
	// Misc
	private static final String KILL_COUNT_VAR = "KillCounts";
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	
	public Q10440_TheSealOfPunishmentTheFields()
	{
		super(10440);
		addStartNpc(HELVETICA, ATHENIA);
		addTalkId(HELVETICA, ATHENIA);
		addKillId(MUCROKIAN_FANATIC, MUCROKIAN_ASCETIC, MUCROKIAN_SAVIOR, MUCROKIAN_PROPHET, CONTAMINATED_MUCROKIAN, AWAKENED_MUCROKIAN);
		addCondMaxLevel(MAX_LEVEL, "noLevel.html");
		addCondMinLevel(MIN_LEVEL, "noLevel.html");
		addCondNotRace(Race.ERTHEIA, "noErtheia.html");
		addCondInCategory(CategoryType.MAGE_GROUP, "noLevel.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		String htmltext = event;
		switch (event)
		{
			case "32641-02.htm":
			case "32641-03.htm":
			case "32643-02.htm":
			case "32643-03.htm":
			{
				htmltext = event;
				break;
			}
			case "32641-04.htm":
			{
				qs.startQuest();
				qs.setMemoState(1);
				qs.set(Integer.toString(MUCROKIAN_FANATIC), 0);
				htmltext = event;
				break;
			}
			case "32643-04.htm":
			{
				qs.startQuest();
				qs.setMemoState(2);
				qs.set(Integer.toString(MUCROKIAN_FANATIC), 0);
				htmltext = event;
				break;
			}
			case "reward_9546":
			case "reward_9547":
			case "reward_9548":
			case "reward_9549":
			case "reward_9550":
			case "reward_9551":
			{
				final int stoneId = Integer.parseInt(event.replaceAll("reward_", ""));
				giveItems(player, stoneId, 15);
				giveStoryQuestReward(player, 60);
				final int count = qs.getInt(KILL_COUNT_VAR);
				if ((count >= 50) && (count < 100))
				{
					addExpAndSp(player, 28240800, 6777);
				}
				else if ((count >= 100) && (count < 200))
				{
					addExpAndSp(player, 56481600, 13554);
				}
				else if ((count >= 200) && (count < 300))
				{
					addExpAndSp(player, 84722400, 20331);
				}
				else if ((count >= 300) && (count < 400))
				{
					addExpAndSp(player, 112963200, 27108);
				}
				else if ((count >= 400) && (count < 500))
				{
					addExpAndSp(player, 141204000, 33835);
				}
				else if ((count >= 500) && (count < 600))
				{
					addExpAndSp(player, 169444800, 40662);
				}
				else if ((count >= 600) && (count < 700))
				{
					addExpAndSp(player, 197685600, 47439);
				}
				else if ((count >= 700) && (count < 800))
				{
					addExpAndSp(player, 225926400, 54216);
				}
				else if ((count >= 800) && (count < 900))
				{
					addExpAndSp(player, 254167200, 60993);
				}
				else if (count >= 900)
				{
					addExpAndSp(player, 282408000, 67770);
				}
				if ((qs.isCond(2)) && (qs.isMemoState(1)))
				{
					htmltext = "32641-07.html";
				}
				else if ((qs.isCond(3)) && (qs.isMemoState(2)))
				{
					htmltext = "32643-07.html";
				}
				qs.exitQuest(false, true);
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
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.getCond() > 0))
		{
			final int count = qs.getInt(KILL_COUNT_VAR) + 1;
			qs.set(KILL_COUNT_VAR, count);
			if ((count >= 50) && (qs.isMemoState(1)))
			{
				qs.setCond(2, true);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if ((count >= 50) && (qs.isMemoState(2)))
			{
				qs.setCond(3, true);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else
			{
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (qs.getCond() > 0))
		{
			final int killCounts = qs.getInt(KILL_COUNT_VAR);
			if (killCounts > 0)
			{
				final Set<NpcLogListHolder> holder = new HashSet<>();
				holder.add(new NpcLogListHolder(NpcStringId.ELIMINATING_THE_MUCROKIANS, killCounts));
				return holder;
			}
		}
		return super.getNpcLogList(player);
	}
}