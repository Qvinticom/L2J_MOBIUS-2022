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
package quests.Q10515_NewWayForPride;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10879_ExaltedGuideToPower.Q10879_ExaltedGuideToPower;

/**
 * New Way For Pride (10515)
 * @URL https://l2wiki.com/New_Way_for_Pride
 * @author CostyKiller
 */
public class Q10515_NewWayForPride extends Quest
{
	// NPCs
	private static final int CARDINAL_SERESIN = 30657;
	// Items
	private static final int SERESIN_CERTIFICATE = 80829;
	private static final int PROOF_OF_PRIDE = 80827;
	private static final ItemHolder LIONEL_HUNTERS_LIST_PART_5 = new ItemHolder(47834, 1);
	// Monsters
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
		20936, // Tanor Silenos
		20937, // Tanor Silenos Solider
		20938, // Tanor Silenos Scout
		20939, // Tanor Silenos Warrior
		20941, // Tanor Silenos Chieftain
		20942, // Nightmare Guide
		20943, // Nightmare Watchman
		20944, // Nightmare Lord
	};
	
	// Misc
	private static final int MIN_LEVEL = 104;
	private static final int PROOF_OF_PRIDE_NEEDED = 80000;
	
	public Q10515_NewWayForPride()
	{
		super(10515);
		addStartNpc(CARDINAL_SERESIN);
		addTalkId(CARDINAL_SERESIN);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "30657-00.html");
		addCondStartedQuest(Q10879_ExaltedGuideToPower.class.getSimpleName(), "30657-00.html");
		registerQuestItems(PROOF_OF_PRIDE);
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
			case "30657-02.htm":
			case "30657-03.htm":
			case "30657-04.htm":
			{
				htmltext = event;
				break;
			}
			case "30657-05.html":
			{
				if (hasItem(player, LIONEL_HUNTERS_LIST_PART_5))
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "30657-08.html":
			{
				if (qs.isCond(2) && (getQuestItemsCount(player, PROOF_OF_PRIDE) >= PROOF_OF_PRIDE_NEEDED))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						takeItems(player, PROOF_OF_PRIDE, PROOF_OF_PRIDE_NEEDED);
						giveItems(player, SERESIN_CERTIFICATE, 1);
						addExpAndSp(player, 3480527972686L, 0);
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
		final QuestState mainQs = player.getQuestState("Q10879_ExaltedGuideToPower");
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if ((npc.getId() == CARDINAL_SERESIN) && (hasItem(player, LIONEL_HUNTERS_LIST_PART_5)) && mainQs.isMemoState(2))
				{
					htmltext = "30657-01.htm";
				}
				else
				{
					htmltext = "30657-00.html";
				}
				break;
			}
			case State.STARTED:
			{
				if (getQuestItemsCount(player, PROOF_OF_PRIDE) >= PROOF_OF_PRIDE_NEEDED)
				{
					htmltext = "30657-07.html";
				}
				else
				{
					htmltext = "30657-06.html";
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
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE) && CommonUtil.contains(MONSTERS, npc.getId()))
		{
			giveItems(player, PROOF_OF_PRIDE, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			
			if (getQuestItemsCount(player, PROOF_OF_PRIDE) >= PROOF_OF_PRIDE_NEEDED)
			{
				qs.setCond(2, true);
			}
		}
	}
}