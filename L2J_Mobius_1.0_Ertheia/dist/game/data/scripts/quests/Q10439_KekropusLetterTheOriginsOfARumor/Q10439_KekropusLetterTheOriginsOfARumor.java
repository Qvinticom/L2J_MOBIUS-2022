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
package quests.Q10439_KekropusLetterTheOriginsOfARumor;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.LetterQuest;

/**
 * Kekropus' Letter: The Origins of a Rumor (10439)
 * @author Stayway
 */
public class Q10439_KekropusLetterTheOriginsOfARumor extends LetterQuest
{
	// NPCs
	private static final int GOSTA = 30916;
	private static final int HELVETICA = 32641;
	private static final int ATHENIA = 32643;
	private static final int INVISIBLE_NPC = 19543;
	// Items
	private static final int SOE_HEINE = 37112; // Scroll of Escape: Heine
	private static final int SOE_FIELD_OF_SILENCE = 37039; // Scroll of Escape: Field of Silence
	private static final int SOE_FIELD_OF_WISPERS = 37040; // Scroll of Escape: Field of Wispers
	private static final int EWS = 959; // Scroll: Enchant Weapon (S-grade)
	private static final int EAS = 960; // Scroll: Enchant Armor (S-grade)
	// Location
	private static final Location TELEPORT_LOC = new Location(108457, 221649, -3598);
	// Misc
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	
	public Q10439_KekropusLetterTheOriginsOfARumor()
	{
		super(10439);
		addTalkId(GOSTA, HELVETICA, ATHENIA);
		addSeeCreatureId(INVISIBLE_NPC);
		setIsErtheiaQuest(false);
		setLevel(MIN_LEVEL, MAX_LEVEL);
		setStartQuestSound("Npcdialog1.kekrops_quest_9");
		setStartLocation(SOE_HEINE, TELEPORT_LOC);
		registerQuestItems(SOE_HEINE, SOE_FIELD_OF_SILENCE, SOE_FIELD_OF_WISPERS);
		addCondInCategory(CategoryType.MAGE_CLOACK, "nocond.html");
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
			case "30916-02.html":
			case "30916-03.html":
			{
				htmltext = event;
				break;
			}
			case "30916-04.html":
			{
				if (qs.isCond(1))
				{
					giveItems(player, SOE_FIELD_OF_SILENCE, 1);
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "30916-06.html":
			{
				if (qs.isCond(1))
				{
					giveItems(player, SOE_FIELD_OF_WISPERS, 1);
					qs.setCond(3, true);
					htmltext = event;
				}
				break;
			}
			case "32641-02.html":
			{
				if (qs.isCond(2))
				{
					qs.exitQuest(false, true);
					giveItems(player, EWS, 1);
					giveItems(player, EAS, 10);
					giveStoryQuestReward(player, 235);
					if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL))
					{
						addExpAndSp(player, 1_412_040, 338);
					}
					htmltext = event;
				}
				break;
			}
			case "32642-02.html":
			{
				if (qs.isCond(3))
				{
					qs.exitQuest(false, true);
					giveItems(player, EWS, 1);
					giveItems(player, EAS, 10);
					giveStoryQuestReward(player, 235);
					if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL))
					{
						addExpAndSp(player, 1_412_040, 338);
					}
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		if (qs.isStarted())
		{
			giveItems(player, SOE_HEINE, 1);
			if ((npc.getId() == GOSTA) && qs.isCond(1))
			{
				htmltext = "30916-01.html";
			}
			else if (qs.isCond(2))
			{
				htmltext = npc.getId() == GOSTA ? "30916-05.html" : "32641-01.html";
			}
			else if (qs.isCond(3))
			{
				htmltext = npc.getId() == GOSTA ? "30917-07.html" : "32642-01.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSeeCreature(Npc npc, Creature creature, boolean isSummon)
	{
		if (creature.isPlayer())
		{
			final PlayerInstance player = creature.getActingPlayer();
			final QuestState qs = getQuestState(player, false);
			if ((qs != null) && qs.isCond(2))
			{
				showOnScreenMsg(player, NpcStringId.FIELD_OF_SILENCE_AND_FIELD_OR_WHISPERS_ARE_GOOD_HUNTING_ZONES_FOR_LV_81_OR_ABOVE, ExShowScreenMessage.TOP_CENTER, 6000);
			}
			else if ((qs != null) && qs.isCond(3))
			{
				showOnScreenMsg(player, NpcStringId.FIELD_OF_SILENCE_AND_FIELD_OR_WHISPERS_ARE_GOOD_HUNTING_ZONES_FOR_LV_81_OR_ABOVE, ExShowScreenMessage.TOP_CENTER, 6000);
			}
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	@Override
	public boolean canShowTutorialMark(PlayerInstance player)
	{
		return player.isInCategory(CategoryType.MAGE_CLOACK);
	}
}