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
package quests.Q10824_ConfrontingTheGreatestDanger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10823_ExaltedOneWhoShattersTheLimit.Q10823_ExaltedOneWhoShattersTheLimit;

/**
 * Confronting the Greatest Danger (10824)
 * @URL https://l2wiki.com/Confronting_the_Greatest_Danger
 * @author Mobius
 */
public class Q10824_ConfrontingTheGreatestDanger extends Quest
{
	// NPC
	private static final int MERLOT = 34018;
	// Items
	private static final int MARK_OF_ADVANCE = 46058;
	private static final int KURTIZ_CERTIFICATE = 46057;
	private static final int MAMMON_CERTIFICATE = 45635;
	private static final int GUSTAV_CERTIFICATE = 45636;
	private static final int PROOF_OF_PREPARATION = 80824;
	private static final ItemHolder LIONEL_HUNTERS_LIST_PART_3 = new ItemHolder(45637, 1);
	// Boss
	private static final int[] DIM_RAID_RBS =
	{
		29283, // Vengeful Lerazia (Gludin - South gate)
		29284, // Vengeful Edaire (Gludin - West gate)
		29285, // Vengeful Agonia (Gludin - North gate)
		29278, // Vengeful Agarez (Gludio - North Gate)
		29279, // Vengeful Oretross (Gludio - West Gate)
		29280, // Vengeful Eligos (Gludio - South Gate)
		29268, // Crook The Mad (Giran - North Gate)
		29269, // Houpon The Warden (Giran - West Gate)
		29270, // Guillotine Of Death (Giran - South Gate)
		29263, // Harp (Aden - West Gate)
		29260, // Maliss (Aden - East Gate)
		29261, // Isadora (Aden - South Gate)
		29298, // Varbasion (Oren - North Gate)
		29299, // Varmonia (Oren - East Gate)
		29300, // Varkaron (Oren - South Gate)
		29273, // Lindvior Herald Numa (Goddard - East Gate)
		29274, // Valakas Herald Potigia (Goddard - North Gate)
		29275, // Antharas Herald Komabor (Goddard - West Gate)
		29288, // Nerva Orc Nergatt (Schuttgart - West Gate)
		29289, // Amden Orc Turation (Schuttgart - South Gate)
		29290, // Nerva Chief Turakan (Schuttgart - East Gate)
		29293, // Theor (Gainak - South West Gate)
		29294, // Tebird (Gainak - West Gate)
		29295 // Tegaffe (Gainak - North Gate)
	};
	// Mobs
	private static final int[] MONSTERS =
	{
		// Atelia Refinery
		24144, // Death Rogue
		24145, // Death Shooter
		24146, // Death Warrior
		24147, // Death Sorcerer
		24148, // Death Pondus
		24149, // Devil Nightmare
		24150, // Devil Warrior
		24151, // Devil Guardian
		24152, // Devil Sinist
		24153, // Devil Varos
		24154, // Demonic Wizard
		24155, // Demonic Warrior
		24156, // Demonic Archer
		24157, // Demonic Keras
		24158, // Demonic Weiss
		
		// Enchanted Valley
		23566, // Nymph rose
		23567, // Nymph rose
		23568, // Nymph Lily
		23569, // Nymph Lily
		23570, // Nymph Tulip
		23571, // Nymph Tulip
		23572, // Nymph Astra
		23573, // Nymph Astra
		23578, // Nymph Sentinel
		23581 // Afros
	};
	// Rewards
	private static final int MERLOT_CERTIFICATE = 46056;
	private static final int SPELLBOOK_BLESSING_OF_THE_EXALTED = 45926;
	// Misc
	private static final int MIN_LEVEL = 100;
	private static final int PROOF_OF_PREPARATION_NEEDED = 20000;
	
	public Q10824_ConfrontingTheGreatestDanger()
	{
		super(10824);
		addStartNpc(MERLOT);
		addTalkId(MERLOT);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "34018-02.html");
		addCondStartedQuest(Q10823_ExaltedOneWhoShattersTheLimit.class.getSimpleName(), "34018-03.html");
		registerQuestItems(MARK_OF_ADVANCE, PROOF_OF_PREPARATION);
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
			case "34018-04.htm":
			case "34018-05.htm":
			case "34018-07.html":
			{
				htmltext = event;
				break;
			}
			case "34018-06.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34018-11.htm":
			{
				if (qs.isCond(1))
				{
					qs.setCond(3, true);
				}
				htmltext = event;
				break;
			}
			case "34018-09.html":
			{
				if (qs.isCond(2))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						if (hasQuestItems(player, KURTIZ_CERTIFICATE, MAMMON_CERTIFICATE, GUSTAV_CERTIFICATE))
						{
							htmltext = "34018-10.html";
						}
						else
						{
							htmltext = event;
						}
						takeItems(player, MARK_OF_ADVANCE, -3);
						giveItems(player, MERLOT_CERTIFICATE, 1);
						giveItems(player, SPELLBOOK_BLESSING_OF_THE_EXALTED, 1);
						qs.exitQuest(false, true);
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
				break;
			}
			case "34018-13.html":
			{
				if (qs.isCond(4))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						if (hasQuestItems(player, KURTIZ_CERTIFICATE, MAMMON_CERTIFICATE, GUSTAV_CERTIFICATE))
						{
							htmltext = "34018-14.html";
						}
						else
						{
							htmltext = event;
						}
						takeItems(player, PROOF_OF_PREPARATION, PROOF_OF_PREPARATION_NEEDED);
						giveItems(player, MERLOT_CERTIFICATE, 1);
						giveItems(player, SPELLBOOK_BLESSING_OF_THE_EXALTED, 1);
						qs.exitQuest(false, true);
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
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
				if (hasItem(player, LIONEL_HUNTERS_LIST_PART_3))
				{
					htmltext = "34018-01.htm";
				}
				else
				{
					htmltext = "noItem.html";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "34018-07.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "34018-08.html";
				}
				else if (qs.isCond(3))
				{
					htmltext = "34018-11.htm";
				}
				else if (qs.isCond(4))
				{
					htmltext = "34018-12.html";
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
		if ((qs != null) && qs.isCond(3) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE) && CommonUtil.contains(MONSTERS, npc.getId()))
		{
			giveItems(player, PROOF_OF_PREPARATION, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if (getQuestItemsCount(player, PROOF_OF_PREPARATION) >= PROOF_OF_PREPARATION_NEEDED)
			{
				qs.setCond(4, true);
			}
		}
		else if ((qs != null) && qs.isCond(1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE) && CommonUtil.contains(DIM_RAID_RBS, npc.getId()))
		{
			giveItems(player, MARK_OF_ADVANCE, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if (getQuestItemsCount(player, MARK_OF_ADVANCE) >= 3)
			{
				qs.setCond(2, true);
			}
		}
	}
}