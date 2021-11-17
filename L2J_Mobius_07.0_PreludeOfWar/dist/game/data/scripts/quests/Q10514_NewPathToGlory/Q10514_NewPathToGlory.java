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
package quests.Q10514_NewPathToGlory;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.util.Util;

import quests.Q10873_ExaltedReachingAnotherLevel.Q10873_ExaltedReachingAnotherLevel;

/**
 * New Path To Glory (10514)
 * @URL https://l2wiki.com/New_Path_to_Glory
 * @author CostyKiller
 */
public class Q10514_NewPathToGlory extends Quest
{
	// NPC
	private static final int HIGH_PRIEST_SYLVAIN = 30070;
	private static final int[] MONSTERS =
	{
		// Atelia Refinery
		24150, // Devil Warrior
		24149, // Devil Nightmare
		24153, // Devil Varos
		24152, // Devil Sinist
		24151, // Devil Guardian
		24144, // Death Rogue
		24145, // Death Shooter
		24146, // Death Warrior
		24147, // Death Sorcerer
		24148, // Death Pondus
		24155, // Demonic Warrior
		24154, // Demonic Wizard
		24158, // Demonic Weiss
		24157, // Demonic Keras
		24156, // Demonic Archer
		
		// Ivory Tower Crater
		24421, // Stone Gargoyle
		24422, // Stone Golem
		24423, // Monster Eye
		24424, // Gargoyle Hunter
		24425, // Steel Golem
		24426, // Stone Cube
		
		// Alligator Island
		24372, // Crokian Lad
		24373, // Dailaon Lad
		24375, // Farhite Lad
		24376, // Nos Lad
		24377, // Swamp Tribe
		24378, // Swamp Alligator
		24379, // Swamp Warrior
		
		// Tanor Canyon
		20936, // Tanor Silenos -->
		20937, // Tanor Silenos Solider -->
		20938, // Tanor Silenos Scout -->
		20939, // Tanor Silenos Warrior -->
		20941, // Tanor Silenos Chieftain -->
		20942, // Nightmare Guide -->
		20943, // Nightmare Watchman -->
		20944 // Nightmare Lord -->
	};
	// Items
	private static final int PROOF_OF_REPUTATION = 80826;
	private static final ItemHolder LIONEL_HUNTERS_LIST_PART_4 = new ItemHolder(47829, 1);
	// Rewards
	private static final int SYLVAIN_CERTIFICATE = 80828;
	// Misc
	private static final int MIN_LEVEL = 103;
	private static final int PROOF_OF_REPUTATION_NEEDED = 80000;
	
	public Q10514_NewPathToGlory()
	{
		super(10514);
		addStartNpc(HIGH_PRIEST_SYLVAIN);
		addTalkId(HIGH_PRIEST_SYLVAIN);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "30070-00.html");
		addCondStartedQuest(Q10873_ExaltedReachingAnotherLevel.class.getSimpleName(), "30070-00.html");
		registerQuestItems(PROOF_OF_REPUTATION);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		switch (event)
		{
			case "30070-02.htm":
			case "30070-03.htm":
			case "30070-04.htm":
			{
				htmltext = event;
				break;
			}
			case "30070-05.html":
			{
				if (hasItem(player, LIONEL_HUNTERS_LIST_PART_4))
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "30070-08.html":
			{
				if (qs.isCond(2) && (getQuestItemsCount(player, PROOF_OF_REPUTATION) >= PROOF_OF_REPUTATION_NEEDED))
				{
					takeItems(player, PROOF_OF_REPUTATION, PROOF_OF_REPUTATION_NEEDED);
					giveItems(player, SYLVAIN_CERTIFICATE, 1);
					addExpAndSp(player, 3480527972686L, 0);
					qs.exitQuest(false, true);
					
					final Quest mainQ = QuestManager.getInstance().getQuest(Q10873_ExaltedReachingAnotherLevel.class.getSimpleName());
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
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		final QuestState mainQs = player.getQuestState("Q10873_ExaltedReachingAnotherLevel");
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (hasItem(player, LIONEL_HUNTERS_LIST_PART_4) && mainQs.isMemoState(2))
				{
					htmltext = "30070-01.htm";
				}
				else
				{
					htmltext = "30070-00.html";
				}
				break;
			}
			case State.STARTED:
			{
				if (getQuestItemsCount(player, PROOF_OF_REPUTATION) >= PROOF_OF_REPUTATION_NEEDED)
				{
					htmltext = "30070-07.html";
				}
				else
				{
					htmltext = "30070-06.html";
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
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, player, false))
		{
			if ((getQuestItemsCount(player, PROOF_OF_REPUTATION) < PROOF_OF_REPUTATION_NEEDED) && (getRandom(100) < 90))
			{
				giveItems(player, PROOF_OF_REPUTATION, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if ((getQuestItemsCount(player, PROOF_OF_REPUTATION) >= PROOF_OF_REPUTATION_NEEDED) && (player.getLevel() >= MIN_LEVEL))
			{
				qs.setCond(2, true);
			}
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
}
