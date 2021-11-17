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
package quests.Q10323_TrainLikeItsReal;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

import quests.Q10322_SearchingForTheMysteriousPower.Q10322_SearchingForTheMysteriousPower;

/**
 * Train Like Its Real (10323)
 * @author Sdw, Gladicek
 */
public class Q10323_TrainLikeItsReal extends Quest
{
	// NPCs
	private static final int EVAIN = 33464;
	private static final int HOLDEN = 33194;
	private static final int SHANNON = 32974;
	private static final int TRAINING_GOLEM = 27532;
	// Items
	private static final ItemHolder SPIRITSHOTS = new ItemHolder(2509, 500);
	private static final ItemHolder SOULSHOTS = new ItemHolder(1835, 500);
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10323_TrainLikeItsReal()
	{
		super(10323);
		addStartNpc(EVAIN);
		addTalkId(HOLDEN, EVAIN, SHANNON);
		addKillId(TRAINING_GOLEM);
		addCondMaxLevel(MAX_LEVEL, "33464-05.html");
		addCondCompletedQuest(Q10322_SearchingForTheMysteriousPower.class.getSimpleName(), "33464-05.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33464-02.htm":
			case "33194-02.html":
			{
				htmltext = event;
				break;
			}
			case "33464-03.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33194-03.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "33194-05.html":
			{
				if (qs.isCond(3))
				{
					qs.setMemoState(0);
					player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_003_bullet_01.htm", TutorialShowHtml.LARGE_WINDOW));
					if (player.isMageClass())
					{
						giveItems(player, SPIRITSHOTS);
						showOnScreenMsg(player, NpcStringId.SPIRITSHOT_HAVE_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
						getTimers().addTimer("SHOW_SCREENMSG", 4500, evnt -> showOnScreenMsg(player, NpcStringId.AUTOMATE_SPIRITSHOT_AS_SHOWN_IN_THE_TUTORIAL, ExShowScreenMessage.TOP_CENTER, 4500));
						qs.setCond(5, true);
					}
					else
					{
						giveItems(player, SOULSHOTS);
						showOnScreenMsg(player, NpcStringId.SOULSHOT_HAVE_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
						getTimers().addTimer("SHOW_SCREENMSG", 4500, evnt -> showOnScreenMsg(player, NpcStringId.AUTOMATE_SOULSHOT_AS_SHOWN_IN_THE_TUTORIAL, ExShowScreenMessage.TOP_CENTER, 4500));
						qs.setCond(4, true);
					}
					htmltext = event;
				}
				break;
			}
			case "33194-08.html":
			{
				if (qs.isCond(8))
				{
					qs.setCond(9, true);
					htmltext = event;
				}
				break;
			}
			case "32974-02.html":
			{
				if (qs.isCond(9))
				{
					addExpAndSp(player, 1700, 5);
					giveAdena(player, 90, true);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == EVAIN)
				{
					htmltext = "33464-01.htm";
				}
				else if (npc.getId() == SHANNON)
				{
					htmltext = "32974-03.html";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == EVAIN)
				{
					htmltext = "33464-03.html";
				}
				else if (npc.getId() == HOLDEN)
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "33194-01.html";
							break;
						}
						case 3:
						{
							htmltext = "33194-04.html";
							break;
						}
						case 4:
						{
							qs.setCond(6, true);
							htmltext = "33194-06.html";
							break;
						}
						case 5:
						{
							qs.setCond(7, true);
							htmltext = "33194-06.html";
							break;
						}
						case 8:
						{
							htmltext = "33194-07.html";
							break;
						}
					}
				}
				else if ((npc.getId() == SHANNON) && qs.isCond(9))
				{
					htmltext = "32974-01.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == EVAIN)
				{
					htmltext = "33464-04.html";
				}
				else if (npc.getId() == SHANNON)
				{
					htmltext = "32974-05.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isStarted())
		{
			if (qs.isCond(2))
			{
				qs.setMemoState(1);
				qs.setCond(3, true);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if (qs.isCond(6) || qs.isCond(7))
			{
				qs.setMemoState(1);
				qs.setCond(8, true);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs != null)
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(1);
			if (qs.isCond(2))
			{
				npcLogList.add(new NpcLogListHolder(NpcStringId.ELIMINATE_THE_TRAINING_GOLEM, qs.getMemoState()));
			}
			else if (qs.isCond(6) || qs.isCond(7))
			{
				npcLogList.add(new NpcLogListHolder(NpcStringId.ELIMINATE_THE_TRAINING_GOLEM_2, qs.getMemoState()));
			}
			return npcLogList;
		}
		return super.getNpcLogList(player);
	}
}
