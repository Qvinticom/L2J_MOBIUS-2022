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
package quests.Q10881_ForThePride;

import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10879_ExaltedGuideToPower.Q10879_ExaltedGuideToPower;

/**
 * For the Pride (10881)
 * @URL https://l2wiki.com/For_the_Pride
 * @author CostyKiller
 */
public class Q10881_ForThePride extends Quest
{
	private static final int DINFORD = 34236;
	private static final int LOGART_VAN_DYKE = 34235;
	
	public Q10881_ForThePride()
	{
		super(10881);
		addStartNpc(DINFORD);
		addTalkId(DINFORD, LOGART_VAN_DYKE);
		addCondMinLevel(MIN_LEVEL, "34236-00.htm");
		addCondStartedQuest(Q10879_ExaltedGuideToPower.class.getSimpleName(), "34236-00.htm");
	}
	
	// Items
	private static final int LOGART_VAN_DYKE_CERTIFICATE = 47836;
	private static final int MOTHER_TREE_GUARDIAN_CERTIFICATION = 47844;
	private static final int UNWORLDLY_VISITORS_CERTIFICATION = 47845;
	private static final int KINGDOM_ROYAL_GUARD_CERTIFICATION = 47846;
	private static final ItemHolder LIONEL_HUNTERS_LIST_PART_5 = new ItemHolder(47834, 1);
	private static final int SPELLBOOK_VITALITY_OF_THE_EXALTED = 47855;
	// Misc
	private static final int MIN_LEVEL = 104;
	
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
			case "34326-02.htm":
			case "34326-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34326-04.htm":
			{
				if (hasItem(player, LIONEL_HUNTERS_LIST_PART_5))
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "34325-07.html":
			{
				if (qs.isCond(1) && (!(hasQuestItems(player, MOTHER_TREE_GUARDIAN_CERTIFICATION, UNWORLDLY_VISITORS_CERTIFICATION)) || !(hasQuestItems(player, MOTHER_TREE_GUARDIAN_CERTIFICATION, KINGDOM_ROYAL_GUARD_CERTIFICATION)) || !(hasQuestItems(player, UNWORLDLY_VISITORS_CERTIFICATION, KINGDOM_ROYAL_GUARD_CERTIFICATION))))
				{
					htmltext = event;
				}
				else
				{
					qs.setCond(2);
					htmltext = "34325-08.htm";
				}
				break;
			}
			case "34325-09.html":
			{
				if (qs.isCond(2))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						giveItems(player, LOGART_VAN_DYKE_CERTIFICATE, 1);
						giveItems(player, SPELLBOOK_VITALITY_OF_THE_EXALTED, 1);
						qs.exitQuest(false, true);
						
						final Quest mainQ = QuestManager.getInstance().getQuest(Q10879_ExaltedGuideToPower.class.getSimpleName());
						if (mainQ != null)
						{
							mainQ.notifyEvent("SUBQUEST_FINISHED_NOTIFY", npc, player);
						}
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
					break;
				}
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
				if ((npc.getId() == DINFORD) && (hasItem(player, LIONEL_HUNTERS_LIST_PART_5)))
				{
					htmltext = "34326-01.htm";
				}
				else
				{
					htmltext = "noItem.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case DINFORD:
					{
						if (qs.isCond(1) && ((hasQuestItems(player, MOTHER_TREE_GUARDIAN_CERTIFICATION, UNWORLDLY_VISITORS_CERTIFICATION)) || (hasQuestItems(player, MOTHER_TREE_GUARDIAN_CERTIFICATION, KINGDOM_ROYAL_GUARD_CERTIFICATION)) || (hasQuestItems(player, UNWORLDLY_VISITORS_CERTIFICATION, KINGDOM_ROYAL_GUARD_CERTIFICATION))))
						{
							htmltext = "34326-05.html";
						}
						break;
					}
					case LOGART_VAN_DYKE:
					{
						if (qs.isCond(1))
						{
							htmltext = "34325-06.htm";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34325-08.html";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
}
