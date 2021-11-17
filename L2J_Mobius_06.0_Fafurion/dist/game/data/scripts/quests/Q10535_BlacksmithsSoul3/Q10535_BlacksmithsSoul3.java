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
package quests.Q10535_BlacksmithsSoul3;

import java.util.List;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10356_BlacksmithsSoul2.Q10356_BlacksmithsSoul2;

/**
 * @author Sero
 */
public class Q10535_BlacksmithsSoul3 extends Quest
{
	// NPCs
	private static final int BLACKSMITH_MAMMON = 31126;
	private static final int SHADAI = 32347;
	private static final int ISHUMA = 32615;
	// Monsters
	private static final int[] HELL_MONSTERS =
	{
		23386, // Jabberwok
		23387, // Kanzaroth
		23388, // Kandiloth
		23384, // Smaug
		23385, // Lunatikan
		23399, // Bend Beetle
		23398, // Koraza
		23397, // Desert Wendigo
		23395, // Garion
		23396, // Garion neti
	};
	private static final int[] CAVE_MONSTERS =
	{
		23727, // Shaqrima Bathus
		23728, // Shaqrima Carcass
		23729, // Kshana
	};
	// Items
	private static final int OREWITH_GIANTS_ENERGY = 47892;
	private static final int CRYSTAL_WITH_MAGOCAL_POWER = 47891;
	private static final int ENCHANTED_SHADOW_INGOT = 47886;
	// Misc
	private static final int MAIN_LEVEL = 99;
	
	public Q10535_BlacksmithsSoul3()
	{
		super(10535);
		addStartNpc(BLACKSMITH_MAMMON);
		addTalkId(BLACKSMITH_MAMMON, SHADAI, ISHUMA);
		addKillId(HELL_MONSTERS);
		addKillId(CAVE_MONSTERS);
		addCondCompletedQuest(Q10356_BlacksmithsSoul2.class.getSimpleName(), "31126-02.htm");
		addCondMinLevel(MAIN_LEVEL, getNoQuestMsg(null));
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case BLACKSMITH_MAMMON:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = "31126-00.htm";
						qs.startQuest();
						break;
					}
					case State.STARTED:
					{
						if (qs.isCond(9))
						{
							htmltext = "31126-01.htm";
							addExpAndSp(player, 40346120829L, 36311508);
							giveItems(player, ENCHANTED_SHADOW_INGOT, 1);
							qs.exitQuest(false, true);
						}
						break;
					}
					case State.COMPLETED:
					{
						if (!qs.isNowAvailable())
						{
							htmltext = "31126-00a.htm";
							break;
						}
					}
				}
				break;
			}
			case SHADAI:
			{
				switch (qs.getState())
				{
					case State.STARTED:
					{
						if (qs.isCond(7) && (getQuestItemsCount(player, CRYSTAL_WITH_MAGOCAL_POWER) >= 500) && (getQuestItemsCount(player, OREWITH_GIANTS_ENERGY) >= 500))
						{
							htmltext = "32347-00.htm";
							takeItems(player, CRYSTAL_WITH_MAGOCAL_POWER, -1);
							qs.setCond(8);
						}
						break;
					}
					case State.COMPLETED:
					{
						if (!qs.isNowAvailable())
						{
							htmltext = "31126-00a.htm";
							break;
						}
					}
				}
				break;
			}
			case ISHUMA:
			{
				switch (qs.getState())
				{
					case State.STARTED:
					{
						if (qs.isCond(8) && (getQuestItemsCount(player, OREWITH_GIANTS_ENERGY) >= 500))
						{
							htmltext = "32615-00.htm";
							takeItems(player, OREWITH_GIANTS_ENERGY, -1);
							qs.setCond(9);
						}
						break;
					}
					case State.COMPLETED:
					{
						if (!qs.isNowAvailable())
						{
							htmltext = "31126-00a.htm";
							break;
						}
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (killer.isInParty())
		{
			final Party party = killer.getParty();
			final List<Player> partyMember = party.getMembers();
			for (Player singleMember : partyMember)
			{
				final QuestState qsPartyMember = getQuestState(singleMember, false);
				final double distance = npc.calculateDistance3D(singleMember);
				if ((qsPartyMember != null) && (distance <= 1000))
				{
					if (qsPartyMember.isCond(1))
					{
						if (CommonUtil.contains(HELL_MONSTERS, npc.getId()) && (getQuestItemsCount(singleMember, CRYSTAL_WITH_MAGOCAL_POWER) < 500))
						{
							giveItems(singleMember, CRYSTAL_WITH_MAGOCAL_POWER, 1);
							playSound(singleMember, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					if (qsPartyMember.isCond(1))
					{
						if (CommonUtil.contains(CAVE_MONSTERS, npc.getId()) && (getQuestItemsCount(singleMember, OREWITH_GIANTS_ENERGY) < 500))
						{
							giveItems(singleMember, OREWITH_GIANTS_ENERGY, 1);
							playSound(singleMember, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					if (qsPartyMember.isCond(1) && (getQuestItemsCount(singleMember, OREWITH_GIANTS_ENERGY) >= 500) && (getQuestItemsCount(singleMember, CRYSTAL_WITH_MAGOCAL_POWER) >= 500))
					{
						qsPartyMember.setCond(7);
					}
				}
			}
		}
		else
		{
			final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
			if (qs != null)
			{
				if (qs.isCond(1))
				{
					if (CommonUtil.contains(HELL_MONSTERS, npc.getId()) && (getQuestItemsCount(killer, CRYSTAL_WITH_MAGOCAL_POWER) < 500))
					{
						giveItems(killer, CRYSTAL_WITH_MAGOCAL_POWER, 1);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						
					}
				}
				if (qs.isCond(1))
				{
					if (CommonUtil.contains(CAVE_MONSTERS, npc.getId()) && (getQuestItemsCount(killer, OREWITH_GIANTS_ENERGY) < 500))
					{
						giveItems(killer, OREWITH_GIANTS_ENERGY, 1);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				if (qs.isCond(1) && (getQuestItemsCount(killer, OREWITH_GIANTS_ENERGY) >= 500) && (getQuestItemsCount(killer, CRYSTAL_WITH_MAGOCAL_POWER) >= 500))
				{
					qs.setCond(7);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
